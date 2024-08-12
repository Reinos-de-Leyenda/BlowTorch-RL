package es.reinosdeleyenda.flamethrower_lib.service.plugin.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import es.reinosdeleyenda.flamethrower_lib.alias.AliasData;
import es.reinosdeleyenda.flamethrower_lib.alias.AliasParser;
import es.reinosdeleyenda.flamethrower_lib.script.ScriptData;
import es.reinosdeleyenda.flamethrower_lib.service.Connection;
import es.reinosdeleyenda.flamethrower_lib.service.ConnectionPluginCallback;
import es.reinosdeleyenda.flamethrower_lib.service.WindowToken;
import es.reinosdeleyenda.flamethrower_lib.service.WindowTokenParser;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.Plugin;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.PluginSettings.PLUGIN_LOCATION;
import es.reinosdeleyenda.flamethrower_lib.timer.TimerData;
import es.reinosdeleyenda.flamethrower_lib.timer.TimerParser;
import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerData;
import es.reinosdeleyenda.flamethrower_lib.trigger.TriggerParser;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.sax.Element;
import android.sax.ElementListener;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.sax.TextElementListener;
import android.util.Log;
import android.util.Xml;

public class PluginParser extends BasePluginParser {

	//chainsaw for more of a "plug-in group" parser.
	
	//LuaState L = null;
	//Plugin p = null;
	
	ArrayList<Plugin> plugins = null;
	PluginSettings tmp = null;
	Handler serviceHandler = null;
	ConnectionPluginCallback parent = null;
	
	enum TYPE {
		EXTERNAL,
		INTERNAL
	};
	
	protected TYPE type;
	protected String shortName;
	protected String mDataDir;
	
