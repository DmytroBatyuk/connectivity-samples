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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.android.bluetoothadvertisements.advertisement.AdvertiserFragment;
import com.example.android.bluetoothadvertisements.scanning.ScannerFragment;
import com.example.android.bluetoothadvertisements.scanning.StartScannerFragment;

/**
 * Setup display fragments and ensure the device supports Bluetooth.
 */
public class MainActivity extends FragmentActivity {
    private final int REFRESH_ID = 0;
    private final int SETTINGS_ID = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private StartScannerFragment startScannerFragment;
    private ScannerFragment scannerFragment;
    private AdvertiserFragment advertiserFragment;
    private boolean isRefreshVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.activity_main_title);
        startScannerFragment = new StartScannerFragment();
        scannerFragment = new ScannerFragment();
        advertiserFragment = new AdvertiserFragment();

        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                if (!mBluetoothAdapter.isEnabled()) {
                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(this, new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            },
                            Constants.REQUEST_LOCATION_PERMISSIONS);
                } else {
                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(R.string.bt_not_supported);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 2 &&
                (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED)
        ) {
            setupFragments();
        } else {
            showErrorText(R.string.no_location_permissions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:

                if (resultCode == RESULT_OK) {

                    // Bluetooth is now Enabled, are Bluetooth Advertisements supported on
                    // this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        setupFragments();

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // User declined to enable Bluetooth, exit the app.
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isRefreshVisible) {
            menu.add(Menu.NONE, REFRESH_ID, REFRESH_ID, R.string.refresh)
                    .setIcon(R.drawable.ic_action_refresh)
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(Menu.NONE, SETTINGS_ID, SETTINGS_ID, R.string.settings)
                .setIcon(R.drawable.ic_baseline_settings_24)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case REFRESH_ID:
                refreshScanner();
                return true;
            case SETTINGS_ID:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateIsScanning(boolean isScanning) {
        isRefreshVisible = !isScanning;
        if (isScanning) {
            findViewById(R.id.scanning).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.scanning).setVisibility(View.INVISIBLE);
        }
        invalidateOptionsMenu();
    }

    private void refreshScanner() {
        scannerFragment.startScanning();
    }

    private void showSettings() {
        SettingsDialogUtils.showSettings(this);
    }

    private void setupFragments() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        startScannerFragment.callback = new StartScannerFragment.Callback() {
            @Override
            public void onStartScanner() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.scanner_fragment_container, scannerFragment)
                        .commit();
            }
        };
        // Fragments can't access system services directly, so pass it the BluetoothAdapter
        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
        transaction.replace(R.id.scanner_fragment_container, startScannerFragment);
        transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);

        transaction.commit();
    }

    private void showErrorText(int messageId) {

        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }
}