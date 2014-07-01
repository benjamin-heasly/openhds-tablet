package org.openhds.mobile.fragment.detailfragments;

import java.util.ArrayList;
import java.util.List;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectResources;

import static org.openhds.mobile.utilities.LayoutUtils.makeDetailFragmentTextView;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndividualDetailFragment extends DetailFragment {

	LinearLayout detailContainer;
	int greenLabel;
	int greenValue;
	int yellowLabel;
	int yellowValue;
	int pinkLabel;
	int pinkValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		detailContainer = (LinearLayout) inflater.inflate(
				R.layout.individual_detail_fragment, container, false);

		greenLabel = getResources().getColor(R.color.LabelGreen);
		greenValue = getResources().getColor(R.color.ValueGreen);

		yellowLabel = getResources().getColor(R.color.LabelYellow);
		yellowValue = getResources().getColor(R.color.ValueYellow);

		pinkLabel = getResources().getColor(R.color.LabelPink);
		pinkValue = getResources().getColor(R.color.ValuePink);

		return detailContainer;
	}

	@Override
	public void setUpDetails() {

		Individual individual = getIndividual(navigateActivity
				.getCurrentSelection().getExtId(), navigateActivity);

		List<Membership> memberships = getMemberships(individual.getExtId());

		LinearLayout personalInfoContainer = (LinearLayout) detailContainer
				.findViewById(R.id.individual_detail_frag_personal_info);

		LinearLayout contactInfoContainer = (LinearLayout) detailContainer
				.findViewById(R.id.individual_detail_frag_contact_info);

		LinearLayout membershipInfoContainer = (LinearLayout) detailContainer
				.findViewById(R.id.individual_detail_frag_membership_info);

		personalInfoContainer.removeAllViews();
		contactInfoContainer.removeAllViews();
		membershipInfoContainer.removeAllViews();
		
		// Draw extId
		TextView extIdTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_extid);
		extIdTextView.setText(individual.getExtId());

		// Name
		personalInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_name_label),
				individual.getFirstName() + " " + individual.getLastName(),
				greenLabel, greenValue));
		// Other names
		personalInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_other_names_label),
				individual.getOtherNames(), greenLabel, greenValue));

		personalInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.gender_lbl),
				getString(ProjectResources.Individual
						.getIndividualStringId(individual.getGender())),
				greenLabel, greenValue));

		// Language Preference
		personalInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_language_preference_label),
				getString(ProjectResources.Individual
						.getIndividualStringId(individual
								.getLanguagePreference())), greenLabel,
				greenValue));

		// age and birthday
		personalInfoContainer
				.addView(makeDetailFragmentTextView(getActivity(),
						getString(R.string.individual_age_label),
						Individual.getAgeWithUnits(individual), greenLabel,
						greenValue));

		personalInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_date_of_birth_label),
				individual.getDob(), greenLabel, greenValue));

		// Contact Info
		contactInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_personal_phone_number_label),
				individual.getPhoneNumber(), pinkLabel, pinkValue));
		contactInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_other_phone_number_label),
				individual.getOtherPhoneNumber(), pinkLabel, pinkValue));
		contactInfoContainer.addView(makeDetailFragmentTextView(getActivity(),
				getString(R.string.individual_point_of_contact_label),
				individual.getPointOfContactName(), pinkLabel, pinkValue));
		contactInfoContainer
				.addView(makeDetailFragmentTextView(
						getActivity(),
						getString(R.string.individual_point_of_contact_phone_number_label),
						individual.getPointOfContactPhoneNumber(), pinkLabel,
						pinkValue));

		// Memberships
		for (Membership membership : memberships) {

			membershipInfoContainer.addView(makeDetailFragmentTextView(
					getActivity(),
					getString(R.string.individual_relationship_to_head_label),
					getString(ProjectResources.Relationship
							.getRelationshipStringId(membership
									.getRelationshipToHead())), yellowLabel,
					yellowValue));

			membershipInfoContainer
					.addView(makeDetailFragmentTextView(
							getActivity(),
							getString(R.string.individual_socialgroup_extid_label),
							membership.getSocialGroupExtId(), yellowLabel,
							yellowValue));

		}

	}

	private Individual getIndividual(String extId,
			NavigateActivity navigateActivity) {

		Cursor cursor = Queries.getIndividualByExtId(
				navigateActivity.getContentResolver(), extId);
		cursor.moveToFirst();
		Individual individual = Converter.toIndividual(cursor, true);

		return individual;

	}

	private List<Membership> getMemberships(String individualExtId) {
		Cursor cursor = Queries.getMembershipsByIndividualExtId(
				navigateActivity.getContentResolver(), individualExtId);
		ArrayList<Membership> memberships = Converter.toMembershipList(cursor,
				true);

		return memberships;
	}

}
