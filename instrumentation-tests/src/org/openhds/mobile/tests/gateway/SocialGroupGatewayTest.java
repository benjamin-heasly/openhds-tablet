package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;


public class SocialGroupGatewayTest extends GatewayTest<SocialGroup> {

    private SocialGroupGateway socialGroupGateway;

    public SocialGroupGatewayTest() {
        super(new SocialGroupGateway());
        this.socialGroupGateway = (SocialGroupGateway) this.gateway;
    }

    @Override
    protected SocialGroup makeTestEntity(String id, String name) {
        SocialGroup socialGroup = new SocialGroup();

        socialGroup.setExtId(id);
        socialGroup.setGroupName(name);
        socialGroup.setGroupHead("HEAD");

        return socialGroup;
    }
}
