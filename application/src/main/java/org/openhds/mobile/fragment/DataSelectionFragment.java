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
import android.widget.ListView;
import android.widget.RelativeLayout;
import org.openhds.mobile.R;
import org.openhds.mobile.repository.DataWrapper;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.configureTextWithPayload;
import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithPayload;

public class DataSelectionFragment extends Fragment {

    private SelectionHandler selectionHandler;
    private DataSelectionListAdapter dataWrapperAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.data_selection_fragment, container, false);
    }

    public void populateData(List<DataWrapper> dataWrappers) {
        dataWrapperAdapter = new DataSelectionListAdapter(getActivity(), R.layout.generic_list_item, dataWrappers);
        ListView listView = (ListView) getActivity().findViewById(R.id.data_fragment_listview);
        listView.setAdapter(dataWrapperAdapter);
        listView.setOnItemClickListener(new DataClickListener());
    }

    public void setSelectionHandler(SelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
    }

    public interface SelectionHandler {
        public void handleSelectedData(DataWrapper dataWrapper);
    }

    private class DataClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DataWrapper selected = dataWrapperAdapter.getItem(position);
            selectionHandler.handleSelectedData(selected);
        }
    }

    private class DataSelectionListAdapter extends ArrayAdapter<DataWrapper> {

        public DataSelectionListAdapter(Context context, int resource, List<DataWrapper> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DataWrapper qr = dataWrapperAdapter.getItem(position);

            if (convertView == null) {
                convertView = makeTextWithPayload(getActivity(), qr.getName(), qr.getExtId(), qr.getName(),
                        null, null, R.drawable.data_frag_selector, qr.getStringsPayload(), qr.getStringIdsPayload());
            } else {
                configureTextWithPayload(getActivity(), (RelativeLayout) convertView, qr.getName(), qr.getExtId(),
                        qr.getStringsPayload(), qr.getStringIdsPayload());
            }

            return convertView;
        }
    }
}
