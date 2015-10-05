package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.Residency;
import org.openhds.mobile.repository.gateway.ResidencyGateway;


public class ResidencyGatewayTest extends GatewayTest<Residency> {

    private ResidencyGateway residencyGateway;

    public ResidencyGatewayTest() {
        super(new ResidencyGateway());
        this.residencyGateway = (ResidencyGateway) this.gateway;
    }

    @Override
    protected Residency makeTestEntity(String id, String name, String modificationDate) {
        Residency residency = new Residency();

        residency.setUuid(id);
        residency.setIndividualUuid(id);
        residency.setLocationUuid("LOCATION");
        residency.setLastModifiedServer(modificationDate);
        residency.setLastModifiedClient(modificationDate);

        return residency;
    }
}
