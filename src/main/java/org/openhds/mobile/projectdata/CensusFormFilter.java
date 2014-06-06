package org.openhds.mobile.projectdata;

import java.util.ArrayList;
import java.util.Map;

import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.queries.QueryResult;

public class CensusFormFilter {

	public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return true;
		}

	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return false;
		}

	}

	public static class EditIndividual implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return true;
		}

	}

}
