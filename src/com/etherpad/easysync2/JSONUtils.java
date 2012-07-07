package com.etherpad.easysync2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

	public static JSONObject toJSON(Map<Integer, String> map) throws JSONException {
		JSONObject result = new JSONObject();
		for (Entry<Integer, String > e  : map.entrySet()) {
			result.put(e.getKey().toString(), e.getValue());
		}
		return result;
	}

	public static HashMap<Integer, String> fromJSONInt(JSONObject obj) throws NumberFormatException, JSONException {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		for(String name : JSONObject.getNames(obj)) {
			result.put(Integer.parseInt(name), obj.getString(name));
		}
		return result;
	}

	public static HashMap<Integer, JSONArray> fromJSONArray(JSONObject obj) throws NumberFormatException, JSONException {
		HashMap<Integer, JSONArray> result = new HashMap<Integer, JSONArray>();
		// HACK: if the list is empty. Otherwise getNames throws an exception :(
		if (obj.toString().length()==2)
			return result;
		for(String name : JSONObject.getNames(obj)) {
			result.put(Integer.parseInt(name), obj.getJSONArray(name));
		}
		return result;
	}

}
