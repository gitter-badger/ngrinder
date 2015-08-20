<#if !(siteMon.createdUser??) || siteMon.createdUser.userId != currentUser.factualUser.userId>
	<#assign disabled = "disabled">
</#if>
<#if formMode?? && formMode == true>
	<#assign saved = false>
<#else>
	<#assign saved = true>
</#if>
<form id="siteMon_config_form" name="siteMon_config_form" action="${req.getContextPath()}/sitemon/save"  method="POST" style="margin-bottom: 0px;">
	<div class="well" style="margin-top:0">
		<input type="hidden" id="siteMon_id" name="id" value="${(siteMon.id)!}">
		<input type="hidden" name="run" value="${(siteMon.run)?string('true', 'false')}">
		<div class="form-horizontal" id="query_div">
			<fieldset>
				<div class="control-group" style="margin-bottom:0px;">
					<div class="row">
						<div class="span5-6">
							<@control_group name = "siteMon_control" controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key = "siteMon.config.id">
								<input class="required span3 left-float" maxlength="50" size="30" type="text" id="siteMon_id" name="siteMonId" value="${siteMon.id}" disabled/>
							</@control_group>
						</div>
						<div class="span3"></div>
						<div class="span3-4 pull-right" style="margin-left:0">
							<div class="control-group">
								<#if !(siteMon.createdUser??) || siteMon.createdUser.userId != currentUser.factualUser.userId>
									<#assign disabled = "disabled">
								</#if>
								<button type="submit" class="btn btn-success" id="save_siteMon_btn" ${disabled!}>
									<@spring.message "siteMon.config.save"/>
								<#if !saved>
									&nbsp;<@spring.message "siteMon.config.andStart"/>
								</#if>
								</button>
							<#if saved>
								<button type="button" class="btn btn-danger" id="delete_siteMon_btn" ${disabled!}>
									<@spring.message "siteMon.config.delete"/>
								</button>
							</#if>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="span">
							<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="siteMon.config.name">
								<input class="span3 left-float" name="name" maxlength="50" size="30" type="text" value="${siteMon.name!}"/>
							</@control_group>
						</div>
					</div>
				<#if saved>
					<div class="row">
						<div class="span">
							<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="siteMon.config.agentName">
								<input class="span3 left-float" maxlength="50" size="30" type="text" value="${siteMon.agentName!}" disabled/>
							</@control_group>
						</div>
					</div>
					<div class="row">
						<div class="span">
							<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="siteMon.list.runState">
							<#if siteMon.run>
								<input class="span3 left-float" maxlength="50" size="30" type="text" value="<@spring.message 'siteMon.state.start'/>" disabled/>
								<button type="button" class="btn btn-warning" id="pause_siteMon_btn" ${disabled!}>
									<@spring.message "siteMon.config.pause"/>
								</button>
							<#else>
								<input class="span3 left-float" maxlength="50" size="30" type="text" value="<@spring.message 'siteMon.state.pause'/>" disabled/>
								<button type="button" class="btn btn-warning" id="start_siteMon_btn" ${disabled!}>
									<@spring.message "siteMon.config.start"/>
								</button>
							</#if>
							</@control_group>
						</div>
					</div>
				</#if>
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
									style="margin-top: 3px;"><@spring.message "siteMon.config.editScript"/>
								<#if scriptRevision != -1>
									${siteMon.scriptRevision}
								<#else>
									HEAD
								</#if>
								</button>
								<#if scriptRevision != -1>
									<button class="btn btn-mini btn-info"
										id="use_revision_btn" rel="popover"
										type="button" title="<@spring.message "siteMon.config.useHead"/>"
										data-content="<@spring.message "siteMon.config.useHead.help"/>"
										data-html="true" data-placement="bottom"
										style="margin-top: 3px;"><@spring.message "siteMon.config.useHead"/></button>
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
				<legend><span><@spring.message "siteMon.config.hosts.title"/></span></legend>
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
				<legend><span><@spring.message "siteMon.config.param.title"/></span></legend>
			</fieldset>
			<div class="form-horizontal form-horizontal-2">
				<@control_group name="param" label_message_key="perfTest.config.param" err_style="margin-left:-90px">
					<@input_popover name="param"
						value="${(siteMon.param?html)}"
						message="perfTest.config.param"/>
				</@control_group>
			</div>
			
			<fieldset>
				<legend><span><@spring.message "siteMon.config.errorCallback"/></span></legend>
			</fieldset>
			<div class="form-horizontal form-horizontal-2">
				<@control_group name="errorCallback" label_message_key="siteMon.config.errorCallback" err_style="margin-left:-90px">
					<@input_popover name="errorCallback"
						value="${(siteMon.errorCallback)}"
						message="siteMon.config.errorCallback"/>
				</@control_group>
			</div>
		</div>
		<!-- end test content right -->
	</div>
</form>
<!-- end configuration -->