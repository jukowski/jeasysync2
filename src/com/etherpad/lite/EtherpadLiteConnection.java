package com.etherpad.lite;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.etherpad.easysync2.Changeset;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

public class EtherpadLiteConnection {
	SocketIO sock = new SocketIO();
	String padID;
	String text;
	EtherpadEventHandler handler;
	
	class EtherpadLiteConnectionHandler implements IOCallback {

		boolean firstMessage = true;
		
		@Override
		public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
			
		}

		@Override
		public void onConnect() {
			JSONObject msg = new JSONObject();
			try {
				msg.put("component", "pad");
				msg.put("type", "CLIENT_READY");
				msg.put("padId", padID);
				msg.put("sessionID", "sdfiasdhgas");
				msg.put("token", "sdgasdlgh");
				msg.put("password", "asdgas");
				msg.put("protocolVersion", 2);
				sock.send(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}

		@Override
		public void onDisconnect() {
		}

		@Override
		public void onError(SocketIOException arg0) {
		}

		@Override
		public void onMessage(String arg0, IOAcknowledge arg1) {
		}

		public void handleFirstMessage(JSONObject arg0) {
			try {
				JSONObject vars = arg0.getJSONObject("collab_client_vars"); 
				for (String r : JSONObject.getNames(vars))
					System.out.println(r);
				text = vars.getJSONObject("initialAttributedText").getString("text");
				System.out.println(text);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void sendClearAttributes() {
			//"type":"COLLABROOM","component":"pad","data":{"type":"USER_CHANGES","baseRev":102,"changeset":"Z:qw>0|1=6*0=8$","apool":{"numToAttrib":{"0":["author",""]},"nextNum":1}}
			JSONObject msg = new JSONObject();
			JSONObject cs = new JSONObject();

			try {
				msg.put("component", "pad");
				msg.put("type", "COLLABROOM");
				msg.put("data", cs);
				cs.put("type", "USER_CHANGES");
				sock.send(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		@Override
		public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
			if (firstMessage) {
				firstMessage = false;
				handleFirstMessage(arg0);
				return;
			}
			System.out.println(arg0.toString());
			handler.onMessage(arg0);
			
			try {
				if (arg0.has("type") && arg0.getString("type").equals("COLLABROOM")) {
					arg0 = arg0.getJSONObject("data");
				}
				if (arg0.has("type") && arg0.getString("type").equals("NEW_CHANGES")) {
					//handler.onChangeset(arg0.getString("changeset"));
//					ChangeSet cs = ChangeSet.unpack(arg0.getString("changeset"));
//					text = cs.applyToText(text);
				}
				
				if (text.contains("damn")) {
					sendClearAttributes();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendChange(JSONObject arg0) {
		sock.send(arg0);
	}
	
	public EtherpadLiteConnection(String url, String padID, EtherpadEventHandler handler) {
		this.padID = padID;
		this.handler = handler;
		try {
			sock.connect("http://localhost:9001", new EtherpadLiteConnectionHandler());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
}
