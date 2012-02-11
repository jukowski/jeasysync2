package com.etherpad.easysync2;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeSet {

	static Pattern headerRegex = Pattern.compile("Z:([0-9a-z]+)([><])([0-9a-z]+)|");

	int oldLen, newLen;
	String ops, charBank;


	public Iterable<Operation> getOpIterator() {
		return new OperationSerializer.OpIterator(this.ops);
	}

	public ChangeSet(int oldLen, int newLen, String ops, String charBank) {
		this.oldLen = oldLen;
		this.newLen = newLen;
		this.ops = ops;
		this.charBank = charBank;
	}

	@Override
	public String toString() {
		return "["+oldLen+"->"+newLen+"]("+ops+")("+charBank+")"; 
	}

	public static int parseNum(String str) {
		return Integer.parseInt(str, 36);
	}

	public static String numToString(int num) {
		return Integer.toString(num, 36);
	}

	protected static void error(String message) {
		System.out.println(message);
	}

	public String applyToText(String str) {
		assert(str.length() == oldLen); // , "mismatched apply: ", str.length, " / ", unpacked.oldLen

		StringIterator bankIter = new StringIterator(charBank);
		StringIterator strIter = new StringIterator(str);
		StringAssembler assem = new StringAssembler();

		for (Operation op : getOpIterator()) 
		{
			switch (op.opcode)
			{
			case '+':
				assem.append(bankIter.take(op.chars));
				break;
			case '-':
				strIter.skip(op.chars);
				break;
			case '=':
				assem.append(strIter.take(op.chars));
				break;
			}
		}
		assem.append(strIter.take(strIter.remaining()));
		return assem.toString();		  
	}

	public static ChangeSet unpack(String cs) {
		Matcher headerMatch = headerRegex.matcher(cs);
		if (!headerMatch.find())
		{
			error("Not a changeset: " + cs);
		}

		int oldLen = parseNum(headerMatch.group(1));
		int changeSign = (headerMatch.group(2).equals(">")) ? 1 : -1;
		int changeMag = parseNum(headerMatch.group(3));
		int newLen = oldLen + changeSign * changeMag;
		int opsStart = headerMatch.group(0).length();
		int opsEnd = cs.indexOf("$");

		if (opsEnd < 0) opsEnd = cs.length();
		return new ChangeSet(oldLen, newLen, cs.substring(opsStart, opsEnd), cs.substring(opsEnd + 1));
	}

	public static String makeAttribsString(char opcode, String attribs, AttribPool pool) {
		return attribs;
	}


	public static String makeAttribsString(char opcode, Collection<Attribute> attribs, AttribPool pool)
	{
		// makeAttribsString(opcode, '*3') or makeAttribsString(opcode, [['foo','bar']], myPool) work
		if (pool != null && attribs != null && attribs.size()>0)
		{
			// TODO: Not clear how this needs to be translated
			/*
			if (attribs.size() > 1)
			{
				attribs = attribs.slice();
				attribs.sort();
			}
			*/
			StringBuffer result = new StringBuffer();
			for (Attribute pair : attribs)
			{
				if (opcode == '=' || (opcode == '+' && pair.value.length()>0))
				{
					result.append('*');
					result.append(ChangeSet.numToString(pool.putAttrib(pair)));
				}
			}
			return result.toString();
		}
		return "";
	};
	
}
