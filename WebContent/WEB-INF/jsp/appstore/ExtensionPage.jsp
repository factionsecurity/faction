<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<style>
.extensionDescription{
	min-height: 500px;
	padding: 30px;
	
}
.disabled{
	//opacity: 0.2;
    pointer-events: none;
}
.appLogo{
  border-radius: 25px;
  height: 150px;
  margin-top:20px;
}
.appLogo-small{
  border-radius: 5px;
  height: 50px;
  margin-left: 30px;
}
.appAuthor{
font-weight: normal;
}
.appDescription{
 padding-top: 50px;
 font-size: large;
 padding-left: 20px;
}
</style>	
<div class="extensionDescription">
	<bs:row>
		<bs:mco colsize="2" style="min-width: 200px">
			<img id="appLogo" src="../app-default.png" class="appLogo" />
		</bs:mco>
		<bs:mco colsize="8">
			<h1 id="appTitle">
			</h1>
			By: <span id="appAuthor" class="appAuthor"></span>
			<br />
			Link: <a href="" id="appURL"></a>
			<br />
			Hash: <span id="appHash"></span>
		</bs:mco>
	</bs:row>
	<bs:row>
		<bs:mco colsize="12">
			<div id="appDescription" class="appDescription">
			</div>
		</bs:mco>
	</bs:row>
</div>