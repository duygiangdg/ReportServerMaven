import static org.junit.Assert.*;

import java.util.LinkedHashMap;

import org.elasticsearch.action.search.SearchRequest;
import org.junit.Test;

import com.viettel.ocs.oam.reportserver.es.util.RequestBuilder;
import com.viettel.ocs.oam.reportserver.es.util.RequestWrapper;

public class RequestBuilderTest {

	@Test
	public void test() {
		LinkedHashMap<String, String> groupFields = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> calculatedFields = new LinkedHashMap<String, String>();
		groupFields.put("percent_cpu", "asc");
		calculatedFields.put("percent_cpu", "min");
		
		RequestWrapper requestWrapper = new RequestWrapper();
		requestWrapper.setGroupFields(groupFields);
		requestWrapper.setCalculatedFields(calculatedFields);
		requestWrapper.setFromTime("2019-07-24 10:00:00");
		requestWrapper.setToTime("2019-07-25 10:00:00");
		requestWrapper.setTimeField("time");
		
		RequestBuilder requestBuilder = new RequestBuilder();
		String index = "application_resource_stat_2019_07_20_3months";
		
		try {
			SearchRequest request = requestBuilder.statisticMaxMinAvgSum(requestWrapper, index);
			System.out.println(request);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
