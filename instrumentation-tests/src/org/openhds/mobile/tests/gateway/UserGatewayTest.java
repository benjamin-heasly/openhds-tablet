package org.openhds.mobile.tests.gateway;

import org.openhds.mobile.model.core.User;
import org.openhds.mobile.repository.gateway.UserGateway;


public class UserGatewayTest extends GatewayTest<User> {

    private UserGateway userGateway;

    public UserGatewayTest() {
        super(new UserGateway());
        this.userGateway = (UserGateway) this.gateway;
    }

    @Override
    protected User makeTestEntity(String id, String name) {
        User user = new User();

        user.setUuid(id);
        user.setUsername(id);
        user.setFirstName(name);
        user.setLastName("LASTNAME");
        user.setPasswordHash("PASSWORD_HASH");

        return user;
    }
}
