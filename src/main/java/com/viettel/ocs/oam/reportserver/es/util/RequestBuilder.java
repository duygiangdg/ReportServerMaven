package com.viettel.ocs.oam.reportserver.es.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class RequestBuilder {
	private final static Logger logger = Logger.getLogger(RequestBuilder.class);
	
	public SearchRequest statisticMaxMinAvgSum(RequestWrapper requestWrapper, String indexName) throws Exception {
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

		// Check syntax error of the request format
		// Check if groupField is null
		if (groupFields == null)
			throw new Error.Exception13();

		// Check maximum/minimum number of fields in groupFields
		else if (groupFields.size() < 1 || groupFields.size() > 4)
			throw new Error.Exception7();
		else {
			for (Map.Entry<String, String> mapElement : groupFields.entrySet()) {
				String orderOperator = mapElement.getValue();
				if (isValidGroupOperator(orderOperator) == false)
					throw new Error.Exception17();
			}
		}

		// Check if calculatedFields is null
		if (calculatedFields == null)
			throw new Error.Exception18();
		else {
			for (Map.Entry<String, String> entry : calculatedFields.entrySet()) {
				String calOperator = entry.getValue();
				if (isValidCalculationOperator(calOperator) == false)
					throw new Error.Exception19();
			}
			if (orderFields != null && orderFields.size() > 0) {
				for (Map.Entry<String, String> entry : orderFields.entrySet()) {
					String orderField = entry.getKey();
					String orderValue = entry.getValue();
					if (calculatedFields.containsKey(orderField) == false)
						throw new Error.Exception20();
					else if (isValidGroupOperator(orderValue) == false)
						throw new Error.Exception21();
				}
			}
		}

		// Check if fromTime field is null and its format
		if (fromTime == null)
			throw new Error.Exception22();
		else if (!isValidFormat("yyyy-MM-dd HH:mm:ss", fromTime)) {
			throw new Error.Exception1();
		}

		// Check if toTime field is null and its format
		if (toTime == null)
			throw new Error.Exception23();
		else if (!isValidFormat("yyyy-MM-dd HH:mm:ss", toTime)) {
			throw new Error.Exception1();
		}

		// Check if timeField is null
		if (timeField == null)
			throw new Error.Exception25();

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
		searchSourceBuilder.fetchSource(true).size(100);
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
		long fromTimeMilis = timeFormatter.parse(fromTime).getTime();
		long toTimeMilis = timeFormatter.parse(toTime).getTime();

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
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.order(BucketOrder.key(true));

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
								bucketOrders
										.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), true));
							} else if (orderOperator.equalsIgnoreCase("desc")) {
								bucketOrders
										.add(BucketOrder.aggregation(bucketFieldsHashMap.get(orderField), false));
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
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.order(BucketOrder.key(true)).subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("desc")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.order(BucketOrder.key(false)).subAggregation(groupBuilder);
				} else if (groupOrder.equalsIgnoreCase("none")) {
					groupBuilder = AggregationBuilders.terms(groupField).field(groupField)
							.subAggregation(groupBuilder);
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
					aggBuilder = aggBuilder
							.subAggregation(AggregationBuilders.avg("avg_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("min")) {
					aggBuilder = aggBuilder
							.subAggregation(AggregationBuilders.min("min_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("max")) {
					aggBuilder = aggBuilder
							.subAggregation(AggregationBuilders.max("max_" + aggField).field(aggField));
				} else if (aggOperator.equalsIgnoreCase("sum")) {
					aggBuilder = aggBuilder
							.subAggregation(AggregationBuilders.sum("sum_" + aggField).field(aggField));
				}
			}
		}

		// Packaging queryBuilder, aggBuilder as a request
		searchSourceBuilder.query(queryBuilder).aggregation(aggBuilder);
		searchRequest.source(searchSourceBuilder);
		
		return searchRequest;
	}
	
	// Check the validity of Group Operator
	public boolean isValidGroupOperator(String groupOperator) {
		boolean sastified = false;
		if (groupOperator == null)
			return sastified;

		if (groupOperator.equalsIgnoreCase("asc"))
			sastified = true;
		else if (groupOperator.equalsIgnoreCase("desc"))
			sastified = true;
		else if (groupOperator.equalsIgnoreCase("none"))
			sastified = true;
		return sastified;
	}

	// Check the validity of Calculation Operator
	public boolean isValidCalculationOperator(String calOperator) {
		boolean sastified = false;
		if (calOperator == null)
			return sastified;

		if (calOperator.equalsIgnoreCase("max"))
			sastified = true;
		else if (calOperator.equalsIgnoreCase("min"))
			sastified = true;
		else if (calOperator.equalsIgnoreCase("avg"))
			sastified = true;
		else if (calOperator.equalsIgnoreCase("sum"))
			sastified = true;
		return sastified;
	}
	
	// Check the validity of time string
	public boolean isValidFormat(String timeFormat, String timeString) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
			// Convert String to Date
			date = sdf.parse(timeString);
			// Format date as a string, and compare to original timeString
			if (!timeString.equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return (date != null);
	}
}
