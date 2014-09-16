package org.openhds.mobile.utilities;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.openhds.mobile.R;

import java.util.Map;

public class LayoutUtils {

    // Create a new Button with the given description, name, tag, and listener.
    public static Button makeButton(Activity activity, int descriptionId, int buttonNameId, Object buttonTag,
                                    OnClickListener listener, ViewGroup container) {

        View view = activity.getLayoutInflater().inflate(R.layout.generic_textview_button, null);
        container.addView(view);

        TextView textView = (TextView) view.findViewById(R.id.generic_button_description);
        if (descriptionId > 0) {
            textView.setText(descriptionId);
        } else {
            textView.setVisibility(View.GONE);
        }

        Button button = (Button) view.findViewById(R.id.generic_button);
        button.setText(buttonNameId);
        button.setTag(buttonTag);
        button.setOnClickListener(listener);

        return button;
    }

    // Create a new Layout that contains two text views and optionally several "payload" text views beneath.
    public static RelativeLayout makeTextWithPayload(Activity activity, String primaryText, String secondaryText,
                                                     Object layoutTag, OnClickListener listener, ViewGroup container,
                                                     int background, Map<Integer, String> stringsPayLoad,
                                                     Map<Integer, Integer> stringsIdsPayLoad) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.generic_list_item, null);
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

        configureTextWithPayload(activity, layout, primaryText, secondaryText, stringsPayLoad, stringsIdsPayLoad);

        return layout;
    }

    // Pass new data to a layout that was created with makeTextWithPayload().
    public static void configureTextWithPayload(Activity activity, RelativeLayout layout, String primaryText,
                                                String secondaryText, Map<Integer, String> stringsPayload,
                                                Map<Integer, Integer> stringsIdsPayload) {

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

        // fill in payload strings, if any
        payLoadContainer.removeAllViews();

        if (null != stringsPayload) {
            for (Integer key : stringsPayload.keySet()) {
                String value = stringsPayload.get(key);

                if (null == value) {
                    continue;
                }

                TextView textView = new TextView(activity);
                textView.setTextAppearance(activity, android.R.style.TextAppearance_Small_Inverse);
                textView.setText(activity.getResources().getString(key) + ": " + value);

                payLoadContainer.addView(textView);
            }
        }

        if (null != stringsIdsPayload) {
            for (Integer key : stringsIdsPayload.keySet()) {
                String value = activity.getResources().getString(stringsIdsPayload.get(key));

                if (null == value) {
                    continue;
                }

                TextView textView = new TextView(activity);
                textView.setTextAppearance(activity, android.R.style.TextAppearance_Small_Inverse);
                textView.setText(activity.getResources().getString(key) + ": " + value);

                payLoadContainer.addView(textView);
            }
        }

        if (0 == payLoadContainer.getChildCount()) {
            payLoadContainer.setVisibility(View.GONE);
        } else {
            payLoadContainer.setVisibility(View.VISIBLE);
        }
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeTextWithValueAndLabel(Activity activity, int labelId, String valueText,
                                                           int labelColor, int valueColor, int missingColor) {

        final int textSize = 20;

        //create text views
        TextView labelTextView = new TextView(activity);
        labelTextView.setId(1);
        labelTextView.setTextSize(textSize);

        TextView delimiterTextView = new TextView(activity);
        delimiterTextView.setId(2);
        delimiterTextView.setTextSize(textSize);
        delimiterTextView.setText(" : ");

        TextView valueTextView = new TextView(activity);
        valueTextView.setId(3);
        valueTextView.setTextSize(textSize);

        RelativeLayout layout = new RelativeLayout(activity);

        // assemble text views into one layout
        layout.addView(labelTextView);

        RelativeLayout.LayoutParams delimiterParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        delimiterParams.addRule(RelativeLayout.RIGHT_OF, labelTextView.getId());
        layout.addView(delimiterTextView, delimiterParams);

        RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        valueParams.addRule(RelativeLayout.RIGHT_OF, delimiterTextView.getId());
        layout.addView(valueTextView, valueParams);

        configureTextWithValueAndLabel(layout, labelId, valueText, labelColor, valueColor, missingColor);

        return layout;
    }

    // Pass new data to text views created with makeTextWithValueAndLabel().
    public static void configureTextWithValueAndLabel(RelativeLayout layout,int labelId, String valueText,
                                                      int labelColor, int valueColor, int missingColor) {

        TextView labelTextView = (TextView) layout.findViewById(1);
        TextView delimiterTextView = (TextView) layout.findViewById(2);
        TextView valueTextView = (TextView) layout.findViewById(3);

        labelTextView.setText(labelId);
        valueTextView.setText(valueText);
        if (null == valueText || valueText.isEmpty()) {
            // TODO: must convert color id to actual color-int
            labelTextView.setTextColor(missingColor);
            delimiterTextView.setTextColor(missingColor);
            valueTextView.setTextColor(missingColor);
            valueTextView.setText(R.string.not_available);
        } else {
            labelTextView.setTextColor(labelColor);
            delimiterTextView.setTextColor(labelColor);
            valueTextView.setTextColor(valueColor);
        }
    }


    // Create a new edit text with the given hint (String resource id) and tag.
    public static EditText makeEditText(Activity activity, int hintId, String tag) {
        EditText editText = (EditText) activity.getLayoutInflater().inflate(R.layout.generic_edit_text, null);
        configureEditText(editText, hintId, tag);
        return editText;
    }

    // Pass new data to an edit text that was created with makeEditText().
    public static void configureEditText(EditText editText, int hintId, String tag) {
        editText.setHint(hintId);
        editText.setTag(tag);
        editText.setText(null);
        return;
    }
}