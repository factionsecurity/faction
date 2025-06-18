
require('select2/dist/css/select2.min.css')
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import 'select2';

global._token = $("#_token")[0].value;
var selectedUser = "";

global.unlock = function unlock(id) {
	$.confirm({
		title: "Are you sure?",
		content: "Do you want to unlock this account?",
		buttons: {
			"Yes": function() {
				let data = "userId=" + id;
				data += "&_token=" + _token;
				$.post("Unlock", data).done(function(resp) {
					alertRedirect(resp);
				});
			},
			"No": function() { return; }
		}
	});

}
global.delTeam = function delTeam(id) {
	let data = "team_id=" + id;
	data += "&_token=" + _token;
	$.post('DeleteTeamName', data).done(function(resp) {
		alertRedirect(resp);
	});

}
global.editTeam = function editTeam(el, id) {
	$('#teamModal').modal('show');
	let title = $(el).parent().siblings(":first").text();
	$('#team_name').val(title);
	$('#addTeam').off("click");
	$('#addTeam').on('click', function() {
		let data = "team_id=" + id;
		data += "&team=" + $('#team_name').val();
		data += "&_token=" + _token;
		$.post('UpdateTeamName', data).done(function(resp) {
			alertRedirect(resp);
		});
	});

}
global.del = function del(id, name) {
	selectedUser = id;
	let ddata = "userId=" + selectedUser;
	ddata += "&_token=" + _token;
	$.confirm({
		title: "Confirm Delete",
		content: `Are you Sure you want to delete <b>${name}</b>?`,
		buttons: {
			delete: ()=> {
				$.post("DeleteUser", ddata).done(function(resp) {
					alertRedirect(resp);

				});
				
			},
			cancel: ()=>{}
		}
		
	})

}
global.edit = function edit(id) {
	selectedUser = id;
	$('#userModal').modal({ backdrop: 'static',
	  keyboard: false});
	let pdata = "userId=" + selectedUser;
	pdata += "&_token=" + _token;
	$.post("GetUser", pdata, function(data) {
		console.log(data)
		$("#uname").val(data.username);
		if(data.authMethod != "Native"){
			$("#fname").attr("disabled", "true");
			$("#lname").attr("disabled", "true");
			$("#email").attr("disabled", "true");
		}
		$("#fname").val(data.fname);
		$("#lname").val(data.lname);
		$("#email").val(decodeURIComponent(data.email));
		$("#authMethod").val(data.authMethod).trigger("change");
		$("#teamName").val(data.team);
		if (typeof data.accesscontrol != 'undefined') {
			$('input:radio[name=accesslevel]')[2 - data.accesscontrol].checked = true;
			$($('input:radio[name=accesslevel]')[2 - data.accesscontrol]).parent().addClass("checked");
		}
		else {
			$('input:radio[name=accesslevel]')[2].checked = true;
			$($('input:radio[name=accesslevel]')[2]).parent().addClass("checked");
		}
		if (data.team == "") {
			console.log("really")
			$("#teamName").val('-1').trigger('change');
		} else {
			console.log(data.team)
			$("#teamName").val(data.team).trigger('change');
		}

		if (data.admin) {
			$("#adminck").attr("checked", "");
			$("#adminck").parent().addClass("checked");
		} else {
			$("#adminck").removeAttr("checked");
			$("#adminck").parent().removeClass("checked");
		}

		if (data.exec) {
			$("#execk").attr("checked", "");
			$("#execk").parent().addClass("checked");
		} else {
			$("#execk").removeAttr("checked");
			$("#execk").parent().removeClass("checked");
		}
		if (data.mgr) {
			$("#mgrck").attr("checked", "");
			$("#mgrck").parent().addClass("checked");
		} else {
			$("#mgrck").removeAttr("checked");
			$("#mgrck").parent().removeClass("checked");
		}
		if (data.inactive) {
			$("#activeck").attr("checked", "");
			$("#activeck").parent().addClass("checked");
		} else {
			$("#activeck").removeAttr("checked");
			$("#activeck").parent().removeClass("checked");
		}
		if (data.eng) {
			$("#engck").attr("checked", "");
			$("#engck").parent().addClass("checked");
		} else {
			$("#engck").removeAttr("checked");
			$("#engck").parent().removeClass("checked");
		}
		if (data.rem) {
			$("#remck").attr("checked", "");
			$("#remck").parent().addClass("checked");
		} else {
			$("#remck").removeAttr("checked");
			$("#remck").parent().removeClass("checked");
		}
		if (data.assessor) {
			$("#assck").attr("checked", "");
			$("#assck").parent().addClass("checked");
		} else {
			$("#assck").removeAttr("checked");
			$("#assck").parent().removeClass("checked");
		}
		if (data.apikey != "") {
			$("#apick").attr("checked", "");
			$("#apick").parent().addClass("checked");
			$("#api").val(data.apikey);
		} else {
			$("#apick").removeAttr("checked");
			$("#apick").parent().removeClass("checked");
			$("#api").val("");
		}
		$("#saveUser").off("click");
		$("#saveUser").on('click', function() {
			pdata = permissionString();
			pdata += "&userId=" + selectedUser;
			pdata += "&username=" +  $("#uname").val();
			pdata += "&fname=" + $("#fname").val();
			pdata += "&lname=" + $("#lname").val();
			pdata += "&email=" + encodeURIComponent($("#email").val());
			pdata += "&team=" + $("#teamName").val();
			pdata += "&credential=" + $("#credential").val();
			pdata += "&authMethod=" + $("#authMethod").val();
			pdata += "&accesscontrol=" + $('input:radio[name=accesslevel]:checked').val();
			pdata += "&update=true";
			pdata += "&_token=" + _token;
			$.post("UpdateUser", pdata, function(data) {
				global._token = data.token;
				if (data.result == "success") {
					$('#userModal').modal('hide');
					alertRedirect(data);
				} else {
					$.confirm({
						title: "Error",
						content: data.message,
						buttons: {
							"OK": function() { return; }
						}
					});
				}
			});
		});
	}).fail(function(data) { console.log(data); });

}

