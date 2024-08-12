package es.reinosdeleyenda.flamethrower_lib.trigger;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import es.reinosdeleyenda.flamethrower_lib.responder.TriggerResponder;
import es.reinosdeleyenda.flamethrower_lib.responder.ack.AckResponderParser;
import es.reinosdeleyenda.flamethrower_lib.responder.color.ColorActionParser;
import es.reinosdeleyenda.flamethrower_lib.responder.gag.GagActionParser;
import es.reinosdeleyenda.flamethrower_lib.responder.notification.NotificationResponderParser;
import es.reinosdeleyenda.flamethrower_lib.responder.replace.ReplaceParser;
import es.reinosdeleyenda.flamethrower_lib.responder.script.ScriptResponderParser;
import es.reinosdeleyenda.flamethrower_lib.responder.toast.ToastResponderParser;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BasePluginParser;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.PluginParser;
import es.reinosdeleyenda.flamethrower_lib.timer.TimerData;

import android.sax.Element;

public final class TriggerParser {
	public static void registerListeners(Element root, PluginParser.NewItemCallback callback, Object obj, TriggerData current_trigger, TimerData current_timer) {
		//Element triggers = root.getChild("triggers");
		Element trigger = root.getChild(BasePluginParser.TAG_TRIGGER);
		TriggerElementListener listener = new TriggerElementListener(callback,current_trigger);
		
		trigger.setElementListener(listener);
		//trigger.sete
		
		AckResponderParser.registerListeners(trigger, obj, current_timer, current_trigger);
		ToastResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		NotificationResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		ScriptResponderParser.registerListeners(trigger, obj, current_trigger, current_timer);
		ReplaceParser.registerListeners(trigger, current_trigger);
		ColorActionParser.registerListeners(trigger, current_trigger);
		GagActionParser.registerListeners(trigger, current_trigger);
		
	}
	
	public static void saveTriggerToXML(XmlSerializer out,TriggerData trigger) throws IllegalArgumentException, IllegalStateException, IOException {
		if(trigger.isSave()) {
			out.startTag("", BasePluginParser.TAG_TRIGGER);
			out.attribute("", BasePluginParser.ATTR_TRIGGERTITLE, trigger.getName());
			out.attribute("", BasePluginParser.ATTR_TRIGGERPATTERN, trigger.getPattern());
			if(trigger.isInterpretAsRegex()) {
				out.attribute("", "regexp", trigger.isInterpretAsRegex() ? "true" : "false");
			}
			if(trigger.isFireOnce()) {
				out.attribute("", BasePluginParser.ATTR_TRIGGERONCE, trigger.isFireOnce() ? "true" : "false");
			}
			if(trigger.isHidden())  out.attribute("", BasePluginParser.ATTR_TRIGGERHIDDEN, "true");
			if(!trigger.isEnabled()) {
				out.attribute("", BasePluginParser.ATTR_TRIGGERENEABLED, trigger.isEnabled() ? "true" : "false");
			}
			if(trigger.getSequence() != TriggerData.DEFAULT_SEQUENCE) {
				out.attribute("", BasePluginParser.ATTR_SEQUENCE, Integer.toString(trigger.getSequence()));
			}
			if(!trigger.getGroup().equals(TriggerData.DEFAULT_GROUP)) out.attribute("", BasePluginParser.ATTR_GROUP, trigger.getGroup());
			
			//if(trigger.isKeepEvaluating()) {
				//out.attribute("", BasePluginParser.ATTR_KEEPEVALUATING, trigger.isKeepEvaluating() ? "true" : "false");
			//}
			
			for(TriggerResponder r : trigger.getResponders()){
				r.saveResponderToXML(out);
			}
			//OutputResponders(out,trigger.getResponders());
			out.endTag("", BasePluginParser.TAG_TRIGGER);
		}
	}
	
	
}
