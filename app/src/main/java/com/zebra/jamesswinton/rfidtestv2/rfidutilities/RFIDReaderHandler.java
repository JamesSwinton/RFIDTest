package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.zebra.jamesswinton.rfidtestv2.MainActivity;
import com.zebra.jamesswinton.rfidtestv2.utilities.CustomDialog;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;

import java.lang.ref.WeakReference;

public class RFIDReaderHandler {

    // Debugging
    private static final String TAG = "RFIDReaderHandler";

    // Constants
    private static WeakReference<Context> mContextWeakReference;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    // Private Variables
    private Readers mReaders;
    private RfidEventsListener mRfidEventsListener;
    private InitialisationCallback mInitialisationCallback;

    // Public Variables
    public RFIDReader mRfidReader;

    /**
     * Constructor
     * @param context - Activity context
     * @param rfidEventsListener - Interface for RFID Read & Status Events
     */

    public RFIDReaderHandler(Context context, RfidEventsListener rfidEventsListener) {
        mContextWeakReference = new WeakReference<>(context);
        mRfidEventsListener = rfidEventsListener;
    }

    /**
     * Initialisation Methods
     */

    public void initialiseRFIDReader(InitialisationCallback initialisationCallback) {
        // Assign Callback to Member variable
        this.mInitialisationCallback = initialisationCallback;

        // Start Retrieval / Connection
        if (mReaders == null) {
            // Get Available Readers && RFIDReaders if available
            new GetReadersAsync(mContextWeakReference, mGetRfidReaderCallback).execute();
        } else {
            // Readers Already Available - Just get RFID Reader
            new GetRfidReaderAsync(mContextWeakReference, mReaders, mRfidReaderEventHandler,
                    mGetRfidReaderCallback).execute();
        }
    }

    /**
     * Interface(s)
     */

    private GetRfidReaderCallback mGetRfidReaderCallback = new GetRfidReaderCallback() {
        @Override
        public void onReadersFound(Readers readers) {
            Log.i(TAG, "GetRfidReaderCallback - onReadersFound");

            // Assign Readers to Member Variable
            mReaders = readers;

            // Get RFIDReader from Readers
            new GetRfidReaderAsync(mContextWeakReference, readers, mRfidReaderEventHandler,
                    mGetRfidReaderCallback).execute();
        }

        @Override
        public void onRfidReaderFound(RFIDReader rfidReader) {
            Log.i(TAG, "RFID Reader Found: " + rfidReader.getHostName());

            // Connect To Reader
            new ConnectToRfidReaderAsync(rfidReader, mGetRfidReaderCallback).execute();
        }

        @Override
        public void onReaderConnected(RFIDReader rfidReader) {
            Log.i(TAG, "RFID Reader Connected: " + rfidReader.getHostName());

            // Store Reader as Member Variables
            mRfidReader = rfidReader;

            // Configure Reader Once Connected
            new ConfigureRfidReaderAsync(mContextWeakReference, rfidReader, mRfidEventsListener,
                    mGetRfidReaderCallback).execute();
        }

        @Override
        public void onReaderConfigured(RFIDReader rfidReader) {
            Log.i(TAG, "RFID Reader Configured: " + rfidReader.getHostName());

            // notify Calling Class we're ready to go
            mInitialisationCallback.onInitialisationComplete();
        }

        @Override
        public void onError(String errorMessage) {
            Log.e(TAG, "GetRfidReaderCallback - onError: " + errorMessage);

            // Show Error Dialog
            mHandler.post(() -> CustomDialog.showCustomDialog(mContextWeakReference.get(),
                    CustomDialog.DialogType.ERROR, "RFID Reader Error", errorMessage));
        }
    };

