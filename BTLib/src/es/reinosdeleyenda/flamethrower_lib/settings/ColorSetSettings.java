package es.reinosdeleyenda.flamethrower_lib.settings;

import android.os.Parcel;
import android.os.Parcelable;

import es.reinosdeleyenda.flamethrower_lib.button.SlickButtonData;

public class ColorSetSettings implements Parcelable {
	
	private int selectedColor;
	private int primaryColor;
	private int flipColor;
	private int labelColor;
	private int buttonHeight;
	private int buttonWidth;
	private int labelSize;
	private int flipLabelColor;
	private boolean locked;
	private boolean lockNewButtons;
	private boolean lockMoveButtons;
	private boolean lockEditButtons;
	
	public static final boolean DEFAULT_LOCKED = false;
	public static final boolean DEFAULT_LOCKNEWBUTTONS = true;
	public static final boolean DEFAULT_LOCKMOVEBUTTONS = false;
	public static final boolean DEFAULT_LOCKEDITBUTTONS = false;
	
	public ColorSetSettings() {
		toDefautls();
	}
	
	public ColorSetSettings copy() {
		ColorSetSettings tmp = new ColorSetSettings();
		tmp.selectedColor = this.selectedColor;
		tmp.primaryColor = this.primaryColor;
		tmp.flipColor = this.flipColor;
		tmp.labelColor = this.labelColor;
		tmp.buttonHeight = this.buttonHeight;
		tmp.buttonWidth = this.buttonWidth;
		tmp.labelSize = this.labelSize;
		tmp.flipLabelColor = this.flipLabelColor;
		tmp.locked = this.locked;
		tmp.lockNewButtons = this.lockNewButtons;
		tmp.lockMoveButtons = this.lockMoveButtons;
		tmp.lockEditButtons = this.lockEditButtons;
		return tmp;
	}
	
	public void toDefautls() {
		selectedColor = SlickButtonData.DEFAULT_SELECTED_COLOR;
		primaryColor = SlickButtonData.DEFAULT_COLOR;
		flipColor = SlickButtonData.DEFAULT_FLIP_COLOR;
		labelColor = SlickButtonData.DEFAULT_LABEL_COLOR;
		buttonWidth = SlickButtonData.DEFAULT_BUTTON_WDITH;
		buttonHeight = SlickButtonData.DEFAULT_BUTTON_HEIGHT;
		labelSize = SlickButtonData.DEFAULT_LABEL_SIZE;
		flipLabelColor = SlickButtonData.DEFAULT_FLIPLABEL_COLOR;
		locked = ColorSetSettings.DEFAULT_LOCKED;
		lockNewButtons = ColorSetSettings.DEFAULT_LOCKNEWBUTTONS;
		lockMoveButtons = ColorSetSettings.DEFAULT_LOCKMOVEBUTTONS;
		lockEditButtons = ColorSetSettings.DEFAULT_LOCKEDITBUTTONS;
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		
		if(!(o instanceof ColorSetSettings)) {
			return false;
		}
		
		ColorSetSettings test = (ColorSetSettings)o;
		
		if(this.selectedColor != test.selectedColor) return false;
		if(this.flipColor != test.flipColor) return false;
		if(this.primaryColor != test.primaryColor) return false;
		if(this.labelColor != test.labelColor) return false;
		if(this.flipLabelColor != test.flipLabelColor) return false;
		if(this.labelSize != test.labelSize) return false;
		if(this.buttonHeight != test.buttonHeight) return false;
		if(this.buttonWidth != test.buttonWidth) return false;
		if(this.locked != test.locked) return false;
		if(this.lockNewButtons != test.lockNewButtons) return false;
		if(this.lockMoveButtons != test.lockMoveButtons) return false;
		if(this.lockEditButtons != test.lockEditButtons) return false;
		return true;
	}

	public void setSelectedColor(int selectedColor) {
		this.selectedColor = selectedColor;
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	public void setPrimaryColor(int primaryColor) {
		this.primaryColor = primaryColor;
	}

	public int getPrimaryColor() {
		return primaryColor;
	}

	public void setFlipColor(int flipColor) {
		this.flipColor = flipColor;
	}

	public int getFlipColor() {
		return flipColor;
	}

	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;
	}

	public int getLabelColor() {
		return labelColor;
	}

	public void setButtonHeight(int buttonHeight) {
		this.buttonHeight = buttonHeight;
	}

	public int getButtonHeight() {
		return buttonHeight;
	}

	public void setButtonWidth(int buttonWidth) {
		this.buttonWidth = buttonWidth;
	}

	public int getButtonWidth() {
		return buttonWidth;
	}
	
	public void setLabelSize(int labelSize) {
		this.labelSize = labelSize;
	}

	public int getLabelSize() {
		return labelSize;
	}
	
	public void setFlipLabelColor(int flipLabelColor) {
		this.flipLabelColor = flipLabelColor;
	}

	public int getFlipLabelColor() {
		return flipLabelColor;
	}

	public static final Parcelable.Creator<ColorSetSettings> CREATOR = new Parcelable.Creator<ColorSetSettings>() {

		public ColorSetSettings createFromParcel(Parcel arg0) {
			return new ColorSetSettings(arg0);
		}

		public ColorSetSettings[] newArray(int arg0) {
			// TODO Auto-generated method stub
			return new ColorSetSettings[arg0];
		}
	
	
	};
	
	public ColorSetSettings(Parcel p) {
		readFromParcel(p);
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel d, int arg1) {
		// TODO Auto-generated method stub
		d.writeInt(labelColor);
		d.writeInt(selectedColor);
		d.writeInt(flipColor);
		d.writeInt(primaryColor);
		d.writeInt(buttonHeight);
		d.writeInt(buttonWidth);
		d.writeInt(labelSize);
		d.writeInt(flipLabelColor);
		d.writeInt(locked ? 1 : 0);
		d.writeInt(lockNewButtons ? 1 : 0);
		d.writeInt(lockMoveButtons ? 1 : 0);
		d.writeInt(lockEditButtons ? 1 : 0);
	}
	
	public void readFromParcel(Parcel in) {
		labelColor = in.readInt();
		selectedColor = in.readInt();
		flipColor = in.readInt();
		primaryColor = in.readInt();
		buttonHeight = in.readInt();
		buttonWidth = in.readInt();
		labelSize = in.readInt();
		flipLabelColor = in.readInt();
		if(in.readInt() == 1) {
			locked = true;
		} else {
			locked = false;
		}
		if(in.readInt() == 1) {
			lockNewButtons = true;
		} else {
			lockNewButtons = false;
		}
		if(in.readInt() == 1) {
			lockMoveButtons = true;
		} else {
			lockMoveButtons = false;
		}
		if(in.readInt() == 1) {
			lockEditButtons = true;
		} else {
			lockEditButtons = false;
		}
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLockNewButtons(boolean lockNewButtons) {
		this.lockNewButtons = lockNewButtons;
	}

	public boolean isLockNewButtons() {
		return lockNewButtons;
	}

	public void setLockMoveButtons(boolean lockMoveButtons) {
		this.lockMoveButtons = lockMoveButtons;
	}

	public boolean isLockMoveButtons() {
		return lockMoveButtons;
	}

	public void setLockEditButtons(boolean lockEditButtons) {
		this.lockEditButtons = lockEditButtons;
	}

	public boolean isLockEditButtons() {
		return lockEditButtons;
	}



}