package com.viettel.ocs.oam.reportserver.es.util;

public class Error {
	public static class Exception1 extends Exception {
		@Override
		public String getMessage() {
			return "Time string format is not valid.";
		}
	}
	
	public static class Exception7 extends Exception {
		@Override
		public String getMessage() {
			return "The number of fields in groupFields is not satisfied.";
		}
	}
	
	public static class Exception13 extends Exception {
		@Override
		public String getMessage() {
			return "kpiFieldsQuerys must not be empty or null.";
		}
	}
	
	public static class Exception17 extends Exception {
		@Override
		public String getMessage() {
			return "Group Operator is not correct. The set of valid group operators is {asc, desc, none}.";
		}
	}
	
	public static class Exception18 extends Exception {
		@Override
		public String getMessage() {
			return "calculatedFields must not be null.";
		}
	}
	
	public static class Exception19 extends Exception {
		@Override
		public String getMessage() {
			return "Calculation Operator is not correct.";
		}
	}
	
	public static class Exception20 extends Exception {
		@Override
		public String getMessage() {
			return "orderFields is not valid.";
		}
	}
	
	public static class Exception21 extends Exception {
		@Override
		public String getMessage() {
			return "Operator in orderFields is not correct. The set of valid operators is {asc, desc, none}.";
		}
	}
	
	public static class Exception22 extends Exception {
		@Override
		public String getMessage() {
			return "Field 'fromTime' is not found.";
		}
	}
	
	public static class Exception23 extends Exception {
		@Override
		public String getMessage() {
			return "Field 'toTime' is not found.";
		}
	}
	
	public static class Exception25 extends Exception {
		@Override
		public String getMessage() {
			return "Field 'timeField' is not found.";
		}
	}
	
	public static class Exception26 extends Exception {
		@Override
		public String getMessage() {
			return "Index is not found.";
		}
	}
}
