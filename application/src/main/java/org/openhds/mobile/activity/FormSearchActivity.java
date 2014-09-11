package org.openhds.mobile.activity;

import android.app.Activity;
import android.os.Bundle;
import org.openhds.mobile.R;
import org.openhds.mobile.fragment.SearchFragment;
import org.openhds.mobile.fragment.ValueSelectionFragment;
import org.openhds.mobile.repository.search.FormSearchPluginModule;

public class FormSearchActivity extends Activity {

    public static final String FORM_SEARCH_PLUGINS_KEY = "formSearchPlugins";
    public static final String FORM_FIELD_NAMES_KEY = "formFieldNames";
    public static final String FORM_FIELD_VALUES_KEY = "formFieldValues";

    private static final String SEARCH_FRAGMENT_TAG = "searchFragment";
    private static final String VALUE_SELECTION_FRAGMENT_TAG = "valueSelectionFragmet";

    private SearchFragment searchFragment;
    private ValueSelectionFragment valueSelectionFragment;
    FormSearchPluginModule[] formSearchPluginModules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // basic view setup
        setContentView(R.layout.form_search_activity);
        setTitle(this.getResources().getString(R.string.search_lbl));

        if (null == savedInstanceState) {
            searchFragment = new SearchFragment();
            valueSelectionFragment = new ValueSelectionFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.form_search_middle_column, searchFragment, SEARCH_FRAGMENT_TAG)
                    .add(R.id.form_search_right_column, valueSelectionFragment, VALUE_SELECTION_FRAGMENT_TAG)
                    .commit();

            // what does the calling activity need the user to search for?
            formSearchPluginModules = (FormSearchPluginModule[]) getIntent().getExtras().get(FORM_SEARCH_PLUGINS_KEY);


        } else {
            searchFragment = (SearchFragment) getFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
            valueSelectionFragment= (ValueSelectionFragment) getFragmentManager().findFragmentByTag(VALUE_SELECTION_FRAGMENT_TAG);

            // remember what the user wants to search for on rotate
            formSearchPluginModules = (FormSearchPluginModule[]) savedInstanceState.get(FORM_SEARCH_PLUGINS_KEY);
        }

        int i = 4;

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // remember what the user wants to search for on rotate
        savedInstanceState.putSerializable(FORM_SEARCH_PLUGINS_KEY, formSearchPluginModules);
        super.onSaveInstanceState(savedInstanceState);
    }
}
