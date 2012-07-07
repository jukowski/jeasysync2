package com.etherpad.easysync2;

/**
 * Attributed Text
 * @author cjucovschi
 */
public class PadState {
	AttribPool pool;
	Changeset cs;
	
	public PadState cloneState () {
		PadState newObj = new PadState(this.cs.newLen);
		return newObj;
	}
	
	public void reset() {
		cs = new Changeset(cs.newLen, cs.newLen, "", "");
		pool = new AttribPool();
	}
	
	public void applyCS(Changeset cs, AttribPool newPool) {
		Changeset.moveOpsToNewPool(cs, newPool, pool);

		this.cs = this.cs.compose(cs, pool);
	}

	public void applyCS(ChangesetBuilder cs) {
		Changeset chs = cs.toChangeset();
		applyCS(chs, cs.newPool);
	}
	
	public PadState(int len) {
		cs = new Changeset(len, len, "", "");
		pool = new AttribPool();
	}
	
	public boolean hasChanged() {
		return !cs.ops.equals("");
	}

	public String getPackedCS() {
		return cs.pack();
	}

	public Changeset getCS() {
		return cs;
	}
	
	public void setCs(Changeset cs) {
		this.cs = cs;
	}
	
	public int getLen() {
		return cs.newLen;
	}
	
	public AttribPool getPool() {
		return pool;
	}
	public void setPool(AttribPool pool) {
		this.pool = pool;
	}
}
