package co.peaku.automessenger;

import android.content.Context;
import android.app.AlertDialog;


public class AlertDialogs {

    /**
     * Outputs a simple message.
     */
    public static AlertDialog.Builder getSimpleMessage(Context context, String message){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);

        return builder;
    }

}
