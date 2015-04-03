package com.qiniu.timeline;

import android.content.Context;
import android.net.Uri;
import com.qiniu.auth.Client;
import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.IO;
import com.qiniu.io.PutExtra;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class Api extends Client {
	public static String host = "http://qiniutimeline.sinaapp.com";
	public static PutExtra putExtra = new PutExtra();

	public Api(HttpClient client) {
		super(client);
	}
	public Api() {
		super(Client.getMultithreadClient());
	}

	public void login(String user, String pswd, JSONObjectRet callback) {
		get("/user-api.php?action=login&user="+user+"&pswd="+pswd, callback);
	}

	public void register(String user, String pswd, JSONObjectRet callback) {
		get("/user-api.php?action=register&user="+user+"&pswd="+pswd, callback);
	}

	protected String verifyString() {
		return "user=" + TmpData.user.optString("user") + "&pswd=" + TmpData.user.optString("pswd");
	}

	public void list(long lastid, JSONObjectRet callback) {
		get("/list-api.php?" + verifyString() + "&count=10&lastid="+lastid, callback);
	}

	public void token(JSONObjectRet callback) {
		get("/upload-api.php?action=token&" + verifyString(), callback);
	}

	public void put(final Context mContext, final Uri uri, final JSONObjectRet callback) {
		TokenMgr.getToken(new TokenMgr.IGetToken() {
			@Override
			public void onToken(String token) {
				IO.putFile(mContext, token, null, uri, putExtra, callback);
			}

			@Override
			public void onFailure(Exception ex) {
				callback.onFailure(ex);
			}
		});
	}

	public void put(final Context mContext, final Uri uri, final PutExtra extra, final JSONObjectRet callback) {
		TokenMgr.getToken(new TokenMgr.IGetToken() {
			@Override
			public void onToken(String token) {
				IO.putFile(mContext, token, null, uri, extra, callback);
			}

			@Override
			public void onFailure(Exception ex) {
				callback.onFailure(ex);
			}
		});
	}

	public void get(String url, final JSONObjectRet callback) {
		url = host + url;
		get(makeClientExecutor(), url, new JSONObjectRet() {
			@Override
			public void onSuccess(JSONObject obj) {
				callback.onSuccess(obj);
			}

			@Override
			public void onFailure(Exception ex) {
				try {
					JSONObject json = new JSONObject(ex.getMessage());
					callback.onFailure(new Exception(json.optString("data")));
				} catch (JSONException e) {
					callback.onFailure(e);
					return;
				}
			}
		});
	}
}
