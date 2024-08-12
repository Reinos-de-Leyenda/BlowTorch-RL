package es.reinosdeleyenda.flamethrower_lib.responder.gag;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import es.reinosdeleyenda.flamethrower_lib.responder.TriggerResponder.FIRE_WHEN;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BasePluginParser;
import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerData;

import android.sax.Element;

public final class GagActionParser {
	public static void registerListeners(Element root,TriggerData current_trigger) {
		Element gag = root.getChild(BasePluginParser.TAG_GAGACTION);
		gag.setStartElementListener(new GagElementListener(current_trigger));
	}
	
	public static void saveGagActionToXML(XmlSerializer out,GagAction r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_GAGACTION);
		if(r.getRetarget() != null) {
			out.attribute("", BasePluginParser.ATTR_RETARGET, r.getRetarget());
		}
		
		if(r.isGagLog()) {
			
		} else {
			out.attribute("", BasePluginParser.ATTR_GAGLOG, "false");
		}
		
		if(r.isGagOutput()) {
			
		} else {
			out.attribute("", BasePluginParser.ATTR_GAGOUTPUT, "false");
		}
		if(r.getFireType() != FIRE_WHEN.WINDOW_BOTH) {
			out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		}
		out.endTag("", BasePluginParser.TAG_GAGACTION);
	}
}
