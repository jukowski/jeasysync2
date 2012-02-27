package com.etherpad.easysync2;

import com.etherpad.easysync2.AttribPool.AttribTester;
import com.etherpad.easysync2.Operation.OperationCombiner;

public class FollowOperationCombiner implements OperationCombiner {

	boolean reverseInsertOrder;
	AttribPool pool;
	Changeset unpacked1;
	Changeset unpacked2;
	int oldPos;
	int newLen;

	public FollowOperationCombiner(boolean reverseInsertOrder, 
			AttribPool pool,	
			Changeset unpacked1,
			Changeset unpacked2,
			int oldPos,
			int newLen) {
		this.reverseInsertOrder = reverseInsertOrder;
		this.pool = pool;
		this.unpacked1 = unpacked1;
		this.unpacked2 = unpacked2;
	}

	@Override
	public void combine(Operation op1, Operation op2, Operation opOut) {
		AttribTester hasInsertFirst = pool.attributeTester(new Attribute("insertorder", "first"));
		// TODO Auto-generated method stub
		StringIterator chars1 = new StringIterator(unpacked1.charBank);
		StringIterator chars2 = new StringIterator(unpacked2.charBank);

		if (op1.opcode == '+' || op2.opcode == '+')
		{
			int whichToDo;
			if (op2.opcode != '+')
			{
				whichToDo = 1;
			}
			else if (op1.opcode != '+')
			{
				whichToDo = 2;
			}
			else
			{
				// both +
				String firstChar1 = chars1.peek(1);
				String firstChar2 = chars2.peek(1);
				Boolean insertFirst1 = hasInsertFirst.test(op1.attribs);
				Boolean insertFirst2 = hasInsertFirst.test(op2.attribs);
				if (insertFirst1 && !insertFirst2)
				{
					whichToDo = 1;
				}
				else if (insertFirst2 && !insertFirst1)
				{
					whichToDo = 2;
				}
				// insert string that doesn't start with a newline first so as not to break up lines
				else if (firstChar1.equals("\n")  && firstChar2.equals("\n"))
				{
					whichToDo = 2;
				}
				else if (!firstChar1.equals("\n") && firstChar2.equals("\n"))
				{
					whichToDo = 1;
				}
				// break symmetry:
				else if (reverseInsertOrder)
				{
					whichToDo = 2;
				}
				else
				{
					whichToDo = 1;
				}
			}
			if (whichToDo == 1)
			{
				chars1.skip(op1.chars);
				opOut.opcode = '=';
				opOut.lines = op1.lines;
				opOut.chars = op1.chars;
				opOut.attribs = "";
				op1.opcode = 0;
			}
			else
			{
				// whichToDo == 2
				chars2.skip(op2.chars);
				Operation.copyOp(op2, opOut);
				op2.opcode = 0;
			}
		}
		else if (op1.opcode == '-')
		{
			if (op2.opcode == 0)
			{
				op1.opcode = 0;
			}
			else
			{
				if (op1.chars <= op2.chars)
				{
					op2.chars -= op1.chars;
					op2.lines -= op1.lines;
					op1.opcode = 0;
					if (op2.chars == 0)
					{
						op2.opcode = 0;
					}
				}
				else
				{
					op1.chars -= op2.chars;
					op1.lines -= op2.lines;
					op2.opcode = 0;
				}
			}
		}
		else if (op2.opcode == '-')
		{
			Operation.copyOp(op2, opOut);
			if (op1.opcode == 0)
			{
				op2.opcode = 0;
			}
			else if (op2.chars <= op1.chars)
			{
				// delete part or all of a keep
				op1.chars -= op2.chars;
				op1.lines -= op2.lines;
				op2.opcode = 0;
				if (op1.chars == 0)
				{
					op1.opcode = 0;
				}
			}
			else
			{
				// delete all of a keep, and keep going
				opOut.lines = op1.lines;
				opOut.chars = op1.chars;
				op2.lines -= op1.lines;
				op2.chars -= op1.chars;
				op1.opcode = 0;
			}
		}
		else if (op1.opcode==0)
		{
			Operation.copyOp(op2, opOut);
			op2.opcode = 0;
		}
		else if (op2.opcode==0)
		{
			Operation.copyOp(op1, opOut);
			op1.opcode = 0;
		}
		else
		{
			// both keeps
			opOut.opcode = '=';
			opOut.attribs = Changeset.followAttributes(op1.attribs, op2.attribs, pool);
			if (op1.chars <= op2.chars)
			{
				opOut.chars = op1.chars;
				opOut.lines = op1.lines;
				op2.chars -= op1.chars;
				op2.lines -= op1.lines;
				op1.opcode = 0;
				if (op2.chars == 0)
				{
					op2.opcode = 0;
				}
			}
			else
			{
				opOut.chars = op2.chars;
				opOut.lines = op2.lines;
				op1.chars -= op2.chars;
				op1.lines -= op2.lines;
				op2.opcode = 0;
			}
		}
		switch (opOut.opcode)
		{
		case '=':
			setOldPos(getOldPos() + opOut.chars);
			setNewLen(getNewLen() + opOut.chars);
			break;
		case '-':
			setOldPos(getOldPos() + opOut.chars);
			break;
		case '+':
			setNewLen(getNewLen() + opOut.chars);
			break;
		}
	}

	public int getOldPos() {
		return oldPos;
	}

	public void setOldPos(int oldPos) {
		this.oldPos = oldPos;
	}

	public int getNewLen() {
		return newLen;
	}

	public void setNewLen(int newLen) {
		this.newLen = newLen;
	}
};


