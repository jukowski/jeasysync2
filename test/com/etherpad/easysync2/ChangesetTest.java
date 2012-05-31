package com.etherpad.easysync2;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import com.etherpad.easysync2.OperationSerializer.OpIterator;

public class ChangesetTest {

	Random random;

	void print(String str) {
		System.out.println(str);
	}

	void _assert(Boolean code, String optMsg) {
		if (!code) throw new Error("FALSE: " + optMsg);
	}

	String throughIterator(String opsStr) {
		OpIterator iter = new OpIterator(opsStr);
		OpAssembler assem = new OpAssembler();
		while (iter.hasNext()) {
			assem.append(iter.next());
		}
		return assem.toString();
	}

	String throughSmartAssembler(String opsStr) {
		OpIterator iter = Changeset.opIterator(opsStr);
		SmartOpAssembler assem = Changeset.smartOpAssembler();
		while (iter.hasNext()) {
			assem.append(iter.next());
		}
		assem.endDocument();
		return assem.toString();
	}

	AttribPool poolOrArray(Object attribs) {
		if (attribs instanceof AttribPool) {
			return (AttribPool) attribs; // it's already an attrib pool
		} else {
			// assume it's an array of attrib strings to be split and added
			String[] attribStr = (String[]) attribs;
			AttribPool p = new AttribPool();
			for (String kv : attribStr) {
				p.putAttrib(Attribute.parse(kv));				
			}
			return p;
		}
	}

	void runApplyToAttributionTest(String testId, Object attribs, String _cs, String inAttr, String outCorrect) {
		print("> applyToAttribution#" + testId);
		AttribPool p = poolOrArray(attribs);
		Changeset cs = Changeset.unpack(_cs);
		String result = Changeset.applyToAttribution(cs, inAttr, p);
		Assert.assertEquals(outCorrect, result);
	}

	String randomInlineString(int len, Random rand) {
		StringAssembler assem = Changeset.stringAssembler();
		for (int i = 0; i < len; i++) {
			assem.append(String.valueOf((rand.nextInt(26) + 97)));
		}
		return assem.toString();
	}

	String randomMultiline(int approxMaxLines, int approxMaxCols, Random rand) {
		int numParts = rand.nextInt(approxMaxLines * 2) + 1;
		StringAssembler txt = Changeset.stringAssembler();
		txt.append(rand.nextInt(2)!=0 ? "\n" : "");
		for (int i = 0; i < numParts; i++) {
			if ((i % 2) == 0) {
				if (rand.nextInt(10)!=0) {
					txt.append(randomInlineString(rand.nextInt(approxMaxCols) + 1, rand));
				} else {
					txt.append("\n");
				}
			} else {
				txt.append("\n");
			}
		}
		return txt.toString();
	}

	enum StrOpType {insert, delete, skip}

	class StrOp {
		StrOpType type;
		String text;
		int len;
	};

	StrOp randomStringOperation(int numCharsLeft, Random rand) {
		StrOp result = new StrOp();
		switch (rand.nextInt(9)) {
		case 0:
		{
			// insert char
			result.type = StrOpType.insert;
			result.text = randomInlineString(1, rand);
			break;
		}
		case 1:
		{
			// delete char
			result.type = StrOpType.delete;
			result.len = 1;
			break;
		}
		case 2:
		{
			// skip char
			result.type = StrOpType.skip;
			result.len = 1;
			break;
		}
		case 3:
		{
			// insert small
			result.type = StrOpType.insert;
			result.text = randomInlineString(1, rand);
			break;
		}
		case 4:
		{
			// delete small
			result.type = StrOpType.delete;
			result.len = rand.nextInt(4) + 1;
			break;
		}
		case 5:
		{
			// skip small
			result.type = StrOpType.skip;
			result.len = rand.nextInt(4) + 1;
			break;
		}
		case 6:
		{
			// insert multiline;
			result.type = StrOpType.insert;
			result.text = randomMultiline(5, 20, rand);
			break;
		}
		case 7:
		{
			// delete multiline
			result.type = StrOpType.delete;
			result.len = (int)Math.round(numCharsLeft * rand.nextDouble() * rand.nextDouble());
			break;
		}
		case 8:
		{
			// skip multiline
			result.type = StrOpType.skip;
			result.len = (int)Math.round(numCharsLeft * rand.nextDouble() * rand.nextDouble());
			break;
		}
		case 9:
		{
			// delete to end
			result.type = StrOpType.delete;
			result.len = numCharsLeft;
			break;
		}
		case 10:
		{
			// skip to end
			result.type = StrOpType.skip;
			result.len = numCharsLeft;
			break;
		}
		}
		int maxOrig = numCharsLeft - 1;
		if (result.type == StrOpType.delete) {
			result.len = Math.min(result.len, maxOrig);
		} else if (result.type == StrOpType.skip) {
			result.len = Math.min(result.len, maxOrig);
		}
		return result;
	}