    private Readers.RFIDReaderEventHandler mRfidReaderEventHandler = new Readers.RFIDReaderEventHandler() {
        @Override
        public void RFIDReaderAppeared(ReaderDevice readerDevice) {
            // New RFID Reader Appeared -> Get new Readers
            new GetReadersAsync(mContextWeakReference, mGetRfidReaderCallback).execute();
        }

        @Override
        public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
            // Disconnect from RFIDReader
            disconnectRfidReader();
        }
    };

    /**
     * Life Cycle Methods
     */

    public synchronized void connectToRfidReader() {
        if (mRfidReader != null) {
            new ConnectToRfidReaderAsync(mRfidReader, mGetRfidReaderCallback).execute();
        } else {
            Log.e(TAG, "Connection Attempt Aborted - No RFID Reader Available");
        }
    }

    public synchronized void disconnectRfidReader() {
        // Log Disconnection Attempt
        Log.d(TAG, "Disconnecting RFIDReader " + mRfidReader);

        if (mRfidReader != null) {
            try {
                mRfidReader.Events.removeEventsListener(mRfidEventsListener);
                mRfidReader.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Disconnection Error: " + e.getMessage());
            }
        }
    }

    public synchronized void dispose() {
        // Log Disconnection Attempt
        Log.d(TAG, "Disconnecting Readers");

        if (mReaders != null) {
            try {
                // Remove RFID Reader Instance
                mRfidReader = null;

                // Dispose & Remove Readers
                mReaders.Dispose();
                mReaders = null;
            } catch (Exception e) {
                Log.e(TAG, "Dispose Error: " + e.getMessage());
            }
        }
    }

    /**
     * Action Methods
     */

    public synchronized void performInventory() {
        if (!isReaderConnected()) {
            Log.e(TAG, "Cannot Start Inventory - RFID Reader is not connected");
            return;
        }

        // Stop Inventory on Background Thread
        AsyncTask.execute(() -> {
            try {
                mRfidReader.Actions.Inventory.perform();
            } catch (InvalidUsageException | OperationFailureException e) {
                mHandler.post(() -> Log.e(TAG, "Error Starting Inventory: " + e.getMessage()));
            }
        });
    }

    public synchronized void stopInventory() {
        if (!isReaderConnected()) {
            Log.e(TAG, "Cannot Stop Inventory - RFID Reader is not connected");
            return;
        }

        // Stop Inventory on Background Thread
        AsyncTask.execute(() -> {
            try {
                // Stop Inventory
                mRfidReader.Actions.Inventory.stop();

                // Purge Tags (To allow re-reading next trigger pull)
                mRfidReader.Actions.purgeTags();
            } catch (InvalidUsageException | OperationFailureException e) {
                mHandler.post(() -> Log.e(TAG, "Error Stopping Inventory: " + e.getMessage()));
            }
        });
    }

    private boolean isReaderConnected() {
        return mRfidReader != null && mRfidReader.isConnected();
    }

    /**
     * Configuration Methods
     */

    public int getCurrentPower() {
        try {
            return mRfidReader.Config.Antennas.getAntennaRfConfig(1).getTransmitPowerIndex();
        } catch (InvalidUsageException | OperationFailureException e) {
            return mRfidReader.ReaderCapabilities.getTransmitPowerLevelValues().length -1;
        }
    }

    public int getReaderMinPower() {
        return mRfidReader.ReaderCapabilities.getTransmitPowerLevelValues()[0] + 1;
    }

    public int getReaderMaxPower() {
        return mRfidReader.ReaderCapabilities.getTransmitPowerLevelValues().length -1;
    }

    public void setReaderPower(int power) {
        AsyncTask.execute(() -> {
            try {
                Antennas antennas = mRfidReader.Config.Antennas;
                Antennas.AntennaRfConfig antennaRfConfig = antennas.getAntennaRfConfig(1);
                antennaRfConfig.setTransmitPowerIndex(power);
                antennaRfConfig.setrfModeTableIndex(0);
                antennaRfConfig.setTari(0);
                antennas.setAntennaRfConfig(1, antennaRfConfig);
            } catch (InvalidUsageException | OperationFailureException e) {
                mGetRfidReaderCallback.onError("Error during Configuration: "
                        + e.getMessage());
            }
        });
    }
}

