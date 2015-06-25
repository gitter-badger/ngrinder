<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
<#include "../common/datatables.ftl"/>
	<title><@spring.message "sitemonitoring.title"/></title>
</head>
<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
	<fieldSet>
		<legend class="header"> <@spring.message "sitemonitor.list.title"/> </legend>
	</fieldSet>

	<table class="table table-striped table-bordered ellipsis" id="sitemonitoring_table">
		<colgroup>
			<col width="80">
			<col width="100">
			<col width="*">
			<col width="100">
			<col width="100">
			<col width="100">
			<col width="100">
			<col width="80">
		</colgroup>
		<thead>
		<tr>
			<th><@spring.message "sitemonitor.list.sitemonitorState"/></th>
			<th><@spring.message "sitemonitor.list.id"/></th>
			<th class="ellipsis"><@spring.message "sitemonitor.list.scriptName"/></th>
			<th><@spring.message "sitemonitor.list.scriptRevision"/></th>
			<th class="no-click"><@spring.message "sitemonitor.list.targetHosts"/></th>
			<th><@spring.message "sitemonitor.list.param"/></th>
			<th><@spring.message "sitemonitor.list.agentName"/></th>
			<th><@spring.message "sitemonitor.list.unregist"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=sitemonitorings; sitemonitoring>
		<tr>
			<#if sitemonitoring.agentRunning><#assign iconName="green.png"/><#else><#assign iconName="red.png"/></#if>
			<td class="center">
				<div class="ball"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${iconName}"/>
				</div>
			</td>
			<td>${sitemonitoring.id}</td>
			<td>
				<div class="ellipsis" title="${sitemonitoring.scriptName}">${sitemonitoring.scriptName}</div>
			</td>
			<td>${sitemonitoring.scriptRevision}</td>
			<td class="ellipsis" title="${sitemonitoring.targetHosts}">${sitemonitoring.targetHosts}</td>
			<td class="ellipsis" title="${sitemonitoring.param}">${sitemonitoring.param}</td>
			<td class="ellipsis" title="${sitemonitoring.agentName}">${sitemonitoring.agentName}</td>
			<td><i id="unregist_${sitemonitoring.id}" style="" class="icon-remove test-remove pointer-cursor" sid="${sitemonitoring.id}"></i></td>
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
		var $sitemonitoringTable = $("#sitemonitoring_table");
	<#if sitemonitorings?has_content>
		$sitemonitoringTable.dataTable({
			"bAutoWidth": false,
			"bFilter": false,
			"bLengthChange": false,
			"bInfo": false,
			"iDisplayLength": 10,
			"aaSorting": [
				[1, "asc"]
			],
			"aoColumns": [null, {"asSorting": []}, {"asSorting": []}, null, null, null, null, null],
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
		$("i").click(function () {
			var id = $(this).attr("sid");
			var ajaxObj = new AjaxObj("/sitemonitor/api/del/" + id);
			ajaxObj.success = function() {
				location.reload();
			}
			ajaxObj.call();
		});
	});
</script>
</body>
</html>
