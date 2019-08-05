package com.viettel.ocs.oam.reportserver.es.exception;

public class InvalidRequestException extends Exception {
	private static final long serialVersionUID = -2641559752458173057L;
	private String message;
	
	public InvalidRequestException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
