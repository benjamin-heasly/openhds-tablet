package org.openhds.mobile.task.parsing.entities;

import android.os.AsyncTask;
import android.util.Log;

import org.openhds.mobile.links.JsonLinkParser;
import org.openhds.mobile.links.ResourceLinkRegistry;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Read an input stream, parse it into Links, put them in the ResourceLinkRegistry.
 *
 * BSH
 */
public class ParseLinksTask extends AsyncTask<InputStream, Void, Void> {

    @Override
    protected Void doInBackground(InputStream... inputStreams) {

        JsonLinkParser jsonLinkParser = new JsonLinkParser();
        try {
            ResourceLinkRegistry.addLinks(jsonLinkParser.parseLinks(inputStreams[0]));
        } catch (IOException e) {
            Log.e(ParseLinksTask.class.getName(), "doInBackground " + e);
        }

        return null;
    }

}
