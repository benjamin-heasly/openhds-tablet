package org.openhds.mobile.utilities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.forms.FormContent;
import org.openhds.mobile.forms.FormInstance;
import org.openhds.mobile.task.parsing.AbstractPageParser;

import java.io.File;

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
    public static RelativeLayout makeTextWithPayload(Activity activity,
                                                     String primaryText,
                                                     String secondaryText,
                                                     Object layoutTag,
                                                     OnClickListener listener,
                                                     ViewGroup container,
                                                     int background,
                                                     ContentValues contentValues,
                                                     boolean centerText) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.generic_list_item_white_text, null);
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

        configureTextWithPayload(activity, layout, primaryText, secondaryText, contentValues, centerText);

        return layout;
    }

    // Pass new data to a layout that was created with makeTextWithPayload().
    public static void configureTextWithPayload(Activity activity,
                                                RelativeLayout layout,
                                                String primaryText,
                                                String secondaryText,
                                                ContentValues contentValues,
                                                boolean centerText) {

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

        if (null != contentValues) {
            for (String key : contentValues.keySet()) {
                String value = contentValues.getAsString(key);
                if (null == value) {
                    continue;
                }

                RelativeLayout relativeLayout = makeSmallTextWithValueAndLabel(activity, key, value, R.color.Black, R.color.AcidGray, R.color.AliceBlue);
                relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                payLoadContainer.addView(relativeLayout);
            }
        }

        if (0 == payLoadContainer.getChildCount()) {
            payLoadContainer.setVisibility(View.GONE);
        } else {
            payLoadContainer.setVisibility(View.VISIBLE);
        }

        if (centerText) {
            primary.setGravity(Gravity.CENTER);
            secondary.setGravity(Gravity.CENTER);
            payLoadContainer.setGravity(Gravity.CENTER);
            primary.setPadding(0, 0, 0, 0);
            secondary.setPadding(0, 0, 0, 0);
            payLoadContainer.setPadding(0, 0, 0, 0);

        } else {
            primary.setGravity(Gravity.CENTER);
            secondary.setGravity(Gravity.CENTER);
            payLoadContainer.setGravity(Gravity.NO_GRAVITY);
            primary.setPadding(0, 0, 0, 0);
            secondary.setPadding(0, 0, 0, 0);
            payLoadContainer.setPadding(15, 0, 0, 0);

        }
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeLargeTextWithValueAndLabel(Activity activity,
                                                                String label,
                                                                String valueText,
                                                                int labelColorId,
                                                                int valueColorId,
                                                                int missingColorId) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.value_with_label_large, null);
        configureTextWithValueAndLabel(layout, label, valueText, labelColorId, valueColorId, missingColorId);

        return layout;
    }

    // Create a pair of text views to represent some value plus its label, with given colors.
    public static RelativeLayout makeSmallTextWithValueAndLabel(Activity activity, String label, String valueText,
                                                                int labelColorId, int valueColorId, int missingColorId) {

        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.value_with_label_small, null);
        configureTextWithValueAndLabel(layout, label, valueText, labelColorId, valueColorId, missingColorId);

        return layout;
    }

    // Pass new data to text views created with makeLargeTextWithValueAndLabel().
    public static void configureTextWithValueAndLabel(RelativeLayout layout, String label, String valueText,
                                                      int labelColorId, int valueColorId, int missingColorId) {

        TextView labelTextView = (TextView) layout.findViewById(R.id.label_text);
        TextView delimiterTextView = (TextView) layout.findViewById(R.id.delimiter_text);
        TextView valueTextView = (TextView) layout.findViewById(R.id.value_text);

        labelTextView.setText(label);
        valueTextView.setText(valueText);

        Context context = layout.getContext();
        if (null == valueText || valueText.isEmpty() || valueText.equals("null")) {
            labelTextView.setTextColor(context.getResources().getColor(missingColorId));
            delimiterTextView.setTextColor(context.getResources().getColor(missingColorId));
            valueTextView.setTextColor(context.getResources().getColor(missingColorId));
            valueTextView.setText(R.string.not_available);
        } else {
            labelTextView.setTextColor(context.getResources().getColor(labelColorId));
            delimiterTextView.setTextColor(context.getResources().getColor(labelColorId));
            valueTextView.setTextColor(context.getResources().getColor(valueColorId));
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
    }

    // Set up a form list item based on a given form instance.
    public static void configureFormListItem(Context context, View view, FormInstance formInstance) {
        // get the data we want to display
        File formFile = new File(formInstance.getFilePath());
        EncryptionHelper.decryptFile(formFile, context);
        FormContent formContent = FormContent.readFormContent(new File(formInstance.getFilePath()));
        EncryptionHelper.encryptFile(formFile, context);

        // stuff values into the view widget
        TextView formTypeView = (TextView) view.findViewById(R.id.form_instance_list_type);
        formTypeView.setText(formInstance.getDisplayName());

        TextView formIdView = (TextView) view.findViewById(R.id.form_instance_list_id);
        setTextIfExists(formIdView, formContent, FormContent.TOP_LEVEL_ALIAS, "extId");

        TextView fieldWorkerView = (TextView) view.findViewById(R.id.form_instance_list_fieldworker);
        setTextIfExists(fieldWorkerView, formContent, FormContent.TOP_LEVEL_ALIAS, "collectedByUuid");

        TextView formDateView = (TextView) view.findViewById(R.id.form_instance_list_date);
        setTextIfExists(formDateView, formContent, FormContent.TOP_LEVEL_ALIAS, "registrationDateTime");
    }

    private static void setTextIfExists(TextView textView, FormContent formContent, String alias, String fieldName) {
        if (null == textView) {
            return;
        }

        if (null == formContent) {
            textView.setText(R.string.not_available);
            return;
        }

        String value = formContent.getContentString(alias, fieldName);
        if (null == value) {
            textView.setText(R.string.not_available);
        } else {
            textView.setText(value);
        }
    }
}
