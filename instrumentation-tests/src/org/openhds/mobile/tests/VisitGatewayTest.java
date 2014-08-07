package org.openhds.mobile.tests;

import org.openhds.mobile.model.Visit;
import org.openhds.mobile.repository.VisitGateway;


public class VisitGatewayTest extends GatewayTest<Visit> {

    private VisitGateway visitGateway;

    public VisitGatewayTest() {
        super(new VisitGateway());
        this.visitGateway = (VisitGateway) this.gateway;
    }

    @Override
    protected Visit makeTestEntity(String id, String name) {
        Visit visit = new Visit();

        visit.setVisitExtId(id);
        visit.setVisitDate("2000-01-01 00:00:00");
        visit.setLocationExtId("LOCATION");
        visit.setFieldWorkerExtId("FIELD_WORKER");

        return visit;
    }
}
