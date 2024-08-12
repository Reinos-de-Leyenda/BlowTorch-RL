/*
 * Copyright (C) Dan Block 2013
 */
package es.reinosdeleyenda.flamethrower_lib.service;

interface IConnectionBinderCallback {
	boolean isWindowShowing();
	void dataIncoming(inout byte[] seq);
	void processedDataIncoming(CharSequence seq);
	void htmlDataIncoming(String html);
	void rawDataIncoming(inout byte[] raw);
	void rawBufferIncoming(inout byte[] incoming);
	void loadSettings();
	void displayXMLError(String error);
	void displaySaveError(String error);
	void displayPluginSaveError(String plugin, String error);
	void executeColorDebug(int arg);
	void invokeDirtyExit();
	void showMessage(String message,boolean longtime);
	void showDialog(String message);
	void doVisualBell();
	void setScreenMode(boolean fullscreen);
	void showKeyBoard(String txt,boolean popup,boolean add,boolean flush,boolean clear,boolean close);
	void doDisconnectNotice(String display);
	void doLineBreak(int i);
	void reloadButtons(String setName);
	void clearAllButtons();
	void updateMaxVitals(int hp, int mana, int moves);
	void updateVitals(int hp,int mana,int moves);
	void updateEnemy(int hp);
	void updateVitals2(int hp,int mp,int maxhp, int maxmana,int enemy);
	void luaOmg(int stateIndex);
	void updateTriggerDebugString(String str);
	int getPort();
	String getHost();
	String getDisplay();
	void switchTo(String connection);
	void reloadBuffer();
	void loadWindowSettings();
	void markWindowsDirty();
	void markSettingsDirty();
	void setKeepLast(boolean keep);
	void setOrientation(int orientation);
	void setKeepScreenOn(boolean value);
	void setUseFullscreenEditor(boolean value);
	void setUseSuggestions(boolean value);
	void setCompatibilityMode(boolean value);
	void setRegexWarning(boolean value);
}
