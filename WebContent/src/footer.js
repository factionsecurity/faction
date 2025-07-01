
//import 'jquery';
//import 'jquery-ui';
//import 'bootstrap';
import 'jquery-confirm';
var _token = $("#_token")[0].value;
global._token = _token;
global.showLoading = function showLoading(com) {
	$(com).loading({ overlay: true, base: 0.3 });
};
global.clearLoading = function clearLoading(com) {
	if ($(com).hasClass('js-loading'))
		$(com).loading({ destroy: true });
};
global.alertRedirect = function alertRedirect(resp) {
	if (typeof resp.message == "undefined") window.location = window.location;
	else
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small'
			}
		);

	global._token = resp.token;

};
global.getData = function getData(resp) {
	global._token = resp.token;
	if (typeof resp.message == "undefined") return resp.data;
	else {
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small'
			}
		);
		return "error";
	}


};

global.alertMessage = function alertMessage(resp, success) {
	if (typeof resp.message == "undefined") {
		$.alert(
			{
				title: "SUCCESS!",
				type: "green",
				content: success,
				columnClass: 'small',
				autoClose: 'OK|2000',
				backgroundDismiss: 'OK',
				buttons: {
					OK: ()=>{}
				}
			}
		);
		return true;
	} else {
		$.alert(
			{
				title: "Error",
				type: "red",
				content: resp.message,
				columnClass: 'small',
				autoClose: 'OK|2000',
				backgroundDismiss: 'OK',
				buttons: {
					OK: ()=>{}
				}
			}
		);
		return false;
	}

	//global._token = resp.token;
};


setInterval(function() {
	updateNotificaitons();
}, 60000);

function updateNotificaitons() {
	$.get('../services/getAssessments', function(json) {
		//json = JSON.parse(data);
		if (json.count != 0)
			$(".assessmentCount").each(function(a, b) { $(b).html(json.count); });
		if (json.prcount != 0)
			$(".prCount").each(function(a, b) { $(b).html(json.prcount); });
		var assessments = json.assessments;
		var innerData = "<li class='header'>You have " + json.count + " assessments</li>\n";
		innerData += " <li>\n";
		innerData += " <ul class='menu'>\n";
		for (var i = 0; i < json.count; i++) {
			innerData += "<li>\n";
			innerData += "<a href='SetAssessment?id=" + assessments[i][4] + "'>\n";
			innerData += "<div class='clipped' >&nbsp;</div><small><i class='fa fa-clock-o'></i> " + assessments[i][2] + "</small>";
			innerData += "<h4>\n";
			innerData += assessments[i][1] + " - " + assessments[i][0];
			innerData += "</h4>";
			innerData += "\n";
			innerData += "</a>\n";
			innerData += "</li>\n";
		}
		innerData += "</ul></li>";
		$("#assessmentWidget").html(innerData);

	});
	$.get('../services/getVerifications', function(json) {
		//json = JSON.parse(data);
		if (json.count != 0)
			$(".verificationCount").each(function(a, b) { $(b).html(json.count); });
		var verifications = json.verifications;
		var innerData = "<li class='header'>You have " + json.count + " Verifications</li>\n";
		innerData += " <li>\n";
		innerData += " <ul class='menu'>\n";


		for (var i = 0; i < json.count; i++) {
			innerData += "<li>\n";
			innerData += "<a href='Verifications?id=" + verifications[i][3] + "'>\n";
			innerData += "<h4>\n";
			innerData += "<div class='clipped' >" + verifications[i][4] + "</div><small><i class='fa fa-clock-o'></i> " + verifications[i][2] + "</small>";
			innerData += "</h4>";
			innerData += "<p>" + verifications[i][1] + " - " + verifications[i][0] + "</p>";
			innerData += "\n";
			innerData += "</a>\n";
			innerData += "</li>\n";
		}
		innerData += "</ul></li>";
		$("#verificationWidget").html(innerData);


	});

}
$(function() {

	function checkVersion() {
		let versionId = $("#versionName")[0].innerHTML
		versionId = versionId.replace("Version ", "");
		fetch(`https://api.factionsecurity.com/api/version?version=${versionId}&ts=${new Date().getTime()}`)
			.then(resp => resp.json())
			.then(json => {
				if (json.update) {
					$("#newVersion").html(" : New Version Available. <a href='https://github.com/factionsecurity/faction/releases'>Click Here to Download</a>");
					$(".version").css("color", "red");
				}

			});
	}
	checkVersion();

	updateNotificaitons();
	$("#Profile").click(() => {
		$.confirm({
			content: 'url:Profile',
			title: 'Update Your Profile',
			theme: "black",
			columnClass: 'col-md-8 col-md-offset-2',
			buttons: {
				save: function() {
					var data = "action=update";
					data += "&fname=" + encodeURIComponent($("#profile_fname").val());
					data += "&lname=" + encodeURIComponent($("#profile_lname").val());
					data += "&email=" + encodeURIComponent($("#profile_email").val());
					data += "&current=" + $("#profile_password").val();
					data += "&password=" + $("#profile_newpassword").val();
					data += "&confirm=" + $("#profile_confirm").val();
					$.post("Profile", data).done(function(resp) {
						var title = "Success!";
						var content = "Your profile was updated.";
						if (resp.message != null) {
							title = "Error!";
							content = resp.message;
						}
						$.alert({
							title: title,
							content: content
						});

					}).error(function() {
						$.alert({
							title: 'Error!',
							content: "There is a problem with your request."
						});
					});
				},
				cancel: function() { }
			}
		});

	});
});

global.b64DecodeUnicode = function b64DecodeUnicode(str) {
	str = decodeURIComponent(str);
	return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
		return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
	}).join(''));
};
global.b64EncodeUnicode= function b64EncodeUnicode(str) {
  return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
    (match, p1) => String.fromCharCode('0x' + p1)
  ));
}
global.entityDecode = function entityDecode(encoded){
	let textArea = document.createElement("textarea");
	textArea.innerHTML = encoded;
	return textArea.innerText;
	
}

global.entityEncode = function entityEncode(data){
	let textArea = document.createElement("textarea");
	textArea.innerText = data;
	return textArea.innerHTML
	
}
global.imageToURL = function imageToURL(imageFile) {
 return new Promise((resolve, reject) => {
   const reader = new FileReader();

   reader.onload = (event) => {
	 resolve(event.target.result);
   };

   reader.onerror = (error) => {
	 reject(error);
   };

   reader.readAsDataURL(imageFile);
 });
}


