package com.zebra.jamesswinton.rfidtestv2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.zebra.jamesswinton.rfidtestv2.databinding.ActivityMainBinding;
import com.zebra.jamesswinton.rfidtestv2.databinding.LayoutReaderPowerSeekbarBinding;
import com.zebra.jamesswinton.rfidtestv2.rfidutilities.InitialisationCallback;
import com.zebra.jamesswinton.rfidtestv2.rfidutilities.RFIDReaderHandler;
import com.zebra.jamesswinton.rfidtestv2.settings.SettingsActivity;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.List;

import static com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;
import static com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED;
import static com.zebra.rfid.api3.STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT;

public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Constants
    public static final String READ_POWER_PREFERENCE = "preference_rfid_read_power";

    // Private Variables
    private static List<TagInfo> mReadTags = new ArrayList<>();
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    // Public Variables
    private ActivityMainBinding mDataBinding;
    private RFIDReaderHandler mRFIDReaderHandler;

    private ReadTagsAdapter mReadTagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Configure Toolbar
        configureToolbar();

        // Initialise RFID Reader Handler
        mRFIDReaderHandler = new RFIDReaderHandler(this, mRfidEventsListener);
        mRFIDReaderHandler.initialiseRFIDReader(mInitialisationCompleteCallback);

        // Init Recycler View
        mReadTagsAdapter = new ReadTagsAdapter(this, mReadTags);
        mDataBinding.tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataBinding.tagsRecyclerView.setAdapter(mReadTagsAdapter);

        // Init RecyclerView Actions
        mDataBinding.clearTagsButton.setOnClickListener(view -> {
            Log.i(TAG, "Clearing Read Tags List");

            // Clear List
            mReadTags.clear();

            // Update Recycler View
            mReadTagsAdapter.notifyDataSetChanged();
        });

        mDataBinding.selectAllTagsButton.setOnClickListener(view -> {
            Log.i(TAG, "Selecting all Tags in List");

            // Update List
            for (TagInfo tagInfo : mReadTags) {
                tagInfo.setChecked(true);
            }

            // Refresh List
            mReadTagsAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRFIDReaderHandler.disconnectRfidReader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRFIDReaderHandler.connectToRfidReader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRFIDReaderHandler.dispose();
    }

    /**
     * Toolbar Methods
     */

    private void configureToolbar() {
        setSupportActionBar(mDataBinding.toolbarLayout.toolbar);
        mDataBinding.toolbarLayout.toolbar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle Navigation Events
        switch(item.getItemId()) {
//            case R.id.settings:
//                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//                break;
            case R.id.adjust_power:
                showRfidPowerDialog();
                break;
        } return true;
    }

    /**
     * Action Methods
     */

    private void handleTriggerPress(boolean pressed) {
        if (pressed) {
            mRFIDReaderHandler.performInventory();
        } else {
            mRFIDReaderHandler.stopInventory();
        }
    }

    private void addTagToList(TagData tagData) {

    }

    private void showRfidPowerDialog() {
        // Create View && DataBinding
        LayoutReaderPowerSeekbarBinding adjustPowerLayoutBinding = DataBindingUtil.inflate(
                getLayoutInflater(), R.layout.layout_reader_power_seekbar, null,
                false);

        // Get Min Power (default to 31 if less)
        final int minPower = mRFIDReaderHandler.getReaderMinPower() >= 31
                ? mRFIDReaderHandler.getReaderMinPower() : 31;

        // Set Max / Min Power (Min only enabled oreo+)
        adjustPowerLayoutBinding.rfidReaderPowerSeekbar.setMax(mRFIDReaderHandler.getReaderMaxPower());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            adjustPowerLayoutBinding.rfidReaderPowerSeekbar.setMin(minPower);
        }

        // Get & Set Current Power
        adjustPowerLayoutBinding.rfidReaderPowerSeekbar.setProgress(mRFIDReaderHandler.getCurrentPower());

        // Set Change Listener
        adjustPowerLayoutBinding.rfidReaderPowerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle Min Power Pre-Oreo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if(seekBar.getProgress() < mRFIDReaderHandler.getReaderMinPower()) {
                        seekBar.setProgress(mRFIDReaderHandler.getReaderMinPower());
                    }
                }
            }
        });

        // Show Dialog with Seek Bar
        new AlertDialog.Builder(this)
                .setTitle("Set RFID Reader Power")
                .setView(adjustPowerLayoutBinding.getRoot())
                .setPositiveButton("SET POWER", (dialogInterface, i) -> {
                    // Set Power
                    mRFIDReaderHandler.setReaderPower(adjustPowerLayoutBinding.rfidReaderPowerSeekbar.getProgress());

                    // Store Power As Preference
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .edit()
                            .putInt(READ_POWER_PREFERENCE, adjustPowerLayoutBinding.rfidReaderPowerSeekbar.getProgress())
                            .apply();
                })
                .setNegativeButton("CANCEL", null)
                .create().show();
    }

    /**
     * Interface
     */

    private InitialisationCallback mInitialisationCompleteCallback = new InitialisationCallback() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onInitialisationComplete() {
            // Remove Loading view
            mHandler.post(() -> mDataBinding.loadingLayout.loadingView.setVisibility(View.GONE));



            // Init RFID Read Button Listener
            mDataBinding.startRfidScan.setOnTouchListener(mRfidReadButtonTouchListener);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener mRfidReadButtonTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRFIDReaderHandler.performInventory();
                break;
            case MotionEvent.ACTION_UP:
                mRFIDReaderHandler.stopInventory();
                break;
        } return false;
    };

    private RfidEventsListener mRfidEventsListener = new RfidEventsListener() {
        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {
//            // Get Last 100 Read Tags
////            TagDataArray tagDataArrayParent = mRFIDReaderHandler.mRfidReader.Actions.getReadTagsEx(100);
////            TagData[] tagDataArray = tagDataArrayParent.getTags();
////
////            // Verify we have TagData
////            if (tagDataArray != null && tagDataArray.length > 0) {
////                // Loop Tag Data
////                for (TagData tagData : tagDataArray) {
////                    // Validate Tag has ID
////                    if (tagData.getTagID() != null) {
////                        // Log Tag ID
////                        Log.i(TAG, "Read Tag: " + tagData.getTagID());
////
////                        // Verify Tag is New
////                        if (!mReadTags.contains(tagData)) {
////                            addTagToList(tagData);
////                        }
////                    }
////                }
////            }


            if (rfidReadEvents.getReadEventData() != null
                    && rfidReadEvents.getReadEventData().tagData != null) {

                // Get TagData
                TagData tagData = rfidReadEvents.getReadEventData().tagData;

                // Validate Tag ID
                if (tagData.getTagID() != null && !TextUtils.isEmpty(tagData.getTagID())) {

                    // Validate Uniqueness
                    boolean tagAlreadyHandled = false;
                    for (TagInfo readTag : mReadTags) {
                        if (readTag.getTagId().equals(tagData.getTagID())) {
                            Log.i(TAG, "Ignoring Tag: " + tagData.getTagID() + ". Already Handled.");
                            tagAlreadyHandled = true;
                        }
                    }

                    // Handle New Tag
                    if (!tagAlreadyHandled) {
                        // Create TagInfo object (With TagData ID)
                        TagInfo tagInfo = new TagInfo(tagData.getTagID());

                        // Add TAG Info Object to List
                        mReadTags.add(tagInfo);

                        // Refresh List
                        mHandler.post(() -> mReadTagsAdapter.notifyDataSetChanged());
                    }
                } else {
                    Log.i(TAG, "Tag does not contain valid ID");
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            // Get Status Event Type
            STATUS_EVENT_TYPE statusEventType = rfidStatusEvents.StatusEventData.getStatusEventType();

            // Log Event Type
            Log.d(TAG, "Status Event: " + statusEventType);

            // Handle Event
            if (statusEventType == HANDHELD_TRIGGER_EVENT) {
                // Get Handheld Trigger Event Data
                HANDHELD_TRIGGER_EVENT_TYPE handheldTriggerEventType = rfidStatusEvents
                        .StatusEventData.HandheldTriggerEventData.getHandheldEvent();

                // Handle Press
                if (handheldTriggerEventType == HANDHELD_TRIGGER_PRESSED) {
                    handleTriggerPress(true);
                } else if (handheldTriggerEventType == HANDHELD_TRIGGER_RELEASED) {
                    handleTriggerPress(false);
                }
            }

            if (statusEventType == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                Log.e(TAG, "Reader Disconnected");
            }
        }
    };


}
