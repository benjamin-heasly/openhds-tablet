package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.core.Residency;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Residencies.
 */
public class ResidencyParser extends EntityParser<Residency> {

    private static final String pageName = "residencies";

    @Override
    protected Residency toEntity(DataPage dataPage) {
        Residency residency = new Residency();

        residency.setUuid(dataPage.getFirstString(asList("uuid")));
        residency.setIndividualUuid(dataPage.getFirstString(asList("individual", "uuid")));
        residency.setLocationUuid(dataPage.getFirstString(asList("location", "uuid")));
        residency.setEndType(dataPage.getFirstString(asList("endType")));

        return residency;
    }
}
