package com.example.android.bluetoothadvertisements.scanning;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.android.bluetoothadvertisements.AppSettings;
import com.example.android.bluetoothadvertisements.Constants;
import com.example.android.bluetoothadvertisements.MainActivity;
import com.example.android.bluetoothadvertisements.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScannerService extends Service {
    private final String TAG = ScannerService.class.getSimpleName();
    private final String NOTIFICATION_CHANNEL_ID = "scanner_service_id";
    private static final String STOP_SCANNER =
            "com.example.android.bluetoothadvertisements.stop_scanner";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private SampleScanCallback mScanCallback;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DIMA", "scan service created");
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();
        registerReceiver(stopReceiver, new IntentFilter(STOP_SCANNER));

        startScanning();
        goForeground();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(stopReceiver);
        if (isScanning) {
            stopScanning();
        }
        stopForeground(true);
        super.onDestroy();
        Log.e("DIMA", "scan service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startScanning() {
        if (mScanCallback == null) {
            Log.e("DIMA", "start scanning");
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
                        stopSelf();
                    }
                }, scanPeriodSeconds * 1000);
            }

            // Kick off a new scan.
            mScanCallback = new SampleScanCallback();
            mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);

            isScanning = true;
            if (null != callback) {
                callback.isScanningUpdated();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.already_scanning, Toast.LENGTH_SHORT);
        }
    }

    private void stopScanning() {
        Log.e("DIMA", "Stopping Scanning");
        AppSettings.getInstance().scannerChangeListener = null;

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;

        isScanning = false;
        if (null != callback) {
            callback.isScanningUpdated();
        }
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(AppSettings.getInstance().getScanMode());
        return builder.build();
    }

    private void goForeground() {
        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(
                    new NotificationChannel(
                            NOTIFICATION_CHANNEL_ID,
                            NOTIFICATION_CHANNEL_ID,
                            NotificationManager.IMPORTANCE_DEFAULT
                    ));
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification n = builder
                .setContentTitle("Bluetooth LE Scanning")
                .setContentText("This device is is scanning devices nearby.")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .addAction(
                        R.drawable.ic_baseline_close_24,
                        "Stop",
                        PendingIntent.getBroadcast(this, 1, new Intent(STOP_SCANNER), 0)
                )
                .build();
        startForeground(new Random().nextInt(), n);
    }

    private BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };


    public static boolean isScanning;
    public static Callback callback;

    public interface Callback {
        void isScanningUpdated();
    }

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
                list.add(Wrapper.create(result));
            }
            if (null != listCallback) {
                listCallback.onChanged();
            }
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
            list.add(Wrapper.create(result));
            if (null != listCallback) {
                listCallback.onChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(getApplicationContext(), "Scan failed with error: " + errorCode, Toast.LENGTH_LONG)
                    .show();
            stopSelf();
        }
    }

    public static class Wrapper {
        ScanResult scanResult;
        String text;

        private Wrapper() {}

        public static Wrapper create(ScanResult scanResult) {
            Wrapper w = new Wrapper();
            w.scanResult = scanResult;
            return w;
        }

        public static Wrapper create(String text) {
            Wrapper w = new Wrapper();
            w.text = text;
            return w;
        }
    }
    public static ArrayList<Wrapper> list = new ArrayList<>();
    public static ListCallback listCallback;

    public interface ListCallback {
        void onChanged();
    }
}
