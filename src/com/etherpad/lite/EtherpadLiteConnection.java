package com.etherpad.lite;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import com.etherpad.easysync2.AttribPool;
import com.etherpad.easysync2.Changeset;
import com.etherpad.easysync2.ChangesetBuilder;
import com.etherpad.easysync2.PadState;

public class EtherpadLiteConnection  {
	SocketIO sock = new SocketIO();
	String padID;
	EtherpadPad pad;

	class ConnHandler implements IOCallback, ConnectionCallback {
		EtherpadPad pad;
		boolean firstMessage = true;		
		EtherpadLiteConnection conn;
		Logger logger;
		
		public ConnHandler(EtherpadPad pad, EtherpadLiteConnection conn) {
			this.pad = pad;
			this.conn = conn;
			pad.setCallback(this);
			logger = Logger.getLogger(this.getClass().getName());
		}
				
		@Override
		public void sendChange(Changeset cs, AttribPool pool, int rev) {
			try {
				//{"type":"COLLABROOM","component":"pad","data":{"type":"USER_CHANGES","baseRev":0,"changeset":"Z:6c>1*0+1$ ","apool":{"numToAttrib":{"0":["author","a.jEZ5PvMeaHliX25s"]},"nextNum":1}}}
				JSONObject msg = new JSONObject();
				JSONObject msg1 = new JSONObject();
				msg.put("type", "COLLABROOM");
				msg.put("component", "pad");
				msg1.put("type", "USER_CHANGES");
				msg1.put("baseRev", rev);
				msg1.put("changeset", cs.pack());
				msg1.put("apool", pool.toJsonable());
				msg.put("data", msg1);
				sock.send(msg);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void onConnect() {
			JSONObject msg = new JSONObject();
			try {
				msg.put("component", "pad");
				msg.put("type", "CLIENT_READY");
				msg.put("padId", padID);
				msg.put("sessionID", "asdasgasdg");
				msg.put("token", "sdgasdlgh");
				msg.put("password", "fasdfasdg");
				msg.put("protocolVersion", 2);
				sock.send(msg);
			} catch (JSONException e) {
				handler.onError(e);
			}			
		}
		
		@Override
		public void onDisconnect() {
		}
		
		@Override
		public void onError(SocketIOException arg0) {
			handler.onError(arg0);
		}

		@Override
		public void onMessage(String arg0, IOAcknowledge arg1) {
			logger.severe("Not implemented for "+arg1);
		}

		@Override
		public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
			logger.severe("Not implemented for "+arg0);
		}

		public void handleFirstMessage(JSONObject arg0) {
			try {
				JSONObject vars = arg0.getJSONObject("data").getJSONObject("collab_client_vars"); 
				JSONObject t = vars.getJSONObject("initialAttributedText");

				pad.init(t.getString("text"), t.getString("attribs"), vars.getInt("rev"), AttribPool.fromJsonable(vars.getJSONObject("apool")));
				ticker = new Timer(500, pad);
				ticker.start();

				handler.onInit(conn);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				handler.onError(e);
			}
		}
		@Override
		public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
			logger.info("XXXXXXXXXX Received from server "+arg0);
			if (firstMessage) {
				firstMessage = false;
				handleFirstMessage(arg0);
				return;
			}

			try {
				if (arg0.has("type") && arg0.getString("type").equals("COLLABROOM")) {
					arg0 = arg0.getJSONObject("data");
				}

				if (arg0.has("type") && arg0.getString("type").equals("NEW_CHANGES")) {
					Changeset cs = Changeset.unpack(arg0.getString("changeset"));
					AttribPool t;
					if (arg0.has("apool")) {
						t = AttribPool.fromJsonable(arg0.getJSONObject("apool"));
					} else
						t = new AttribPool();
					
					int newRev = arg0.getInt("newRev");
					pad.onNewChanges(cs, t, newRev);
					return;
				}

				if (arg0.has("type") && arg0.getString("type").equals("ACCEPT_COMMIT")) {
					int newRev = arg0.getInt("newRev");
					pad.onAcceptCommit(newRev);
					return;
				}
				if (arg0.has("type") && arg0.getString("type").equals("USER_NEWINFO")) {
					return;
				}
				
				logger.severe("Message "+arg0+" could not be handled");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				handler.onError(e);
			}
		}


	}
	
	Timer ticker;
	EtherpadEventHandler handler;

	Logger log;

	public String getBaseText() {
		return pad.getBaseText();
	}
	
	public AttribPool getBasePool() {
		return pad.getBasePool();
	}
	
	public String getBaseAttrib() {
		return pad.getBaseAttrib();
	}
	
	
	public PadState getPendingState() {
		return pad.pendingState;
	}

	public void disconnect() {
		sock.disconnect();
	}
	
	public void sendChange(ChangesetBuilder cs) {
		pad.sendChange(cs);
	}
	
	public String getSessionID(String url) throws MalformedURLException {
		URL add = new URL(url);
		URL newAddr = new URL(add.getProtocol(), add.getHost(), add.getPort(), "/favicon.ico");
		try {
			URLConnection conn = newAddr.openConnection();
			String cookie = conn.getHeaderField("Set-Cookie");
			String []flds = cookie.split(";");
			return flds[0];
		} catch (IOException e) {
			handler.onError(e);
		}
		return null;
	}

	
	public EtherpadLiteConnection(String url, String padID, EtherpadEventHandler handler) {
		log = Logger.getLogger(this.getClass().getName());
		pad = new EtherpadPad(handler);
		this.padID = padID;
		try {
			String cookie = getSessionID(url);
			log.finest("SocketIO cookie = "+cookie);
			sock.addHeader("cookie", cookie);
			sock.connect(url, new ConnHandler(pad, this));
		} catch (MalformedURLException e) {
			this.handler.onError(e);
		}

		log.info("Opening connection to pad "+padID+" at URL "+url);
		this.handler = handler;
	}

}
