package es.reinosdeleyenda.flamethrower_lib.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class HyperDialogPreference extends DialogPreference {

	Handler dodefaulter = null;
	
	public HyperDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setHandler(Handler in) {
		dodefaulter = in;
	}
	
	public void onDialogClosed(boolean positiveResult) {
		SharedPreferences pref = this.getSharedPreferences();
		
		SharedPreferences.Editor edit = pref.edit();
		
		if(positiveResult) {
			edit.putString("SETTINGS_TO_DEFAULT", "doit");
			dodefaulter.sendEmptyMessageDelayed(0, 15);
		} else {
			edit.putString("SETTINGS_TO_DEFAULT", "");
		}
		
		edit.commit();
	}

}
