package com.viettel.ocs.oam.reportserver.es.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;

public class ResponseParser {
	private ResponseParser() {
	}

	@SuppressWarnings("rawtypes")
	public static Map mapStatisticInMinutes(SearchResponse searchResponse, String[] keyGroupFields) {
		if (keyGroupFields == null)	keyGroupFields = new String[] {};
		return mapStatisticInMinutes(searchResponse, keyGroupFields, 0);
	}

	@SuppressWarnings("rawtypes")
	private static Map mapStatisticInMinutes(Object entry, String[] keyGroupFields, int idx) {
		if (idx == 0 && idx < keyGroupFields.length) {
			Map<String, Map> map = new HashMap<String, Map>();
			Terms nestedGroup = ((SearchResponse) entry).getAggregations().get(keyGroupFields[idx]);
			for (Terms.Bucket nestedEntry : nestedGroup.getBuckets()) {
				Map nestedMap = mapStatisticInMinutes(nestedEntry, keyGroupFields, idx + 1);
				map.put(((Terms.Bucket) nestedEntry).getKeyAsString(), nestedMap);
			}
			return map;
		}
			
		if (idx > 0 && idx < keyGroupFields.length) {
			Map<String, Map> map = new HashMap<String, Map>();
			Terms nestedGroup = ((Terms.Bucket) entry).getAggregations().get(keyGroupFields[idx]);
			for (Terms.Bucket nestedEntry : nestedGroup.getBuckets()) {
				Map nestedMap = mapStatisticInMinutes(nestedEntry, keyGroupFields, idx + 1);
				map.put(((Terms.Bucket) entry).getKeyAsString(), nestedMap);
			}
			return map;
		}

		Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();
		Histogram groupByTime;
		if (idx == 0) {
			groupByTime = ((SearchResponse) entry).getAggregations().get("group_by_time");
		} else {
			groupByTime = ((Terms.Bucket) entry).getAggregations().get("group_by_time");
		}
		for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {
			for (Entry<String, Aggregation> calculatedEntry : timeEntry.getAggregations().asMap().entrySet()) {
				String calculatedField = calculatedEntry.getKey();
				double calculatedValue = -1.0;

				if (calculatedField.startsWith("avg_")) {
					calculatedValue = ((Avg) calculatedEntry.getValue()).getValue();

				} else if (calculatedField.startsWith("max_")) {
					calculatedValue = ((Max) calculatedEntry.getValue()).getValue();

				} else if (calculatedField.startsWith("min_")) {
					calculatedValue = ((Min) calculatedEntry.getValue()).getValue();

				} else if (calculatedField.startsWith("sum_")) {
					calculatedValue = ((Sum) calculatedEntry.getValue()).getValue();
				}

				calculatedValue = Double.isInfinite(calculatedValue) ? 0.0 : calculatedValue;
				
				if (map.get(calculatedField) == null) {
					map.put(calculatedField, new HashMap<String, Double>());
				}
				map.get(calculatedField).put(timeEntry.getKeyAsString(), calculatedValue);
			}
		}
		return map;
	}
}
