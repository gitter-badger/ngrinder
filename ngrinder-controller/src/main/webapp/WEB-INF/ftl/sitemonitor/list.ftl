<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
<#include "../common/datatables.ftl"/>
	<title><@spring.message "sitemonitor.title"/></title>
</head>
<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
	<fieldSet>
		<legend class="header"> <@spring.message "sitemonitor.list.title"/> </legend>
	</fieldSet>

	<table class="table table-striped table-bordered ellipsis" id="agent_table">
		<colgroup>
			<col width="80">
			<col width="130">
			<col width="60">
			<col width="*">
			<col width="100">
			<col width="70">
			<col width="70">
			<col width="70">
			<col width="60">
		</colgroup>
		<thead>
		<tr>
			<th><@spring.message "agent.list.state"/></th>
			<th><@spring.message "agent.list.IPAndDns"/></th>
			<th class="no-click"><@spring.message "agent.list.port"/></th>
			<th class="ellipsis"><@spring.message "agent.list.name"/></th>
			<th><@spring.message "agent.list.version"/></th>
			<th><@spring.message "sitemonitor.list.maxCpu"/></th>
			<th><@spring.message "sitemonitor.list.minMem"/></th>
			<th><@spring.message "sitemonitor.list.maxTime"/></th>
			<th><@spring.message "sitemonitor.list.registScript"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=allAgentStatus others="table_list" colspan="9"; agentStatus>
		<tr>
			<td class="center">
				<div class="ball"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${agentStatus.agentControllerState.iconName}"/>
				</div>
			</td>
			<td>
				<div class="ellipsis" title="${agentStatus.agentIdentity.ip}">${agentStatus.agentIdentity.ip}</div>
			</td>
			<td>${(agentStatus.connectingPort)!}</td>
			<td class="ellipsis agent-name" title="${(agentStatus.agentName)!}">${(agentStatus.agentName)!}</td>
			<td class="ellipsis"><#if agentStatus.version?has_content>${agentStatus.version}<#else>Prior to 3.3</#if></td>
			<td>${(agentStatus.agentStateMonitor.maxCpuUsePer) ? string["0.#"]}%</td>
			<td>${(agentStatus.agentStateMonitor.minFreeMemory / 1024 / 1024) ? string["0.##"]}GB</td>
			<td>${(agentStatus.agentStateMonitor.maxUseTimeMilisec / 1000) ? string["0"]}sec</td>
			<td>${agentStatus.agentStateMonitor.registScriptCount}</td>
		</tr>
		</@list>
		</tbody>
	</table>
	<!--content-->
</div>
</div>
<#include "../common/copyright.ftl">
<script>
	$(document).ready(function () {
		var $agentTable = $("#agent_table");
	<#if agents?has_content>
		$agentTable.dataTable({
			"bAutoWidth": false,
			"bFilter": false,
			"bLengthChange": false,
			"bInfo": false,
			"iDisplayLength": 10,
			"aaSorting": [
				[1, "asc"]
			],
			"aoColumns": [null, {"asSorting": []}, null, {"asSorting": []}, null, null, null, null, null],
			"sPaginationType": "bootstrap",
			"oLanguage": {
				"oPaginate": {
					"sPrevious": "<@spring.message 'common.paging.previous'/>",
					"sNext": "<@spring.message 'common.paging.next'/>"
				}
			}
		});

		removeClick();
	</#if>
	});
</script>
</body>
</html>
