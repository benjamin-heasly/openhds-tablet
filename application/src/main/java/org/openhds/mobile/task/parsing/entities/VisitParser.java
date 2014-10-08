package org.openhds.mobile.task.parsing.entities;

import org.openhds.mobile.model.Visit;
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

        visit.setFieldWorkerExtId(dataPage.getFirstString(asList(pageName, "extId")));
        visit.setLocationExtId(dataPage.getFirstString(asList(pageName, "visitLocation", "extId")));
        visit.setVisitDate(dataPage.getFirstString(asList(pageName, "visitDate")));
        visit.setVisitExtId(dataPage.getFirstString(asList(pageName, "collectedBy", "extId")));

        return visit;
    }
}