	String randomTwoPropAttribs(char opcode, Random rand) {
		// assumes attrib pool like ['apple,','apple,true','banana,','banana,true']
		if (opcode == '-' || rand.nextInt(3) != 0) {
			return "";
		} else if (rand.nextInt(3) != 0) {
			if (opcode == '+' || rand.nextInt(2) != 0) {
				return '*' + Changeset.numToString(rand.nextInt(2) * 2 + 1);
			} else {
				return '*' + Changeset.numToString(rand.nextInt(2) * 2);
			}
		} else {
			if (opcode == '+' || rand.nextInt(4) == 0) {
				return "*1*3";
			} else {
				return (new String[]{"*0*2", "*0*3", "*1*2"})[rand.nextInt(3)];
			}
		}
	}

	void appendMultilineOp(char opcode, String txt, Operation nextOp, boolean withAttribs, Random rand, SmartOpAssembler opAssem) {
		nextOp.opcode = opcode;
		if (withAttribs) {
			nextOp.attribs = randomTwoPropAttribs(opcode, rand);
		}
		int lines = 0;
		for (int start = 0, pos = 0; (pos=txt.indexOf('\n', start))!=-1; start=pos+1) {
			lines ++;
		}
		nextOp.lines = lines;
		nextOp.chars = txt.length();
		opAssem.append(nextOp);		
	}

	String doOp(Operation nextOp, String textLeft, StringAssembler charBank, StringAssembler outTextAssem, Random rand, boolean withAttribs, SmartOpAssembler opAssem) {
		StrOp o = randomStringOperation(textLeft.length(), rand);
		if (o.type == StrOpType.insert) {
			String txt = o.text;
			charBank.append(txt);
			outTextAssem.append(txt);
			appendMultilineOp('+', txt, nextOp, withAttribs, rand, opAssem);
		} else if (o.type == StrOpType.skip) {
			String txt = textLeft.substring(0, o.len);
			textLeft = textLeft.substring(o.len);
			outTextAssem.append(txt);
			appendMultilineOp('=', txt, nextOp, withAttribs, rand, opAssem);
		} else if (o.type == StrOpType.delete) {
			String txt = textLeft.substring(0, o.len);
			textLeft = textLeft.substring(o.len);
			appendMultilineOp('-', txt, nextOp, withAttribs, rand, opAssem);
		}
		return textLeft;
	}

	class ChangesetTextPair {
		String cs;
		String outText;

		public ChangesetTextPair(String cs, String outText) {
			this.cs = cs;
			this.outText = outText;
		}
	}

	ChangesetTextPair randomTestChangeset(String origText, Random rand, boolean withAttribs) {
		StringAssembler charBank = Changeset.stringAssembler();
		String textLeft = origText; // always keep final newline
		StringAssembler outTextAssem = Changeset.stringAssembler();
		SmartOpAssembler opAssem = Changeset.smartOpAssembler();
		int oldLen = origText.length();

		Operation nextOp = new Operation();

		while (textLeft.length() > 1) textLeft = doOp(nextOp, textLeft, charBank, outTextAssem, rand, withAttribs, opAssem);
		for (int i = 0; i < 5; i++) textLeft = doOp(nextOp, textLeft, charBank, outTextAssem, rand, withAttribs, opAssem); // do some more (only insertions will happen)
		String outText = outTextAssem.toString() + '\n';
		opAssem.endDocument();
		String cs = Changeset.pack(oldLen, outText.length(), opAssem.toString(), charBank.toString());
		Changeset.checkRep(cs);
		return new ChangesetTextPair(cs, outText);
	}

