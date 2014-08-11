package org.openhds.mobile.tests;

import android.content.ContentResolver;
import android.test.ProviderTestCase2;
import com.google.common.base.Charsets;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.model.Location;
import org.openhds.mobile.model.Visit;
import org.openhds.mobile.provider.OpenHDSProvider;
import org.openhds.mobile.provider.PasswordHelper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;
import org.openhds.mobile.repository.gateway.VisitGateway;
import org.openhds.mobile.task.SyncEntitiesTask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Feed XML documents to SyncEntitiesTask and verify entities in database.
 */
public class SyncEntitiesTaskTest extends ProviderTestCase2<OpenHDSProvider> {

    private static final String TEST_PASSWORD = "";

    private OpenHDSProvider provider;
    private ContentResolver contentResolver;
    private SyncEntitiesTask syncEntitiesTask;

    private class ConstantPasswordHelper implements PasswordHelper {
        @Override
        public String getPassword() {
            return TEST_PASSWORD;
        }
    }

    public SyncEntitiesTaskTest() {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.provider = (OpenHDSProvider) getProvider();
        this.contentResolver = getMockContentResolver();

        // inject a password helper that uses a known password
        // and doesn't use shared preferences (which are not enabled under ProviderTestCase2)
        provider.setPasswordHelper(new ConstantPasswordHelper());

        // make sure we have a fresh database for each test
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase.loadLibs(getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        databaseHelper.onUpgrade(db, 0, 0);

        // test the task against the mock content resolver, needs no other dependencies
        syncEntitiesTask = new SyncEntitiesTask(null, null, null, null, getMockContext(), null);
    }

    @Override
    protected void tearDown() {
        SQLiteOpenHelper databaseHelper = provider.getDatabaseHelper();
        SQLiteDatabase db = databaseHelper.getWritableDatabase(TEST_PASSWORD);
        db.close();
    }

    public void testProcessVisitXML() throws Exception {
//        final String visitXML = "No Dice Baby!";
//        InputStream inputStream = new ByteArrayInputStream(visitXML.getBytes(Charsets.UTF_8));
//        syncEntitiesTask.processXMLDocument(inputStream);
//
//        VisitGateway visitGateway = GatewayRegistry.getVisitGateway();
//        List<Visit> visits = visitGateway.getList(contentResolver, visitGateway.findAll());
//        assertEquals(2, visits.size());
    }

    public void testProcessLocationXML() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(getLocationXml().getBytes(Charsets.UTF_8));
        syncEntitiesTask.processXMLDocument(inputStream);

        LocationGateway locationGateway = GatewayRegistry.getLocationGateway();
        List<Location> locations = locationGateway.getList(contentResolver, locationGateway.findAll());
        assertEquals(2, locations.size());
    }

    private static String getLocationXml() {
        final String xml = "<?xml version=\"1.0\" ?>"
                + "<locations>"
                + "<location>"
                + "<collectedBy><extId>FWIMPORT</extId></collectedBy>"
                + "<accuracy></accuracy>"
                + "<altitude></altitude>"
                + "<buildingNumber>1</buildingNumber>"
                + "<communityName>Batoicopo</communityName>"
                + "<districtName>Malabo</districtName>"
                + "<extId>M1027S20E01P1</extId>"
                + "<floorNumber>1</floorNumber>"
                + "<latitude></latitude>"
                + "<localityName>7 Batoicopo</localityName>"
                + "<locationLevel><extId>M1027S020</extId></locationLevel>"
                + " <locationName>M1027S20E01P1</locationName>"
                + " <locationType>Household</locationType>"
                + " <longitude></longitude>"
                + " <mapAreaName>M1027</mapAreaName>"
                + " <provinceName>Bioko Norte</provinceName>"
                + " <regionName></regionName>"
                + " <sectorName>S020</sectorName>"
                + " <subDistrictName>Consejo de Poblado</subDistrictName>"
                + " </location>"
                + " <location><collectedBy><extId>FWIMPORT</extId></collectedBy>"
                + " <accuracy></accuracy>"
                + " <altitude></altitude>"
                + " <buildingNumber>2</buildingNumber>"
                + " <communityName>Batoicopo</communityName>"
                + " <districtName>Malabo</districtName>"
                + " <extId>M1027S20E02P1</extId>"
                + " <floorNumber>1</floorNumber>"
                + " <latitude></latitude>"
                + " <localityName>7 Batoicopo</localityName>"
                + " <locationLevel><extId>M1027S020</extId></locationLevel>"
                + " <locationName>M1027S20E02P1</locationName>"
                + " <locationType>Household</locationType>"
                + " <longitude></longitude>"
                + " <mapAreaName>M1027</mapAreaName>"
                + " <provinceName>Bioko Norte</provinceName>"
                + " <regionName></regionName>"
                + " <sectorName>S020</sectorName>"
                + " <subDistrictName>Consejo de Poblado</subDistrictName>"
                + " </location>"
                + "</locations>";
        return xml;
    }
}
