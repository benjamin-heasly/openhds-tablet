package org.openhds.mobile.projectdata.FormAdapters;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.projectdata.ProjectFormFields;

import java.util.Map;

import static org.openhds.mobile.OpenHDS.Locations.*;

public class LocationFormAdapter {

    public static Location fromForm(Map<String, String> formInstanceData) {
        Location location = new Location();


        location.setUuid(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(UUID)));
        location.setExtId(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(EXT_ID)));
        location.setName(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(NAME)));
        location.setHierarchyUuid(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(LOCATION_HIERARCHY_UUID)));

        location.setLongitude(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(LONGITUDE)));
        location.setLatitude(formInstanceData.get(
                ProjectFormFields.Locations.getFieldNameFromColumn(LATITUDE)));

        return location;
    }
}
