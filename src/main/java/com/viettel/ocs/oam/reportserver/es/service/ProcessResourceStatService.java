package com.viettel.ocs.oam.reportserver.es.service;

import java.util.Map;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.ocs.oam.reportserver.es.model.ProcessResourceStat;

@Service
public class ProcessResourceStatService {
	private RestHighLevelClient client;
    private ObjectMapper objectMapper;
    
    @Autowired
    public ProcessResourceStatService(RestHighLevelClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
    }
    
    public ProcessResourceStat findById(String id) throws Exception {
        GetRequest getRequest = new GetRequest("process_resource_stat", "process_resource_stat", id);
        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> resultMap = getResponse.getSource();

        return objectMapper.convertValue(resultMap, ProcessResourceStat.class);
    }
}
