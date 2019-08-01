package com.viettel.ocs.oam.reportserver.es.model;

public class SystemResourceSummary {
	private float avg_percent_cpu;
	private float max_percent_cpu;
	private float avg_percent_ram;
	private float max_percent_ram;
	
	public float getAvg_percent_cpu() {
		return avg_percent_cpu;
	}
	public void setAvg_percent_cpu(float avg_percent_cpu) {
		this.avg_percent_cpu = avg_percent_cpu;
	}
	public float getMax_percent_cpu() {
		return max_percent_cpu;
	}
	public void setMax_percent_cpu(float max_percent_cpu) {
		this.max_percent_cpu = max_percent_cpu;
	}
	public float getAvg_percent_ram() {
		return avg_percent_ram;
	}
	public void setAvg_percent_ram(float avg_percent_ram) {
		this.avg_percent_ram = avg_percent_ram;
	}
	public float getMax_percent_ram() {
		return max_percent_ram;
	}
	public void setMax_percent_ram(float max_percent_ram) {
		this.max_percent_ram = max_percent_ram;
	}

	
}
