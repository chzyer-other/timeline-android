package com.qiniu.timeline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.qiniu.auth.JSONObjectRet;
import org.json.JSONException;
import org.json.JSONObject;

public class MyActivity extends Activity implements View.OnClickListener {
	Button btnLogin;
	Button btnRegister;
	EditText edtUsername;
	EditText edtPassword;
	Api api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		api = new Api();
		initWidget();
		edtUsername.setText("chzyer_android");
		edtPassword.setText("adf");
//		onClick(btnLogin);
	}

	private void initWidget() {
		btnLogin = (Button) findViewById(R.id.button1);
		btnLogin.setOnClickListener(this);
		btnRegister = (Button) findViewById(R.id.button);
		btnRegister.setOnClickListener(this);
		edtUsername = (EditText) findViewById(R.id.editText1);
		edtPassword = (EditText) findViewById(R.id.editText);
	}

	@Override
	public void onClick(View view) {
		String user = edtUsername.getText().toString();
		final String pswd = edtPassword.getText().toString();
		if (user.length() <= 0 && pswd.length() <= 0) {
			toast("miss user or pswd");
			return;
		}

		if (view.equals(btnLogin)) {
			api.login(user, pswd, new JSONObjectRet() {
				@Override
				public void onSuccess(JSONObject obj) {
					obj = obj.optJSONObject("data");
					toast("welcome! " + obj.optString("user"));
					try {
						obj.put("pswd", pswd);
					} catch (JSONException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
						onFailure(e);
						return;
					}
					TmpData.user = obj;
					startActivity(new Intent(MyActivity.this, Main.class));
				}

				@Override
				public void onFailure(Exception ex) {
					toast(ex.getMessage());
				}
			});
			return;
		}

		if (view.equals(btnRegister)) {
			api.register(user, pswd, new JSONObjectRet() {
				@Override
				public void onSuccess(JSONObject obj) {
					obj = obj.optJSONObject("data");
					toast("register success!");
					try {
						obj.put("pswd", pswd);
					} catch (JSONException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
						onFailure(e);
						return;
					}
					TmpData.user = obj;
					startActivity(new Intent(MyActivity.this, Main.class));
				}

				@Override
				public void onFailure(Exception ex) {
					toast(ex.getMessage());
				}
			});
			return;
		}
	}

	private void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
}
