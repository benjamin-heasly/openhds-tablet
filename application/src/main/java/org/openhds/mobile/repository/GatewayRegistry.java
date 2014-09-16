package org.openhds.mobile.repository;

import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
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
    }

    private GatewayRegistry () {};

    public static FieldWorkerGateway getFieldWorkerGateway() {
        final String gatewayName = FieldWorkerGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new FieldWorkerGateway());
        }
        return (FieldWorkerGateway) allGateways.get(gatewayName);
    }

    public static IndividualGateway getIndividualGateway() {
        final String gatewayName = IndividualGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new IndividualGateway());
        }
        return (IndividualGateway) allGateways.get(gatewayName);
    }

    public static LocationGateway getLocationGateway() {
        final String gatewayName = LocationGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new LocationGateway());
        }
        return (LocationGateway) allGateways.get(gatewayName);
    }

    public static LocationHierarchyGateway getLocationHierarchyGateway() {
        final String gatewayName = LocationHierarchyGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new LocationHierarchyGateway());
        }
        return (LocationHierarchyGateway) allGateways.get(gatewayName);
    }

    public static MembershipGateway getMembershipGateway() {
        final String gatewayName = MembershipGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new MembershipGateway());
        }
        return (MembershipGateway) allGateways.get(gatewayName);
    }

    public static RelationshipGateway getRelationshipGateway() {
        final String gatewayName = RelationshipGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new RelationshipGateway());
        }
        return (RelationshipGateway) allGateways.get(gatewayName);
    }

    public static SocialGroupGateway getSocialGroupGateway() {
        final String gatewayName = SocialGroupGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new SocialGroupGateway());
        }
        return (SocialGroupGateway) allGateways.get(gatewayName);
    }

    public static VisitGateway getVisitGateway() {
        final String gatewayName = VisitGateway.class.getName();
        if (!allGateways.containsKey(gatewayName)) {
            allGateways.put(gatewayName, new VisitGateway());
        }
        return (VisitGateway) allGateways.get(gatewayName);
    }

    public static Gateway getGatewayByName(String gatewayName) {
        if (allGateways.containsKey(gatewayName)) {
            return allGateways.get(gatewayName);
        }
        return null;
    }
}
