export default class CVSS31 {

	constructor() {
		this.scores = {
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
		this.limits = {
			"None": [0, 0],
			"Low": [0.1, 3.9],
			"Medium": [4, 6.9],
			"High": [7, 8.9],
			"Critical": [9, 10]
		}
	}
	getSeverity(score) {
		if (score == 0) {
			return "None"
		}
		return Object.keys(limits).reduce((acc, key) => score >= limits[key][0] && score <= limits[key][1] ? key : acc, "None")
	}
	check(id) {
		return $("#" + id).is(":checked");
	}
	calcScoreFromString(cvssString, errorCallback, successCallback){
		let cvssLower = cvssString.lower();
		if(cvssLower.indexOf("cvss:3.1")){
			let params = cvssLower.split("/");
			let cvssMap = params.reduce( (acc,param) => {
				let attr = param.split(":");
				acc[attr[0]] = attr[1];	
			}, {});
			successCallBack(
				calcScore(cvssMap)	
				);
		}else{
			errorCallback("Not a valid CVSS 3.1 String");
		}
		
	}
	calcScore(cvssMap){
		let CVSSString = "CVSS:3.1"
		let av = scores["network"];
		let ac = scores["ac_low"];
		let pr = scores["pr_none"];
		let ui = scores["ui_none"];
		let c = scores["none"];
		let i = scores["none"];
		let a = scores["none"];
		if (cvssMap["av"] == "n") {
			av = scores["network"]
			CVSSString += "/AV:N"
		} else if (cvssMap["av"] == "a") {
			av = scores["adjacent"]
			CVSSString += "/AV:A"
		} else if (cvssMap["av"] == "l") {
			av = scores["local"]
			CVSSString += "/AV:L"
		} else {
			av = scores["physical"]
			CVSSString += "/AV:P"
		}
		
		if (cvssMap["ac"] == "l") {
			ac = scores["ac_low"]
			CVSSString += "/AC:L"
		} else {
			ac = scores["ac_high"]
			CVSSString += "/AC:H"
		}

		if (cvssMap["pr"] == "n") {
			pr = scores["pr_none"]
			CVSSString += "/PR:N"
		} else if (cvssMap["pr"] == "l" && cvssMap["s"] == "u") {
			pr = scores["pr_low"]
			CVSSString += "/PR:L"
		} else if (cvssMap["pr"] == "l" && cvssMap["s"] != "u") {
			pr = 0.68
			CVSSString += "/PR:L"
		} else if (cvssMap["pr"] == "h" && cvssMap["s"] != "u") {
			pr = 0.5
			CVSSString += "/PR:H"
		} else {
			pr = scores["pr_high"]
			CVSSString += "/PR:H"
		}

		if (cvssMap["ui"] == "n") {
			ui = scores["ui_none"]
			CVSSString += "/UI:N"
		} else {
			ui = scores["ui_required"]
			CVSSString += "/UI:R"
		}

		if (cvssMap["s"] =="u") {
			CVSSString += "/S:U"
		} else {
			CVSSString += "/S:C"
		}

		if (cvssMap["c"] =="n") {
			c = scores["none"]
			CVSSString += "/C:N"
		} else if (cvssMap["c"] =="l") {
			c = scores["low"]
			CVSSString += "/C:L"
		} else {
			c = scores["high"]
			CVSSString += "/C:H"
		}

		if (cvssMap["i"] =="n") {
			i = scores["none"]
			CVSSString += "/I:N"
		} else if (cvssMap["i"] == "l") {
			i = scores["low"]
			CVSSString += "/I:L"
		} else {
			i = scores["high"]
			CVSSString += "/I:H"
		}

		if (cvssMap["a"] == "n") {
			a = scores["none"]
			CVSSString += "/A:N"
		} else if (cvssMap["a"] == "l") {
			a = scores["low"]
			CVSSString += "/A:L"
		} else {
			a = scores["high"]
			CVSSString += "/A:H"
		}
		
		let iss = 1 - ((1 - c) * (1 - i) * (1 - a))
		let impact = 0.0
		if (cvssMap["s"] == "u") {
			impact = iss * 6.42
		} else if (iss == 0) {
			return { "score": 0.0, "string": CVSSString };
		} else {
			let p1 = (7.52 * (iss - 0.029));
			let p2 = (iss - 0.02) ** 15;
			let p3 = 3.25 * p2
			impact = p1 - p3;
		}

		if (impact == 0) {
			return { "score": 0.0, "string": CVSSString };
		}

		let exploitability = 8.22 * av * ac * pr * ui;
		let score = 0;

		if (cvssMap["s"] == "u") {
			score = impact + exploitability;
		} else {
			score = 1.08 * (impact + exploitability)
		}

		return { "score": score, "string": CVSSString }
		
	}
	
	roundup(score) {
		let rounded = Math.round(score * 10) / 10;
		if (score > rounded) {
			rounded = Math.min(Math.round((rounded + 0.1) * 10) / 10, 10);
		} else {
			rounded = Math.min(rounded, 10)
		}
		return rounded.toFixed(1)
	}

	updateColors(severity) {
		let score = $("#score")
		Object.keys(limits).forEach((a, b) => {
			$("#score").removeClass(a);
			$("#severity").removeClass(a);
		});
		$("#score").addClass(severity);
		$("#severity").addClass(severity);

	}
	/*$("input").on("click", function(event){
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
		   
	  });*/
}