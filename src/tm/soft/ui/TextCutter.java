package tm.soft.ui;

import java.util.Collections;
import java.util.Stack;
import java.util.StringTokenizer;

import android.graphics.Paint;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Pair;

public class TextCutter {

	private TextPaint mTextPaint = new TextPaint();
	private boolean mHasWordParting;

	public void setFontSize(float fontSize) {
		this.mTextPaint.setTextSize(fontSize);
	}

	public boolean isHasWordParting() {
		return mHasWordParting;
	}

	/**
	 * @param text
	 * @return
	 */
	public Pair<Boolean, Stack<TextLine>> convert(String text, int hSpace, int maxWidth, int maxHeight) {

		mHasWordParting = false;
		Stack<TextLine> stack = new Stack<TextLine>();

		Stack<String> words = getWords(text);
		if (words.size() == 0) {
			return new Pair<Boolean, Stack<TextLine>>(true, stack);
		}
		String word = words.pop();

		TextLine item = new TextLine();
		stack.add(item);
		Boolean res = nood(word, words, mTextPaint, hSpace, maxWidth, maxHeight, maxWidth, maxHeight, item, stack);

		return new Pair<Boolean, Stack<TextLine>>(res, stack);
	}

	/**
	 * @param text
	 * @return
	 */
	private Stack<String> getWords(String text) {
		StringTokenizer textTokenizer = new StringTokenizer(text, " ");
		Stack<String> words = new Stack<String>();
		while (textTokenizer.hasMoreElements()) {
			words.add(textTokenizer.nextToken());
		}
		Collections.reverse(words);
		return words;
	}

	/**
	 * @param word
	 * @param words
	 * @param p
	 * @param hSpace
	 * @param freeWidth
	 * @param freeHeight
	 * @param maxWidth
	 * @param maxHeight
	 * @param cur
	 * @param stack
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean nood(String word, Stack<String> words, TextPaint p, int hSpace, int freeWidth, int freeHeight,
			int maxWidth, int maxHeight, TextLine cur, Stack<TextLine> stack) {

		if (freeHeight < p.getTextSize()) {
			return false;
		}

		if (!toLeft(word, (Stack<String>) words.clone(), p, hSpace, freeWidth, freeHeight, maxWidth, maxHeight, cur,
				stack)) {
			TextLine item = new TextLine();
			stack.add(item);
			freeHeight -= (p.getTextSize() + hSpace);
			if (!toLeft(word, (Stack<String>) words.clone(), p, hSpace, maxWidth, freeHeight, maxWidth, maxHeight,
					item, stack)) {
				stack.remove(item);
				mHasWordParting = true;
				freeHeight += (p.getTextSize() + hSpace);
				return toRight(word, (Stack<String>) words.clone(), p, hSpace, freeWidth, freeHeight, maxWidth,
						maxHeight, cur, stack);
			}
		}
		return true;
	}

	/**
	 * word has being written to 2 lines
	 * 
	 * @param word
	 * @param words
	 * @param p
	 * @param hSpace
	 * @param freeWidth
	 * @param freeHeight
	 * @param maxWidth
	 * @param maxHeight
	 * @param cur
	 * @param stack
	 * @return
	 */
	private boolean toRight(String word, Stack<String> words, TextPaint p, int hSpace, int freeWidth, int freeHeight,
			int maxWidth, int maxHeight, TextLine cur, Stack<TextLine> stack) {
		if (word.length() == 0) {
			return true;
		}
		float oneSymbol = p.measureText(word.substring(0, 1));
		if (freeWidth < oneSymbol) {
			return false;
		}
		Pair<String, String> pair = subString(word, p, freeWidth, freeHeight);
		cur.addString(pair.first, p);

		TextLine newCur = new TextLine();
		stack.add(newCur);
		boolean nood = nood(pair.second, words, p, hSpace, maxWidth, freeHeight, maxWidth, maxHeight, newCur, stack);
		if (!nood) {
			stack.remove(newCur);
			cur.line.replace(pair.first, "");
			cur.width = (int) p.measureText(cur.line);
		}
		return nood;
	}

	/**
	 * word has being added wholly
	 * 
	 * @param word
	 * @param words
	 * @param p
	 * @param hSpace
	 * @param freeWidth
	 * @param freeHeight
	 * @param maxWidth
	 * @param maxHeight
	 * @param cur
	 * @param stack
	 * @return
	 */
	public boolean toLeft(String word, Stack<String> words, TextPaint p, int hSpace, int freeWidth, int freeHeight,
			int maxWidth, int maxHeight, TextLine cur, Stack<TextLine> stack) {
		int wordWidth = (int) p.measureText(word);
		if (wordWidth > freeWidth) {
			return false;
		}

		cur.addString(word + " ", p);

		if (words.size() > 0) {
			freeWidth -= cur.width;
			boolean nood = nood(words.pop(), words, p, hSpace, freeWidth, freeHeight, maxWidth, maxHeight, cur, stack);
			if (!nood) {
				cur.line.replace(word + " ", "");
			}
			return nood;
		}
		return true;
	}

	/**
	 * @param word
	 * @param p
	 * @param freeWidth
	 * @param freeHeight
	 * @return
	 */
	private Pair<String, String> subString(String word, Paint p, int freeWidth, int freeHeight) {
		// binary search
		int firstPos = 0;
		int lastPos = word.length();

		int bestPos = 0;
		boolean found = false;
		while (!found && lastPos > firstPos) {
			int middlePos = (firstPos + lastPos) / 2;
			String forTest = word.substring(0, middlePos);
			float wordWidth = p.measureText(forTest);
			if (wordWidth < freeWidth) {
				firstPos = middlePos + 1;
				bestPos = middlePos;
			} else if (wordWidth > freeWidth) {
				lastPos = middlePos - 1;
				bestPos = firstPos;
			} else {
				bestPos = middlePos;
				found = true;
			}
		}

		if (bestPos == 0)
			bestPos = 1;
		return new Pair<String, String>(word.substring(0, bestPos), word.substring(bestPos));
	}

	/**
	 * @author TMalygin
	 */
	public static class TextLine {
		public String line = "";
		public int width;
		public int heigth;

		public void addString(String text, TextPaint p) {
			line += text;

			width = (int) p.measureText(line);
			StaticLayout layout = new StaticLayout(line, p, width, Alignment.ALIGN_NORMAL, 0.0f, 1.0f, true);
			heigth = layout.getHeight();
		}

		@Override
		public String toString() {
			return line;
		}
	}
}
