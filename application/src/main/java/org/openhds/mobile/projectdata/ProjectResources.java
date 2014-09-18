package org.openhds.mobile.projectdata;

import java.util.HashMap;
import java.util.Map;

import org.openhds.mobile.R;

public class ProjectResources {

    public static class General {

        public static final String FORM_NEEDS_REVIEW = "0";
        public static final String FORM_NO_REVIEW_NEEDED = "1";

    }

    public static final class Individual {

		/*
		 * INDIVIDUAL mapping of Database/Form VALUES to String resources IDs
		 */

        public static final Map<String, Integer> Individual = new HashMap<>();

        public static final String RESIDENCY_END_TYPE_NA = "NA";
        public static final String RESIDENCY_END_TYPE_OMG = "OMG";

        public static final String END_TYPE_DEATH = "DTH";

        public static final String AGE_UNITS_YEARS = "Years";
        public static final String AGE_UNITS_MONTHS = "Months";

        public static final String GENDER_MALE = "Male";
        public static final String GENDER_FEMALE = "Female";

        public static final String LANGUAGE_PREF_SPANISH = "Spanish";
        public static final String LANGUAGE_PREF_FANG = "Fang";
        public static final String LANGUAGE_PREF_BUBE = "Bubi";
        public static final String LANGUAGE_PREF_ENGLISH = "English";
        public static final String LANGUAGE_PREF_FRENCH = "French";

        private static final String STATUS_PERMANENT = "Permanent";
        private static final String STATUS_VISITOR = "Visitor";
        static {
            Individual.put(AGE_UNITS_YEARS, R.string.db_val_age_units_years);
            Individual.put(AGE_UNITS_MONTHS, R.string.db_val_age_units_months);
            Individual.put(GENDER_MALE, R.string.db_val_gender_male);
            Individual.put(GENDER_FEMALE, R.string.db_val_gender_female);
            Individual.put(LANGUAGE_PREF_SPANISH,
                    R.string.db_val_language_pref_spanish);
            Individual.put(LANGUAGE_PREF_FANG,
                    R.string.db_val_language_pref_fang);
            Individual.put(LANGUAGE_PREF_BUBE,
                    R.string.db_val_language_pref_bubi);
            Individual.put(LANGUAGE_PREF_ENGLISH,
                    R.string.db_val_language_pref_english);
            Individual.put(LANGUAGE_PREF_FRENCH,
                    R.string.db_val_language_pref_french);
            Individual.put(STATUS_PERMANENT, R.string.db_val_status_permanent);
            Individual.put(STATUS_VISITOR, R.string.db_val_status_visitor);
        }

        public static int getIndividualStringId(String key) {
            if (Individual.containsKey(key)) {
                return Individual.get(key);
            } else {
                return 0;
            }
        }
    }

    public static final class Relationship {
		/*
		 * RELATIONSHIPS mapping of Database/Form VALUES to String resources IDs
		 */

        private static final Map<String, Integer> Relationship = new HashMap<>();

        public static final String RELATION_TO_HOH_TYPE_HEAD = "1";
        public static final String RELATION_TO_HOH_TYPE_SPOUSE = "2";
        public static final String RELATION_TO_HOH_TYPE_SON_DAUGHTER = "3";
        public static final String RELATION_TO_HOH_TYPE_BROTHER_SISTER = "4";
        public static final String RELATION_TO_HOH_TYPE_PARENT = "5";
        public static final String RELATION_TO_HOH_TYPE_GRANDCHILD = "6";
        public static final String RELATION_TO_HOH_TYPE_NOT_RELATED = "7";
        public static final String RELATION_TO_HOH_TYPE_OTHER_RELATIVE = "8";
        public static final String RELATION_TO_HOH_TYPE_DONT_KNOW = "9";

        static {
            Relationship.put(RELATION_TO_HOH_TYPE_HEAD,
                    R.string.db_val_relation_to_head_type_head);
            Relationship.put(RELATION_TO_HOH_TYPE_SPOUSE,
                    R.string.db_val_relation_to_head_type_spouse);
            Relationship.put(RELATION_TO_HOH_TYPE_SON_DAUGHTER,
                    R.string.db_val_relation_to_head_type_son_daughter);
            Relationship.put(RELATION_TO_HOH_TYPE_BROTHER_SISTER,
                    R.string.db_val_relation_to_head_type_brother_sister);
            Relationship.put(RELATION_TO_HOH_TYPE_PARENT,
                    R.string.db_val_relation_to_head_type_parent);
            Relationship.put(RELATION_TO_HOH_TYPE_GRANDCHILD,
                    R.string.db_val_relation_to_head_type_grandchild);
            Relationship.put(RELATION_TO_HOH_TYPE_NOT_RELATED,
                    R.string.db_val_relation_to_head_type_not_related);
            Relationship.put(RELATION_TO_HOH_TYPE_OTHER_RELATIVE,
                    R.string.db_val_relation_to_head_type_other_relative);
            Relationship.put(RELATION_TO_HOH_TYPE_DONT_KNOW,
                    R.string.db_val_relation_to_head_type_dont_know);
        }

        public static int getRelationshipStringId(String key) {
            if (Relationship.containsKey(key)) {
                return Relationship.get(key);
            } else {
                return 0;
            }
        }

    }

    public static final class FormType {

        //provides a mapping between JR form id and android string resource id

        private static final Map<String, Integer> FormType = new HashMap<>();

        private static final String FORM_TYPE_INDIVIDUAL = "individual";
        private static final String FORM_TYPE_LOCATION = "location";
        private static final String FORM_TYPE_VISIT = "visit";
        private static final String FORM_TYPE_IN_MIGRATION = "in_migration";
        private static final String FORM_TYPE_OUT_MIGRATION = "out_migration";
        private static final String FORM_TYPE_BED_NET = "bed_net";
        private static final String FORM_TYPE_DEATH = "death";
        private static final String FORM_TYPE_PREGNANCY_OBSERVATION = "pregnancy_observation";

        static {
            FormType.put(FORM_TYPE_INDIVIDUAL, R.string.form_type_individual);
            FormType.put(FORM_TYPE_LOCATION, R.string.form_type_location);
            FormType.put(FORM_TYPE_VISIT, R.string.form_type_visit);
            FormType.put(FORM_TYPE_IN_MIGRATION, R.string.form_type_in_migration);
            FormType.put(FORM_TYPE_OUT_MIGRATION, R.string.form_type_out_migration);
            FormType.put(FORM_TYPE_BED_NET, R.string.form_type_bed_net);
            FormType.put(FORM_TYPE_DEATH, R.string.form_type_death);
            FormType.put(FORM_TYPE_PREGNANCY_OBSERVATION, R.string.form_type_pregnancy_observation);
        }

        public static int getFormTypeStringId(String key) {
            if (FormType.containsKey(key)) {
                return FormType.get(key);
            } else {
                return R.string.form_type_other;
            }
        }
    }
}
