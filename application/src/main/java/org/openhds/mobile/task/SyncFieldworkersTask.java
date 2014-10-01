package org.openhds.mobile.task;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import org.apache.http.HttpResponse;
import org.openhds.mobile.listener.SyncDatabaseListener;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.FieldWorkerGateway;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SyncFieldworkersTask extends HttpTask<Void, Integer> {

    private ContentResolver contentResolver;
    private ProgressDialog progressDialog;
    private SyncDatabaseListener syncListener;

    public SyncFieldworkersTask(RequestContext requestContext,
                                ContentResolver contentResolver, ProgressDialog progressDialog,
                                SyncDatabaseListener syncListener) {
        super(requestContext);
        this.listener = new SyncFieldWorkerListener();
        this.syncListener = syncListener;
        this.progressDialog = progressDialog;
        this.contentResolver = contentResolver;
    }

    @Override
    protected EndResult handleResponseData(HttpResponse response) {
        try {
            processXMLDocument(response.getEntity().getContent());
        } catch (IllegalStateException | XmlPullParserException | IOException e) {
            return EndResult.FAILURE;
        }
        return EndResult.SUCCESS;
    }

    public void processXMLDocument(InputStream content)
            throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(content));

        ArrayList<FieldWorker> list = new ArrayList<FieldWorker>();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT && !isCancelled()) {
            String name = null;

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    if (name.equalsIgnoreCase("fieldworker")) {
                        list.add(processFieldWorkerParams(parser));
                    }
                    break;
            }
            eventType = parser.next();
        }
        replaceAllFieldWorkers(list);
    }

    private FieldWorker processFieldWorkerParams(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        FieldWorker fieldWorker = new FieldWorker();

        parser.nextTag();
        fieldWorker.setExtId(parser.nextText());

        parser.nextTag();
        fieldWorker.setFirstName(parser.nextText());

        parser.nextTag();
        fieldWorker.setLastName(parser.nextText());

        parser.nextTag();
        fieldWorker.setPasswordHash(parser.nextText());
        return fieldWorker;
    }

    private void replaceAllFieldWorkers(List<FieldWorker> list) {
        deleteAllFieldWorkers();
        for (FieldWorker fw : list) {
            addFieldWorker(fw);
        }
    }

    private boolean addFieldWorker(FieldWorker fieldWorker) {
        FieldWorkerGateway fieldWorkerGateway = GatewayRegistry.getFieldWorkerGateway();
        return fieldWorkerGateway.insertOrUpdate(contentResolver, fieldWorker);
    }

    private int deleteAllFieldWorkers() {
        FieldWorkerGateway fieldWorkerGateway = GatewayRegistry.getFieldWorkerGateway();
        int nDeleted = 0;

        List<FieldWorker> fieldWorkers = fieldWorkerGateway.getList(contentResolver, fieldWorkerGateway.findAll());
        for (FieldWorker fieldWorker : fieldWorkers) {
            boolean wasDeleted = fieldWorkerGateway.deleteById(contentResolver, fieldWorker.getExtId());
            if (wasDeleted) {
                nDeleted++;
            }
        }

        return nDeleted;
    }

    private void onSyncSuccess() {
        progressDialog.setTitle("Synced field workers.");
        syncListener.collectionComplete(HttpTask.EndResult.SUCCESS);
    }

    private void onSyncFailure() {
        progressDialog.setTitle("Failed to sync field workers.");
        syncListener.collectionComplete(HttpTask.EndResult.FAILURE);
    }

    private class SyncFieldWorkerListener implements TaskListener {
        @Override
        public void onFailedAuthentication() {
            onSyncFailure();
        }

        @Override
        public void onConnectionError() {
            onSyncFailure();
        }

        @Override
        public void onConnectionTimeout() {
            onSyncFailure();
        }

        @Override
        public void onSuccess() {
            onSyncSuccess();
        }

        @Override
        public void onFailure() {
            onSyncFailure();
        }

        @Override
        public void onNoContent() {
            onSyncFailure();
        }
    }
}
