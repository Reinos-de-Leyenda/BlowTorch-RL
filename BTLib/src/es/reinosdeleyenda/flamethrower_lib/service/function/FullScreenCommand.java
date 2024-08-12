package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.settings.BaseOption;

public class FullScreenCommand extends SpecialCommand {
	public FullScreenCommand() {
		this.commandName = "togglefullscreen";
	}
	
	public Object execute(Object o,Connection c) {
		Boolean current = (Boolean)((BaseOption)c.getSettings().findOptionByKey("fullscreen")).getValue();
		c.getSettings().setOption("fullscreen", ((Boolean)!current).toString());
		//c.service.doExecuteFullscreen();
		return null;
	}
}

