package es.reinosdeleyenda.flamethrower_lib.responder.ack;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BasePluginParser;
import es.reinosdeleyenda.flamethrower_lib.timer.TimerData;
import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerData;

import android.sax.Element;

public final class AckResponderParser {
	public static void registerListeners(Element root,Object obj,TimerData current_timer,TriggerData current_trigger) {
		Element ack = root.getChild(BasePluginParser.TAG_ACKRESPONDER);
		ack.setStartElementListener(new AckElementListener(new TriggerData(),current_trigger,current_timer));
	}
	
	public static void saveResponderToXML(XmlSerializer out,AckResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_ACKRESPONDER);
		out.attribute("", BasePluginParser.ATTR_ACKWITH, r.getAckWith());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString());
		out.endTag("",BasePluginParser.TAG_ACKRESPONDER);
	}
}
