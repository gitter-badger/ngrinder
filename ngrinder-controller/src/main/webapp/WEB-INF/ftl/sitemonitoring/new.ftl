<!DOCTYPE html>
<html>
<head>
<#include "../common/common.ftl"/>
	<title><@spring.message "sitemonitoring.config.title"/></title>
	<style>
	.control-label input {
		vertical-align: top;
		margin-left: 2px
	}

	#sitemonitoring_id + span {
		float: left;
	}

	.controls .span3 {
		margin-left: 0;
	}

	.control-group.success td > label[for="sitemonitoring_id"] {
		color: #468847;
	}

	.control-group.error td > label[for="sitemonitoring_id"] {
		color: #B94A48;
	}

	.span5-6 {
		width: 410px;
	}
	
	div.div-resources {
        border: 1px solid #D6D6D6;
        height: 140px;
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
		height: 140px;
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
        margin-top:117px;
        margin-left:287px;
        position:absolute
    }
	</style>
</head>
<body>
<div id="wrap">
	<#include "../common/navigator.ftl">
	<div class="container">
		<fieldSet>
			<legend class="header"> <@spring.message "sitemonitoring.config.title"/> </legend>
		</fieldSet>
		<form id="sitemonitoring_config_form" name="sitemonitoring_config_form" action="${req.getContextPath()}/sitemonitoring/save"  method="POST">
			<div class="well" style="margin-bottom: 5px;margin-top:0">
				<input type="hidden" id="sitemonitoring_id" name="id" value="${(sitemonitoring.id)!}">
				<div class="form-horizontal" id="query_div">
					<fieldset>
						<div class="control-group" style="margin-bottom:0px;">
							<div class="row">
								<div class="span5-6">
									<@control_group name = "sitemonitoring_control" controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key = "sitemonitoring.config.id">
										<input class="required span3 left-float" maxlength="50" size="30" type="text" id="sitemonitoring_id" name="sitemonitoringId" value="${sitemonitoring.id}" disabled/>
									</@control_group>
								</div>
								<div class="span5-6"></div>
								<div class="span1" style="margin-left:0">
									<div class="control-group">
										<#if !(sitemonitoring.registUser??) || sitemonitoring.registUser.userId != currentUser.factualUser.userId>
											<#assign disabled = "disabled">
										</#if>
										<button type="submit" class="btn btn-success" id="save_sitemonitoring_btn" style="width:55px" ${disabled!}>
											<@spring.message "common.button.save"/>
										</button>
									</div>
								</div>
							</div>
							<div class="row">
								<div class="span">
									<#if (sitemonitoring.scriptRevision > 0)>
										<#assign scriptRevision = sitemonitoring.scriptRevision>
									<#else>
										<#assign scriptRevision = -1>
									</#if>
									<@control_group name = "script_control" controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key = "perfTest.config.script">
										<select id="script_name" class="required" name="scriptName" style="width: 330px">
											<option value="${sitemonitoring.scriptName}">${sitemonitoring.scriptName}</option>
										</select>
										<input type="hidden" id="script_revision"
											name="scriptRevision"
											value="${scriptRevision}"/>
										<button class="btn btn-mini btn-info pull-right" type="button"
											id="show_script_btn"
											style="margin-top: 3px;">R
										<#if scriptRevision != -1>
											${sitemonitoring.scriptRevision}
										<#else>
											HEAD
										</#if>
										</button>
										<#if scriptRevision != -1>
											<button class="btn btn-mini btn-info pull-right" type="button"
												id="use_revision_btn"
												style="margin-top: 3px;">use HEAD</button>
										</#if>
									</@control_group>
								</div>
							</div>
							<div class="row">
								<div class="span">
									<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="perfTest.config.scriptResources">
						            	<div class="div-resources" id="script_resources"></div>
									</@control_group>
								</div>
							</div>
						</div>
					</fieldset>
				</div>
			</div>
			<!-- end well -->
			<@security.authorize ifAnyGranted="A, S">
				<#if sitemonitoring.registUser?? && currentUser.userId != sitemonitoring.registUser.userId>
					<div class="pull-right">
						<a href="${req.getContextPath()}/user/switch?to=${sitemonitoring.registUser.userId!""}">
						<@spring.message "perfTest.list.owner"/> : ${sitemonitoring.registUser.userName!""} (${sitemonitoring.registUser.userId!""})
						</a>
					</div>
				</#if>
			</@security.authorize >
			<!-- end owner info -->
			
			<div class="row">
				<div class="span6">
					<fieldset>
						<legend><span>Hosts Configuration</span></legend>
					</fieldset>
					<div class="form-horizontal form-horizontal-2">
						<#assign targetHosts = sitemonitoring.targetHosts>
						<@control_group label_message_key="perfTest.config.targetHost">
							<#include "../perftest/host.ftl">
						</@control_group>
					</div>
				</div>
				<!-- end test content left -->
			
				<div class="span6">
					<fieldset>
						<legend><span>param Configuration</span></legend>
					</fieldset>
					<div class="form-horizontal form-horizontal-2">
						<@control_group name="param" label_message_key="perfTest.config.param" err_style="margin-left:-90px">
							<@input_popover name="param"
								value="${(sitemonitoring.param?html)}"
								message="perfTest.config.param"/>
						</@control_group>
					</div>
				</div>
				<!-- end test content right -->
			</div>
		</form>
		<!--content-->
	</div>
	<#include "../perftest/host_modal.ftl">
</div>
<#include "../common/copyright.ftl">
<script type="text/javascript">
$(document).ready(function() {
	bindEvent();
	updateScriptResources();
<#if msg??>
	alert("${msg}");
</#if>
});

function bindEvent() {
	$("#show_script_btn").click(function() {
		var currentScript = $("#script_name").val();
        var ownerId = "";
		<@security.authorize ifAnyGranted="A, S">					
			ownerId = "&ownerId=${(sitemonitoring.registUser.userId)!}";
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
			,'ownerId' : '${(sitemonitoring.registUser.userId)!}'
		</@security.authorize>
	};
	ajaxObj.success = function(res) {
		var html = "";
		var len = res.resources.length;
		if (bInitHosts) {
			initHosts(res.targetHosts);
		}
		for ( var i = 0; i < len; i++) {
			var value = res.resources[i];
			html = html + "<div class='resource ellipsis' title='" + value + "'>" + value + "</div>";
		}
		$("#script_resources").html(html);
	};
	ajaxObj.call();
}
</script>
</body>
</html>
