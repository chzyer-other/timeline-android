package com.qiniu.timeline;

import com.qiniu.auth.JSONObjectRet;
import org.json.JSONObject;

import java.util.ArrayList;

public class TokenMgr {
	public static int expiredTime = 3600;
	public static long tokenMakeTime = 0;
	public static String token = "";
	private static ArrayList<IGetToken> queue = new ArrayList<IGetToken>();
	public static void getToken(final IGetToken callback) {
		if (System.currentTimeMillis()/1000 - expiredTime <= expiredTime-10) {
			callback.onToken(token);
			return;
		}

		if (queue.size() > 0) {
			queue.add(callback);
			return;
		}
		queue.add(callback);
		new Api().token(new JSONObjectRet() {
			@Override
			public void onSuccess(JSONObject obj) {
				token = obj.optString("data");
				tokenMakeTime = System.currentTimeMillis() / 1000;
				while (queue.size() > 0) {
					IGetToken gt = queue.remove(0);
					if (gt != null) gt.onToken(token);
				}
			}

			@Override
			public void onFailure(Exception ex) {
				callback.onFailure(ex);
			}
		});
	}

	public static interface IGetToken {
		public void onToken(String token);
		public void onFailure(Exception ex);
	}
}
