<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<bs:box type="success" title="">
<bs:row>
	<bs:mco colsize="3">
	<bs:row>
			<bs:mco colsize="12">
				<bs:box type="primary" title="Documentation">
				<ul>
					<li>				
						<a href="https://github.com/factionsecurity/report_templates/raw/main/default-report-template.docx">Example Template docx</a>
					</li>
					<li>
						<a href="https://docs.factionsecurity.com/Custom%20Security%20Report%20Templates/">Report Variables and Scripting</a>
					</li>
				</ul>
				</bs:box>
				<bs:box type="primary" title="CSS">
						<bs:row>
							<bs:inputgroup name="Font Size" colsize="12" id="fontsize"><s:property value="fontsize"/></bs:inputgroup>
							<bs:inputgroup name="Font Family" colsize="12" id="fontname"><s:property value="fontname"/></bs:inputgroup>
							
	                    </bs:row>
	                    <br>
	                    <bs:row>
							<bs:inputgroup name="Header Size" colsize="6" id="headersize"><s:property value="headerSize"/></bs:inputgroup>
							<bs:inputgroup name="Footer Size" colsize="6" id="footersize"><s:property value="footerSize"/></bs:inputgroup>
	                    </bs:row>
	                    <br>
	                    <bs:row>
							<bs:mco colsize="12"><input type="checkbox" id="coverHeader" <s:if test="headerCover==true"> checked=checked</s:if> >Header on Cover</input></bs:mco>
							<bs:mco colsize="12"><input type="checkbox" id="coverFooter" <s:if test="footerCover==true"> checked=checked</s:if>>Footer on Cover</input></bs:mco>
	                    </bs:row>
	                    <bs:row>
	                    	<bs:button color="primary" size="md" colsize="6" text="Update" id="cssUpdate"></bs:button>
	                    	<bs:button color="success" size="md" colsize="6" text="Edit CSS" id="editCSS"></bs:button>
	                    </bs:row>
	                    <br>
	                    
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="12">
				<bs:box type="primary" title="Front Pages">
					<s:iterator value="fpages">
						<bs:row>
							<bs:mco colsize="8">
							    <s:if test="name == ''">
									<span class="page" id="page<s:property value="id"/>">Untitled</span>
								</s:if>
								<s:else>
									<span class="page" id="page<s:property value="id"/>"><s:property value="name"/></span>
								</s:else>
							</bs:mco>
							<bs:mco colsize="4">
								<div class="btn-group">
		                          <button id="up<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-menu-up"></i></button>
		                          <button id="dn<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-menu-down"></i></button>
		                          <button id="del<s:property value="id"/>" type="button" class="btn btn-danger btn-xs"><i class="glyphicon glyphicon-remove"></i></button>
		                        </div>
		                    </bs:mco>
	                    </bs:row>
	                    <hr>

					</s:iterator>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="12">
				<bs:box type="success" title="Back Pages">
					<s:iterator value="bpages">
						<bs:row>
							<bs:mco colsize="8">
								<span class="page" id="page<s:property value="id"/>"><s:property value="name"/></span>
							</bs:mco>
							<bs:mco colsize="4">
								<div class="btn-group" style="min-width:70px">
		                          <button id="up<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-menu-up"></i></button>
		                          <button id="dn<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-menu-down"></i></button>
		                          <button id="del<s:property value="id"/>"type="button" class="btn btn-danger btn-xs"><i class="glyphicon glyphicon-remove"></i></button>
		                        </div>
		                    </bs:mco>
	                    </bs:row>
	                    <hr>

					</s:iterator>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
			<bs:mco colsize="12">
				<bs:box type="warning" title="Images">
				<s:iterator value="images">
						<bs:row>
							<bs:mco colsize="8">
								<span class="images" id="img<s:property value="id"/>"><s:property value="name"/></span>
							</bs:mco>
							<bs:mco colsize="4">
								<div class="btn-group" style="min-width:70px">
								<button id="showImg<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-picture"></i></button>
								  <button id="copyImg<s:property value="id"/>" type="button" class="btn btn-default btn-xs"><i class="glyphicon glyphicon-copy"></i></button>
		                          <button id="delImg<s:property value="id"/>" type="button" class="btn btn-danger btn-xs"><i class="glyphicon glyphicon-remove"></i></button>
		                        </div>
		                    </bs:mco>
	                    </bs:row>
	                    <hr>

					</s:iterator>
				</bs:box>
			</bs:mco>
		</bs:row>
		<bs:row>
		 <bs:button color="warning" size="md" colsize="12" text="New Page" id="new"></bs:button>
		</bs:row>
		<br>
		<bs:row>
		 <bs:button color="primary" size="md" colsize="12" text="Upload Image" id="imgBtn"></bs:button>
		</bs:row>
		<br>
		<bs:row>
		 <bs:button color="success" size="md" colsize="12" text="Show Sample Report" id="sample"></bs:button>
		</bs:row>
	</bs:mco>
	<bs:mco colsize="9">
		<bs:box type="warning" title="<span id='boxTitle'> Page Editor</span>">
			<bs:row>
				<bs:inputgroup name="" colsize="6" id="titleName" placeholder="Title"></bs:inputgroup>
				<bs:mco colsize="3">
						<div class="form-group">
	                    <label>
	                      <input type="radio" name="r3" class="flat-red" id="isFront" checked>
	                    </label>
	                    &nbsp;Front
	                    &nbsp;|&nbsp;
	                    <label>
	                      <input type="radio" name="r3" class="flat-red" id="isBack" value="false">
	                    </label>
	                    &nbsp;Back

	                  </div>
	        	</bs:mco>
				<bs:button color="primary" size="md" colsize="2" text="Save" id="addPage"></bs:button>

			</bs:row>
			<bs:row>
				<bs:mco colsize="12">
					 <textarea id="header" name="header">
	                 </textarea>
                </bs:mco>
				<bs:mco colsize="12">
					 <textarea id="editor" name="editor">
	                 </textarea>
                </bs:mco>
                <bs:mco colsize="12">
					 <textarea id="footer" name="footer">
	                 </textarea>
                </bs:mco>
			</bs:row>
		</bs:box>
	</bs:mco>
</bs:row>
</bs:box>

<bs:modal modalId="fileDialog" saveId="uploadFile" title="Image Upload" width="40%" >
<form enctype="multipart/form-data" action="CMSImageUpload" id="imgForm" method="POST">
	<bs:row>
		<bs:inputgroup name="Display Name" colsize="4" id="displayName" htmlname="displayName"></bs:inputgroup>
		<div class="col-md-12">
                <label class="control-label">Select Image File</label>
    			<input id="image" type="file" name="file_data"/>
        </div>
	</bs:row>
</form>
</bs:modal>
<bs:modal modalId="cssModal" saveId="SaveCSS" title="Edit CSS">
	<textarea id="css" class="css">
		<s:property value="css"/>
	</textarea>
</bs:modal>




