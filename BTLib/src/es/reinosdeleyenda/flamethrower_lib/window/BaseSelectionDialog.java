package es.reinosdeleyenda.flamethrower_lib.window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewParent;
import android.view.Window;
import android.view.View;
import android.view.WindowManager;

import es.reinosdeleyenda.flamethrower_lib.R;

public class BaseSelectionDialog extends Dialog {

	private String mTitle;
	private ListView mList;
	private ListView mOptionsList;
	private boolean mOptionsListToggle;
	private ArrayAdapter<ItemEntry> mAdapter;
	protected Button mOptionsButton;
	
	private int mLastSelectedIndex = -1;
	private int mTargetIndex;
	private RelativeLayout targetHolder;
	private int toolbarLength = 0;
	private LayoutAnimationController animateInController;
	private TranslateAnimation animateOut;
	private TranslateAnimation animateOutNoTransition;
	private TranslateAnimation animateIn;
	//private ArrayList<Drawable> miniIcons;
	private Context mContext;
	
	
	
	private LinearLayout mToolbar;
	private TextView mTitlebar;
	private CharSequence mNewTitle = "New";
	
	public BaseSelectionDialog(Context context) {
		super(context,android.R.style.Theme_Light);
		// TODO Auto-generated constructor stub
		mContext = context;
		mToolbarButtons = new ArrayList<UtilityButton>();
		
		UtilityButton delete = new UtilityButton();
		delete.action = UTILITY_BUTTON_ACTION.DELETE;
		delete.imageResource = R.drawable.toolbar_delete_button;
		
		UtilityButton toggle = new UtilityButton();
		toggle.action = UTILITY_BUTTON_ACTION.TOGGLE;
		toggle.imageResource = R.drawable.toolbar_toggleon_button;
		
		UtilityButton edit = new UtilityButton();
		edit.action = UTILITY_BUTTON_ACTION.NORMAL;
		edit.imageResource = R.drawable.toolbar_modify_button;
		
		mToolbarButtons.add(toggle);
		mToolbarButtons.add(edit);
		mToolbarButtons.add(delete);
		
		
		
		//mToolbarButtons.
		
		//miniIcons = new ArrayList<Drawable>();
		
		//miniIcons.add(context.getResources().getDrawable(R.drawable.toolbar_mini_disabled));
		//miniIcons.add(context.getResources().getDrawable(R.drawable.toolbar_mini_enabled));
		
		//mListItems = new ArrayList<ItemEntry>();
		/*ItemEntry first = new ItemEntry();
		ItemEntry second = new ItemEntry();
		ItemEntry third = new ItemEntry();
		
		first.title = "first item";
		first.extra = "sskdfjasdffsdf";
		
		second.title = "second item";
		second.extra = "sfsafdsafd";
		
		third.title = "third item";
		third.extra = "sdfasdf";
		
		mListItems.add(first);
		mListItems.add(second);
		mListItems.add(third);
		
		OptionItem help = new OptionItem();
		help.title = "Help";
		help.centered = true;
		
		OptionItem enableAll = new OptionItem();
		enableAll.title = "Enable All";
		enableAll.centered = true;
		
		DividerItem divider = new DividerItem();
		divider.title = "Filter By Plugin";
		
		OptionItem foo = new OptionItem();
		foo.title = "foo";
		OptionItem bar = new OptionItem();
		bar.title = "bar";
		OptionItem baz = new OptionItem();
		baz.title = "baz";
		
		optionItems.add(help);
		optionItems.add(enableAll);
		optionItems.add(divider);
		optionItems.add(foo);
		optionItems.add(bar);
		optionItems.add(baz);*/
	}
	
