import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.reportserver.es.config.ElasticsearchConfig;
import com.viettel.ocs.oam.reportserver.es.service.ProcessResourceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class ProcessResourceServiceTest {
	
	@Autowired
	ProcessResourceService service;
	
	@Test
	public void testNotNull() {
		assertNotNull(this.service);
	}
}
