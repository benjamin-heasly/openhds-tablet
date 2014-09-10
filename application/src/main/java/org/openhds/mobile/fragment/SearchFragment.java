package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.repository.Query;
import org.openhds.mobile.repository.QueryResult;
import org.openhds.mobile.repository.search.SearchPluginModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openhds.mobile.utilities.LayoutUtils.makeEditText;

/**
 * Allow user to search for entities using free text criteria.
 *
 * The gateway and database columns to search are supplied by one or more
 * SearchPluginModules.  The search results are passed as a list of
 * QueryResults to a listener.
 */
public class SearchFragment extends Fragment {

    private SearchPluginModule currentPluginModule;
    private ResultsHandler resultsHandler;
    private ArrayAdapter<SearchPluginModule> searchPluginAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        View button = view.findViewById(R.id.search_fragment_button);
        button.setOnClickListener(new ButtonClickHandler());

        return view;
    }

    public void setResultsHandler(ResultsHandler resultsHandler) {
        this.resultsHandler = resultsHandler;
    }

    public void setSearchPluginModules(List<SearchPluginModule> searchPluginModules) {

        Spinner spinner = (Spinner) getView().findViewById(R.id.search_fragment_spinner);

        if (null == searchPluginModules || 0 == searchPluginModules.size()) {
            spinner.setVisibility(View.GONE);
            return;
        }

        // activate the only plugin
        if (1 == searchPluginModules.size()) {
            setPluginModule(searchPluginModules.get(0));
            spinner.setVisibility(View.GONE);
            return;
        }

        // allow the user to choose from 2 or more plugins
        searchPluginAdapter = new SpinnerListAdapter(getActivity(), R.layout.generic_dropdown_item, searchPluginModules);
        spinner.setAdapter(searchPluginAdapter);
        spinner.setOnItemSelectedListener(new SpinnerClickHandler());
    }

    public void setTitle(int titleId) {
        TextView textView = (TextView) getView().findViewById(R.id.search_fragment_title);
        textView.setText(titleId);
    }

    // Set up search fields for a selected search plugin module.
    private void setPluginModule(SearchPluginModule searchPluginModule) {
        currentPluginModule = searchPluginModule;
        configureEditTexts();
    }

    private void configureEditTexts() {
        LinearLayout editTextContainer = (LinearLayout) getView().findViewById(R.id.search_fragment_container);
        editTextContainer.removeAllViews();
        for (String columnName : currentPluginModule.getColumnsAndLabels().keySet()) {
            Integer textHintId = currentPluginModule.getColumnsAndLabels().get(columnName);
            EditText editText = makeEditText(getActivity(), textHintId, columnName);
            editTextContainer.addView(editText);
        }
    }

    // Gather column values from the current edit texts, exclude empty text.
    private Map<String, String> gatherColumnValues() {
        Map<String, String> columnValues = new HashMap<>();
        for (String columnName : currentPluginModule.getColumnsAndLabels().keySet()) {
            EditText editText = (EditText) getView().findViewWithTag(columnName);
            String columnValue = editText.getText().toString();
            if (null != columnValue && !columnValue.isEmpty()) {
                columnValues.put(columnName, columnValue);
            }
        }
        return columnValues;
    }

    private void performQuery() {
        Map<String, String> columnNamesAndValues = gatherColumnValues();
        int nValues = columnNamesAndValues.size();
        if (0 == nValues) {
            return;
        }

        // build a query with the values that the user typed in
        final String[] columnNames = columnNamesAndValues.keySet().toArray(new String[nValues]);
        final String[] columnValues = columnNamesAndValues.values().toArray(new String[nValues]);
        Query query = currentPluginModule.getGateway().findByCriteriaLike(columnNames, columnValues, columnNames[0]);
        List<QueryResult> queryResults = currentPluginModule.getGateway().getQueryResultList(
                getActivity().getContentResolver(), query, "searchFragment");

        // report the results to the listener
        if (null != resultsHandler) {
            resultsHandler.handleSearchResults(queryResults);
        }
    }

    public interface ResultsHandler {
        public void handleSearchResults(List<QueryResult> queryResults);
    }

    // Display a choice of search plugin modules.
    private class SpinnerListAdapter extends ArrayAdapter<SearchPluginModule> {

        public SpinnerListAdapter(Context context, int resource, List<SearchPluginModule> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // make or reuse a text view for this item
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.generic_dropdown_item, null);
            }

            // set the text of this item from the corresponding search module plugin
            final TextView textView = (TextView) convertView;
            SearchPluginModule searchPluginModule = getItem(position);
            textView.setText(searchPluginModule.getLabelId());
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }

    // Set up search fields when the user chooses a plugin module from the spinner.
    private class SpinnerClickHandler implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            SearchPluginModule searchPluginModule = (SearchPluginModule) adapterView.getItemAtPosition(position);
            setPluginModule(searchPluginModule);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }
    private class ButtonClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            performQuery();
        }
    }
}