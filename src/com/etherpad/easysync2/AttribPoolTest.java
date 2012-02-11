package com.etherpad.easysync2;

import org.junit.Test;

public class AttribPoolTest {

	@Test
	public void test() {
		AttribPool pool = new AttribPool();
		assert(pool.putAttrib(new Attribute("author", "a1"), false)==0);
		assert(pool.putAttrib(new Attribute("author", "a2"), false)==1);
		assert(pool.putAttrib(new Attribute("author", "a1"), false)==0);
		assert(pool.getAttrib(0).equals("author,a1"));
		assert(pool.getAttrib(1).equals("author,a2"));
		assert(pool.getAttribKey(0).equals("author"));
		assert(pool.getAttribKey(1).equals("author"));
		assert(pool.getAttribValue(0).equals("a1"));
		assert(pool.getAttribValue(1).equals("a2"));
		
	}

}
