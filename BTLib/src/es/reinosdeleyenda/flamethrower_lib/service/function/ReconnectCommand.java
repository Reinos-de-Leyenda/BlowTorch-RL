package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Colorizer;
import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class ReconnectCommand extends SpecialCommand {
	public ReconnectCommand() {
		this.commandName = "reconnect";
	}
	public Object execute(Object o,Connection c) {
		
		
		//myhandler.sendEmptyMessage(MESSAGE_RECONNECT);
		String msg = "\n" + Colorizer.getRedColor() + "Reconnecting . . ." + Colorizer.getWhiteColor() + "\n";
		c.sendDataToWindow(msg);
		return null;
	}
}
