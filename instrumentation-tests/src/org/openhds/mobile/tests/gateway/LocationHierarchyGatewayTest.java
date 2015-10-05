package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.LocationHierarchy;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;


public class LocationHierarchyGatewayTest extends GatewayTest<LocationHierarchy> {

    private LocationHierarchyGateway locationHierarchyGateway;

    public LocationHierarchyGatewayTest() {
        super(new LocationHierarchyGateway());
        this.locationHierarchyGateway = (LocationHierarchyGateway) this.gateway;
    }

    @Override
    protected LocationHierarchy makeTestEntity(String id, String name, String modificationDate) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setUuid(id);
        locationHierarchy.setExtId(id);
        locationHierarchy.setName(name);
        locationHierarchy.setLevelUuid("LEVEL");
        locationHierarchy.setParentUuid("PARENT");
        locationHierarchy.setLastModifiedServer(modificationDate);
        locationHierarchy.setLastModifiedClient(modificationDate);

        return locationHierarchy;
    }
}
