<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
<#include "../common/jqplot.ftl">
	<title><@spring.message "siteMon.config.title"/></title>
	<style>
	.control-label input {
		vertical-align: top;
		margin-left: 2px
	}

	#siteMon_id + span {
		float: left;
	}

	.controls .span3 {
		margin-left: 0;
	}

	.control-group.success td > label[for="siteMon_id"] {
		color: #468847;
	}

	.control-group.error td > label[for="siteMon_id"] {
		color: #B94A48;
	}

	.span5-6 {
		width: 410px;
	}
	
	div.div-resources {
        border: 1px solid #D6D6D6;
        min-width: 340px;
        height: 110px;
        margin-bottom: 8px;
        overflow-y: scroll;
        border-radius: 3px 3px 3px 3px;
    }

    div.div-resources .resource {
        width: 300px;
        color: #666666;
        display: block;
        margin-left: 7px;
        margin-top: 2px;
        margin-bottom: 2px;
    }

	div.div-host {
		border: 1px solid #D6D6D6;
		height: 110px;
		margin-bottom: 8px;
		overflow-y: scroll;
		border-radius: 3px 3px 3px 3px;
	}

	div.div-host .host {
		color: #666666;
		display: inline-block;
		margin-left: 7px;
		margin-top: 2px;
		margin-bottom: 2px;
	}

    .add-host-btn {
        margin-top:87px;
        margin-left:287px;
        position:absolute
    }
    
    <!-- jqplot -->
    div.chart {
		border: 1px solid #878988;
		height: 300px;
		min-width: 615px;
	}
	
	.jqplot-yaxis {
		margin-right: 10px;
	}

	.jqplot-xaxis {
		margin-right: 5px;
		margin-top: 5px;
	}
	</style>
</head>
<body>
<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container" style="margin-top: 5px;">
		<@security.authorize ifAnyGranted="A, S">
			<#if siteMon.createdUser?? && currentUser.userId != siteMon.createdUser.userId>
				<div class="pull-right">
					<a href="${req.getContextPath()}/user/switch?to=${siteMon.createdUser.userId!""}">
					<@spring.message "perfTest.list.owner"/> : ${siteMon.createdUser.userName!""} (${siteMon.createdUser.userId!""})
					</a>
				</div>
			</#if>
		</@security.authorize >
		<!-- end owner info -->
	
		<div class="tabbable" style="margin-top: 0;margin-bottom: 50px">
			<ul class="nav nav-tabs" id="homeTab" style="margin-bottom: 5px">
				<li id="sitemon_config_section_tab">
					<a href="#sitemon_config_section" data-toggle="tab">
						<@spring.message "siteMon.config.title"/>
					</a>
				</li> 
				<li id="sitemon_report_section_tab" style="display: none;">
					<a href="#report_section" data-toggle="tab" id="report_section_btn">
						<@spring.message "siteMon.report.title"/>
					</a>
				</li>
			</ul>
			<div class="tab-content">
				<div class="tab-pane" id="sitemon_config_section">
					<#include "config.ftl">
				</div>

				<div class="tab-pane" id="report_section">
					<#include "detail_report.ftl">
				</div>
			</div>
			<!-- end tab content -->
		</div>
		<!-- end tabbable -->
		<!--content-->
	</div>
	<#include "../perftest/host_modal.ftl">
</div>
<#include "../common/copyright.ftl">
<script type="text/javascript">
var bUpdatedReport = false;
var bUpdatedResources = false;
$(document).ready(function() {
	showProgressBar('<@spring.message "siteMon.message.updateReport"/> & <@spring.message "perfTest.message.updateResource"/>');
	$("#sitemon_config_section_tab").find("a").tab('show');
	bindEvent();
	updateScriptResources();
	drawReportChart();
<#if msg??>
	alert("${msg}");
</#if>
});

function bindEvent() {
	$("#show_script_btn").click(function() {
		var currentScript = $("#script_name").val();
        var ownerId = "";
		<@security.authorize ifAnyGranted="A, S">					
			ownerId = "&ownerId=${(siteMon.createdUser.userId)!}";
		</@security.authorize>
		var scriptRevision = $("#script_revision").val();
		var openedWindow = window.open("${req.getContextPath()}/script/detail/" + currentScript + "?r=" + scriptRevision + ownerId, "scriptSource");
		openedWindow.focus(); 
	});
	$("#use_revision_btn").click(function() {
		$("#script_revision").val(-1);
		$("#show_script_btn").html("HEAD");
		$("#use_revision_btn").hide();
		updateScriptResources(true);
	});
}

function updateScriptResources(bInitHosts) {
	var scriptName = $("#script_name").val();

	var ajaxObj = new AjaxObj("/perftest/api/resource", null, "<@spring.message "common.error.error"/>");
	ajaxObj.params = {
		'scriptPath' : scriptName,
		'scriptRevision' : $("#script_revision").val()
		<@security.authorize ifAnyGranted="A, S">
			,'ownerId' : '${(siteMon.createdUser.userId)!}'
		</@security.authorize>
	};
	ajaxObj.success = function(res) {
		var html = "";
		var len = res.resources.length;
		if (bInitHosts) {
			initHosts(res.targetHosts);
		}
		for (var i = 0; i < len; i++) {
			var value = res.resources[i];
			html = html + "<div class='resource ellipsis' title='" + value + "'>" + value + "</div>";
		}
		$("#script_resources").html(html);
		
		bUpdatedResources = true;
		if (bUpdatedReport) {
			hideProgressBar();
		}
	};
	ajaxObj.call();
}

function drawReportChart() {
	var ajaxObj = new AjaxObj("/sitemon/api/${siteMon.id}/result", null, "<@spring.message "common.error.error"/>");
	ajaxObj.success = function(res) {
		if (res.successData && res.successData.length > 0) {
			$("#sitemon_report_section_tab").show();
			$("#sitemon_report_section_tab").find("a").tab('show');
			new Chart("success_chart", res.successData, 1, {labels : res.labels, xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
			new Chart("error_chart", res.errorData, 1, {labels : res.labels, xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
			new Chart("test_time_chart", res.testTimeData, 1, {labels : res.labels, xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
			$("#sitemon_config_section_tab").find("a").tab('show');
			
			bUpdatedReport = true;
			if (bUpdatedResources) {
				hideProgressBar();
			}
		}
	};
	ajaxObj.call();
}
</script>
</body>
</html>
