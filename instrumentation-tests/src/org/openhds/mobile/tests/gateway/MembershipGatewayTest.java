package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.Membership;
import org.openhds.mobile.repository.gateway.MembershipGateway;


public class MembershipGatewayTest extends GatewayTest<Membership> {

    private MembershipGateway membershipGateway;

    public MembershipGatewayTest() {
        super(new MembershipGateway());
        this.membershipGateway = (MembershipGateway) this.gateway;
    }

    @Override
    protected Membership makeTestEntity(String id, String name) {
        Membership membership = new Membership();

        membership.setIndividualExtId(id);
        membership.setSocialGroupExtId("SOCIALGROUP");
        membership.setRelationshipToHead("RELATIONSHIPTOHEAD");

        return membership;
    }
}
