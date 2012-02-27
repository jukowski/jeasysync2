package com.etherpad.easysync2;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperationSerializer {
	static EasySync2Support _opt = null;
	static Pattern opRegex = Pattern.compile("((?:\\*[0-9a-z]+)*)(?:\\|([0-9a-z]+))?([-+=])([0-9a-z]+)|\\?|");

	public static Iterable<Operation> Iterator(String ops) {
		return new OperationSerializer.OpIterator(ops);
	}
	
	static class OpIterator implements Iterator<Operation>, Iterable<Operation> {

		public OpIterator(CharSequence ops, int optStartIndex) { initIterator(ops, optStartIndex); }

		public OpIterator(CharSequence ops) { initIterator(ops, 0); }

		int curIndex;
		int prevIndex;
		CharSequence ops;

		/**
		 * creates an iterator which decodes string changeset operations
		 * @param ops {string} String encoding of the change operations to be performed 
		 * @param startIndex {int} from where in the string should the iterator start 
		 */
		void initIterator(CharSequence ops, int startIndex) {
			this.ops = ops;
			curIndex = startIndex;
			prevIndex = curIndex;
		}

		protected Operation nextRegexMatch() {
			prevIndex = curIndex;
			Operation result;

			if (_opt != null) {
				result = _opt.nextOpInString(ops, curIndex);
				curIndex = result.lastIndex();
			}
			else {
				Matcher regResult = opRegex.matcher(ops.subSequence(curIndex, ops.length()));
				result = new Operation(regResult);
				curIndex += result.lastIndex();
			}
			if (result != null)
			{
				if (result.opcode() == '?')
				{
					//TODO: HANDLE THIS SOMEHOW...
					//throw Exception("Hit error opcode in op stream");
				}
			}

			return result;
		}


		@Override
		public boolean hasNext() {
			return curIndex<ops.length();
		}

		@Override
		public Operation next() {
			return nextRegexMatch();
		}

		@Override
		public void remove() {
			// TODO: No remove actually needed but 
		}

		@Override
		public Iterator<Operation> iterator() {
			return this;
		}		
	}

}
