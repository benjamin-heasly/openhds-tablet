package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Relationship;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Relationships.
 */
public class RelationshipParser extends EntityParser<Relationship> {

    private static final String pageName = "relationship";

    @Override
    protected Relationship toEntity(DataPage dataPage) {
        Relationship relationship = new Relationship();

        relationship.setIndividualA(dataPage.getFirstString(asList("individualA", "uuid")));
        relationship.setIndividualB(dataPage.getFirstString(asList("individualB", "uuid")));
        relationship.setStartDate(dataPage.getFirstString(asList("startDate")));
        relationship.setType(dataPage.getFirstString(asList("relationshipType")));
        relationship.setUuid(dataPage.getFirstString(asList("uuid")));

        return relationship;
    }
}
