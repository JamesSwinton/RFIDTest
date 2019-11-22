package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.Readers;

import java.lang.ref.WeakReference;

import static com.zebra.rfid.api3.ENUM_TRANSPORT.SERVICE_SERIAL;

public class GetReadersAsync extends AsyncTask<Void, Void, Readers> {

    // Debugging
    private static final String TAG = "GetReadersAsync";

    // Constants


    // Private Variables
    private WeakReference<Context> mContextWeakReference;
    private GetRfidReaderCallback mGetRfidReaderCallback;

    // Public Variables


    /**
     * Constructor
     */

    GetReadersAsync(WeakReference<Context> contextWeakReference, GetRfidReaderCallback rfidReaderCallback) {
        this.mContextWeakReference = contextWeakReference;
        this.mGetRfidReaderCallback = rfidReaderCallback;
    }

    /**
     * Async Methods
     */

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "Getting Available Readers");
    }

    @Override
    protected Readers doInBackground(Void... voids) {
        // Get Readers
        return getBluetoothOrSerialRfidReaders();
    }

    @Override
    protected void onPostExecute(Readers readers) {
        super.onPostExecute(readers);

        // Validate Readers
        if (readers == null) {
            mGetRfidReaderCallback.onError("No Readers Returned.");
            return;
        }

        // Notify Calling Class && Pass Readers
        mGetRfidReaderCallback.onReadersFound(readers);
    }

    /**
     * Support Methods
     */

    private Readers getBluetoothOrSerialRfidReaders() {
        // Verify Activity Context
        Context context = mContextWeakReference.get();
        if (context == null) {
            Log.e(TAG, "Invalid context, quitting");
            return null;
        }

        // Get Readers
        Readers readers = new Readers(context, SERVICE_SERIAL);

        // Get Reader (Serial or Bluetooth)
        try {

            // Check Available Serial Readers -> If no exception is thrown, we can continue.
            readers.GetAvailableRFIDReaderList();

        } catch (InvalidUsageException e) {
            // Exception thrown because reader does not support SERVICE_SERIAL
            Log.w(TAG,"InvalidUsageException: " + e.getInfo());
            Log.i(TAG,"Serial Service Not Supported - Attempting Bluetooth Instead");

            // Dispose Readers
            readers.Dispose();

            // Get Bluetooth Readers
            readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
        }

        // Return Readers for connection
        return readers;
    }
}
