package org.openhds.mobile.utilities;

import java.util.Map;

import org.openhds.mobile.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class LayoutUtils {

	public static Button makeNewGenericButton(Activity activity,
			String description, String buttonName, Object buttonTag,
			OnClickListener listener, ViewGroup container) {

		View v = activity.getLayoutInflater().inflate(
				R.layout.generic_textview_button, null);
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

	public static RelativeLayout makeNewGenericLayout(Activity activity,
			String primaryText, String secondaryText, Object layoutTag,
			OnClickListener listener, ViewGroup container, int background,
			Map<Integer, String> stringsPayLoad,
			Map<Integer, Integer> stringsIdsPayLoad) {

		RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater()
				.inflate(R.layout.generic_list_item, null);
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

		configureGenericLayout(activity, layout, primaryText, secondaryText,
				stringsPayLoad, stringsIdsPayLoad);

		return layout;
	}

	public static void configureGenericLayout(Activity activity,
			RelativeLayout layout, String primaryText, String secondaryText,
			Map<Integer, String> stringsPayLoad,
			Map<Integer, Integer> stringsIdsPayLoad) {

		TextView primary = (TextView) layout.findViewById(R.id.primary_text);
		TextView secondary = (TextView) layout
				.findViewById(R.id.secondary_text);
		LinearLayout payLoadContainer = (LinearLayout) layout
				.findViewById(R.id.pay_load_container);

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

		if (null == stringsPayLoad) {
			payLoadContainer.setVisibility(View.GONE);
		} else {
			payLoadContainer.removeAllViews();

			for (Integer key : stringsPayLoad.keySet()) {
				String value = stringsPayLoad.get(key);

				if (null == value) {
					continue;
				}

				TextView textView = new TextView(activity);
				textView.setTextAppearance(activity,
						android.R.style.TextAppearance_Small_Inverse);

				textView.setText(activity.getResources().getString(key) + ": "
						+ value);

				payLoadContainer.addView(textView);
			}

			for (Integer key : stringsIdsPayLoad.keySet()) {

				String value = activity.getResources().getString(
						stringsIdsPayLoad.get(key));

				if (null == value) {
					continue;
				}

				TextView textView = new TextView(activity);
				textView.setTextAppearance(activity,
						android.R.style.TextAppearance_Small_Inverse);

				textView.setText(activity.getResources().getString(key) + ": "
						+ value);

				payLoadContainer.addView(textView);
			}

			payLoadContainer.setVisibility(View.VISIBLE);
		}
	}

	public static RelativeLayout makeDetailFragmentTextView(Activity activity,
			String labelText, String valueText, int labelColor, int valueColor) {

		RelativeLayout layout = new RelativeLayout(activity);

		TextView labelTextView = new TextView(activity);
		labelTextView.setTextSize(20);
		labelTextView.setText(labelText + ": ");

		TextView valueTextView = new TextView(activity);
		valueTextView.setTextSize(20);
		valueTextView.setText(valueText);

		if (null == valueText || valueText.isEmpty()) {
			labelTextView.setTextColor(activity.getResources().getColor(
					R.color.NA_Gray));
			valueTextView.setTextColor(activity.getResources().getColor(
					R.color.NA_Gray));
			valueTextView.setText(activity.getResources().getString(
					R.string.not_available));
		} else {
			labelTextView.setTextColor(labelColor);
			valueTextView.setTextColor(valueColor);
		}

		layout.addView(labelTextView);

		labelTextView.setId(1);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF, labelTextView.getId());

		layout.addView(valueTextView, params);

		return layout;
	}

}
