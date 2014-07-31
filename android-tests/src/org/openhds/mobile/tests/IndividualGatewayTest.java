package org.openhds.mobile.tests;

import android.test.ProviderTestCase2;
import org.openhds.mobile.OpenHDS;
import org.openhds.mobile.provider.OpenHDSProvider;

public class IndividualGatewayTest extends ProviderTestCase2<OpenHDSProvider> {

    public IndividualGatewayTest () {
        super(OpenHDSProvider.class, OpenHDS.AUTHORITY);
    }

    public void testNothing() {
        assertEquals(false, false);
    }

}
