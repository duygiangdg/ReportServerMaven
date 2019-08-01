import static org.junit.Assert.fail;

import java.util.LinkedHashMap;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.reportserver.es.config.ElasticsearchConfig;
import com.viettel.ocs.oam.reportserver.es.util.InvalidRequestException;
import com.viettel.ocs.oam.reportserver.es.util.RequestBuilder;
import com.viettel.ocs.oam.reportserver.es.util.RequestWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class RequestBuilderTest {

	@Autowired
    RestHighLevelClient client;
	
	@Test
	public void testStatisticMaxMinAvgSum() {
		LinkedHashMap<String, String> groupFields = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> calculatedFields = new LinkedHashMap<String, String>();
		groupFields.put("node_name", "none");
		calculatedFields.put("percent_cpu", "min");
		calculatedFields.put("percent_ram", "min");
		
		RequestWrapper.Builder builder = new RequestWrapper.Builder();
		builder.setGroupFields(groupFields);
		builder.setCalculatedFields(calculatedFields);
		builder.setFromTime("2019-07-24 10:00:00");
		builder.setToTime("2019-07-25 10:00:00");
		builder.setTimeField("time");
		RequestWrapper requestWrapper;
		try {
			requestWrapper = builder.build();
			RequestBuilder requestBuilder = new RequestBuilder();
			String index = "application_resource_stat_2019_07_20_3months";
			
			SearchRequest request;
			request = requestBuilder.statisticMaxMinAvgSum(requestWrapper, index);
			
			System.out.println(request);
		} catch (InvalidRequestException ex) {
			ex.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testStatisticInMinutes() {
		LinkedHashMap<String, String> groupFields = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> calculatedFields = new LinkedHashMap<String, String>();
		groupFields.put("node_name", "none");
		calculatedFields.put("percent_cpu", "min");
		calculatedFields.put("percent_ram", "min");
		
		RequestWrapper.Builder builder = new RequestWrapper.Builder();
		builder.setGroupFields(groupFields);
		builder.setCalculatedFields(calculatedFields);
		builder.setFromTime("2019-07-20 10:00:00");
		builder.setToTime("2019-07-30 10:00:00");
		builder.setTimeField("time");
		builder.setInterval(5);
		RequestWrapper requestWrapper;
		
		try {
			requestWrapper = builder.build();
			RequestBuilder requestBuilder = new RequestBuilder();
			String index = "application_resource_stat_2019_07_20_3months";
			
			SearchRequest request;
			request = requestBuilder.statisticInMinutes(requestWrapper, index);
			
			System.out.println(request);
			
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);
			String processedResponse = requestBuilder.processStatisticInMinutes(response, keyGroupFields);
			
			System.out.println(processedResponse);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}
}
