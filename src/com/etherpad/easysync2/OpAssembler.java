package com.etherpad.easysync2;

public class OpAssembler {
	StringBuffer pieces;

	public OpAssembler() {
		pieces = new StringBuffer();
	}
	
    public void append(Operation op)
    {
      if (op.opcode == 0)
    	  return;
      pieces.append(op.attribs);
      if (op.lines > 0)
      {
        pieces.append('|');
        pieces.append(Changeset.numToString(op.lines));
      }
      pieces.append(op.opcode);
      pieces.append(Changeset.numToString(op.chars));
    }

    public String toString()
    {
    	return pieces.toString();
    }

    public void clear()
    {
      pieces = new StringBuffer();
    }
}
