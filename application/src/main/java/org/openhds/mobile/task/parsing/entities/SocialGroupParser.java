package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.SocialGroup;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to SocialGroups.
 */
public class SocialGroupParser extends EntityParser<SocialGroup> {

    @Override
    protected SocialGroup toEntity(DataPage dataPage) {
        SocialGroup socialGroup = new SocialGroup();

        socialGroup.setUuid(dataPage.getFirstString(asList("uuid")));
        socialGroup.setGroupHeadUuid(dataPage.getFirstString(asList("groupHead", "uuid")));
        socialGroup.setExtId(dataPage.getFirstString(asList("extId")));
        socialGroup.setGroupName(dataPage.getFirstString(asList("groupName")));

        return socialGroup;
    }
}
