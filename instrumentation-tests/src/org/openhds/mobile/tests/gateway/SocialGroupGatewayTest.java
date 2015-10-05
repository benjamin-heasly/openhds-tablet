package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;


public class SocialGroupGatewayTest extends GatewayTest<SocialGroup> {

    private SocialGroupGateway socialGroupGateway;

    public SocialGroupGatewayTest() {
        super(new SocialGroupGateway());
        this.socialGroupGateway = (SocialGroupGateway) this.gateway;
    }

    @Override
    protected SocialGroup makeTestEntity(String id, String name, String modificationDate) {
        SocialGroup socialGroup = new SocialGroup();

        socialGroup.setUuid(id);
        socialGroup.setGroupName(name);
        socialGroup.setExtId(name);
        socialGroup.setLastModifiedServer(modificationDate);
        socialGroup.setLastModifiedClient(modificationDate);

        return socialGroup;
    }
}
