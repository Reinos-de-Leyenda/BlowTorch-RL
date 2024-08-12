package es.reinosdeleyenda.flamethrower_lib.alias;

import java.io.IOException;

import org.xmlpull.v1.XmlSerializer;

import android.sax.Element;

import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BasePluginParser;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.PluginParser;

public class AliasParser {
	//PluginSettings settings = null;
	//final AliasData current_alias = new AliasData();
	//public AliasParser(Element root, PluginSettings settings) {
		//this.settings = settings;
		//registerListeners(root);
	//}
	public static void registerListeners(Element root, PluginParser.NewItemCallback callback, AliasData current_alias) {
		Element aliases = root.getChild(BasePluginParser.TAG_ALIASES);
		Element alias = aliases.getChild(BasePluginParser.TAG_ALIAS);
		alias.setStartElementListener(new AliasElementListener(callback,current_alias));
	}
	
	public static void saveAliasToXML(XmlSerializer out,AliasData data) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", BasePluginParser.TAG_ALIAS);
		out.attribute("", BasePluginParser.ATTR_PRE, data.getPre());
		out.attribute("", BasePluginParser.ATTR_POST, data.getPost());
		if(data.isEnabled()) {
			out.attribute("", "enabled", (data.isEnabled() == true) ? "true" : "false");
		}
		out.endTag("", BasePluginParser.TAG_ALIAS);
	}
	
	
	
	
	
}
