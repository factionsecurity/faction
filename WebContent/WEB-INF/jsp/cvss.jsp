<%@page import="org.apache.struts2.components.Include"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<jsp:include page="header.jsp" />
<%@taglib prefix="bs" uri="/WEB-INF/BootStrapHandler.tld"%>

<style>
.active {
	background-color: purple !important;
	color: white !important;
	font-weight: bold;
}

label.btn {
	background-color: lightgray;
	color: #030D1C;
}

label.btn:hover {
	font-weight: bold;
}
.scoreBody{
	background-color: lightGray;
	border-radius: 9px;
	text-align: center;
	padding-bottom: 5px;
	margin-top: -100px;
}
.scoreBody h3{
	font-size: xxx-large;
	color: lightgray;
	background-color:green;
	border-top-right-radius: 9px;
	border-top-left-radius: 9px;
}
.scoreBody span{
	font-size: large;
	color: green;
}
</style>


<link href="../fileupload/css/fileinput.min.css" media="all"
	rel="stylesheet" type="text/css" />
<!-- Content Wrapper. Contains page content -->
<div class="content-wrapper">
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			CVSS 3.1 <small></small>
		</h1>
	</section>

	<!-- Main content -->
	<section class="content">
		<bs:row>
			<bs:mco colsize="8">
				<bs:box type="success" title="<h1>Base Score</h1>">
					<bs:row>
						<bs:mco colsize="6">
							<bs:box type="success" title="Attack Vector (AV)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="attackVector" id="av_n" autocomplete="off"
										checked> Network (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="attackVector" id="av_a" autocomplete="off">
										Adjacent (A)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="attackVector" id="av_l" autocomplete="off">
										Local (L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="attackVector" id="av_p" autocomplete="off">
										Physical (P)
									</label>
								</div>
							</bs:box>
						</bs:mco>
						<bs:mco colsize="4">
							<bs:box type="success" title="Scope (S)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="scope" id="s_u" autocomplete="off" checked>
										Unchanged (U)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="scope" id="s_o" autocomplete="off"> Changed (O)
									</label>
								</div>
							</bs:box>
						</bs:mco>
						<bs:mco colsize="2">
							<div class="scoreBody">
								<h3 class="scoreNumber" id="score">0.0</h3>
								<span class="severity" id="severity">None</span>
							</div>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="6">
							<bs:box type="success" title="Attack Complexity (AC)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="attackComplexity" id="ac_l"
										autocomplete="off" checked> Low (L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="attackComplexity" id="ac_h" autocomplete="off">
										High (H)
									</label>
								</div>
							</bs:box>
						</bs:mco>
						<bs:mco colsize="6">
							<bs:box type="success" title="Confidentiality (C)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="confidentiality" id="c_n"
										autocomplete="off" checked> None (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="confidentiality" id="c_l" autocomplete="off">
										Low (L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="confidentiality" id="c_h" autocomplete="off">
										High (H)
									</label>
								</div>
							</bs:box>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="6">
							<bs:box type="success" title="Privileges Required (PR)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="privileges" id="pr_n" autocomplete="off"
										checked> None (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="privileges" id="pr_l" autocomplete="off"> Low
										(L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="privileges" id="pr_h" autocomplete="off"> High
										(H)
									</label>
								</div>
							</bs:box>
						</bs:mco>
						<bs:mco colsize="6">
							<bs:box type="success" title="Integrity (I)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="integrity" id="i_n" autocomplete="off"
										checked> None (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="integrity" id="i_l" autocomplete="off"> Low (L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="integrity" id="i_h" autocomplete="off"> High (H)
									</label>
								</div>
							</bs:box>
						</bs:mco>
					</bs:row>
					<bs:row>
						<bs:mco colsize="6">
							<bs:box type="success" title="User Interaction (UI)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="userInteraction" id="ui_n"
										autocomplete="off" checked> None (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="userInteraction" id="ui_r" autocomplete="off">
										Required (R)
									</label>
								</div>
							</bs:box>
						</bs:mco>
						<bs:mco colsize="6">
							<bs:box type="success" title="Availability (A)">
								<div class="btn-group btn-group-toggle" data-toggle="buttons">
									<label class="btn btn-secondary active"> <input
										type="radio" name="availability" id="a_n" autocomplete="off"
										checked> None (N)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="availability" id="a_l" autocomplete="off"> Low
										(L)
									</label> <label class="btn btn-secondary"> <input type="radio"
										name="availability" id="a_h" autocomplete="off"> High
										(H)
									</label>
								</div>
							</bs:box>
						</bs:mco>
					</bs:row>
				</bs:box>
			</bs:mco>
		</bs:row>

		<jsp:include page="footer.jsp" />
		<script>
  $(function(){
	  let scores = {
		"network": 0.85,
		"adjacent": 0.62,
		"local": 0.55,
		"physical": 0.2,
		"ac_low": 0.77,
		"ac_high": 0.44,
		"pr_none": 0.85,
		"pr_low": 0.62,
		"pr_high": 0.27,
		"ui_none": 0.85,
		"ui_required": 0.62,
		"high": 0.56,
		"low": 0.22,
		"none": 0
	  }
	  function check(id){
		  return $("#"+id).is(":checked");
	  }
	  function calcScore(){
		  let av=scores["network"];
		  let ac=scores["ac_low"];
		  let pr=scores["pr_none"];
		  let ui=scores["ui_none"];
		  let c=scores["none"];
		  let i=scores["none"];
		  let a=scores["none"];
		  
		  if(check("av_n")){
			  av=scores["network"]
		  }else if(check("av_a")){
			  av=scores["adjacent"]
		  }else if(check("av_l")){
			  av=scores["local"]
		  }else{
			  av=scores["physical"]
		  }
		  
		  if(check("ac_l")){
			  ac=scores["ac_low"]
		  }else{
			  ac= scores["ac_high"]
		  }
		  
		  if(check("pr_n")){
			  pr=scores["pr_none"]
		  }else if(check("pr_l") && check("s_u")){
			  pr=scores["pr_low"]
		  }else if(check("pr_l") && !check("s_u")){
			  pr=0.68
		  }else if(check("pr_h") && !check("s_u")){
			  pr=0.5
		  }else {
			  pr=scores["pr_high"]
		  }
		  
		  if(check("ui_n")){
			  ui=scores["ui_none"]
		  }else {
			  ui=scores["ui_required"]
		  }
		  
		  if(check("c_n")){
			  c=scores["none"]
		  }else if(check("c_l")){
			  c=scores["low"]
		  }else {
			  c=scores["high"]
		  }
		  
		  if(check("i_n")){
			  i=scores["none"]
		  }else if(check("i_l")){
			  i=scores["low"]
		  }else {
			  i=scores["high"]
		  }
		  
		  if(check("a_n")){
			  a=scores["none"]
		  }else if(check("a_l")){
			  a=scores["low"]
		  }else {
			  a=scores["high"]
		  }
		  
		  let iss = 1 - ( (1 - c) * (1 - i) * (1 - a)  )
		  console.log(iss);
		  let impact = 0.0
		  if(check("s_u")){
			  impact = iss * 6.42
		  }else if (iss == 0){
			  return 0;
		  }else{
			  let p1 = (7.52 * (iss - 0.029));
			  let p2 = (iss - 0.02) ** 15;
			  let p3 = 3.25 * p2
			  impact = p1 - p3;
		  }
		  
		  if(impact == 0){
			  return 0.0
		  }
		  
		  let exploitability = 8.22 * av * ac * pr * ui;
		  
		  if(check("s_u")){
			 return impact + exploitability; 
		  }else{
			  return 1.08 * (impact + exploitability)
		  }
		  
	  }
	  function roundup(score){
		  let rounded = Math.round(score *10) / 10;
		  if(score > rounded){
			  return Math.min(Math.round( (rounded + 0.1) *10 )/ 10, 10);
		  }else{
			  return Math.min(rounded,10)
		  }
	  }
	  $("input").on("click", function(event){
		 let el = this;
		 let name = el.name;
		 $("input[name=" + name +"]").each( (_index, e) => {
			 let p = $(e).parent()[0];
			 $(p).removeClass("active");
		 });
	  	 $($(el).parent()[0]).addClass("active");
	  	 let score = roundup(calcScore());
	  	 $("#score").html(score);
		});
	  
  });
		</script>
		</body>
		</html>