package com.example.android.bluetoothadvertisements;

import android.app.AlertDialog;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsDialogUtils {
    public static void showSettings(final MainActivity activity) {
        final AppSettings settings = AppSettings.getInstance();

        View root = LayoutInflater.from(activity).inflate(R.layout.dialog_settings, null, false);

        final int[] selectedScanPeriodSeconds = {settings.getScanPeriodSeconds()};
        {
            final RadioGroup scanDuration = root.findViewById(R.id.scanDuration);
            final RadioButton[] selected = {null};
            for (final int seconds : SCAN_DURATION_SECONDS) {
                final RadioButton button = new RadioButton(root.getContext());
                if (seconds > 0) {
                    button.setText(Integer.toString(seconds));
                } else {
                    button.setText("Infinitive");
                }
                if (seconds == selectedScanPeriodSeconds[0]) {
                    selected[0] = button;
                }
                button.setChecked(seconds == selectedScanPeriodSeconds[0]);
                button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedScanPeriodSeconds[0] = seconds;
                            if (null != selected[0]) {
                                selected[0].setChecked(false);
                            }
                            selected[0] = button;
                        }
                    }
                });
                scanDuration.addView(button);
            }
        }

        final int[] selectedScanMode = {settings.getScanMode()};
        {
            RadioGroup scanMode = root.findViewById(R.id.scanMode);
            final RadioButton[] selected = {null};
            for (final ScanMode mode : SCAN_MODE) {
                final RadioButton button = new RadioButton(scanMode.getContext());
                button.setText(mode.name);
                if (mode.value == selectedScanMode[0]) {
                    selected[0] = button;
                }
                button.setChecked(mode.value == selectedScanMode[0]);
                button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedScanMode[0] = mode.value;
                            if (selected[0] != null) {
                                selected[0].setChecked(false);
                            }
                            selected[0] = button;
                        }
                    }
                });
                scanMode.addView(button);
            }
        }

        final int[] selectedAdvertiseMode = {settings.getAdvertiseMode()};
        {
            RadioGroup advertiseMode = root.findViewById(R.id.advertiseMode);
            final RadioButton[] selected = {null};
            for (final AdvertiseMode mode : ADVERTISE_MODES) {
                final RadioButton button = new RadioButton(advertiseMode.getContext());
                button.setText(mode.name);
                if (mode.value == selectedAdvertiseMode[0]) {
                    selected[0] = button;
                }
                button.setChecked(mode.value == selectedAdvertiseMode[0]);
                button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedAdvertiseMode[0] = mode.value;
                            if (selected[0] != null) {
                                selected[0].setChecked(false);
                            }
                            selected[0] = button;
                        }
                    }
                });
                advertiseMode.addView(button);
            }
        }

        final int[] selectedAdvertisePower = {settings.getAdvertisePower()};
        {
            RadioGroup advertisePower = root.findViewById(R.id.advertisePower);
            final RadioButton[] selected = {null};
            for (final AdvertisePower mode : ADVERTISE_POWERS) {
                final RadioButton button = new RadioButton(advertisePower.getContext());
                button.setText(mode.name);
                if (mode.value == selectedAdvertisePower[0]) {
                    selected[0] = button;
                }
                button.setChecked(mode.value == selectedAdvertisePower[0]);
                button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedAdvertisePower[0] = mode.value;
                            if (selected[0] != null) {
                                selected[0].setChecked(false);
                            }
                            selected[0] = button;
                        }
                    }
                });
                advertisePower.addView(button);
            }
        }

        final int[] selectedAdvertiseTimeout = {settings.getAdvertiseTimeout()};
        {
            RadioGroup advertiseTimeout = root.findViewById(R.id.advertiseTimeout);
            final RadioButton[] selected = {null};
            for (final AdvertiseTerminationDuration timeout : ADVERTISE_TERMINATION_DURATIONS) {
                final RadioButton button = new RadioButton((advertiseTimeout.getContext()));
                button.setText(timeout.name);
                if (timeout.value == selectedAdvertiseTimeout[0]) {
                    selected[0] = button;
                }
                button.setChecked(timeout.value == selectedAdvertiseTimeout[0]);
                button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectedAdvertiseTimeout[0] = timeout.value;
                            if (selected[0] != null) {
                                selected[0].setChecked(false);
                            }
                            selected[0] = button;
                        }
                    }
                });
                advertiseTimeout.addView(button);
            }
        }

        new AlertDialog.Builder(activity)
                .setView(root)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.setScanPeriodSeconds(selectedScanPeriodSeconds[0]);
                        settings.setScanMode(selectedScanMode[0]);
                        settings.setAdvertiseMode(selectedAdvertiseMode[0]);
                        settings.setAdvertisePower(selectedAdvertisePower[0]);
                        settings.setAdvertiseTimeout(selectedAdvertiseTimeout[0]);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private static final int[] SCAN_DURATION_SECONDS = new int[]{
            5,
            10,
            15,
            30,
            0
    };

    private static final ScanMode[] SCAN_MODE = new ScanMode[]{
            new ScanMode("Low", ScanSettings.SCAN_MODE_LOW_POWER),
            new ScanMode("Balanced", ScanSettings.SCAN_MODE_BALANCED),
            new ScanMode("Fast", ScanSettings.SCAN_MODE_LOW_LATENCY)
    };

    private static class ScanMode {
        ScanMode(String name, int value) {
            this.name = name;
            this.value = value;
        }

        final String name;
        final int value;
    }

    private static final AdvertiseMode[] ADVERTISE_MODES = new AdvertiseMode[]{
            new AdvertiseMode("Low", AdvertiseSettings.ADVERTISE_MODE_LOW_POWER),
            new AdvertiseMode("Balanced", AdvertiseSettings.ADVERTISE_MODE_BALANCED),
            new AdvertiseMode("Fast", AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY),
    };

    private static class AdvertiseMode {
        final String name;
        final int value;

        private AdvertiseMode(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final AdvertisePower[] ADVERTISE_POWERS = new AdvertisePower[]{
            new AdvertisePower("Ultra Low", AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW),
            new AdvertisePower("Low", AdvertiseSettings.ADVERTISE_TX_POWER_LOW),
            new AdvertisePower("Medium", AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM),
            new AdvertisePower("High", AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
    };

    private static class AdvertisePower {
        final String name;
        final int value;

        private AdvertisePower(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final AdvertiseTerminationDuration[] ADVERTISE_TERMINATION_DURATIONS = new AdvertiseTerminationDuration[]{
            new AdvertiseTerminationDuration("Disabled", 0),
            new AdvertiseTerminationDuration("2 seconds", 2 * 1000),
            new AdvertiseTerminationDuration("5 seconds", 5 * 1000),
            new AdvertiseTerminationDuration("10 seconds", 10 * 1000),
            new AdvertiseTerminationDuration("15 seconds", 15 * 1000)
    };

    private static class AdvertiseTerminationDuration {
        final String name;
        final int value;

        private AdvertiseTerminationDuration(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
