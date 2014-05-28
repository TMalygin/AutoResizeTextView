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
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.wc.R;

/**
 * @author tmalygin
 * @version 1.0
 */
public class AutoResizeTextView extends TextView {

	private TextArea mText;
	private float mTouchedX;
	private float mTouchedY;

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

	/**
	 * @param text
	 */
	public void setText(String text) {
		mText.setText(getContext(), text);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mText.drawText(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchedX = event.getX();
			mTouchedY = event.getY();
			mText.isTouched(mTouchedX, mTouchedY);
			break;
		case MotionEvent.ACTION_MOVE:
			mText.move((int) (mTouchedX - event.getX()), (int) (event.getY() - mTouchedY));
			mTouchedY = event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			mText.touchUp((int) event.getX(), (int) event.getY());
		default:
			break;
		}
		return true;
	}

	/**
	 * @author tmalygin
	 */
	private static class TextArea {

		private static final int STATE_NORMAL = 0;
		private static final int STATE_MOVING = 1;

		int state = STATE_NORMAL;

		final int maxWidth, maxHeight;
		final float maxSize, minSize;
		final float hspacing;

		Bitmap mTextBitmap;
		int mTextX, mTextY;

		final Paint textPaint = new Paint();

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

			// init textArea
			mTextX = 0;
			mTextY = (int) (maxHeight - textPaint.getTextSize());

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
		private void checkWidthOfText(String text) {
			TextCutter textCutter = new TextCutter();

			Stack<TextLine> result = null;

			int currentSize = (int) maxSize;
			int best = currentSize;
			boolean found = false;
			while (!found && currentSize > minSize) {
				textCutter.setFontSize(currentSize);
				Pair<Boolean, Stack<TextLine>> convert = textCutter.convert(text, (int) hspacing, (int) maxWidth,
						(int) maxHeight);

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

				if (mTextY > (maxHeight - mTextBitmap.getHeight())) {
					mTextY = maxHeight - mTextBitmap.getHeight();
				}

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
				canvas.drawBitmap(mTextBitmap, mTextX, mTextY, textPaint);
		}

		/**
		 * @param x
		 * @param y
		 * @return
		 */
		boolean isTouched(float x, float y) {

			float x2 = x + mTextBitmap.getWidth();
			float y2 = y + mTextBitmap.getHeight();
			if (y < this.mTextY || y > y2 || x < this.mTextX || x > x2) {
				return false;
			}

			state = STATE_MOVING;
			return true;
		}

		/**
		 * @param diffX
		 * @param diffY
		 */
		void move(int diffX, int diffY) {
			switch (state) {
			case STATE_MOVING:
				int newPosX1 = mTextX + diffX;
				int newPosY1 = mTextY + diffY;

				if (diffY < 0 && newPosY1 > 0) {
					mTextY = newPosY1;
				} else if (diffY > 0 && (newPosY1 + mTextBitmap.getHeight()) < maxHeight) {
					mTextY = newPosY1;
				}

				if (diffX < 0 && newPosX1 > 0) {
					mTextX = newPosX1;
				} else if (diffX > 0 && (newPosX1 + mTextBitmap.getWidth()) < maxWidth) {
					mTextX = newPosX1;
				}

				break;
			}
		}

		void touchUp(int x, int y) {
			state = STATE_NORMAL;
		}
	}
}
