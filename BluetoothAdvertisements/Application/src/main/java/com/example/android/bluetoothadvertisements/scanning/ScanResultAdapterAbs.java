package com.example.android.bluetoothadvertisements.scanning;

import android.bluetooth.le.ScanResult;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class ScanResultAdapterAbs<T> extends BaseAdapter {
    protected ArrayList<T> list = new ArrayList<>();

    void add(List<ScanResult> list) {
        for (ScanResult scanResult : list) {
            addInternal(scanResult);
        }
        notifyDataSetChanged();
    }
    void add(ScanResult result) {
        addInternal(result);
        notifyDataSetChanged();
    }

    void add(String text) {
        addInternal(text);
    }

    void clear() {
        list.clear();
        notifyDataSetChanged();
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

    abstract protected void addInternal(ScanResult result);
    abstract protected void addInternal(String text);
}
