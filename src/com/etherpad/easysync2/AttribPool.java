package com.etherpad.easysync2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AttribPool {

	private HashMap<Integer, Attribute> numToAttrib;
	private HashMap<String, Integer> attribToNum;
	private int nextNum;
	private AttribTester never;

	public static interface AttribTester {
		boolean test(String s);
	}

	public interface AttribRunnable {
		void run(String key, String value);
	};

	public AttribPool() {
		numToAttrib = new HashMap<Integer, Attribute>();
		attribToNum = new HashMap<String, Integer>();
		nextNum = 0;
		never = new AttribTester() {
			@Override
			public boolean test(String s) {
				return false;
			}
		};
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
		numToAttrib.put(num, attrib);
		return num;
	};

	public Attribute getAttrib(int num)
	{
		return numToAttrib.get(num); // return a mutable copy
	};

	public String getAttribKey(int num)
	{
		Attribute pair = numToAttrib.get(num);
		if (pair==null) 
			return "";
		return pair.key;
	};

	public String getAttribValue(int num)
	{
		Attribute pair = numToAttrib.get(num);
		if (pair==null) 
			return "";
		return pair.value;
	};

	public void eachAttrib(AttribRunnable func)
	{
		for (Attribute attr : numToAttrib.values())
		{
			func.run(attr.key, attr.value);
		}
	};

	public JSONObject toJsonable() throws JSONException
	{
		JSONObject result = new JSONObject();
//		result.put("numToAttrib", JSONUtils.toJSON(numToAttrib));
		result.put("nextNum", nextNum);
		return result;
	};


	public static AttribPool fromJsonable(JSONObject obj) throws NumberFormatException, JSONException {
		AttribPool result = new AttribPool();
		if (!obj.has("numToAttrib")) 
			return result;
		Map<Integer, JSONArray> map = JSONUtils.fromJSONArray(obj.getJSONObject("numToAttrib"));
		if (map == null || map.size()==0)
			return result;
		for (Entry<Integer, JSONArray> e : map.entrySet()) {
			Attribute q = new Attribute(e.getValue().getString(0), e.getValue().getString(1));
			result.attribToNum.put(q.toString(), e.getKey());
			result.numToAttrib.put(e.getKey(), q);
		}
		result.nextNum = obj.getInt("nextNum");
		return result;
	}

	public AttribTester attributeTester(Attribute attr) {
		int attribNum = this.putAttrib(attr, true);
		if (attribNum < 0)
		{
			return never;
		}
		else
		{
			
			final Pattern re = Pattern.compile("\\*" + Changeset.numToString(attribNum) + "(?!\\w)");
			return new AttribTester() {
				
				
				@Override
				public boolean test(String s) {
					// TODO Auto-generated method stub
					return re.matcher(s).matches();
				}
			};
		}
	}
}
