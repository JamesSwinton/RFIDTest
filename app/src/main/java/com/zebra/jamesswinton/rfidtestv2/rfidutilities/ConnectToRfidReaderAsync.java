package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.RegionInfo;
import com.zebra.rfid.api3.RegulatoryConfig;

import static com.zebra.rfid.api3.RFIDResults.RFID_BATCHMODE_IN_PROGRESS;
import static com.zebra.rfid.api3.RFIDResults.RFID_CONNECTION_PASSWORD_ERROR;
import static com.zebra.rfid.api3.RFIDResults.RFID_READER_REGION_NOT_CONFIGURED;

public class ConnectToRfidReaderAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "ConnectRfidReaderAsync";

    // Constants


    // Private Variables
    private RFIDReader mRfidReader;
    private GetRfidReaderCallback mGetRfidReaderCallback;

    // Public Variables


    /**
     * Constructor
     */

    ConnectToRfidReaderAsync(RFIDReader rfidReader, GetRfidReaderCallback getRfidReaderCallback) {
        this.mRfidReader = rfidReader;
        this.mGetRfidReaderCallback = getRfidReaderCallback;
    }

    /**
     * Async Methods
     */

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Connect to Reader
        if (mRfidReader != null) {
            connectToRfidReader(mRfidReader);
        } else {
            mGetRfidReaderCallback.onError("No RFID Reader available to connect to");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i(TAG, "Connect to RFID reader Complete");
    }

    /**
     * Handle Connection
     */

    private void connectToRfidReader(@NonNull RFIDReader rfidReader) {
        // Log connection Attempt
        Log.i(TAG, "Connecting To RFID Reader: " + rfidReader.getHostName());

        // Validate connection State
        if (!rfidReader.isConnected()) {
            try {
                // Attempt connection
                rfidReader.connect();

                // Notify Connection Complete
                mGetRfidReaderCallback.onReaderConnected(rfidReader);
            } catch (OperationFailureException e) {
                // Handle Special Connection Failures
                handleSpecialConnectionUseCases(e.getResults(), rfidReader);
            } catch (InvalidUsageException e) {
                // Notify Calling Class
                mGetRfidReaderCallback.onError("Invalid Usage Exception | " +
                        "Error Connecting to Reader: " + e.getVendorMessage());
            }
        } else {
            Log.i(TAG, "RFID Reader Already Connected");
        }
    }

    /**
     * Special Connection Use-Cases
     */

    private void handleSpecialConnectionUseCases(RFIDResults rfidResults, RFIDReader rfidReader) {
        // Handle Special Cases
        if (rfidResults == RFID_READER_REGION_NOT_CONFIGURED) {
            getAndSetRegulatoryConfigSettings(rfidReader);
        } else if (rfidResults == RFID_CONNECTION_PASSWORD_ERROR) {
            requestAndApplyBluetoothPassword(rfidReader);
        } else if (rfidResults ==  RFID_BATCHMODE_IN_PROGRESS) {
            handleReaderInBatchMode(rfidReader);
        } else {
            // Unhandled Exception -> Notify Calling Class
            mGetRfidReaderCallback.onError(
                    "Operation Failure Exception | Error Connecting to Reader: "
                            + rfidResults.toString());
        }
    }

    // Configure Regulatory Settings
    private void getAndSetRegulatoryConfigSettings(RFIDReader rfidReader) {
        try {
            // Get Config & Region Info
            RegulatoryConfig regulatoryConfig = rfidReader.Config.getRegulatoryConfig();
            RegionInfo regionInfo = rfidReader.ReaderCapabilities.SupportedRegions.getRegionInfo(1);

            // Set Region in Regulatory Config
            regulatoryConfig.setRegion(regionInfo.getRegionCode());

            // Apply Regulatory Config to Reader
            rfidReader.Config.setRegulatoryConfig(regulatoryConfig);

            // Re-attempt connect
            rfidReader.connect();

            // Notify Connection Complete
            mGetRfidReaderCallback.onReaderConnected(rfidReader);
        } catch (InvalidUsageException e1) {
            // Notify Calling Class
            mGetRfidReaderCallback.onError("Invalid Usage Exception | " +
                    "Error Connecting to Reader: " + e1.getVendorMessage());
        } catch (OperationFailureException e1) {
            // Unhandled Exception -> Notify Calling Class
            mGetRfidReaderCallback.onError(
                    "Operation Failure Exception | Error Connecting to Reader: "
                            + e1.getVendorMessage() + " " + e1.getResults().toString());
        }
    }

    // Set password in RFIDReader instance and then call connect again
    private void requestAndApplyBluetoothPassword(RFIDReader rfidReader) {
        mGetRfidReaderCallback.onError("Bluetooth Password Required for Connect");
    }

    // Set password in RFIDReader instance and then call connect again
    private void handleReaderInBatchMode(RFIDReader rfidReader) {
        mGetRfidReaderCallback.onError("Batch Mode Unhandled");
    }

}
