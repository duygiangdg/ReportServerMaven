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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResponseParser {
	private ResponseParser() {
	}

	public static String processStatisticInMinutes(SearchResponse searchResponse, String[] keyGroupFields) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode jsonResponse = mapper.createObjectNode();
		Histogram groupByTime = searchResponse.getAggregations().get("group_by_time");

		switch (keyGroupFields.length) {
		case 0: {
			ArrayNode arrayItemNodes1 = mapper.createArrayNode();
			for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {
				ObjectNode itemNode1 = mapper.createObjectNode();
				ObjectNode calculatedFieldsMapper = mapper.createObjectNode();
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
					calculatedFieldsMapper.put(calculatedField, calculatedValue);
				}
				itemNode1.put("group_by_time", timeEntry.getKeyAsString());
				itemNode1.putPOJO("statistic_result", calculatedFieldsMapper);
				arrayItemNodes1.add(itemNode1);
			}
			jsonResponse.putPOJO("json_response", arrayItemNodes1);

			break;
		}
		case 1: {
			ArrayNode arrayItemNodes1 = mapper.createArrayNode();
			for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {
				ObjectNode itemNode1 = mapper.createObjectNode();
				ArrayNode arrayItemNodes2 = mapper.createArrayNode();
				Terms groupByField1 = timeEntry.getAggregations().get(keyGroupFields[0]);
				for (Terms.Bucket field1Entry : groupByField1.getBuckets()) {

					ObjectNode itemNode2 = mapper.createObjectNode();
					ObjectNode calculatedFieldsMapper = mapper.createObjectNode();
					for (Entry<String, Aggregation> calculatedEntry : field1Entry.getAggregations().asMap()
							.entrySet()) {
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
						calculatedFieldsMapper.put(calculatedField, calculatedValue);
					}

					itemNode2.put(keyGroupFields[0], field1Entry.getKeyAsString());
					itemNode2.putPOJO("statistic_result", calculatedFieldsMapper);
					arrayItemNodes2.add(itemNode2);
				}

				itemNode1.put("group_by_time", timeEntry.getKeyAsString());
				itemNode1.putPOJO("array_" + keyGroupFields[0], arrayItemNodes2);
				arrayItemNodes1.add(itemNode1);
			}
			jsonResponse.putPOJO("json_response", arrayItemNodes1);

			break;
		}
		case 2: {
			ArrayNode arrayItemNodes1 = mapper.createArrayNode();
			for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {
				ObjectNode itemNode1 = mapper.createObjectNode();

				ArrayNode arrayItemNodes2 = mapper.createArrayNode();
				Terms groupByField1 = timeEntry.getAggregations().get(keyGroupFields[0]);
				for (Terms.Bucket field1Entry : groupByField1.getBuckets()) {

					ObjectNode itemNode2 = mapper.createObjectNode();
					ArrayNode arrayItemNodes3 = mapper.createArrayNode();
					Terms groupByField2 = field1Entry.getAggregations().get(keyGroupFields[1]);
					for (Terms.Bucket field2Entry : groupByField2.getBuckets()) {

						ObjectNode itemNode3 = mapper.createObjectNode();
						ObjectNode calculatedFieldsMapper = mapper.createObjectNode();
						for (Entry<String, Aggregation> calculatedEntry : field2Entry.getAggregations().asMap()
								.entrySet()) {
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
							calculatedFieldsMapper.put(calculatedField, calculatedValue);
						}

						itemNode3.put(keyGroupFields[1], field2Entry.getKeyAsString());
						itemNode3.putPOJO("statistic_result", calculatedFieldsMapper);
						arrayItemNodes3.add(itemNode3);
					}

					itemNode2.put(keyGroupFields[0], field1Entry.getKeyAsString());
					itemNode2.putPOJO("array_" + keyGroupFields[1], arrayItemNodes3);
					arrayItemNodes2.add(itemNode2);
				}
				itemNode1.put("group_by_time", timeEntry.getKeyAsString());
				itemNode1.putPOJO("array_" + keyGroupFields[0], arrayItemNodes2);
				arrayItemNodes1.add(itemNode1);
			}
			jsonResponse.putPOJO("json_response", arrayItemNodes1);

			break;
		}
		case 3: {
			ArrayNode arrayItemNodes1 = mapper.createArrayNode();
			for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {
				ObjectNode itemNode1 = mapper.createObjectNode();
				ArrayNode arrayItemNodes2 = mapper.createArrayNode();
				Terms groupByField1 = timeEntry.getAggregations().get(keyGroupFields[0]);
				for (Terms.Bucket field1Entry : groupByField1.getBuckets()) {

					ObjectNode itemNode2 = mapper.createObjectNode();
					ArrayNode arrayItemNodes3 = mapper.createArrayNode();
					Terms groupByField2 = field1Entry.getAggregations().get(keyGroupFields[1]);
					for (Terms.Bucket field2Entry : groupByField2.getBuckets()) {

						ObjectNode itemNode3 = mapper.createObjectNode();
						ArrayNode arrayItemNodes4 = mapper.createArrayNode();
						Terms groupByField3 = field2Entry.getAggregations().get(keyGroupFields[2]);
						for (Terms.Bucket field3Entry : groupByField3.getBuckets()) {

							ObjectNode itemNode4 = mapper.createObjectNode();
							ObjectNode calculatedFieldsMapper = mapper.createObjectNode();
							for (Entry<String, Aggregation> calculatedEntry : field3Entry.getAggregations().asMap()
									.entrySet()) {
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
								calculatedFieldsMapper.put(calculatedField, calculatedValue);
							}
							itemNode4.put(keyGroupFields[2], field3Entry.getKeyAsString());
							itemNode4.putPOJO("statistic_result", calculatedFieldsMapper);
							arrayItemNodes4.add(itemNode4);
						}
						itemNode3.put(keyGroupFields[1], field2Entry.getKeyAsString());
						itemNode3.putPOJO("array_" + keyGroupFields[2], arrayItemNodes4);
						arrayItemNodes3.add(itemNode3);
					}
					itemNode2.put(keyGroupFields[0], field1Entry.getKeyAsString());
					itemNode2.putPOJO("array_" + keyGroupFields[1], arrayItemNodes3);
					arrayItemNodes2.add(itemNode2);
				}
				itemNode1.put("group_by_time", timeEntry.getKeyAsString());
				itemNode1.putPOJO("array_" + keyGroupFields[0], arrayItemNodes2);
				arrayItemNodes1.add(itemNode1);
			}
			jsonResponse.putPOJO("json_response", arrayItemNodes1);

			break;
		}
		case 4: {
			ArrayNode arrayItemNodes1 = mapper.createArrayNode();
			for (Histogram.Bucket timeEntry : groupByTime.getBuckets()) {

				ObjectNode itemNode1 = mapper.createObjectNode();
				ArrayNode arrayItemNodes2 = mapper.createArrayNode();
				Terms groupByField1 = timeEntry.getAggregations().get(keyGroupFields[0]);
				for (Terms.Bucket field1Entry : groupByField1.getBuckets()) {

					ObjectNode itemNode2 = mapper.createObjectNode();
					ArrayNode arrayItemNodes3 = mapper.createArrayNode();
					Terms groupByField2 = field1Entry.getAggregations().get(keyGroupFields[1]);
					for (Terms.Bucket field2Entry : groupByField2.getBuckets()) {

						ObjectNode itemNode3 = mapper.createObjectNode();
						ArrayNode arrayItemNodes4 = mapper.createArrayNode();
						Terms groupByField3 = field2Entry.getAggregations().get(keyGroupFields[2]);
						for (Terms.Bucket field3Entry : groupByField3.getBuckets()) {

							ObjectNode itemNode4 = mapper.createObjectNode();
							ArrayNode arrayItemNodes5 = mapper.createArrayNode();
							Terms groupByField4 = field3Entry.getAggregations().get(keyGroupFields[3]);
							for (Terms.Bucket field4Entry : groupByField4.getBuckets()) {

								ObjectNode itemNode5 = mapper.createObjectNode();
								ObjectNode calculatedFieldsMapper = mapper.createObjectNode();
								for (Entry<String, Aggregation> calculatedEntry : field4Entry.getAggregations().asMap()
										.entrySet()) {
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
									calculatedFieldsMapper.put(calculatedField, calculatedValue);
								}
								itemNode5.put(keyGroupFields[3], field4Entry.getKeyAsString());
								itemNode5.putPOJO("statistic_result", calculatedFieldsMapper);
								arrayItemNodes5.add(itemNode5);
							}
							itemNode4.put(keyGroupFields[2], field3Entry.getKeyAsString());
							itemNode4.putPOJO("array_" + keyGroupFields[3], arrayItemNodes5);
							arrayItemNodes4.add(itemNode4);
						}
						itemNode3.put(keyGroupFields[1], field2Entry.getKeyAsString());
						itemNode3.putPOJO("array_" + keyGroupFields[2], arrayItemNodes4);
						arrayItemNodes3.add(itemNode3);
					}
					itemNode2.put(keyGroupFields[0], field1Entry.getKeyAsString());
					itemNode2.putPOJO("array_" + keyGroupFields[1], arrayItemNodes3);
					arrayItemNodes2.add(itemNode2);
				}
				itemNode1.put("group_by_time", timeEntry.getKeyAsString());
				itemNode1.putPOJO("array_" + keyGroupFields[0], arrayItemNodes2);
				arrayItemNodes1.add(itemNode1);
			}
			jsonResponse.putPOJO("json_response", arrayItemNodes1);

			break;
		}
		}

		return jsonResponse.toString();
	}
	
	public static Map mapStatisticInMinutes(SearchResponse searchResponse, String[] keyGroupFields) {
		return new HashMap();
	}
}
