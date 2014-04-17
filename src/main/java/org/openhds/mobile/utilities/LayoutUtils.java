package org.openhds.mobile.utilities;

import org.openhds.mobile.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
			String secondaryText, Object layoutTag, OnClickListener listener, ViewGroup container, int background) {

		RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(
				R.layout.generic_list_item, null);
		layout.setTag(layoutTag);

		if (null != listener) {
			layout.setOnClickListener(listener);
		}

		if (null != container) {
			container.addView(layout);
		}
		
		if(0 != background){
		layout.setBackgroundResource(background);
		}

		configureGenericLayout(activity, layout, primaryText, secondaryText);

		return layout;
	}

	public static void configureGenericLayout(Activity activity, RelativeLayout layout, String primaryText,
			String secondaryText) {

		TextView primary = (TextView) layout.findViewById(R.id.primary_text);
		TextView secondary = (TextView) layout.findViewById(R.id.secondary_text);

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
	}
}
