package org.openhds.mobile.links;

import android.net.Uri;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
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

    public String buildUrlWithParameters(Map<String, String> params) {
        if (null == params) {
            return url;
        }

        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());

            if (!parameters.contains(entry.getKey())) {
                Log.w(this.getClass().getName(), "Building Url with query for <" + entry.getKey() + "> but Link declares no such parameter.");
            }
        }
        return builder.build().toString();
    }

    public String getRel() {
        return rel;
    }

    public Set<String> getParameters() {
        return parameters;
    }
}
