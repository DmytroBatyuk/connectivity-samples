/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothadvertisements;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class ScannerFragment extends ListFragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();

    /**
     * Stops scanning after 5 seconds.
     */
    private static final long SCAN_PERIOD = 5000;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private ScanResultAdapter mAdapter;

    private Handler mHandler;

    /**
     * Must be called after object creation by MainActivity.
     *
     * @param btAdapter the local BluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Use getActivity().getApplicationContext() instead of just getActivity() because this
        // object lives in a fragment and needs to be kept separate from the Activity lifecycle.
        //
        // We could get a LayoutInflater from the ApplicationContext but it messes with the
        // default theme, so generate it from getActivity() and pass it in separately.
        mAdapter = new ScanResultAdapter(getActivity().getApplicationContext(),
                LayoutInflater.from(getActivity()));
        mHandler = new Handler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setListAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        setEmptyText(getString(R.string.empty_list));

        // Trigger refresh on app's 1st load
        startScanning();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopScanning();
    }

    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
    public void startScanning() {
        if (mScanCallback == null) {
            AppSettings.getInstance().scannerChangeListener = new AppSettings.OnScannerSettingsChanged() {
                @Override
                public void onChanged() {
                    stopScanning();
                    startScanning();
                }
            };
            Log.d(TAG, "Starting Scanning");

            int scanPeriodSeconds = AppSettings.getInstance().getScanPeriodSeconds();

            if (scanPeriodSeconds > 0) {
                // Will stop the scanning after a set time.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanning();
                    }
                }, scanPeriodSeconds * 1000);
            }

            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
            FragmentActivity activity = getActivity();
            if (null != activity) {
                ((MainActivity) activity).updateRefreshMenuItem(false);
            }

            String toastText;
            if (scanPeriodSeconds > 0) {
                toastText = getString(R.string.scan_start_toast) + " "
                        + scanPeriodSeconds + " "
                        + getString(R.string.seconds);
            } else {
                toastText = "Scanning infinitive";
            }
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), R.string.already_scanning, Toast.LENGTH_SHORT);
        }
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");
        AppSettings.getInstance().scannerChangeListener = null;

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;

        // Even if no new results, update 'last seen' times.
        mAdapter.notifyDataSetChanged();

        FragmentActivity activity = getActivity();
        if (null != activity) {
            ((MainActivity) activity).updateRefreshMenuItem(true);
        }
    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(AppSettings.getInstance().getScanMode());
        return builder.build();
    }

    /**
     * Custom ScanCallback object - adds to adapter on success, displays error on failure.
     */
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            ArrayList<String> macs = new ArrayList<>(results.size());
            for (ScanResult result : results) {
                macs.add(result.getDevice().getAddress());
            }
            Log.e("DIMA", "onBatchScanResults: list=" + macs);

            for (ScanResult result : results) {
                mAdapter.add(result);
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String cbType = "--";
            switch (callbackType) {
                case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                    cbType = "ALL_MATCHES";
                    break;
                case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                    cbType = "FIRST_MATCH";
                    break;
                case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                    cbType = "MATCH_LOST";
                    break;
            }
            Log.e("DIMA", "onScanResult: mac=" + result.getDevice().getAddress() + ", cbType=" + cbType);
            mAdapter.add(result);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getActivity(), "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
        }
    }
}
