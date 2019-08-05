package com.viettel.ocs.oam.reportserver.es.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viettel.ocs.oam.reportserver.es.exception.InvalidRequestException;
import com.viettel.ocs.oam.reportserver.es.util.RequestBuilder;
import com.viettel.ocs.oam.reportserver.es.util.RequestWrapper;
import com.viettel.ocs.oam.reportserver.es.util.ResponseParser;

@Service
public class SystemResourceService {
	private RestHighLevelClient client;

	@Autowired
	public SystemResourceService(RestHighLevelClient client) {
		this.client = client;
	}

	public Map<String, Map<String, Map<String, Double>>> getSummary(String fromTime, String toTime, int interval)
			throws InvalidRequestException, IOException {

		LinkedHashMap<String, String> groupFields = new LinkedHashMap<String, String>();
		List<String[]> calculatedFields = new ArrayList<String[]>();
		groupFields.put("node_name", "none");
		calculatedFields.add(new String[] {"percent_cpu", "max"});
		calculatedFields.add(new String[] {"percent_ram", "max"});
		calculatedFields.add(new String[] {"percent_cpu", "avg"});
		calculatedFields.add(new String[] {"percent_ram", "avg"});

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

		SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
		String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);
		
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Map<String, Double>>> mapResponse = (Map<String, Map<String, Map<String, Double>>>)
				ResponseParser.mapStatisticInMinutes(searchResponse, keyGroupFields);

		return mapResponse;
	}
}