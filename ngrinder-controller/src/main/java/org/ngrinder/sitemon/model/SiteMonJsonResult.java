package org.ngrinder.sitemon.model;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ngrinder.common.util.DateUtils;

/**
 * Create sitemon result for json object.
 * 
 * @author Gisoo Gwon
 */
public class SiteMonJsonResult {
	
	private String minTimestamp = null;
	private String maxTimestamp = null;
	// Map<TestNumber, [{Timestamp, Value}, ...]>
	private Map<Integer, List<List<Object>>> successMap = new HashMap<Integer, List<List<Object>>>();
	private Map<Integer, List<List<Object>>> errorMap = new HashMap<Integer, List<List<Object>>>();
	private Map<Integer, List<List<Object>>> testTimeMap = new HashMap<Integer, List<List<Object>>>();
	
	public SiteMonJsonResult(List<SiteMonResult> siteMonResults) {
		try {
			minTimestamp = DateUtils.dateToString(DateUtils.toSimpleDate("2999-12-31 00:00:00"));
			maxTimestamp = DateUtils.dateToString(DateUtils.toSimpleDate("1900-01-01 00:00:00"));
		} catch (ParseException e) {
		}
		for (SiteMonResult siteMonResult : siteMonResults) {
			if (successMap.get(siteMonResult.getTestNumber()) == null) {
				successMap.put(siteMonResult.getTestNumber(), new LinkedList<List<Object>>());
				errorMap.put(siteMonResult.getTestNumber(), new LinkedList<List<Object>>());
				testTimeMap.put(siteMonResult.getTestNumber(), new LinkedList<List<Object>>());
			}
			parseAndAdd(siteMonResult);
		}
	}
	
	public List<Integer> getLabelsList() {
		List<Integer> labels = new LinkedList<Integer>();
		for (Integer key : successMap.keySet()) {
			labels.add(key);
		}
		return labels;
	}
	
	public List<List<List<Object>>> getSuccessList() {
		return toArray(successMap);
	}
	
	public List<List<List<Object>>> getErrorList() {
		return toArray(errorMap);
	}
	
	public List<List<List<Object>>> getTestTimeList() {
		return toArray(testTimeMap);
	}
	
	public String getMinTimestamp() {
		return minTimestamp;
	}

	public String getMaxTimestamp() {
		return maxTimestamp;
	}

	private List<List<List<Object>>> toArray(Map<Integer, List<List<Object>>> dataMap) {
		List<List<List<Object>>> arr = new LinkedList<List<List<Object>>>();
		for (int key : dataMap.keySet()) {
			arr.add(dataMap.get(key));
		}
		return arr;
	}
	
	private void parseAndAdd(SiteMonResult data) {
		String timestamp = DateUtils.dateToString(data.getTimestamp());
		if (timestamp.compareTo(maxTimestamp) > 0) {
			maxTimestamp = timestamp;
		}
		if (timestamp.compareTo(minTimestamp) < 0) {
			minTimestamp = timestamp;
		}
		int testNumber = data.getTestNumber();
		successMap.get(testNumber).add(asList(timestamp, data.getSuccess()));
		errorMap.get(testNumber).add(asList(timestamp, data.getError()));
		testTimeMap.get(testNumber).add(asList(timestamp, data.getTestTime()));
	}
	
	private List<Object> asList(String timestamp, long value) {
		List<Object> list = new LinkedList<Object>();
		list.add(timestamp);
		list.add(value);
		return list;
	}

}
