package com.etherpad.lite;
import org.json.JSONObject;

public class ConnectionTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EtherpadLiteConnection conn = null;
		conn = new EtherpadLiteConnection("http://localhost:9001", "test2", new EtherpadEventHandler() {
			@Override
			public void onMessage(JSONObject obj) {
				System.out.println("Received some message "+obj);
			}
		});
	}

}
