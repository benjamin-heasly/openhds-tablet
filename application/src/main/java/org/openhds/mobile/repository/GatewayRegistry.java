package org.openhds.mobile.repository;

import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyLevelGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.ResidencyGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.UserGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Well know location for accessing entity-specific gateway instances.
 */
public class GatewayRegistry {

    private static Map<String, Gateway> allGateways;

    static {
        allGateways = new HashMap<>();

        allGateways.put(FieldWorkerGateway.class.getSimpleName(), new FieldWorkerGateway());
        allGateways.put(UserGateway.class.getSimpleName(), new UserGateway());
        allGateways.put(IndividualGateway.class.getSimpleName(), new IndividualGateway());
        allGateways.put(LocationGateway.class.getSimpleName(), new LocationGateway());
        allGateways.put(LocationHierarchyGateway.class.getSimpleName(), new LocationHierarchyGateway());
        allGateways.put(LocationHierarchyLevelGateway.class.getSimpleName(), new LocationHierarchyLevelGateway());
        allGateways.put(MembershipGateway.class.getSimpleName(), new MembershipGateway());
        allGateways.put(ResidencyGateway.class.getSimpleName(), new ResidencyGateway());
        allGateways.put(RelationshipGateway.class.getSimpleName(), new RelationshipGateway());
        allGateways.put(SocialGroupGateway.class.getSimpleName(), new SocialGroupGateway());
        allGateways.put(VisitGateway.class.getSimpleName(), new VisitGateway());
    }

    private GatewayRegistry () {};

    public static FieldWorkerGateway getFieldWorkerGateway() {
        return (FieldWorkerGateway) allGateways.get(FieldWorkerGateway.class.getSimpleName());
    }

    public static UserGateway getUsesrGateway() {
        return (UserGateway) allGateways.get(UserGateway.class.getSimpleName());
    }

    public static IndividualGateway getIndividualGateway() {
        return (IndividualGateway) allGateways.get(IndividualGateway.class.getSimpleName());
    }

    public static LocationGateway getLocationGateway() {
        return (LocationGateway) allGateways.get(LocationGateway.class.getSimpleName());
    }

    public static LocationHierarchyGateway getLocationHierarchyGateway() {
        return (LocationHierarchyGateway) allGateways.get(LocationHierarchyGateway.class.getSimpleName());
    }

    public static LocationHierarchyLevelGateway getLocationHierarchyLevelGateway() {
        return (LocationHierarchyLevelGateway) allGateways.get(LocationHierarchyLevelGateway.class.getSimpleName());
    }

    public static MembershipGateway getMembershipGateway() {
        return (MembershipGateway) allGateways.get(MembershipGateway.class.getSimpleName());
    }

    public static ResidencyGateway getResidencyGateway() {
        return (ResidencyGateway) allGateways.get(ResidencyGateway.class.getSimpleName());
    }

    public static RelationshipGateway getRelationshipGateway() {
        return (RelationshipGateway) allGateways.get(RelationshipGateway.class.getSimpleName());
    }

    public static SocialGroupGateway getSocialGroupGateway() {
        return (SocialGroupGateway) allGateways.get(SocialGroupGateway.class.getSimpleName());
    }

    public static VisitGateway getVisitGateway() {
        return (VisitGateway) allGateways.get(VisitGateway.class.getSimpleName());
    }

    public static Gateway getGatewayByName(String gatewayName) {
        if (allGateways.containsKey(gatewayName)) {
            return allGateways.get(gatewayName);
        }
        return null;
    }
}
