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
	
	div.div-logs {
        border: 1px solid #D6D6D6;
        min-width: 340px;
        height: 110px;
        margin-bottom: 8px;
        overflow-y: scroll;
        border-radius: 3px 3px 3px 3px;
    }

    div.div-logs .log {
        color: #666666;
        display: block;
        margin-left: 7px;
        margin-top: 2px;
        margin-bottom: 2px;
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
var labels = [];
var bUpdatedReport = false;
var bUpdatedResources = false;
$(document).ready(function() {
	$("#nav_siteMon").addClass("active");
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
	$("#pause_siteMon_btn").click(function() {
		var ajaxObj = new AjaxProgressBarObj("${req.getContextPath()}/sitemon/api/pause/${siteMon.id}");
		ajaxObj.success = function() {
			location.href = "${req.getContextPath()}/sitemon/get/${siteMon.id}";
		}
		ajaxObj.call();
	});
	$("#start_siteMon_btn").click(function() {
		var ajaxObj = new AjaxProgressBarObj("${req.getContextPath()}/sitemon/api/run/${siteMon.id}");
		ajaxObj.success = function() {
			location.href = "${req.getContextPath()}/sitemon/get/${siteMon.id}";
		}
		ajaxObj.call();
	});
	$("#delete_siteMon_btn").click(function() {
		var ajaxObj = new AjaxProgressBarObj("${req.getContextPath()}/sitemon/api/delete/${siteMon.id}");
		ajaxObj.success = function() {
			location.href = "${req.getContextPath()}/sitemon/list";
		}
		ajaxObj.call();
	});
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
	$("#test_result_chart").bind("jqplotDataClick", function (evt, seriesIndex, pointIndex, data) {
		if (seriesIndex == 0) {	// success result value
			return;
		}
		var ajaxObj = new AjaxObj("${req.getContextPath()}/sitemon/api/${siteMon.id}/log", null, "<@spring.message "common.error.error"/>");
		ajaxObj.params = {
			'minTimestamp' : new Date(errorData[pointIndex][TIMESTAMP_INDEX]).getTime(),
			'maxTimestamp' : new Date(errorData[pointIndex][LAST_TIMESTAMP_INDEX]).getTime()
		};
		ajaxObj.success = function(res) {
			var html = "";
			for (var i = 0; i < res.length; i++) {
				var log = res[i];
				if (log.length > 0) {
					html = html + "<div class='log ellipsis' title='" + log + "'>" + log + "</div>";
				}
			}
			$("#error_log").html(html);
		};
		ajaxObj.call();
    });
}

function updateScriptResources(bInitHosts) {
	var scriptName = $("#script_name").val();

	var ajaxObj = new AjaxObj("${req.getContextPath()}/perftest/api/resource", null, "<@spring.message "common.error.error"/>");
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

var TEN_MINUTE_MS = 1000 * 60 * 10;
var TIMESTAMP_INDEX = 0;
var VALUE_INDEX = 1;
var LAST_TIMESTAMP_INDEX = 2;
var errorData;
function drawReportChart() {
	var ajaxObj = new AjaxObj("${req.getContextPath()}/sitemon/api/${siteMon.id}/result", null, "<@spring.message "common.error.error"/>");
	ajaxObj.success = function(res) {
		if (res.successData && res.successData.length > 0) {
			labels = res.labels;
			$("#sitemon_report_section_tab").show();
			$("#sitemon_report_section_tab").find("a").tab('show');
			var sumResult = sumResultGroupByTimestamp(res.successData, res.errorData);
			var resultData = getSamplingData(sumResult[0], sumResult[1], TEN_MINUTE_MS);
			errorData = resultData[1];
			new Chart("test_result_chart", resultData, 1, {labels : ["success", "error"], seriesColors: ["#0000ff", "#ff0000"], markerStyle: "square", xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
			new Chart("test_time_chart", res.testTimeData, 1, {labels : res.labels, xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
			$("#sitemon_config_section_tab").find("a").tab('show');
		}
		bUpdatedReport = true;
		if (bUpdatedResources) {
			hideProgressBar();
		}
	};
	ajaxObj.call();
}

function sumResultGroupByTimestamp(successData, errorData) {
	var testCount = successData.length;
	var timestampCount = successData[0].length;
	var successRes = [];
	var errorRes = [];
	for (var i = 0; i < timestampCount; i++) {
		var timestamp = successData[0][i][TIMESTAMP_INDEX];
		var success = 0;
		var error = 0;
		for (var j = 0; j < testCount; j++) {
			success += successData[j][i][VALUE_INDEX];
			error += errorData[j][i][VALUE_INDEX];
		}
		successRes[i] = [timestamp, success];
		errorRes[i] = [timestamp, error];
	}
	return [successRes, errorRes];
}

function getSamplingData(successData, errorData, samplingInterval) {
	var successRes = [];
	var errorRes = []
	var len = successData.length;
	
	var i = 0;
	do {
		var minTime = successData[i][TIMESTAMP_INDEX];
		var maxTime = new Date(minTime).getTime() + samplingInterval - 1;
		maxTime = dateToString(new Date(maxTime), "yyyy-MM-dd HH:mm:ss");
		var success = successData[i][VALUE_INDEX];
		var error = errorData[i][VALUE_INDEX];
		
		for(i = i + 1; i < len; i++) {
			if (successData[i][TIMESTAMP_INDEX] > maxTime) {
				break;
			}
			if (successData[i][VALUE_INDEX] < success) success = successData[i][VALUE_INDEX];
			if (errorData[i][VALUE_INDEX] > error) error = errorData[i][VALUE_INDEX];
		}
		successRes.push([minTime, success]);
		errorRes.push([minTime, error, maxTime]);
	} while (i < len);
	
	return [successRes, errorRes];
}
</script>
</body>
</html>
