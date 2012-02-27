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

	@Test
	public void test3() {

		// turn c<b>a</b>ctus\n into a<b>c</b>tusabcd\n
		runApplyToAttributionTest("1", new String[]{"bold,", "bold,true"}, "Z:7>3-1*0=1*1=1=3+4$abcd", "+1*1+1|1+5", "+1*1+1|1+8");

		// turn "david\ngreenspan\n" into "<b>david\ngreen</b>\n"
		runApplyToAttributionTest("2", new String[]{"bold,", "bold,true"}, "Z:g<4*1|1=6*1=5-4$", "|2+g", "*1|1+6*1+5|1+1");
	}
}
