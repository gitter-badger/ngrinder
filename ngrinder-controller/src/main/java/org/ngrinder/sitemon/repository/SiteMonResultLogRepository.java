package org.ngrinder.sitemon.repository;

import java.util.Date;
import java.util.List;

import org.ngrinder.sitemon.model.SiteMonResultLog;
import org.ngrinder.sitemon.model.SiteMonResultLog.PK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * {@link SiteMonResultLog} Repository.
 * 
 * @author Gisoo Gwon
 */
public interface SiteMonResultLogRepository extends JpaRepository<SiteMonResultLog, PK>,
	JpaSpecificationExecutor<SiteMonResultLog> {
	
	@Modifying
	@Query("delete from SiteMonResultLog s where s.pk.timestamp <= ?1")
	public int deleteBeforeTimestamp(Date end);
	
	@Query("select s.log from SiteMonResultLog s"
		+ " where s.pk.siteMonId = ?1"
		+ " and s.pk.timestamp >= ?2 and s.pk.timestamp <= ?3")
	public List<String> findErrorLog(String siteMonId, Date minTimestamp, Date maxTimestamp);

}
