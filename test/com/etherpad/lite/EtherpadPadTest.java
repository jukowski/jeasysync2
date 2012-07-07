package com.etherpad.lite;

import org.junit.Test;

import com.etherpad.easysync2.AttribPool;
import com.etherpad.easysync2.Attribute;
import com.etherpad.easysync2.Changeset;

public class EtherpadPadTest {

	@Test
	public void test() {
		EtherpadPad pad = new EtherpadPad(new MockEtherpadEventHandler());
		pad.init("Hello World!\n\n", "|2+e", 9, new AttribPool());
		AttribPool newOne = new AttribPool();
		newOne.putAttrib(new Attribute("author", "blah"));
		pad.onNewChanges(Changeset.unpack("Z:e>5=c*0+5$ haha"), newOne, 10);
	}

	@Test
	public void test2() {
		EtherpadPad pad = new EtherpadPad(new MockEtherpadEventHandler());
		pad.init("Hello World!\n\n", "|2+e", 9, new AttribPool());
		pad.onNewChanges(Changeset.unpack("Z:e>5=c+5$ haha"), new AttribPool(), 10);
	}

}
