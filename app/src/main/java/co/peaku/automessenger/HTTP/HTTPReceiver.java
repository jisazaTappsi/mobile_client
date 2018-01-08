package co.peaku.automessenger.HTTP;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class HTTPReceiver extends ResultReceiver {
    private Receiver receiver;

    public HTTPReceiver(Handler handler) {
        super(handler);
    }
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }


    /**
     * overrides the receiver and adds !null condition.
     * @param resultCode code received.
     * @param resultData all data contained.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null)
            receiver.onReceiveResult(resultCode, resultData);
    }
}

