package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.update.Visit;
import org.openhds.mobile.task.parsing.DataPage;

import static java.util.Arrays.asList;

/**
 * Convert DataPages to Visits.
 */
public class VisitParser extends EntityParser<Visit> {

    private static final String pageName = "visit";

    @Override
    protected Visit toEntity(DataPage dataPage) {
        Visit visit = new Visit();

        visit.setExtId(dataPage.getFirstString(asList( "extId")));
        visit.setUuid(dataPage.getFirstString(asList("uuid")));
        visit.setLocationUuid(dataPage.getFirstString(asList("visitLocation", "uuid")));
        visit.setVisitDate(dataPage.getFirstString(asList("visitDate")));
        visit.setFieldWorkerUuid(dataPage.getFirstString(asList("collectedBy", "uuid")));
        visit.setLastModifiedServer(dataPage.getFirstString(asList("lastModifiedDate")));

        return visit;
    }
}
