package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.repository.gateway.RelationshipGateway;


public class RelationshipGatewayTest extends GatewayTest<Relationship> {

    private RelationshipGateway relationshipGateway;

    public RelationshipGatewayTest() {
        super(new RelationshipGateway());
        this.relationshipGateway = (RelationshipGateway) this.gateway;
    }

    @Override
    protected Relationship makeTestEntity(String id, String name, String modificationDate) {
        Relationship relationship = new Relationship();

        relationship.setUuid(id);
        relationship.setIndividualA(id);
        relationship.setIndividualB("INDIVIDUALB");
        relationship.setStartDate("2000-01-01 00:00:00");
        relationship.setType("TYPE");
        relationship.setLastModifiedServer(modificationDate);
        relationship.setLastModifiedClient(modificationDate);

        return relationship;
    }
}
