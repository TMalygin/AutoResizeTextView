package tm.soft;

import tm.soft.ui.AutoResizeTextView;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.wc.R;

public class MainActivity extends Activity implements TextWatcher {

	private AutoResizeTextView mGift;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		mGift = (AutoResizeTextView) findViewById(R.id.giftView1);

		((EditText) findViewById(R.id.editText1)).addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mGift.setText(s.toString());
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
}
