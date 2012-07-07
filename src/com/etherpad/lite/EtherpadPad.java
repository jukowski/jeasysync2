package com.etherpad.lite;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.etherpad.easysync2.AttribPool;
import com.etherpad.easysync2.Changeset;
import com.etherpad.easysync2.ChangesetBuilder;
import com.etherpad.easysync2.PadState;

public class EtherpadPad implements ActionListener {
	Lock pendingMutex;

	Logger logger;
	
	String baseText;
	String baseAttrib;
	int baseRev;
	AttribPool basePool;

	EtherpadEventHandler handler;
	ConnectionCallback callback;

	PadState sentState;
	PadState pendingState;

	public EtherpadPad(EtherpadEventHandler handler) {
		this.handler = handler;
		this.pendingMutex = new ReentrantLock();
		logger = Logger.getLogger(this.getClass().getName());
		callback = null;
	}

	public void setCallback(ConnectionCallback callback) {
		this.callback = callback;
	}

	public String getBaseText() {
		return baseText;
	}

	public AttribPool getBasePool() {
		return basePool;
	}

	public String getBaseAttrib() {
		return baseAttrib;
	}


	public PadState getPendingState() {
		return pendingState;
	}

	public void init(String baseText, String baseAttrib, int baseRev, AttribPool basePool) {
		this.baseText = baseText;
		this.baseAttrib = baseAttrib;
		this.baseRev = baseRev;
		this.basePool = basePool;

		pendingState = new PadState(baseText.length());
		sentState = new PadState(baseText.length());
	}

	void propagateBaseChanges(Changeset B, AttribPool p ){
		applyToBase(B, p);
		Changeset Xp = Changeset.follow(B, sentState.getCS(), false, sentState.getPool());
		Changeset XB = Changeset.follow(sentState.getCS(),B, false, sentState.getPool());
		Changeset Yp = Changeset.follow(XB, pendingState.getCS(), false, pendingState.getPool());
		Changeset D = Changeset.follow(pendingState.getCS(), XB, false, pendingState.getPool());
		sentState.setCs(Xp);
		pendingState.setCs(Yp);
		handler.onNewChange(D);
	}

	public void sendChange(ChangesetBuilder cs) {
		pendingMutex.lock();
		try {
			pendingState.applyCS(cs);
		} catch (Exception e) {
			handler.onError(e);
		}
		finally {
			pendingMutex.unlock();
		}
	}


	void applyToBase(Changeset cs, AttribPool p) {
		prepareApplying(cs, p);
		baseText = cs.applyToText(baseText);
		baseAttrib = cs.applyToAttribution(baseAttrib, basePool);
	}

	/**
	 * Rewrite attributes from cs from the localPool to the global pool  
	 * @param cs 
	 * @param localPool
	 */
	void prepareApplying(Changeset cs, AttribPool localPool) {
		Changeset.moveOpsToNewPool(cs, localPool, basePool);
	}

	void onNewChanges(Changeset cs, AttribPool t, int newRev) {
		logger.info("New Change with rev" + newRev);
		if (newRev > baseRev + 1) {
			logger.severe("Sent changeset revision is too high. Trying to recover by sending an implicit ACCEPT");
			onAcceptCommit(newRev-1);
		}
		pendingMutex.lock();
		try {
			propagateBaseChanges(cs, t);
			baseRev = newRev;
		} finally {
			pendingMutex.unlock();
		}
	}

	void onAcceptCommit(int newRev) {
		logger.info("Accepting commit with rev" + newRev);
		Changeset cs = sentState.getCS();
		applyToBase(cs, sentState.getPool());
		baseRev = newRev;
		sentState.reset();
	}


	private void sendPendingChanges() {
		sentState = pendingState;
		pendingState = sentState.cloneState();
		if (callback != null)
			callback.sendChange(sentState.getCS(), sentState.getPool(), baseRev);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		pendingMutex.lock();
		if (pendingState.hasChanged() && !sentState.hasChanged()) {
			sendPendingChanges();
		} else {
			if (pendingState.hasChanged())
				logger.info("Failed to send changes because ACCEPT was not yet received. ");
			else
				logger.info("No changes to be send. ");
		}
		pendingMutex.unlock();
	}
}
