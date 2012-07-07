package com.etherpad.lite;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.etherpad.easysync2.Changeset;
import com.etherpad.easysync2.ChangesetBuilder;
import com.etherpad.easysync2.ChangesetUtils;
import com.etherpad.easysync2.PadState;

public class ConnectionTest {

	static class TimedAction implements ActionListener {
		EtherpadLiteConnection conn;
		
		public TimedAction(EtherpadLiteConnection conn) {
			this.conn = conn;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			PadState atext = conn.getPendingState();
			ChangesetBuilder cs = ChangesetBuilder.new_builder(atext).keep(5, 0, "").insert("blah", "");
			conn.sendChange(cs);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new EtherpadLiteConnection("http://localhost:9001", "test2", new EtherpadEventHandler() {
			EtherpadLiteConnection conn;
			Timer ticker;
			
			@Override
			public void onInit(EtherpadLiteConnection conn) {
				this.conn = conn;
				PadState atext = conn.getPendingState();
				int len = atext.getLen();
				int lines = ChangesetUtils.getNumLines(conn.getBaseText());
				ChangesetBuilder cs = ChangesetBuilder.new_builder(atext).remove(len-1, lines-1).insert("here is my text", "");
				conn.sendChange(cs);
				ticker = new Timer(1000, new TimedAction(conn));
				ticker.start();
			}
			
			@Override
			public void onError(Exception exception) {
				exception.printStackTrace();
			}
			
			@Override
			public void onNewChange(Changeset cs) {
			}
		});
	}

}