package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class SwitchWindowCommand extends SpecialCommand {
	public SwitchWindowCommand() {
		this.commandName = "switch";
	}
	
	public Object execute(Object o,Connection c) {
		String connection = (String)o;
		
		c.getService().setClutch(connection);
		c.switchTo(connection);
		
		return null;
	}
}
