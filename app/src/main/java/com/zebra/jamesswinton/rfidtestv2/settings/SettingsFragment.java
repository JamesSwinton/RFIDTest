package com.zebra.jamesswinton.rfidtestv2.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.zebra.jamesswinton.rfidtestv2.R;

public class SettingsFragment extends PreferenceFragmentCompat {

  // Debugging
  private static final String TAG = "SettingsFragment";

  // Constants


  // Static Variables


  // Variables


  public SettingsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
  }

  @Override
  public void onStart() {
    super.onStart();
  }


}
