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
var bReportAutoRefresh = false;
$(document).ready(function() {
	$("#nav_siteMon").addClass("active");
	showProgressBar('<@spring.message "siteMon.message.updateReport"/> & <@spring.message "perfTest.message.updateResource"/>');
	$("#sitemon_config_section_tab").find("a").tab('show');
	addValidation();
	bindEvent();
	updateScriptResources();
	initReportChart();
<#if msg??>
	alert("${msg}");
</#if>
});

var validationOptions = {};
function addValidation() {
    validationOptions = {
		rules: {
			monitoringInterval : {
				required: true,
				digits: true,
				min: 1,
				max: 9999
			}
		},
		errorPlacement : function(error, element) {
			error.appendTo(element.parent());
		}
	};

	$("#siteMon_config_form").validate(validationOptions);
}

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
	$("#auto_refresh_on").click(function() {
		bReportAutoRefresh = true;
		$("#auto_refresh_on").addClass("disabled");
		$("#auto_refresh_off").removeClass("disabled");
	});
	$("#auto_refresh_off").click(function() {
		bReportAutoRefresh = false;
		$("#auto_refresh_on").removeClass("disabled");
		$("#auto_refresh_off").addClass("disabled");
	});
	$("#test_result_chart").bind("jqplotDataClick", function (evt, seriesIndex, pointIndex, data) {
		if (seriesIndex == 0) {	// success result event
			return;
		}
		var ajaxObj = new AjaxObj("${req.getContextPath()}/sitemon/api/${siteMon.id}/log", null, "<@spring.message "common.error.error"/>");
		ajaxObj.params = {
			'minTimestamp' : new Date(resultErrorData[pointIndex][TIMESTAMP_INDEX]).getTime(),
			'maxTimestamp' : new Date(resultErrorData[pointIndex][LAST_TIMESTAMP_INDEX]).getTime()
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
		onHideProgress();
	};
	ajaxObj.call();
}

var ONE_MINUTE_MS = 1000 * 60;
var TEN_MINUTE_MS = ONE_MINUTE_MS * 10;
var TIMESTAMP_INDEX = 0;
var VALUE_INDEX = 1;
var LAST_TIMESTAMP_INDEX = 2;
var lastDataTimestamp;
var resultSuccessData = [];
var resultErrorData = [];
var testTimeData;
var resultChart;
var timeChart;
function initReportChart() {
<#if formMode?? && formMode == true>
	var formMode = true;
<#else>
	var formMode = false;
</#if>
	if (formMode) {
		bUpdatedReport = true;
		onHideProgress();
		return;
	}
	$("#sitemon_report_section_tab").find("a").tab('show');
	$("#sitemon_report_section_tab").show();
	var ajaxObj = new AjaxObj("${req.getContextPath()}/sitemon/api/${siteMon.id}/result", null, "<@spring.message "common.error.error"/>");
	ajaxObj.success = function(res) {
		if (eval(res.successData).length == 0) {
			$("#noResultGuide").removeClass("hide");
			bUpdatedReport = true;
			onHideProgress();
			return;
		}
		$("#resultChart").removeClass("hide");
		drawReportChart(res);
		setInterval(reportAutoRefresh, ONE_MINUTE_MS);
		bUpdatedReport = true;
		onHideProgress();
	};
	ajaxObj.call();
}

function drawReportChart(res) {
	labels = res.labels;
	var mergeResult = mergeLabel(eval(res.successData), eval(res.errorData));
	createSamplingData(mergeResult[0], mergeResult[1], TEN_MINUTE_MS);
	testTimeData = eval(res.testTimeData);
	resultChart = new Chart("test_result_chart", [resultSuccessData, resultErrorData], 1, {labels : ["success", "error"], seriesColors: ["#0000ff", "#ff0000"], xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
	timeChart = new Chart("test_time_chart", testTimeData, 1, {labels : labels, xAxisMin : res.minTimestamp, xAxisMax : res.maxTimestamp, xAxisRenderer : $.jqplot.DateAxisRenderer, xAxisFormatString : '%H:%M'}).plot();
}

function reportAutoRefresh() {
	if (bReportAutoRefresh == false) {
		return;
	}
	var ajaxObj = new AjaxObj("${req.getContextPath()}/sitemon/api/${siteMon.id}/result", null, "<@spring.message "common.error.error"/>");
	ajaxObj.success = function(res) {
		drawReportChart(res);
	};
	ajaxObj.call();
}

function mergeLabel(successData, errorData) {
	if (successData.length == 1) {
		return [successData[0], errorData[0]];
	}
	successData.push(merge(successData.pop(), successData.pop()));
	errorData.push(merge(errorData.pop(), errorData.pop()));
	
	return mergeLabel(successData, errorData);
}

function merge(data1, data2) {
	var i = 0;
	var j = 0;
	var result = [];
	for (; i < data1.length && j < data2.length;) {
		var time1 = data1[i][TIMESTAMP_INDEX];
		var time2 = data2[j][TIMESTAMP_INDEX];
		var value1 = data1[i][VALUE_INDEX];
		var value2 = data2[j][VALUE_INDEX];
		if (time1 == time2) {
			result.push([time1, value1 + value2]);
			i++;
			j++;
		} else if (time1 < time2) {
			result.push([time1, value1]);
			i++;
		} else {
			result.push([time2, value2]);
			j++;
		}
	}
	for (; i < data1.length; i++) {
		result.push(data1[i]);
	}
	for (; j < data2.length; j++) {
		result.push(data2[j]);
	}
	
	return result;
}

function createSamplingData(successData, errorData, samplingInterval) {
	resultSuccessData = [];
	resultErrorData = [];
	var len = successData.length;
	var i = 0;
	do {
		var minTime = successData[i][TIMESTAMP_INDEX];
		minTime = minTime.replace(/(\d\d:\d)\d(:\d\d)/, "$10$2");
		var maxTime = new Date(minTime).getTime() + samplingInterval - 1;
		maxTime = dateToString(new Date(maxTime), "yyyy-MM-dd HH:mm:ss");
		var success = successData[i][VALUE_INDEX];
		var error = errorData[i][VALUE_INDEX];
		var data = new SamplingData(minTime, maxTime, success, error);
		
		for(; i < len; i++) {
			if (successData[i][TIMESTAMP_INDEX] > data.maxTime) {
				break;
			}
			data.add(successData[i][VALUE_INDEX], errorData[i][VALUE_INDEX]);
		}
		resultSuccessData.push([data.minTime, data.success, data.maxTime]);
		resultErrorData.push([data.minTime, data.error, data.maxTime]);
	} while (i < len);
}

function SamplingData(minTime, maxTime, success, error) {
	this.minTime = minTime;
	this.maxTime = maxTime;
	this.success = success;
	this.error = error;
}
SamplingData.prototype = {
	add : function(success, error) {
		if (success < this.success) this.success = success;
		if (error > this.error) this.error = error;
	}
}

function onHideProgress() {
	if (bUpdatedResources && bUpdatedReport) {
		hideProgressBar();
	}
}
</script>
</body>
</html>
