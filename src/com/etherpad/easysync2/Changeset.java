package com.etherpad.easysync2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.etherpad.easysync2.Operation.OperationCombiner;
import com.etherpad.easysync2.OperationSerializer.OpIterator;

public class Changeset {

	static Pattern headerRegex = Pattern.compile("Z:([0-9a-z]+)([><])([0-9a-z]+)|");

	/**
	 * length of the text before changeset can be applied
	 */ 
	int oldLen;

	/**
	 * the length of the text after changeset is applied
	 */ 	
	int newLen;

	/**
	 * string encoded changeset operations
	 */
	String ops;

	/**
	 * charbank of the changeset
	 */
	String charBank;


	public Iterable<Operation> getOpIterator() {
		return new OperationSerializer.OpIterator(this.ops);
	}

	public Changeset(int oldLen, int newLen, String ops, String charBank) {
		this.oldLen = oldLen;
		this.newLen = newLen;
		this.ops = ops;
		this.charBank = charBank;
	}

	@Override
	public String toString() {
		return "["+oldLen+"->"+newLen+"]("+ops+")("+charBank+")"; 
	}

	/**
	 * Parses a number from string base 36
	 * @param str {string} string of the number in base 36
	 * @returns number
	 */
	public static int parseNum(String str) {
		return Integer.parseInt(str, 36);
	}

	/**
	 * Writes a number in base 36 and puts it in a string
	 * @param num {int} number
	 * @returns {string} string
	 */
	public static String numToString(int num) {
		return Integer.toString(num, 36);
	}

	/**
	 * This method is called whenever there is an error in the sync process
	 * @param msg {string} Just some message
	 */
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
	/**
	 * Packs Changeset object into a string 
	 * @params oldLen {int} Old length of the Changeset
	 * @params newLen {int] New length of the Changeset
	 * @params opsStr {string} String encoding of the changes to be made
	 * @params bank {string} Charbank of the Changeset
	 * @returns {Changeset} a Changeset class
	 */
	public static String pack(int oldLen, int newLen, String opsStr, String bank) {
		int lenDiff = newLen - oldLen;
		String lenDiffStr = (lenDiff >= 0 ? '>' + Changeset.numToString(lenDiff) : '<' + Changeset.numToString(-lenDiff));

		StringBuffer a = new StringBuffer();
		a.append("Z:");
		a.append(Changeset.numToString(oldLen));
		a.append(lenDiffStr);
		a.append(opsStr);
		a.append("$");
		a.append(bank);
		return a.toString();
	};

