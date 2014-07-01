package org.openhds.mobile.fragment.detailfragments;

import static org.openhds.mobile.utilities.LayoutUtils.makeDetailFragmentTextView;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
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
	int greenLabel;
	int greenValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		detailContainer = (LinearLayout) inflater.inflate(
				R.layout.household_detail_fragment, container, false);

		greenLabel = getResources().getColor(R.color.LabelGreen);
		greenValue = getResources().getColor(R.color.ValueGreen);

		return detailContainer;
	}

	@Override
	public void setUpDetails() {

		SocialGroup socialGroup = getHousehold(navigateActivity
				.getCurrentSelection().getExtId(), navigateActivity);

		if (null != socialGroup) {
			LinearLayout socialGroupBasicInfoContainer = (LinearLayout) detailContainer
					.findViewById(R.id.household_detail_frag_basic_info);

			socialGroupBasicInfoContainer.removeAllViews();

			TextView extIdTextView = (TextView) detailContainer
					.findViewById(R.id.household_detail_frag_extid);
			extIdTextView.setText(socialGroup.getExtId());

			socialGroupBasicInfoContainer.addView(makeDetailFragmentTextView(
					getActivity(), getString(R.string.household_name),
					socialGroup.getGroupName(), greenLabel, greenValue));

			socialGroupBasicInfoContainer.addView(makeDetailFragmentTextView(
					getActivity(), getString(R.string.household_head_extid),
					socialGroup.getGroupHead(), greenLabel, greenValue));

			socialGroupBasicInfoContainer.addView(makeDetailFragmentTextView(
					getActivity(), getString(R.string.household_member_count),
					getMemberCount(socialGroup.getExtId()), greenLabel,
					greenValue));

		} else {
			TextView extIdTextView = (TextView) detailContainer
					.findViewById(R.id.household_detail_frag_extid);
			extIdTextView.setText(getString(R.string.details_not_available));
		}

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

	private String getMemberCount(String extId) {
		String memberCount;

		Cursor cursor = Queries.getIndividualsByResidency(getActivity()
				.getContentResolver(), extId);

		memberCount = Integer.toString(cursor.getCount());

		return memberCount;

	}

}
