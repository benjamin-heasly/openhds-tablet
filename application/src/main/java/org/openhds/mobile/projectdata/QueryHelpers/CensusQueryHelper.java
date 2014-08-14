package org.openhds.mobile.projectdata.QueryHelpers;

import android.content.ContentResolver;
import org.openhds.mobile.R;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.LocationHierarchy;
import org.openhds.mobile.model.Membership;
import org.openhds.mobile.projectdata.ProjectActivityBuilder;
import org.openhds.mobile.projectdata.ProjectResources;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;

import java.util.ArrayList;
import java.util.List;

public class CensusQueryHelper implements QueryHelper {

    // These must match the server data.
    // They come from the name column of the locationhierarchylevel table
    public static final String REGION_HIERARCHY_LEVEL_NAME = "Region";
    public static final String PROVINCE_HIERARCHY_LEVEL_NAME = "Province";
    public static final String DISTRICT_HIERARCHY_LEVEL_NAME = "District";
    public static final String SUB_DISTRICT_HIERARCHY_LEVEL_NAME = "SubDistrict";
    public static final String LOCALITY_HIERARCHY_LEVEL_NAME = "Locality";
    public static final String MAP_AREA_HIERARCHY_LEVEL_NAME = "MapArea";
    public static final String SECTOR_HIERARCHY_LEVEL_NAME = "Sector";

    public CensusQueryHelper() {}

    public List<QueryResult> getAll(ContentResolver contentResolver, String state) {

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(REGION_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(PROVINCE_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(DISTRICT_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(SUB_DISTRICT_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(LOCALITY_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(MAP_AREA_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByLevel(SECTOR_HIERARCHY_LEVEL_NAME));
            return getLocationHierarchyQueryResultList(hierarchyIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            List<Location> locationIterator = locationGateway.getList(contentResolver, locationGateway.findAll());
            return getLocationQueryResultList(locationIterator, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            List<Individual> individualIterator = individualGateway.getList(contentResolver, individualGateway.findAll());
            return getIndividualQueryResultList(individualIterator, state, contentResolver);
        }

        return new ArrayList<QueryResult>();
    }

    public QueryResult getIfExists(ContentResolver contentResolver, String state, String extId) {

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {

            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            LocationHierarchy locationHierarchy = locationHierarchyGateway.getFirst(contentResolver,
                    locationHierarchyGateway.findById(extId));
            return getQueryResult(locationHierarchy, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            Location location = locationGateway.getFirst(contentResolver, locationGateway.findById(extId));
            return getQueryResult(location, state, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.INDIVIDUAL_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            Individual individual = individualGateway.getFirst(contentResolver, individualGateway.findById(extId));
            return getQueryResult(individual, state, contentResolver);
        }

        return null;
    }

    public List<QueryResult> getChildren(ContentResolver contentResolver,
                                         QueryResult qr, String childState) {
        String state = qr.getState();

        if (state.equals(ProjectActivityBuilder.CensusActivityModule.REGION_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.PROVINCE_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.SUB_DISTRICT_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.LOCALITY_STATE)
                || state.equals(ProjectActivityBuilder.CensusActivityModule.MAP_AREA_STATE)) {

            LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
            List<LocationHierarchy> hierarchyIterator = locationHierarchyGateway.getList(contentResolver,
                    locationHierarchyGateway.findByParent(qr.getExtId()));
            return getLocationHierarchyQueryResultList(hierarchyIterator, childState, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.SECTOR_STATE)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            List<Location> locationIterator = locationGateway.getList(contentResolver,
                    locationGateway.findByHierarchyDescendingBuildingNumber(qr.getExtId()));
            return getLocationQueryResultList(locationIterator, childState, contentResolver);

        } else if (state.equals(ProjectActivityBuilder.CensusActivityModule.HOUSEHOLD_STATE)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            List<Individual> individualIterator = individualGateway.getList(contentResolver,
                    individualGateway.findByResidency(qr.getExtId()));
            return getIndividualQueryResultList(individualIterator, childState, contentResolver);
        }

        return new ArrayList<QueryResult>();
    }

    private static QueryResult getQueryResult(LocationHierarchy locationHierarchy, String state, ContentResolver contentResolver) {

        if (null == locationHierarchy) {
            return null;
        }

        QueryResult qr = new QueryResult();
        qr.setExtId(locationHierarchy.getExtId());
        qr.setName(locationHierarchy.getName());
        qr.setState(state);

        return qr;
    }

    private static QueryResult getQueryResult(Location location, String state, ContentResolver contentResolver) {

        if (null == location) {
            return null;
        }

        QueryResult qr = new QueryResult();
        qr.setExtId(location.getExtId());
        qr.setName(location.getName());
        qr.setState(state);

        return qr;
    }

    private static QueryResult getQueryResult(Individual individual, String state, ContentResolver contentResolver) {

        if (null == individual) {
            return null;
        }

        QueryResult qr = new QueryResult();
        qr.setExtId(individual.getExtId());
        qr.setExtId(individual.getExtId());
        qr.setName(Individual.getFullName(individual));
        qr.setState(state);

        // add individual details to payload
        qr.getStringsPayLoad().put(R.string.individual_other_names_label, individual.getOtherNames());
        qr.getStringsPayLoad().put(R.string.individual_age_label, Individual.getAgeWithUnits(individual));
        qr.getStringsPayLoad().put(R.string.individual_language_preference_label, individual.getLanguagePreference());

        // add household membership details to payload
        MembershipGateway membershipGateway = GatewayRegistry.getMembershipGateway();
        Membership membership = membershipGateway.getFirst(contentResolver,
                membershipGateway.findBySocialGroupAndIndividual(individual.getCurrentResidence(), individual.getExtId()));
        int relationshipId =  ProjectResources.Relationship.getRelationshipStringId(membership.getRelationshipToHead());
        qr.getStringIdsPayLoad().put(R.string.individual_relationship_to_head_label, relationshipId);

        return qr;
    }

    private static List<QueryResult> getLocationHierarchyQueryResultList(List<LocationHierarchy> entities, String state, ContentResolver contentResolver) {
        List<QueryResult> results = new ArrayList<QueryResult>();

        if (null == entities) {
            return results;
        }

        for (LocationHierarchy locationHierarchy : entities) {
            results.add(getQueryResult(locationHierarchy, state, contentResolver));
        }

        return results;
    }

    private static List<QueryResult> getLocationQueryResultList(List<Location> entities, String state, ContentResolver contentResolver) {
        List<QueryResult> results = new ArrayList<QueryResult>();

        if (null == entities) {
            return results;
        }

        for (Location location : entities) {
            results.add(getQueryResult(location, state, contentResolver));
        }

        return results;
    }

    private static List<QueryResult> getIndividualQueryResultList(List<Individual> entities, String state, ContentResolver contentResolver) {
        List<QueryResult> results = new ArrayList<QueryResult>();

        if (null == entities) {
            return results;
        }

        for (Individual individual : entities) {
            results.add(getQueryResult(individual, state, contentResolver));
        }

        return results;
    }
}
