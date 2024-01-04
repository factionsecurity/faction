<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<style>
.extensionPage{
	padding-bottom: 50px;
	padding-right: 50px;

}
</style>

<div class="content-wrapper">
	<section class="content-header">
		<h1>
			<i class="fa fa-upload"></i> Install Extension
		</h1>
	</section>

	<section class="content">
		<bs:row>
			<bs:mco colsize="6">
				<bs:box type="primary" title="Install Custom Extensions">
					<bs:row>
						<bs:mco colsize="9"></bs:mco>
						<bs:button color="primary" size="2" colsize="3" text="Back To Store" id="backToAppStore"></bs:button>
					</bs:row>
					<bs:row>
					<bs:mco colsize="12">
					<div class="form-group" id="appUpload">
						<label>Upload Extension</label> <input id="appFile" type="file"
							multiple name="file_data" />
					</div>
					</bs:mco>
					</bs:row>
					<div id="extensionPage" class="extensionPage" style="display:none">
						<jsp:include page="ExtensionPage.jsp" />
						<bs:row>
							<bs:mco colsize="8"></bs:mco>
							<bs:button color="primary" size="2" colsize="2" text="Cancel" id="cancelInstall"></bs:button>
							<bs:button color="success" size="2" colsize="2" text="Install" id="installExtension"></bs:button>
						</bs:row>
					</div>
				</bs:box>
			</bs:mco>
		</bs:row>
	</section>
</div>



	<jsp:include page="../footer.jsp" />
	<script src="../dist/js/install_extension.js"></script>

	</body>
	</html>