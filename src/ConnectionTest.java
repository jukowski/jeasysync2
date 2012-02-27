import org.json.JSONObject;

import com.etherpad.lite.EtherpadEventHandler;
import com.etherpad.lite.EtherpadLiteConnection;

public class ConnectionTest {
	EtherpadLiteConnection conn1 = null;
	EtherpadLiteConnection conn2 = null;

	public void run() {
		
		EtherpadEventHandler e1 = new EtherpadEventHandler() {
			
			@Override
			public void onMessage(JSONObject changeset) {
				conn2.sendChange(changeset);
			}
		};

		EtherpadEventHandler e2 = new EtherpadEventHandler() {
			
			@Override
			public void onMessage(JSONObject changeset) {
			}
		};

		conn1 = new EtherpadLiteConnection("http://localhost:9001", "test", e1);
		conn2 = new EtherpadLiteConnection("http://localhost:9001", "test2", e2);
	    //System.out.println("Exiting");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConnectionTest tt = new ConnectionTest();
		tt.run();
	}

}
