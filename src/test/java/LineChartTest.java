import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.viettel.ocs.oam.reportserver.es.util.LineChart;

public class LineChartTest {

	@Test
	public void drawChartToImage() {
		Random random = new Random();

		List<String> groups = new ArrayList<String>(); 
		groups.add("node1"); 
		groups.add("process1");
		
		Map<String, Map<String, Double>> fieldMap = new HashMap<String, Map<String, Double>>();
		String[] statisticFields = new String[] {"max_cpu_percent", "avg_cpu_percent", "max_ram_percent", "avg_ram_percent"};
		String[] times = new String[] {
			"2019-08-04 01:00:00", "2019-08-04 02:00:00", 
			"2019-08-04 03:00:00", "2019-08-04 04:00:00", 
			"2019-08-04 05:00:00", "2019-08-04 06:00:00"
		};
		
		for (String field: statisticFields) {
			Map<String, Double> timeMap = new HashMap<String, Double>();
			for (String time: times) {
				timeMap.put(time, random.nextDouble()*100);
			}
			fieldMap.put(field, timeMap);
		}
		
		try {
			LineChart.drawChartToImage(fieldMap, groups, "src/main/resources/chart.png");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
}
