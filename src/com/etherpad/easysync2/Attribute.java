package com.etherpad.easysync2;

public class Attribute {
	String key, value;
	
	public Attribute(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return key+","+value;
	}
	
	static public Attribute parse(String str) {
		String[] arr = str.split(",");
		assert(arr.length==2);
		return new Attribute(arr[0], arr[1]);
	}
}
