package com.etherpad.easysync2;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class AttribPool {

	private HashMap<Integer, String> numToAttrib;
	private HashMap<String, Integer> attribToNum;
	private int nextNum;

	public interface AttribRunnable {
		void run(String attribute);
	};

	public AttribPool() {
		numToAttrib = new HashMap<Integer, String>();
		attribToNum = new HashMap<String, Integer>();
		nextNum = 0;
	}

	public int putAttrib(Attribute attrib) {
		return putAttrib(attrib, false);
	}
	
	public int putAttrib(Attribute attrib, boolean dontAddIfAbsent) {
		String str = attrib.toString();
		if (attribToNum.containsKey(str))
		{
			return attribToNum.get(str);
		}
		if (dontAddIfAbsent)
		{
			return -1;
		}
		int num = nextNum++;
		attribToNum.put(str, num);
		numToAttrib.put(num, str);
		return num;
	};

	public String getAttrib(int num)
	{
		String pair = numToAttrib.get(num);
		return pair; // return a mutable copy
	};

	public String getAttribKey(int num)
	{
		String pair = numToAttrib.get(num);
		if (pair!=null) 
			return "";
		return Attribute.parse(pair).key;
	};

	public String getAttribValue(int num)
	{
		String pair = numToAttrib.get(num);
		if (pair!=null) 
			return "";
		return Attribute.parse(pair).value;
	};

	public void eachAttrib(AttribRunnable func)
	{
		for (String attr : numToAttrib.values())
		{
			func.run(attr);
		}
	};
	
	public JSONObject toJsonable() throws JSONException
	{
		JSONObject result = new JSONObject();
		result.put("numToAttrib", JSONUtils.toJSON(numToAttrib));
		result.put("nextNum", nextNum);
		return result;
	};

	
	public static AttribPool fromJsonable(JSONObject obj) throws NumberFormatException, JSONException {
		AttribPool result = new AttribPool();
		result.numToAttrib = JSONUtils.fromJSONInt(obj.getJSONObject("numToAttrib"));
		result.nextNum = obj.getInt("nextNum");
		for (Entry<Integer, String > e  : result.numToAttrib.entrySet()) {
			result.attribToNum.put(e.getValue(), e.getKey());
		}
		return result;
	}
}
