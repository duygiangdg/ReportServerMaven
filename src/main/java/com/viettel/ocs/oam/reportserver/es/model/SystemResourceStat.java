package com.viettel.ocs.oam.reportserver.es.model;

public class SystemResourceStat {
	private String node_name;
	private int percent_cpu;
	private int total_ram;
	private int usage_ram;
	private int percent_ram;
	private int swap_ram;
	private long time;
	
	public String getNode_name() {
		return node_name;
	}
	public void setNode_name(String node) {
		this.node_name = node;
	}
	public int getPercent_cpu() {
		return percent_cpu;
	}
	public void setPercent_cpu(int percent_cpu) {
		this.percent_cpu = percent_cpu;
	}
	public int getTotal_ram() {
		return total_ram;
	}
	public void setTotal_ram(int total_ram) {
		this.total_ram = total_ram;
	}
	public int getUsage_ram() {
		return usage_ram;
	}
	public void setUsage_ram(int usage_ram) {
		this.usage_ram = usage_ram;
	}
	public int getPercent_ram() {
		return percent_ram;
	}
	public void setPercent_ram(int percent_ram) {
		this.percent_ram = percent_ram;
	}
	public int getSwap_ram() {
		return swap_ram;
	}
	public void setSwap_ram(int swap_ram) {
		this.swap_ram = swap_ram;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
