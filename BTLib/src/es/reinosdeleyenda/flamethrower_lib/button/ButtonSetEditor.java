package es.reinosdeleyenda.flamethrower_lib.button;

import java.util.List;

import es.reinosdeleyenda.flamethrower_lib.R;
import es.reinosdeleyenda.flamethrower_lib.service.IConnectionBinder;
import es.reinosdeleyenda.flamethrower_lib.button.ButtonEditorDialog.COLOR_FIELDS;
import es.reinosdeleyenda.flamethrower_lib.settings.ColorSetSettings;
import es.reinosdeleyenda.flamethrower_lib.validator.Validator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;

public class ButtonSetEditor extends Dialog implements ColorPickerDialog.OnColorChangedListener {

	Button normalColor = null;
	Button focusColor = null;
	Button flipColor = null;
	Button labelColor = null;
	Button flipLabelColor = null;
	
	EditText labelSize;
	EditText buttonWidth;
	EditText buttonHeight;
	EditText nameEditor;
	
	ColorSetSettings newsettings;
	ColorSetSettings oldsettings;
	
	CheckBox lockNewButtons = null;
	CheckBox lockMoveButtons = null;
	CheckBox lockEditButtons = null;
	
	Handler notifychanged = null;
	
	IConnectionBinder service;
	String set;
	public ButtonSetEditor(Context context,IConnectionBinder the_service,String selected_set,Handler use_this_handler) {
		super(context);
		service = the_service;
		set = selected_set;
		notifychanged = use_this_handler;
	}
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.buttonset_settings_editor_dialog);
		
		ScrollView sv = (ScrollView)findViewById(R.id.btn_set_editor_scroll);
		sv.setScrollbarFadingEnabled(false);
		//attempt to fetch the settings.
		//ColorSetSettings the_settings =  null;
		//try {
			//newsettings = service.getColorSetDefaultsForSet(set);
		//} catch (RemoteException e) {
		//	throw new RuntimeException(e);
		//}
		//
		oldsettings = newsettings.copy();
		
		normalColor = (Button)findViewById(R.id.btnset_defaultcolor);
		focusColor =(Button)findViewById(R.id.btnset_focuscolor);
		flipColor = (Button)findViewById(R.id.btnset_flippedcolor);
		labelColor = (Button)findViewById(R.id.btnset_labelcolor);
		flipLabelColor = (Button)findViewById(R.id.btnset_fliplabelcolor);
		
		lockNewButtons = (CheckBox)findViewById(R.id.locknewbuttons);
		lockMoveButtons = (CheckBox)findViewById(R.id.lockmovebuttons);
		lockEditButtons = (CheckBox)findViewById(R.id.lockeditbuttons);
		
		
		lockNewButtons.setChecked(oldsettings.isLockNewButtons());
		lockMoveButtons.setChecked(oldsettings.isLockMoveButtons());
		lockEditButtons.setChecked(oldsettings.isLockEditButtons());
		
		
		nameEditor = (EditText)findViewById(R.id.name);
		nameEditor.setText(set);
		nameEditor.setSelection(set.length());
		if(set.equals("default")) {
			nameEditor.setEnabled(false); //can not edit default set name
		}
		//normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		normalColor.setBackgroundColor(oldsettings.getPrimaryColor());
		focusColor.setBackgroundColor(oldsettings.getSelectedColor());
		labelColor.setBackgroundColor(oldsettings.getLabelColor());
		flipColor.setBackgroundColor(oldsettings.getFlipColor());
		flipLabelColor.setBackgroundColor(oldsettings.getFlipLabelColor());
		
		normalColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getPrimaryColor());
				diag.show();
			}
		});
		
		focusColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getSelectedColor());
				diag.show();
			}
		});
		
		labelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getLabelColor());
				diag.show();
			}
		});
		
		flipColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getFlipColor());
				diag.show();
			}
		});
		
		flipLabelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonSetEditor.this.getContext(),ButtonSetEditor.this,newsettings.getFlipLabelColor());
				diag.show();
			}
		});
		
		
		labelSize = (EditText)findViewById(R.id.btnset_editor_lblsize_et);
		buttonHeight = (EditText)findViewById(R.id.btnset_editor_height_et);
		buttonWidth = (EditText)findViewById(R.id.btnset_editor_width_et);
		
		labelSize.setText(new Integer(oldsettings.getLabelSize()).toString());
		buttonHeight.setText(new Integer(oldsettings.getButtonHeight()).toString());
		buttonWidth.setText(new Integer(oldsettings.getButtonWidth()).toString());
		
		Button done = (Button)findViewById(R.id.btnset_done_btn);
		Button cancel = (Button)findViewById(R.id.btnset_cancel_btn);
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ButtonSetEditor.this.dismiss();
			}
		});
		
		done.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				Validator checker = new Validator();
				checker.add(buttonHeight, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Button Height");
				checker.add(buttonWidth, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Button Width");
				checker.add(labelSize, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER|Validator.VALIDATE_NUMBER_NOT_ZERO, "Label Size");
				
				String result = checker.validate();
				if(result != null) {
					checker.showMessage(ButtonSetEditor.this.getContext(), result);
					return;
				}
				
				//get set names
				List<String> takenNames = null;
				//try {
					//takenNames = service.getButtonSetNames();
				//} catch (RemoteException e1) {
					// TODO Auto-generated catch block
				//	e1.printStackTrace();
				//}
				
				for(String str : takenNames) {
					if(nameEditor.getText().toString().equals(str) && !nameEditor.getText().toString().equals(set)) {
						checker.showMessage(ButtonSetEditor.this.getContext(), nameEditor.getText().toString() + " is an existing button set.");
						return;
					}
				}
				
				
				newsettings.setButtonHeight(Integer.parseInt(buttonHeight.getText().toString()));
				newsettings.setButtonWidth(Integer.parseInt(buttonWidth.getText().toString()));
				newsettings.setLabelSize(Integer.parseInt(labelSize.getText().toString()));
				
				newsettings.setLockNewButtons(lockNewButtons.isChecked());
				newsettings.setLockMoveButtons(lockMoveButtons.isChecked());
				newsettings.setLockEditButtons(lockEditButtons.isChecked());
				
				if(!(nameEditor.getText().toString().equals(set))) {
					//try {
						//service.updateAndRenameSet(set, nameEditor.getText().toString(), newsettings);
					//} catch (RemoteException e) {
						// TODO Auto-generated catch block
					//	e.printStackTrace();
					//}
					notifychanged.sendMessage(notifychanged.obtainMessage(101,nameEditor.getText().toString()));
					ButtonSetEditor.this.dismiss();
					return;
				} 
				
				if(newsettings.equals(oldsettings)) {

				} else {
					//changes made, notify the service that the change has been made, and notify the settings dialog that when it exits it needs to reload whatever button set changed.
					//try {
						//service.setColorSetDefaultsForSet(set, newsettings);
					//} catch (RemoteException e) {
					//	throw new RuntimeException(e);
					//}
					notifychanged.sendEmptyMessage(100);
				}
				ButtonSetEditor.this.dismiss();
			}
		});
		
	}

	public void colorChanged(int color) {
		COLOR_FIELDS which = COLOR_FIELDS.COLOR_MAIN;
		switch(which) {
		case COLOR_MAIN:
			newsettings.setPrimaryColor(color);
			normalColor.setBackgroundColor(color);
			break;
		case COLOR_SELECTED:
			newsettings.setSelectedColor(color);
			focusColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPPED:
			flipColor.setBackgroundColor(color);
			newsettings.setFlipColor(color);
			break;
		case COLOR_LABEL:
			newsettings.setLabelColor(color);
			labelColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPLABEL:
			newsettings.setFlipLabelColor(color);
			flipLabelColor.setBackgroundColor(color);
			break;
		default:
			break;
		}
	}

}
