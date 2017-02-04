package com.meridian.fonduetimer;

import com.meridian.fonduetimer.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	protected SharedPreferences preferences;
	GuestPreferences guest1;
	GuestPreferences guest2;
	GuestPreferences guest3;
	GuestPreferences guest4;
	
	SwitchPreference vibratePreference;
	Preference cookingMethodPreference;
	CookingTimePreferenceSet brothCookingTimePreferenceSet;
	CookingTimePreferenceSet oilCookingTimePreferenceSet;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("SettingsFragment.onCreate", "Starting on Create");
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		guest1 = new GuestPreferences("pref_guest1", "pref_guest1_enabled", "pref_guest1_name", "pref_guest1_color");
		guest1.loadAll();
		guest2 = new GuestPreferences("pref_guest2", "pref_guest2_enabled", "pref_guest2_name", "pref_guest2_color");
		guest2.loadAll();
		guest3 = new GuestPreferences("pref_guest3", "pref_guest3_enabled", "pref_guest3_name", "pref_guest3_color");
		guest3.loadAll();
		guest4 = new GuestPreferences("pref_guest4", "pref_guest4_enabled", "pref_guest4_name", "pref_guest4_color");
		guest4.loadAll();
		
		vibratePreference = (SwitchPreference) this.findPreference("pref_vibrate");
		
		cookingMethodPreference = this.findPreference("pref_cooking_method");
		brothCookingTimePreferenceSet = new CookingTimePreferenceSet("pref_broth_chicken_time", "pref_broth_beef_time", "pref_broth_pork_time", "pref_broth_seafood_time", "pref_broth_vegetable_time");
		oilCookingTimePreferenceSet = new CookingTimePreferenceSet("pref_oil_chicken_time", "pref_oil_beef_time", "pref_oil_pork_time", "pref_oil_seafood_time", "pref_oil_vegetable_time");
		

		cookingMethodPreference.setSummary(preferences.getString(cookingMethodPreference.getKey(), "Broth"));
		brothCookingTimePreferenceSet.updateSummaries();
		oilCookingTimePreferenceSet.updateSummaries();
		
		
		preferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	public void onResume() {
		super.onResume();
		Log.i("SettingsFragment.onResume", "On Resume");
	}
	
	public void onStart() {
		super.onStart();
		Log.i("SettingsFragment.onStart", "On Start");
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if(key.equals("pref_guest1_enabled")) {
			guest1.loadAll();
		} else if(key.equals("pref_guest1_name")) {
			guest1.loadName();
		} else if(key.equals("pref_guest1_color")) {
			guest1.loadIcon();
		} else if(key.equals("pref_guest2_enabled")) {
			guest2.loadAll();
		} else if(key.equals("pref_guest2_name")) {
			guest2.loadName();
		} else if(key.equals("pref_guest2_color")) {
			guest2.loadIcon();
		} else if(key.equals("pref_guest3_enabled")) {
			guest3.loadAll();
		} else if(key.equals("pref_guest3_name")) {
			guest3.loadName();
		} else if(key.equals("pref_guest3_color")) {
			guest3.loadIcon();
		} else if(key.equals("pref_guest4_enabled")) {
			guest4.loadAll();
		} else if(key.equals("pref_guest4_name")) {
			guest4.loadName();
		} else if(key.equals("pref_guest4_color")) {
			guest4.loadIcon();
		} else if (key.equals("pref_cooking_method")) {
			cookingMethodPreference.setSummary(preferences.getString(cookingMethodPreference.getKey(), "Broth"));
		} else if(key.contains("time")) {
			brothCookingTimePreferenceSet.updateSummaries();
			oilCookingTimePreferenceSet.updateSummaries();	
		}
	}

	private Preference findPref(String key) {
		return this.findPreference(key);
	}
	
	private int getPreferenceDrawable(String key) {
		if(key.equals("Black")) { return R.drawable.square_black; }
		else if(key.equals("Blue")) { return R.drawable.square_blue; }
		else if(key.equals("Brown")) { return R.drawable.square_brown; }
		else if(key.equals("Green")) { return R.drawable.square_green; }
		else if(key.equals("Pink")) { return R.drawable.square_pink; }
		else if(key.equals("Red")) { return R.drawable.square_red; }
		else if(key.equals("Teal")) { return R.drawable.square_teal; }
		else if(key.equals("White")) { return R.drawable.square_white; }
		else if(key.equals("Yellow")) { return R.drawable.square_yellow; }
		else return -1;
		
	}
	
	private class GuestPreferences {
		Preference topLevelGuestPreference;
		Preference guestNamePerference;
		Preference guestColorPreference;
		String guestKey;
		String enabledKey;
		String guestNameKey;
		String guestColorKey;
		
		public GuestPreferences(String guestKey, String enabledKey, String guestNameKey, String guestColorKey) {
			this.guestKey = guestKey;
			this.enabledKey = enabledKey;
			this.guestNameKey = guestNameKey;
			this.guestColorKey = guestColorKey;
			this.topLevelGuestPreference = findPref(this.guestKey);
			this.guestNamePerference = findPref(this.guestNameKey);
			this.guestColorPreference = findPref(this.guestColorKey);
		}
		
		public void loadAll() {
			this.loadName();
			this.loadIcon();
			
			// Workaround (read hack) to force update the topLevelGuestPreference icon
			if(vibratePreference != null) {
				boolean val = preferences.getBoolean(vibratePreference.getKey(), false);
				vibratePreference.setChecked(!val);
				vibratePreference.setChecked(val);
			}
		}
		
		public void loadName() {
			if(preferences.getBoolean(this.enabledKey, false)) {
				String name = preferences.getString(guestNameKey, "");	
				this.topLevelGuestPreference.setSummary(name);
				this.guestNamePerference.setSummary(name);		
			} else {
				this.topLevelGuestPreference.setSummary("Disabled");
				this.guestNamePerference.setSummary("");		
			}
		}
		
		public void loadIcon() {
			if(preferences.getBoolean(this.enabledKey, false)) {
				Log.i("SettingsFragment.loadIcon", "Loading Icon");
				int drawableId = getPreferenceDrawable(preferences.getString(guestColorKey, ""));
				this.topLevelGuestPreference.setIcon(drawableId);
				this.guestColorPreference.setIcon(drawableId);
				
			} else {
				this.topLevelGuestPreference.setIcon(R.drawable.null_icon);
				this.guestColorPreference.setIcon(R.drawable.null_icon);
			}
			
			// Workaround (read hack) to force update the topLevelGuestPreference icon
			if(vibratePreference != null) {
				boolean val = preferences.getBoolean(vibratePreference.getKey(), false);
				vibratePreference.setChecked(!val);
				vibratePreference.setChecked(val);
			}
		}
	}

	private class CookingTimePreferenceSet {
		Preference chickenTime;
		Preference beefTime;
		Preference porkTime;
		Preference seafoodTime;
		Preference vegetableTime;
		
		public CookingTimePreferenceSet(String chickenTimeKey, String beefTimeKey, String porkTimeKey, String seafoodTimeKey, String vegetableTimeKey) {
			chickenTime = findPref(chickenTimeKey);
			beefTime = findPref(beefTimeKey);
			porkTime = findPref(porkTimeKey);
			seafoodTime = findPref(seafoodTimeKey);
			vegetableTime = findPref(vegetableTimeKey);
		}
		
		public void updateSummaries() {
			chickenTime.setSummary(preferences.getString(chickenTime.getKey(), "Unavaliable"));
			beefTime.setSummary(preferences.getString(beefTime.getKey(), "Unavaliable"));
			porkTime.setSummary(preferences.getString(porkTime.getKey(), "Unavaliable"));
			seafoodTime.setSummary(preferences.getString(seafoodTime.getKey(), "Unavaliable"));
			vegetableTime.setSummary(preferences.getString(vegetableTime.getKey(), "Unavaliable"));
		}
	}
}
