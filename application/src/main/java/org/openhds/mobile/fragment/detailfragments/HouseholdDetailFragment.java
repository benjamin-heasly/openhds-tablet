package org.openhds.mobile.fragment.detailfragments;

import android.content.ContentResolver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.makeDetailFragmentTextView;

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

		SocialGroup socialGroup = getHousehold(navigateActivity.getCurrentSelection().getExtId());

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

	private SocialGroup getHousehold(String extId) {

        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        SocialGroup socialGroup = socialGroupGateway.getFirst(contentResolver, socialGroupGateway.findById(extId));

		return socialGroup;
	}

	private String getMemberCount(String extId) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(extId));

		return Integer.toString(individuals.size());
	}
}
