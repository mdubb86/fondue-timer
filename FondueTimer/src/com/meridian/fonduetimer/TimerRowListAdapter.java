package com.meridian.fonduetimer;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;

import com.meridian.fonduetimer.R;
import com.meridian.fonduetimer.TimerRow.FondueTimerSavedInstanceData;

public class TimerRowListAdapter extends ArrayAdapter<TimerRow> {

	public TimerRowListAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId, new ArrayList<TimerRow>());
	}
	
	public void addTimerRow(TimerRow timerRow, boolean showAnimation) {	
		long currentTime = System.currentTimeMillis();
		long timeLeft = timerRow.getTimeRemaining(System.currentTimeMillis());		
		//We will assume it is going at the end
		int index = this.getCount();
		//And move backwards until we find the correct insertion index
		while(index > 0 && this.getItem(index - 1).getTimeRemaining(currentTime) > timeLeft) {
			index--;
		}		
		super.insert(timerRow, index);
		if(showAnimation) {
			Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.wave_scale);
			timerRow.startAnimation(hyperspaceJumpAnimation);
		}
	}
	
	public FondueTimerSavedInstanceData[] toArray() {
		FondueTimerSavedInstanceData[] result = new FondueTimerSavedInstanceData[this.getCount()];
		for(int i=0;i<result.length;i++) {
			result[i] = this.getItem(i).getSavedInstanceData();
		}
		return result;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		TimerRow row = this.getItem(index);
		return row;
	}
	
	public void updateTimerRowCountdownText() {
		long systemTimeForUpdate = System.currentTimeMillis();
		for(int i=0;i<this.getCount();i++) {
			this.getItem(i).update(systemTimeForUpdate);
		}
		this.notifyDataSetChanged();	
	}

	public void setMultiSelectModeActive(boolean active) {
		for(int i=0;i<this.getCount();i++) {
			this.getItem(i).setMultiselectModeIsActive(active);
		}
	}
}
