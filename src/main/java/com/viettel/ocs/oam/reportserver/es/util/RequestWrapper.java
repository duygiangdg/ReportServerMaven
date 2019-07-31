package com.viettel.ocs.oam.reportserver.es.util;

import java.util.LinkedHashMap;

public class RequestWrapper {
	
	private LinkedHashMap<String, String[]> whereFields;
	private String problemType;
	private LinkedHashMap<String, String> groupFields;
	private LinkedHashMap<String, String> calculatedFields;
	private LinkedHashMap<String, String> orderFields;
	private int size;
	private String fromTime;
	private String toTime;
	private int interval;
	private String timeField;
	
	
	public String getProblemType() {
		return problemType;
	}
	public void setProblemType(String problemType) {
		this.problemType = problemType;
	}
	
	public LinkedHashMap<String, String[]> getWhereFields() {
		return whereFields;
	}
	public void setWhereFields(LinkedHashMap<String, String[]> whereFields) {
		this.whereFields = whereFields;
	}
	public LinkedHashMap<String, String> getGroupFields() {
		return groupFields;
	}
	public void setGroupFields(LinkedHashMap<String, String> groupFields) {
		this.groupFields = groupFields;
	}
	public LinkedHashMap<String, String> getCalculatedFields() {
		return calculatedFields;
	}
	public void setCalculatedFields(LinkedHashMap<String, String> calculatedFields) {
		this.calculatedFields = calculatedFields;
	}
	public LinkedHashMap<String, String> getOrderFields() {
		return orderFields;
	}
	public void setOrderFields(LinkedHashMap<String, String> orderFields) {
		this.orderFields = orderFields;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getFromTime() {
		return fromTime;
	}
	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}
	public String getToTime() {
		return toTime;
	}
	public void setToTime(String toTime) {
		this.toTime = toTime;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public String getTimeField() {
		return timeField;
	}
	public void setTimeField(String timeField) {
		this.timeField = timeField;
	}
	@Override
	public String toString() {
		return "RequestWrapper [whereFields=" + whereFields + ", groupFields=" + groupFields + ", calculatedFields="
				+ calculatedFields + ", orderFields=" + orderFields + ", size=" + size + ", fromTime=" + fromTime
				+ ", toTime=" + toTime + ", interval=" + interval + ", timeField=" + timeField + "]";
	}
	
}

