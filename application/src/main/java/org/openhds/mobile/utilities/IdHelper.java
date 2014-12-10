package org.openhds.mobile.utilities;

import android.content.ContentResolver;
import org.openhds.mobile.model.FieldWorker;
import org.openhds.mobile.model.Individual;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.IndividualGateway;

import java.util.Iterator;
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

    public static String generateIndividualExtId(ContentResolver resolver, FieldWorker fieldWorker){



        String idPrefix = fieldWorker.getIdPrefix();

        //TODO this is a hack to pad the ID prefix just incase it is being checked on the server
        if(idPrefix.length()<2){
            idPrefix = "0"+ idPrefix;
        }


        IndividualGateway individualGateway = GatewayRegistry.getIndividualGateway();


        Iterator<Individual> individualIterator = individualGateway.getIterator(resolver,
                individualGateway.findByExtIdPrefixDescending(idPrefix));
        int nextSequence = 0;
        if (individualIterator.hasNext()) {
            String lastExtId = individualIterator.next().getExtId();
            int prefixLength = idPrefix.length();
            int checkDigitLength = 1;
            String lastSequenceNumber = lastExtId.substring(prefixLength + 1,
                    lastExtId.length() - checkDigitLength);
            nextSequence = Integer.parseInt(lastSequenceNumber) + 1;
        }

        // TODO: break out 5-digit number format, don't use string literal here.
        String generatedIdSeqNum = String.format(INDIVIDUAL_ID_FORMAT, nextSequence);

        Character generatedIdCheck = LuhnValidator
                .generateCheckCharacter(generatedIdSeqNum + generatedIdSeqNum);

        return idPrefix + generatedIdSeqNum
                + generatedIdCheck.toString();

    }


}
