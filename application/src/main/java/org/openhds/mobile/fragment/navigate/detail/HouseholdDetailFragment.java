//package org.openhds.mobile.fragment.navigate.detail;
//
//import android.content.ContentResolver;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import org.openhds.mobile.R;
//import org.openhds.mobile.model.core.Individual;
//import org.openhds.mobile.model.core.Location;
//import org.openhds.mobile.model.core.SocialGroup;
//import org.openhds.mobile.repository.GatewayRegistry;
//import org.openhds.mobile.repository.gateway.IndividualGateway;
//import org.openhds.mobile.repository.gateway.LocationGateway;
//import org.openhds.mobile.repository.gateway.SocialGroupGateway;
//
//import java.util.List;
//
//import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;
//
//public class HouseholdDetailFragment extends DetailFragment {
//	LinearLayout detailContainer;
//	int greenLabel;
//	int greenValue;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		detailContainer = (LinearLayout) inflater.inflate(R.layout.household_detail_fragment, container, false);
//
//		greenLabel = R.color.LabelGreen;
//		greenValue = R.color.ValueGreen;
//
//		return detailContainer;
//	}
//
//	@Override
//	public void setUpDetails() {
//
//		SocialGroup socialGroup = getHousehold(navigateActivity.getCurrentSelection().getUuid());
//
//		if (null != socialGroup) {
//			LinearLayout householdBasicInfoContainer =
//                    (LinearLayout) detailContainer.findViewById(R.id.household_detail_frag_basic_info);
//
//			householdBasicInfoContainer.removeAllViews();
//
//			TextView extIdTextView = (TextView) detailContainer.findViewById(R.id.household_detail_frag_extid);
//			extIdTextView.setText(socialGroup.getLocationUuid());
//
//			householdBasicInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
//                    R.string.social_group_name,
//                    socialGroup.getGroupName(),
//                    greenLabel, greenValue, R.color.NA_Gray));
//
//			householdBasicInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
//                    R.string.social_group_head_ext_id,
//                    socialGroup.getGroupHeadUuid(),
//                    greenLabel, greenValue, R.color.NA_Gray));
//
//			householdBasicInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
//                    R.string.social_group_member_count,
//                    getMemberCount(socialGroup.getExtId()),
//                    greenLabel, greenValue, R.color.NA_Gray));
//
//            Location location = getLocation(navigateActivity.getCurrentSelection().getExtId());
//
//            householdBasicInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
//                    R.string.location_description_label,
//                    location.getDescription(),
//                    greenLabel, greenValue, R.color.NA_Gray));
//
//
//		} else {
//			TextView extIdTextView = (TextView) detailContainer.findViewById(R.id.household_detail_frag_extid);
//			extIdTextView.setText(R.string.details_not_available);
//		}
//	}
//
//	private SocialGroup getHousehold(String extId) {
//
//        SocialGroupGateway socialGroupGateway = GatewayRegistry.getSocialGroupGateway();
//        ContentResolver contentResolver = navigateActivity.getContentResolver();
//        SocialGroup socialGroup = socialGroupGateway.getFirst(contentResolver, socialGroupGateway.findById(extId));
//
//		return socialGroup;
//	}
//
//    private Location getLocation(String extId) {
//
//        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
//        ContentResolver contentResolver = navigateActivity.getContentResolver();
//        Location location = locationGateway.getFirst(contentResolver, locationGateway.findById(extId));
//
//        return location;
//
//    }
//
//	private String getMemberCount(String extId) {
//        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
//        ContentResolver contentResolver = navigateActivity.getContentResolver();
//        List<Individual> individuals = individualGateway.getList(contentResolver, individualGateway.findByResidency(extId));
//
//		return Integer.toString(individuals.size());
//	}
//}