	public void testCompose(int seed) {
		Random rand = new Random(seed);
		print("> testCompose#");

		AttribPool p = new AttribPool();

		String startText = randomMultiline(10, 20, rand) + '\n';

		ChangesetTextPair x1 = randomTestChangeset(startText, rand, false);
		String change1 = x1.cs;
		String text1 = x1.outText;

		ChangesetTextPair x2 = randomTestChangeset(text1, rand, false);
		String change2 = x2.cs;
		String text2 = x2.outText;

		ChangesetTextPair x3 = randomTestChangeset(text2, rand, false);
		String change3 = x3.cs;
		String text3 = x3.outText;

		//print(literal(Changeset.toBaseTen(startText)));
		//print(literal(Changeset.toBaseTen(change1)));
		//print(literal(Changeset.toBaseTen(change2)));
		String change12 = Changeset.checkRep(Changeset.compose(change1, change2, p));
		String change23 = Changeset.checkRep(Changeset.compose(change2, change3, p));
		String change123 = Changeset.checkRep(Changeset.compose(change12, change3, p));
		String change123a = Changeset.checkRep(Changeset.compose(change1, change23, p));
		Assert.assertEquals(change123, change123a);

		Assert.assertEquals(text2, Changeset.applyToText(change12, startText));
		Assert.assertEquals(text3, Changeset.applyToText(change23, text1));
		Assert.assertEquals(text3, Changeset.applyToText(change123, startText));
	}

	@Test
	public void test1() {
		print("> throughIterator");
		String x = "-c*3*4+6|3=az*asdf0*1*2*3+1=1-1+1*0+1=1-1+1|c=c-1";
		Assert.assertEquals(throughIterator(x), x);
	}

	@Test
	public void test2() {
		print("> throughSmartAssembler");
		String x = "-c*3*4+6|3=az*asdf0*1*2*3+1=1-1+1*0+1=1-1+1|c=c-1";

		Assert.assertEquals(throughSmartAssembler(x), x);
	}

	@Test
	public void test3() {

		// turn c<b>a</b>ctus\n into a<b>c</b>tusabcd\n
		runApplyToAttributionTest("1", new String[]{"bold,", "bold,true"}, "Z:7>3-1*0=1*1=1=3+4$abcd", "+1*1+1|1+5", "+1*1+1|1+8");

		// turn "david\ngreenspan\n" into "<b>david\ngreen</b>\n"
		runApplyToAttributionTest("2", new String[]{"bold,", "bold,true"}, "Z:g<4*1|1=6*1=5-4$", "|2+g", "*1|1+6*1+5|1+1");
	}


	@Test
	public void test4() {
		for (int i = 26; i < 1000; i++) 
			testCompose(i);
	} 

	@Test
	public void test6() {
		print("> simpleComposeAttributesTest");
		AttribPool p = new AttribPool();
		p.putAttrib(new Attribute("bold", ""));
		p.putAttrib(new Attribute("bold", "true"));
		String cs1 = Changeset.checkRep("Z:2>1*1+1*1=1$x");
		String cs2 = Changeset.checkRep("Z:3>0*0|1=3$");
		String cs12 = Changeset.checkRep(Changeset.compose(cs1, cs2, p));
		Assert.assertEquals("Z:2>1+1*0|1=2$x", cs12);

	}

	@Test
	public void test5() {
		print("> simpleComposeAttributesTest");
		AttribPool p = new AttribPool();
		p.putAttrib(new Attribute("bold", ""));
		p.putAttrib(new Attribute("bold", "true"));
		String result = Changeset.composeAttributes("*1","*0", true, p);
		Assert.assertEquals("*0", result);
		result = Changeset.composeAttributes("*1","*0", false, p);
		Assert.assertEquals("", result);
	}


	@Test
	public void test8() {
		SmartOpAssembler assem = new SmartOpAssembler();
		Operation o1 = new Operation('-'); o1.lines = 1; o1.chars = 1;
		assem.append(o1);
		Operation o2 = new Operation('-'); o2.lines = 0; o2.chars = 3;
		assem.append(o2);
		assem.endDocument();
		Assert.assertEquals("|1-4", assem.toString());
	} 

}

