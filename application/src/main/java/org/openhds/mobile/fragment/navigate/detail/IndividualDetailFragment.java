package org.openhds.mobile.fragment.navigate.detail;

import java.util.List;

import android.content.ContentResolver;
import android.widget.ScrollView;
import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectResources;

import static org.openhds.mobile.utilities.LayoutUtils.makeTextWithValueAndLabel;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;

public class IndividualDetailFragment extends DetailFragment {

    ScrollView detailContainer;
    int greenLabel;
    int greenValue;
    int yellowLabel;
    int yellowValue;
    int pinkLabel;
    int pinkValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        detailContainer = (ScrollView) inflater.inflate(R.layout.individual_detail_fragment, container, false);

        greenLabel = R.color.LabelGreen;
        greenValue = R.color.ValueGreen;

        yellowLabel = R.color.LabelYellow;
        yellowValue = R.color.ValueYellow;

        pinkLabel = R.color.LabelPink;
        pinkValue = R.color.ValuePink;

        return detailContainer;
    }

    @Override
    public void setUpDetails() {

        Individual individual = getIndividual(navigateActivity.getCurrentSelection().getExtId());

        List<Membership> memberships = getMemberships(individual.getExtId());

        LinearLayout personalInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_personal_info);

        LinearLayout contactInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_contact_info);

        LinearLayout membershipInfoContainer =
                (LinearLayout) detailContainer.findViewById(R.id.individual_detail_frag_membership_info);

        personalInfoContainer.removeAllViews();
        contactInfoContainer.removeAllViews();
        membershipInfoContainer.removeAllViews();

        // Draw extId
        TextView extIdTextView = (TextView) detailContainer.findViewById(R.id.individual_detail_frag_extid);
        extIdTextView.setText(individual.getExtId());

        // Name
        personalInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_full_name_label,
                individual.getFirstName() + " " + individual.getLastName(),
                greenLabel, greenValue, R.color.NA_Gray));
        // Other names
        personalInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_names_label,
                individual.getOtherNames(),
                greenLabel, greenValue, R.color.NA_Gray));

        personalInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.gender_lbl,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getGender())),
                greenLabel, greenValue, R.color.NA_Gray));

        // Language Preference
        personalInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_language_preference_label,
                getString(ProjectResources.Individual.getIndividualStringId(individual.getLanguagePreference())),
                greenLabel, greenValue, R.color.NA_Gray));

        // age and birthday
        personalInfoContainer .addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_age_label,
                Individual.getAgeWithUnits(individual),
                greenLabel, greenValue, R.color.NA_Gray));

        personalInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_date_of_birth_label,
                individual.getDob(),
                greenLabel, greenValue, R.color.NA_Gray));

        // Contact Info
        contactInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_personal_phone_number_label,
                individual.getPhoneNumber(),
                pinkLabel, pinkValue, R.color.NA_Gray));
        contactInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_other_phone_number_label,
                individual.getOtherPhoneNumber(),
                pinkLabel, pinkValue, R.color.NA_Gray));
        contactInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_label,
                individual.getPointOfContactName(),
                pinkLabel, pinkValue, R.color.NA_Gray));
        contactInfoContainer.addView(makeTextWithValueAndLabel(getActivity(),
                R.string.individual_point_of_contact_phone_number_label,
                individual.getPointOfContactPhoneNumber(),
                pinkLabel, pinkValue, R.color.NA_Gray));

        // Memberships
        for (Membership membership : memberships) {

            membershipInfoContainer.addView(makeTextWithValueAndLabel(
                    getActivity(),
                    R.string.individual_relationship_to_head_label,
                    getString(ProjectResources.Relationship.getRelationshipStringId(membership.getRelationshipToHead())),
                    yellowLabel, yellowValue, R.color.NA_Gray));

            membershipInfoContainer.addView(makeTextWithValueAndLabel(
                    getActivity(),
                    R.string.individual_socialgroup_extid_label,
                    membership.getSocialGroupExtId(),
                    yellowLabel, yellowValue, R.color.NA_Gray));
        }
    }

    private Individual getIndividual(String extId) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        Individual individual = individualGateway.getFirst(contentResolver, individualGateway.findById(extId));

        return individual;
    }

    private List<Membership> getMemberships(String individualExtId) {
        MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        List<Membership> memberships = membershipGateway.getList(contentResolver,
                membershipGateway.findByIndividual(individualExtId));

        return memberships;
    }
}
