package es.reinosdeleyenda.flamethrower_lib.responder.notification;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BasePluginParser;
import es.reinosdeleyenda.flamethrower_lib.timer.TimerData;
import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerData;

import android.sax.Element;

public final class NotificationResponderParser {
	public static void registerListeners(Element root,Object obj,TriggerData current_trigger,TimerData current_timer) {
		Element note = root.getChild(BasePluginParser.TAG_NOTIFICATIONRESPONDER);
		note.setStartElementListener(new NotificationElementListener(obj,current_trigger,current_timer));
	}
	
	public static void saveNotificationResponderToXML(XmlSerializer out,NotificationResponder r) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_NOTIFICATIONRESPONDER);
		out.attribute("", BasePluginParser.ATTR_NOTIFICATIONTITLE,r.getTitle());
		out.attribute("", BasePluginParser.ATTR_NOTIFICATIONMESSAGE, r.getMessage());
		out.attribute("", BasePluginParser.ATTR_FIRETYPE, r.getFireType().getString() );
		if(r.isUseDefaultSound()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTSOUND, "true");
			out.attribute("", BasePluginParser.ATTR_SOUNDPATH, r.getSoundPath());
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTSOUND, "false");
		}
		
		if(r.isUseDefaultLight()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTLIGHT, "true");
			out.attribute("", BasePluginParser.ATTR_LIGHTCOLOR, Integer.toHexString(r.getColorToUse()));
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTLIGHT, "false");
		}
		
		if(r.isUseDefaultVibrate()) {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTVIBRATE, "true");
			out.attribute("", BasePluginParser.ATTR_VIBRATELENGTH, Integer.toString(r.getVibrateLength()));
		} else {
			out.attribute("", BasePluginParser.ATTR_USEDEFAULTVIBRATE, "false");
		}
		
		out.attribute("", BasePluginParser.ATTR_NEWNOTIFICATION, (r.isSpawnNewNotification()) ? "true" : "false");
		out.attribute("", BasePluginParser.ATTR_USEONGOING, (r.isUseOnGoingNotification()) ? "true" : "false");
		
		out.endTag("", BasePluginParser.TAG_NOTIFICATIONRESPONDER);
	}
}
