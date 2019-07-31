package com.viettel.ocs.oam.reportserver.es.model;

import lombok.Data;

@Data
public class ProcessResourceStat {
	private String process_name;
	private String node_name;
	private int percent_cpu;
	private int percent_ram;
	private String time;
	public String getProcess_name() {
		return process_name;
	}
	public void setProcess_name(String process_name) {
		this.process_name = process_name;
	}
	public String getNode_name() {
		return node_name;
	}
	public void setNode_name(String node_name) {
		this.node_name = node_name;
	}
	public int getPercent_cpu() {
		return percent_cpu;
	}
	public void setPercent_cpu(int percent_cpu) {
		this.percent_cpu = percent_cpu;
	}
	public int getPercent_ram() {
		return percent_ram;
	}
	public void setPercent_ram(int percent_ram) {
		this.percent_ram = percent_ram;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
}
