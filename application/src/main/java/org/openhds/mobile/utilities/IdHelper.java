package org.openhds.mobile.utilities;

import android.content.ContentResolver;

import org.openhds.mobile.repository.DataWrapper;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.UUID;

import static org.openhds.mobile.repository.RepositoryUtils.LIKE_WILD_CARD;

/**
 * Created by motech-admin on 12/9/14.
 */
public class IdHelper {

    public static String INDIVIDUAL_ID_FORMAT = LIKE_WILD_CARD + "05d";

    public static String generateEntityUuid(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String generateIndividualExtId(ContentResolver contentResolver, DataWrapper location) {
        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();
        return Integer.toString(individualGateway.countAll(contentResolver) + 1);
    }

}
