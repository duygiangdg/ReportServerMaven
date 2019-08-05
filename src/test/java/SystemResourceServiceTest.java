import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.reportserver.es.config.ElasticsearchConfig;
import com.viettel.ocs.oam.reportserver.es.exception.InvalidRequestException;
import com.viettel.ocs.oam.reportserver.es.service.SystemResourceService;
import com.viettel.ocs.oam.reportserver.es.util.LineChart;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class SystemResourceServiceTest {

	@Autowired
	SystemResourceService service;
	
	@Test
	public void getSummary() {
		try {
			Map<String, Map<String, Map<String, Double>>> response = 
					service.getSummary("2019-07-24 10:00:00", "2019-07-25 10:00:00", 30);
			for (Entry<String, Map<String, Map<String, Double>>> nodeEntry: response.entrySet()) {
				String nodeKey = nodeEntry.getKey();
				Map<String, Map<String, Double>> nodeValue = nodeEntry.getValue();
				List<String> groupList = new ArrayList<String>();
				groupList.add(nodeKey);
				LineChart.drawChartToImage(nodeValue, groupList,
						"src/main/resources/system_resource_summary-" + nodeKey + ".png");
			}
			
		} catch (InvalidRequestException | IOException | ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