	@Override
	public void onCreate(Bundle b) {
		
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setBackgroundDrawableResource(R.drawable.dialog_window_crawler1);
		MainWindow w = (MainWindow)mContext;
		if(w.isStatusBarHidden()) {
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		this.setCanceledOnTouchOutside(false);
		setContentView(R.layout.editor_selection_dialog);
		this.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);
		
		TextView tst = (TextView) this.findViewById(R.id.titlebar);
		tst.setText(mTitle);
		
		//initialize the list view
		mList = (ListView)findViewById(R.id.list);
		
		mList.setScrollbarFadingEnabled(false);
		
		//mList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		mList.setOnItemSelectedListener(new DpadSelectionListener());
		//mList.setOnFocusChangeListener(new ListFocusFixerListener());
		//mList.setSelector(R.drawable.filter_selection_selector);
		mList.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					if(mToolbar.getParent() != null) {
						removeToolbar();
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
	
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//we just want to have one
				
				if(mToolbar.getParent() != null) {
					removeToolbar();
					return;
				}
				//arg1.performClick();
				int duration = 500;
				
				
				//determine if target is all the way visible;
				if(arg1.getBottom() > mList.getHeight() || arg1.getTop() < 0) {
					mList.smoothScrollToPosition(arg2,100);
					mList.postDelayed(new ScrollSelectionRunner(arg2), 100);
					Log.e("CLICK","bringing view into full visibility" + arg2);
					return;
				}
				
				mLastSelectedIndex = arg2;
				RelativeLayout target = (RelativeLayout)mList.getParent().getParent();
				FrameLayout frame = (FrameLayout)mList.getParent();
				//oops, re make the toolbar insertion code.
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				
				int v_top = mList.getChildAt(arg2 - mList.getFirstVisiblePosition()).getTop();
				int f_top = frame.getTop();
				
				Log.e("WINDOW","MARGIN TOP: " + v_top +" + " + f_top + " " + arg2);
				params.setMargins(0, v_top + f_top, 0, 0);
				
				mToolbar.setLayoutParams(params);
				target.addView(mToolbar);
				mToolbarListener.willShowToolbar(mToolbar, mLastSelectedIndex);
				mToolbar.startAnimation(animateIn);
				mToolbar.getChildAt(1).requestFocus();
				//show the toolbar.
				
				
				
				//insert the toolbar into the hierarchy.
				
				
				Log.e("CLICK","CLICK CLICK CLICK CLICK");
			}
		});
	
		//list.setOnFocusChangeListener(new ListFocusFixerListener());
		
		mList.setSelector(R.drawable.blue_frame_nomargin_nobackground);
		
		
		
		mList.setEmptyView(findViewById(R.id.empty));
		
		buildRawList();
		
		Button newbutton = (Button)findViewById(R.id.add);
		newbutton.setText(mNewTitle );
		
		newbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				if(mToolbarListener != null) {
					mToolbarListener.onNewPressed(arg0);
				}
				//TriggerEditorDialog editor = new TriggerEditorDialog(TriggerSelectionDialog.this.getContext(),null,service,triggerEditorDoneHandler,currentPlugin);
				//editor.show();
			}
		});
		
		Button cancelbutton = (Button)findViewById(R.id.done);
		
		cancelbutton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				if(mToolbarListener != null) {
					mToolbarListener.onDonePressed(arg0);
				}
				
				BaseSelectionDialog.this.dismiss();
			}
			
		});
	
		//gett he plugin list.
		//try {
			//List<String> pluginList = (List<String>)service.getPluginsWithTriggers();
			String[] pluginList = new String[] { "foo","bar","baz" };
			
			
			plugins = new String[pluginList.length+3];
			plugins[0] = "Help";
			//plugins[1] = "Disable All";
			plugins[1] = "divider";
			plugins[2] = "Main";
			
			//String[] tmp = new String[pluginList.size()];
			//tmp = pluginList.toArray(tmp);
			//String 
			java.util.Arrays.sort(pluginList);
			for(int i=0;i<pluginList.length;i++) {
				plugins[i+3] = pluginList[i];
				
			}
			//java.util.Arrays.sort(plugins);
			
		//} catch (RemoteException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		
		mOptionAdapter = new OptionListAdapter(this.getContext(),0,optionItems);
		
		mOptionsList =(ListView) this.findViewById(R.id.optionslist);
		
		mOptionsList.setAdapter(mOptionAdapter);
		
		mOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
					/*if(pos == 0 || pos == 1) {
						return;
					}
					
					String plugin = plugins[pos];
					
					if(plugin.equals("Help")) {
						
					} else if(plugin.equals("Disable All")) {
						
					} else if(plugin.equals("Main")) {
						//currentPlugin = "main";
						BaseSelectionDialog.this.buildList();
						mOptionsButton.performClick();
						return;
					} else {
						//currentPlugin = plugin;
						
						BaseSelectionDialog.this.buildList();
						
						mOptionsButton.performClick();
					}*/
					if(mOptionItemClickListener != null) {
						mOptionItemClickListener.onOptionItemClicked(pos);
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
				if(mPromoteHelp) {
					//launch the help menu and return.
					if(mOptionItemClickListener != null) {
						//actually, click help.
						mOptionItemClickListener.onOptionItemClicked(0);
					}	
					return;
				}
				
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
					mOptionsList.bringToFront();
					TextView tst = (TextView) BaseSelectionDialog.this.findViewById(R.id.titlebar);
					tst.bringToFront();
					mOptionsButton.bringToFront();
					mOptionsList.invalidate();
					Animation inAnimation = new TranslateAnimation(0,0,-mOptionsList.getHeight(),0);
					inAnimation.setDuration(300);
					mOptionsList.startAnimation(inAnimation);
				}
			}
		});
		
		if(mPromoteHelp) {
			mOptionsButton.setText("?");
		}
		//mOptionsButton.invalidate();
		//optionsButton.setOnClickListener()
		
		mTitlebar = (TextView) this.findViewById(R.id.titlebar);
		
		ViewParent parent = mTitlebar.getParent();
		
		parent.bringChildToFront(mTitlebar);
		parent.bringChildToFront(mOptionsButton);
		
		makeToolbar();
		
	}
	
	private class ScrollSelectionRunner implements Runnable {
		int target;
		public ScrollSelectionRunner(int target) {
			this.target = target;
		}
		@Override
		public void run() {
			//mList.setSelection(target);
			//mList.performClick();
			Log.e("WINDOW","Clicking on list item: " + target);
			mList.performItemClick(mList.getAdapter().getView(target, null, null), target, target);
		}
		
	}
	
	private class DpadSelectionListener implements AdapterView.OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(arg1.getTop() < 0 || arg1.getBottom() > mList.getHeight()) {
				mList.smoothScrollToPosition(arg2,100);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

	
	private void removeToolbar() {
		if(mToolbar.getParent() != null) {
			mToolbar.startAnimation(animateOut);
		}
		
	}
	
	public void setNewButtonLabel(String str) {
		mNewTitle = str;
	}
	
	@Override
	public void setTitle(CharSequence title) {
		//((TextView) this.findViewById(R.id.titlebar)).setText(title);
		mTitle = (String)title;
		//this.setTitle(title);
		super.setTitle(title);
	}
	
	protected ArrayList<ItemEntry> mListItems = new ArrayList<ItemEntry>();
	
	public void addListItem(String name,String extra,int mini_icon,boolean enabled) {
		ItemEntry newEntry = new ItemEntry();
		newEntry.title = name;
		newEntry.extra = extra;
		newEntry.enabled = enabled;
		newEntry.mini_icon = mini_icon;
		//newEntry.mini_icon_on = icon_on; 
		//newEntry.mini_icon_off = icon_off;
		mListItems.add(newEntry);
	}
	
	public void clearListItems() {
		this.mListItems.clear();
	}
	
	public void clearOptionItems() {
		this.optionItems.clear();
	}
	
	@Override
	public void onBackPressed() {
		if(mToolbarListener!=null) {
			mToolbarListener.onDonePressed(null);
		}
		this.dismiss();
	}
	
	public void invalidateList() {
		if(mAdapter == null) return;
		removeToolbar();
		this.mAdapter.notifyDataSetChanged();
		this.mAdapter.notifyDataSetInvalidated();
	}
	//@SuppressWarnings("unchecked")
	private void buildRawList() {
		if(mAdapter == null) {
			mAdapter = new ItemAdapter(BaseSelectionDialog.this.getContext(), R.layout.editor_selection_list_row, mListItems);
			mList.setAdapter(mAdapter);
		}
		//list.setOnFocusChangeListener(new ListFocusFixerListener());
		//mAdapter.sort(new ItemSorter());
		mAdapter.notifyDataSetInvalidated();
	}
	

	private class ItemAdapter extends ArrayAdapter<ItemEntry> {

		public ItemAdapter(Context context, int textViewResourceId,
				List<ItemEntry> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public View getView(int pos,View convertView,ViewGroup parent) {
			View v = convertView;
			if(convertView == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.editor_selection_list_row,null);
				
				RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
				//root.setOnClickListener(mLineClicker);
			}
			
			RelativeLayout holder = (RelativeLayout)v.findViewById(R.id.toolbarholder);
			holder.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			
			
			if(holder.getChildCount() > 0) {
				holder.removeAllViews();
				mLastSelectedIndex = -1;
			}
			
			v.setId(pos*157);
			
			//RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
			//root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
			ItemEntry e = this.getItem(pos);
			
			if(e != null) {
				TextView label = (TextView)v.findViewById(R.id.infoTitle);
				TextView extra = (TextView)v.findViewById(R.id.infoExtended);
				
				label.setText(e.title);
				extra.setText(e.extra);
				
				label.setBackgroundColor(0x00000000);
				extra.setBackgroundColor(0x00000000);
				
			}
			
			//v.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
			
			
			ImageView iv = (ImageView) v.findViewById(R.id.icon);
			/*if(e.enabled) {
				if(e.mini_icon_on != 0) {
					iv.setImageResource(e.mini_icon_on);
				}
			} else {
				if(e.mini_icon_off != 0) {
					iv.setImageResource(e.mini_icon_off);
				}
			}*/
			if(e.mini_icon == 0) {
				iv.setVisibility(View.GONE);
			} else {
				iv.setImageResource(e.mini_icon);
			}
			//iv.setImage
			
			//iv.setIma
			return v;
		}
		
	}
	
	public class ItemEntry {
		public String title;
		public String extra;
		public boolean selected;
		public boolean enabled;
		public int mini_icon;
		//public int mini_icon_on;
		//public int mini_icon_off;
	}
	
	public class ItemSorter implements Comparator<ItemEntry>{

		public int compare(ItemEntry a, ItemEntry b) {
			return a.title.compareToIgnoreCase(b.title);
		}
		
	}
	
	String[] plugins = new String[]{"foo","bar","baz","zip","zop","woobity","flip","flop"};
	OptionListAdapter mOptionAdapter = null;
	class OptionListAdapter extends ArrayAdapter<BaseOptionItem> {

		public OptionListAdapter(Context context,
				int textViewResourceId, ArrayList<BaseOptionItem> objects) {
			super(context, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int pos) {
			BaseOptionItem item = this.getItem(pos);
			if(item instanceof DividerItem) {
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
			BaseOptionItem item = this.getItem(pos);
			
			if(item instanceof DividerItem) {
				return false;
			} else {
				return true;
			}
		}
		
		@Override
		public View getView(int pos,View convertView,ViewGroup parent) {
			
			BaseOptionItem item = this.getItem(pos);
			
			if(item instanceof DividerItem) {
				//need to do the special text view.
				View tmp = convertView;
				if(tmp == null) {
					LayoutInflater li = (LayoutInflater) BaseSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
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
				LayoutInflater li = (LayoutInflater) BaseSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
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
			
			
			retView.setText(item.title);
			
			if(item.centered) {
				retView.setGravity(Gravity.CENTER);
			} else {
				retView.setGravity(Gravity.LEFT);
			}
			return retView;
		}
		
	
		
	}
	
	private void makeToolbar() {
		LayoutInflater li = (LayoutInflater)BaseSelectionDialog.this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mToolbar = (LinearLayout) li.inflate(R.layout.editor_selection_list_row_toolbar, null);
		RelativeLayout.LayoutParams toolbarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		toolbarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mToolbar.setLayoutParams(toolbarParams);
		
		
		//ImageButton toggle = new ImageButton(BaseSelectionDialog.this.getContext());
		//ImageButton modify = new ImageButton(BaseSelectionDialog.this.getContext());
		//ImageButton delete = new ImageButton(BaseSelectionDialog.this.getContext());
		
		LinearLayout.LayoutParams params = (new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		params.setMargins(0, 0, 0, 0);
		
		for(int i=0;i<mToolbarButtons.size();i++) {
			UtilityButton b = mToolbarButtons.get(i);
			ImageButton tmp = new ImageButton(BaseSelectionDialog.this.getContext());
			tmp.setLayoutParams(params);
			tmp.setPadding(0, 0, 0, 0);
			tmp.setImageResource(b.imageResource);
			tmp.setBackgroundColor(0);
			tmp.setOnKeyListener(theButtonKeyListener);
			//hack for the new toolbar paradigm
			tmp.setNextFocusDownId(mList.getId());
			tmp.setNextFocusUpId(mList.getId());
			switch(b.action) {
			case DELETE:
				tmp.setOnClickListener(new DeleteButtonListener());
				break;
			case NORMAL:
				tmp.setOnClickListener(new UtilityButtonListener(i));
				break;
			case TOGGLE:
				tmp.setOnClickListener(new ToggleButtonListener(i));
				break;
			}
			b.view = tmp;
			mToolbar.addView(tmp);
			toolbarLength = toolbarLength + tmp.getDrawable().getIntrinsicWidth();
		}
		
		
		/*toggle.setLayoutParams(params);
		modify.setLayoutParams(params);
		delete.setLayoutParams(params);
		
		toggle.setPadding(0, 0, 0, 0);
		modify.setPadding(0, 0, 0, 0);
		delete.setPadding(0, 0, 0, 0);

		toggle.setImageResource(R.drawable.toolbar_toggleon_button);
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
		
		mToolbar.addView(toggle);
		mToolbar.addView(modify);
		mToolbar.addView(delete);*/
		
		
		ImageButton close = (ImageButton)mToolbar.findViewById(R.id.toolbar_tab_close);
		close.setOnKeyListener(theButtonKeyListener);
		
		close.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				removeToolbar();
			}
		});
		
		toolbarLength = toolbarLength + close.getDrawable().getIntrinsicWidth();// + (((ImageButton)(mToolbar.getChildAt(0))).getDrawable().getIntrinsicWidth() * 3); 
		
		animateIn = new TranslateAnimation(toolbarLength,0,0,0);
		animateIn.setDuration(300);
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(animateIn);
		animateInController = new LayoutAnimationController(set);
		
		animateOut = new TranslateAnimation(0,toolbarLength,0,0);
		animateOut.setDuration(300);
		

		animateOutNoTransition = new TranslateAnimation(0,toolbarLength,0,0);
		animateOutNoTransition.setDuration(300);
		animateOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout parent = (RelativeLayout) mToolbar.getParent();
				parent.removeView(mToolbar);
				mList.requestFocus();
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
		});
	}
	
public ToolBarButtonKeyListener theButtonKeyListener = new ToolBarButtonKeyListener();
	
	public class ToolBarButtonKeyListener implements View.OnKeyListener {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			switch(keyCode) {
			case KeyEvent.KEYCODE_DPAD_UP:
				removeToolbar();
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				removeToolbar();
				break;
			}
			return false;
		}
		
	}
	
	public ArrayList<UtilityButton> mToolbarButtons;
	
	public class ToggleButtonListener implements View.OnClickListener {

		//private int index = -1;
		//ImageView icon = null;
		//String key = null;
		int utilityIndex = -1;
		public ToggleButtonListener(int index) {
			utilityIndex = index;
			//this.index = index;
			////this.icon = icon;
			//this.key = key;
		}
		
		public void onClick(View v) {
			int index = mLastSelectedIndex;
			UtilityButton entry = mToolbarButtons.get(utilityIndex);
			
			entry.toggle = !entry.toggle;
			if(mToolbarListener != null) {
				mToolbarListener.onButtonStateChanged((ImageButton)v, index, entry.id, entry.toggle);
			}
			
		}
		
	}
	
	public class UtilityButtonListener implements View.OnClickListener {

		//private int index = -1;
		//ImageView icon = null;
		//String key = null;
		int utilityIndex = -1;
		public UtilityButtonListener(int index) {
			utilityIndex = index;
			//this.index = index;
			////this.icon = icon;
			//this.key = key;
		}
		
		public void onClick(View v) {
			int index = mLastSelectedIndex;
			UtilityButton entry = mToolbarButtons.get(utilityIndex);
			
			//entry.toggle = !entry.toggle;
			if(mToolbarListener != null) {
				mToolbarListener.onButtonPressed(v, index, entry.id);
			}
			
		}
		
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
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(BaseSelectionDialog.this.getContext());
			builder.setTitle("Delete Item");
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
			BaseSelectionDialog.this.mToolbar.startAnimation(a);
		}
		
	}
	
	public class DeleteAnimationListener implements Animation.AnimationListener {

		//int entry = -1;
		public DeleteAnimationListener() {
			//this.entry = entry;
		}
		
		public void onAnimationEnd(Animation animation) {
			//mList.setOnFocusChangeListener(null);
			//mList.setFocusable(false);
			
			mAdapter.remove(mAdapter.getItem(mLastSelectedIndex));
			mAdapter.notifyDataSetInvalidated();
			if(mToolbarListener != null) {
				mToolbarListener.onItemDeleted(mLastSelectedIndex);
			}
			
			((RelativeLayout)mToolbar.getParent()).removeView(mToolbar);
			
			mLastSelectedIndex = -1;
			

			//triggerModifier.sendMessageDelayed(triggerModifier.obtainMessage(104), 10);
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class UtilityButton {
		UTILITY_BUTTON_ACTION action;
		int id;
		ImageButton view;
		boolean toggle;
		int imageResource;
		public UtilityButton() {
			action = UTILITY_BUTTON_ACTION.NORMAL;
			id = 0;
			view = new ImageButton(BaseSelectionDialog.this.getContext());
			toggle = false;
		}
	}
	
	public enum UTILITY_BUTTON_ACTION {
		NORMAL,
		TOGGLE,
		DELETE
	}
	
	public interface UtilityToolbarListener {
		public void onButtonPressed(View v,int row,int index);
		public void onButtonStateChanged(ImageButton v,int row,int index, boolean state);
		public void onItemDeleted(int row);
		public void onNewPressed(View v);
		public void onDonePressed(View v);	
		public void willShowToolbar(LinearLayout v, int row);
		public void willHideToolbar(LinearLayout v, int row);
	}
	
	public interface OptionItemClickListener {
		public void onOptionItemClicked(int row);
	}
	
	private OptionItemClickListener mOptionItemClickListener;
	
	public void setOptionItemClickListener(OptionItemClickListener listener) {
		mOptionItemClickListener = listener;
	}
	
	private UtilityToolbarListener mToolbarListener;
	
	public void setToolbarListener(UtilityToolbarListener listener) {
		mToolbarListener = listener;
	}
	
	private class BaseOptionItem {
		String title;
		boolean centered;
		
		public BaseOptionItem() {
			title = null;
			centered = false;
		}
	}
	
	private class OptionItem extends BaseOptionItem {
		
	}
	
	private class DividerItem extends BaseOptionItem {
		
	}
	
	ArrayList<BaseOptionItem> optionItems = new ArrayList<BaseOptionItem>();
	private boolean mPromoteHelp = false;
	
	public void hideOptionsMenu() {
		if(mOptionsList.getVisibility() == View.VISIBLE) {
			mOptionsButton.performClick();
		}
	}
	
	public void addOptionItem(String name,boolean centered) {
		OptionItem item = new OptionItem();
		item.title = name;
		item.centered = centered;
		this.optionItems.add(item);
	}
	
	public void addOptionDivider(String name,boolean centered) {
		DividerItem divider = new DividerItem();
		divider.title = name;
		divider.centered = centered;
		this.optionItems.add(divider);
	}
	
	public void setItemMiniIcon(int row,int resource) {
		ItemEntry entry = mAdapter.getItem(row);
		
		entry.mini_icon = resource;
		//if the toolbar is out, fetch up the icon and change it, otherwise invalidate the list
		if(mToolbar.getParent() != null) {
			//we now have to find the view of the last selected item.
			
			
			RelativeLayout root = (RelativeLayout)mList.getChildAt(row - mList.getFirstVisiblePosition());
			((ImageView)root.findViewById(R.id.icon)).setImageResource(entry.mini_icon);
			
		} else {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void rebuildList() {
		mList.setFocusable(false);
		mList.setOnFocusChangeListener(null);
		buildRawList();
		//mList.setOnFocusChangeListener(new ListFocusFixerListener());
		mList.setFocusable(true);
	}
	
	public void scrollToSelection(String str) {
		for(int i=0;i<mAdapter.getCount();i++) {
			ItemEntry foo = mAdapter.getItem(i);
			if(str.equals(foo.title)) {
				mList.setSelection(i);
				return;
			}
		}
	}
	
	
	
	public void addToolbarButton(int drawable,int id) {
		UtilityButton edit = new UtilityButton();
		edit.id = id;
		edit.action = UTILITY_BUTTON_ACTION.NORMAL;
		edit.imageResource = drawable;
		
		mToolbarButtons.add(edit);
	}
	
	public void addToolbarDeleteButton(int drawable,int id) {
		UtilityButton edit = new UtilityButton();
		edit.id = id;
		edit.action = UTILITY_BUTTON_ACTION.DELETE;
		edit.imageResource = drawable;
		
		mToolbarButtons.add(edit);
	}
	
	public void clearToolbarButtons() {
		mToolbarButtons.clear();
	}
	
	protected void promoteHelp() {
		//promotes the help button to the filter selection dialog.
		mPromoteHelp = true;
		if(mOptionsButton != null) {
			mOptionsButton.setText("?");
			mOptionsButton.invalidate();
		}
	}
	
}
