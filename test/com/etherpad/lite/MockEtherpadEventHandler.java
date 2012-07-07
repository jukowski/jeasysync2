package com.etherpad.lite;

import com.etherpad.easysync2.Changeset;

public class MockEtherpadEventHandler implements EtherpadEventHandler {

	@Override
	public void onInit(EtherpadLiteConnection conn) {
		System.out.println("Got initalized");
	}

	@Override
	public void onNewChange(Changeset cs) {
		System.out.println("Got new change "+cs);
	}

	@Override
	public void onError(Exception exception) {
		System.out.println("Got some error");
		exception.printStackTrace();		
	}

}
