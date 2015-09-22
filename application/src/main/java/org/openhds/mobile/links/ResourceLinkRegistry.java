package org.openhds.mobile.links;

import org.openhds.mobile.R;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.Gateway;
import org.openhds.mobile.task.parsing.entities.EntityParser;
import org.openhds.mobile.task.parsing.entities.FieldWorkerParser;
import org.openhds.mobile.task.parsing.entities.IndividualParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyLevelParser;
import org.openhds.mobile.task.parsing.entities.LocationHierarchyParser;
import org.openhds.mobile.task.parsing.entities.LocationParser;
import org.openhds.mobile.task.parsing.entities.MembershipParser;
import org.openhds.mobile.task.parsing.entities.RelationshipParser;
import org.openhds.mobile.task.parsing.entities.ResidencyParser;
import org.openhds.mobile.task.parsing.entities.SocialGroupParser;
import org.openhds.mobile.task.parsing.entities.UserParser;
import org.openhds.mobile.task.parsing.entities.VisitParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Home-level HATEOAS links from OpenHDS server and tablet-side interpretation of the rels.
 */
public class ResourceLinkRegistry {

    private static final Map<String, Link> links;

    private static final Map<String, RelInterpretation<?>> interpretations;

    static {
        links = new HashMap<>();
        interpretations = new HashMap<>();

        addRelInterpretation("fieldWorkers",
                R.string.label_field_workers,
                new FieldWorkerParser(),
                GatewayRegistry.getFieldWorkerGateway());
        addRelInterpretation("individuals",
                R.string.label_individuals,
                new IndividualParser(),
                GatewayRegistry.getIndividualGateway());
        addRelInterpretation("locations",
                R.string.label_locations,
                new LocationParser(),
                GatewayRegistry.getLocationGateway());
        addRelInterpretation("locationHierarchyLevels",
                R.string.label_location_hierarchy_levels,
                new LocationHierarchyLevelParser(),
                GatewayRegistry.getLocationHierarchyLevelGateway());
        addRelInterpretation("locationHierarchies",
                R.string.label_location_hierarchies,
                new LocationHierarchyParser(),
                GatewayRegistry.getLocationHierarchyGateway());
        addRelInterpretation("memberships",
                R.string.label_memberships,
                new MembershipParser(),
                GatewayRegistry.getMembershipGateway());
        addRelInterpretation("relationships",
                R.string.label_relationships,
                new RelationshipParser(),
                GatewayRegistry.getRelationshipGateway());
        addRelInterpretation("residencies",
                R.string.label_residencies,
                new ResidencyParser(),
                GatewayRegistry.getResidencyGateway());
        addRelInterpretation("socialGroups",
                R.string.label_social_groups,
                new SocialGroupParser(),
                GatewayRegistry.getSocialGroupGateway());
        addRelInterpretation("users",
                R.string.label_users,
                new UserParser(),
                GatewayRegistry.getUsesrGateway());
        interpretations.get("users").setSyncRel("bulk");
        addRelInterpretation("visits",
                R.string.label_visits,
                new VisitParser(),
                GatewayRegistry.getVisitGateway());
    }

    private static <T> void addRelInterpretation(String rel, Integer label, EntityParser<T> parser, Gateway<T> gateway) {
        interpretations.put(rel, new RelInterpretation<T>(rel, label, parser, gateway));
    }

    public static void addLink(String rel, Link link) {
        links.put(rel, link);
    }

    public static void addLinks(Map<String, Link> linksToAdd) {
        links.putAll(linksToAdd);
    }

    public static Map<String, Link> getLinks() {
        return Collections.unmodifiableMap(links);
    }

    public static Link getLink(String rel) {
        return links.containsKey(rel) ? links.get(rel) : null;
    }

    public static RelInterpretation<?> getInterpretation(String rel) {
        return interpretations.containsKey(rel) ? interpretations.get(rel) : null;
    }

    // Only rels with server link and tablet-side interpretation.
    public static List<String> activeRels() {
        List<String> activeRels = new ArrayList<>();

        for (String rel : interpretations.keySet()) {
            if (links.containsKey(rel)) {
                activeRels.add(rel);
            }
        }

        return activeRels;
    }

}
