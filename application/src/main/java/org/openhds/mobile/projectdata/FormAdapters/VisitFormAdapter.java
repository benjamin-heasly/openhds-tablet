package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_DATE;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_FIELDWORKER_EXTID;
import static org.openhds.mobile.OpenHDS.Visits.COLUMN_VISIT_LOCATION_EXTID;

public class VisitFormAdapter {

    public static Visit fromForm(Map<String, String> formInstanceData){
        Visit visit = new Visit();

        visit.setVisitExtId(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_EXTID)));
        visit.setVisitDate(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_DATE)));
        visit.setFieldWorkerExtId(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_FIELDWORKER_EXTID)));
        visit.setLocationExtId(formInstanceData.get(
                ProjectFormFields.Visits.getFieldNameFromColumn(COLUMN_VISIT_LOCATION_EXTID)));

        return visit;
    }
}
