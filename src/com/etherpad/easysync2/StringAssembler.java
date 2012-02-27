package com.etherpad.easysync2;

/**
 * A custom made StringBuffer 
 */
public class StringAssembler {
	StringBuffer sb;
	
	public StringAssembler() {
		sb = new StringBuffer();
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	public void append(String str) {
		sb.append(str);
	}
	
	public void clear() {
		sb = new StringBuffer();
	}
}
