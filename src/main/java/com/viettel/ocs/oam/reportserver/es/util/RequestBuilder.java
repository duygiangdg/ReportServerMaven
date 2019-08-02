package com.viettel.ocs.oam.reportserver.es.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RequestBuilder {

	public SearchRequest statisticMaxMinAvgSum(RequestWrapper requestWrapper, String indexName) {
		// Gets query information from request
		// Using LinkedHashMap to preserve the insertion orders
		LinkedHashMap<String, String[]> whereFields = requestWrapper.getWhereFields();
		LinkedHashMap<String, String> groupFields = requestWrapper.getGroupFields();
		LinkedHashMap<String, String> calculatedFields = requestWrapper.getCalculatedFields();
		LinkedHashMap<String, String> orderFields = requestWrapper.getOrderFields();
		String fromTime = requestWrapper.getFromTime();
		String toTime = requestWrapper.getToTime();
		String timeField = requestWrapper.getTimeField();
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Create LinkedHashMap : calculatedField ==>
		// operator_calculatedField
		// Ex : (percent_usage, avg_percent_usage)
		LinkedHashMap<String, String> bucketFieldsHashMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
			String aggField = entry.getKey();
			String aggOperator = entry.getValue();
			bucketFieldsHashMap.put(aggField, aggOperator + "_" + aggField);
		}

		String[] indices = new String[] { indexName };
		SearchRequest searchRequest = new SearchRequest(indices);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(true).size(0);
		searchSourceBuilder.timeout(TimeValue.timeValueMinutes(2));

		// Object for managing queries
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

		// Process whereFields
		if (whereFields != null) {
			for (Map.Entry<String, String[]> entry : whereFields.entrySet()) {
				String queryFieldName = entry.getKey();
				String[] queryValues = entry.getValue();
				if (queryValues.length == 0)
					continue;
				for (int stringIndex = 0; stringIndex < queryValues.length; stringIndex++)
					queryValues[stringIndex] = queryValues[stringIndex];

				queryBuilder = queryBuilder.must(QueryBuilders.termsQuery(queryFieldName, queryValues));
			}
		}

		// Convert time to milliseconds
		long fromTimeMilis;
		try {
			fromTimeMilis = timeFormatter.parse(fromTime).getTime();
		} catch (ParseException e) {
			fromTimeMilis = 0;
		}

		long toTimeMilis;
		try {
			toTimeMilis = timeFormatter.parse(toTime).getTime();
		} catch (ParseException e) {
			toTimeMilis = 0;
		}

		// Process time range : fromTime --> toTime
		queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery(timeField).gte(fromTimeMilis).lte(toTimeMilis));

		// Object for managing aggregations
		// Filter by time
		AggregationBuilder aggBuilder = AggregationBuilders.filter("filter_by_time",
				QueryBuilders.rangeQuery(timeField).gte(fromTimeMilis).lt(toTimeMilis));

		// Process groupFields
		AggregationBuilder groupBuilder = null;
		String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);

		for (int keyIndex = keyGroupFields.length - 1; keyIndex >= 0; keyIndex--) {
			String groupField = keyGroupFields[keyIndex];
			String groupOrder = groupFields.get(groupField);

			// Packaging from deepest layer (most-child layer) to outer
			// layers
			if (keyIndex == (keyGroupFields.length - 1)) {
				// Order of group field
				if (groupOrder.equalsIgnoreCase("asc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(true));

				} else if (groupOrder.equalsIgnoreCase("desc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.order(BucketOrder.key(false));

					// Process orderFields
					// When deepest group-field order is 'none', the
					// calculated-fields order is validated
				} else if (groupOrder.equalsIgnoreCase("none")) {
					if (orderFields != null && orderFields.size() > 0) {
						// Save order of calculated fields
						ArrayList<BucketOrder> bucketOrders = new ArrayList<BucketOrder>();
						for (Map.Entry<String, String> entry : orderFields.entrySet()) {
							String orderField = entry.getKey();
							String orderOperator = entry.getValue();
							if (orderOperator.equalsIgnoreCase("asc")) {
								bucketOrders.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), true));
							} else if (orderOperator.equalsIgnoreCase("desc")) {
								bucketOrders.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), false));
							}
						}

						// Apply the calculated-fields order list into
						// deepest layer
						if (bucketOrders.size() > 0)
							groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
									.order(BucketOrder.compound(bucketOrders));
						else
							groupBuilder = AggregationBuilders.terms(groupField).field(groupField);
					} else {
						groupBuilder = AggregationBuilders.terms(groupField).field(groupField);
					}
				}

				// Process calculatedFields
				// Attach these calculated fields into buckets of deepest
				// layer
				for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
					String aggField = entry.getKey();
					String aggOperator = entry.getValue();

					if (aggOperator.equals("avg")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.avg("avg_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("min")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.min("min_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("max")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.max("max_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("sum")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.sum("sum_" + aggField).field(aggField));
					}
				}

				// In case of not deepest layers, simply package its child
				// layer
				// Consider the order of group fields (given by user like :
				// asc, desc, none)
			} else {
				if (groupOrder.equalsIgnoreCase("asc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(true))
							.subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("desc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(false))
							.subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("none")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).subAggregation(groupBuilder);
				}
			}
		}

		// Packaging groupBuilder as a child layer of aggBuilder
		// In case of groupFields is not empty
		if (keyGroupFields.length > 0) {
			aggBuilder = aggBuilder.subAggregation(groupBuilder);
		}

		// In case of groupField is empty. aggBuider directly contains
		// calculated fields
		if (keyGroupFields.length == 0) {
			for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
				String aggField = entry.getKey();
				String aggOperator = entry.getValue();

				if (aggOperator.equalsIgnoreCase("avg")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.avg("avg_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("min")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.min("min_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("max")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.max("max_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("sum")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.sum("sum_" + aggField).field(aggField));
				}
			}
		}

		// Packaging queryBuilder, aggBuilder as a request
		searchSourceBuilder.query(queryBuilder).aggregation(aggBuilder);
		searchRequest.source(searchSourceBuilder);

		return searchRequest;
	}

	public SearchRequest statisticInMinutes(RequestWrapper requestWrapper, String indexName) {
		// Gets query information from request
		// Using LinkedHashMap to preserve the insertion orders
		LinkedHashMap<String, String[]> whereFields = requestWrapper.getWhereFields();
		LinkedHashMap<String, String> groupFields = requestWrapper.getGroupFields();
		LinkedHashMap<String, String> calculatedFields = requestWrapper.getCalculatedFields();
		LinkedHashMap<String, String> orderFields = requestWrapper.getOrderFields();
		String fromTime = requestWrapper.getFromTime();
		String toTime = requestWrapper.getToTime();
		int interval = requestWrapper.getInterval();
		String timeField = requestWrapper.getTimeField();
		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// Create LinkedHashMap : calculatedField ==>
		// operator_calculatedField
		// Ex : (percent_usage, avg_percent_usage)
		LinkedHashMap<String, String> bucketFieldsHashMap = new LinkedHashMap<String, String>();
		for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
			String aggField = entry.getKey();
			String aggOperator = entry.getValue();
			bucketFieldsHashMap.put(aggField, aggOperator + "_" + aggField);
		}

		String[] indices = new String[] { indexName };
		SearchRequest searchRequest = new SearchRequest(indices);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.fetchSource(true).size(0);
		searchSourceBuilder.timeout(TimeValue.timeValueMinutes(2));

		// Object for managing queries
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

		// Process whereFields
		if (whereFields != null) {
			for (Map.Entry<String, String[]> entry : whereFields.entrySet()) {
				String queryFieldName = entry.getKey();
				String[] queryValues = entry.getValue();
				if (queryValues.length == 0)
					continue;
				for (int stringIndex = 0; stringIndex < queryValues.length; stringIndex++)
					queryValues[stringIndex] = queryValues[stringIndex];

				queryBuilder = queryBuilder.must(QueryBuilders.termsQuery(queryFieldName, queryValues));
			}
		}

		// Convert time to milliseconds
		long fromTimeMilis;
		try {
			fromTimeMilis = timeFormatter.parse(fromTime).getTime();
		} catch (ParseException e) {
			fromTimeMilis = 0;
		}

		long toTimeMilis;
		try {
			toTimeMilis = timeFormatter.parse(toTime).getTime();
		} catch (ParseException e) {
			toTimeMilis = 0;
		}

		// Process time range : fromTime --> toTime
		queryBuilder = queryBuilder.must(QueryBuilders.rangeQuery(timeField).gte(fromTimeMilis).lte(toTimeMilis));

		// Object for managing aggregations
		// Group by time
		AggregationBuilder aggBuilder = AggregationBuilders.dateHistogram("group_by_time").field(timeField)
				.dateHistogramInterval(DateHistogramInterval.minutes(interval)).format("yyyy-MM-dd HH:mm:ss")
				.timeZone(DateTimeZone.getDefault()).extendedBounds(new ExtendedBounds(fromTimeMilis, toTimeMilis));

		// Process groupFields
		AggregationBuilder groupBuilder = null;
		String[] keyGroupFields = groupFields.keySet().toArray(new String[groupFields.keySet().size()]);

		for (int keyIndex = keyGroupFields.length - 1; keyIndex >= 0; keyIndex--) {
			String groupField = keyGroupFields[keyIndex];
			String groupOrder = groupFields.get(groupField);

			// Packaging from deepest layer (most-child layer) to outer
			// layers
			if (keyIndex == (keyGroupFields.length - 1)) {
				// Order of group field
				if (groupOrder.equalsIgnoreCase("asc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(true));

				} else if (groupOrder.equalsIgnoreCase("desc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.order(BucketOrder.key(false));

					// Process orderFields
					// When deepest group-field order is 'none', the
					// calculated-fields order is validated
				} else if (groupOrder.equalsIgnoreCase("none")) {
					if (orderFields != null && orderFields.size() > 0) {
						// Save order of calculated fields
						ArrayList<BucketOrder> bucketOrders = new ArrayList<BucketOrder>();
						for (Map.Entry<String, String> entry : orderFields.entrySet()) {
							String orderField = entry.getKey();
							String orderOperator = entry.getValue();
							if (orderOperator.equalsIgnoreCase("asc")) {
								bucketOrders.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), true));
							} else if (orderOperator.equalsIgnoreCase("desc")) {
								bucketOrders.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), false));
							}
						}

						// Apply the calculated-fields order list into
						// deepest layer
						if (bucketOrders.size() > 0)
							groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
									.order(BucketOrder.compound(bucketOrders));
						else
							groupBuilder = AggregationBuilders.terms(groupField).field(groupField);
					} else {
						groupBuilder = AggregationBuilders.terms(groupField).field(groupField);
					}
				}

				// Process calculatedFields
				// Attach these calculated fields into buckets of deepest
				// layer
				for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
					String aggField = entry.getKey();
					String aggOperator = entry.getValue();

					if (aggOperator.equals("avg")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.avg("avg_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("min")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.min("min_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("max")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.max("max_" + aggField).field(aggField));
					} else if (aggOperator.equalsIgnoreCase("sum")) {
						groupBuilder = groupBuilder
								.subAggregation(AggregationBuilders.sum("sum_" + aggField).field(aggField));
					}
				}

				// In case of not deepest layers, simply package its child
				// layer
				// Consider the order of group fields (given by user like :
				// asc, desc, none)
			} else {
				if (groupOrder.equalsIgnoreCase("asc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(true))
							.subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("desc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).order(BucketOrder.key(false))
							.subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("none")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField).subAggregation(groupBuilder);
				}
			}
		}

		// Packaging groupBuilder as a child layer of aggBuilder
		// In case of groupFields is not empty
		if (keyGroupFields.length > 0) {
			aggBuilder = aggBuilder.subAggregation(groupBuilder);
		}

		// In case of groupField is empty. aggBuider directly contains
		// calculated fields
		if (keyGroupFields.length == 0) {
			for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
				String aggField = entry.getKey();
				String aggOperator = entry.getValue();

				if (aggOperator.equalsIgnoreCase("avg")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.avg("avg_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("min")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.min("min_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("max")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.max("max_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("sum")) {
					aggBuilder = aggBuilder.subAggregation(AggregationBuilders.sum("sum_" + aggField).field(aggField));
				}
			}
		}

		// Packaging queryBuilder, aggBuilder as a request
		searchSourceBuilder.query(queryBuilder).aggregation(aggBuilder);
		searchRequest.source(searchSourceBuilder);

		return searchRequest;
	}

	public String processStatisticInMinutes(SearchResponse searchResponse, String[] keyGroupFields) {
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
}
