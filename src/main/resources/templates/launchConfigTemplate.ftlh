<#ftl output_format="XML" auto_esc=true>
<?xml version="1.0" encoding="UTF-8"?>
<weasisConfig>
	<javaOptions>
		<@show_marker/>

		<#if ver?has_content>
			<#assign ver = ver?ensure_starts_with('-') >
		</#if>

		<#if !(cdb?has_content) || assignFromTemplate??  >
			<#assign cdb = r'${svr}/weasis${ver!}'>
		</#if>

		<#if !(cfg?has_content) || assignFromTemplate??  >
			<#assign cfg = r'${svr}/overrideConfig/properties?packageVersionId=${packageVersionId!}&launchConfigId=${launchConfigId!}&groupId=${groupId!}'>
		</#if>

		<#macro show_marker>
			<#if showMarker??>
				<!-- ########################## Template Markers : ##########################-->
				<!-- ${r'${ver}'} = weasis.version = '${ver!}' -->
				<!-- ${r'${svr}'} = weasis.server.url = '${svr!}' -->
				<!-- ${r'${cdb}'} = weasis.codebase.url = '${cdb!}' -->
				<!-- ${r'${cfg}'} = felix.config.properties = '${cfg!}'  -->
				<!-- ######################################################################## -->
			</#if>
		</#macro>

		<@show_marker/>

		<#-- Re interpret placeHolders for final assignment -->
		<@'<#assign cdb = "${cdb!?trim}">'?interpret />
		<@'<#assign cfg = "${cfg!?trim}">'?interpret />

		<@show_marker/>

		<#-- properties below can't be overloaded with their name-->

		<#assign "weasis.codebase.url" = cdb!>
		<#assign "felix.config.properties" = cfg!>

		<#--note following interpret shouldn't be required -->
		<property name="weasis.codebase.url" value= "<@weasis\.codebase\.url?interpret/>" />
		<property name="felix.config.properties" value="<@felix\.config\.properties?interpret/>" />

		<!-- weasis user property -->
		<#if user?has_content >
		<property name="weasis.user" value="${user?trim}" />
		</#if>

		<!-- i18n version property -->
		<#if i18nVersion?has_content>
		<property name="weasis.i18n" value="${viewerHubServerUrl}/weasis/i18n/${i18nVersion!?trim}" />
		</#if>

		<#macro setProperty property>
			<@'<#assign interpretedProperty = "${property?trim}">'?interpret/>
			<#local propertyName = interpretedProperty?keep_before(' ')>
			<#local propertyValue= interpretedProperty?keep_after(' ')?no_esc>
		<property name="${propertyName}" value="${propertyValue}"/>
		</#macro>

		<#list pro as property>
			<@setProperty property />
		</#list>

	</javaOptions>

	<#if arg?? >
	<arguments>
		<#list arg as argument>
		<argument>${argument}</argument>
		</#list>
	</arguments>
	</#if>

</weasisConfig>


