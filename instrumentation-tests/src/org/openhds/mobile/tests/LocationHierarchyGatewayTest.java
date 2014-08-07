package org.openhds.mobile.tests;

import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.repository.LocationHierarchyGateway;


public class LocationHierarchyGatewayTest extends GatewayTest<LocationHierarchy> {

    private LocationHierarchyGateway locationHierarchyGateway;

    public LocationHierarchyGatewayTest() {
        super(new LocationHierarchyGateway());
        this.locationHierarchyGateway = (LocationHierarchyGateway) this.gateway;
    }

    @Override
    protected LocationHierarchy makeTestEntity(String id, String name) {
        LocationHierarchy locationHierarchy = new LocationHierarchy();

        locationHierarchy.setExtId(id);
        locationHierarchy.setName(name);
        locationHierarchy.setLevel("HIERARCHY");
        locationHierarchy.setParent("PARENT");

        return locationHierarchy;
    }
}