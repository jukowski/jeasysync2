package com.etherpad.easysync2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class TextLinesMutator {
	List<String> lines;
	
	class Splice {
		public int startLine;
		public int lenght;
		public List<String> lines;
		
		public Splice() {
			startLine = 0;
			lenght = 0;
			lines = new ArrayList<String>();
		}
	}
	
	Splice curSplice;
	boolean inSplice;
	int curLine, curCol;
	
	public TextLinesMutator(ArrayList<String> lines) {
		this.lines = lines;
		curSplice = new Splice();
		inSplice = false;
		curLine = 0;
		curCol = 0;
	}
	
	public void lines_applySplice(Splice s) {
		int m = Math.min(s.lenght, s.lines.size());
		ListIterator<String> cLines = lines.listIterator(s.startLine);
		ListIterator<String> sLines = s.lines.listIterator(s.startLine);
		for (int i=0; i<m; ++i) {
			cLines.next();
			cLines.set(sLines.next());
		}
		if (s.lenght > s.lines.size()) {
			
		}
	}
	
}