	public static Changeset unpack(String cs) {
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
		return new Changeset(oldLen, newLen, cs.substring(opsStart, opsEnd), cs.substring(opsEnd + 1));
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
					result.append(Changeset.numToString(pool.putAttrib(pair)));
				}
			}
			return result.toString();
		}
		return "";
	};

	static Pattern opRegex = Pattern.compile("\\*([0-9a-z]+)");

	public static String followAttributes(String att1, String att2, AttribPool pool) {
		// The merge of two sets of attribute changes to the same text
		// takes the lexically-earlier value if there are two values
		// for the same key.  Otherwise, all key/value changes from
		// both attribute sets are taken.  This operation is the "follow",
		// so a set of changes is produced that can be applied to att1
		// to produce the merged set.
		if ((att2.length() == 0) || (pool == null)) return "";
		if (att1.length()==0) return att2;
		ArrayList<String> atts = new ArrayList<String>();
		Matcher m;
		int start = 0; 
		int len = att2.length();
		while ((m=opRegex.matcher(att2.subSequence(start, len))).matches()) {
			atts.add(pool.getAttrib(Changeset.parseNum(m.group())));			  
			start = m.end()+1;
		}

		start = 0;
		len = att1.length();
		while ((m=opRegex.matcher(att1.subSequence(start, len))).matches()) {
			Attribute pair1 = Attribute.parse(pool.getAttrib(Changeset.parseNum(m.group())));
			for (int i = 0; i < atts.size(); i++) {
				Attribute pair2 = Attribute.parse(atts.get(i));
				if (pair1.key == pair2.key) {
					if (pair1.value.compareTo(pair2.value) <= 0) {
						// winner of merge is pair1, delete this attribute
						atts.remove(i);
					}
					break;
				}
			}
			start = m.end()+1;
		}		  
		// we've only removed attributes, so they're already sorted
		StringAssembler buf = new StringAssembler();
		for (int i = 0; i < atts.size(); i++) {
			buf.append("*");
			buf.append(Changeset.numToString(pool.putAttrib(Attribute.parse(atts.get(i)))));
		}
		return buf.toString();
	};

	public static String applyZip(String in1, int idx1, String in2, int idx2, OperationCombiner func)
	{
		OperationSerializer.OpIterator iter1 = new OpIterator(in1, idx1);
		OperationSerializer.OpIterator iter2 = new OpIterator(in2, idx2);
		SmartOpAssembler assem = new SmartOpAssembler();

		Operation op1 = new Operation();
		Operation op2 = new Operation();
		Operation opOut = new Operation();

		while (op1.opcode!=0 || iter1.hasNext() || op2.opcode!=0 || iter2.hasNext())
		{
			if ((op1.opcode==0) && iter1.hasNext()) op1 = iter1.next();
			if ((op2.opcode==0) && iter2.hasNext()) op2 = iter2.next();
			func.combine(op1, op2, opOut);
			
			if (opOut.opcode != 0)
			{
				//print(opOut.toSource());
				System.out.println("opout = "+opOut);
				assem.append(opOut);
				opOut.opcode = 0;
			}
		}
		assem.endDocument();
		return assem.toString();
	};

	public static String follow(Changeset cs1, Changeset cs2, boolean reverseInsertOrder, AttribPool pool) {
		Changeset unpacked1 = cs1;
		Changeset unpacked2 = cs2;
		int len1 = unpacked1.oldLen;
		int len2 = unpacked2.oldLen;
		assert(len1 == len2);

		int oldLen = unpacked1.newLen;
		int oldPos = 0;
		int newLen = 0;

		FollowOperationCombiner fw = new FollowOperationCombiner(reverseInsertOrder, pool, unpacked1, unpacked2, oldPos, newLen);
		String newOps = Changeset.applyZip(unpacked1.ops, 0, unpacked2.ops, 0, fw); 
		oldPos = fw.getOldPos();
		newLen = fw.getNewLen();

		return Changeset.pack(oldLen, newLen + oldLen - oldPos, newOps, unpacked2.charBank);
	}

	/**
	 * Composes two attribute strings (see below) into one.
	 * @param att1 {string} first attribute string
	 * @param att2 {string} second attribue string
	 * @param resultIsMutaton {boolean} 
	 * @param pool {AttribPool} attribute pool 
	 */
	public static String composeAttributes(String att1, String att2, boolean resultIsMutation, AttribPool pool) {
		// att1 and att2 are strings like "*3*f*1c", asMutation is a boolean.
		// Sometimes attribute (key,value) pairs are treated as attribute presence
		// information, while other times they are treated as operations that
		// mutate a set of attributes, and this affects whether an empty value
		// is a deletion or a change.
		// Examples, of the form (att1Items, att2Items, resultIsMutation) -> result
		// ([], [(bold, )], true) -> [(bold, )]
		// ([], [(bold, )], false) -> []
		// ([], [(bold, true)], true) -> [(bold, true)]
		// ([], [(bold, true)], false) -> [(bold, true)]
		// ([(bold, true)], [(bold, )], true) -> [(bold, )]
		// ([(bold, true)], [(bold, )], false) -> []
		// pool can be null if att2 has no attributes.
		if ((att1.length()==0) && resultIsMutation) {
			// In the case of a mutation (i.e. composing two exportss),
			// an att2 composed with an empy att1 is just att2.  If att1
			// is part of an attribution string, then att2 may remove
			// attributes that are already gone, so don't do this optimization.
			return att2;
		}
		if (att2.length()==0) return att1;

		if ((att2.length() == 0) || (pool == null)) return "";
		if (att1.length()==0) return att2;
		ArrayList<String> atts = new ArrayList<String>();
		Matcher m;

		int start = 0; 
		int len = att2.length()-1;
		while ((m=opRegex.matcher(att2.subSequence(start, len))).matches()) {
			atts.add(pool.getAttrib(Changeset.parseNum(m.group(1))));			  
			start = m.end()+1;
		}

		start = 0;
		len = att1.length()-1;
		while ((m=opRegex.matcher(att1.subSequence(start, len))).matches()) {
			Attribute pair = Attribute.parse(pool.getAttrib(parseNum(m.group(1))));
			Boolean found = false;
			for (int i = 0; i < atts.size(); i++) {
				Attribute oldPair = Attribute.parse(atts.get(i));
				if (oldPair.key == pair.key) {
					if (pair.value.length()>0 || resultIsMutation) {
						oldPair.value = pair.value;
					} else {
						atts.remove(i);
					}
					found = true;
					break;
				}
			}
			if ((!found) && (pair.value.length()>0 || resultIsMutation)) {
				atts.add(pair.toString());
			}
			start = m.end()+1;
		}		  

		String[] _atts = new String[atts.size()];
		_atts = atts.toArray(_atts);

		Arrays.sort(_atts);
		
		StringAssembler buf = Changeset.stringAssembler();
		for (int i = 0; i < _atts.length; i++) {
			buf.append("*");
			buf.append(numToString(pool.putAttrib(Attribute.parse(_atts[i]))));
		}
		//print(att1+" / "+att2+" / "+buf.toString());
		return buf.toString();
	};

	/**
	 * Applies a Changeset to the attribs string of a AText.
	 * @param cs {string} Changeset
	 * @param astr {string} the attribs string of a AText
	 * @param pool {AttribsPool} the attibutes pool
	 */
	public static String applyToAttribution(Changeset cs, String astr, AttribPool pool) {
		Changeset unpacked = cs;
		SlicerOperationCombiner soc = new SlicerOperationCombiner(pool);

		return applyZip(astr, 0, unpacked.ops, 0, soc); 
	};	

	public static OpIterator opIterator(String opsStr) {
		return new OpIterator(opsStr);
	}

	public static SmartOpAssembler smartOpAssembler() {
		return new SmartOpAssembler();
	}
	
	public static StringAssembler stringAssembler() {
		return new StringAssembler();
	}
}
