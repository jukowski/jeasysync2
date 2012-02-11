package com.etherpad.easysync2;

import java.util.regex.Matcher;

import org.json.JSONException;
import org.json.JSONObject;

public class Operation {

	protected char opcode;
	protected String attribs;
	protected int lastIndex, lines, chars;

	public Operation() {
		opcode = 0;
		attribs = "";
	}

	public Operation(char opcode) {
		this.opcode = opcode;
		attribs = "";
	}

	public static void copyOp(Operation op1, Operation op2) {
		op2.opcode = op1.opcode;
		op2.chars = op1.chars;
		op2.lines = op1.lines;
		op2.attribs = op1.attribs;
	}

	public static void clearOp(Operation op) {
		op.opcode = 0;
		op.chars = 0;
		op.lines = 0;
		op.attribs = "";
	}

	public static Operation cloneOp(Operation op)
	{
		Operation result = new Operation();
		result.opcode = op.opcode;
		result.chars = op.chars;
		result.lines = op.lines;
		result.attribs = op.attribs;
		return result;
	};

	public Operation(Matcher matcher) {
		if (matcher.find()) {
			attribs = matcher.group(1);
			lines = matcher.group(2) != null? ChangeSet.parseNum(matcher.group(2)) : 0; 
			opcode = matcher.group(3) != null? matcher.group(3).charAt(0) : '?';
			chars = matcher.group(4) != null? ChangeSet.parseNum(matcher.group(4)) : 0;
			lastIndex = matcher.group(0).length();
		}
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		try {
			result.put("opcode", Character.toString(opcode));
			result.put("chars", chars);
			result.put("lines", lines);
			result.put("attribs", attribs);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public String toString() {
		OpAssembler assem = new OpAssembler();
		assem.append(this);
		return assem.toString();
	}

	int lastIndex() {
		return this.lastIndex;
	}

	public char opcode() {
		return this.opcode;
	}

	public int lines() {
		return this.lines;
	}

	public int chars() {
		return this.chars;
	}

	public String attribs() {
		return this.attribs;
	}
}
