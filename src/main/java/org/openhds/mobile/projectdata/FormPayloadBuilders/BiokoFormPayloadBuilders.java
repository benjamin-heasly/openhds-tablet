package org.openhds.mobile.projectdata.FormPayloadBuilders;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

/**
 * Created by motech on 7/31/14.
 */
public class BiokoFormPayloadBuilders {

    public static class DistributeBednets implements FormPayloadBuilder {

        @Override
        public void buildFormPayload(Map<String, String> formPayload,
                                     NavigateActivity navigateActivity) {

            PayloadTools.addMinimalFormPayload(formPayload, navigateActivity);

            //rename the current datetime to be the bednet's distribution datetime
            String distributionDateTime = formPayload.get(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.remove(ProjectFormFields.General.COLLECTION_DATE_TIME);
            formPayload.put(ProjectFormFields.General.DISTRIBUTION_DATE_TIME, distributionDateTime);

            String locationExtId = navigateActivity.getHierarchyPath()
                    .get(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE).getExtId();
            formPayload.put(ProjectFormFields.Locations.LOCATION_EXTID, locationExtId);

        }

    }

}
