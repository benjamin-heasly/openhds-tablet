package org.openhds.mobile.fragment;

import org.openhds.mobile.R;
import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.model.Individual;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IndividualDetailFragment extends DetailFragment {

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

		Individual individual = getIndividual(navigateActivity
				.getCurrentSelection().getExtId(), navigateActivity);

		TextView extIdTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_extid);
		extIdTextView.setText(individual.getExtId());

		TextView firstNameTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_firstname);
		firstNameTextView.setText(individual.getFirstName());

		TextView lastNameTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_lastname);
		lastNameTextView.setText(individual.getLastName());

		TextView otherNamesTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_othernames);
		otherNamesTextView.setText(individual.getOtherNames());

		TextView genderTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_gender);
		genderTextView.setText(individual.getGender());

		TextView dobTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_dob);
		dobTextView.setText(individual.getDob());

		TextView ageTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_age);
		ageTextView.setText(individual.getAge());

		TextView otherIdTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_otherid);
		otherIdTextView.setText(individual.getOtherId());

		TextView phoneNumberTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_phonenumber);
		phoneNumberTextView.setText(individual.getPhoneNumber());

		TextView otherPhoneNumberTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_otherphonenumber);
		otherPhoneNumberTextView.setText(individual.getOtherPhoneNumber());

		TextView pointOfContactNameTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_pointofcontactname);
		pointOfContactNameTextView.setText(individual.getPointOfContactName());

		TextView pointOfContactNumberTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_pointofcontactnumber);
		pointOfContactNumberTextView.setText(individual
				.getPointOfContactPhoneNumber());

		TextView languagePreferenceTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_languagepreference);
		languagePreferenceTextView.setText(individual.getLanguagePreference());

		TextView statusTextView = (TextView) detailContainer
				.findViewById(R.id.individual_detail_frag_memberstatus);
		statusTextView.setText(individual.getMemberStatus());
	}

	private Individual getIndividual(String extId,
			NavigateActivity navigateActivity) {

		Cursor cursor = Queries.getIndividualByExtId(
				navigateActivity.getContentResolver(), extId);
		cursor.moveToFirst();
		Individual individual = Converter.toIndividual(cursor, true);

		return individual;

	}
}
