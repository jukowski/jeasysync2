package com.etherpad.easysync2;

/**
 * creates an object that allows you to append operations (type Op) and also
 * compresses them if possible
 */
public class SmartOpAssembler {
	MergingOpAssembler minusAssem, plusAssem, keepAssem;
	StringAssembler assem;
	char lastOpcode;
	int lengthChange;

	public SmartOpAssembler() {
		minusAssem = new MergingOpAssembler();
		plusAssem = new MergingOpAssembler();
		keepAssem = new MergingOpAssembler();
		assem = new StringAssembler();

		lastOpcode = 0;
		lengthChange = 0;
	}

	public void flushKeeps()
	{
		assem.append(keepAssem.toString());
		keepAssem.clear();
	}

	public void flushPlusMinus()
	{
		assem.append(minusAssem.toString());
		minusAssem.clear();
		assem.append(plusAssem.toString());
		plusAssem.clear();
	}

	public void append(Operation op)
	{
		if (op.opcode == 0) return;
		if (op.chars == 0) return;

		if (op.opcode == '-')
		{
			if (lastOpcode == '=')
			{
				flushKeeps();
			}
			minusAssem.append(op);
			lengthChange -= op.chars;
		}
		else if (op.opcode == '+')
		{
			if (lastOpcode == '=')
			{
				flushKeeps();
			}
			plusAssem.append(op);
			lengthChange += op.chars;
		}
		else if (op.opcode == '=')
		{
			if (lastOpcode != '=')
			{
				flushPlusMinus();
			}
			keepAssem.append(op);
		}
		lastOpcode = op.opcode;
	}

	public void appendOpWithText(char opcode, String text, String attribs, AttribPool pool)
	{
		Operation op = new Operation(opcode);
		op.attribs = Changeset.makeAttribsString(opcode, attribs, pool);
		int lastNewlinePos = text.lastIndexOf('\n');
		if (lastNewlinePos < 0)
		{
			op.chars = text.length();
			op.lines = 0;
			append(op);
		}
		else
		{
			op.chars = lastNewlinePos + 1;
			op.lines = text.split("\n").length;
			append(op);
			op.chars = text.length() - (lastNewlinePos + 1);
			op.lines = 0;
			append(op);
		}
	}

	@Override
	public String toString()
	{
		flushPlusMinus();
		flushKeeps();
		return assem.toString();
	}

	public void clear()
	{
		minusAssem.clear();
		plusAssem.clear();
		keepAssem.clear();
		assem.clear();
		lengthChange = 0;
	}

	public void endDocument()
	{
		keepAssem.endDocument();
	}

	public int getLengthChange()
	{
		return lengthChange;
	}
}
