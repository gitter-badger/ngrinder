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
		<legend class="header"> <@spring.message "sitemonitoring.list.title"/> </legend>
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
			<th><@spring.message "sitemonitoring.list.sitemonitorState"/></th>
			<th><@spring.message "sitemonitoring.list.id"/></th>
			<th class="ellipsis"><@spring.message "sitemonitoring.list.scriptName"/></th>
			<th><@spring.message "sitemonitoring.list.scriptRevision"/></th>
			<th class="no-click"><@spring.message "sitemonitoring.list.targetHosts"/></th>
			<th><@spring.message "sitemonitoring.list.param"/></th>
			<th><@spring.message "sitemonitoring.list.agentName"/></th>
			<th><@spring.message "sitemonitoring.list.unregist"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=sitemonitorings others="table_list" colspan="8"; sitemonitoring>
		<tr>
			<#if sitemonitoring.agentRunning><#assign iconName="green.png"/><#else><#assign iconName="red.png"/></#if>
			<td class="center">
				<div class="ball"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${iconName}"/>
				</div>
			</td>
			<td><a href="${req.getContextPath()}/sitemonitoring/get/${sitemonitoring.id}">${sitemonitoring.id}</a></td>
			<td>
				<div class="ellipsis" title="${sitemonitoring.scriptName}">${sitemonitoring.scriptName}</div>
			</td>
			<td><#if (sitemonitoring.scriptRevision != -1)>${sitemonitoring.scriptRevision}<#else>HEAD</#if></td>
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
