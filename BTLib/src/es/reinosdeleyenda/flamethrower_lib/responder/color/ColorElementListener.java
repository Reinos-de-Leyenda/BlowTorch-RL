package es.reinosdeleyenda.flamethrower_lib.responder.color;

import org.xml.sax.Attributes;

import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerData;

import android.sax.TextElementListener;

public class ColorElementListener implements TextElementListener{

	//PluginSettings settings = null;
	TriggerData current_trigger = null;
	//TimerData current_timer = null;
	//Object selector = null;
	
	public ColorElementListener(TriggerData current_trigger) {
		//this.settings = settings;
		//this.selector = selector;
		//this.current_timer = current_timer;
		this.current_trigger = current_trigger;
	}
	
	public void start(Attributes a) {
		ColorAction tmp = new ColorAction();
		if(a.getValue("","text") != null) {
			tmp.setColor(Integer.parseInt(a.getValue("","text")));
		}
		
		if(a.getValue("","background") != null) {
			tmp.setBackgroundColor(Integer.parseInt(a.getValue("","background")));
		}
		current_trigger.getResponders().add(tmp.copy());
	}

	public void end(String body) {
		
	
	}

}
