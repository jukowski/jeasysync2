package com.etherpad.lite;

import com.etherpad.easysync2.Changeset;

public interface EtherpadEventHandler {
	void onInit(EtherpadLiteConnection conn);
	
	void onNewChange(Changeset cs);
	void onError(Exception exception);
}