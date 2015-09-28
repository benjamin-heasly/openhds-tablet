package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.User;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Users.
 */
public class UserParser extends EntityParser<User> {

    private static final String pageName = "user";

    @Override
    protected User toEntity(DataPage dataPage) {
        User user = new User();

        user.setPasswordHash(dataPage.getFirstString(asList("passwordHash")));
        user.setFirstName(dataPage.getFirstString(asList("firstName")));
        user.setLastName(dataPage.getFirstString(asList("lastName")));
        user.setUuid(dataPage.getFirstString(asList("uuid")));
        user.setUsername(dataPage.getFirstString(asList("username")));
        user.setLastModifiedServer(dataPage.getFirstString(asList("lastModifiedDate")));

        return user;
    }
}
