package org.openhds.mobile.projectdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhds.mobile.R;
import org.openhds.mobile.model.FormBehaviour;

public class ProjectActivityBuilder {

	public static final String ACTIVITY_MODULE_EXTRA = "CensusActivityModule";

	// INTERFACE FOR ALL THE INNER CLASSES
	public interface ActivityBuilderModule {
		public Map<String, Integer> getStateLabels();

		public List<String> getStateSequence();

		public QueryHelper getQueryHelper();

		public Map<String, List<FormBehaviour>> getFormsforstates();

	}

	public static class CensusActivityBuilder implements
			ActivityBuilderModule, Serializable {
		private static final long serialVersionUID = -2241775877237937942L;

		public static final String REGION_STATE = "region";
		public static final String PROVINCE_STATE = "province";
		public static final String DISTRICT_STATE = "district";
		public static final String MAP_AREA_STATE = "mapArea";
		public static final String SECTOR_STATE = "sector";
		public static final String HOUSEHOLD_STATE = "household";
		public static final String INDIVIDUAL_STATE = "individual";
		public static final String BOTTOM_STATE = "bottom";

		private static final Map<String, Integer> stateLabels = new HashMap<String, Integer>();
		private static final List<String> stateSequence = new ArrayList<String>();
		private static final Map<String, List<FormBehaviour>> formsForStates = new HashMap<String, List<FormBehaviour>>();

		static {

			stateLabels.put(REGION_STATE, R.string.region_label);
			stateLabels.put(PROVINCE_STATE, R.string.province_label);
			stateLabels.put(DISTRICT_STATE, R.string.district_label);
			stateLabels.put(MAP_AREA_STATE, R.string.map_area_label);
			stateLabels.put(SECTOR_STATE, R.string.sector_label);
			stateLabels.put(HOUSEHOLD_STATE, R.string.household_label);
			stateLabels.put(INDIVIDUAL_STATE, R.string.individual_label);
			stateLabels.put(BOTTOM_STATE, R.string.bottom_label);

			stateSequence.add(REGION_STATE);
			stateSequence.add(PROVINCE_STATE);
			stateSequence.add(DISTRICT_STATE);
			stateSequence.add(MAP_AREA_STATE);
			stateSequence.add(SECTOR_STATE);
			stateSequence.add(HOUSEHOLD_STATE);
			stateSequence.add(INDIVIDUAL_STATE);
			stateSequence.add(BOTTOM_STATE);

			ArrayList<FormBehaviour> regionFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> provinceFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> districtFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> mapAreaFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> sectorFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> householdFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> individualFormList = new ArrayList<FormBehaviour>();
			ArrayList<FormBehaviour> bottomFormList = new ArrayList<FormBehaviour>();

			individualFormList.add(new FormBehaviour("Individual",
					R.string.create_head_of_household_label, null,
					new CensusFormFilter.AddHeadOfHousehold()));
			individualFormList.add(new FormBehaviour("Individual",
					R.string.add_member_of_household_label, null,
					new CensusFormFilter.AddMemberOfHousehold()));

			bottomFormList.add(new FormBehaviour("Individual",
					R.string.edit_individual_label, INDIVIDUAL_STATE,
					new CensusFormFilter.EditIndividual()));

			formsForStates.put(REGION_STATE, regionFormList);
			formsForStates.put(PROVINCE_STATE, provinceFormList);
			formsForStates.put(DISTRICT_STATE, districtFormList);
			formsForStates.put(MAP_AREA_STATE, mapAreaFormList);
			formsForStates.put(SECTOR_STATE, sectorFormList);
			formsForStates.put(HOUSEHOLD_STATE, householdFormList);
			formsForStates.put(INDIVIDUAL_STATE, individualFormList);
			formsForStates.put(BOTTOM_STATE, bottomFormList);

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
		public Map<String, List<FormBehaviour>> getFormsforstates() {
			return formsForStates;
		}

		@Override
		public QueryHelper getQueryHelper() {

			return new CensusQueryHelper();
		}

	}

	public static CensusActivityBuilder getCensusActivityBuilder() {

		return new CensusActivityBuilder();
	}

}
