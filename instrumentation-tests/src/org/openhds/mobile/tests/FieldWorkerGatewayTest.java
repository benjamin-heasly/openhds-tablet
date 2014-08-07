package org.openhds.mobile.tests;

import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.repository.FieldWorkerGateway;


public class FieldWorkerGatewayTest extends GatewayTest<FieldWorker> {

    private FieldWorkerGateway fieldWorkerGateway;

    public FieldWorkerGatewayTest() {
        super(new FieldWorkerGateway());
        this.fieldWorkerGateway = (FieldWorkerGateway) this.gateway;
    }

    @Override
    protected FieldWorker makeTestEntity(String id, String name) {
        FieldWorker fieldWorker = new FieldWorker();

        fieldWorker.setExtId(id);
        fieldWorker.setFirstName("2000-01-01 00:00:00");
        fieldWorker.setLastName("LASTNAME");
        fieldWorker.setPassword("PASSWORD");

        return fieldWorker;
    }
}
