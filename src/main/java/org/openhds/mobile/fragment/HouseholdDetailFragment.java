package org.openhds.mobile.fragment;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HouseholdDetailFragment extends DetailFragment {

	LinearLayout detailContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		detailContainer = (LinearLayout) inflater.inflate(
				R.layout.individual_detail_fragment, container, false);

		return detailContainer;
	}

	@Override
	public void setUpDetails() {

		SocialGroup socialGroup = getHousehold(navigateActivity
				.getCurrentSelection().getExtId(), navigateActivity);

		if(null == socialGroup){
			TextView extIdTextView = (TextView) detailContainer
					.findViewById(R.id.individual_detail_frag_extid);
			extIdTextView.setText("NO DETAILS FOR THIS HOUSEHOLD");
			return;
		}
		
		TextView extIdTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_extid);
		extIdTextView.setText(socialGroup.getExtId());

		TextView firstNameTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_firstname);
		firstNameTextView.setText("You are viewing details for the house of "
				+ socialGroup.getGroupName());

	}

	private SocialGroup getHousehold(String extId,
			NavigateActivity navigateActivity) {

		Cursor cursor = Queries.getSocialGroupByExtId(
				navigateActivity.getContentResolver(), extId);

		if (cursor.moveToFirst()) {
			SocialGroup socialGroup = Converter.toSocialGroup(cursor, true);
			return socialGroup;
		}
		return null;

	}
}
