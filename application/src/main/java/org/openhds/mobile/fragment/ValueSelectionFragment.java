package org.openhds.mobile.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.repository.QueryResult;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureGenericLayout;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericLayout;

public class ValueSelectionFragment extends Fragment {

    private SelectionHandler selectionHandler;
    private ValueSelectionListAdapter queryResultAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout valueContainer = (LinearLayout) inflater.inflate(R.layout.value_selection_fragment, container, false);
        return valueContainer;
    }

    public void populateValues(List<QueryResult> values) {
        queryResultAdapter = new ValueSelectionListAdapter(getActivity(), R.layout.generic_list_item, values);
        ListView listView = (ListView) getActivity().findViewById(R.id.value_fragment_listview);
        listView.setAdapter(queryResultAdapter);
        listView.setOnItemClickListener(new ValueClickListener());
    }

    public void setSelectionHandler(SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public interface SelectionHandler {
        public void handleSelectedValue(QueryResult queryResult);
    }

    private class ValueClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            QueryResult selected = queryResultAdapter.getItem(position);
            selectionHandler.handleSelectedValue(selected);
        }
    }

    private class ValueSelectionListAdapter extends ArrayAdapter<QueryResult> {

        public ValueSelectionListAdapter(Context context, int resource, List<QueryResult> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            QueryResult qr = queryResultAdapter.getItem(position);

            if (convertView == null) {
                convertView = makeNewGenericLayout(getActivity(), qr.getName(), qr.getExtId(), qr.getName(),
                        null, null, R.drawable.value_frag_selector, qr.getStringsPayload(), qr.getStringIdsPayload());
            } else {
                configureGenericLayout(getActivity(), (RelativeLayout) convertView, qr.getName(), qr.getExtId(),
                        qr.getStringsPayload(), qr.getStringIdsPayload());
            }

            return convertView;
        }
    }
}
