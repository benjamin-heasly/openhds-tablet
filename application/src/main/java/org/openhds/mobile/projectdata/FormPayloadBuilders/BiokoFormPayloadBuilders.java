package org.openhds.mobile.projectdata.FormPayloadBuilders;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;

import java.util.Calendar;
import java.util.Map;

public class BiokoFormPayloadBuilders {

    public static class DistributeBednets implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);
            PayloadTools.flagForReview(formPayload, false);

            //rename the current datetime to be the bednet's distribution datetime
            String distributionDateTime = formPayload.get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.remove(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.put(ProjectFormFields.General.DISTRIBUTION_DATE_TIME, distributionDateTime);

            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE).getExtId();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(locationExtId));

            String communityCode = location.getCommunityCode();
            String yearPrefix = Integer.toString (Calendar.getInstance().get(Calendar.YEAR));
            yearPrefix = yearPrefix.substring(2);
            String netCode = yearPrefix + "-" + communityCode;

            formPayload.put(ProjectFormFields.BedNet.BED_NET_CODE, netCode);
        }

    }

}
