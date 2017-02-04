package com.meridian.fonduetimer;

import java.util.ArrayList;

import com.meridian.fonduetimer.MainActivity.Guest;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class GuestSelectionListAdapater extends ArrayAdapter<GuestSelectionRow> implements OnItemClickListener {
	ArrayList<GuestSelectionRow> data = null;
	ArrayList<GuestSelectionRow> enabled = null;
	ImageView selectedColorView;

	public GuestSelectionListAdapater(Context context, int layoutResourceId, ImageView selectedColorView) {
		super(context, layoutResourceId);
		this.data = new ArrayList<GuestSelectionRow>();
		this.selectedColorView = selectedColorView;
	}

	public void add(Guest guest) {
		GuestSelectionRow row = new GuestSelectionRow(this.getContext(), guest);
		this.add(row);
	}

	public void add(GuestSelectionRow view) {
		super.add(view);
		this.data.add(view);
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		View row = data.get(index);
		return row;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		Log.i("ColorRowAdapter", "Item Clicked");		
	}

}
