package com.meridian.fonduetimer;

import com.meridian.fonduetimer.R;
import com.meridian.fonduetimer.MainActivity.ForkColor;
import com.meridian.fonduetimer.MainActivity.Guest;
import com.meridian.fonduetimer.MainActivity.Morsel;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimerRow extends LinearLayout {	
	private static long OVERCOOKED_CONSTANT = -20000;
	private static long EXPIRED_CONSTANT = -3600000;
	public static enum State { COOKING, DONE, OVERCOOKED, EXPIRED, MULTISELECT, UNKNOWN };
	
	private String guestName;
	private Morsel morsel;
	private ForkColor forkColor;
	
	private long durationMillis;
	private long baseTimeMillis;

	private State state;
	
	boolean multiSelectModeActive;
	private TextView countdownView;
	private Handler handler;
	private ImageView deleteButton;

	public TimerRow(Context context, ViewGroup parent, Handler handler, Morsel morsel, Guest guest) {
		super(context);
		this.state = State.COOKING;
		long timeNow = System.currentTimeMillis();
		this.init(context, handler, timeNow, timeNow, morsel.getCookingTime() * 1000, guest.getGuestName(), morsel, guest.getForkColor());
	}
	
	
	public TimerRow(Context context, ViewGroup parent, Handler handler, FondueTimerSavedInstanceData savedInstanceBundle, long currentTimeMillis) {
		super(context);
		this.state = State.UNKNOWN;
		this.init(context, handler, currentTimeMillis, savedInstanceBundle.baseTimeMillis, savedInstanceBundle.durationMillis,
				savedInstanceBundle.guestName, savedInstanceBundle.morsel, savedInstanceBundle.forkColor);
	}
	
	private void init(Context context, Handler handler, long currentTimeMillis, long baseTimeMillis, long durationMillis, String guestName, Morsel morsel, ForkColor forkColor) {
		this.handler = handler;
		this.baseTimeMillis = baseTimeMillis;
		this.durationMillis = durationMillis;
		this.morsel = morsel;
		this.forkColor = forkColor;
		this.guestName = guestName;
		
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		inflater.inflate(R.layout.listview_item_row, this);
		((ImageView) this.findViewById(R.id.imgIcon)).setImageDrawable(morsel.getDrawable(context));
		((TextView) this.findViewById(R.id.txtTitle)).setText(guestName);
		
		this.deleteButton = (ImageView) this.findViewById(R.id.deleteButton);
		this.deleteButton.setTag(this);
		this.deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				((TimerRow)(view.getTag())).delete();
			}
		});
		
		((ImageView) this.findViewById(R.id.rowColorView)).setImageDrawable(forkColor.getDrawable(context));
		countdownView = (TextView) this.findViewById(R.id.txtCountdown);
		this.update(currentTimeMillis);
	}
	

	
	
	public class FondueTimerSavedInstanceData {
		private long baseTimeMillis;
		private long durationMillis;
		private Morsel morsel;
		private ForkColor forkColor;
		private String guestName;
		
		public FondueTimerSavedInstanceData(long baseTimeMillis, long durationMillis, Morsel morsel, ForkColor forkColor, String guestName) {
			this.baseTimeMillis = baseTimeMillis;
			this.durationMillis = durationMillis;
			this.morsel = morsel;
			this.forkColor = forkColor;
			this.guestName = guestName;
		}
	}
	
	public FondueTimerSavedInstanceData getSavedInstanceData() {
		return new FondueTimerSavedInstanceData(this.baseTimeMillis, this.durationMillis, this.morsel, this.forkColor, this.guestName);
	}
	
	public long getTimeRemaining(long systemTime) {
		long timeElapsed = systemTime - this.baseTimeMillis;
		return Math.max(durationMillis - timeElapsed, EXPIRED_CONSTANT);
	}
	
	private void updateState(long timeLeft) {
		switch(this.state) {
		//If the state is unknown, figure out correct state
		case UNKNOWN:
			if(timeLeft > 0) {
				this.state = State.COOKING;
			} else if(timeLeft > OVERCOOKED_CONSTANT) {
				this.state = State.DONE;
				((TextView)(this.findViewById(R.id.txtCountdown))).setTextColor(getResources().getColor(R.color.green));
				deleteButton.setVisibility(VISIBLE);
			} else if(timeLeft > EXPIRED_CONSTANT) {
				this.state = State.OVERCOOKED;
				((TextView)(this.findViewById(R.id.txtCountdown))).setTextColor(getResources().getColor(R.color.red));
				deleteButton.setVisibility(VISIBLE);
			} else {
				this.state = State.EXPIRED;
				((TextView)(this.findViewById(R.id.txtCountdown))).setTextColor(getResources().getColor(R.color.red));
				deleteButton.setVisibility(VISIBLE);
			}
			break;
		//If the case is cooking, the only possible next state is Done
		case COOKING:
			if(timeLeft <= 0) {
				this.state = State.DONE;
				((TextView)(this.findViewById(R.id.txtCountdown))).setTextColor(getResources().getColor(R.color.green));
				Message message = new Message();
				message.what = 2;
				message.obj = this;
				this.handler.sendMessage(message);
				if(!multiSelectModeActive) { deleteButton.setVisibility(VISIBLE); }
			}
			break;
		//If the case is done, the only possible next state is overcooked
		case DONE:
			if(timeLeft < OVERCOOKED_CONSTANT) {
				this.state = State.OVERCOOKED;
				((TextView)(this.findViewById(R.id.txtCountdown))).setTextColor(getResources().getColor(R.color.red));
			}
			break;
		//If the case is overcooked, the only possible next state is expired
		case OVERCOOKED:
			if(timeLeft < EXPIRED_CONSTANT) {
				this.state = State.EXPIRED;
			}
			break;
		}
	}
	
	private String formatTime(long timeLeft) {
		int secondsLeft = (int) (timeLeft / 1000);
		StringBuilder builder = new StringBuilder();
		if(this.state != State.COOKING) {
			builder.append("+");
			secondsLeft*=-1;
		}
		if(secondsLeft >= 60) {
			int minutesLeft = secondsLeft / 60;
			secondsLeft%=60;
			builder.append(minutesLeft);
			builder.append(":");
			if(secondsLeft<10) {
				builder.append("0");
			}
		}
		builder.append(secondsLeft);
		return builder.toString();
	}
	
	public void update(long systemTime) {
		long timeLeft = this.getTimeRemaining(systemTime);
		this.updateState(timeLeft);
		this.countdownView.setText(this.formatTime(timeLeft));
	}
	
	public void setMultiselectModeIsActive(boolean value) {
		this.multiSelectModeActive = value;
		if(this.multiSelectModeActive) {
			deleteButton.setVisibility(GONE);
		} else {
			if(this.state != State.COOKING) { deleteButton.setVisibility(VISIBLE); }
		}
	}

	public void delete() {
		Message message = new Message();
		message.what = 1;
		message.obj = this;
		this.handler.sendMessage(message);
	}

}
