package es.reinosdeleyenda.flamethrower_lib.settings;

import es.reinosdeleyenda.flamethrower_lib.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import es.reinosdeleyenda.flamethrower_lib.button.ColorPickerDialog;

public class ColorSwatchPreference extends Preference implements ColorPickerDialog.OnColorChangedListener {

	public int colorValue = 0xFF883333;
	private Button theView = null;
	
	public ColorSwatchPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public ColorSwatchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ColorSwatchPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public View onCreateView(ViewGroup root) {
		View v = super.onCreateView(root);
		//read out preference value.
		SharedPreferences prefs = this.getSharedPreferences();
		
		int val = prefs.getInt(this.getKey(), 0xFF883333);
		
		colorValue = val;
		
		return v;
		
	}
	
	public void onBindView(View v) {
		
		Button b = (Button) v.findViewById(R.id.colorswatch);
		TextView summary = (TextView) v.findViewById(android.R.id.summary);
		summary.setText("Text for your viewing pleasuer.");
		TextView title = (TextView)v.findViewById(android.R.id.title);
		title.setText("Hyperlink Color");
		b.setBackgroundColor(colorValue);
		
		b.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View v) {
				//
				ColorPickerDialog d = new ColorPickerDialog(v.getContext(), ColorSwatchPreference.this, colorValue);
				d.show();
			}
		});
		
		theView = b;
		
	}

	//@Override
	public void colorChanged(int color) {

		theView.setBackgroundColor(color);
		colorValue = color;
		
		SharedPreferences.Editor edit = this.getSharedPreferences().edit();
		
		edit.putInt(this.getKey(), color);
		
		edit.commit();
		
	}
	
	public void onDependencyChanged(Preference dependency, boolean disableDependent) {
		Log.e("SWATCH",dependency.getKey() + "is telling me " + Boolean.toString(disableDependent));
		if(theView != null) {
			if(disableDependent) {
				theView.setEnabled(false);
			} else {
				theView.setEnabled(true);
			}
		}
		this.setEnabled(!disableDependent);
		//super.onDependencyChanged(dependency, disableDependent);
	}
	
	
	
}
