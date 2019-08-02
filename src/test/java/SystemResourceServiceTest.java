import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.reportserver.es.config.ElasticsearchConfig;
import com.viettel.ocs.oam.reportserver.es.model.SystemResourceStat;
import com.viettel.ocs.oam.reportserver.es.service.SystemResourceService;
import com.viettel.ocs.oam.reportserver.es.util.InvalidRequestException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class SystemResourceServiceTest {

	@Autowired
	SystemResourceService service;
	
	@Test
	public void testGetSummary() {
		try {
			SystemResourceStat.Response response = 
					service.getSummary("2019-07-24 10:00:00", "2019-07-25 10:00:00", 5);
			System.out.print(response);
		} catch (InvalidRequestException | IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
