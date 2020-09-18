package com.example.android.bluetoothadvertisements.scanning;

import android.bluetooth.le.ScanResult;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bluetoothadvertisements.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ScanResultLogAdapter extends ScanResultAdapterAbs<String> {
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S", Locale.getDefault());

    @Override
    protected void addInternal(ScanResult result) {
        long time = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(SystemClock.elapsedRealtimeNanos() - result.getTimestampNanos(), TimeUnit.NANOSECONDS);
        list.add(sdf.format(time) + ": " + result.getDevice().getAddress() + ' ' + result.getDevice().getName());
    }

    @Override
    protected void addInternal(String text) {
        list.add(text);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.textView)).setText(list.get(position));

        return convertView;
    }
}
