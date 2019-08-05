package com.viettel.ocs.oam.reportserver.es.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.viettel.ocs.oam.reportserver.es.exception.InvalidRequestException;

public class RequestWrapper {
	
	private LinkedHashMap<String, String[]> whereFields;
	private LinkedHashMap<String, String> groupFields;
	private List<String[]> calculatedFields;
	private int size;
	private String fromTime;
	private String toTime;
	private int interval;
	private String timeField;
	
	private RequestWrapper(
		LinkedHashMap<String, String[]> whereFields, LinkedHashMap<String, 
		String> groupFields, List<String[]> calculatedFields, int size, 
		String fromTime, String toTime, int interval, String timeField
	) {
		this.whereFields = whereFields;
		this.groupFields = groupFields;
		this.calculatedFields = calculatedFields;
		this.size = size;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.interval = interval;
		this.timeField = timeField;
	}
	
	public LinkedHashMap<String, String[]> getWhereFields() {
		return whereFields;
	}
	
	public LinkedHashMap<String, String> getGroupFields() {
		return groupFields;
	}
	
	public List<String[]> getCalculatedFields() {
		return calculatedFields;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getFromTime() {
		return fromTime;
	}
	
	public String getToTime() {
		return toTime;
	}

	public int getInterval() {
		return interval;
	}
	
	public String getTimeField() {
		return timeField;
	}
	
	@Override
	public String toString() {
		return "RequestWrapper [whereFields=" + whereFields + ", groupFields=" + groupFields + 
				", calculatedFields=" + calculatedFields + ", size=" + size + ", fromTime=" + fromTime
				+ ", toTime=" + toTime + ", interval=" + interval + ", timeField=" + timeField + "]";
	}
	
	public static class Builder {
		private LinkedHashMap<String, String[]> whereFields;
		private LinkedHashMap<String, String> groupFields;
		private List<String[]> calculatedFields;
		private int size;
		private String fromTime;
		private String toTime;
		private int interval;
		private String timeField;
		
		public Builder() {}
		
		public void setWhereFields(LinkedHashMap<String, String[]> whereFields) {
			this.whereFields = whereFields;
		}
		
		public void setGroupFields(LinkedHashMap<String, String> groupFields) {
			this.groupFields = groupFields;
		}
		
		public void setCalculatedFields(List<String[]> calculatedFields) {
			this.calculatedFields = calculatedFields;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public void setFromTime(String fromTime) {
			this.fromTime = fromTime;
		}

		public void setToTime(String toTime) {
			this.toTime = toTime;
		}

		public void setInterval(int interval) {
			this.interval = interval;
		}

		public void setTimeField(String timeField) {
			this.timeField = timeField;
		}
		
		public RequestWrapper build() throws InvalidRequestException {
			if (groupFields == null)
				throw new InvalidRequestException("groupField must not be null");
			
			if (groupFields.size() > 4)
				throw new InvalidRequestException("The number of fields in groupFields is not satisfied.");
			
			for (Map.Entry<String, String> mapElement : groupFields.entrySet()) {
				String orderOperator = mapElement.getValue();
				if (!isValidGroupOperator(orderOperator))
					throw new InvalidRequestException("Group Operator is not correct. The set of valid group operators is {asc, desc, none}.");
			}
			
			if (calculatedFields == null)
				throw new InvalidRequestException("calculatedFields must not be null.");
			
			for (String[] entry : calculatedFields) {
				String calOperator = entry[1];
				if (!isValidCalculationOperator(calOperator))
					throw new InvalidRequestException("Calculation Operator is not correct.");
			}
			
			// Check if fromTime field is null and its format
			if (fromTime == null)
				throw new InvalidRequestException("Field 'fromTime' is not found.");
			else if (!isValidFormat("yyyy-MM-dd HH:mm:ss", fromTime)) {
				throw new InvalidRequestException("Time string format is not valid.");
			}

			// Check if toTime field is null and its format
			if (toTime == null)
				throw new InvalidRequestException("Field 'toTime' is not found.");
			else if (!isValidFormat("yyyy-MM-dd HH:mm:ss", toTime)) {
				throw new InvalidRequestException("Time string format is not valid.");
			}

			// Check if timeField is null
			if (timeField == null)
				throw new InvalidRequestException("Field 'timeField' is not found.");
			
			return new RequestWrapper(whereFields, groupFields, calculatedFields,
					size, fromTime, toTime, interval, timeField);
		}
		
		// Check the validity of Group Operator
		public boolean isValidGroupOperator(String groupOperator) {
			if (groupOperator == null)
				return false;
			if (groupOperator.equalsIgnoreCase("asc"))
				return true;
			if (groupOperator.equalsIgnoreCase("desc"))
				return true;
			if (groupOperator.equalsIgnoreCase("none"))
				return true;
			return false;
		}

		// Check the validity of Calculation Operator
		public boolean isValidCalculationOperator(String calOperator) {
			if (calOperator == null)
				return false;
			if (calOperator.equalsIgnoreCase("max"))
				return true;
			if (calOperator.equalsIgnoreCase("min"))
				return true;
			if (calOperator.equalsIgnoreCase("avg"))
				return true;
			if (calOperator.equalsIgnoreCase("sum"))
				return true;
			return false;
		}
		
		// Check the validity of time string
		public boolean isValidFormat(String timeFormat, String timeString) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
				// Convert String to Date
				Date date = sdf.parse(timeString);
				// Format date as a string, and compare to original timeString
				if (timeString.equals(sdf.format(date))) {
					return true;
				}
			} catch (ParseException ex) {
				return false;
			}
			return false;
		}
	}
}

