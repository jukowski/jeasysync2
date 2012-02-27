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
		if (arr.length == 2) {
			return new Attribute(arr[0], arr[1]);
		} else 
			if (arr.length == 1) {
				return new Attribute(arr[0], "");				
			} else
				return new Attribute("","");
	}
}
