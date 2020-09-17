package com.example.android.bluetoothadvertisements.scanning;

import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.bluetoothadvertisements.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ScanResultLogAdapter extends BaseAdapter {
    private ArrayList<String> list = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S", Locale.getDefault());

    void add(ScanResult result) {
        list.add(sdf.format(new Date()) + ": " + result.getDevice().getAddress() + ' ' + result.getDevice().getName());
    }

    void add(String text) {
        list.add(text);
    }

    void clear() {
        list.clear();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
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
