package es.reinosdeleyenda.flamethrower_lib.trigger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import es.reinosdeleyenda.flamethrower_lib.R;
import es.reinosdeleyenda.flamethrower_lib.service.IConnectionBinder;
import es.reinosdeleyenda.flamethrower_lib.window.AnimatedRelativeLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TriggerSelectionDialog extends Dialog {

	private ListView list;
	private IConnectionBinder service;
	private List<TriggerItem> entries;
	private TriggerListAdapter adapter;
	//private int lastSelectedIndex = -1;
	String currentPlugin = "main";
	ListView mOptionsList;
	boolean mOptionsListToggle = false;
	
	private int lastSelectedIndex = -1;
	private RelativeLayout targetHolder = null;
	private int targetIndex = -1;
	private Button mOptionsButton = null;
	int toolbarLength = 0;
	LayoutAnimationController animateInController = null;
	TranslateAnimation animateOut = null;
	TranslateAnimation animateOutNoTransition = null;
	
	LinearLayout theToolbar = null;
	
	public TriggerSelectionDialog(Context context,IConnectionBinder the_service) {
		super(context);
		service = the_service;
		entries = new ArrayList<TriggerItem>();
	}
	
	//private boolean noTriggers = false;
	public class CustomAnimationEndListener implements AnimatedRelativeLayout.OnAnimationEndListener {

		@Override
		public void onCustomAnimationEnd() {
			
			RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
			if(rl == null) {
				return;
			}
			rl.removeAllViews();

			if(targetHolder != null) {
				//set the image view.
				TriggerItem data = adapter.getItem(targetIndex);
				if(data.enabled) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
				}
				targetHolder.setLayoutAnimation(animateInController);
				
				targetHolder.addView(theToolbar);
			}
			lastSelectedIndex = targetIndex;
		}
		
	}
	
	public CustomAnimationEndListener mCustomAnimationListener = new CustomAnimationEndListener();
	
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		
		setContentView(R.layout.editor_selection_dialog);
		
		//initialize the list view
		list = (ListView)findViewById(R.id.list);
		
		list.setScrollbarFadingEnabled(false);
		
		list.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		//list.setOnItemClickListener(new EditTriggerListener());
		//list.setOnItemLongClickListener(new DeleteTriggerListener());
		//list.setFocusable(false);
		//list.setFocusableInTouchMode(false);
		
		//list.setOnI
		//attempt to fish out the trigger list.
		
		/*list.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.e("WINDOW","LISTVIEW HAS FOCUS");
			}
			
		});*/
		//list.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//we just want to have one
				arg1.performClick();
				Log.e("CLICK","CLICK CLICK CLICK CLICK");
			}
		});
		
		list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(arg2 != lastSelectedIndex) {
					//arg0.is
					if(arg0.getFirstVisiblePosition() <= lastSelectedIndex && arg0.getLastVisiblePosition() >= lastSelectedIndex) {
						if(theToolbar.getParent() != null) {
							theToolbar.startAnimation(animateOutNoTransition);
						}
					} else {
						if(theToolbar.getParent() != null) {
							((RelativeLayout)theToolbar.getParent()).removeAllViews();
						}
					}
				}
				lastSelectedIndex = arg2;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
				Log.e("LIST","NOTHING SELECTED");
				
			}
		});
		
		//list.setOnFocusChangeListener(new ListFocusFixerListener());
		
		list.setSelector(R.drawable.transparent);
		
		
		list.setEmptyView(findViewById(R.id.empty));
		buildList();
		
		Button newbutton = (Button)findViewById(R.id.add);
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),null,service,triggerEditorDoneHandler,currentPlugin,true);
				editor.show();
			}
		});
		
		
		
		Button cancelbutton = (Button)findViewById(R.id.done);
		
		cancelbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				TriggerSelectionDialog.this.dismiss();
			}
			
		});
	
		//gett he plugin list.
		try {
			List<String> pluginList = (List<String>)service.getPluginsWithTriggers();
			
			plugins = new String[pluginList.size()+4];
			plugins[0] = "Help";
			plugins[1] = "Disable All";
			plugins[2] = "divider";
			plugins[3] = "Main";
			
			String[] tmp = new String[pluginList.size()];
			tmp = pluginList.toArray(tmp);
			java.util.Arrays.sort(tmp);
			for(int i=0;i<tmp.length;i++) {
				plugins[i+4] = tmp[i];
				
			}
			//java.util.Arrays.sort(plugins);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mPluginAdapter = new PluginListAdapter(this.getContext(),0,plugins);
		
		//mOptionsList = new ListView(this.getContext());
		mOptionsList =(ListView) this.findViewById(R.id.optionslist);
		//RelativeLayout.LayoutParams olparams = new RelativeLayout.LayoutParams(200,200);
		//olparams.addRule(RelativeLayout.BELOW,R.id.optionsbutton);
		//olparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		//optionsList.setMinimumHeight(60);
		//mOptionsList.setLayoutParams(olparams);
		
		mOptionsList.setAdapter(mPluginAdapter);
		
		mOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
					if(pos == 0 || pos == 1) {
						return;
					}
					
					String plugin = plugins[pos];
					
					if(plugin.equals("Help")) {
						
					} else if(plugin.equals("Disable All")) {
						
					} else if(plugin.equals("Main")) {
						currentPlugin = "main";
						TriggerSelectionDialog.this.buildList();
						mOptionsButton.performClick();
						return;
					} else {
						currentPlugin = plugin;
						
						TriggerSelectionDialog.this.buildList();
						
						mOptionsButton.performClick();
					}
					
					
			}
		});
		
		mOptionsList.setVerticalFadingEdgeEnabled(false);
		
		mOptionsList.setVisibility(View.INVISIBLE);
		
		//mOptionsList.setDividerHeight(0);
		//mOptionsList.setDivider(null);
		//RelativeLayout root = (RelativeLayout) this.findViewById(R.id.root);
		//root.addView(mOptionsList);
		
		mOptionsButton = (Button)this.findViewById(R.id.optionsbutton);
		
		mOptionsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mOptionsListToggle) {
					mOptionsListToggle = false;
					Animation outAnimation = new TranslateAnimation(0,0,0,-mOptionsList.getHeight());
					outAnimation.setDuration(300);
					outAnimation.setAnimationListener(new AnimationListener() {

						public void onAnimationEnd(Animation animation) {
							mOptionsList.setVisibility(View.INVISIBLE);
						}

						public void onAnimationRepeat(Animation animation) {
							// TODO Auto-generated method stub
							
						}

						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							
						}
						
					});
					mOptionsList.startAnimation(outAnimation);
					
				} else {
					mOptionsListToggle = true;
					mOptionsList.setVisibility(View.VISIBLE);
					mOptionsList.invalidate();
					Animation inAnimation = new TranslateAnimation(0,0,-mOptionsList.getHeight(),0);
					inAnimation.setDuration(300);
					mOptionsList.startAnimation(inAnimation);
				}
			}
		});
		//optionsButton.setOnClickListener()
		
		TextView titlebar = (TextView) this.findViewById(R.id.titlebar);
		
		ViewParent parent = titlebar.getParent();
		
		parent.bringChildToFront(titlebar);
		parent.bringChildToFront(mOptionsButton);
		
		makeToolbar();
		
	}
	
	private void makeToolbar() {
		LayoutInflater li = (LayoutInflater)TriggerSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		theToolbar = (LinearLayout) li.inflate(R.layout.editor_selection_list_row_toolbar, null);
		RelativeLayout.LayoutParams toolbarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		theToolbar.setLayoutParams(toolbarParams);
		
		ImageButton toggle = new ImageButton(TriggerSelectionDialog.this.getContext());
		ImageButton modify = new ImageButton(TriggerSelectionDialog.this.getContext());
		ImageButton delete = new ImageButton(TriggerSelectionDialog.this.getContext());
		
		LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		params.setMargins(0, 0, 0, 0);
		
		
		toggle.setLayoutParams(params);
		modify.setLayoutParams(params);
		delete.setLayoutParams(params);
		
		toggle.setPadding(0, 0, 0, 0);
		modify.setPadding(0, 0, 0, 0);
		delete.setPadding(0, 0, 0, 0);
		//AliasEntry a = entries.get(pos);
		//if(a.enabled) {
			toggle.setImageResource(R.drawable.toolbar_toggleon_button);
		//} else {
		//	toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
		//}
		modify.setImageResource(R.drawable.toolbar_modify_button);
		delete.setImageResource(R.drawable.toolbar_delete_button);
		
		toggle.setBackgroundColor(0x0000000000);
		modify.setBackgroundColor(0);
		delete.setBackgroundColor(0);
		
		toggle.setOnKeyListener(theButtonKeyListener);
		modify.setOnKeyListener(theButtonKeyListener);
		delete.setOnKeyListener(theButtonKeyListener);
		
		toggle.setOnClickListener(new ToggleButtonListener());
		modify.setOnClickListener(new ModifyButtonListener());
		delete.setOnClickListener(new DeleteButtonListener());
		
		theToolbar.addView(toggle);
		theToolbar.addView(modify);
		theToolbar.addView(delete);
		
		
		ImageButton close = (ImageButton)theToolbar.findViewById(R.id.toolbar_tab_close);
		close.setOnKeyListener(theButtonKeyListener);
		
		toolbarLength = close.getDrawable().getIntrinsicWidth() + (toggle.getDrawable().getIntrinsicWidth() * 3); 
		
		TranslateAnimation animation2 = new TranslateAnimation(toolbarLength,0,0,0);
		animation2.setDuration(300);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(animation2);
		animateInController = new LayoutAnimationController(set);
		
		animateOut = new TranslateAnimation(0,toolbarLength,0,0);
		animateOut.setDuration(300);

		animateOutNoTransition = new TranslateAnimation(0,toolbarLength,0,0);
		animateOutNoTransition.setDuration(300);
		animateOutNoTransition.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout rl = (RelativeLayout)theToolbar.getParent();
				rl.removeAllViews();
				lastSelectedIndex = targetIndex;
				lastSelectedIndex = -1;
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
		});
	}
	
	public class ListFocusFixerListener implements View.OnFocusChangeListener {
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus) {
				for(int i=0;i<adapter.getCount();i++) {
					View view = list.getChildAt(i);
					if(view != null)  {
						view.findViewById(R.id.toolbar_tab).setFocusable(false);
					}
				}
				if(lastSelectedIndex < 0) {
					
				} else {
					Log.e("LIST","SETTING FOCUS ON:" + lastSelectedIndex);
					int index = lastSelectedIndex;
					int first = list.getFirstVisiblePosition();
					int last = list.getLastVisiblePosition();
					if(first <= index && index <= last) {
						index = index - first;
					} else {
						index = list.getFirstVisiblePosition();
					}
					list.setSelection(lastSelectedIndex);
					list.getChildAt(index).findViewById(R.id.toolbar_tab).setFocusable(true);
					list.getChildAt(index).findViewById(R.id.toolbar_tab).requestFocus();
				}
				
			}
			Log.e("FOCUS","FOCUS CHANGE LISTENER FIRE, focus is " + hasFocus);
		}
	}
	
	public void onStart() {
		/*if(noTriggers) {
			Toast t = Toast.makeText(TriggerSelectionDialog.this.getContext(), "No triggers loaded. Click below to create new Triggers.", Toast.LENGTH_LONG);
			t.show();
		}*/
	}
	
	public class DeleteButtonListener implements View.OnClickListener {

		//private int entry = -1;
		//ViewFlipper flip = null;
		//private int animateDistance = 0;
		public DeleteButtonListener() {
			//entry = element;
			//this.flip = flip;
			//this.animateDistance = animateDistance;
		}
		
		public void onClick(View v) {
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(TriggerSelectionDialog.this.getContext());
			builder.setTitle("Delete Trigger");
			builder.setMessage("Confirm Delete?");
			builder.setPositiveButton("Delete", new ReallyDeleteTriggerListener());
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			AlertDialog d = builder.create();
			d.show();
			
			
			
			//arg0.dismiss();
		}
		
	}
	
	public class ReallyDeleteTriggerListener implements DialogInterface.OnClickListener {
		//ViewFlipper flip = null;
		//int animateDistance = 0;
		//int entry = -1;
		public ReallyDeleteTriggerListener() {
			//this.flip = flip;
			//this.animateDistance = animateDistance;
			//this.entry = entry;
		}
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			Animation a = new TranslateAnimation(0, toolbarLength, 0, 0);
			a.setDuration(300);
			a.setAnimationListener(new DeleteAnimationListener());
			//list.setOnFocusChangeListener(null);
			//list.setFocusable(false);
			//flip.setOutAnimation(a);
			//flip.showNext();
			theToolbar.startAnimation(a);
		}
		
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		//int entry = -1;
		public DeleteAnimationListener() {
			//this.entry = entry;
		}
		
		public void onAnimationEnd(Animation animation) {
			list.setOnFocusChangeListener(null);
			list.setFocusable(false);
			try {
				if(currentPlugin.equals("main")) {
					service.deleteTrigger(entries.get(lastSelectedIndex).name);
				} else {
					service.deletePluginTrigger(currentPlugin,entries.get(lastSelectedIndex).name);
				}
				
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
			adapter.remove(adapter.getItem(lastSelectedIndex));
			adapter.notifyDataSetInvalidated();
			lastSelectedIndex = -1;
			//triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(104), 10);
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class ModifyButtonListener implements View.OnClickListener {
		//private int index = -1;
		public ModifyButtonListener() {
			//this.index = entry;
		}
		public void onClick(View v) {
			int index = lastSelectedIndex;
			TriggerItem entry = adapter.getItem(index);
			//launch the trigger editor with this item.
			try {
				TriggerData data = null;
				if(currentPlugin.equals("main")) {
					data = service.getTrigger(entry.name);
				} else {
					data = service.getPluginTrigger(currentPlugin,entry.name);
				}
				TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),data,service,triggerEditorDoneHandler,currentPlugin,true);
				editor.show();
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class ToggleButtonListener implements View.OnClickListener {

		//private int index = -1;
		//ImageView icon = null;
		//String key = null;
	
		public ToggleButtonListener() {
			//this.index = index;
			////this.icon = icon;
			//this.key = key;
		}
		
		public void onClick(View v) {
			int index = lastSelectedIndex;
			TriggerItem entry = adapter.getItem(index);
			String key = entry.name;
			//View top = list.getChildAt(index);
			//ViewFlipper flip = top.findViewById(R.id.flipper);
			ImageButton b = (ImageButton)v;
			if(entry.enabled) {
				b.setImageResource(R.drawable.toolbar_toggleoff_button);
				try {
					if(currentPlugin.equals("main")) {
						service.setTriggerEnabled( false,key);
					} else {
						service.setPluginTriggerEnabled( currentPlugin,false,key);
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = false;
				RelativeLayout root = (RelativeLayout) v.getParent().getParent().getParent();
				
				((ImageView)root.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_disabled);
			} else {
				b.setImageResource(R.drawable.toolbar_toggleon_button);
				try {
					if(currentPlugin.equals("main")) {
						service.setTriggerEnabled( true,key);
					} else {
						service.setPluginTriggerEnabled( currentPlugin,true,key);
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				entry.enabled = true;
				RelativeLayout root = (RelativeLayout) v.getParent().getParent().getParent();
				
				((ImageView)root.findViewById(R.id.icon)).setImageResource(R.drawable.toolbar_mini_enabled);
			}
			
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void buildList() {
		//list.setOnItemSelectedListener(null);
		if(adapter != null) {
			adapter.clear();
		}
		entries.clear();
		//adapter.notifyDataSetInvalidated();
		HashMap<String, TriggerData> trigger_list = null;
		try {
			if(currentPlugin.equals("main")) {
				trigger_list = (HashMap<String, TriggerData>) service.getTriggerData();
			} else {
				trigger_list = (HashMap<String, TriggerData>) service.getPluginTriggerData(currentPlugin);
			}
			
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		for(TriggerData data : trigger_list.values()) {
			if(!data.isHidden()) {
				TriggerItem t = new TriggerItem();
				t.name = data.getName();
				t.extra = data.getPattern();
				t.enabled = data.isEnabled();
				
				entries.add(t);
			}
		}
		//adapter.clear();
		//adapter = null;
		
		//list.removeAllViews();
		if(adapter == null) {
			
			adapter = new TriggerListAdapter(list.getContext(),0,entries);
			list.setAdapter(adapter);
		}
		
		
		//list.setOnFocusChangeListener(new ListFocusFixerListener());
		adapter.sort(new ItemSorter());
		adapter.notifyDataSetInvalidated();
	}
	
	private Handler triggerEditorDoneHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 102:
				scrollToSelection(msg.arg1);
				break;
			case 100:
				//refresh the list because it's done.
				//TriggerItem e = adapter.getItem(lastSelectedIndex);
				TriggerData d = (TriggerData)msg.obj;
				TriggerItem tmp = new TriggerItem();
				tmp.name = d.getName();
				tmp.extra = d.getPattern();
				tmp.enabled = true; //TODO: set this to the actual setting when implemented.
				
				list.setFocusable(false);
				list.setOnFocusChangeListener(null);
				buildList();
				list.setOnFocusChangeListener(new ListFocusFixerListener());
				list.setFocusable(true);
				//re-select the index.
				
				int index = adapter.getPosition(tmp);
				this.sendMessageDelayed(this.obtainMessage(102, index, 0),1);
				//scrollToSelection(index);
				//find the index of the new child.
				//View v = list.getChildAt(lastSelectedIndex);
				/*list.invalidate();
				
				int childcount = list.getChildCount();
				View tmpView = list.getChildAt(index);
				list.setSelection(index);
				ViewFlipper f = (ViewFlipper)tmpView.findViewById(R.id.flipper);
				
				f.setInAnimation(new TranslateAnimation(0,0,0,0));
				
				f.showNext();
				list.getChildAt(index).findViewById(R.id.toolbar_tab_close).requestFocus();*/
				break;
			case 101:
				//refresh the list because it's done.
				//TriggerItem e = adapter.getItem(lastSelectedIndex);
				list.setFocusable(false);
				list.setOnFocusChangeListener(null);
				buildList();
				list.setOnFocusChangeListener(new ListFocusFixerListener());
				list.setFocusable(true);
				
				
				break;
			}
			
		}
	};

	private class LineClickedListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			int pos = v.getId() / 157;
			Log.e("CLICK","this is the clicker, clicked view:"+ pos);
			
			if(lastSelectedIndex < 0) {
				
				lastSelectedIndex = pos;
				RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
				rl.setLayoutAnimation(animateInController);
				TriggerItem data = adapter.getItem(lastSelectedIndex);
				if(data.enabled) {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
				} else {
					((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
				}
				rl.addView(theToolbar);
			} else if(lastSelectedIndex != pos) {
				Log.e("SLDF","AM I EVEN HERE");
				AnimatedRelativeLayout holder = (AnimatedRelativeLayout)theToolbar.getParent();
				if(holder != null) {
					if(list.getFirstVisiblePosition() <= lastSelectedIndex && list.getLastVisiblePosition() >= lastSelectedIndex) {
					
						holder.setAnimationListener(mCustomAnimationListener);
						holder.startAnimation(animateOut);
						targetIndex = pos;
						targetHolder = (RelativeLayout) v.findViewById(R.id.toolbarholder);
						
					} else {
						holder.removeAllViews();
						RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.toolbarholder);
						rl.setLayoutAnimation(animateInController);
						TriggerItem data = adapter.getItem(pos);
						if(data.enabled) {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
						} else {
							((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
						}
						rl.addView(theToolbar);
					}
				}
				//theToolbar.startAnimation(animateOut);
			} else {
				
				//lastSelectedIndex = -1;
				if(theToolbar.getParent() == null) {
					lastSelectedIndex = pos;
					RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
					holder.setLayoutAnimation(animateInController);
					TriggerItem data = adapter.getItem(pos);
					if(data.enabled) {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleon_button);
					} else {
						((ImageButton)theToolbar.getChildAt(1)).setImageResource(R.drawable.toolbar_toggleoff_button);
					}
					holder.addView(theToolbar);
				} else {
					targetIndex = pos;
					theToolbar.startAnimation(animateOutNoTransition);
					
				}
			}
		}
		
	}
	private LineClickedListener mLineClicker = new LineClickedListener();
	
	
	public class TriggerListAdapter extends ArrayAdapter<TriggerItem> {

		List<TriggerItem> entries;
		
		public TriggerListAdapter(Context context,
				int textViewResourceId, List<TriggerItem> objects) {
			super(context, textViewResourceId, objects);
			entries = objects;
		}
		
		public View getView(int pos, View convertView,ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.editor_selection_list_row,null);
				
				RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
				root.setOnClickListener(mLineClicker);
			}
			
			v.setId(157*pos);
			
			RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
			holder.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			
			
			if(holder.getChildCount() > 0) {
				holder.removeAllViews();
				lastSelectedIndex = -1;
			}
			
			//RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
			//root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			TriggerItem e = entries.get(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.infoTitle);
				TextView extra = (TextView)v.findViewById(R.id.infoExtended);
				
				label.setText(e.name);
				extra.setText(e.extra);
				
			}
			
			//v.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
			
			
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			if(e.enabled) {
				iv.setImageResource(R.drawable.toolbar_mini_enabled);
			} else {
				iv.setImageResource(R.drawable.toolbar_mini_disabled);
			}
			//int totalWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_holder).getWidth();
			//int tabWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_tab).getWidth();
			//ViewFlipper flip = (ViewFlipper) v.findViewById(R.id.flipper);
			//flip.showNext();
			
			//v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper)));
			
			//v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper)));
			
			//make and populate utility buttons buttons.
			
			//ImageButton toggle = new ImageButton(TriggerSelectionDialog.this.getContext());
			//ImageButton modify = new ImageButton(TriggerSelectionDialog.this.getContext());
			//ImageButton delete = new ImageButton(TriggerSelectionDialog.this.getContext());
			
			//LayoutParams params = LinearLayout.
			//params.height = LayoutParams.WRAP_CONTENT;
			//params.width = LayoutParams.WRAP_CONTENT;
			//params.
			//LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			//params.setMargins(0, 0, 0, 0);
			//params.
			//params.
			//toggle.setM
			
			//toggle.setLayoutParams(params);
			//modify.setLayoutParams(params);
			//delete.setLayoutParams(params);
			
			//toggle.setPadding(0, 0, 0, 0);
			//modify.setPadding(0, 0, 0, 0);
			//delete.setPadding(0, 0, 0, 0);
			if(e.enabled) {
				//toggle.setImageResource(R.drawable.toolbar_toggleon_button);
			} else {
				//toggle.setImageResource(R.drawable.toolbar_toggleoff_button);
			}
			//modify.setImageResource(R.drawable.toolbar_modify_button);
			//delete.setImageResource(R.drawable.toolbar_delete_button);
			
			//toggle.setIm
			
			//toggle.setBackgroundColor(0x0000000000);
			//modify.setBackgroundColor(0);
			//delete.setBackgroundColor(0);
			
			//toggle.setOnKeyListener(theButtonKeyListener);
			//modify.setOnKeyListener(theButtonKeyListener);
			//delete.setOnKeyListener(theButtonKeyListener);
			
			
			//toggle.setOnClickListener(new ToggleButtonListener(pos));
			//modify.setOnClickListener(new ModifyButtonListener(pos));
			//delete.setOnClickListener(new DeleteButtonListener(pos,v.findViewById(R.id.flipper),width));
			//get the holder.
			//LinearLayout holder = (LinearLayout) v.findViewById(R.id.button_holder);
			//holder.removeAllViews();
			//holder.addView(toggle);
			//holder.addView(modify);
			//holder.addView(delete);
			
			//int width = toggle.getDrawable().getIntrinsicWidth() + delete.getDrawable().getIntrinsicWidth() + modify.getDrawable().getIntrinsicWidth();
			
			//toggle.setOnClickListener(new ToggleButtonListener(pos,iv,e.name));
			//modify.setOnClickListener(new ModifyButtonListener(pos));
			//delete.setOnClickListener(new DeleteButtonListener(pos,(ViewFlipper)v.findViewById(R.id.flipper),width));
			
			//v.findViewById(R.id.toolbar_tab).setOnClickListener(new ToolbarTabOpenListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,pos));
			
			//v.findViewById(R.id.toolbar_tab_close).setOnClickListener(new ToolbarTabCloseListener(v,(ViewFlipper)v.findViewById(R.id.flipper),width,v.findViewById(R.id.toolbar_tab)));
			//v.findViewById(R.id.toolbar_tab_close).setOnKeyListener(theButtonKeyListener);
			
//			v.findViewById(R.id.toolbar_tab).setOnFocusChangeListener(new View.OnFocusChangeListener() {
//				
//				public void onFocusChange(View v, boolean hasFocus) {
//					if(hasFocus) {
//						v.setFocusable(true);
//						v.setFocusableInTouchMode(true);
//					} else {
//						v.setFocusable(false);
//						v.setFocusableInTouchMode(false);
//					}
//				}
//			});
			
			return v;
			
		}
		
	}
	
	public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				int first = 0;
				//int last = list.getLastVisiblePosition();
				if(lastSelectedIndex - 1 >= first) {
					/*list.setSelection(lastSelectedIndex -1);
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex -1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex - 1);
					return true;
				} else {
					return false;
				}
				//break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				int last = list.getCount() -1;
				if(lastSelectedIndex + 1 <= last) {
					/*list.setSelection(lastSelectedIndex +1);
					int childCount = list.getChildCount();
					//list.getAdapter().get
					RelativeLayout row = (RelativeLayout) list.getChildAt(lastSelectedIndex +1);
					row.performClick();*/
					list.setSelection(lastSelectedIndex + 1);
					return true;
				} else {
					return false;
				}
				//break;
			}
			return false;
		}
		
	}
	
	public class ToolbarTabOpenListener implements View.OnClickListener {
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		private int index;
		
		public ToolbarTabOpenListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,int index) {
			this.parent = parent;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
			this.index = index;
		}
		
		public void onClick(View v) {
			//v.requestFocus();
			lastSelectedIndex = this.index;
			
			//int targetWidth = 100;
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			
			targetFlipper.setInAnimation(ai);
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			
			targetFlipper.setOutAnimation(ao);
			
			targetFlipper.showNext();
			
			parent.findViewById(R.id.toolbar_tab_close).requestFocus();
		}
		
	}
	
	public class ToolbarTabCloseListener implements View.OnClickListener {
		View viewToFocus = null;
		View parent = null;
		ViewFlipper targetFlipper = null;
		int toolbarLength = 0;
		public ToolbarTabCloseListener(View parent, ViewFlipper targetFlipper, int toolBarWidth,View viewToFocus) {
			this.parent = parent;
			this.viewToFocus = viewToFocus;
			this.targetFlipper = targetFlipper;
			toolbarLength = toolBarWidth;
		}
		
		public void onClick(View v) {
			//int totalWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_holder).getWidth();
			//int tabWidth = TriggerSelectionDialog.this.findViewById(R.id.toolbar_tab).getWidth();
			
			//int targetWidth = TriggerSelectionDialog.this.findViewById(R.id.button_holder).getWidth();
			
			Animation ao = new TranslateAnimation(0, toolbarLength, 0, 0);
			ao.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setOutAnimation(ao);
			
			Animation ai = new TranslateAnimation(toolbarLength, 0, 0, 0);
			ai.setDuration(800);
			//a.setFillBefore(true);
			//a.setFillAfter(false);
			targetFlipper.setInAnimation(ai);
			targetFlipper.showNext();
			
			//parent.findViewById(R.id.toolbar_tab).requestFocus();
			viewToFocus.setFocusable(true);
			viewToFocus.requestFocus();
		}
		
	}
	
	
	public class ItemSorter implements Comparator<TriggerItem>{

		public int compare(TriggerItem a, TriggerItem b) {
			return a.name.compareToIgnoreCase(b.name);
		}
		
	}
	
	public class TriggerItem {
		public boolean enabled;
		String name;
		String extra;
		
		public TriggerItem() {
			name = "";
			extra = "";
			enabled = true;
		}
		
		public boolean equals(Object o) {
			if(o == this) return true;
			
			if(!(o instanceof TriggerItem)) return false;
			TriggerItem tmp = (TriggerItem)o;
			if(!this.name.equals(tmp.name)) return false;
			if(!this.extra.equals(tmp.extra)) return false;
			if(this.enabled != tmp.enabled) return false;
			return true;
		}
	}
	
	public static final int MESSAGE_NEW_TRIGGER = 100;
	public static final int MESSAGE_MOD_TRIGGER = 101;
	public static final int MESSAGE_DELETE_TRIGGER = 102;
	
	public Handler triggerModifier = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case 104:
				finishDelete();
				break;
			case 103:
				//finishScroll(msg.arg1);
				break;
			case MESSAGE_NEW_TRIGGER:
				TriggerData tmp = (TriggerData)msg.obj;
				//attempt to modify service
				try {
					service.newTrigger(tmp);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			case MESSAGE_MOD_TRIGGER:
				TriggerData from = msg.getData().getParcelable("old");
				TriggerData to = msg.getData().getParcelable("new");
				
				try {
					service.updateTrigger(from, to);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				break;
			case MESSAGE_DELETE_TRIGGER:
				String which = (String)msg.obj;
				try {
					service.deleteTrigger(which);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				break;
			}
		}
	};
	
	protected void finishDelete() {
		buildList();
		list.setFocusable(true);
		list.setOnFocusChangeListener(new ListFocusFixerListener());
	}

	protected void scrollToSelection(int index) {
		//list.invalidate();
		
		int childcount = list.getChildCount();
		list.setSelection(index);
		//list.poin
		triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(103,index,0),10);
		
	}
	
	protected void finishScroll(int index) {
		int first = list.getFirstVisiblePosition();
		int last = list.getLastVisiblePosition();
		if(first <= index && index <= last) {
			index = index - first;
		}
		
		
		View tmpView = list.getChildAt(index);
		if(tmpView != null) {
		
			ViewFlipper f = (ViewFlipper)tmpView.findViewById(R.id.flipper);
			LinearLayout holder = (LinearLayout)tmpView.findViewById(R.id.button_holder);
			int width = 0;
			for(int i = 0;i<holder.getChildCount();i++) {
				ImageButton b = (ImageButton)holder.getChildAt(i);
				width += b.getDrawable().getIntrinsicWidth();
			}
			Animation a = new TranslateAnimation(width,0,0,0);
			a.setDuration(800);
			f.setInAnimation(a);
			
			f.showNext();
			list.getChildAt(index).findViewById(R.id.toolbar_tab_close).requestFocus();
		}
	}
	
	//set up the "options" list view
	String[] plugins = new String[]{"foo","bar","baz","zip","zop","woobity","flip","flop"};
	PluginListAdapter mPluginAdapter = null;
	class PluginListAdapter extends ArrayAdapter<String> {

		public PluginListAdapter(Context context,
				int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int pos) {
			if(pos == 2) {
				return 1;
			} else {
				return 0;
			}
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}
		
		@Override
		public boolean isEnabled(int pos) {
			if(pos == 2) {
				return false;
			} else {
				return true;
			}
		}
		
		@Override
		public View getView(int pos,View convertView,ViewGroup parent) {
			
			if(pos == 2) {
				//need to do the special text view.
				View tmp = convertView;
				if(tmp == null) {
					LayoutInflater li = (LayoutInflater) TriggerSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
					tmp = li.inflate(R.layout.editor_selection_filter_divider_row, null);
					((TextView)tmp).setText("Filter by plugin");
					//tmp = new TextView(TriggerSelectionDialog.this.getContext());
					//AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);
					//tmp.setLayoutParams(params);
					//((TextView)tmp).setTextSize(13);
					return tmp;
				} else {
					return tmp;
				}
			}
			
			TextView retView = null;
			if(convertView == null) {
				LayoutInflater li = (LayoutInflater) TriggerSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				retView = (TextView) li.inflate(R.layout.editor_selection_filter_list_row, null);
				
				//retView = new TextView(TriggerSelectionDialog.this.getContext());
				//AbsListView.LayoutParams params = new AbsListView.LayoutParams(200,60);
				//retView.setLayoutParams(params);
				//retView = v;
				

			} else {
				retView = (TextView)convertView;
			}
			//retView.setTextSize(26);
			//retView.setBackgroundColor(0xFF444444);
			//retView.setTextColor(0xFFAAAAAA);
			//Log.e("TRIG","LOADING: "+this.getItem(pos));
			retView.setText(this.getItem(pos));
			
			if(pos == 1 || pos ==0) {
				retView.setGravity(Gravity.CENTER);
			} else {
				retView.setGravity(Gravity.LEFT);
			}
			return retView;
		}
		
	
		
	}
	
}

