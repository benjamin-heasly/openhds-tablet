package org.openhds.mobile.projectdata;

import java.util.Map;

import org.openhds.mobile.activity.Skeletor;
import org.openhds.mobile.database.queries.Converter;
import org.openhds.mobile.database.queries.Queries;
import org.openhds.mobile.database.queries.QueryResult;
import org.openhds.mobile.model.SocialGroup;

import android.database.Cursor;

// These are not necessarily 1 to 1 with the form types, 
// but instead filter when a form's behaviour may or may not be appropriate
// i.e. after you 'add a head of household' you no longer have to display the
// button (aka it's amIValid() == false).
public class CensusFormFilters {

	private static boolean hasHeadOfHousehold(Skeletor skeletor,
			Map<String, QueryResult> hierarchyPath) {

		String socialGroupExtId = hierarchyPath.get(
				ProjectActivityBuilder.CensusActivityBuilder.HOUSEHOLD_STATE)
				.getExtId();

		Cursor socialGroupCursor = Queries.getSocialGroupByExtId(
				skeletor.getContentResolver(), socialGroupExtId);

		if (socialGroupCursor.moveToFirst()) {

			SocialGroup sg = Converter.toSocialGroup(socialGroupCursor, true);

			if (!"UNK".equals(sg.getGroupHead())) {

				return true;

			}
		}
		return false;
	}

	public static class AddHeadOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return !CensusFormFilters.hasHeadOfHousehold(skeletor,
					skeletor.getHierarchyPath());
		}

	}

	public static class AddMemberOfHousehold implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return CensusFormFilters.hasHeadOfHousehold(skeletor,
					skeletor.getHierarchyPath());
		}

	}

	public static class EditIndividual implements FormFilter {

		@Override
		public boolean amIValid(Skeletor skeletor) {

			return true;
		}

	}

}
