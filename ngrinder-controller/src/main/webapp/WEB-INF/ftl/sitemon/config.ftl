<form id="siteMon_config_form" name="siteMon_config_form" action="${req.getContextPath()}/sitemon/save"  method="POST" style="margin-bottom: 0px;">
	<div class="well" style="margin-top:0">
		<input type="hidden" id="siteMon_id" name="id" value="${(siteMon.id)!}">
		<div class="form-horizontal" id="query_div">
			<fieldset>
				<div class="control-group" style="margin-bottom:0px;">
					<div class="row">
						<div class="span5-6">
							<@control_group name = "siteMon_control" controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key = "siteMon.config.id">
								<input class="required span3 left-float" maxlength="50" size="30" type="text" id="siteMon_id" name="siteMonId" value="${siteMon.id}" disabled/>
							</@control_group>
						</div>
						<div class="span5-6"></div>
						<div class="span1" style="margin-left:0">
							<div class="control-group">
								<#if !(siteMon.createdUser??) || siteMon.createdUser.userId != currentUser.factualUser.userId>
									<#assign disabled = "disabled">
								</#if>
								<button type="submit" class="btn btn-success" id="save_siteMon_btn" style="width:55px" ${disabled!}>
									<@spring.message "common.button.save"/>
								</button>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="span">
							<#if (siteMon.scriptRevision > 0)>
								<#assign scriptRevision = siteMon.scriptRevision>
							<#else>
								<#assign scriptRevision = -1>
							</#if>
							<@control_group name = "script_control" controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key = "perfTest.config.script">
								<select id="script_name" class="required" name="scriptName" style="width: 330px">
									<option value="${siteMon.scriptName}">${siteMon.scriptName}</option>
								</select>
								<input type="hidden" id="script_revision"
									name="scriptRevision"
									value="${scriptRevision}"/>
								<button class="btn btn-mini btn-info" type="button"
									id="show_script_btn"
									style="margin-top: 3px;">R
								<#if scriptRevision != -1>
									${siteMon.scriptRevision}
								<#else>
									HEAD
								</#if>
								</button>
								<#if scriptRevision != -1>
									<button class="btn btn-mini btn-info" type="button"
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
	
	<div class="row">
		<div class="span6">
			<fieldset>
				<legend><span>Hosts Configuration</span></legend>
			</fieldset>
			<div class="form-horizontal form-horizontal-2">
				<#assign targetHosts = siteMon.targetHosts>
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
						value="${(siteMon.param?html)}"
						message="perfTest.config.param"/>
				</@control_group>
			</div>
		</div>
		<!-- end test content right -->
	</div>
</form>
<!-- end configuration -->