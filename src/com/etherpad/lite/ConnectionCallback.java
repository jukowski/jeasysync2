package com.etherpad.lite;

import com.etherpad.easysync2.AttribPool;
import com.etherpad.easysync2.Changeset;

public interface ConnectionCallback {
	void sendChange(Changeset cs, AttribPool pool, int rev);
}
