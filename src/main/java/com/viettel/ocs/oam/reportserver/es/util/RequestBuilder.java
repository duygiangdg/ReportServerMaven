package com.viettel.ocs.oam.reportserver.es.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTimeZone;

public class RequestBuilder {

	private RequestBuilder() {
	};

	public static SearchRequest statisticMaxMinAvgSum(RequestWrapper requestWrapper, String indexName) {
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

	public static SearchRequest statisticInMinutes(RequestWrapper requestWrapper, String indexName) {
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
}
