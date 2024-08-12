package es.reinosdeleyenda.flamethrower_lib.window;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class LuaDialog extends Dialog {

	private View mView = null;
	private Context mContext = null;
	private boolean mTitle;
	private Drawable mBorder;;
	
	public LuaDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LuaDialog(Context context,View v,boolean title,Drawable border) {
		super(context,android.R.style.Theme_Black);
		mContext = context;
		mView = v;
		mTitle = title;
		mBorder = border;
		
		
		
		
		//this.setCont
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(!mTitle) {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			//this.getWindow().setFla
		}
		//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		if(mBorder != null) {
			this.getWindow().setBackgroundDrawable(mBorder);
		} else {
			this.getWindow().setBackgroundDrawableResource(es.reinosdeleyenda.flamethrower_lib.R.drawable.dialog_window_crawler1);
		}
		
		//Window w = this.getWindow();
		
		//WindowManager.LayoutParams wparams = w.getAttributes();
		//params
		//wparams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		//wparams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		
		
		//w.setAttributes(wparams);
		//mView = v;	
		
		//ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		//mView.setLayoutParams(params);
		//mView.setScrollContainer(false);
		
		MainWindow w = (MainWindow)mContext;
		if(w.isStatusBarHidden()) {
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		this.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
		//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		if(mView.getLayoutParams() != null) {
			//LayoutParams tmp = (LayoutParams) mView.getLayoutParams();
			//ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(tmp.width, tmp.height);
			//this.setContentView(mView,mView.getLayoutParams());
			
			this.setContentView(mView,mView.getLayoutParams());
		} else {
			this.setContentView(mView);
		}
	}
	
	
	
	
}
