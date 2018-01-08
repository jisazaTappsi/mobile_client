package co.peaku.automessenger;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import co.peaku.automessenger.HTTP.HTTPReceiver;
import co.peaku.automessenger.HTTP.HTTPService;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_CONTACTS = 100;

    private String url_base;
    private String url_send_contacts;
    private Activity context = this;

    public HTTPReceiver receiver;
    private HTTPReceiver.Receiver getContactsReceiver = new HTTPReceiver.Receiver() {


        /**
         * Receives the result from the http service, upon that results fills in the data on the
         * objects or display a error message.
         * @param resultCode http code.
         * @param resultData data.
         */
        @Override
        public void onReceiveResult(int resultCode, Bundle resultData) {

            String jsonAsString = (String)resultData.get(Cts.JSON);

            //failure
            if(failedRequest(resultCode)) {
                AlertDialogs.getSimpleMessage(context, getFailureString(resultCode)).create().show();
            }else{//success :)
                addContacts(jsonAsString);
                AlertDialogs.getSimpleMessage(context, "Contacts added successfully to phone book").create().show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url_base = getResources().getString(R.string.url_base);
        url_send_contacts = getResources().getString(R.string.url_send_contacts);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_WRITE_CONTACTS);
            //then wait for callback in onRequestPermissionsResult(int, String[], int[]) overridden method
        } else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            requestNewContacts();
        }
    }


    /**
     * Adds contacts if permission granted.
     * @param requestCode: received code, sent on requestPermission() call.
     * @param permissions: do not know
     * @param grantResults: check permission grant
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                requestNewContacts();
            } else {
                Toast.makeText(this, "Permission NOT Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Asks for new users to add.
     */
    private void requestNewContacts(){
        receiver = new HTTPReceiver(new Handler());
        receiver.setReceiver(getContactsReceiver);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, HTTPService.class);
        intent.putExtra(Cts.RESTCts.RECEIVER, receiver);
        intent.putExtra(Cts.RESTCts.COMMAND, Cts.RESTCts.COMMAND_GET_CONTACTS);
        intent.putExtra(Cts.URL, url_base + url_send_contacts);
        startService(intent);
        Toast.makeText(this, "Sent message intent", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param jsonString: string
     */
    private void addContacts(String jsonString) {

        try {
            JSONArray jsonarray = new JSONArray(jsonString);
            for(int i=0; i < jsonarray.length(); i++){

                JSONObject user = jsonarray.getJSONObject(i);
                JSONObject fields = user.getJSONObject("fields");

                String pk = user.getString("pk");
                String name = fields.getString("name");
                String phone = fields.getString("phone");
                String email = fields.getString("email");

                addContactToPhoneBook(pk, name, phone, email);
            }
        }catch (org.json.JSONException e){
            Toast.makeText(this, "Cannot parse jsonArray on addContacts", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Adds a new contact to the phone book.
     * @param name: contact name
     * @param phone: contact phone
     */
    private void addContactToPhoneBook(String pk, String name, String phone, String email) {
        ArrayList<ContentProviderOperation> op_list = new ArrayList<>();
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // first and last names
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, pk)
                .build());

        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                .build());
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                .build());

        try{
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * decides between a successful or failed request.
     * @param code http response code.
     * @return boolean value.
     */
    private boolean failedRequest(int code){
        return code != Cts.HTTPCts.OK && code != Cts.HTTPCts.FIRST_CODE;
    }


    /**
     * The message to display on a alert, given a http code.
     * @param code http response code.
     * @return message to display on alert.
     */
    private String getFailureString(int code){
        return "Error completing request with HTTP code: " +
                String.valueOf(code) +
                ", please see https://en.wikipedia.org/wiki/List_of_HTTP_status_codes";
    }
}