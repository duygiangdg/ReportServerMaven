package com.viettel.ocs.oam.reportserver.es.service;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.ocs.oam.reportserver.es.model.SystemResourceStat;
import com.viettel.ocs.oam.reportserver.es.util.InvalidRequestException;
import com.viettel.ocs.oam.reportserver.es.util.RequestBuilder;
import com.viettel.ocs.oam.reportserver.es.util.RequestWrapper;
import com.viettel.ocs.oam.reportserver.es.util.ResponseParser;

@Service
public class SystemResourceService {
	private final static Logger logger = Logger.getLogger(SystemResourceService.class);

	private RestHighLevelClient client;
	private ObjectMapper objectMapper;

	@Autowired
	public SystemResourceService(RestHighLevelClient client) {
		this.client = client;
		this.objectMapper = new ObjectMapper();
	}

	public SystemResourceStat.Response getSummary(String fromTime, String toTime, int interval)
			throws InvalidRequestException, IOException {

		LinkedHashMap<String, String> groupFields = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> calculatedFields = new LinkedHashMap<String, String>();
		groupFields.put("node_name", "none");
		calculatedFields.put("percent_cpu", "avg");
		calculatedFields.put("percent_ram", "avg");

		RequestWrapper.Builder builder = new RequestWrapper.Builder();
		builder.setGroupFields(groupFields);
		builder.setCalculatedFields(calculatedFields);
		builder.setFromTime(fromTime);
		builder.setToTime(toTime);
		builder.setTimeField("time");
		builder.setInterval(interval);
		RequestWrapper requestWrapper;

		requestWrapper = builder.build();
		String index = "system_resource_stat_2019_07_20_3months";

		SearchRequest request;
		request = RequestBuilder.statisticInMinutes(requestWrapper, index);

		logger.debug(request);

		SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
		String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);
		String jsonResponse = ResponseParser.processStatisticInMinutes(searchResponse, keyGroupFields);

		logger.debug(jsonResponse);

		SystemResourceStat.Response objectResponse = objectMapper.readValue(jsonResponse,
				SystemResourceStat.Response.class);

		return objectResponse;
	}
}