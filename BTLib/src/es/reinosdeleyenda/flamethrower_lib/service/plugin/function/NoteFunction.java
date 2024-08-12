package es.reinosdeleyenda.flamethrower_lib.service.plugin.function;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import android.os.Handler;
import android.util.Log;

import es.reinosdeleyenda.flamethrower_lib.service.Connection;
import es.reinosdeleyenda.flamethrower_lib.service.plugin.Plugin;

public class NoteFunction extends PluginFunction {

	public NoteFunction(LuaState L, Plugin p,Handler h) {
		super(L, p,h);
		
	}

	@Override
	public int execute() throws LuaException {
		String str = this.getParam(2).getString();
		Log.e("LUA","NOTE("+L.getStateId()+"):"+str);
		mHandler.sendMessage(mHandler.obtainMessage(Connection.MESSAGE_LUANOTE,str));
		return 0;
	}

}
