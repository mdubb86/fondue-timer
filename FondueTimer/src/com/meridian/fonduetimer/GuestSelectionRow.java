package com.meridian.fonduetimer;

import com.meridian.fonduetimer.R;
import com.meridian.fonduetimer.MainActivity.Guest;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuestSelectionRow extends LinearLayout {
	private Guest guest;	
	private ImageView iconView;
	
	public GuestSelectionRow(Context context, Guest guest) {
		super(context);
		this.guest = guest;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.color_row, this);
		iconView = (ImageView) this.findViewById(R.id.color_icon);
		iconView.setImageDrawable(guest.getForkColor().getDrawable(context));
		((TextView) this.findViewById(R.id.color_text)).setText(guest.getGuestName());
	}

	public Guest getGuest() {
		return guest;
	}
}