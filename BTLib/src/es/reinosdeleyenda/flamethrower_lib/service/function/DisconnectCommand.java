package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Colorizer;
import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class DisconnectCommand extends SpecialCommand {
	
	public DisconnectCommand() {
		this.commandName = "disconnect";
	}
	public Object execute(Object o,Connection c) {
		
		
		//myhandler.sendEmptyMessage(MESSAGE_DODISCONNECT);
		String msg = "\n" + Colorizer.getRedColor() + "Disconnected." + Colorizer.getWhiteColor() + "\n";
		c.sendDataToWindow(msg);
		return null;
	}
}
