package com.etherpad.easysync2;

import java.util.Arrays;
import java.util.Collection;

public class ChangesetBuilder {
	int oldLen;
	SmartOpAssembler assem;
	Operation o;
	StringAssembler charBank;
	PadState padState;
	AttribPool newPool;
	
	public static ChangesetBuilder new_builder(PadState padState) {
		return new ChangesetBuilder(padState);
	}
	
	public ChangesetBuilder(PadState padState) {
		newPool = new AttribPool();
		this.padState = padState;
		this.oldLen = padState.getLen();
		assem = new SmartOpAssembler();
		o = new Operation();
		charBank = new StringAssembler();
	}
	
	public String Attribs2String(Attribute attrib) {
		return "*"+Changeset.numToString(newPool.putAttrib(attrib));
	}

	public String Attribs2String(Attribute[] attrib) {
		return Changeset.makeAttribsString('=', Arrays.asList(attrib), newPool);
	}

	public String Attribs2String(Collection<Attribute> attrib) {
		return Changeset.makeAttribsString('=', attrib, newPool);
	}

	public ChangesetBuilder keep(int N, int L, Attribute[] attribs) {
		return keep(N, L, Attribs2String(attribs));
	}

	public ChangesetBuilder keep(int N, int L, Attribute attribs) {
		return keep(N, L, Attribs2String(attribs));
	}
	
	public ChangesetBuilder keep(int N, int L, String attribs) {
		o.opcode = '=';
		if (attribs != null)
			o.attribs = Changeset.makeAttribsString('=', attribs, newPool);
		else
			o.attribs = "";
		o.chars = N;
		o.lines = L;
		assem.append(o);
		return this;
	};

	public ChangesetBuilder keepText(String text, Attribute attribs) {
		return keepText(text, Attribs2String(attribs));
	};
	
	public ChangesetBuilder keepText(String text, Attribute[] attribs) {
		return keepText(text, Attribs2String(attribs));
	};

	public ChangesetBuilder keepText(String text, String attribs) {
		assem.appendOpWithText('=', text, attribs, newPool);
		return this;
	};

	public ChangesetBuilder insert (String text, Attribute attribs) {
		return insert(text, Attribs2String(attribs));
	}
	
	public ChangesetBuilder insert (String text, Attribute[] attribs) {
		return insert(text, Attribs2String(attribs));
	}
	
	public ChangesetBuilder insert (String text, String attribs) {
		assem.appendOpWithText('+', text, attribs, newPool);
		charBank.append(text);
		return this;
	};
	
	public ChangesetBuilder remove(int N, int L) {
		o.opcode = '-';
		o.attribs = "";
		o.chars = N;
		o.lines = L;
		assem.append(o);
		return this;
	}

	public Changeset toChangeset() {
		assem.endDocument();
		int newLen = oldLen + assem.getLengthChange();
		return new Changeset(oldLen, newLen, assem.toString(), charBank.toString());
	}
	
	public String toString() {
		assem.endDocument();
		int newLen = oldLen + assem.getLengthChange();
		return Changeset.pack(oldLen, newLen, assem.toString(), charBank.toString());
	}
}
