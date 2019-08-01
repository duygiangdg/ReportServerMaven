package com.viettel.ocs.oam.reportserver.es.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.ocs.oam.reportserver.es.model.SystemResourceStat;

@Service
public class FakeDataService {
	private RestHighLevelClient client;
	private ObjectMapper objectMapper;
	
    @Autowired
    public FakeDataService(RestHighLevelClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }
    
    public void generateData(String indexName, int interval, String fromTime, String toTime) throws ParseException, IOException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long fromTimeMillis = sdf.parse(fromTime).getTime();
		long toTimeMillis = sdf.parse(toTime).getTime();
		interval *= 1000; // convert to millisecond
		int id = 1;
		
		Random random = new Random();
		String[] nodeNames = new String[] { "node1", "node2", "node3" };

		// Generate random records
		while (fromTimeMillis < toTimeMillis) {
			SystemResourceStat systemResourceStat = new SystemResourceStat();
			systemResourceStat.setTime(fromTimeMillis);
			fromTimeMillis += interval;
			systemResourceStat.setNode_name(nodeNames[random.nextInt(nodeNames.length)]);
			systemResourceStat.setPercent_cpu(random.nextInt(100));
			systemResourceStat.setTotal_ram(random.nextInt(100));
			systemResourceStat.setUsage_ram(random.nextInt(100));
			systemResourceStat.setSwap_ram(random.nextInt(100));
			
	        String json = this.objectMapper.writeValueAsString(systemResourceStat);
			
			IndexRequest indexRequest = new IndexRequest(
				"system_resource_stat_2019_07_20_3months", "system_resource_stat"
			).source(json, XContentType.JSON);

			UpdateRequest updateRequest = new UpdateRequest(
				"system_resource_stat_2019_07_20_3months", "system_resource_stat", "" + id++
			).upsert(indexRequest);

		    updateRequest.doc(indexRequest);
		    client.update(updateRequest, RequestOptions.DEFAULT);
		    System.out.println("Created doc: " + json);
		}
    }
}