	public PluginParser(String location,String name, Context context,ArrayList<Plugin> plugins,Handler serviceHandler,Connection parent) {
		super(location, context);
		// TODO Auto-generated constructor stub
		//L = p.getLuaState();
		//this.p = p;
		ApplicationInfo ai = null;
		try {
			ai = context.getApplicationContext().getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dataDir = ai.dataDir;
		mDataDir = dataDir;
		this.parent = parent;
		shortName = name;
		this.serviceHandler = serviceHandler;
		this.plugins = plugins;
		type = TYPE.EXTERNAL;
	}
	
	final TimerData current_timer = new TimerData();
	final TriggerData current_trigger = new TriggerData();
	final AliasData current_alias = new AliasData();
	final String current_script_body = new String();
	final WindowToken current_window = new WindowToken();
	final FileOption currentFileOption = new FileOption();
	final ListOption currentListOption = new ListOption();
	String current_script_name = new String();
	boolean current_script_execute = false;
	
	public ArrayList<Plugin> load() throws FileNotFoundException, IOException, SAXException {
		RootElement root = new RootElement("blowtorch");
		tmp = new PluginSettings();
		attatchListeners(root);
		
		InputStream in = this.getInputStream();
		Xml.parse(in, Xml.Encoding.UTF_8, root.getContentHandler());
		
		//tmp.setPath(path);
		//p.setSettings(tmp);
		//do alternate parsing for plugin data.
		RootElement root2 = new RootElement("blowtorch");
		Element data = root2.getChild(TAG_PLUGINS).getChild(TAG_PLUGIN);
		//data.
		//upon encountering.
		//ok, so here is now where bootstrapping happens.
		
		boolean hasXML = false;
		for(Plugin p : plugins) {
			//set up the stuff.
			switch(type) {
			case INTERNAL:
				p.setFullPath(null);
				p.setShortName(null);
				break;
			case EXTERNAL:
				p.setFullPath(path);
				p.setShortName(shortName);
				break;
			}
			if(p.getSettings().getWindows().size() > 0) {
				for(WindowToken t : p.getSettings().getWindows().values()) {
					t.setPluginName(p.getName());
				}
			}
			
			//if(p.getName().equals("whichmob")) {
			//	long time = System.currentTimeMillis();
			//}
			for(ScriptData d : p.getSettings().getScripts().values()) {
				if(d.getName().equals("bootstrap") || d.isExecute()) {
					//run this script.
					LuaState pL = p.getLuaState();
					String dataDir = null;
					mContext = parent.getContext();
					try {
						ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
						dataDir = ai.dataDir;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(dataDir == null) {
						//this is bad.
						
					} else {
						//String launchPath = Plugin.this.getFullPath();
						//L.pushString(mDataDir + "/lua/share/5.1/?.lua;"+launchPath);
						//set up the path/cpath.
						pL.getGlobal("package");
						//L.getField(-1, "path");
						//String str = L.toString(-1);
						String packagePath = dataDir + "/lua/share/5.1/?.lua";
						if(type == TYPE.EXTERNAL) {
							//packagePath += ";"+path;
						}
						pL.pushString(packagePath);
						pL.setField(-2, "path");
						//L.pop(1);
						
						pL.pushString(dataDir + "/lib/lib?.so");
						pL.setField(-2, "cpath");
						pL.pop(1);
						
					}
					
					pL.getGlobal("debug");
					pL.getField(-1, "traceback");
					pL.remove(-2);
					
					String datas = d.getData();
					pL.LloadString(datas);
					
					int ret = pL.pcall(0, 1, -2);
					if(ret != 0) {
						p.displayLuaError("Error in Bootstrap("+p.getName()+"):"+pL.getLuaObject(-1).getString());
					} else {
						//bootstrap success.
						//i think i can use the existing traceback, but the pcall has left a nil on the stack
						//L.pop(1);
						
/*! \page entry_points
 * \subsection OnPrepareXML OnPrepareXML
 * This function is called during the loading process when scripts attach xml element listeners. These are used in order to parse custom data that is saved in the descriptor file.
 * 
 * \param none
 * 
 * \note This is not the best way to do this, it came out of a necessity for dealing with legacy button data in the new button plugin. It is advantageous to save custom data in a separate file, and saving it using the Android xml suites is relatively easy using the luajava api.
 */
		
						
						pL.getGlobal("debug");
						pL.getField(-1, "traceback");
						pL.remove(-2);
						
						pL.getGlobal("OnPrepareXML");
						if(pL.isFunction(-1)) {
							hasXML = true;
							pL.pushJavaObject(data);
							int r2 = pL.pcall(1, 1, -3);
							if(r2 != 0) {
								p.displayLuaError("Error in OnPrepareXML: "+pL.getLuaObject(-1).getString());
							} else {
								pL.pop(2);
							}
						} else {
							pL.pop(2);
						}
					}
				}
			}
		}
		
		if(hasXML) {
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root2.getContentHandler());	
		}
		return plugins;
	}
	
	protected void attatchListeners(RootElement root) {
		Element pgroup = root.getChild(TAG_PLUGINS);
		
		Element plugin = pgroup.getChild(TAG_PLUGIN);
		//Element aliases = plugin.getChild(BasePluginParser.TAG_ALIASES);
		Element author = plugin.getChild("author");
		Element desc = plugin.getChild("description");
		
		Element triggers = plugin.getChild(TAG_TRIGGERS);
		Element timers = plugin.getChild(TAG_TIMERS);
		Element scripts = plugin.getChild(TAG_SCRIPT);
		Element windows = plugin.getChild("windows");
		Element window = windows.getChild("window");
		//Element alias = aliases.getChild(BasePluginParser.TAG_ALIAS);
		AliasParser.registerListeners(plugin, newItemHandler, current_alias);
		
		//Element trigger = triggers.getChild(BasePluginParser.TAG_TRIGGER);
		TriggerParser.registerListeners(triggers, newItemHandler, new TriggerData(),current_trigger,current_timer);
		
		//Element timer = timers.getChild(BasePluginParser.TAG_TIMER);
		TimerParser.registerListeners(timers, newItemHandler, new TimerData(), current_trigger, current_timer);
		
		WindowTokenParser.registerListeners(window, current_window, newItemHandler);
		
		scripts.setTextElementListener(new TextElementListener() {

			public void start(Attributes a) {
				current_script_name = a.getValue("", ATTR_NAME);
				if(a.getValue("","execute") != null) {
					if(a.getValue("","execute").equals("true")) {
						current_script_execute = true;
					} else {
						current_script_execute = false;
					}
				} else {
					current_script_execute = false;
				}
			}

			public void end(String body) {
				//Log.e("SCRIPT","SCRIPT BODY:\n"+body);
				if(current_script_name == null) {
					Random r = new Random();
					r.setSeed(System.currentTimeMillis());
					int rand = r.nextInt();
					
					current_script_name = Integer.toHexString(rand).toUpperCase();
					
				}
				//current_script_body = body;
				ScriptData d = new ScriptData();
				d.setName(current_script_name);
				d.setExecute(current_script_execute);
				d.setData(body);
				tmp.getScripts().put(current_script_name,d);
			}
			
		});
		
		plugin.setStartElementListener(new StartElementListener() {

			public void start(Attributes a) {
				
				Log.e("Parse","Parsing plugin: "+a.getValue("", ATTR_NAME));
				tmp.setName(a.getValue("", ATTR_NAME));
				//tmp.setAuthor(a.getValue("",BasePluginParser.ATTR_AUTHOR));
				tmp.setId(Integer.parseInt(a.getValue("", ATTR_ID)));
				/*if(a.getValue("","location") == null) {
					tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
				} else {
					if(a.getValue("","location").equals("external")) {
						tmp.setLocationType(PLUGIN_LOCATION.EXTERNAL);
					} else {
						tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
					}
				}*/
				switch(type) {
				case INTERNAL:
					tmp.setLocationType(PLUGIN_LOCATION.INTERNAL);
					break;
				case EXTERNAL:
					tmp.setLocationType(PLUGIN_LOCATION.EXTERNAL);
					break;
				}
			}
			
		});
		
		plugin.setEndElementListener(new EndElementListener() {

			public void end() {
				//construct the new plugin.
				Plugin p;
				try {
					p = new Plugin(serviceHandler,parent,path,mDataDir);
					tmp.setPath(path);
					p.setSettings(tmp);
					p.getSettings().getOptions().setListener(p);
					plugins.add(p);
				} catch (LuaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tmp = new PluginSettings();
				
			}
			
		});
		
		author.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void end(String body) {
				tmp.setAuthor(body);
			}
			
		});
		
		desc.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void end(String body) {
				tmp.setDescription(body);
			}
			
		});
		
		Element options = plugin.getChild("options");
		options.setStartElementListener(new StartElementListener() {

			@Override
			public void start(Attributes a) {
				String title = tmp.getName() + " Options";
				String summary = "";
				if(a.getValue("","title") != null) {
					title = a.getValue("","title");
				}
				
				if(a.getValue("","summary") != null) {
					summary = a.getValue("","summary");
				}
				
				tmp.getOptions().setTitle(title);
				tmp.getOptions().setDescription(summary);
			}
			
		});
		
		Element fileO = options.getChild("file");
		Element boolO = options.getChild("boolean");
		Element inteO = options.getChild("integer");
		Element coloO = options.getChild("color");
		Element encoO = options.getChild("encoding");
		Element listO = options.getChild("list");
		Element stringO = options.getChild("string");
		Element callbackO = options.getChild("callback");
		
		//set up sub listeners for attributes that need them.
		Element fileValue = fileO.getChild("value");
		Element filePath = fileO.getChild("path");
		Element fileExt = fileO.getChild("extension");
		
		Element listValue = listO.getChild("value");
		Element listItem = listO.getChild("item");
		
		//set up handlers.
		boolO.setTextElementListener(new TextElementListener() {
			BooleanOption opt = null;
			@Override
			public void start(Attributes a) {
				opt = new BooleanOption();
				if(a.getValue("","key") != null) {
					opt.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					opt.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					opt.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String body) {
				if(body.equals("true")) {
					opt.setValue(true);
				} else {
					opt.setValue(false);
				}
				
				tmp.getOptions().addOption(opt);
			}
			
		});
		
		coloO.setTextElementListener(new TextElementListener() {
			ColorOption opt = null;
			@Override
			public void start(Attributes a) {
				opt = new ColorOption();
				if(a.getValue("","key") != null) {
					opt.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					opt.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					opt.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String body) {
				
				opt.setValue(new String(body));
				tmp.getOptions().addOption(opt);
				//try {
					//BigInteger num = new BigInteger(body,16);
					
				//	opt.setValue(num.intValue());
					
				//	tmp.getOptions().addOption(opt);
				//}catch(NumberFormatException e) {
					
				//}
			}
			
		});
		
		fileO.setElementListener(new ElementListener() {

			@Override
			public void start(Attributes a) {
				if(a.getValue("","key") != null) {
					currentFileOption.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					currentFileOption.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					currentFileOption.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end() {
				tmp.getOptions().addOption(currentFileOption.copy());
				currentFileOption.reset();
			}
			
		});
		
		fileValue.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				//has no attributes
			}

			@Override
			public void end(String body) {
				currentFileOption.setValue(body);
			}
			
		});
		
		filePath.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				//no attributes
			}

			@Override
			public void end(String body) {
				currentFileOption.paths.add(body);
			}
			
		});
		
		fileExt.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				//has no attributes
			}

			@Override
			public void end(String body) {
				currentFileOption.extensions.add(body);
			}
			
		});
		
		stringO.setTextElementListener(new TextElementListener() {
			private StringOption opt = null;
			@Override
			public void start(Attributes a) {
				opt = new StringOption();
				if(a.getValue("","key") != null) {
					opt.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					opt.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					opt.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String body) {
				// TODO Auto-generated method stub
				opt.setValue(new String(body));
				tmp.getOptions().addOption(opt.copy());
			}

			
			
		});
		
		listO.setElementListener(new ElementListener() {

			@Override
			public void start(Attributes a) {
				if(a.getValue("","key") != null) {
					currentListOption.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					currentListOption.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					currentListOption.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end() {
				tmp.getOptions().addOption(currentListOption.copy());
				currentListOption.reset();
			}
			
		});
		
		listValue.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				//no attributes
			}

			@Override
			public void end(String body) {
				try {
				int num = Integer.parseInt(body);
					currentListOption.setValue(num);
				} catch (NumberFormatException e) {
					currentListOption.setValue(0);
				}
			}
			
		});
		
		listItem.setTextElementListener(new TextElementListener() {

			@Override
			public void start(Attributes attributes) {
				//no attributes
			}

			@Override
			public void end(String body) {
				currentListOption.items.add(body);
			}
			
		});
		
		inteO.setTextElementListener(new TextElementListener() {
			IntegerOption o = null;
			@Override
			public void start(Attributes a) {
				o = new IntegerOption();
				if(a.getValue("","key") != null) {
					o.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					o.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					o.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String body) {
				try {
					int num = Integer.parseInt(body);
					o.setValue(num);
					tmp.getOptions().addOption(o);
				} catch(NumberFormatException e) {
					
				}
			}
			
		});
		
		callbackO.setTextElementListener(new TextElementListener() {
			CallbackOption o = null;
			
			@Override
			public void start(Attributes a) {
				o = new CallbackOption();
				if(a.getValue("","key") != null) {
					o.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					o.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					o.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String arg0) {
				o.setValue(arg0);
				tmp.getOptions().addOption(o);
			}
			
		});
		
		encoO.setTextElementListener(new TextElementListener() {
			EncodingOption o = null;
			@Override
			public void start(Attributes a) {
				o = new EncodingOption();
				if(a.getValue("","key") != null) {
					o.setKey(a.getValue("","key"));
				}
				
				if(a.getValue("","title") != null) {
					o.setTitle(a.getValue("","title"));
				}
				
				if(a.getValue("","summary") != null) {
					o.setDescription(a.getValue("","summary"));
				}
			}

			@Override
			public void end(String body) {
				o.setValue(body);
				tmp.getOptions().addOption(o);
			}
			
		});
		
		
	}
	
	public interface NewItemCallback {
		public void addAlias(String key,AliasData a);
		public void addTrigger(String key,TriggerData t);
		public void addTimer(String key,TimerData t);
		public void addScript(String name,String body,boolean execute);
		public void addWindow(String name,WindowToken w);
	}
	
	protected class NewItemHandler implements NewItemCallback {

		public void addTrigger(String key, TriggerData t) {
			PluginParser.this.tmp.getTriggers().put(key, t);
		}

		public void addTimer(String key, TimerData t) {
			PluginParser.this.tmp.getTimers().put(key, t);
		}

		public void addScript(String name, String body, boolean execute) {
			ScriptData d = new ScriptData();
			d.setName(name);
			d.setData(body);
			d.setExecute(execute);
			PluginParser.this.tmp.getScripts().put(name, d);
		}

		public void addAlias(String key, AliasData a) {
			PluginParser.this.tmp.getAliases().put(key, a);
		}
		
		public void addWindow(String key, WindowToken w) {
			PluginParser.this.tmp.getWindows().put(key,w);
		}
		
	}
	
	private NewItemHandler newItemHandler = new NewItemHandler();

	public static void saveToXml(XmlSerializer out, Plugin plugin) throws IllegalArgumentException, IllegalStateException, IOException {
		out.startTag("", "plugin");
		out.attribute("", "name", plugin.getName());
		out.attribute("", "id", Integer.toString(plugin.getSettings().getId()));
		
		if(plugin.getSettings().getAuthor() != null) {
			out.startTag("", "author");
			out.text(plugin.getSettings().getAuthor());
			out.endTag("", "author");
		}
		
		if(plugin.getSettings().getDescription() != null) {
			out.startTag("", "description");
			out.cdsect(plugin.getSettings().getDescription());
			out.endTag("", "description");
		}
		
		/*if(plugin.getSettings(). != null) {
			out.startTag("", "author");
			out.text(plugin.getSettings().getAuthor());
		}*/
		
		out.startTag("","windows");
		for(WindowToken w : plugin.getSettings().getWindows().values()) {
			WindowTokenParser.saveToXml(out, w);
		}
		out.endTag("", "windows");
		
		//this is the same-ish as the connection settings parser, save alias, triggers and timers.
		out.startTag("", "aliases");
		for(AliasData a : plugin.getSettings().getAliases().values()) {
			AliasParser.saveAliasToXML(out, a);
		}
		out.endTag("", "aliases");
		
		out.startTag("", "triggers");
		for(TriggerData t : plugin.getSettings().getTriggers().values()) {
			TriggerParser.saveTriggerToXML(out, t);
		}
		out.endTag("", "triggers");
		
		out.startTag("", "timers");
		for(TimerData t : plugin.getSettings().getTimers().values()) {
			TimerParser.saveTimerToXML(out, t);
		}
		out.endTag("", "timers");
		
		out.startTag("", "options");
		if(plugin.getSettings().getOptions().title != null && !plugin.getSettings().getOptions().title.equals("")) {
			out.attribute("", "title", plugin.getSettings().getOptions().title);
		}
		if(plugin.getSettings().getOptions().description != null && !plugin.getSettings().getOptions().description.equals("")) {
			out.attribute("", "summary", plugin.getSettings().getOptions().description);
		}
		
		dumpPluginOptions(out,plugin.getSettings().getOptions());
		out.endTag("", "options");
		
		for(String scriptName : plugin.getSettings().getScripts().keySet()) {
			ScriptData d = plugin.getSettings().getScripts().get(scriptName);
			
			out.startTag("", "script");
			out.attribute("", "name", scriptName);
			if(d.isExecute()) {
				out.attribute("", "execute", "true");
			}

			out.cdsect(d.getData());
			out.endTag("", "script");
		}
		
		plugin.scriptXmlExport(out);
		
		out.endTag("", "plugin");
	}
	
	private static void dumpPluginOptions(XmlSerializer out,SettingsGroup o) throws IllegalArgumentException, IllegalStateException, IOException {
		for(Option tmp : o.getOptions()) {
			if(tmp instanceof SettingsGroup) {
				if(!((SettingsGroup)tmp).getSkipForPluginSave()) {
					dumpPluginOptions(out,(SettingsGroup)tmp);
				}
			} else {
				BaseOption opt = (BaseOption)tmp;
				opt.saveToXML(out);
			}
		}
	}
	
}
