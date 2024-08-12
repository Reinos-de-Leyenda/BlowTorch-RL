package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class BellCommand extends SpecialCommand {
	public BellCommand() {
		this.commandName = "dobell";
	}
	public Object execute(Object o,Connection c) {
		
		c.getHandler().sendEmptyMessage(Connection.MESSAGE_BELLINC);
		
		return null;
		
	}
}