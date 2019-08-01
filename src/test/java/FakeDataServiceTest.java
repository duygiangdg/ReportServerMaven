import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.reportserver.es.config.ElasticsearchConfig;
import com.viettel.ocs.oam.reportserver.es.service.FakeDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class FakeDataServiceTest {

	@Autowired
	FakeDataService service;
	
	@Test
	public void testGenerateData() {
		try {
			service.generateData("application_resource_stat", 60, "2019-07-20 10:00:00", "2019-07-30 10:00:00");	
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
