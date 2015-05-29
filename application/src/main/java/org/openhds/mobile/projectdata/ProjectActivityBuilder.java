package org.openhds.mobile.projectdata;

import org.openhds.mobile.R;
import org.openhds.mobile.fragment.navigate.detail.DetailFragment;
import org.openhds.mobile.fragment.navigate.detail.IndividualDetailFragment;
import org.openhds.mobile.model.form.FormBehaviour;
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
import org.openhds.mobile.projectdata.QueryHelpers.QueryHelper;
import org.openhds.mobile.repository.search.FormSearchPluginModule;
import org.openhds.mobile.repository.search.SearchUtils;

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

    public static ArrayList<String> getActivityModuleNames() {
        return activityModules;
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

    // This class defines the "hierarchy" for the PluginModules
    // The "hierarchy" is simply the list of state names, state labels (for UI), and state sequence.
    public static class BiokoHierarchy implements HierarchyInfo {

        public static final String HIERARCHY_NAME = "biokoHierarchy";

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
        public String getHierarchyName(){
            return HIERARCHY_NAME;
        }
    }

    // These modules are passed to NavigateActivity and inject it project specific data and hierarchy information
    public static class BiokoActivityModule implements NavigatePluginModule {

        private static final BiokoHierarchy biokoHierarchy = new BiokoHierarchy();
        private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();
        public static class BiokoUiHelper implements ModuleUiHelper {

            @Override
            public int getModuleLabelStringId() {
                return R.string.bioko_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.bioko_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.bioko_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.bioko_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.bioko_data_selector;
            }

            @Override
            public int getFormSelectionDrawableId() {
                return R.drawable.bioko_form_selector;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.bioko_hierarchy_selector;
            }

            @Override
            public int getMiddleColumnDrawableId() {
                return R.drawable.bioko_middle_column_drawable;
            }

        }

        static {

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
                    new BiokoFormPayloadConsumers.DistributeBednets()));

            individualFormList.add(new FormBehaviour("spraying",
                    R.string.spray_household,
                    new BiokoFormFilters.SprayHousehold(),
                    new BiokoFormPayloadBuilders.SprayHousehold(),
                    new BiokoFormPayloadConsumers.SprayHousehold()));

            formsForStates.put(biokoHierarchy.REGION_STATE, regionFormList);
            formsForStates.put(biokoHierarchy.PROVINCE_STATE, provinceFormList);
            formsForStates.put(biokoHierarchy.DISTRICT_STATE, districtFormList);
            formsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(biokoHierarchy.LOCALITY_STATE, localityFormList);
            formsForStates.put(biokoHierarchy.MAP_AREA_STATE, mapAreaFormList);
            formsForStates.put(biokoHierarchy.SECTOR_STATE, sectorFormList);
            formsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, householdFormList);
            formsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, individualFormList);
            formsForStates.put(biokoHierarchy.BOTTOM_STATE, bottomFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(biokoHierarchy.REGION_STATE, null);
            detailFragsForStates.put(biokoHierarchy.PROVINCE_STATE, null);
            detailFragsForStates.put(biokoHierarchy.DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.MAP_AREA_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SECTOR_STATE, null);
            detailFragsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, null);
            detailFragsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, null);
            detailFragsForStates.put(biokoHierarchy.BOTTOM_STATE,  new IndividualDetailFragment());

        }

        @Override
        public QueryHelper getQueryHelper() {
            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new BiokoUiHelper();
        }

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return biokoHierarchy;
        }

        @Override
        public Map<String, List<FormBehaviour>> getFormsForStates() {
            return formsForStates;
        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }



    }

    public static class CensusActivityModule implements NavigatePluginModule {

        private static final BiokoHierarchy biokoHierarchy = new BiokoHierarchy();
        private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();
        public static class CensusUiHelper implements ModuleUiHelper {

            @Override
            public int getModuleLabelStringId() {
                return R.string.census_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.census_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.census_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.census_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.census_data_selector;
            }

            @Override
            public int getFormSelectionDrawableId() {
                return R.drawable.census_form_selector_orange;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.census_hierarchy_selector;
            }

            @Override
            public int getMiddleColumnDrawableId() {
                return R.drawable.census_middle_column_drawable;
            }
        }

        public static FormBehaviour visitPregObFormBehaviour;

        public static FormBehaviour pregObFormBehaviour;

        public static FormBehaviour addLocationFormBehaviour;

        static {

            visitPregObFormBehaviour = new FormBehaviour("Visit",
                    R.string.start_a_visit,
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new CensusFormPayloadConsumers.ChainedVisitForPregnancyObservation());

            pregObFormBehaviour = new FormBehaviour("Pregnancy_observation",
                    R.string.record_pregnancy_observation,
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    new CensusFormPayloadConsumers.ChainedPregnancyObservation());

            addLocationFormBehaviour = new FormBehaviour("Location",
                    R.string.create_location,
                    new CensusFormFilters.AddLocation(),
                    new CensusFormPayloadBuilders.AddLocation(),
                    new CensusFormPayloadConsumers.AddLocation());

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

            householdFormList.add(addLocationFormBehaviour);

            individualFormList.add(new FormBehaviour("Location_evaluation",
                    R.string.evaluate_location_label,
                    new CensusFormFilters.EvaluateLocation(),
                    new CensusFormPayloadBuilders.EvaluateLocation(),
                    new CensusFormPayloadConsumers.EvaluateLocation()));

            individualFormList.add(new FormBehaviour("Individual",
                    R.string.create_head_of_household_label,
                    new CensusFormFilters.AddHeadOfHousehold(),
                    new CensusFormPayloadBuilders.AddHeadOfHousehold(),
                    new CensusFormPayloadConsumers.AddHeadOfHousehold()));

            individualFormList.add(new FormBehaviour("Individual",
                    R.string.add_member_of_household_label,
                    new CensusFormFilters.AddMemberOfHousehold(),
                    new CensusFormPayloadBuilders.AddMemberOfHousehold(),
                    new CensusFormPayloadConsumers.AddMemberOfHousehold()));



            formsForStates.put(biokoHierarchy.REGION_STATE, regionFormList);
            formsForStates.put(biokoHierarchy.PROVINCE_STATE, provinceFormList);
            formsForStates.put(biokoHierarchy.DISTRICT_STATE, districtFormList);
            formsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(biokoHierarchy.LOCALITY_STATE, localityFormList);
            formsForStates.put(biokoHierarchy.MAP_AREA_STATE, mapAreaFormList);
            formsForStates.put(biokoHierarchy.SECTOR_STATE, sectorFormList);
            formsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, householdFormList);
            formsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, individualFormList);
            formsForStates.put(biokoHierarchy.BOTTOM_STATE, bottomFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(biokoHierarchy.REGION_STATE, null);
            detailFragsForStates.put(biokoHierarchy.PROVINCE_STATE, null);
            detailFragsForStates.put(biokoHierarchy.DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.MAP_AREA_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SECTOR_STATE, null);
            detailFragsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, null);
            detailFragsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, null);
            detailFragsForStates.put(biokoHierarchy.BOTTOM_STATE,
                    new IndividualDetailFragment());

        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }

        @Override
        public Map<String, List<FormBehaviour>> getFormsForStates() {
            return formsForStates;
        }

        @Override
        public QueryHelper getQueryHelper() {

            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new CensusUiHelper();
        }

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return biokoHierarchy;
        }
    }

    public static class UpdateActivityModule implements NavigatePluginModule {

        private static final BiokoHierarchy biokoHierarchy = new BiokoHierarchy();
        private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();
        private static final Map<String, DetailFragment> detailFragsForStates = new HashMap<String, DetailFragment>();
        public static class UpdateUiHelper implements ModuleUiHelper {

            @Override
            public int getModuleLabelStringId() {
                return R.string.update_portal_label;
            }

            @Override
            public int getModuleDescriptionStringId() {
                return R.string.update_portal_description;
            }

            @Override
            public int getModulePortalDrawableId() {
                return R.drawable.update_hierarchy_selector;
            }

            @Override
            public int getModuleTitleStringId() {
                return R.string.update_activity_title;
            }

            @Override
            public int getDataSelectionDrawableId() {
                return R.drawable.update_data_selector;
            }

            @Override
            public int getFormSelectionDrawableId() {
                return R.drawable.update_form_selector;
            }

            @Override
            public int getHierarchySelectionDrawableId() {
                return R.drawable.update_hierarchy_selector;
            }

            @Override
            public int getMiddleColumnDrawableId() {
                return R.drawable.update_middle_column_drawable;
            }
        }


        public static FormBehaviour externalInMigrationFormBehaviour;

        static {

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


            // Start a Visit FormBehaviour
            individualFormList.add(new FormBehaviour("Visit",
                    R.string.start_a_visit,
                    new UpdateFormFilters.StartAVisit(),
                    new UpdateFormPayloadBuilders.StartAVisit(),
                    new UpdateFormPayloadConsumers.StartAVisit()));

            // Register an Internal Inmigration, requires a search to do
            ArrayList<FormSearchPluginModule> searches = new ArrayList<>();
            searches.add(SearchUtils.getIndividualPlugin(ProjectFormFields.Individuals.INDIVIDUAL_UUID, R.string.search_individual_label));
            individualFormList.add(new FormBehaviour("In_migration",
                    R.string.internal_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterInternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration(),
                    searches));


            // Register an External InMigration form (chained after individual form)
                    externalInMigrationFormBehaviour = new FormBehaviour("In_migration",
                    R.string.external_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.RegisterExternalInMigration(),
                    new UpdateFormPayloadConsumers.RegisterInMigration());


            // Register an Individual for External InMigration (chained with in_migration form)
            individualFormList.add(new FormBehaviour("Individual",
                    R.string.external_in_migration,
                    new UpdateFormFilters.RegisterInMigration(),
                    new UpdateFormPayloadBuilders.AddIndividualFromInMigration(),
                    new UpdateFormPayloadConsumers.AddIndividualFromInMigration()));

            // Register an OutMigration FormBehaviour
            bottomFormList.add(new FormBehaviour("Out_migration",
                    R.string.out_migration,
                    new UpdateFormFilters.RegisterOutMigration(),
                    new UpdateFormPayloadBuilders.RegisterOutMigration(),
                    new UpdateFormPayloadConsumers.RegisterOutMigration()));

            // Register a Death FormBehaviour
            bottomFormList.add(new FormBehaviour("Death",
                    R.string.register_death,
                    new UpdateFormFilters.RegisterDeath(),
                    new UpdateFormPayloadBuilders.RegisterDeath(),
                    new UpdateFormPayloadConsumers.RegisterDeath()));

            // Register a Pregnancy Observation FormBehaviour
            bottomFormList.add(new FormBehaviour("Pregnancy_observation",
                    R.string.record_pregnancy_observation,
                    new UpdateFormFilters.RecordPregnancyObservation(),
                    new UpdateFormPayloadBuilders.RecordPregnancyObservation(),
                    null));

            // Register a Pregnancy OutCome FormBehaviour
            ArrayList<FormSearchPluginModule> daddySearch = new ArrayList<>();
            daddySearch.add(SearchUtils.getIndividualPlugin(ProjectFormFields.PregnancyOutcome.FATHER_UUID, R.string.search_father_label));
            bottomFormList.add(new FormBehaviour("Pregnancy_outcome",
                    R.string.record_pregnancy_outcome,
                    new UpdateFormFilters.RecordPregnancyOutcome(),
                    new UpdateFormPayloadBuilders.RecordPregnancyOutcome(),
                    null,
                    daddySearch));

            formsForStates.put(biokoHierarchy.REGION_STATE, regionFormList);
            formsForStates.put(biokoHierarchy.PROVINCE_STATE, provinceFormList);
            formsForStates.put(biokoHierarchy.DISTRICT_STATE, districtFormList);
            formsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, subDistrictFormList);
            formsForStates.put(biokoHierarchy.LOCALITY_STATE, localityFormList);
            formsForStates.put(biokoHierarchy.MAP_AREA_STATE, mapAreaFormList);
            formsForStates.put(biokoHierarchy.SECTOR_STATE, sectorFormList);
            formsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, householdFormList);
            formsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, individualFormList);
            formsForStates.put(biokoHierarchy.BOTTOM_STATE, bottomFormList);

            // these details are off by 1: details for an individual should be
            // shown when you click a specific individual which is technically
            // in the bottom state.
            detailFragsForStates.put(biokoHierarchy.REGION_STATE, null);
            detailFragsForStates.put(biokoHierarchy.PROVINCE_STATE, null);
            detailFragsForStates.put(biokoHierarchy.DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SUB_DISTRICT_STATE, null);
            detailFragsForStates.put(biokoHierarchy.MAP_AREA_STATE, null);
            detailFragsForStates.put(biokoHierarchy.SECTOR_STATE, null);
            detailFragsForStates.put(biokoHierarchy.HOUSEHOLD_STATE, null);
            detailFragsForStates.put(biokoHierarchy.INDIVIDUAL_STATE, null);
            detailFragsForStates.put(biokoHierarchy.BOTTOM_STATE,
                    new IndividualDetailFragment());

        }

        @Override
        public Map<String, DetailFragment> getDetailFragsForStates() {
            return detailFragsForStates;
        }

        @Override
        public Map<String, List<FormBehaviour>> getFormsForStates() {
            return formsForStates;
        }

        @Override
        public QueryHelper getQueryHelper() {

            return new CensusQueryHelper();
        }

        @Override
        public ModuleUiHelper getModuleUiHelper() {
            return new UpdateUiHelper();
        }

        @Override
        public HierarchyInfo getHierarchyInfo() {
            return biokoHierarchy;
        }

    }


}
