import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.viettel.ocs.oam.elastic.config.ElasticsearchConfig;
import com.viettel.ocs.oam.elastic.manager.RepositoryFactory;
import com.viettel.ocs.oam.elastic.repositories.GeneralRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class GeneralRepositoryTest {
	
	@Autowired
	private RepositoryFactory repositoryFactory;

	@Test
	public void TestGenData() {
		assertNotNull(repositoryFactory);
		GeneralRepository generalRepository = repositoryFactory.getGeneralRepository();
		generalRepository.generateData("application_resource_stat", 60, "2019-07-20 10:00:00", "2019-07-30 10:00:00");
	}
}
