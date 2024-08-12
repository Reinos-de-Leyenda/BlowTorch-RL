package es.reinosdeleyenda.flamethrower_lib.button;

import es.reinosdeleyenda.flamethrower_lib.R;
import es.reinosdeleyenda.flamethrower_lib.validator.Validator;
import es.reinosdeleyenda.flamethrower_lib.window.MainWindow;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class ButtonEditorDialog extends Dialog implements ColorPickerDialog.OnColorChangedListener {
	
	final int EXIT_CANCEL = 0;
	final int EXIT_DONE = 1;
	final int EXIT_DELETE = 2;
	
	public String mod_cmd = null;
	public String mod_lbl = null;
	public int EXIT_STATE = EXIT_CANCEL;
	
	Handler deleter = null;
	SlickButton the_button = null;
	
	CheckBox move_free = null;
	CheckBox move_nudge = null;
	CheckBox move_freeze = null;
	
	Button normalColor = null;
	Button focusColor = null;
	Button flipColor = null;
	Button labelColor = null;
	Button flipLabelColor = null;
	
	EditText labelSize;
	EditText xPos;
	EditText yPos;
	EditText eWidth;
	EditText eHeight;
	
	EditText targetSet;
	
	//SlickButtonData orig_data = null;
	
	public ButtonEditorDialog(Context context,SlickButton useme,Handler callback) {
		super(context);
		
		//mod_cmd = cmd;
		//mod_lbl = lbl;
		
		the_button = useme;
		deleter = callback;
		//orig_data = useme.getData().copy();
	}
	
	public ButtonEditorDialog(Context context,int themeid,SlickButton useme,Handler callback) {
		super(context,themeid);
		
		//mod_cmd = cmd;
		//mod_lbl = lbl;
		
		the_button = useme;
		deleter = callback;
		//orig_data = useme.getData().copy();
	}
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		//this.setTitle("Modify Button Properties...");
		setContentView(R.layout.button_properties_dialog_tabbed);
		
		TabHost thost = (TabHost)findViewById(R.id.btn_editor_tabhost);
		
		thost.setup();
		
		TabSpec tab1 = (TabSpec) thost.newTabSpec("tab_one_btn_tab");
		TextView lbl1 = new TextView(this.getContext());
		lbl1.setText("Click");
		lbl1.setGravity(Gravity.CENTER);
		lbl1.setBackgroundResource(R.drawable.tab_background);
		
		//lbl1.setHeight(20);
		tab1.setIndicator(lbl1);
		tab1.setContent(R.id.btn_editor_tab1);
		thost.addTab(tab1);
		
		TabSpec tab2 = (TabSpec) thost.newTabSpec("tab_two_btn_tab");
		TextView lbl2 = new TextView(this.getContext());
		lbl2.setText("Flip");
		lbl2.setGravity(Gravity.CENTER);
		lbl2.setBackgroundResource(R.drawable.tab_background);
		tab2.setIndicator(lbl2);
		tab2.setContent(R.id.btn_editor_tab2);
		thost.addTab(tab2);
		
		TabSpec tab3 = (TabSpec) thost.newTabSpec("tab_three_btn_tab");
		TextView lbl3 = new TextView(this.getContext());
		lbl3.setText("Advanced");
		lbl3.setGravity(Gravity.CENTER);
		lbl3.setBackgroundResource(R.drawable.tab_background);
		tab3.setIndicator(lbl3);
		tab3.setContent(R.id.btn_editor_tab3);
		thost.addTab(tab3);
		
		thost.setCurrentTab(0);
		
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		EditText label = (EditText)findViewById(R.id.button_text_et);
		label.setText(the_button.getData().getLabel());
		
		EditText command = (EditText)findViewById(R.id.button_command_et);
		command.setText(the_button.getData().getText());
		
		EditText flip = (EditText)findViewById(R.id.button_flip_et);
		flip.setText(the_button.getData().getFlipCommand());
		
		EditText fliplabel = (EditText)findViewById(R.id.button_flip_label_et);
		fliplabel.setText(the_button.getData().getFlipLabel());
		
		ScrollView sv = (ScrollView)findViewById(R.id.btn_editor_advanced_scroll_containter);
		sv.setScrollbarFadingEnabled(false);
		
		//Button fitbutton = (Button)findViewById(R.id.fit);
		//fitbutton.setOnClickListener(new FitClickListener());
		
		move_free = (CheckBox)findViewById(R.id.move_free);
		move_nudge = (CheckBox)findViewById(R.id.move_nudge);
		move_freeze = (CheckBox)findViewById(R.id.move_freeze);
		//set up radio button handling.
		//set initial checked value
		////Log.e("BTNEDITOR","INITIALIZING DIALOG WITH:" + the_button.getMoveMethod());
		switch(the_button.getMoveMethod()) {
		case SlickButtonData.MOVE_FREE:
			move_free.setChecked(true);
			move_nudge.setChecked(false);
			move_freeze.setChecked(false);
			break;
		case SlickButtonData.MOVE_NUDGE:
			move_free.setChecked(false);
			move_nudge.setChecked(true);
			move_freeze.setChecked(false);
			break;
		case SlickButtonData.MOVE_FREEZE:
			move_free.setChecked(false);
			move_nudge.setChecked(false);
			move_freeze.setChecked(true);
			break;
		}
		
		
		move_free.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_free.isChecked()) {
					move_free.setChecked(true);
					move_nudge.setChecked(false);
					move_freeze.setChecked(false);
				//}
			}
		});
		
		move_nudge.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_nudge.isChecked()) {
					move_free.setChecked(false);
					move_nudge.setChecked(true);
					move_freeze.setChecked(false);
				//}
			}
		});
		
		move_freeze.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				//if(!move_freeze.isChecked()) {
					move_free.setChecked(false);
					move_nudge.setChecked(false);
					move_freeze.setChecked(true);
				//}
			}
		});
		//Button normalColor = null;
		//Button focusColor = null;
		//Button flipColor = null;
		//Button labelColor = null;
		//Button flipLabelColor = null;
		
		
		normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		focusColor =(Button)findViewById(R.id.btn_focuscolor);
		flipColor = (Button)findViewById(R.id.btn_flippedcolor);
		labelColor = (Button)findViewById(R.id.btn_labelcolor);
		flipLabelColor = (Button)findViewById(R.id.btn_fliplabelcolor);
		//normalColor = (Button)findViewById(R.id.btn_defaultcolor);
		normalColor.setBackgroundColor(the_button.getData().getPrimaryColor());
		focusColor.setBackgroundColor(the_button.getData().getSelectedColor());
		labelColor.setBackgroundColor(the_button.getData().getLabelColor());
		flipColor.setBackgroundColor(the_button.getData().getFlipColor());
		flipLabelColor.setBackgroundColor(the_button.getData().getFlipLabelColor());
		
		

		
		normalColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getPrimaryColor());
				diag.show();
			}
		});
		
		focusColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getSelectedColor());
				diag.show();
			}
		});
		
		labelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getLabelColor());
				diag.show();
			}
		});
		
		flipColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getFlipColor());
				diag.show();
			}
		});
		
		flipLabelColor.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				ColorPickerDialog diag = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,the_button.getData().getFlipLabelColor());
				diag.show();
			}
		});
		
		labelSize = (EditText)findViewById(R.id.btn_editor_lblsize_et);
		xPos = (EditText)findViewById(R.id.btn_editor_xcoord_et);
		yPos = (EditText)findViewById(R.id.btn_editor_ycoord_et);
		eWidth = (EditText)findViewById(R.id.btn_editor_width_et);
		eHeight = (EditText)findViewById(R.id.btn_editor_height_et);
		
		labelSize.setText(new Integer(this.the_button.getData().getLabelSize()).toString());
		xPos.setText(new Integer(the_button.getData().getX()).toString());
		yPos.setText(new Integer(the_button.getData().getY()).toString());
		eWidth.setText(new Integer(the_button.getData().getWidth()).toString());
		eHeight.setText(new Integer(the_button.getData().getHeight()).toString());
		
		targetSet = (EditText)findViewById(R.id.btn_editor_targetset_et);
		targetSet.setText(the_button.getData().getTargetSet());
		
		Button delbutton = (Button)findViewById(R.id.button_delete_btn);
		
		delbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				EXIT_STATE = EXIT_DELETE;
				//Message msg = deleter.obtainMessage(ByteView.MSG_REALLYDELETEBUTTON, the_button);
				//deleter.sendMessage(msg);
				ButtonEditorDialog.this.dismiss();
			}
		});
		
		Button donebutton = (Button)findViewById(R.id.button_done_btn);
		donebutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				//ColorPickerDialog cpd = new ColorPickerDialog(ButtonEditorDialog.this.getContext(),ButtonEditorDialog.this,0xFF00FF00,COLOR_FIELDS.COLOR_MAIN);
				//cpd.show();
				
				//big ugly validation step.
				//labels/commands can be empty or whatever I don't care.
				//all numeric fields must be numbers and greater than 0.
				boolean passed = validate();
				if(!passed) {
					return;
				}
				/*Validator checker = new Validator();
				checker.add(xPos, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "X Coordinate");
				checker.add(yPos, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Y Coordinate");
				checker.add(eWidth, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Width");
				checker.add(eHeight, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Height");
				checker.add(labelSize, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Label Size");
				
				String result = checker.validate();
				if(result != null) {
					checker.showMessage(ButtonEditorDialog.this.getContext(), result);
					return;
				}*/
				
				updateData();
				/*EditText label = (EditText)findViewById(R.id.button_text_et);
				
				
				EditText command = (EditText)findViewById(R.id.button_command_et);
				EditText flip = (EditText)findViewById(R.id.button_flip_et);
				EditText fliplbl = (EditText)findViewById(R.id.button_flip_label_et);
				the_button.setLabel(label.getText().toString());
				the_button.setText(command.getText().toString());
				the_button.setFlipCommand(flip.getText().toString());
				the_button.getData().setFlipLabel(fliplbl.getText().toString());
				the_button.getData().setLabelSize(new Integer(labelSize.getText().toString()));
				the_button.getData().setX(new Integer(xPos.getText().toString()));
				the_button.getData().setY(new Integer(yPos.getText().toString()));
				the_button.getData().setWidth(new Integer(eWidth.getText().toString()));
				the_button.getData().setHeight(new Integer(eHeight.getText().toString()));
				the_button.getData().setTargetSet(targetSet.getText().toString());
				CheckBox tfree = (CheckBox)findViewById(R.id.move_free);
				CheckBox tnudge = (CheckBox)findViewById(R.id.move_nudge);
				CheckBox tfreeze = (CheckBox)findViewById(R.id.move_freeze);
				
				if(tfree.isChecked()) {
					//Log.e("BTNEDITOR","SAVING WITH MOVE_FREE");
					the_button.setMoveMethod(SlickButtonData.MOVE_FREE);
				}
				
				if(tnudge.isChecked()) {
					//Log.e("BTNEDITOR","SAVING WITH MOVE_NUDGE");
					the_button.setMoveMethod(SlickButtonData.MOVE_NUDGE);
				}
				
				if(tfreeze.isChecked()) {
					the_button.setMoveMethod(SlickButtonData.MOVE_FREEZE);
				}*/
				
				//do the check for the button height/width.
				/*Paint opts = new Paint();
				opts.setTypeface(Typeface.DEFAULT_BOLD);
				opts.setTextSize(the_button.getData().getLabelSize()*ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density);
				//opts.setF
				
				opts.setFlags(Paint.ANTI_ALIAS_FLAG);
				
				float length = opts.measureText(the_button.getData().getLabel());
				float length2 = opts.measureText(the_button.getData().getFlipLabel());
				float height = the_button.getData().getLabelSize();
				
				boolean needsfit = false;
				
				float density = ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density;
				float lengthtofit = the_button.getData().getWidth()*density;
				//Log.e("BUTTONEDITOR","LENGTH CALC: " + length2 + " width:" + the_button.getData().getWidth());
				if(length/density > the_button.getData().getWidth() || length2/density > the_button.getData().getWidth()) {
					needsfit = true;
				}
				if(length > length2) {
					lengthtofit = length;
				} else {
					lengthtofit = length2;
				}
				//Log.e("BUTTONEDITOR","HEIGHT CALC: " + height + " height:" + the_button.getData().getHeight());
				if(height > the_button.getHeight()) {
					needsfit = true;
				}
				
				if(needsfit) {
					AlertDialog.Builder b = new AlertDialog.Builder(ButtonEditorDialog.this.getContext());
					b.setMessage("The button label has exceeded the bounds of the button. Fit button to label?");
					b.setTitle("Label too big.");
					//float density = ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density;
					b.setPositiveButton("Fit please.", new FitClickListener((int)(height+20*density),(int)((lengthtofit/density)+(int)20*density)));
					b.setNegativeButton("No thanks.", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
						
					});
					
					AlertDialog dialog = b.create();
					dialog.show();
					
				}*/
				
				
				
				
				the_button.dialog_launched = false;
				the_button.iHaveChanged(the_button.orig_data);
				the_button.invalidate();
				EXIT_STATE = EXIT_DONE;
				ButtonEditorDialog.this.dismiss();
			}
		}); 
	}
	
	private boolean validate() {
		Validator checker = new Validator();
		checker.add(xPos, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "X Coordinate");
		checker.add(yPos, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Y Coordinate");
		checker.add(eWidth, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Width");
		checker.add(eHeight, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Height");
		checker.add(labelSize, Validator.VALIDATE_NOT_BLANK|Validator.VALIDATE_NUMBER_NOT_ZERO, "Label Size");
		
		String result = checker.validate();
		if(result != null) {
			checker.showMessage(ButtonEditorDialog.this.getContext(), result);
			return false;
		}
		return true;
	}
	
	private void updateData() {
		EditText label = (EditText)findViewById(R.id.button_text_et);
		
		
		EditText command = (EditText)findViewById(R.id.button_command_et);
		EditText flip = (EditText)findViewById(R.id.button_flip_et);
		EditText fliplbl = (EditText)findViewById(R.id.button_flip_label_et);
		the_button.setLabel(label.getText().toString());
		the_button.setText(command.getText().toString());
		the_button.setFlipCommand(flip.getText().toString());
		the_button.getData().setFlipLabel(fliplbl.getText().toString());
		the_button.getData().setLabelSize(new Integer(labelSize.getText().toString()));
		the_button.getData().setX(new Integer(xPos.getText().toString()));
		the_button.getData().setY(new Integer(yPos.getText().toString()));
		the_button.getData().setWidth(new Integer(eWidth.getText().toString()));
		the_button.getData().setHeight(new Integer(eHeight.getText().toString()));
		the_button.getData().setTargetSet(targetSet.getText().toString());
		CheckBox tfree = (CheckBox)findViewById(R.id.move_free);
		CheckBox tnudge = (CheckBox)findViewById(R.id.move_nudge);
		CheckBox tfreeze = (CheckBox)findViewById(R.id.move_freeze);
		
		if(tfree.isChecked()) {
			//Log.e("BTNEDITOR","SAVING WITH MOVE_FREE");
			the_button.setMoveMethod(SlickButtonData.MOVE_FREE);
		}
		
		if(tnudge.isChecked()) {
			//Log.e("BTNEDITOR","SAVING WITH MOVE_NUDGE");
			the_button.setMoveMethod(SlickButtonData.MOVE_NUDGE);
		}
		
		if(tfreeze.isChecked()) {
			the_button.setMoveMethod(SlickButtonData.MOVE_FREEZE);
		}
	}
	
	public void onBackPressed() {
		//the_button.iHaveChanged(the_button.orig_data);
		the_button.moving = false;
		the_button.button_down = false;
		the_button.doing_flip = false;
		the_button.hasfocus = false;
		the_button.dialog_launched = false;
		//the_button.
		the_button.invalidate();
		this.dismiss();
	}
	
	private class FitClickListener implements View.OnClickListener {

		private int height;
		private int width;
		
		public FitClickListener() {
			//height = pHeight;
			//width = pWidth;
		}
		
		public void onClick(View v) {
			SlickButtonData fitbutton = the_button.getData().copy();
			
			//boolean needsfit = false;
			
			Paint opts = new Paint();
			opts.setTypeface(Typeface.DEFAULT_BOLD);
			opts.setTextSize(the_button.getData().getLabelSize()*ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density);
			//opts.setF
			
			opts.setFlags(Paint.ANTI_ALIAS_FLAG);
			
			float length = opts.measureText(the_button.getData().getLabel());
			float length2 = opts.measureText(the_button.getData().getFlipLabel());
			float height = the_button.getData().getLabelSize();
			
			float density = ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density;
			float lengthtofit = the_button.getData().getWidth()*density;
			//Log.e("BUTTONEDITOR","LENGTH CALC: " + length2 + " width:" + the_button.getData().getWidth());
			if(length/density > the_button.getData().getWidth() || length2/density > the_button.getData().getWidth()) {
				//needsfit = true;
			}
			if(length > length2) {
				lengthtofit = length;
			} else {
				lengthtofit = length2;
			}
			//Log.e("BUTTONEDITOR","HEIGHT CALC: " + height + " height:" + the_button.getData().getHeight());
			if(height > the_button.getHeight()) {
				//needsfit = true;
			}
			
			/*if(needsfit) {
				AlertDialog.Builder b = new AlertDialog.Builder(ButtonEditorDialog.this.getContext());
				b.setMessage("The button label has exceeded the bounds of the button. Fit button to label?");
				b.setTitle("Label too big.");
				//float density = ButtonEditorDialog.this.getContext().getResources().getDisplayMetrics().density;
				b.setPositiveButton("Fit please.", new FitClickListener((int)(height+20*density),(int)((lengthtofit/density)+(int)20*density)));
				b.setNegativeButton("No thanks.", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
					
				});
				
				AlertDialog dialog = b.create();
				dialog.show();
				
			}*/
			boolean passed = validate();
			if(!passed) {
				return;
			}
			updateData();
			
			fitbutton.setWidth((int)((lengthtofit/density)+(int)20*density));
			fitbutton.setHeight((int)(height+20*density));
			the_button.iHaveChanged(the_button.orig_data);
			the_button.invalidate();
			
			
			Message msg = deleter.obtainMessage(MainWindow.MESSAGE_MODIFYBUTTON);
			Bundle b = msg.getData();
			b.putParcelable("ORIG_DATA", the_button.getData());
			b.putParcelable("MOD_DATA", fitbutton);
			msg.setData(b);
			deleter.sendMessage(msg);
		}
		
	}
	
	public enum COLOR_FIELDS {
		COLOR_MAIN,
		COLOR_SELECTED,
		COLOR_FLIPPED,
		COLOR_LABEL,
		COLOR_FLIPLABEL
	}

	
	public void colorChanged(int color) {
		COLOR_FIELDS which = COLOR_FIELDS.COLOR_MAIN;
		switch(which) {
		case COLOR_MAIN:
			the_button.getData().setPrimaryColor(color);
			normalColor.setBackgroundColor(color);
			break;
		case COLOR_SELECTED:
			the_button.getData().setSelectedColor(color);
			focusColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPPED:
			flipColor.setBackgroundColor(color);
			the_button.getData().setFlipColor(color);
			break;
		case COLOR_LABEL:
			the_button.getData().setLabelColor(color);
			labelColor.setBackgroundColor(color);
			break;
		case COLOR_FLIPLABEL:
			the_button.getData().setFlipLabelColor(color);
			flipLabelColor.setBackgroundColor(color);
			break;
		default:
			break;
		}
		
	}

}
