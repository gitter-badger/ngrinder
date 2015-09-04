/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sitemon.service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ngrinder.common.util.DateUtils;
import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonResultLog;
import org.ngrinder.sitemon.model.SiteMonResult.SiteMonResultPK;
import org.ngrinder.sitemon.repository.SiteMonResultLogRepository;
import org.ngrinder.sitemon.repository.SiteMonResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link SiteMonResult} service class.
 * 
 * @author Gisoo Gwon
 */
@Component
public class SiteMonResultService {

	@Autowired
	private SiteMonResultRepository siteMonResultRepository;
	
	@Autowired
	private SiteMonResultLogRepository siteMonResultLogRepository;
	
	public Map<String, Object> getTodayGraphData(String siteMonId) throws ParseException {
		Date todayDate = DateUtils.getStartTimeOf(new Date());
		return getGraphData(siteMonId, todayDate);
	}
	
	/**
	 * @param siteMonId	sitemon id
	 * @param date		use yyyy-MM-dd
	 * @return
	 * @throws ParseException
	 */
	public Map<String, Object> getGraphData(String siteMonId, Date date) throws ParseException {
		Date startOfDate = DateUtils.getStartTimeOf(date);
		Date endOfDate = DateUtils.getEndTimeOf(date);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		// get labels
		List<Integer> testNumbers = siteMonResultRepository.findDistinctTestNumberOrderByTestNumber(
			siteMonId, startOfDate);
		List<SiteMonResult> results = siteMonResultRepository.findAllBySiteMonIdEqualAndTimestampEqualOrderByTestNumber(
			siteMonId, startOfDate);
		
		StringBuilder successData = new StringBuilder("[");
		StringBuilder errorData = new StringBuilder("[");
		StringBuilder testTimeData = new StringBuilder("[");
		for (SiteMonResult result : results) {
			successData.append("[" + result.getSuccess() + "],");
			errorData.append("[" + result.getError() + "],");
			testTimeData.append("[" + result.getTestTime() + "],");
		}
		if (successData.charAt(successData.length() - 1) == ',') {
			successData.deleteCharAt(successData.length() - 1);
			errorData.deleteCharAt(errorData.length() - 1);
			testTimeData.deleteCharAt(testTimeData.length() - 1);
		}
		successData.append("]");
		errorData.append("]");
		testTimeData.append("]");
		
		resultMap.put("labels", testNumbers);
		resultMap.put("successData", successData.toString());
		resultMap.put("errorData", errorData.toString());
		resultMap.put("testTimeData", testTimeData.toString());
		resultMap.put("minTimestamp", DateUtils.dateToString(startOfDate));
		resultMap.put("maxTimestamp", DateUtils.dateToString(endOfDate));
		
		return resultMap;
	}

	/**
	 * Find day of result.getTimestamp.
	 * If exists, append result at last.
	 * If not exists, insert result row.
	 * @param result
	 * @return 
	 */
	public void appendResult(SiteMonResult result) {
		Date yyyyMMdd = DateUtils.getStartTimeOf(result.getTimestamp());
		SiteMonResultPK pk = new SiteMonResultPK(result.getSiteMonId(), result.getTestNumber(),
			yyyyMMdd);
		SiteMonResult findResult = siteMonResultRepository.findOne(pk);
		
		if (findResult == null) {
			result.setTimestamp(yyyyMMdd);
			siteMonResultRepository.save(result);
		} else {
			findResult.setSuccess(findResult.getSuccess() + "," + result.getSuccess());
			findResult.setError(findResult.getError() + "," + result.getError());
			findResult.setTestTime(findResult.getTestTime() + "," + result.getTestTime());
			siteMonResultRepository.saveAndFlush(findResult);
		}
	}
	
	public void saveLog(List<SiteMonResultLog> logs) {
		siteMonResultLogRepository.save(logs);
	}
	
	public List<String> findAllLog(String siteMonId, Date minTimestamp, Date maxTimestamp) {
		return siteMonResultLogRepository.findErrorLog(siteMonId, minTimestamp, maxTimestamp);
	}
	
}
