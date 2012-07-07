package com.etherpad.easysync2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangesetUtils {

	static public int getNumLines(final String s) {
		int result = 0;
		for (int from = 0; (from = s.indexOf('\n', from)) != -1; result++, from++);
		return result;
	}
	
	public interface RegexReplacer {
		String replace(Matcher m);
	};
	
	static public String regexReplacer(Pattern p, String s, RegexReplacer r) {
		Matcher m = p.matcher(s);
		StringBuilder b = new StringBuilder();
		int start = 0;
		int len = s.length();
		while (start < len && m.find(start)) {
			b.append(s.subSequence(start, m.start()));
			String toReplceWith = r.replace(m);
			if (toReplceWith != null)
				b.append(toReplceWith);
			else
				b.append(m.group());
			start = m.end();
		}
		b.append(s.subSequence(start, s.length()));
		return b.toString();
	}
}
