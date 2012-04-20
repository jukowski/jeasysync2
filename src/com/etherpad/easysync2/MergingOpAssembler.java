package com.etherpad.easysync2;

// This assembler can be used in production; it efficiently
// merges consecutive operations that are mergeable, ignores
// no-ops, and drops final pure "keeps".  It does not re-order
// operations.
public class MergingOpAssembler {
	OpAssembler assem;
	Operation bufOp;

	public MergingOpAssembler() {
		assem = new OpAssembler();
		bufOp = new Operation();
	}


	// If we get, for example, insertions [xxx\n,yyy], those don't merge,
	// but if we get [xxx\n,yyy,zzz\n], that merges to [xxx\nyyyzzz\n].
	// This variable stores the length of yyy and any other newline-less
	// ops immediately after it.
	int bufOpAdditionalCharsAfterNewline = 0;

	public void flush() {
		flush(false);
	}


	public void flush(boolean isEndDocument)
	{
		if (bufOp.opcode != 0)
		{
			if (isEndDocument && bufOp.opcode == '=' && bufOp.attribs.length()==0)
			{
				// final merged keep, leave it implicit
			}
			else
			{
				assem.append(bufOp);
				if (bufOpAdditionalCharsAfterNewline > 0)
				{
					bufOp.chars = bufOpAdditionalCharsAfterNewline;
					bufOp.lines = 0;
					assem.append(bufOp);
					bufOpAdditionalCharsAfterNewline = 0;
				}
			}
			bufOp.opcode = 0;
		}
	}

	void append(Operation op)
	{
		if (op.chars > 0)
		{
			if (bufOp.opcode == op.opcode && bufOp.attribs.equals(op.attribs))
			{
				if (op.lines >= 0)
				{
					// bufOp and additional chars are all mergeable into a multi-line op
					bufOp.chars += bufOpAdditionalCharsAfterNewline + op.chars;
					bufOp.lines += op.lines;
					bufOpAdditionalCharsAfterNewline = 0;
				}
				else if (bufOp.lines == 0)
				{
					// both bufOp and op are in-line
					bufOp.chars += op.chars;
				}
				else
				{
					// append in-line text to multi-line bufOp
					bufOpAdditionalCharsAfterNewline += op.chars;
				}
			}
			else
			{
				flush();
				Operation.copyOp(op, bufOp);
			}
		}
	}

	public void endDocument()
	{
		flush(true);
	}

	public String toString()
	{
		flush();
		return assem.toString();
	}

	public void clear()
	{
		assem.clear();
		Operation.clearOp(bufOp);
	}
};