$(function() {
	
	$("#authMethod").on('select2:select', function(){
		console.log("auth change");
		let method = this.value;
		if(method == "LDAP"){
			$("#fname").prop('disabled', true);
			$("#fname").css('background-color', "#030D1C")
			$("#lname").prop('disabled', true);
			$("#lname").css('background-color', "#030D1C")
			$("#email").prop('disabled', true);
			$("#email").css('background-color', "#030D1C")
			$("#credential").prop('disabled', true);
			$("#credential").css('background-color', "#030D1C")
			
		}else{
			$("#fname").prop('disabled', false);
			$("#fname").css('background-color', "#192338")
			$("#lname").prop('disabled', false);
			$("#lname").css('background-color', "#192338")
			$("#email").prop('disabled', false);
			$("#email").css('background-color', "#192338")
			$("#credential").prop('disabled', false);
			$("#credential").css('background-color', "#192338")
			
		}
	});

	$("#uioli-btn").on('click', function() {
		let data = "uioli=" + $("#uioli").val();
		data += "&_token=" + _token;
		$.post("UpdateUIOLI", data).done(function(resp) {
			alertMessage(resp, "Parameter Updated");
		});
	});
	$("input:checkbox").on('click', function() {
		if ($(this).is(':checked')) {
			$(this).parent().addClass("checked");
		} else {
			$(this).parent().removeClass("checked");
		}
	});
	$(".select2").select2();
	$("#ldapTest").on('click', function() {
		
		ldapSave( (data) => {
			let pdata = "_token=" + global._token;
			$.get("TestLDAP", pdata, function(data) {
				global._token = data.token;
				if (data.result == "success") {
					$.alert("Connect Success");
				} else {
					$.alert("Connect Failed");
				}
			});
			});

	});
	
	function ldapSave(callback){
		let pdata = "ldapURL=" + $("#ldapURL").val();
		pdata += "&ldapBaseDn=" + $("#ldapBaseDn").val();
		pdata += "&ldapUserName=" + $("#ldapUserName").val();
		pdata += "&ldapPassword=" + $("#ldapPassword").val();
		pdata += "&ldapSecurity=" + $("#ldapSecurity").val();
		pdata += "&ldapObjectClass=" + $("#ldapObjectClass").val();
		pdata += "&isInsecure=" + $("#isInsecure").is(':checked');
		pdata += "&_token=" + global._token;
		$.post("SaveLDAP", pdata, function(data) {
			global._token = data.token;
			callback(data)
		});
		
	}
	

	$("#ldapSave").on('click', function() {
		ldapSave( (data) => {
			global._token = data.token;
			if (data.result == "success") {
				$.alert("LDAP Settings Saved");
			} else {
				$.confirm({
					title: "Error",
					content: data.message,
					buttons: {
						"OK": function() { return; }
					}
				});
			}
		});

	});
	
	function oauthSave(callback){
		let pdata = "oauthClientId=" + $("#oauthClientId").val();
		pdata += "&oauthClientSecret=" + $("#oauthClientSecret").val();
		pdata += "&oauthDiscoveryURI=" + $("#oauthDiscoveryURI").val();
		pdata += "&_token=" + global._token;
		$.post("SaveOAUTH", pdata, function(data) {
			global._token = data.token;
			callback(data)
		});
		
	}
	$("#oauthSave").on('click', function() {
		oauthSave( (data) => {
			global._token = data.token;
			if (data.result == "success") {
				$.alert("OAUTH Settings Saved");
			} else {
				$.confirm({
					title: "Error",
					content: data.message,
					buttons: {
						"OK": function() { return; }
					}
				});
			}
		});

	});
	function saml2Save(callback){
		let pdata = "saml2MetaUrl=" + $("#saml2MetaUrl").val();
		pdata += "&_token=" + global._token;
		$.post("SaveSAML2", pdata, function(data) {
			global._token = data.token;
			callback(data)
		});
		
	}
	$("#saml2Save").on('click', function() {
		saml2Save( (data) => {
			global._token = data.token;
			if (data.result == "success") {
				$.alert("SAML2 Settings Saved");
			} else {
				$.confirm({
					title: "Error",
					content: data.message,
					buttons: {
						"OK": function() { return; }
					}
				});
			}
		});

	});
	$("#uname").autoComplete({
		minLength: 2,
		cache: false,
		source: function(term, response) {
			let data = "username=" + term;
			$.post("SearchLDAP", data).done(function(resp) {
				response(resp.users.map((u) => `${u.username} ${u.email} ${u.fname} ${u.lname}`));
			});
		},
		onSelect: function(e, term, item) {
			let userInfo = term.split(" ");
			$("#uname").val(userInfo[0]);
			$("#email").val(userInfo[1]);
			$("#fname").val(userInfo[2]);
			$("#lname").val(userInfo[3]);
			$("#authMethod").val("LDAP").trigger("change");
		}

	});

	$("#addUser").on('click', function() {
		clearModal();
		$("#assck").attr("checked", true);
		$("#assck").parent().addClass("checked");
		$("#uname").removeAttr("disabled");
		$('#userModal').modal({ backdrop: 'static',
		  keyboard: false});
		$('input:radio[name=accesslevel]')[2].checked = true;
		$($('input:radio[name=accesslevel]')[2]).parent().addClass("checked");
		$('#saveUser').off("click");
		$('#saveUser').on('click', function() {
			let pdata = permissionString();
			pdata += "&username=" + $("#uname").val();
			pdata += "&fname=" + $("#fname").val();
			pdata += "&lname=" + $("#lname").val();
			pdata += "&credential=" + $("#credential").val();
			pdata += "&email=" + encodeURIComponent($("#email").val());
			pdata += "&team=" + $("#teamName").val();
			pdata += "&authMethod=" + $("#authMethod").val();
			pdata += "&accesscontrol=" + $('input:radio[name=accesslevel]:checked').val();
			pdata += "&_token=" + global._token;
			$.post("AddUser", pdata, function(data) {
				global._token = data.token;
				if (data.result == "success") {
					$('#userModal').modal('hide');
					alertRedirect(data);
				} else {

					global._token = data.token;
					$.confirm({
						title: "Error",
						content: data.message,
						buttons: {
							"OK": function() { return; }
						}
					});
				}
			});

		});
	});


	$("#createTeam").on('click', function() {
		$('#teamModal').modal('show');
		$('#addTeam').on('click', function() {
			let data = "team_name=" + $('#team_name').val();
			data += "&_token=" + _token;
			$.post('CreateTeamName', data).done(function(resp) {
				alertRedirect(resp);
			});
		});
	});

	$('#userTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		columnDefs: [
			{ width: "50px", targets: 6 },
			{ width: "70px", targets: 7 }
		],

	});

	$('#teamTable').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		columnDefs: [
			{ width: "70px", targets: 1 }
		],

	});

});
function permissionString() {
	var data = "admin=" + $("#adminck").is(':checked');
	data += "&rem=" + $("#remck").is(':checked');
	data += "&eng=" + $("#engck").is(':checked');
	data += "&inactive=" + $("#activeck").is(':checked');
	data += "&mgr=" + $("#mgrck").is(':checked');
	data += "&exec=" + $("#execk").is(':checked');
	data += "&assessor=" + $("#assck").is(':checked');
	data += "&api=" + $("#apick").is(':checked');
	return data;

}
function clearModal() {
	$("#uname").val("");
	$("#fname").val("");
	$("#lname").val("");
	$("#email").val("");
	$("#api").val("");
	$("#authMethod").val("Native").trigger("change");
	$("#teamName").select2().select2('val', '-1');
	$("input:checkbox").each(function() {
		if(this.id == "isInsecure") return;
		$(this).removeAttr("checked");
		$(this).parent().removeClass("checked");
	});
}
