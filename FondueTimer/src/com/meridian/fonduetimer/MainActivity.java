package com.meridian.fonduetimer;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.meridian.fonduetimer.R;
import com.google.gson.Gson;

/**
 * Entry point of Fondue Timer
 * 
 * @author Michael Waage
 */
public class MainActivity extends Activity implements Handler.Callback,
		OnItemClickListener, MultiChoiceModeListener {
	public static SharedPreferences PREFERENCES;
	// ------------------------------------------------------------------------
	// Private Data
	// ------------------------------------------------------------------------
	private Handler handler;
	private TimerRowListAdapter timerRowListAdapter;
	private Timer secondTimer;
	private Animation buttonClicked;
	private ActionMode cabMode;
	private boolean reloadSettings = true;
	private Guest selectedGuest;

	// ------------------------------------------------------------------------
	// Private UI Elements
	// ------------------------------------------------------------------------
	private ListView timerListView;
	private ImageView guestSelectionView;
	private ListView guestSelectionListView;
	private GuestSelectionListAdapater guestSelectionListAdapater;
	private PopupWindow guestSelectionPopupWindow;

	// ------------------------------------------------------------------------
	// Public Static Enums
	// ------------------------------------------------------------------------
	public static enum Guest {
		GUEST1, GUEST2, GUEST3, GUEST4;
		private boolean enabled;
		private String guestName;
		private ForkColor forkColor;

		public static void UpdateAll(GuestSelectionListAdapater adapter) {
			for (Guest guest : values()) {
				guest.update();
				if (guest.isEnabled()) {
					adapter.add(guest);
				}
			}
		}

		public void update() {
			String prefStr = super.toString().toLowerCase();
			this.enabled = MainActivity.PREFERENCES.getBoolean("pref_"
					+ prefStr + "_enabled", false);
			this.guestName = MainActivity.PREFERENCES.getString("pref_"
					+ prefStr + "_name", null);
			this.forkColor = ForkColor.getColor(MainActivity.PREFERENCES
					.getString("pref_" + prefStr + "_color", null)
					.toLowerCase());
		}

		public boolean isEnabled() {
			return enabled;
		}

		public String getGuestName() {
			return guestName;
		}

		public ForkColor getForkColor() {
			return forkColor;
		}
	}

	public static enum ForkColor {
		BLACK(R.drawable.square_black), BLUE(R.drawable.square_blue), BROWN(
				R.drawable.square_brown), GREEN(R.drawable.square_green), PINK(
				R.drawable.square_pink), RED(R.drawable.square_red), TEAL(
				R.drawable.square_teal), WHITE(R.drawable.square_white), YELLOW(
				R.drawable.square_yellow);

		/**
		 * A mapping between the integer code and its corresponding Status to
		 * facilitate lookup by code.
		 */
		private static Map<String, ForkColor> nameToColorMapping;
		private String colorName;
		private int drawableId;
		private Drawable drawable;

		public static ForkColor getColor(String color) {
			if (nameToColorMapping == null) {
				initMapping();
			}
			return nameToColorMapping.get(color);
		}

		private static void initMapping() {
			nameToColorMapping = new HashMap<String, ForkColor>();
			for (ForkColor color : values()) {
				nameToColorMapping.put(color.colorName, color);
			}
		}

		private ForkColor(int drawableId) {
			this.colorName = super.toString().toLowerCase();
			this.drawableId = drawableId;
		}

		public Drawable getDrawable(Context context) {
			if (this.drawable == null) {
				loadDrawable(context);
			}
			return drawable;
		}

		private void loadDrawable(Context context) {
			this.drawable = context.getResources().getDrawable(drawableId);
		}
	}

	public static enum CookingMethod {
		BROTH, OIL;

		public static CookingMethod fromString(String method) {
			if (method.equalsIgnoreCase("broth")) {
				return BROTH;
			} else if (method.equalsIgnoreCase("OIL")) {
				return OIL;
			} else {
				return null;
			}
		}
	}

	public static enum Morsel {
		CHICKEN(R.drawable.morsel_chicken), BEEF(R.drawable.morsel_beef), PORK(
				R.drawable.morsel_pork), SEAFOOD(R.drawable.morsel_seafood), VEGETABLE(
				R.drawable.morsel_vegetable);

		private int drawableId;
		private int cookingTime;
		private Drawable drawable;

		public static void UpdateAll(CookingMethod method) {
			for (Morsel morsel : values()) {
				morsel.updateCookingTime(method);
			}
		}

		private Morsel(int drawableId) {
			this.drawableId = drawableId;
		}

		public void setMenuImageView(View view) {
			view.setTag(this);
		}

		public void updateCookingTime(CookingMethod method) {
			this.cookingTime = Integer.valueOf(MainActivity.PREFERENCES
					.getString("pref_" + method.toString().toLowerCase() + "_"
							+ super.toString().toLowerCase() + "_time", null));
		}

		public int getCookingTime() {
			return cookingTime;
		}

		public Drawable getDrawable(Context context) {
			if (this.drawable == null) {
				loadDrawable(context);
			}
			return drawable;
		}

		private void loadDrawable(Context context) {
			this.drawable = context.getResources().getDrawable(drawableId);
		}
	}

	// ------------------------------------------------------------------------
	// Activity Life Cycle Methods
	// ------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MainActivity.PREFERENCES = PreferenceManager
				.getDefaultSharedPreferences(this);
		// --------------------------------------------------------------------
		// Inflate Activity layout
		// --------------------------------------------------------------------
		this.setContentView(R.layout.activity_main);
		// --------------------------------------------------------------------
		// Initialize default preferences if this is first run
		// --------------------------------------------------------------------
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		// --------------------------------------------------------------------
		// Initialize class variables
		// --------------------------------------------------------------------
		this.handler = new Handler(this);
		this.guestSelectionView = (ImageView) this.findViewById(R.id.fork_view);
		this.timerListView = (ListView) findViewById(R.id.listView1);
		this.buttonClicked = AnimationUtils.loadAnimation(this,
				R.anim.view_fade_in);

		Morsel.CHICKEN.setMenuImageView(this.findViewById(R.id.chicken_view));
		Morsel.BEEF.setMenuImageView(this.findViewById(R.id.beef_view));
		Morsel.PORK.setMenuImageView(this.findViewById(R.id.pork_view));
		Morsel.SEAFOOD.setMenuImageView(this.findViewById(R.id.seafood_view));
		Morsel.VEGETABLE.setMenuImageView(this
				.findViewById(R.id.vegetable_view));

		// --------------------------------------------------------------------
		// Setup timer row list and list adapter
		// --------------------------------------------------------------------
		this.timerRowListAdapter = new TimerRowListAdapter(this,
				R.layout.listview_item_row);
		timerListView.setAdapter(timerRowListAdapter);
		timerListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		timerListView.setMultiChoiceModeListener(this);
		// --------------------------------------------------------------------
		// Setup guest selection popup menu
		// --------------------------------------------------------------------
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup layout = (ViewGroup) layoutInflater.inflate(
				R.layout.color_selection, null);
		guestSelectionPopupWindow = new PopupWindow(this);
		guestSelectionPopupWindow.setContentView(layout);
		guestSelectionPopupWindow
				.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
		guestSelectionPopupWindow
				.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
		guestSelectionPopupWindow.setFocusable(true);
		guestSelectionPopupWindow
				.setAnimationStyle(R.style.GuestSelectionMenuAnimation);
		this.guestSelectionListView = (ListView) layout
				.findViewById(R.id.color_list_view);
		guestSelectionListView.setOnItemClickListener(this);
		// --------------------------------------------------------------------
		// Determine if we are resuming from destroyed state and need to
		// restore rows
		// --------------------------------------------------------------------
		String storedRows = MainActivity.PREFERENCES.getString("storedRows",
				null);
		if (storedRows != null) {
			Gson gson = new Gson();
			TimerRow.FondueTimerSavedInstanceData[] rows = gson.fromJson(
					storedRows, TimerRow.FondueTimerSavedInstanceData[].class);
			long currentTime = System.currentTimeMillis();
			for (TimerRow.FondueTimerSavedInstanceData rowSavedInstanceData : rows) {
				timerRowListAdapter.addTimerRow(new TimerRow(this,
						timerListView, handler, rowSavedInstanceData,
						currentTime), false);
			}
			SharedPreferences.Editor editor = MainActivity.PREFERENCES.edit();
			editor.remove("storedRows");
			editor.commit();
		}
		// --------------------------------------------------------------------
		// Set flag to reload settings since we are just starting up
		// --------------------------------------------------------------------
		reloadSettings = true;
	}

	// ------------------------------------------------------------------------
	// Method to calculate the size of the widest view in a layout without
	// actually displaying it. Used to properly size of the popup menu.
	// Code from http://stackoverflow.com/questions/6547154/wrap-content-for-a
	// -listviews-width
	// ------------------------------------------------------------------------
	public static int getWidestView(Context context, Adapter adapter) {
		int maxWidth = 0;
		View view = null;
		FrameLayout fakeParent = new FrameLayout(context);
		for (int i = 0, count = adapter.getCount(); i < count; i++) {
			view = adapter.getView(i, view, fakeParent);
			view.measure(View.MeasureSpec.UNSPECIFIED,
					View.MeasureSpec.UNSPECIFIED);
			int width = view.getMeasuredWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		return maxWidth;
	}

	public void showGuestSelectionMenu(View v) {
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		guestSelectionPopupWindow.showAtLocation(
				this.findViewById(R.id.fork_view), Gravity.NO_GRAVITY,
				location[0], location[1] + v.getHeight());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onPause() {
		super.onPause();
		Log.i("MainActivity", "Paused " + timerRowListAdapter.getCount()
				+ " elements");
		secondTimer.cancel();
	}

	public void onResume() {
		super.onResume();
		Log.i("MainActivity", "Resumed " + timerRowListAdapter.getCount()
				+ " elements");
		secondTimer = new Timer();
		secondTimer.scheduleAtFixedRate(new MPTimerTask(), 0, 1000);
		if (reloadSettings)
			updateSettings();

		for (Guest guest : Guest.values()) {
			if (guest.isEnabled()) {
				this.selectedGuest = guest;
				break;
			}
		}
		this.guestSelectionView.setImageDrawable(this.selectedGuest
				.getForkColor().getDrawable(this));
	}

	public void onStop() {
		Log.i("MainActivity", "Stopped");
		if (timerRowListAdapter.getCount() > 0) {
			Gson gson = new Gson();
			TimerRow.FondueTimerSavedInstanceData[] rows = this.timerRowListAdapter
					.toArray();
			String json = gson.toJson(rows);

			SharedPreferences.Editor editor = MainActivity.PREFERENCES.edit();
			editor.putString("storedRows", json);
			editor.commit();

			Log.i("MainActivity.onStop", json);
		}
		super.onStop();
	}

	private void updateSettings() {
		guestSelectionListAdapater = new GuestSelectionListAdapater(this,
				R.layout.color_row, guestSelectionView);
		Guest.UpdateAll(guestSelectionListAdapater);
		guestSelectionListView.getLayoutParams().width = (int) (getWidestView(
				this, guestSelectionListAdapater) * 1.05);
		guestSelectionListView.setAdapter(guestSelectionListAdapater);
		CookingMethod cookingMethod = CookingMethod
				.fromString(MainActivity.PREFERENCES.getString(
						"pref_cooking_method", null));
		Morsel.UpdateAll(cookingMethod);

	}

	public void addTimerRow(View view) {
		Morsel morsel = (Morsel) view.getTag();
		TimerRow newRow = new TimerRow(this, timerListView, handler, morsel,
				this.selectedGuest);
		if (this.cabMode != null) {
			newRow.setMultiselectModeIsActive(true);
		}
		timerRowListAdapter.addTimerRow(newRow, true);
		view.startAnimation(buttonClicked);
	}

	public void showSettings(View view) {
		reloadSettings = true;
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}

	@Override
	public boolean handleMessage(Message message) {
		if (message.what == 0) {
			timerRowListAdapter.updateTimerRowCountdownText();
		} else if (message.what == 1) {
			TimerRow rowToDelete = (TimerRow) message.obj;
			timerRowListAdapter.remove(rowToDelete);
			timerRowListAdapter.notifyDataSetChanged();
		} else if (message.what == 2) {
			// Sent when a row is expired
			if (MainActivity.PREFERENCES.getBoolean("pref_vibrate", false)) {
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(200);
			}
		}
		return true;
	}

	private class MPTimerTask extends TimerTask {
		@Override
		public void run() {
			handler.sendEmptyMessage(0);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.i("MainActivity.onItemClick", "Item Clicked");
		if (arg0 == guestSelectionListView) {
			GuestSelectionRow clickedRow = (GuestSelectionRow) arg1;
			this.selectedGuest = clickedRow.getGuest();
			guestSelectionView.setImageDrawable(this.selectedGuest
					.getForkColor().getDrawable(this));
			guestSelectionPopupWindow.dismiss();
		}

	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
		if (menu.getItemId() == R.id.delete) {
			Log.i("MainActivity.onActionItemClicked", "Delete Button Clicked");
			SparseBooleanArray checked = timerListView
					.getCheckedItemPositions();
			for (int i = timerRowListAdapter.getCount() - 1; i >= 0; i--) {
				if (checked.get(i)) {
					TimerRow rowToDelete = timerRowListAdapter.getItem(i);
					timerRowListAdapter.remove(rowToDelete);
				}
			}
			timerRowListAdapter.notifyDataSetChanged();
			mode.finish();
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		Log.i("MainActivity.onCreateActionMode", "Creating Action Mode");
		timerRowListAdapter.setMultiSelectModeActive(true);
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.context, menu);
		this.cabMode = mode;
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		Log.i("MainActivity.onDestroyActionMode", "Destroying ActionMode");
		timerRowListAdapter.setMultiSelectModeActive(false);
		this.cabMode = mode;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		Log.i("MainActivity.onItemCheckedStateChanged",
				String.valueOf(this.timerListView.getCheckedItemCount()));
		mode.setTitle("Selected " + this.timerListView.getCheckedItemCount());
	}

}
