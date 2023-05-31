/*
 * Copyright (C) 2023 The Evolution X Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.pixelparts.batteryinfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference;

import org.evolution.pixelparts.misc.Constants;
import org.evolution.pixelparts.R;
import org.evolution.pixelparts.utils.Utils;

public class BatteryInfo extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = BatteryInfo.class.getSimpleName();

    private Handler mHandler;
    private Runnable mUpdateRunnable;

    // Battery info
    private Preference mTechnologyPreference;
    private Preference mStatusPreference;
    private Preference mTemperaturePreference;
    private Preference mCapacityPreference;
    private Preference mCapacityLevelPreference;
    private Preference mCurrentPreference;
    private Preference mVoltagePreference;
    private Preference mWattagePreference;
    private Preference mHealthPreference;
    private Preference mCycleCountPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.batteryinfo);
        SharedPreferences prefs = getActivity().getSharedPreferences("batteryinfo",
                Activity.MODE_PRIVATE);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context context = getContext();

        mHandler = new Handler();
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                updatePreferenceSummaries();
                mHandler.postDelayed(this,
                        sharedPrefs.getInt(Constants.KEY_BATTERY_INFO_REFRESH, 5) * 1000);
            }
        };

        mTechnologyPreference = findPreference(Constants.KEY_TECHNOLOGY);
        mStatusPreference = findPreference(Constants.KEY_STATUS);
        mTemperaturePreference = findPreference(Constants.KEY_TEMPERATURE);
        mCapacityPreference = findPreference(Constants.KEY_CAPACITY);
        mCapacityLevelPreference = findPreference(Constants.KEY_CAPACITY_LEVEL);
        mCurrentPreference = findPreference(Constants.KEY_CURRENT);
        mVoltagePreference = findPreference(Constants.KEY_VOLTAGE);
        mWattagePreference = findPreference(Constants.KEY_WATTAGE);
        mHealthPreference = findPreference(Constants.KEY_HEALTH);
        mCycleCountPreference = findPreference(Constants.KEY_CYCLE_COUNT);

        updatePreferenceSummaries();
        mHandler.postDelayed(mUpdateRunnable,
                sharedPrefs.getInt(Constants.KEY_BATTERY_INFO_REFRESH, 5) * 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceSummaries();
        mHandler.postDelayed(mUpdateRunnable, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateRunnable);
    }

    private void updatePreferenceSummaries() {
        // Technology preference
        if (Utils.isFileReadable(Constants.NODE_TECHNOLOGY)) {
            String fileValue = Utils.getFileValue(Constants.NODE_TECHNOLOGY, null);
            mTechnologyPreference.setSummary(fileValue);
        } else {
            mTechnologyPreference.setSummary(getString(R.string.node_not_readable_error));
            mTechnologyPreference.setEnabled(false);
        }

        // Status preference
        if (Utils.isFileReadable(Constants.NODE_STATUS)) {
            String fileValue = Utils.getFileValue(Constants.NODE_STATUS, null);
            mStatusPreference.setSummary(fileValue);
        } else {
            mStatusPreference.setSummary(getString(R.string.node_not_readable_error));
            mStatusPreference.setEnabled(false);
        }

        // Temperature preference
        if (Utils.isFileReadable(Constants.NODE_TEMPERATURE)) {
            String fileValue = Utils.getFileValue(Constants.NODE_TEMPERATURE, null);
            int temperature = Integer.parseInt(fileValue);
            float temperatureCelsius = temperature / 10.0f;
            mTemperaturePreference.setSummary(temperatureCelsius + "°C");
        } else {
            mTemperaturePreference.setSummary(getString(R.string.node_not_readable_error));
            mTemperaturePreference.setEnabled(false);
        }

        // Capacity preference
        if (Utils.isFileReadable(Constants.NODE_CAPACITY)) {
            String fileValue = Utils.getFileValue(Constants.NODE_CAPACITY, null);
            mCapacityPreference.setSummary(fileValue + "%");
        } else {
            mCapacityPreference.setSummary(getString(R.string.node_not_readable_error));
            mCapacityPreference.setEnabled(false);
        }

        // Capacity level preference
        if (Utils.isFileReadable(Constants.NODE_CAPACITY_LEVEL)) {
            String fileValue = Utils.getFileValue(Constants.NODE_CAPACITY_LEVEL, null);
            mCapacityLevelPreference.setSummary(fileValue);
        } else {
            mCapacityLevelPreference.setSummary(getString(R.string.node_not_readable_error));
            mCapacityLevelPreference.setEnabled(false);
        }

        // Current preference
        if (Utils.isFileReadable(Constants.NODE_CURRENT)) {
            String fileValue = Utils.getFileValue(Constants.NODE_CURRENT, null);
            int chargingCurrent = Integer.parseInt(fileValue);
            int absoluteChargingCurrent = Math.abs(chargingCurrent);
            String formattedChargingCurrent = (absoluteChargingCurrent / 1000) + "mA";
            mCurrentPreference.setSummary(formattedChargingCurrent);
        } else {
            mCurrentPreference.setSummary(getString(R.string.node_not_readable_error));
            mCurrentPreference.setEnabled(false);
        }

        // Voltage preference
        if (Utils.isFileReadable(Constants.NODE_VOLTAGE)) {
            String fileValue = Utils.getFileValue(Constants.NODE_VOLTAGE, null);
            float chargingVoltage = Float.parseFloat(fileValue);
            String formattedChargingVoltage = String.format("%.1f", (chargingVoltage / 1000000)) + "V";
            mVoltagePreference.setSummary(formattedChargingVoltage);
        } else {
            mVoltagePreference.setSummary(getString(R.string.node_not_readable_error));
            mVoltagePreference.setEnabled(false);
        }

        // Wattage preference
        if (Utils.isFileReadable(Constants.NODE_VOLTAGE) && Utils.isFileReadable(Constants.NODE_CURRENT)) {
            String voltageFileValue = Utils.getFileValue(Constants.NODE_VOLTAGE, null);
            String currentFileValue = Utils.getFileValue(Constants.NODE_CURRENT, null);
            float chargingCurrent = Integer.parseInt(currentFileValue) / 1000.0f;
            float chargingVoltage = Float.parseFloat(voltageFileValue) / 1000000.0f;
            float wattage = (chargingVoltage * chargingCurrent) / 1000.0f;
            float absoluteWattage = Math.abs(wattage);
            String formattedWattage = String.format("%.1f", absoluteWattage) + "W";
            mWattagePreference.setSummary(formattedWattage);
        } else {
            mWattagePreference.setSummary(getString(R.string.node_not_readable_error));
            mWattagePreference.setEnabled(false);
        }

        // Health preference
        if (Utils.isFileReadable(Constants.NODE_HEALTH)) {
            String fileValue = Utils.getFileValue(Constants.NODE_HEALTH, null);
            mHealthPreference.setSummary(fileValue);
        } else {
            mHealthPreference.setSummary(getString(R.string.node_not_readable_error));
            mHealthPreference.setEnabled(false);
        }

        // Cycle count preference
        if (Utils.isFileReadable(Constants.NODE_CYCLE_COUNT)) {
            String fileValue = Utils.getFileValue(Constants.NODE_CYCLE_COUNT, null);
            mCycleCountPreference.setSummary(fileValue);
        } else {
            mCycleCountPreference.setSummary(getString(R.string.node_not_readable_error));
            mCycleCountPreference.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}