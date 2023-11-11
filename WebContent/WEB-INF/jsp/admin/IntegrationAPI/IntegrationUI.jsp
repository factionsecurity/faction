<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
  
<bs:row>
<bs:mco colsize="12">  
	<bs:box type="warning" title="">
		<bs:row>
		<bs:mco colsize="3">
			<bs:box type="success" title="Modules" >
			<div id="mod1" class="modules">
				<span><i class="glyphicon glyphicon-globe">&nbsp;</i>Application Inventory AutoComplete</span>
				<input class="checks" type="checkbox" id="check_mod1"/>
			</div>
			<div id="mod2" class="modules">
				<span><i class="glyphicon glyphicon-th-list">&nbsp;</i>Assessment Finalized</span>
				<input class="checks" type="checkbox" id="check_mod2"/>
			</div>
			<div id="mod3" class="modules">
				<span><i class="glyphicon glyphicon-ok-circle">&nbsp;</i>Verification Completed</span>
				<input class="checks" type="checkbox" id="check_mod3"/>
			</div>
			<!--  <div id="mod4" class="modules">
				<span><i class="glyphicon glyphicon-user">&nbsp;</i>Single Sign On</span>
				<input class="checks" type="checkbox" id="check4"/>
			</div>-->
			</bs:box>
		</bs:mco>
		<bs:mco colsize="7">
			<bs:box type="primary" title="Code Editor">
				<textarea id="code" name="code"></textarea><br>
				<button class='btn btn-primary savebtn'>Save</button> <button  class='btn btn-success testbtn'>Test</button>
			</bs:box>
		</bs:mco>
		<bs:mco colsize="2">
		<bs:box type="success" title="<i class=\"glyphicon glyphicon-log-in\">&nbsp;</i>Input Variables">
			<textarea id="args" name="args"></textarea>
		</bs:box>
		<bs:box type="danger" title="<i class=\"glyphicon glyphicon-log-out\">&nbsp;</i>Output Format">
			<textarea id="retArgs" name="retArgs"></textarea>
		</bs:box>
		</bs:mco>
		</bs:row>

	</bs:box>
</bs:mco>
</bs:row>

<bs:modal modalId="testModal" saveId="" title="Console Output" width="900px">
	<textarea id="console" name="console"></textarea>
</bs:modal>


