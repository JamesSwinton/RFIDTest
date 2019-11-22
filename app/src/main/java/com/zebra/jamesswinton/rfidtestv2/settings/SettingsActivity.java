package com.zebra.jamesswinton.rfidtestv2.settings;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zebra.jamesswinton.rfidtestv2.R;
import com.zebra.jamesswinton.rfidtestv2.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "SettingsActivity";

    // Constants


    // Private Variables


    // Public Variables
    private ActivitySettingsBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        // Configure Toolbar
        configureToolbar();

        // Show Settings Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragent_container, new SettingsFragment())
                .addToBackStack("SETTINGS")
                .commit();
    }

    private void configureToolbar() {
        setSupportActionBar(mDataBinding.toolbarLayout.toolbar);
        mDataBinding.toolbarLayout.toolbar.setTitle("Settings");
    }
}
