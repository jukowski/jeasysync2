package com.etherpad.lite;

import org.json.JSONException;
import org.json.JSONObject;

public class EtherpadCommUtils {
	public static boolean isCollabRomm(JSONObject obj) throws JSONException {
		return obj.has("type") && obj.getString("type").equals("COLLABROOM");
	}

	public static boolean isNewChanges(JSONObject obj) throws JSONException {
		if (!EtherpadCommUtils.isCollabRomm(obj)) {
			return false;
		}
		JSONObject arg0 = obj.getJSONObject("data");
		return arg0.has("type") && arg0.getString("type").equals("NEW_CHANGES");
	}

	public static Integer getCSRevision(JSONObject obj) throws JSONException {
		if (!isNewChanges(obj)) {
			return -1;
		}
		JSONObject arg0 = obj.getJSONObject("data");
		JSONObject arg1 = obj.getJSONObject("data");
		return 1;
	}

	
}
