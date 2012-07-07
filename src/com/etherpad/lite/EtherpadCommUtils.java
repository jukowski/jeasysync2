package com.etherpad.lite;

import java.util.Random;

public class EtherpadCommUtils {
	
	public static String genSessionID() {
		final String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		Random r = new Random();
		char []q = new char[32];
		for (int len=31; len>=0; len--) {
			q[len] = alpha.charAt(r.nextInt(alpha.length()));
		}
		return new String(q);
	}
	
}
