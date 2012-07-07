package com.etherpad.lite;

import junit.framework.Assert;

import org.junit.Test;

public class EtherpadCommUtilsTest {

	@Test
	public void testSessionCreation() {
		String s1 = EtherpadCommUtils.genSessionID();
		String s2 = EtherpadCommUtils.genSessionID();
		System.out.println(s1);
		Assert.assertEquals(s1.length(), 32);
		Assert.assertEquals(s2.length(), 32);
		Assert.assertNotSame(s1, s2);
	}

}
