<div id="noResultGuide" class="alert alert-info hide">
	<@spring.message "siteMon.report.noResultGuideMsg"/>
</div>
<div id="resultChart" class="hide">
	<fieldSet>
		<legend>
			<@spring.message "siteMon.report.testResult"/>
			<div class="pull-right">
				<@spring.message "siteMon.report.autoRefresh"/>
				<div class="btn-group">
					<button id="auto_refresh_on" class="btn btn-default">
						<@spring.message "siteMon.report.autoRefresh.on"/>
					</button>
					<button id="auto_refresh_off" class="btn btn-default disabled">
						<@spring.message "siteMon.report.autoRefresh.off"/>
					</button>
				</div>
			</div>
		</legend>
	</fieldSet>
	<div id="test_result_chart" class="chart"></div>
	<div class="row form-horizontal">
		<@control_group controls_style = "margin-left: 140px;" label_style = "width: 120px;" label_message_key="siteMon.report.error.log">
			<div id="error_log" class="div-logs"></div>
		</@control_group>
	</div>
	<fieldSet>
		<legend><@spring.message "siteMon.report.testTime"/></legend>
	</fieldSet>
	<div id="test_time_chart" class="chart"></div>
</div>