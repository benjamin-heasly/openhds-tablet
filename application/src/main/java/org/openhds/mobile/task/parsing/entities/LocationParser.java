package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Locations.
 */
public class LocationParser extends EntityParser<Location> {

    private static final String pageName = "location";

    @Override
    protected Location toEntity(DataPage dataPage) {
        Location location = new Location();

        location.setUuid(dataPage.getFirstString(asList("uuid")));
        location.setExtId(dataPage.getFirstString(asList("extId")));
        location.setHierarchyUuid(dataPage.getFirstString(asList("locationHierarchy", "uuid")));
        location.setLatitude(dataPage.getFirstString(asList("latitude")));
        location.setLongitude(dataPage.getFirstString(asList("longitude")));
        location.setName(dataPage.getFirstString(asList("name")));
        location.setLastModifiedServer(dataPage.getFirstString(asList("lastModifiedDate")));

        return location;
    }
}
