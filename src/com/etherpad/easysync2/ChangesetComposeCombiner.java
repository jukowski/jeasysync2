package com.etherpad.easysync2;

import com.etherpad.easysync2.Operation.OperationCombiner;

public class ChangesetComposeCombiner implements OperationCombiner {

	StringIterator bankIter1, bankIter2;
	StringAssembler bankAssem;
	AttribPool pool;
	
	public ChangesetComposeCombiner(StringIterator bankIter1, StringIterator bankIter2, StringAssembler bankAssem, AttribPool pool) {
		this.bankIter1 = bankIter1;
		this.bankIter2 = bankIter2;
		this.bankAssem = bankAssem;
		this.pool = pool; 
	}
	
	@Override
	public void combine(Operation op1, Operation op2, Operation opOut) {
		//var debugBuilder = exports.stringAssembler();
		//debugBuilder.append(exports.opString(op1));
		//debugBuilder.append(',');
		//debugBuilder.append(exports.opString(op2));
		//debugBuilder.append(' / ');
		char op1code = op1.opcode;
		char op2code = op2.opcode;
		if (op1code == '+' && op2code == '-') {
			bankIter1.skip(Math.min(op1.chars, op2.chars));
		}
		
		SlicerOperationCombiner _slicerZipperFunc = new SlicerOperationCombiner(pool);
		
		_slicerZipperFunc.combine(op1, op2, opOut);
		if (opOut.opcode == '+') {
			if (op2code == '+') {
				bankAssem.append(bankIter2.take(opOut.chars));
			} else {
				bankAssem.append(bankIter1.take(opOut.chars));
			}
		}

		//debugBuilder.append(exports.opString(op1));
		//debugBuilder.append(',');
		//debugBuilder.append(exports.opString(op2));
		//debugBuilder.append(' -> ');
		//debugBuilder.append(exports.opString(opOut));
		//print(debugBuilder.toString());
	}

}
