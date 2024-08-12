package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class ClearButtonCommand extends SpecialCommand {
	public ClearButtonCommand() {
		this.commandName = "clearbuttons";
	}
	
	public Object execute(Object o,Connection c) {
		c.getService().doClearAllButtons();
		return null;
	}
	
}
