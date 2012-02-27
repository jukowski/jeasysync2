package com.etherpad.easysync2;

import com.etherpad.easysync2.Operation.OperationCombiner;

/**
 * Class used as parameter for applyZip to apply a Changeset to an 
 * attribute 
 */
public class SlicerOperationCombiner implements OperationCombiner {

	AttribPool pool;

	public SlicerOperationCombiner(AttribPool pool) {
		this.pool = pool;
	}

	@Override
	public void combine(Operation attOp, Operation csOp, Operation opOut) {
		// attOp is the op from the sequence that is being operated on, either an
		// attribution string or the earlier of two exportss being composed.
		// pool can be null if definitely not needed.
		//print(csOp.toSource()+" "+attOp.toSource()+" "+opOut.toSource());
		if (attOp.opcode == '-') {
			Operation.copyOp(attOp, opOut);
			attOp.opcode = 0;
		} else if (attOp.opcode==0) {
			Operation.copyOp(csOp, opOut);
			csOp.opcode = 0;
		} else {
			switch (csOp.opcode) {
			case '-':
			{
				if (csOp.chars <= attOp.chars) {
					// delete or delete part
					if (attOp.opcode == '=') {
						opOut.opcode = '-';
						opOut.chars = csOp.chars;
						opOut.lines = csOp.lines;
						opOut.attribs = "";
					}
					attOp.chars -= csOp.chars;
					attOp.lines -= csOp.lines;
					csOp.opcode = 0;
					if (attOp.chars == 0) {
						attOp.opcode = 0;
					}
				} else {
					// delete and keep going
					if (attOp.opcode == '=') {
						opOut.opcode = '-';
						opOut.chars = attOp.chars;
						opOut.lines = attOp.lines;
						opOut.attribs = "";
					}
					csOp.chars -= attOp.chars;
					csOp.lines -= attOp.lines;
					attOp.opcode = 0;
				}
				break;
			}
			case '+':
			{
				// insert
				Operation.copyOp(csOp, opOut);
				csOp.opcode = 0;
				break;
			}
			case '=':
			{
				if (csOp.chars <= attOp.chars) {
					// keep or keep part
					opOut.opcode = attOp.opcode;
					opOut.chars = csOp.chars;
					opOut.lines = csOp.lines;
					opOut.attribs = Changeset.composeAttributes(attOp.attribs, csOp.attribs, attOp.opcode == '=', pool);
					csOp.opcode = 0;
					attOp.chars -= csOp.chars;
					attOp.lines -= csOp.lines;
					if (attOp.chars == 0) {
						attOp.opcode = 0;
					}
				} else {
					// keep and keep going
					opOut.opcode = attOp.opcode;
					opOut.chars = attOp.chars;
					opOut.lines = attOp.lines;
					opOut.attribs = Changeset.composeAttributes(attOp.attribs, csOp.attribs, attOp.opcode == '=', pool);
					attOp.opcode = 0;
					csOp.chars -= attOp.chars;
					csOp.lines -= attOp.lines;
				}
				break;
			}
			case 0:
			{
				Operation.copyOp(attOp, opOut);
				attOp.opcode = 0;
				break;
			}
			}
		}		
	}

}
