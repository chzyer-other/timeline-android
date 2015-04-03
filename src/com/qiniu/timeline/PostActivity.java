package com.qiniu.timeline;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.PutExtra;
import org.json.JSONObject;

public class PostActivity extends Activity implements View.OnClickListener {
	Api api = new Api();
	Uri selectFile;
	Button btnSelect;
	Button btnSend;
	TextView hint;
	ProgressBar pb;
	EditText desc;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post);
		initWidget();
	}

	protected void initWidget() {
		btnSelect = (Button) findViewById(R.id.button1);
		btnSend = (Button) findViewById(R.id.button);
		btnSend.setOnClickListener(this);
		btnSelect.setOnClickListener(this);
		desc = (EditText) findViewById(R.id.editText);
		hint = (TextView) findViewById(R.id.textView);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setMax(100);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(btnSelect)) {
			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, 0);
			return;
		}

		if (view.equals(btnSend)) {
			if (selectFile == null) {
				Toast.makeText(this, "must select files before send!", Toast.LENGTH_SHORT).show();
				return;
			}
			btnSend.setEnabled(false);
			btnSelect.setEnabled(false);
			doUpload(selectFile);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		if (requestCode == 0) {
			selectFile = data.getData();
			hint.setText("select file: " + data.getData().toString());
			return;
		}
	}

	protected void doUpload(Uri uri) {
		PutExtra extra = new PutExtra();
		extra.params.put("x:desc", desc.getText().toString());
		api.put(this, uri, extra, new JSONObjectRet() {
			@Override
			public void onSuccess(JSONObject obj) {
				if (obj.optInt("code", 200) == 200) {
					Toast.makeText(PostActivity.this, "send successfully!", Toast.LENGTH_SHORT).show();
					setResult(RESULT_OK, new Intent(obj.optJSONObject("data").toString()));
					finish();
					return;
				}
				onFailure(new Exception(obj.optString("data")));
			}

			@Override
			public void onProcess(long current, long total) {
				pb.setProgress((int) (current*100/total));
				hint.setText("uploading: " + strfsize(current) + "/" + strfsize(total));
			}

			@Override
			public void onFailure(Exception ex) {
				String info = ex.getMessage();
				if (info.length() == 0) info = "unknown error! please retry!";
				Toast.makeText(PostActivity.this, info, Toast.LENGTH_LONG).show();
				btnSend.setEnabled(true);
				btnSelect.setEnabled(true);
			}
		});
	}

	public String strfsize(float size) {
		String[] unit = new String[] {"B", "K", "M", "G"};
		int unitLevel = 0;
		while (size >= 1024) {
			unitLevel ++;
			if (size < 1024*1024) {
				size = (size/1024);
				break;
			}
			size /= 1024;
		}
		if (unitLevel <= 1) return (int) size + unit[unitLevel];
		return String.format("%.2f%s", size, unit[unitLevel]);
	}
}
