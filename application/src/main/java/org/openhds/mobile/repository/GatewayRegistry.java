package org.openhds.mobile.repository;

import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.openhds.mobile.repository.gateway.IndividualGateway;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.LocationHierarchyGateway;
import org.openhds.mobile.repository.gateway.MembershipGateway;
import org.openhds.mobile.repository.gateway.RelationshipGateway;
import org.openhds.mobile.repository.gateway.SocialGroupGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;

/**
 * Well know location for accessing entity-specific gateway instances.
 */
public class GatewayRegistry {

    private static FieldWorkerGateway fieldWorkerGateway;
    private static IndividualGateway individualGateway;
    private static LocationGateway locationGateway;
    private static LocationHierarchyGateway locationHierarchyGateway;
    private static MembershipGateway membershipGateway;
    private static RelationshipGateway relationshipGateway;
    private static SocialGroupGateway socialGroupGateway;
    private static VisitGateway visitGateway;

    private GatewayRegistry () {};

    public static FieldWorkerGateway getFieldWorkerGateway() {
        if (null == fieldWorkerGateway) {
            fieldWorkerGateway = new FieldWorkerGateway();
        }
        return fieldWorkerGateway;
    }

    public static IndividualGateway getIndividualGateway() {
        if (null == individualGateway) {
            individualGateway = new IndividualGateway();
        }
        return individualGateway;
    }

    public static LocationGateway getLocationGateway() {
        if (null == locationGateway) {
            locationGateway = new LocationGateway();
        }
        return locationGateway;
    }

    public static LocationHierarchyGateway getLocationHierarchyGateway() {
        if (null == locationHierarchyGateway) {
            locationHierarchyGateway = new LocationHierarchyGateway();
        }
        return locationHierarchyGateway;
    }

    public static MembershipGateway getMembershipGateway() {
        if (null == membershipGateway) {
            membershipGateway = new MembershipGateway();
        }
        return membershipGateway;
    }

    public static RelationshipGateway getRelationshipGateway() {
        if (null == relationshipGateway) {
            relationshipGateway = new RelationshipGateway();
        }
        return relationshipGateway;
    }

    public static SocialGroupGateway getSocialGroupGateway() {
        if (null == socialGroupGateway) {
            socialGroupGateway = new SocialGroupGateway();
        }
        return socialGroupGateway;
    }

    public static VisitGateway getVisitGateway() {
        if (null == visitGateway) {
            visitGateway = new VisitGateway();
        }
        return visitGateway;
    }
}
