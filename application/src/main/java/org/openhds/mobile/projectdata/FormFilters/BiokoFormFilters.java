package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;
import org.openhds.mobile.model.core.Location;
import org.openhds.mobile.repository.GatewayRegistry;
import org.openhds.mobile.repository.gateway.LocationGateway;


public class BiokoFormFilters {

    public static class DistributeBednets implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            LocationGateway locationGateway = GatewayRegistry.getLocationGateway();

            Location location = locationGateway.getFirst(navigateActivity.getContentResolver(),
                    locationGateway.findById(navigateActivity.getCurrentSelection().getUuid()));

            if( null == location.getHasRecievedBedNets() || !location.getHasRecievedBedNets().equals("true") ){
                return true;
            }
            return false;


        }

    }

}
