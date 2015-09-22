package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Visits.EXT_ID;
import static org.openhds.mobile.OpenHDS.Visits.UUID;
import static org.openhds.mobile.OpenHDS.Visits.DATE;
import static org.openhds.mobile.OpenHDS.Visits.FIELD_WORKER_UUID;
import static org.openhds.mobile.OpenHDS.Visits.LOCATION_UUID;

public class VisitFormAdapter {

    public static Visit fromForm(Map<String, String> formInstanceData){
        Visit visit = new Visit();

        visit.setUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(UUID)));
        visit.setExtId(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(EXT_ID)));
        visit.setVisitDate(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(DATE)));
        visit.setFieldWorkerUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(FIELD_WORKER_UUID)));
        visit.setLocationUuid(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(LOCATION_UUID)));

        return visit;
    }
}
