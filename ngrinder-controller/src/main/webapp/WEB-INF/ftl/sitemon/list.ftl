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
			<col width="*">
			<col width="100">
			<col width="80">
			<col width="60">
		</colgroup>
		<thead>
		<tr>
			<th><@spring.message "siteMon.list.agentState"/></th>
			<th><@spring.message "siteMon.list.name"/></th>
			<th><@spring.message "siteMon.list.scriptRevision"/></th>
			<th><@spring.message "siteMon.list.runState"/></th>
			<th><@spring.message "common.label.actions"/></th>
		</tr>
		</thead>
		<tbody>
		<@list list_items=siteMons others="table_list" colspan="8"; siteMon>
		<tr>
			<#if siteMon.agentRunning>
				<#if siteMon.runState>
					<#assign iconName="green_anime.gif"/>
				<#else>
					<#assign iconName="green.png"/>
				</#if>
			<#else>
				<#assign iconName="red.png"/>
			</#if>
			<td class="center">
				<div class="ball"
					 data-html="true"
					 rel="popover">
					<img class="status" src="${req.getContextPath()}/img/ball/${iconName}"/>
				</div>
			</td>
			<td>
				<div class="ellipsis" title="${siteMon.name}">
					<a href="${req.getContextPath()}/sitemon/get/${siteMon.id}">${siteMon.name}</a>
				</div>
			</td>
			<td><#if (siteMon.scriptRevision != -1)>${siteMon.scriptRevision}<#else>HEAD</#if></td>
			<td>
				<#if siteMon.runState>
					<@spring.message 'siteMon.state.start'/>
				<#else>
					<@spring.message 'siteMon.state.pause'/>
				</#if>
			</td>
			<td>
				<#if siteMon.runState>
					<i id="pause_${siteMon.id}" class="icon-pause pointer-cursor" sid="${siteMon.id}"></i>
				<#else>
					<i id="play_${siteMon.id}" class="icon-play pointer-cursor" sid="${siteMon.id}"></i>
				</#if>
			</td>
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
		$("#nav_siteMon").addClass("active");
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
			"aoColumns": [null, {"asSorting": []}, {"asSorting": []}, null, null],
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
		$("i.icon-play").click(function () {
			var id = $(this).attr("sid");
			var ajaxObj = new AjaxProgressBarObj("${req.getContextPath()}/sitemon/api/run/" + id);
			ajaxObj.success = function() {
				location.reload();
			}
			ajaxObj.call();
		});
		$("i.icon-pause").click(function () {
			var id = $(this).attr("sid");
			var ajaxObj = new AjaxProgressBarObj("${req.getContextPath()}/sitemon/api/pause/" + id);
			ajaxObj.success = function() {
				location.reload();
			}
			ajaxObj.call();
		});
	});
</script>
</body>
</html>
