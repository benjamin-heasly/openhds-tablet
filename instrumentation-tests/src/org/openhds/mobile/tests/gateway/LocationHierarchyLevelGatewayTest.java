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
    protected LocationHierarchyLevel makeTestEntity(String id, String name) {
        LocationHierarchyLevel locationHierarchy = new LocationHierarchyLevel();

        locationHierarchy.setUuid(id);
        locationHierarchy.setName(name);
        locationHierarchy.setKeyIdentifier(name.hashCode());

        return locationHierarchy;
    }
}
