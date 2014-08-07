package org.openhds.mobile.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;


public class CheckBoxListAdapter extends BaseAdapter {

    private ArrayList<CheckBox>  checkboxes;
    Context context;

    public CheckBoxListAdapter(Context context, int numberOfCheckboxes) {
        this.context = context;
        checkboxes = new ArrayList<CheckBox>();
        while (numberOfCheckboxes > 0) {
            CheckBox cb = new CheckBox(context);
            checkboxes.add(cb);
            numberOfCheckboxes--;
        }
    }

    @Override
    public int getCount() {
        return checkboxes.size();
    }

    @Override
    public Object getItem(int position) {
        return checkboxes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return checkboxes.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = checkboxes.get(position);
        convertView.setTag(position);
        return convertView;

    }
}
