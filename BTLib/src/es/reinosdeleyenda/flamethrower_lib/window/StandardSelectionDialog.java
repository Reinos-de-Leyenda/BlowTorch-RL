package es.reinosdeleyenda.flamethrower_lib.window;

import es.reinosdeleyenda.flamethrower_lib.service.IConnectionBinder;

import android.content.Context;

public class StandardSelectionDialog extends BaseSelectionDialog {
	
	protected IConnectionBinder service;
	
	
	public StandardSelectionDialog(Context context,IConnectionBinder service)  {
		super(context);
		this.service = service;
	}
	
	


}
