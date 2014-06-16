package tm.soft.ui;

import java.util.Stack;

import tm.soft.ui.TextCutter.TextLine;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.TextView;

import com.example.wc.R;

/**
 * @author tmalygin
 * @version 1.0
 */
public class AutoResizeTextView extends TextView {

	private TextArea mText;

	/**
	 * @param context
	 * @param attrs
	 */
	public AutoResizeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/**
	 * @param c
	 * @param attrs
	 */
	private void init(Context c) {
		mText = new TextArea(c.getResources());
		mText.setText(c, "check");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);
		mText.drawText(canvas);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		if (mText != null) {
			mText.checkWidthOfText(text);
			// setTextSize(TypedValue.COMPLEX_UNIT_PX,
			// mText.textPaint.getTextSize());
			setWidth(mText.mTextBitmap.getWidth());
			setHeight(mText.mTextBitmap.getHeight());
		}
		super.onTextChanged(text, start, lengthBefore, lengthAfter);

	}

	/**
	 * @author tmalygin
	 */
	private static class TextArea {

		final int maxWidth, maxHeight;
		final float maxSize, minSize;
		final float hspacing;

		Bitmap mTextBitmap;

		final Paint textPaint = new Paint();
		private CharSequence mLastText;

		/**
		 * @param res
		 */
		public TextArea(Resources res) {
			this.maxSize = res.getDimensionPixelSize(R.dimen.text_size_max);
			this.minSize = res.getDimensionPixelSize(R.dimen.text_size_min);
			this.maxWidth = res.getDimensionPixelSize(R.dimen.textview_width);
			this.hspacing = res.getDimensionPixelOffset(R.dimen.spacing_beetwen_bottom_and_top_words);

			this.maxHeight = res.getDimensionPixelSize(R.dimen.textview_height);
			this.textPaint.setTextSize(maxSize);

			checkAreaForText(2);
		}

		/**
		 * @param c
		 * @param text
		 */
		public void setText(Context c, String text) {
			if (text == null) {
				return;
			}
			checkWidthOfText(text);
		}

		/**
		 * @param afterSizeIncrement
		 *            - флаг. Был ли вызов этого метода после увеличения размера
		 *            шрифта
		 */
		private void checkWidthOfText(CharSequence text) {

			if (mLastText != null && mLastText.equals(text)) {
				return;
			}
			this.mLastText = text;
			TextCutter textCutter = new TextCutter();

			Stack<TextLine> result = null;

			int currentSize = (int) maxSize;
			int best = currentSize;
			boolean found = false;
			while (!found && currentSize > minSize) {
				textCutter.setFontSize(currentSize);
				Pair<Boolean, Stack<TextLine>> convert = textCutter.convert(text.toString(), (int) hspacing,
						(int) maxWidth, (int) maxHeight);

				if (convert.first) {
					if (!textCutter.isHasWordParting()) {
						found = true;
					}

					result = convert.second;
					best = currentSize;
				}
				currentSize--;
			}
			textPaint.setTextSize(best);
			drawText(result);
		}

		void checkAreaForText(int countLines) {
			float height = countLines * (hspacing + textPaint.getTextSize());

			if (mTextBitmap == null || mTextBitmap.getHeight() != height) {
				if (mTextBitmap != null)
					mTextBitmap.recycle();
				if (height <= 0)
					height = 1;
				mTextBitmap = Bitmap.createBitmap(maxWidth, (int) height, Config.ARGB_8888);
			}
		}

		/**
		 * @param stack
		 */
		private void drawText(Stack<TextLine> stack) {

			checkAreaForText(stack.size());
			Canvas c = new Canvas(mTextBitmap);
			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			float posY = mTextBitmap.getHeight() - hspacing;
			while (!stack.isEmpty()) {
				TextLine item = stack.pop();
				float x = (maxWidth - item.width) >> 1;
				c.drawText(item.line, x, posY, textPaint);
				posY -= (hspacing + textPaint.getTextSize());
			}

		}

		/**
		 * @param canvas
		 */
		public void drawText(Canvas canvas) {
			if (mTextBitmap.getHeight() > 1)
				canvas.drawBitmap(mTextBitmap, 0, 0, textPaint);
		}

	}
}
