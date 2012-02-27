package com.etherpad.easysync2;

/**
 * A custom made String Iterator
 * @param str {string} String to be iterated over
 */
public class StringIterator {
	int curIndex = 0;
	String str;

	public StringIterator(String str) {
		this.str = str;
	}

	public void assertRemaining(int n) {
		assert(n <= remaining());
	}	

	public String take(int n) {
		assertRemaining(n);
		String s = str.substring(curIndex, curIndex+n);
		curIndex += n;
		return s;
	}

	public String peek(int n) {
		assertRemaining(n);
		String s = str.substring(curIndex, curIndex+n);
		return s;
	}

	public void skip(int n)
	{
		assertRemaining(n);
		curIndex += n;
	}

	public int remaining()
	{
		return str.length() - curIndex;
	}

}
