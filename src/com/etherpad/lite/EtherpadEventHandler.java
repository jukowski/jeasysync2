package com.etherpad.lite;

import org.json.JSONObject;

public interface EtherpadEventHandler {
	void onMessage(JSONObject obj);
}
