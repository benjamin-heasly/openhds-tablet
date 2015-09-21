package org.openhds.mobile.links;

import java.util.HashSet;
import java.util.Set;

/**
 * Hold a HATEOAS link for the OpenHDS server.
 */
public class Link {

    private final String url;

    private final String rel;

    private final Set<String> parameters;

    public Link(String rel, String url) {
        this.rel = rel;
        this.url = url;

        this.parameters = new HashSet<>();
    }

    // parse from text like "https://foo.bar/baz{?thing,stuff}"
    public static Link parse(String rel, String text) {
        String[] urlSplit = text.split("\\{");
        String url = urlSplit.length > 0 ? urlSplit[0] : null;

        Link link = new Link(rel, url);

        if (urlSplit.length < 2) {
            return link;
        }

        String[] paramSplit = urlSplit[1].split("[?,\\{\\}]");
        for (String param : paramSplit) {
            link.getParameters().add(param);
        }

        return link;
    }

    public String getUrl() {
        return url;
    }

    public String buildUrlWithParameters(String... params) {
        StringBuilder builder = new StringBuilder();
        builder.append(url);
        char delimiter = '?';
        for (int i = 0; i < params.length; i++) {
            builder.append(delimiter).append(params[i]);
            delimiter = 0 == i % 2 ? '=' : '&';
        }
        return builder.toString();
    }

    public String getRel() {
        return rel;
    }

    public Set<String> getParameters() {
        return parameters;
    }
}
