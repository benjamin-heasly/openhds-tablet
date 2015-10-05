package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.LocationHierarchyLevel;
import org.openhds.mobile.repository.gateway.LocationHierarchyLevelGateway;


public class LocationHierarchyLevelGatewayTest extends GatewayTest<LocationHierarchyLevel> {

    private LocationHierarchyLevelGateway locationHierarchyLevelGateway;

    public LocationHierarchyLevelGatewayTest() {
        super(new LocationHierarchyLevelGateway());
        this.locationHierarchyLevelGateway = (LocationHierarchyLevelGateway) this.gateway;
    }

    @Override
    protected LocationHierarchyLevel makeTestEntity(String id, String name, String modificationDate) {
        LocationHierarchyLevel locationHierarchyLevel = new LocationHierarchyLevel();

        locationHierarchyLevel.setUuid(id);
        locationHierarchyLevel.setName(name);
        locationHierarchyLevel.setKeyIdentifier(name.hashCode());
        locationHierarchyLevel.setLastModifiedServer(modificationDate);
        locationHierarchyLevel.setLastModifiedClient(modificationDate);

        return locationHierarchyLevel;
    }
}
