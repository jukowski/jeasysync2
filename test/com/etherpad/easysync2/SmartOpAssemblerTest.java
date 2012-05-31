package com.etherpad.easysync2;

import junit.framework.Assert;

import org.junit.Test;

public class SmartOpAssemblerTest {
	public String throughSmartAssembler(String opsStr) {
		SmartOpAssembler assem = new SmartOpAssembler();
		for (Operation op : OperationSerializer.Iterator(opsStr)) {
			assem.append(op);
		}
		assem.endDocument();
		return assem.toString();
	}

	public String throughIterator(String opsStr) {
	    OpAssembler assem = new OpAssembler();
	    for (Operation op : OperationSerializer.Iterator(opsStr)) {
	    	assem.append(op);
	    }
	    return assem.toString();
	}
	
	@Test
	public void test1() {
		String ops = "-c*3*4+6|3=az*asdf0*1*2*3+1=1-1+1*0+1=1-1+1|c=c-1";
		Assert.assertEquals(throughSmartAssembler(ops), ops);
	}

	@Test
	public void test2() {
		String ops = "-c*3*4+6|3=az*asdf0*1*2*3+1=1-1+1*0+1=1-1+1|c=c-1";
		Assert.assertEquals(throughIterator(ops), ops);
	}

}
