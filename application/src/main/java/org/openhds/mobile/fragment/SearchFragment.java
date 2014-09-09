package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.repository.QueryResult;
import org.openhds.mobile.repository.search.SearchPluginModule;

import java.util.List;

/**
 * Allow user to search for entities using free text criteria.
 *
 * The gateway and database columns to search are supplied by one or more
 * SearchPluginModules.  The search results are passed as a list of
 * QueryResults to a listener.
 */
public class SearchFragment extends Fragment {

    private SelectionHandler selectionHandler;
    private ArrayAdapter<SearchPluginModule> searchPluginAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout searchContainer = (LinearLayout) inflater.inflate(R.layout.search_fragment, container, false);
        return searchContainer;
    }

    public void setSelectionHandler(SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public void setSearchPluginModules(List<SearchPluginModule> searchPluginModules) {
        searchPluginAdapter = new ChooseSearchSpinnerAdapter(getActivity(), R.layout.generic_dropdown_item, searchPluginModules);
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.search_fragment_spinner);
        spinner.setAdapter(searchPluginAdapter);
        //spinner.setOnItemClickListener(new SpinnerClickHandler());
    }

    public interface SelectionHandler {
        public void handleSearchResults(List<QueryResult> queryResults);
    }

    private class ChooseSearchSpinnerAdapter extends ArrayAdapter<SearchPluginModule> {

        public ChooseSearchSpinnerAdapter(Context context, int resource, List<SearchPluginModule> objects) {
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
}