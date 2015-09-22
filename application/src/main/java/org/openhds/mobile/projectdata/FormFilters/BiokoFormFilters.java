package org.openhds.mobile.projectdata.FormFilters;

import org.openhds.mobile.activity.NavigateActivity;


public class BiokoFormFilters {

    public static class DistributeBednets implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            return true;
        }

    }

    public static class SprayHousehold implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {

            return true;
        }
    }

    public static class SuperOjo implements FormFilter {

        @Override
        public boolean amIValid(NavigateActivity navigateActivity) {
            return true;
        }
    }

}
