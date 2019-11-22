package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;


import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.Readers.RFIDReaderEventHandler;

import java.lang.ref.WeakReference;
import java.util.List;

public class GetRfidReaderAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "GetReadersAsync";

    // Constants


    // Private Variables
    private WeakReference<Context> mContextWeakReference;
    private RFIDReaderEventHandler mRfidReaderEventHandler;
    private GetRfidReaderCallback mGetRfidReaderCallback;

    private Readers mReaders;
    private RFIDReader mRfidReader; // Provides the RFIDReader object for performing actions

    // Public Variables


    /**
     * Constructor
     */

    public GetRfidReaderAsync(WeakReference<Context> contextWeakReference,
                              @NonNull Readers readers,
                              RFIDReaderEventHandler rfidReaderEventHandler,
                              GetRfidReaderCallback getRfidReaderCallback) {
        this.mContextWeakReference = contextWeakReference;
        this.mReaders = readers;
        this.mRfidReaderEventHandler = rfidReaderEventHandler;
        this.mGetRfidReaderCallback = getRfidReaderCallback;
    }

    /**
     * Async Methods
     */

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "Connecting to reader");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Get RFID Reader from Available Readers
        RFIDReader rfidReader = getRfidReaderFromAvailableReaders();

        // Connect to Reader if we have it
        if (rfidReader != null) {
            mGetRfidReaderCallback.onRfidReaderFound(rfidReader);
        }

        // Exit
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i(TAG, "RFID Reader Connection Complete");
    }

    /**
     * Support Methods
     */

    private RFIDReader getRfidReaderFromAvailableReaders() {
        // Attach ReaderEventHandler to RFID Readers to Listen for new / gone Readers
        mReaders.attach(mRfidReaderEventHandler);

        try {
            // Get Available Readers
            List<ReaderDevice> availableRfidReaders = mReaders.GetAvailableRFIDReaderList();

            // Handle Available Readers
            if (!availableRfidReaders.isEmpty()) {
                // Create Holder
                RFIDReader rfidReader = null;

                // Get Readers: If only one reader, get reader. If multiple, get reader by name
                if (availableRfidReaders.size() == 1) {
                    Log.i(TAG, "Found One Reader: " + availableRfidReaders.get(0).getName());
                    rfidReader = availableRfidReaders.get(0).getRFIDReader();
                } else  {
                    Log.i(TAG, "Found Multiple RFID Readers");
                    for (ReaderDevice rfidReaderDevice : availableRfidReaders) {
                        Log.i(TAG, "RFID Reader found: " + rfidReaderDevice.getName());
                        // TODO: Handle Multiple Readers
                    }
                }

                // Return RFIDReader
                return rfidReader;

            } else {
                // No Available Readers -> Notify Calling Class
                mGetRfidReaderCallback.onError("No Available Readers");

                // Return Null (Don't continue connection)
                return null;
            }

        } catch (InvalidUsageException e) {
            // Notify Calling Class
            mGetRfidReaderCallback.onError("No Available Readers: "
                    + e.getVendorMessage());

            // Return Null (Don't continue connection)
            return null;
        }
    }
}
