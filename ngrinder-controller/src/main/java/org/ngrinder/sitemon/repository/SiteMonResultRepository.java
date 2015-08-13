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
package org.ngrinder.sitemon.repository;

import java.util.Date;
import java.util.List;

import org.ngrinder.sitemon.model.SiteMonResult;
import org.ngrinder.sitemon.model.SiteMonResult.SiteMonResultPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * {@link SiteMonResult} Repository.
 * 
 * @author Gisoo Gwon
 */
public interface SiteMonResultRepository extends
	JpaRepository<SiteMonResult, SiteMonResultPK>,
	JpaSpecificationExecutor<SiteMonResult> {

	/**
	 * Find all testNumber after the given start date with distinct.
	 *
	 * @param siteMonId sitemon id
	 * @param start time
	 * @return {@link String} test number list
	 */
	@Query("select distinct(s.siteMonResultPK.testNumber) from SiteMonResult s where s.siteMonResultPK.siteMonId = ?1 and s.siteMonResultPK.timestamp > ?2")
	public List<Integer> findTestNumber(String siteMonId, Date start);
	
	@Query("select s.errorLog from SiteMonResult s"
		+ " where s.siteMonResultPK.siteMonId = ?1"
		+ " and s.siteMonResultPK.timestamp >= ?2 and s.siteMonResultPK.timestamp <= ?3")
	public List<String> findErrorLog(String siteMonId, Date minTimestamp, Date maxTimestamp);
	
}
