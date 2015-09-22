package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.LocationHierarchyLevel;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to LocationHierarchyLevels.
 */
public class LocationHierarchyLevelParser extends EntityParser<LocationHierarchyLevel> {

    private static final String pageName = "locationHierarchyLevel";

    @Override
    protected LocationHierarchyLevel toEntity(DataPage dataPage) {
        LocationHierarchyLevel locationHierarchyLevel = new LocationHierarchyLevel();

        locationHierarchyLevel.setUuid(dataPage.getFirstString(asList("uuid")));
        locationHierarchyLevel.setName(dataPage.getFirstString(asList("name")));
        locationHierarchyLevel.setKeyIdentifier(dataPage.getFirstInt(asList("keyIdentifier")));

        return locationHierarchyLevel;
    }
}
