package org.openhds.mobile.utilities;

import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.projectdata.ProjectResourceMapper;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LayoutUtils {

	public static Button makeNewGenericButton(Activity activity, String description, String buttonName,
			Object buttonTag, OnClickListener listener, ViewGroup container) {

		View v = activity.getLayoutInflater().inflate(R.layout.generic_textview_button, null);
		container.addView(v);
		Button b = (Button) v.findViewById(R.id.generic_button);
		TextView t = (TextView) v.findViewById(R.id.generic_button_description);

		if (null == description) {
			t.setVisibility(View.GONE);

		} else {
			t.setText(description);
		}

		b.setText(buttonName);
		b.setTag(buttonTag);
		b.setOnClickListener(listener);

		return b;
	}

	public static RelativeLayout makeNewGenericLayout(Activity activity, String primaryText,
			String secondaryText, Object layoutTag, OnClickListener listener, ViewGroup container,
			int background, Map<String, String> payLoad) {

		RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(
				R.layout.generic_list_item, null);
		layout.setTag(layoutTag);

		if (null != listener) {
			layout.setOnClickListener(listener);
		}

		if (null != container) {
			container.addView(layout);
		}

		if (0 != background) {
			layout.setBackgroundResource(background);
		}

		configureGenericLayout(activity, layout, primaryText, secondaryText, payLoad);

		return layout;
	}

	public static void configureGenericLayout(Activity activity, RelativeLayout layout, String primaryText,
			String secondaryText, Map<String, String> payLoad) {

		TextView primary = (TextView) layout.findViewById(R.id.primary_text);
		TextView secondary = (TextView) layout.findViewById(R.id.secondary_text);
		LinearLayout payLoadContainer = (LinearLayout) layout.findViewById(R.id.pay_load_container);

		if (null == primaryText) {
			primary.setVisibility(View.GONE);
		} else {
			primary.setVisibility(View.VISIBLE);
			primary.setText(primaryText);
		}

		if (null == secondaryText) {
			secondary.setVisibility(View.GONE);
		} else {
			secondary.setVisibility(View.VISIBLE);
			secondary.setText(secondaryText);
		}

		if (null == payLoad) {
			payLoadContainer.setVisibility(View.GONE);
		} else {
			payLoadContainer.removeAllViews();
			
			for (String key : payLoad.keySet()) {
				String value = payLoad.get(key);
				if (null == value) {
					continue;
				}
				TextView textView = new TextView(activity);
				textView.setTextAppearance(activity, android.R.style.TextAppearance_Small_Inverse);
				textView.setText(key + ": " + value);
				payLoadContainer.addView(textView);
			}
			payLoadContainer.setVisibility(View.VISIBLE);
		}
	}
}
