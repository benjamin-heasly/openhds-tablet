package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.Membership;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Memberships.
 */
public class MembershipParser extends EntityParser<Membership> {

    private static final String pageName = "membership";

    @Override
    protected Membership toEntity(DataPage dataPage) {
        Membership membership = new Membership();

        membership.setIndividualExtId(dataPage.getFirstString(asList(pageName, "individual", "extId")));
        membership.setRelationshipToHead(dataPage.getFirstString(asList(pageName, "bIsToA")));
        membership.setSocialGroupExtId(dataPage.getFirstString(asList(pageName, "socialGroup", "extId")));

        return membership;
    }
}
