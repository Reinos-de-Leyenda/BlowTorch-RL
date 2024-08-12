package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class DirtyExitCommand extends SpecialCommand {
	public DirtyExitCommand() {
		this.commandName = "closewindow";
	}
	public Object execute(Object o,Connection c) {
		
		c.getService().doDirtyExit();
		return null;
	}
}
