package com.qiniu.timeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.qiniu.auth.CallRet;
import com.qiniu.auth.Client;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResourceItem extends RelativeLayout {
	private JSONObject mData;
	TextView user;
	TextView date;
	TextView desc;
	ImageView preview;
	static LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024)/8) {
		@Override
		protected int sizeOf(String key, Bitmap bitmap) {
			return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
		}
	};
	ArrayList<String> downloadingUrl = new ArrayList<String>();
	public ResourceItem(Context context) {
		super(context);
		RelativeLayout group = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.item, null);
		user = (TextView) group.findViewById(R.id.textView);
		date = (TextView) group.findViewById(R.id.textView1);
		desc = (TextView) group.findViewById(R.id.textView2);
		preview = (ImageView) group.findViewById(R.id.imageView);
		addView(group);
	}

	public ResourceItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void showData(JSONObject data) {
		mData = data;
		user.setText(data.optString("user"));
		date.setText(data.optString("date"));
		desc.setText(data.optString("desc"));
		final String url = data.optString("url");
		if (data.optString("mime").indexOf("image") >= 0) {
			Bitmap bm = mMemoryCache.get(url);
			if (bm != null) {
				preview.setImageBitmap(bm);
				return;
			}
			if (downloadingUrl.contains(url)) return;
			downloadingUrl.add(url);
			Client.get(url, new CallRet() {
				@Override
				public void onSuccess(byte[] body) {
					Bitmap bm = BitmapFactory.decodeByteArray(body, 0, body.length);
					mMemoryCache.put(url, bm);
					preview.setImageBitmap(bm);
					downloadingUrl.remove(url);
				}

				@Override
				public void onFailure(Exception ex) {
					Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
					downloadingUrl.remove(url);
				}
			});
		} else {
			desc.setText(desc.getText() + "\n" + url);
		}
	}
}
