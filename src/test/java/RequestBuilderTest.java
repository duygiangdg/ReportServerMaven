import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
import com.viettel.ocs.oam.reportserver.es.util.RequestBuilder;
import com.viettel.ocs.oam.reportserver.es.util.RequestWrapper;
import com.viettel.ocs.oam.reportserver.es.util.ResponseParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class RequestBuilderTest {
	@Autowired
	RestHighLevelClient client;
	
	@Test
	public void mapStatisticInMinutes() {
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
		builder.setFromTime("2019-07-20 10:00:00");
		builder.setToTime("2019-07-30 10:00:00");
		builder.setTimeField("time");
		builder.setInterval(5);
		RequestWrapper requestWrapper;

		try {
			requestWrapper = builder.build();
			String index = "application_resource_stat_2019_07_20_3months";

			SearchRequest request;
			request = RequestBuilder.statisticInMinutes(requestWrapper, index);

			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);
			ResponseParser.mapStatisticInMinutes(response, keyGroupFields);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}
}
