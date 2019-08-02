package com.viettel.ocs.oam.reportserver.es.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "avg_percent_cpu", "avg_percent_ram" })
	public static class Summary {
		
		@JsonProperty("avg_percent_cpu")
		private float avg_percent_cpu;
		
		@JsonProperty("avg_percent_ram")
		private float avg_percent_ram;
		
		@JsonIgnore
	    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
		
		@JsonProperty("avg_percent_cpu")
		public float getAvg_percent_cpu() {
			return avg_percent_cpu;
		}
		
		@JsonProperty("avg_percent_cpu")
		public void setAvg_percent_cpu(float avg_percent_cpu) {
			this.avg_percent_cpu = avg_percent_cpu;
		}
		
		@JsonProperty("avg_percent_ram")
		public float getAvg_percent_ram() {
			return avg_percent_ram;
		}
		
		@JsonProperty("avg_percent_ram")
		public void setAvg_percent_ram(float avg_percent_ram) {
			this.avg_percent_ram = avg_percent_ram;
		}
		
		@JsonAnyGetter
	    public Map<String, Object> getAdditionalProperties() {
	        return this.additionalProperties;
	    }

	    @JsonAnySetter
	    public void setAdditionalProperty(String name, Object value) {
	        this.additionalProperties.put(name, value);
	    }
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "node_name", "statistic_result" })
	public static class Node {
		
		@JsonProperty("node_name")
		private String node_name;
		
		@JsonProperty("statistic_result")
		private Summary statistic_result;
		
		@JsonIgnore
	    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
		
		@JsonProperty("node_name")
		public String getNode_name() {
			return node_name;
		}
		
		@JsonProperty("node_name")
		public void setNode_name(String node_name) {
			this.node_name = node_name;
		}
		
		@JsonProperty("statistic_result")
		public Summary getStatistic_result() {
			return statistic_result;
		}
		
		@JsonProperty("statistic_result")
		public void setStatistic_result(Summary statistic_result) {
			this.statistic_result = statistic_result;
		}
		
		@JsonAnyGetter
	    public Map<String, Object> getAdditionalProperties() {
	        return this.additionalProperties;
	    }

	    @JsonAnySetter
	    public void setAdditionalProperty(String name, Object value) {
	        this.additionalProperties.put(name, value);
	    }
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "group_by_time", "array_node_name" })
	public static class Time {
		
		@JsonProperty("group_by_time")
		private String group_by_time;
		
		@JsonProperty("array_node_name")
		private List<Node> array_node_name;
		
		@JsonIgnore
	    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
		
		@JsonProperty("group_by_time")
		public String getGroup_by_time() {
			return group_by_time;
		}
		
		@JsonProperty("group_by_time")
		public void setGroup_by_time(String group_by_time) {
			this.group_by_time = group_by_time;
		}
		
		@JsonProperty("array_node_name")
		public List<Node> getArray_node_name() {
			return array_node_name;
		}
		
		@JsonProperty("array_node_name")
		public void setArray_node_name(List<Node> array_node_name) {
			this.array_node_name = array_node_name;
		}
		
		@JsonAnyGetter
	    public Map<String, Object> getAdditionalProperties() {
	        return this.additionalProperties;
	    }

	    @JsonAnySetter
	    public void setAdditionalProperty(String name, Object value) {
	        this.additionalProperties.put(name, value);
	    }
	}
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonPropertyOrder({ "json_response" })
	public static class Response {
		
		@JsonProperty("json_response")
		private List<Time> json_response;
		
		@JsonIgnore
	    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
		
		@JsonProperty("json_response")
		public List<Time> getJson_response() {
			return json_response;
		}
		
		@JsonProperty("json_response")
		public void setJson_response(List<Time> json_response) {
			this.json_response = json_response;
		}
		
		@JsonAnyGetter
	    public Map<String, Object> getAdditionalProperties() {
	        return this.additionalProperties;
	    }

	    @JsonAnySetter
	    public void setAdditionalProperty(String name, Object value) {
	        this.additionalProperties.put(name, value);
	    }
	}
}
