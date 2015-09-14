package org.openhds.mobile.links;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Various HATEOAS links for OpenHDS server home-level resources, organized by rel.
 */
public class ResourceLinkRegistry {

    private static final Map<String, Link> links;

    static {
        links = new HashMap<>();
    }

    public static Link getLink(String rel) {
        return links.containsKey(rel) ? links.get(rel) : null;
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

}
