package org.openhds.mobile.projectdata;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.detailfragments.DetailFragment;
import org.openhds.mobile.fragment.detailfragments.IndividualDetailFragment;
import org.openhds.mobile.model.FormBehaviour;
import org.openhds.mobile.projectdata.FormFilters.BiokoFormFilters;
import org.openhds.mobile.projectdata.FormFilters.CensusFormFilters;
import org.openhds.mobile.projectdata.FormFilters.UpdateFormFilters;
import org.openhds.mobile.projectdata.FormPayloadBuilders.BiokoFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadBuilders.CensusFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadBuilders.UpdateFormPayloadBuilders;
import org.openhds.mobile.projectdata.FormPayloadConsumers.BiokoFormPayloadConsumers;
import org.openhds.mobile.projectdata.FormPayloadConsumers.CensusFormPayloadConsumers;
import org.openhds.mobile.projectdata.FormPayloadConsumers.UpdateFormPayloadConsumers;
import org.openhds.mobile.projectdata.QueryHelpers.CensusQueryHelper;
import org.openhds.mobile.projectdata.QueryHelpers.NavigateModuleInfo;
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectActivityBuilder {

	public static final String ACTIVITY_MODULE_EXTRA = "ACTIVITY_MODULE_EXTRA";

	private static final String CENSUS_ACTIVITY_MODULE = "CensusActivityModule";
	private static final String UPDATE_ACTIVITY_MODULE = "UpdateActivityModule";
    private static final String BIOKO_ACTIVITY_MODULE = "BiokoActivityModule";

	private static final ArrayList<String> activityModules = new ArrayList<String>();
	static {
		activityModules.add(CENSUS_ACTIVITY_MODULE);
		activityModules.add(UPDATE_ACTIVITY_MODULE);
        activityModules.add(BIOKO_ACTIVITY_MODULE);
	}

    public static class BiokoActivityModule implements NavigatePluginModule {

        public static final String REGION_STATE = "region";
        public static final String PROVINCE_STATE = "province";
        public static final String DISTRICT_STATE = "district";
        public static final String SUB_DISTRICT_STATE = "subDistrict";
        public static final String LOCALITY_STATE = "locality";
        public static final String MAP_AREA_STATE = "mapArea";
        public static final String SECTOR_STATE = "sector";
        public static final String HOUSEHOLD_STATE = "household";
        public static final String INDIVIDUAL_STATE = "individual";
        public static final String BOTTOM_STATE = "bottom";

        private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
        private static final List<String> stateSequence = new ArrayList<String>();
        private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();

        static {

            stateLabels.put(REGION_STATE, R.string.region_label);
            stateLabels.put(PROVINCE_STATE, R.string.province_label);
            stateLabels.put(DISTRICT_STATE, R.string.district_label);
            stateLabels.put(SUB_DISTRICT_STATE, R.string.sub_district_label);
            stateLabels.put(LOCALITY_STATE, R.string.locality_label);
            stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
            stateLabels.put(SECTOR_STATE, R.string.sector_label);
            stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
            stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
            stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

            stateSequence.add(REGION_STATE);
            stateSequence.add(PROVINCE_STATE);
            stateSequence.add(DISTRICT_STATE);
            stateSequence.add(SUB_DISTRICT_STATE);
            stateSequence.add(LOCALITY_STATE);
            stateSequence.add(MAP_AREA_STATE);
            stateSequence.add(SECTOR_STATE);
            stateSequence.add(HOUSEHOLD_STATE);
            stateSequence.add(INDIVIDUAL_STATE);
            stateSequence.add(BOTTOM_STATE);

            ArrayList<FormBehaviour> regionFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> provinceFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> districtFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> subDistrictFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> localityFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> mapAreaFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> sectorFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> householdFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> individualFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> bottomFormList = new ArrayList<FormBehaviour>();

            individualFormList.add(new FormBehaviour("Bed_net",
                    R.string.distribute_bednets,
                    new BiokoFormFilters.DistributeBednets(),
                    new BiokoFormPayloadBuilders.DistributeBednets(),
                    new BiokoFormPayloadConsumers.DistributeBednets(), false));

            formsForStates.put(REGION_STATE, regionFormList);
            formsForStates.put(PROVINCE_STATE, provinceFormList);
            formsForStates.put(DISTRICT_STATE, districtFormList);
            formsForStates.put(SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(LOCALITY_STATE, localityFormList);
            formsForStates.put(MAP_AREA_STATE, mapAreaFormList);
            formsForStates.put(SECTOR_STATE, sectorFormList);
            formsForStates.put(HOUSEHOLD_STATE, householdFormList);
            formsForStates.put(INDIVIDUAL_STATE, individualFormList);
            formsForStates.put(BOTTOM_STATE, bottomFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(REGION_STATE, null);
            detailFragsForStates.put(PROVINCE_STATE, null);
            detailFragsForStates.put(DISTRICT_STATE, null);
            detailFragsForStates.put(SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(MAP_AREA_STATE, null);
            detailFragsForStates.put(SECTOR_STATE, null);
            detailFragsForStates.put(HOUSEHOLD_STATE, null);
            detailFragsForStates.put(INDIVIDUAL_STATE, null);
            detailFragsForStates.put(BOTTOM_STATE, null);

        }

        @Override
        public Map<String, Integer> getStateLabels() {
            return stateLabels;
        }

        @Override
        public List<String> getStateSequence() {
            return stateSequence;
        }

        @Override
        public QueryHelper getQueryHelper() {
            return new CensusQueryHelper();
        }

        @Override
        public Map<String, List<FormBehaviour>> getFormsforstates() {
            return formsForStates;
        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }

        public static class BiokoInfo implements NavigateModuleInfo {

            @Override
            public int getModuleLabelStringId() {
                return R.string.bioko_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.bioko_portal_description;
            }

            @Override
            public int getModuleColorId() {
                return R.color.PowderBlue;
            }
        }

    }

	public static class CensusActivityModule implements NavigatePluginModule {

		public static final String REGION_STATE = "region";
		public static final String PROVINCE_STATE = "province";
		public static final String DISTRICT_STATE = "district";
        public static final String SUB_DISTRICT_STATE = "subDistrict";
        public static final String LOCALITY_STATE = "locality";
		public static final String MAP_AREA_STATE = "mapArea";
		public static final String SECTOR_STATE = "sector";
		public static final String HOUSEHOLD_STATE = "household";
		public static final String INDIVIDUAL_STATE = "individual";
		public static final String BOTTOM_STATE = "bottom";

		private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
		private static final List<String> stateSequence = new ArrayList<String>();
		private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
		private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();

		static {

            stateLabels.put(REGION_STATE, R.string.region_label);
            stateLabels.put(PROVINCE_STATE, R.string.province_label);
            stateLabels.put(DISTRICT_STATE, R.string.district_label);
            stateLabels.put(SUB_DISTRICT_STATE, R.string.sub_district_label);
            stateLabels.put(LOCALITY_STATE, R.string.locality_label);
            stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
            stateLabels.put(SECTOR_STATE, R.string.sector_label);
            stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
            stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
            stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

            stateSequence.add(REGION_STATE);
            stateSequence.add(PROVINCE_STATE);
            stateSequence.add(DISTRICT_STATE);
            stateSequence.add(SUB_DISTRICT_STATE);
            stateSequence.add(LOCALITY_STATE);
            stateSequence.add(MAP_AREA_STATE);
            stateSequence.add(SECTOR_STATE);
            stateSequence.add(HOUSEHOLD_STATE);
            stateSequence.add(INDIVIDUAL_STATE);
            stateSequence.add(BOTTOM_STATE);

			ArrayList<FormBehaviour> regionFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> provinceFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> districtFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> subDistrictFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> localityFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> mapAreaFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> sectorFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> householdFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> individualFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> bottomFormList = new ArrayList<FormBehaviour>();

            householdFormList.add(new FormBehaviour("Location",
                    R.string.create_location,
                    new CensusFormFilters.AddLocation(),
                    new CensusFormPayloadBuilders.AddLocation(),
                    new CensusFormPayloadConsumers.AddLocation(), false));

			individualFormList.add(new FormBehaviour("Individual",
					R.string.create_head_of_household_label,
					new CensusFormFilters.AddHeadOfHousehold(),
					new CensusFormPayloadBuilders.AddHeadOfHousehold(),
					new CensusFormPayloadConsumers.AddHeadOfHousehold(), false));

			individualFormList.add(new FormBehaviour("Individual",
					R.string.add_member_of_household_label,
					new CensusFormFilters.AddMemberOfHousehold(),
					new CensusFormPayloadBuilders.AddMemberOfHousehold(),
					new CensusFormPayloadConsumers.AddMemberOfHousehold(), false));

			bottomFormList.add(new FormBehaviour("Individual",
					R.string.edit_individual_label,
					new CensusFormFilters.EditIndividual(),
					new CensusFormPayloadBuilders.EditIndividual(),
					new CensusFormPayloadConsumers.EditIndividual(), true));

			formsForStates.put(REGION_STATE, regionFormList);
			formsForStates.put(PROVINCE_STATE, provinceFormList);
			formsForStates.put(DISTRICT_STATE, districtFormList);
            formsForStates.put(SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(LOCALITY_STATE, localityFormList);
			formsForStates.put(MAP_AREA_STATE, mapAreaFormList);
			formsForStates.put(SECTOR_STATE, sectorFormList);
			formsForStates.put(HOUSEHOLD_STATE, householdFormList);
			formsForStates.put(INDIVIDUAL_STATE, individualFormList);
			formsForStates.put(BOTTOM_STATE, bottomFormList);

			// these details are off by 1: details for an individual should be
			// shown when you click a specific individual which is technically
			// in the bottom state.
			detailFragsForStates.put(REGION_STATE, null);
			detailFragsForStates.put(PROVINCE_STATE, null);
			detailFragsForStates.put(DISTRICT_STATE, null);
            detailFragsForStates.put(SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(MAP_AREA_STATE, null);
			detailFragsForStates.put(SECTOR_STATE, null);
			detailFragsForStates.put(HOUSEHOLD_STATE, null);
			detailFragsForStates.put(INDIVIDUAL_STATE, null);
			detailFragsForStates.put(BOTTOM_STATE,
					new IndividualDetailFragment());

		}

		@Override
		public Map<String, Integer> getStateLabels() {
			return stateLabels;
		}

		@Override
		public List<String> getStateSequence() {
			return stateSequence;
		}

		@Override
		public Map<String, DetailFragment> getDetailFragsForStates() {
			return detailFragsForStates;
		}

		@Override
		public Map<String, List<FormBehaviour>> getFormsforstates() {
			return formsForStates;
		}

		@Override
		public QueryHelper getQueryHelper() {

			return new CensusQueryHelper();
		}

        public static class CensusInfo implements NavigateModuleInfo {

            @Override
            public int getModuleLabelStringId() {
                return R.string.census_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.census_portal_description;
            }

            @Override
            public int getModuleColorId() {
                return R.color.PowderBlue;
            }
        }
	}

	public static class UpdateActivityModule implements NavigatePluginModule {

		public static final String REGION_STATE = "region";
		public static final String PROVINCE_STATE = "province";
		public static final String DISTRICT_STATE = "district";
        public static final String SUB_DISTRICT_STATE = "subDistrict";
        public static final String LOCALITY_STATE = "locality";
		public static final String MAP_AREA_STATE = "mapArea";
		public static final String SECTOR_STATE = "sector";
		public static final String HOUSEHOLD_STATE = "household";
		public static final String INDIVIDUAL_STATE = "individual";
		public static final String BOTTOM_STATE = "bottom";

		private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
		private static final List<String> stateSequence = new ArrayList<String>();
		private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
		private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();

		static {

            stateLabels.put(REGION_STATE, R.string.region_label);
            stateLabels.put(PROVINCE_STATE, R.string.province_label);
            stateLabels.put(DISTRICT_STATE, R.string.district_label);
            stateLabels.put(SUB_DISTRICT_STATE, R.string.sub_district_label);
            stateLabels.put(LOCALITY_STATE, R.string.locality_label);
            stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
            stateLabels.put(SECTOR_STATE, R.string.sector_label);
            stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
            stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
            stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

            stateSequence.add(REGION_STATE);
            stateSequence.add(PROVINCE_STATE);
            stateSequence.add(DISTRICT_STATE);
            stateSequence.add(SUB_DISTRICT_STATE);
            stateSequence.add(LOCALITY_STATE);
            stateSequence.add(MAP_AREA_STATE);
            stateSequence.add(SECTOR_STATE);
            stateSequence.add(HOUSEHOLD_STATE);
            stateSequence.add(INDIVIDUAL_STATE);
            stateSequence.add(BOTTOM_STATE);

            ArrayList<FormBehaviour> regionFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> provinceFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> districtFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> subDistrictFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> localityFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> mapAreaFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> sectorFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> householdFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> individualFormList = new ArrayList<FormBehaviour>();
            ArrayList<FormBehaviour> bottomFormList = new ArrayList<FormBehaviour>();

			individualFormList.add(new FormBehaviour("Visit",
					R.string.start_a_visit,
					new UpdateFormFilters.StartAVisit(),
					new UpdateFormPayloadBuilders.StartAVisit(),
					new UpdateFormPayloadConsumers.StartAVisit(), false));

            bottomFormList.add(new FormBehaviour("Out_migration",
                    R.string.out_migration,
                    new UpdateFormFilters.RegisterOutMigration(),
                    new UpdateFormPayloadBuilders.RegisterOutMigration(),
                    null, false));

            bottomFormList.add(new FormBehaviour("Death",
                    R.string.register_death,
                    new UpdateFormFilters.RegisterDeath(),
                    new UpdateFormPayloadBuilders.RegisterDeath(),
                    new UpdateFormPayloadConsumers.RegisterDeath(), false));

            bottomFormList.add(new FormBehaviour("Pregnancy_observation",
                    R.string.record_pregnancy_observation,
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    null, false));

			formsForStates.put(REGION_STATE, regionFormList);
			formsForStates.put(PROVINCE_STATE, provinceFormList);
			formsForStates.put(DISTRICT_STATE, districtFormList);
            formsForStates.put(SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(LOCALITY_STATE, localityFormList);
			formsForStates.put(MAP_AREA_STATE, mapAreaFormList);
			formsForStates.put(SECTOR_STATE, sectorFormList);
			formsForStates.put(HOUSEHOLD_STATE, householdFormList);
			formsForStates.put(INDIVIDUAL_STATE, individualFormList);
			formsForStates.put(BOTTOM_STATE, bottomFormList);

			// these details are off by 1: details for an individual should be
			// shown when you click a specific individual which is technically
			// in the bottom state.
			detailFragsForStates.put(REGION_STATE, null);
			detailFragsForStates.put(PROVINCE_STATE, null);
			detailFragsForStates.put(DISTRICT_STATE, null);
            detailFragsForStates.put(SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(MAP_AREA_STATE, null);
			detailFragsForStates.put(SECTOR_STATE, null);
			detailFragsForStates.put(HOUSEHOLD_STATE, null);
			detailFragsForStates.put(INDIVIDUAL_STATE, null);
			detailFragsForStates.put(BOTTOM_STATE,
					new IndividualDetailFragment());

		}

		@Override
		public Map<String, Integer> getStateLabels() {
			return stateLabels;
		}

		@Override
		public List<String> getStateSequence() {
			return stateSequence;
		}

		@Override
		public Map<String, DetailFragment> getDetailFragsForStates() {
			return detailFragsForStates;
		}

		@Override
		public Map<String, List<FormBehaviour>> getFormsforstates() {
			return formsForStates;
		}

		@Override
		public QueryHelper getQueryHelper() {

			return new CensusQueryHelper();
		}

        public static class UpdateInfo implements NavigateModuleInfo{

            @Override
            public int getModuleLabelStringId() {
                return R.string.update_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.update_portal_description;
            }

            @Override
            public int getModuleColorId() {
                return R.color.Blue;
            }
        }

	}

	public static NavigatePluginModule getModuleByName(String name) {

		if (name.equals(CENSUS_ACTIVITY_MODULE)) {
			return new CensusActivityModule();
		} else if (name.equals(UPDATE_ACTIVITY_MODULE)) {
			return new UpdateActivityModule();
		} else if (name.equals(BIOKO_ACTIVITY_MODULE)) {
            return new BiokoActivityModule();
        }

		return null;

	}

	public static ArrayList<String> getActivityModuleNames() {
		return activityModules;
	}



    public static NavigateModuleInfo getModuleInfoByName(String name){
        if (name.equals(CENSUS_ACTIVITY_MODULE)) {
            return new CensusActivityModule.CensusInfo();
        } else if (name.equals(UPDATE_ACTIVITY_MODULE)) {
            return new UpdateActivityModule.UpdateInfo();
        } else if (name.equals(BIOKO_ACTIVITY_MODULE)) {
            return new BiokoActivityModule.BiokoInfo();
        }
        return null;
    }

}
