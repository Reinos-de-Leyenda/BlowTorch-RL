package es.reinosdeleyenda.flamethrower_lib.responder.toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;

import org.keplerproject.luajava.LuaState;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.widget.Toast;

import es.reinosdeleyenda.flamethrower_lib.responder.TriggerResponder;
import es.reinosdeleyenda.flamethrower_lib.window.TextTree;

public class ToastResponder extends TriggerResponder implements Parcelable {

	private String message;
	private int delay;
	
	public ToastResponder() {
		super(RESPONDER_TYPE.TOAST);
		this.setFireType(FIRE_WHEN.WINDOW_BOTH);
		message = "";
		delay = 2;
	}
	
	public ToastResponder(RESPONDER_TYPE pType) {
		super(pType);
	}
	
	public ToastResponder copy() {
		ToastResponder tmp = new ToastResponder();
		tmp.delay = this.delay;
		tmp.message = this.message;
		tmp.setFireType(this.getFireType());
		return tmp;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof ToastResponder)) return false;
		
		ToastResponder test = (ToastResponder)o;
		
		if(test.delay != this.delay) return false;
		if(test.message != this.message) return false;
		if(test.getFireType() != this.getFireType()) return false;
		
		return true;
		
	}
	
	
	public ToastResponder(Parcel in) {
		super(RESPONDER_TYPE.TOAST);
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		setMessage(in.readString());
		setDelay(in.readInt());
		String fireType = in.readString();
		if(fireType.equals(FIRE_WINDOW_OPEN)) {
			setFireType(FIRE_WHEN.WINDOW_OPEN);
		} else if (fireType.equals(FIRE_WINDOW_CLOSED)) {
			setFireType(FIRE_WHEN.WINDOW_CLOSED);
		} else if (fireType.equals(FIRE_ALWAYS)) {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		} else if (fireType.equals(FIRE_NEVER)) {
			setFireType(FIRE_WHEN.WINDOW_NEVER);
		} else {
			setFireType(FIRE_WHEN.WINDOW_BOTH);
		}
		
		//Log.e("TOAST","PARCEL IN:" + message + " |[]| " + delay );
	}

	@Override
	public boolean doResponse(Context c,TextTree tree,int lineNumber,ListIterator<TextTree.Line> iterator,TextTree.Line line,int start,int end,String matched,Object source, String displayname,String host,int port, int triggernumber,
			boolean windowIsOpen,Handler dispatcher,HashMap<String,String> captureMap,LuaState L,String name,String encoding) {
		if(windowIsOpen) {
			if(this.getFireType() == FIRE_WHEN.WINDOW_CLOSED || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) {
				return false;
			}
		} else {
			if(this.getFireType() == FIRE_WHEN.WINDOW_OPEN  || this.getFireType() == FIRE_WHEN.WINDOW_NEVER) {
				return false;
			}
		}
		
		String translated = ToastResponder.this.translate(message, captureMap);
		Toast t = Toast.makeText(c, translated, delay);
		float density = c.getResources().getDisplayMetrics().density;
		t.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, (int) (50*density));
		t.show();
		
		return false;
	}

	public static Parcelable.Creator<ToastResponder> CREATOR = new Parcelable.Creator<ToastResponder>() {

		public ToastResponder createFromParcel(Parcel source) {
			return new ToastResponder(source);
		}

		public ToastResponder[] newArray(int size) {
			return new ToastResponder[size];
		}
		
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(message);
		out.writeInt(delay);
		out.writeString(this.getFireType().getString());
	}

	public void setMessage(String message) {
		if(message == null) message = "Default Trigger Message";
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public int getDelay() {
		return delay;
	}

	@Override
	public void saveResponderToXML(XmlSerializer out)
			throws IllegalArgumentException, IllegalStateException, IOException {
		ToastResponderParser.saveToastResponderToXML(out, this);
	}

	
}
