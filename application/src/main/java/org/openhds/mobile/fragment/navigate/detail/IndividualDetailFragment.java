package org.openhds.mobile.fragment.navigate.detail;

import android.content.ContentResolver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openhds.mobile.R;
import org.openhds.mobile.model.core.Individual;
import org.openhds.mobile.model.core.Membership;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;

import java.util.List;

import static org.openhds.mobile.utilities.LayoutUtils.makeLargeTextWithValueAndLabel;

public class IndividualDetailFragment extends DetailFragment {

    ScrollView detailContainer;
    int labelColor;
    int valueColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        detailContainer = (ScrollView) inflater.inflate(R.layout.individual_detail_fragment, container, false);

        labelColor = R.color.GrayButtonBorder;
        valueColor = R.color.GrayButtonFillPressed;

        return detailContainer;
    }

    @Override
    public void setUpDetails() {

        Individual individual = getIndividual(navigateActivity.getCurrentSelection().getUuid());

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
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                getActivity().getString(R.string.individual_full_name_label),
                individual.getFirstName() + " " + individual.getLastName(),
                labelColor, valueColor, R.color.NA_Gray));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                getActivity().getString(R.string.gender_lbl),
                individual.getGender(),
                labelColor, valueColor, R.color.NA_Gray));

        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                getActivity().getString(R.string.individual_date_of_birth_label),
                individual.getDob(),
                labelColor, valueColor, R.color.NA_Gray));

        //UUID
        personalInfoContainer.addView(makeLargeTextWithValueAndLabel(getActivity(),
                getActivity().getString( R.string.uuid),
                individual.getUuid(),
                labelColor, valueColor, R.color.NA_Gray));

    }

    private Individual getIndividual(String uuid) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        ContentResolver contentResolver = navigateActivity.getContentResolver();
        Individual individual = individualGateway.getFirst(contentResolver, individualGateway.findById(uuid));

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
