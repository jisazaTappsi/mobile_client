package co.peaku.automessenger.HTTP;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import co.peaku.automessenger.Cts;
import co.peaku.automessenger.Cts.*;


public class HTTPService extends IntentService{

    private static final String TAG = "HTTPService";

    public HTTPService() {
        super(TAG);
    }


    /**
     * Place where intents are received.
     * @param intent received from main in this case.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

    //public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "on handle intent", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "on handle intent");


        final ResultReceiver receiver = intent.getParcelableExtra(RESTCts.RECEIVER);

        switch (intent.getIntExtra(RESTCts.COMMAND, 0)) {

            case RESTCts.COMMAND_GET_CONTACTS:

                getContacts(receiver, intent.getStringExtra(Cts.URL));
                break;

        }
    }


    /**
     * sends data back through the receiver.
     * @param receiver the receiver object from main activity.
     * @param url the url to fetch for data.
     */
    private void getContacts(ResultReceiver receiver, String url){
        Log.d(TAG, "Operation to getContacts has started.");
        int responseCode = HTTPCts.FIRST_CODE;

        String jsonResult = "";

        try {

            HttpURLConnection connection = getConnection(url, RESTCts.GET);
            responseCode = connection.getResponseCode();
            jsonResult = processRequest(connection);

            Log.d(TAG, "Operation to getContacts has finished.");
        } catch (Exception e) {

            Toast.makeText(this, "Operation to getContacts has failed: " + e, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Operation to getContacts has failed: " + e);
        }

        Bundle data = new Bundle();
        data.putString(Cts.JSON, jsonResult);

        receiver.send(responseCode, data);
    }


    /**
     * gets the connection object.
     * @param urlString the url to connect to
     * @return HttpURLConnection obj.
     */
    public HttpURLConnection getConnection(String urlString, String restMethod) throws IOException{

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(restMethod);
        connection.connect();

        return connection;
    }


    /**
     * Process the request and return a String with the json.
     */
    public String processRequest(HttpURLConnection connection) throws IOException{

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Here it may come with extra quote chars ("), removes them if present
        if (response.charAt(0) == '"') {
            response.deleteCharAt(0);
        }
        if (response.charAt(response.length() - 1) == '"') {
            response.deleteCharAt(response.length() - 1);
        }

        // removes any extra backslashes (they violate json format)
        return response.toString().replace("\\", "");
    }
}
