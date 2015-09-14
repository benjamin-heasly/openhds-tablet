package org.openhds.mobile.task.parsing.entities;

import android.os.AsyncTask;
import android.util.Log;

import org.openhds.mobile.links.JsonLinkParser;
import org.openhds.mobile.links.Link;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * Read an input stream, parse it into Links, put them in a Map.
 *
 * BSH
 */
public class ParseLinksTask extends AsyncTask<InputStream, Void, Map<String, Link>> {

    private final LinkHandler linkHandler;

    public ParseLinksTask(LinkHandler linkHandler) {
        this.linkHandler = linkHandler;
    }

    public interface LinkHandler {
        void handleLinks(Map<String, Link> links);
    }

    @Override
    protected Map<String, Link> doInBackground(InputStream... inputStreams) {

        JsonLinkParser jsonLinkParser = new JsonLinkParser();
        try {
            return(jsonLinkParser.parseLinks(inputStreams[0]));
        } catch (IOException e) {
            Log.e(ParseLinksTask.class.getName(), "doInBackground " + e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Map<String, Link> links) {
        linkHandler.handleLinks(links);
    }
}
