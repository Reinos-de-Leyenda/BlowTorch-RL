package es.reinosdeleyenda.flamethrower_lib.service.function;

import es.reinosdeleyenda.flamethrower_lib.service.Colorizer;
import es.reinosdeleyenda.flamethrower_lib.service.Connection;

public class ColorDebugCommand extends SpecialCommand {
	public ColorDebugCommand() {
		commandName = "colordebug";
	}
	public Object execute(Object o,Connection c) {
		//Log.e("WINDOW","EXECUTING COLOR DEBUG COMMAND WITH STRING ARGUMENT: " + (String)o);
		String arg = (String)o;
		Integer iarg = 0;
		boolean failed = false;
		
		try {
			iarg = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			//invalid number
			failed = true;
			//errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
			//errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
		}
		if(iarg < 0 || iarg > 3) {
			//invalid number
			failed = true;
		}
		
		if(failed) {
			String errormessage = "\n" + Colorizer.getRedColor() + "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]\n";
			if(arg.equals("")) {
				errormessage += "\"colordebug\" special command requires an argument.\n";
			} else {
				errormessage += "\"colordebug\" special command is unable to use the argument: " + arg + "\n";
			}
			errormessage += "Acceptable arguments are 0, 1, 2 or 3\n";
			errormessage += "[*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*][*]"+Colorizer.getWhiteColor()+"\n";
			
			//try {
				c.sendDataToWindow(errormessage);
			//} catch (RemoteException e) {
			//	throw new RuntimeException(e);
			//} catch (UnsupportedEncodingException e) {
			//	throw new RuntimeException(e);
			//}
			
			return null;
		}
		//if we are here we are good to go.
		c.getService().doExecuteColorDebug(iarg);
		//so with the color debug mode set, we should probably dispatch a message to them.
		String success = "\n" + Colorizer.getRedColor() + "Color Debug Mode " + iarg + " activated. ";
		if(iarg == 0) {
			success = "\n" + Colorizer.getRedColor() + "Normal color processing resumed." ;
		} else if(iarg == 1) {
			success += "(color enabled, color codes shown)";
		} else if(iarg == 2) {
			success += "(color disabled, color codes shown)";
		} else if(iarg == 3) {
			success += "(color disabled, color codes not shown)";
		} else {
			success += "(this argument shouldn't happen, contact developer)";
		}
		
		success += Colorizer.getWhiteColor() +"\n";
		
		//try {
			c.sendDataToWindow(success);
		//} catch (RemoteException e) {
		//	throw new RuntimeException(e);
		//} catch (UnsupportedEncodingException e) {
		//	throw new RuntimeException(e);
		//}
		
		return null;
	}
	
}