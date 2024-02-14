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
	width: 150px;
}
.scoreBody h3{
	font-size: xxx-large;
	color: lightgray;
	border-top-right-radius: 9px;
	border-top-left-radius: 9px;
}
.scoreBody span{
	font-size: large;
	font-weight: bold;
}

h3.None {
	background-color: #00a65a;
}
span.None {
	color: #00a65a;
}
h3.Low {
	background-color: #39cccc;
}
span.Low {
	color: #39cccc;
}
h3.Medium {
	background-color: #00c0ef;
}
span.Medium {
	color: #00c0ef;
}
h3.High {
	background-color: #f39c12;
}
span.High {
	color: #f39c12;
}
h3.Critical {
	background-color: #dd4b39;
}
span.Critical {
	color: #dd4b39;
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
			<bs:mco colsize="8" style="width:1200px">
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
								<h3 class="scoreNumber None" id="score">0.0</h3>
								<span class="severity None" id="severity">None</span>
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
	  let limits = {
			"None": [0,0],
			"Low": [0.1,3.9],
			"Medium":[4,6.9],
			"High": [7,8.9],
			"Critical":[9,10]
	  }
	  function getSeverity(score){
		  if(score == 0){
			  return "None"
		  }
		  return Object.keys(limits).reduce( (acc, key) => score >= limits[key][0] && score <= limits[key][1]? key : acc, "None")
	  }
	  function check(id){
		  return $("#"+id).is(":checked");
	  }
	  function calcScore(){
		  let CVSSString = "CVSS:3.1"
		  let av=scores["network"];
		  let ac=scores["ac_low"];
		  let pr=scores["pr_none"];
		  let ui=scores["ui_none"];
		  let c=scores["none"];
		  let i=scores["none"];
		  let a=scores["none"];
		  
		  if(check("av_n")){
			  av=scores["network"]
			  CVSSString += "/AV:N"
		  }else if(check("av_a")){
			  av=scores["adjacent"]
			  CVSSString += "/AV:A"
		  }else if(check("av_l")){
			  av=scores["local"]
			  CVSSString += "/AV:L"
		  }else{
			  av=scores["physical"]
			  CVSSString += "/AV:P"
		  }
		  
		  if(check("ac_l")){
			  ac=scores["ac_low"]
			  CVSSString += "/AC:L"
		  }else{
			  ac= scores["ac_high"]
			  CVSSString += "/AC:H"
		  }
		  
		  if(check("pr_n")){
			  pr=scores["pr_none"]
			  CVSSString += "/PR:N"
		  }else if(check("pr_l") && check("s_u")){
			  pr=scores["pr_low"]
			  CVSSString += "/PR:L"
		  }else if(check("pr_l") && !check("s_u")){
			  pr=0.68
			  CVSSString += "/PR:L"
		  }else if(check("pr_h") && !check("s_u")){
			  pr=0.5
			  CVSSString += "/PR:H"
		  }else {
			  pr=scores["pr_high"]
			  CVSSString += "/PR:H"
		  }
		  
		  if(check("ui_n")){
			  ui=scores["ui_none"]
			  CVSSString += "/UI:N"
		  }else {
			  ui=scores["ui_required"]
			  CVSSString += "/UI:R"
		  }
		 
		  if(check("s_u")){
			  CVSSString += "/S:U"
		  }else{
			  CVSSString += "/S:C"
		  }
		  
		  
		  if(check("c_n")){
			  c=scores["none"]
			  CVSSString += "/C:N"
		  }else if(check("c_l")){
			  c=scores["low"]
			  CVSSString += "/C:L"
		  }else {
			  c=scores["high"]
			  CVSSString += "/C:H"
		  }
		  
		  if(check("i_n")){
			  i=scores["none"]
			  CVSSString += "/I:N"
		  }else if(check("i_l")){
			  i=scores["low"]
			  CVSSString += "/I:L"
		  }else {
			  i=scores["high"]
			  CVSSString += "/I:H"
		  }
		  
		  if(check("a_n")){
			  a=scores["none"]
			  CVSSString += "/A:N"
		  }else if(check("a_l")){
			  a=scores["low"]
			  CVSSString += "/A:L"
		  }else {
			  a=scores["high"]
			  CVSSString += "/A:H"
		  }
		  
		  let iss = 1 - ( (1 - c) * (1 - i) * (1 - a)  )
		  let impact = 0.0
		  if(check("s_u")){
			  impact = iss * 6.42
		  }else if (iss == 0){
			  return { "score": 0.0, "string": CVSSString};
		  }else{
			  let p1 = (7.52 * (iss - 0.029));
			  let p2 = (iss - 0.02) ** 15;
			  let p3 = 3.25 * p2
			  impact = p1 - p3;
		  }
		  
		  if(impact == 0){
			  return { "score": 0.0, "string": CVSSString};
		  }
		  
		  let exploitability = 8.22 * av * ac * pr * ui;
		  let score=0;
		  
		  if(check("s_u")){
			 score = impact + exploitability; 
		  }else{
			  score = 1.08 * (impact + exploitability)
		  }
		  
		  return { "score": score, "string": CVSSString}
		  
	  }
	  function roundup(score){
		  let rounded = Math.round(score *10) / 10;
		  if(score > rounded){
			  rounded = Math.min(Math.round( (rounded + 0.1) *10 )/ 10, 10);
		  }else{
			  rounded = Math.min(rounded,10)
		  }
		  return rounded.toFixed(1)
	  }
	  
	  function updateColors(severity){
		  let score =$("#score")
		  Object.keys(limits).forEach( (a,b) => {
			  $("#score").removeClass(a);
			  $("#severity").removeClass(a);
		  });
		  $("#score").addClass(severity);
		  $("#severity").addClass(severity);
		  
	  }
	  $("input").on("click", function(event){
		 let el = this;
		 let name = el.name;
		 $("input[name=" + name +"]").each( (_index, e) => {
			 let p = $(e).parent()[0];
			 $(p).removeClass("active");
		 });
	  	 $($(el).parent()[0]).addClass("active");
	  	 let scoreObj = calcScore();
	  	 let score = roundup(scoreObj['score']);
	  	 let scoreString = scoreObj['string'];
	  	 console.log(scoreString);
	  	 let severity = getSeverity(score);
	  	 
	  	 $("#score").html(score);
	  	 $("#severity").html(severity);
	  	 
	  	 updateColors(severity);
	  	 
		});
	  
  });
		</script>
		</body>
		</html>