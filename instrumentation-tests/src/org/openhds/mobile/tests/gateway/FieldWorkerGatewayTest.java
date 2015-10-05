package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.FieldWorker;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;


public class FieldWorkerGatewayTest extends GatewayTest<FieldWorker> {

    private FieldWorkerGateway fieldWorkerGateway;

    public FieldWorkerGatewayTest() {
        super(new FieldWorkerGateway());
        this.fieldWorkerGateway = (FieldWorkerGateway) this.gateway;
    }

    @Override
    protected FieldWorker makeTestEntity(String id, String name, String modificationDate) {
        FieldWorker fieldWorker = new FieldWorker();

        fieldWorker.setUuid(id);
        fieldWorker.setFieldWorkerId(id);
        fieldWorker.setFirstName(name);
        fieldWorker.setLastName("LASTNAME");
        fieldWorker.setPasswordHash("PASSWORD_HASH");
        fieldWorker.setLastModifiedServer(modificationDate);
        fieldWorker.setLastModifiedClient(modificationDate);

        return fieldWorker;
    }
}
