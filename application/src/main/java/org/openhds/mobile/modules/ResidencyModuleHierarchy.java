package org.openhds.mobile.modules;

import android.content.ContentResolver;
import android.content.Context;

import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.R;
import org.openhds.mobile.model.core.LocationHierarchyLevel;
import org.openhds.mobile.model.core.Residency;
import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyLevelGateway;
import org.openhds.mobile.repository.gateway.ResidencyGateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Follow a variable number of LocationHierarchyLevels downloaded from the server, followed by
 * Locations, followed by Individuals resident at the locaitons.
 *
 * Created by ben on 10/12/15.
 */
public class ResidencyModuleHierarchy implements ModuleHierarchy {

    public final String LOCATION_LEVEL_ID = "location";
    public final String INDIVIDUAL_LEVEL_ID = "individual";

    private final List<String> levelSequence = new ArrayList<>();

    private final Map<String, String> levelLabels = new HashMap<>();

    @Override
    public void init(Context context) {

        // start fresh
        levelSequence.clear();
        levelLabels.clear();

        // start with LocationHierarchyLevels from the server
        LocationHierarchyLevelGateway locationHierarchyLevelGateway = GatewayRegistry.getLocationHierarchyLevelGateway();
        List<LocationHierarchyLevel> locationHierarchyLevels = locationHierarchyLevelGateway.getList(
                context.getContentResolver(),
                locationHierarchyLevelGateway.findAll(OpenHDS.LocationHierarchyLevels.KEY_IDENTIFIER));
        for (LocationHierarchyLevel locationHierarchyLevel : locationHierarchyLevels) {

            // ignore the unknown level
            if ("UNKNOWN".equals(locationHierarchyLevel.getUuid())) {
                continue;
            }

            String levelId = Integer.toString(locationHierarchyLevel.getKeyIdentifier());
            levelSequence.add(levelId);
            levelLabels.put(levelId, locationHierarchyLevel.getName());
        }

        // add Locations beneath the LocationHierarchy
        levelSequence.add(LOCATION_LEVEL_ID);
        levelLabels.put(LOCATION_LEVEL_ID, context.getResources().getString(R.string.household_label));

        // add Individuals beneath Locations, to be found by residency
        levelSequence.add(INDIVIDUAL_LEVEL_ID);
        levelLabels.put(INDIVIDUAL_LEVEL_ID, context.getResources().getString(R.string.individual_label));
    }

    @Override
    public String getName() {
        return ResidencyModuleHierarchy.class.getSimpleName();
    }

    @Override
    public Map<String, String> getLevelLabels() {
        return Collections.unmodifiableMap(levelLabels);
    }

    @Override
    public List<String> getLevelSequence() {
        return Collections.unmodifiableList(levelSequence);
    }

    @Override
    public List<DataWrapper> getAll(ContentResolver contentResolver, String levelId) {

        // all individuals
        if (INDIVIDUAL_LEVEL_ID.equals(levelId)) {
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            return individualGateway.getDataWrapperList(contentResolver, individualGateway.findAll(), INDIVIDUAL_LEVEL_ID);
            // TODO: sort by extId
        }

        // all locations
        if (LOCATION_LEVEL_ID.equals(levelId)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getDataWrapperList(contentResolver, locationGateway.findAll(), LOCATION_LEVEL_ID);
            // TODO: sort by extId
        }

        // all location hierarchies at the given level
        LocationHierarchyLevelGateway locationHierarchyLevelGateway = GatewayRegistry.getLocationHierarchyLevelGateway();
        LocationHierarchyLevel locationHierarchyLevel = locationHierarchyLevelGateway.getFirst(
                contentResolver,
                locationHierarchyLevelGateway.findByKeyIdentifier(levelId));

        if (null == locationHierarchyLevel) {
            return null;
        }

        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        return locationHierarchyGateway.getDataWrapperList(contentResolver, locationHierarchyGateway.findByLevel(locationHierarchyLevel.getUuid()), levelId);
    }

    @Override
    public List<DataWrapper> getChildren(ContentResolver contentResolver, DataWrapper dataWrapper) {

        final String levelId = dataWrapper.getLevel();

        // individuals are the bottom of the hierarchy
        if (INDIVIDUAL_LEVEL_ID.equals(levelId)) {
            return null;
        }

        // find individuals by residency at the given location
        if (LOCATION_LEVEL_ID.equals(levelId)) {
            // TODO: optimize multiple queries as one join query

            // residencies at given location
            ResidencyGateway residencyGateway = GatewayRegistry.getResidencyGateway();
            List<Residency> residencies = residencyGateway.getList(contentResolver, residencyGateway.findByLocation(dataWrapper.getUuid()));

            if (null == residencies || residencies.isEmpty()) {
                return null;
            }

            // individual from each residency
            IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
            List<DataWrapper> individuals = new ArrayList<>(residencies.size());
            for (Residency residency : residencies) {
                individuals.add(individualGateway.getFirstDataWrapper(
                        contentResolver,
                        individualGateway.findById(residency.getIndividualUuid()), INDIVIDUAL_LEVEL_ID));
            }
            return individuals;
            // TODO: sort by extId
        }

        // find locations after the lowest level of location hierarchy
        if (lastLocationHierarchyLevelId().equals(levelId)) {
            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
            return locationGateway.getDataWrapperList(
                    contentResolver,
                    locationGateway.findByHierarchy(dataWrapper.getUuid()),
                    LOCATION_LEVEL_ID);
            // TODO: sort by extId
        }


        // find location hierarchy with the given parent
        String childLevelId = subsequentLevelId(levelId);
        LocationHierarchyGateway locationHierarchyGateway = GatewayRegistry.getLocationHierarchyGateway();
        return locationHierarchyGateway.getDataWrapperList(
                contentResolver,
                locationHierarchyGateway.findByParent(dataWrapper.getUuid()),
                childLevelId);
        // TODO: sort by extId
    }

    // the level just above locations
    private String lastLocationHierarchyLevelId() {
        return levelSequence.get(levelSequence.size() - 3);
    }

    // the next level after the given level
    private String subsequentLevelId(String levelId) {
        Iterator<String> levelIterator = levelSequence.iterator();
        while (levelIterator.hasNext()) {
            if (levelIterator.next().equals(levelId)) {
                break;
            }
        }

        if (levelIterator.hasNext()) {
            return levelIterator.next();
        }

        // levelId was invalid, or last in the sequence
        return null;
    }
}
