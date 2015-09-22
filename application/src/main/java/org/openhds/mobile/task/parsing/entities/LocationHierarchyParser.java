package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to LocationHierarchies.
 */
public class LocationHierarchyParser extends EntityParser<LocationHierarchy> {

    private static final String pageName = "hierarchy";

    @Override
    protected LocationHierarchy toEntity(DataPage dataPage) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setUuid(dataPage.getFirstString(asList("uuid")));
        locationHierarchy.setExtId(dataPage.getFirstString(asList("extId")));
        locationHierarchy.setLevelUuid(dataPage.getFirstString(asList("level", "uuid")));
        locationHierarchy.setName(dataPage.getFirstString(asList("name")));
        locationHierarchy.setParentUuid(dataPage.getFirstString(asList("parent", "uuid")));

        return locationHierarchy;
    }
}
