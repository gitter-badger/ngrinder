<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
<#include "../common/datatables.ftl"/>
	<title><@spring.message "siteMon.list.title"/></title>
</head>
<body>
<div id="wrap">
<#include "../common/navigator.ftl">
<div class="container">
	<fieldSet>
		<legend class="header"> <@spring.message "siteMon.list.title"/> </legend>
	</fieldSet>

	<table class="table table-striped table-bordered ellipsis" id="siteMon_table">
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
			<th><@spring.message "siteMon.list.agentState"/></th>
			<th><@spring.message "siteMon.list.id"/></th>
			<th class="ellipsis"><@spring.message "siteMon.list.scriptName"/></th>
			<th><@spring.message "siteMon.list.scriptRevision"/></th>
			<th class="no-click"><@spring.message "siteMon.list.targetHosts"/></th>
			<th><@spring.message "siteMon.list.param"/></th>
			<th><@spring.message "siteMon.list.agentName"/></th>
			<th><@spring.message "siteMon.list.unregist"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=siteMons others="table_list" colspan="8"; siteMon>
		<tr>
			<#if siteMon.agentRunning><#assign iconName="green.png"/><#else><#assign iconName="red.png"/></#if>
			<td class="center">
				<div class="ball"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${iconName}"/>
				</div>
			</td>
			<td><a href="${req.getContextPath()}/sitemon/get/${siteMon.id}">${siteMon.id}</a></td>
			<td>
				<div class="ellipsis" title="${siteMon.scriptName}">${siteMon.scriptName}</div>
			</td>
			<td><#if (siteMon.scriptRevision != -1)>${siteMon.scriptRevision}<#else>HEAD</#if></td>
			<td class="ellipsis" title="${siteMon.targetHosts}">${siteMon.targetHosts}</td>
			<td class="ellipsis" title="${siteMon.param}">${siteMon.param}</td>
			<td class="ellipsis" title="${siteMon.agentName}">${siteMon.agentName}</td>
			<td><i id="unregist_${siteMon.id}" style="" class="icon-remove test-remove pointer-cursor" sid="${siteMon.id}"></i></td>
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
		var $siteMonTable = $("#siteMon_table");
	<#if siteMons?has_content>
		$siteMonTable.dataTable({
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
			var ajaxObj = new AjaxObj("/sitemon/api/del/" + id);
			ajaxObj.success = function() {
				location.reload();
			}
			ajaxObj.call();
		});
	});
</script>
</body>
</html>
