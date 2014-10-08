package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.SocialGroup;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to SocialGroups.
 */
public class SocialGroupParser extends EntityParser<SocialGroup> {

    private static final String pageName = "socialgroup";

    @Override
    protected SocialGroup toEntity(DataPage dataPage) {
        SocialGroup socialGroup = new SocialGroup();

        socialGroup.setExtId(dataPage.getFirstString(asList(pageName, "extId")));
        socialGroup.setGroupHead(dataPage.getFirstString(asList(pageName, "groupHead", "extId")));
        socialGroup.setGroupName(dataPage.getFirstString(asList(pageName, "groupName")));

        return socialGroup;
    }
}
