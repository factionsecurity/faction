<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>
<jsp:include page="../header.jsp" />
<link rel="stylesheet" href="../dist/css/Fuse.css">
	

<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			Team Calendar <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			
				<bs:mco colsize="12">
					<div class="fsgannt" ></div>
				</bs:mco>
			
			<bs:mco colsize="4">
				<bs:row>
					<bs:mco colsize="12">
						<bs:box type="info" title="Search Options:">
							<bs:select name="User:" colsize="12" id="user">
						
								<s:iterator value="users">
									<option value='<s:property value="id"/>'><s:property
											value="fname" />
										<s:property value="lname" /></option>
								</s:iterator>
							</bs:select>
							<bs:select name="Team:" colsize="12" id="team">
								<s:iterator value="teams">
									<option value='<s:property value="id"/>'><s:property
											value="TeamName" /></option>
								</s:iterator>
							</bs:select>
							<bs:dt name="Start and End Date:" colsize="12" id="reservation">
								<s:property value="startStr" /> - <s:property value="endStr" />
							</bs:dt>
							<bs:button color="primary" size="md" colsize="12" text="Search"
								id="search"></bs:button>

						</bs:box>
						</bs:mco>


						
						<bs:mco colsize="12">
							<bs:box type="danger" title="Add OOO: <small>Click any OOO item on the calendar to delete.">
							<bs:inputgroup name="Title:" colsize="12" id="title"></bs:inputgroup>
							<bs:select name="User:" colsize="12" id="userooo">
								<s:iterator value="users">
									<option value='<s:property value="id"/>'><s:property
											value="fname" />
										<s:property value="lname" /></option>
								</s:iterator>
							</bs:select>
							<bs:dt name="Start and End Date:" colsize="12" id="ooo">
								<s:property value="startStr" /> - <s:property value="endStr" />
							</bs:dt>
							<bs:button color="success" size="md" colsize="12" text="Add"
							id="add"></bs:button>

							</bs:box>
						</bs:mco>

						
				
				</bs:row>


			</bs:mco>
			<bs:mco colsize="8">
				<bs:box type="warning" title="">
					<bs:mco colsize="12">
						<div class="box box-primary">
							<div class="box-body no-padding">
								<!-- THE CALENDAR -->
								<div id="calendar"></div>
							</div>
							<!-- /.box-body -->
						</div>
						<!-- /. box -->
					</bs:mco>
				</bs:box>
			</bs:mco>

		</bs:row>
		<jsp:include page="../footer.jsp" />
  <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.29.1/moment.min.js" integrity="sha512-qTXRIMyZIFb8iQcfjXWCO8+M5Tbc38Qi5WzdPOYZHIlZpzBHG3L3by84BBBOiRGiEb7KKtAOAs5qYdUiZiQNNQ==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>

  <link href='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.css' rel='stylesheet' />
  <script src='https://cdn.jsdelivr.net/npm/fullcalendar@5.10.1/main.min.js'></script>
  <script src="../dist/js/calendar.js"></script>


  </body>
</html>