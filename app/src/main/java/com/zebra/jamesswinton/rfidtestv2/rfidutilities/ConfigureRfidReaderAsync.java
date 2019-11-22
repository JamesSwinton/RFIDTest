package com.zebra.jamesswinton.rfidtestv2.rfidutilities;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.zebra.jamesswinton.rfidtestv2.MainActivity;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.TriggerInfo;

import java.lang.ref.WeakReference;

import static com.zebra.rfid.api3.ENUM_TRIGGER_MODE.RFID_MODE;
import static com.zebra.rfid.api3.START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE;
import static com.zebra.rfid.api3.STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE;

public class ConfigureRfidReaderAsync extends AsyncTask<Void, Void, Void> {

    // Debugging
    private static final String TAG = "ConfigRfidReaderAsync";

    // Constants


    // Private Variables
    private RFIDReader mRfidReader;
    private RfidEventsListener mRfidEventsListener;
    private GetRfidReaderCallback mGetRfidReaderCallback;
    private WeakReference<Context> mContextWeakReference;

    // Public Variables


    /**
     * Constructor
     */

    ConfigureRfidReaderAsync(WeakReference<Context> contextWeakReference, RFIDReader rfidReader,
                             RfidEventsListener rfidEventsListener, GetRfidReaderCallback getRfidReaderCallback) {
        this.mRfidReader = rfidReader;
        this.mRfidEventsListener = rfidEventsListener;
        this.mGetRfidReaderCallback = getRfidReaderCallback;
        this.mContextWeakReference = contextWeakReference;
    }

    /**
     * Async Methods
     */

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "Configuring RFID Reader: " + mRfidReader.getHostName());
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mRfidReader == null) {
            mGetRfidReaderCallback.onError("No RFID Reader available to Configure");
            return null;
        }

        if (!mRfidReader.isConnected()) {
            mGetRfidReaderCallback.onError("RFID Reader not connected - unable to Configure");
            return null;
        }

        // Configure RFID Reader
        configureRfidReader();

        // Finish Operation
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.i(TAG, "Configuration Complete");
    }

    /**
     * Support Methods
     */

    private void configureRfidReader() {
        // Build Trigger Info Settings
        TriggerInfo rfidReaderTriggerConfig = new TriggerInfo();
        rfidReaderTriggerConfig.StartTrigger.setTriggerType(START_TRIGGER_TYPE_IMMEDIATE);
        rfidReaderTriggerConfig.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE_IMMEDIATE);

        try {
            // Add Events Listener (For Read / Status Events)
            mRfidReader.Events.addEventsListener(mRfidEventsListener);

            // Configure Events
            mRfidReader.Events.setAttachTagDataWithReadEvent(true);
//            mRfidReader.Events.setBatchModeEvent();
//            mRfidReader.Events.setBatteryEvent();
//            mRfidReader.Events.setBufferFullEvent();
            mRfidReader.Events.setHandheldEvent(true);
//            mRfidReader.Events.setInventoryStartEvent();
//            mRfidReader.Events.setInventoryStopEvent();
//            mRfidReader.Events.setOperationEndSummaryEvent();
//            mRfidReader.Events.setPowerEvent();
//            mRfidReader.Events.setReaderDisconnectEvent();
//            mRfidReader.Events.setReaderExceptionEvent();
//            mRfidReader.Events.setRfidConnectionState();
            mRfidReader.Events.setTagReadEvent(true);
//            mRfidReader.Events.setTemperatureAlarmEvent();

            // Configure Config
//            mRfidReader.Config.setAccessOperationWaitTimeout();
//            mRfidReader.Config.setAttribute();
//            mRfidReader.Config.setBatchMode();
            mRfidReader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
//            mRfidReader.Config.setDefaultConfigurations();
//            mRfidReader.Config.setDPOState();
//            mRfidReader.Config.setDutyCycleIndex();
//            mRfidReader.Config.setLedBlinkEnable();
//            mRfidReader.Config.setLogLevel();
//            mRfidReader.Config.setRadioPowerState();
//            mRfidReader.Config.setRegulatoryConfig();
            mRfidReader.Config.setStartTrigger(rfidReaderTriggerConfig.StartTrigger);
            mRfidReader.Config.setStopTrigger(rfidReaderTriggerConfig.StopTrigger);
//            mRfidReader.Config.setTagStorageSettings();
//            mRfidReader.Config.setTraceLevel();
//            mRfidReader.Config.setTraceLevel();
            mRfidReader.Config.setTriggerMode(RFID_MODE, true);
            mRfidReader.Config.setUniqueTagReport(true);

            // Configure Power
            int maxPower = mRfidReader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
            int preferredPower = PreferenceManager
                    .getDefaultSharedPreferences(mContextWeakReference.get())
                    .getInt(MainActivity.READ_POWER_PREFERENCE, maxPower);

            Antennas antennas = mRfidReader.Config.Antennas;
            Antennas.AntennaRfConfig antennaRfConfig = antennas.getAntennaRfConfig(1);
            antennaRfConfig.setTransmitPowerIndex(preferredPower);
            antennaRfConfig.setrfModeTableIndex(0);
            antennaRfConfig.setTari(0);
            antennas.setAntennaRfConfig(1, antennaRfConfig);

            // Configure Singulation
            Antennas.SingulationControl singulationControl = mRfidReader.Config.Antennas
                    .getSingulationControl(1);
            singulationControl.setSession(SESSION.SESSION_S0);
            singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
            mRfidReader.Config.Antennas.setSingulationControl(1, singulationControl);

            // Remove Pre-Filters
            mRfidReader.Actions.PreFilters.deleteAll();

            // Notify Config Complete
            mGetRfidReaderCallback.onReaderConfigured(mRfidReader);

        } catch (InvalidUsageException | OperationFailureException e) {
            mGetRfidReaderCallback.onError("Error during Configuration: "
                    + e.getMessage());
        }
    }

}
