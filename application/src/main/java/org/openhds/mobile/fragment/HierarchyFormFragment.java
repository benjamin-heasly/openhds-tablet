package org.openhds.mobile.fragment;

import static org.openhds.mobile.utilities.LayoutUtils.configureGenericLayout;
import static org.openhds.mobile.utilities.LayoutUtils.makeNewGenericLayout;

import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.HierarchyNavigator;
import org.openhds.mobile.model.FormBehaviour;

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

public class HierarchyFormFragment extends Fragment {

	private HierarchyNavigator navigator;
	HierarchyFormAdapter formListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout formContainer = (LinearLayout) inflater.inflate(
				R.layout.hierarchy_form_fragment, container, false);

		return formContainer;
	}

	public void setNavigator(HierarchyNavigator navigator) {
		this.navigator = navigator;
	}

	public void createFormButtons(List<FormBehaviour> values) {

		formListAdapter = new HierarchyFormAdapter(getActivity(),
				R.layout.generic_list_item, values);

		ListView listView = (ListView) getActivity().findViewById(
				R.id.form_fragment_listview);
		listView.setAdapter(formListAdapter);
		listView.setOnItemClickListener(new HierarchyFormListener());

	}

	private class HierarchyFormListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FormBehaviour form = formListAdapter.getItem(position);

			navigator.launchForm(form);

		}
	}

	private class HierarchyFormAdapter extends ArrayAdapter<FormBehaviour> {

		public HierarchyFormAdapter(Context context, int resource,
				List<FormBehaviour> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			FormBehaviour form = formListAdapter.getItem(position);

			if (convertView == null) {
				convertView = makeNewGenericLayout(getActivity(),
						getString(form.getFormLabelId()), null,
						form.getFormLabelId(), null, null,
						R.drawable.form_frag_selector, null, null);
			}

			configureGenericLayout(getActivity(), (RelativeLayout) convertView,
					getString(form.getFormLabelId()), null, null, null);
			return convertView;

		}
	}

}
