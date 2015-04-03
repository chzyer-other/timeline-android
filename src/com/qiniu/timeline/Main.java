package com.qiniu.timeline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.qiniu.auth.JSONObjectRet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Main extends Activity implements View.OnClickListener{
	Api api = new Api();
	TextView user;
	Button btnPost;
	SimpleAdapter sa;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initWidget();
	}

	private void initWidget() {
		ListView listView = (ListView) findViewById(R.id.listView);
		sa = new SimpleAdapter();
		listView.setAdapter(sa);
		sa.update();
		user = (TextView) findViewById(R.id.textView);
		user.setText(TmpData.user.optString("user"));

		btnPost = (Button) findViewById(R.id.button);
		btnPost.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(btnPost)) {
			startActivityForResult(new Intent(this, PostActivity.class), 0);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		if (requestCode == 0) {
			try {
				JSONObject obj = new JSONObject(data.getAction());
				sa.add(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	class SimpleAdapter extends BaseAdapter {
		ArrayList<JSONObject> mList = new ArrayList<JSONObject>();
		long lastid = -1;
		boolean hasNext = true;
		Api api = new Api();
		public void add(JSONObject obj) {
			mList.add(0, obj);
			notifyDataSetChanged();
		}

		public void update() {
			api.list(lastid, new JSONObjectRet() {
				@Override
				public void onSuccess(JSONObject obj) {
					JSONArray data = obj.optJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						mList.add(data.optJSONObject(i));
						lastid = mList.get(mList.size() - 1).optLong("rid");
					}
					hasNext = obj.optInt("count", 0) - data.length() > 0;
					notifyDataSetChanged();
				}

				@Override
				public void onFailure(Exception ex) {
					toast("fetch data failure! detail: " + ex.getMessage());
				}
			});
		}

		@Override
		public int getCount() {
			int count = mList.size();
			if (hasNext) count += 1;
			return count;
		}

		@Override
		public Object getItem(int i) {
			if (i == mList.size()) return null;
			return mList.get(i);
		}

		@Override
		public long getItemId(int i) {
			if (i == mList.size()) return -1;
			return mList.get(i).optInt("rid");
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			if (i == mList.size()) {
				Button v = new Button(Main.this);
				v.setText("load more");
				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						update();
					}
				});
				return v;
			}
			ResourceItem item = new ResourceItem(Main.this);
			item.showData(mList.get(i), getMaxPicWidth());
			return item;
		}
	}

	private void toast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	public int getMaxPicWidth() {

		return getWindowManager().getDefaultDisplay().getWidth() - dpToPx(16);
	}

	public int dpToPx(int dp) {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}
}
