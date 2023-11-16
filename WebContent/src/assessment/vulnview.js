require('suneditor/dist/css/suneditor.min.css');
require('jquery-fileinput/fileinput.css');
require('./overview.css');
require('../loading/css/jquery-loading.css');
import suneditor from 'suneditor';
import plugins from 'suneditor/src/plugins';
import CodeMirror from 'codemirror';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/lib/codemirror.css';
import '../loading/js/jquery-loading';
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-fileinput';
import 'jquery-ui';
import 'jquery-confirm';
import 'select2';
import '../scripts/jquery.autocomplete.min';
import { marked } from 'marked';


class VulnerablilityView {
	
	constructor(assessmentId){
		this.vulnId=-1;
		this.editors = {};
		this._token = $("#_token")[0].value;
		this.assesssmentId = assessmentId //$("#assessmentId")[0].value;
		this.editorOptions = {
			codeMirror: CodeMirror,
			plugins: plugins,
			buttonList: [
				['undo', 'redo','fontSize', 'formatBlock','textStyle'],
				['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
				['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
				['link', 'image', 'fullScreen', 'showBlocks','codeView'],

			],
			defaultStyle: 'font-family: arial; font-size: 18px',
			minHeight: 500,
			height: 'auto'
		};
		this.editorTimeout = {};
		this.clearLockTimeout = {};
		$("#overall").select2()
		$("#likelyhood").select2()
		$("#impact").select2()
		
		this.vulnTable = $('#vulntable').DataTable({
			"paging": false,
			"lengthChange": false,
			"searching": false,
			"ordering": true,
			"info": false,
			"autoWidth": true,
			"order": [[1, "desc"]],
			"columns": [
				{width: "10px"}, //checkbox
				null, //name
				{width: "10px"} //controls
				
			]
		});
		this.setUpEventHandlers()
		this.updateColors();
		this.editors.description = suneditor.create("description", this.editorOptions);
		this.editors.recommendation = suneditor.create("recommendation", this.editorOptions);
		this.editors.details = suneditor.create("details", this.editorOptions);
		this.setUpVulnAutoComplete()
		
		
	}
	
	setUpEventHandlers(){
		let _this = this;
		$("#overall").on('change', (event) =>{
			const sev = event.target.value;
			$("#likelyhood").val(sev).trigger("change");
			$("#impact").val(sev).trigger("change");
			
		});
		
		$("#vulntable tr").on('click', function(event){
			_this.vulnId = $(this).data("vulnid");
			$(".selected").each( (_a,s) => $(s).removeClass("selected"))
			$(this).addClass("selected");
			_this.getVuln(_this.vulnId);
			
			
		});
		$("#vulntable span[id^=deleteVuln]").each((_index, element) => {
			$(element).on('click', event => {
				const vulnId = parseInt(event.currentTarget.id.replace("deleteVuln", ""));
				_this.deleteVuln(event.currentTarget, vulnId);
			});
		});
		$("#deleteMulti").on("click", () => {
			_this.deleteMulti();
		});
		$("#reasign").on("click", () => {
			_this.reasign();
		});
		this.setUpAddVulnBtn();
		
	}
	
	updateIntVal(element, elementName) {
		var rank = $(element).html();
		$("#" + elementName).val(rank);
		$("#" + elementName).attr("intVal", getIdFromValue(rank));
	};

	setIntVal(value, el) {
		$("#" + el).val(value).trigger("change");
	};
	getEditorText(name) {
		const html = this.editors[name].getContents();
		return Array.from($(html)).filter( a => a.innerHTML != "<br>").map( a => a.outerHTML).join("")
	}
	updateColors() {
		const colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
		const boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
		const width = 100 / boxCount;
		$("#infobar").find("div.row").find("[class^=col-sm]").css("width", width + "%").css("min-width", "100px");
		const boxes = $("#infobar").find("[class=small-box]");
		let colorCount = 9;
		boxes.each((index, box) => {
			const risk = $(box).find("p")[0].innerText;
			$(`.severity:contains('${risk}')`).css("color", colors[colorCount]).css("font-weight", "bold");
			$(box).css("border-color", colors[colorCount]);
			//$(`.sev${risk}`).css("border-left", `5px solid ${colors[colorCount]}`)
			$(`.sev${risk}`).css("border-left-color", `${colors[colorCount]}`)
			$(box).css("color", colors[colorCount--]);
		});
	}
	reasign() {
		let _this = this;
		const checkboxes = Array.from($("input[id^='ckl']")).filter(cb =>
			$(cb).is(':checked')
		);
		const movedid = $("#re_asmtid").val();
		const movedName = $("#re_asmtid option[value='" + movedid + "']").text();
		$.confirm({
			type: "red",
			title: "Are you sure?",
			content: "You will be moving " + checkboxes.length + " vulnerabilities to <br> <b>" + movedName + "</b>",
			buttons: {
				'yes, reassign': function() {
					const rows = ["id=" + movedid];
					$("#stepstable.table").dataTable().fnClearTable();
					checkboxes.forEach((element, index) => {
						const row = $(element).parents("tr");
						const id = $(element).attr("id").replace("ckl", "");
						rows.push("vulns[" + index + "]=" + id);
						_this.vulnTable.row(row).remove().draw();
					});
					rows.push("_token=" + _this._token);
					$.post('reassignVulns', rows.join("&")).done(function(resp) {
						_this.alertMessage(resp, 'Vulns were successfully reassigned.');

					});


				},
				"no": function() { }
			}
		});
	}
	saveChanges(type, isEditor=true) {
		let _this = this;	
		let edits = "";
		if(isEditor){
			edits = this.getEditorText(type);
		}else{
			edits = $(`#${type}`).val();
		}
		let data = "vulnid=" + this.vulnId;
		data += `&${type}=${encodeURIComponent(edits)}`;
		let fields = [];
		for (let vulnId of vulnTypes) {
			const value = $(`#type${vulnId}`).val();
			fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
		}
		data += '&cf=[' + fields.join(",") + "]";
		data += "&_token=" + _this._token;
		$.post("updateVulnerability", data, function(resp) {
			document.getElementById(`${type}_header`).innerHTML=""
			if(resp.result != "success"){
				$.alert(resp.message);
			}
			_this._token = resp.token;
			clearTimeout(_this.clearLockTimeout[type]);
			_this.clearLockTimeout[type] = setTimeout(() => {
			$.get(`ClearLock?action=${type}`).done();
			}, 5000);
			console.log("check for errors");
		});

	}
	queueSave(type, isEditor=true) {
		let _this = this;
		$.get(`SetLock?action=${type}`).done( (resp) => {
			if(resp.result == "success"){
				document.getElementById(`${type}_header`).innerHTML="*"
				clearTimeout(_this.editorTimeout[type]);
				clearTimeout(_this.clearLockTimeout[type]);
				_this.editorTimeout[type] = setTimeout(() => {
					_this.saveChanges(type, isEditor);
				}, 2000);
			}
		});

	}
	
	getOnGoingAssessmentsToReassign(){
		$.get("onGoingAssessments").done(function(resp) {

			$(resp).each(function(a, b) {
				const id = b.id;
				const name = b.name;
				const appid = b.appid;
				const type = b.type;
				$("#re_asmtid").append("<option value=" + id + " >Move Vulns to <b>" + appid + " " + name + " [" + type + "]</b></option>");
			});
			$('#re_asmtid').select2().select2('val', $('.select2 option:eq(0)').val());
		});
		
	}
	
	setUpVulnAutoComplete(){
		let _this=this;
		$("#dtitle").autoComplete({
			minChars: 3,
			source: function(term, response) {
				$.getJSON('DefaultVulns?action=json&terms=' + term,
					function(data) {
						const vulns = data.vulns;
						let list = [];
						for (let i = 0; i < vulns.length; i++) {
							list[i] = vulns[i].vulnId + " :: " + vulns[i].name + " :: " + vulns[i].category;
						}
						response(list);
					}
				);
			},
			onSelect: function(e, term, item) {
				const d = _this.getEditorText("description");
				const r = _this.getEditorText("recommendation");
				const splits = term.split(" :: ");
				$("#dtitle").val(splits[1]);
				if ($("#title").val() == "")
					$("#title").val(splits[1]);
				$("#dcategory").val(splits[2]);
				const vulnid = splits[0];
				$("#dtitle").attr("intVal", vulnid);
				d = d.replace("<p><br></p>", "");
				r = r.replace("<p><br></p>", "");

				if ((r + d).trim() != "") {

					$.confirm({
						title: 'Are You Sure',
						type: 'orange',
						content: "Do you Want to OverWrite the Recommendation and Description with the default text for this vulnerablity.",
						buttons: {
							"yes": function() {
								$.get('DefaultVulns?action=getvuln&vulnId=' + vulnid)
									.done(function(data) {
										_this.editors.description.setContents(marked.parse(_this.b64DecodeUnicode(data.desc)));
										_this.editors.recommendation.setContents(marked.parse(_this.b64DecodeUnicode(data.rec)));
										_this.setIntVal(data.likelyhood, 'likelyhood');
										_this.setIntVal(data.impact, 'impact');
										_this.setIntVal(data.overall, 'overall');
										$(data.cf).each(function(a, b) {
											$("#type" + b.typeid).val(b.value);
										});
									});

							},
							"cancel": function() { }
						}
					});

				} else {
					$.get('DefaultVulns?action=getvuln&vulnId=' + vulnid)
						.done(function(data) {
							_this.editors.description.setContents(marked.parse(_this.b64DecodeUnicode(data.desc)).replace(/\n/g, " "));
							_this.editors.recommendation.setContents(marked.parse(_this.b64DecodeUnicode(data.rec)).replace(/\n/g, " "));
							_this.setIntVal(data.likelyhood, 'likelyhood');
							_this.setIntVal(data.impact, 'impact');
							_this.setIntVal(data.overall, 'overall');
							$(data.cf).each(function(a, b) {
								$("#type" + b.typeid).val(b.value);
							});
						});
				}
			}
		});
		
	}
	
	setUpAddVulnBtn(){
		let _this=this;
		$("#addVuln").click(function() {

			$("#dtitle").click(function() { $("#dtitle").val(""); $("#dcategory").val(""); });
			const desc = _this.getEditorText("description");
			const rec = _this.getEditorText("recommendation");
			const data = "id=" + assesssmentId;
			data += "&description=" + encodeURIComponent(desc);
			data += "&recommendation=" + encodeURIComponent(rec);
			data += "&title=";
			data += "&impact="
			data += "&likelyhood="
			data += "&overall="
			data += "&category="
			data += "&defaultTitle="
			data += "&feedMsg="
			let fields = [];
			for (let vulnId of vulnTypes) {
				let value = $(`#type${vulnId}`).val();
				fields.push(`{"typeid" : ${vulnId}, "value" : "${value}"}`);
			}
			data += '&cf=[' + fields.join(",") + "]";
			data += "&add2feed=false"
			data += "&action=add";
			data += "&_token=" + _this._token;
			$.post("AddVulnerability", data, function(resp) {

				const respData = getData(resp);
				if (respData != "error") {
					_this.deleteVulnForm();
					$("#vulnForm").removeClass("disabled");
					//$("#vulntable").append(respData[0]);
					const row = _this.vulnTable.row.add($(respData[0])).draw().node()
					const vulnId = resp.vulnId
					$(row).on('click', event => {
						_this.getVuln(vulnId);
					});
					_this.updateColors();
				} else if (text.Text == "WO-nnnn Name") {
					text.Text = "Maintenance";
				}

			});

		});
	}
	deleteMulti() {
		const checkboxes = Array.from($("input[id^='ckl']")).filter(cb =>
			$(cb).is(':checked')
		);

		$.confirm({
			type: "red",
			title: "Are you sure?",
			content: "Do you want to delete all " + checkboxes.length + " vulnerabilities?",
			buttons: {
				'yes, delete': function() {
					let rows = [];
					$("#stepstable.table").dataTable().fnClearTable();

					const checkboxes = $("input[id^='ckl']");
					Array.from(checkboxes).filter(cb =>
						$(cb).is(':checked')
					).forEach((element, index) => {
						const row = $(element).parents("tr");
						const id = $(element).attr("id").replace("ckl", "");
						rows.push("vulns[" + index + "]=" + id);
						_this.vulnTable.row(row).remove().draw();
					});

					rows.push("_token=" + _this._token);
					$.post('DeleteVulns', rows.join("&")).done(function(resp) {
						_this._token = resp.token;
						_this.alertMessage(resp, "Vulnerabilties Deleted Successfully");
					});

				},
				"no": function() { }
			}
		});
	}
	alertMessage(resp, success) {
		if (typeof resp.message == "undefined")
			$.alert(
				{
					title: "SUCCESS!",
					type: "green",
					content: success,
					columnClass: 'small'
				}
			);
		else
			$.alert(
				{
					title: "Error",
					type: "red",
					content: resp.message,
					columnClass: 'small'
				}
			);

		this._token = resp.token;
	}
	
	deleteVulnForm() {
		this.editors.description.setContents("");
		this.editors.recommendation.setContents("");
		$("#title").val("");
		$("#impact").attr("intVal", "-1");
		$("#impact").val("").trigger("change");
		$("#likelyhood").val("").trigger("change");
		$("#overall").val("").trigger("change");
		$("#category").val("");
		$("#dtitle").attr("intVal", "-1");
		$("#dtitle").val("");
		$("#dcategory").attr("intVal", "-1");
		$("#dcategory").val("");
	}
	disableAutoSave(){
		
		this.editors.recommendation.onChange = function(){};
		this.editors.recommendation.onChange = function(){};
		this.editors.description.onChange = function(){};
		this.editors.description.onInput = function(){};
		this.editors.details.onChange = function(){};
		this.editors.details.onInput = function(){};
		$("#title").unbind('input');
		$("#overall").unbind('input');
		$("#impact").unbind('input');
		$("#likelyhood").unbind('input');
		$("#dcategory").unbind('change');
	}

	enableAutoSave(){
		let _this = this;
		this.editors.description.onInput = function(contents, core){
			_this.queueSave("description");
		}
		this.editors.description.onChange = function(contents, core){
			if(contents.endsWith("</div>")){
				_this.editors.description.setContents(contents + "<p><br></p>");
			}
			_this.queueSave("description");
		}
		this.editors.recommendation.onInput = function(contents, core){
			_this.queueSave("recommendation");
		}
		this.editors.recommendation.onChange = function(contents, core){
			if(contents.endsWith("</div>")){
				_this.editors.remediation.setContents(contents + "<p><br></p>");
			}
			_this.queueSave("recommendation");
		}
		this.editors.details.onInput = function(contents, core){
			_this.queueSave("details");
		}
		this.editors.details.onChange = function(contents, core){
			if(contents.endsWith("</div>")){
				_this.editors.details.setContents(contents + "<p><br></p>");
			}
			_this.queueSave("details");
		}
		$("#title").on('input', function(event){
			$(".selected").find(".vulnName")[0].innerHTML=$(this).val()
			_this.queueSave("title", false);
		});
		$("#overall").on('input', function(event){
			const severity = $(this).select2('data')[0].text
			$(".selected").find(".severity")[0].innerHTML=severity
			$(".selected").children()[0].className=`sev${severity}`
			$($(".selected").children()[1]).attr('data-sort', $(this).val())
			_this.vulnTable.row($(".selected")).invalidate()
			_this.vulnTable.order( [ 1, 'desc' ] ).draw();
			_this.updateColors()
			
			_this.queueSave("overall", false);
		});
		$("#impact").on('input', function(event){
			_this.queueSave("impact", false);
		});
		$("#likelyhood").on('input', function(event){
			_this.queueSave("likelyhood", false);
		});
		$("#dcategory").on('change', function(event){
			_this.queueSave("dcategory", false);
		});
		
	}
	setEditorContents(type, data){
		let decoded = this.b64DecodeUnicode(data);
		if(decoded.endsWith("</div>")){
			decoded = decoded + "<p><br></p>";
		}
		this.editors[type].setContents(decoded);
	}
	getVuln(id) {
		this.vulnid=id;
		this.disableAutoSave()
		let _this = this;
		$("#vulnForm").removeClass("disabled");
		$.get('AddVulnerability?vulnid=' + id + '&action=get').done(function(data) {

			$("#title").val($("<div/>").html(data.name).text());
			$("#dtitle").val($("<div/>").html(data.dfname).text());
			$("#dtitle").attr("intVal", data.dfvulnid);
			$("#dcategory").val($("<div/>").html(data.dfcat).text());
			$("#dcategory").attr("intVal", data.dfcatid);
			_this.setEditorContents("description", data.description);
			_this.setEditorContents("recommendation", data.recommendation);
			_this.setEditorContents("details", data.details);
			_this.setIntVal(data.likelyhood, 'likelyhood');
			_this.setIntVal(data.impact, 'impact');
			_this.setIntVal(data.overall, 'overall');
			$(data.cf).each(function(a, b) {
				$("#type" + b.typeid).val(b.value);
			});
			_this.enableAutoSave()
		});

	}
	deleteVuln(el, id) {
		let _this=this;
		const row = $(el).parents("tr");

		$.confirm({
			type: "red",
			title: "Are you sure?",
			content: "Do you want to delete " + _this.vulnTable.row(row).data()[3],
			buttons: {
				"yes, delete it": function() {
					let data = 'vulnid=' + id + '&action=delete';
					data += "&_token=" + _this._token;
					console.log(data);
					$.post('AddVulnerability', data).done(function(resp) {
						const isError = getData(resp);
						if (isError != "error") {
							_this.vulnTable.row(row).remove().draw();
						}
					});
				},
				cancel: function() { return 1; }
			}
		});


	}

	getData(resp) {
		this._token = resp.token;
		if (typeof resp.message == "undefined")
			return resp.data;
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
	}

	b64DecodeUnicode(str) {
		str = decodeURIComponent(str);
		return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
			return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
		}).join(''));
	}
	
}

$(function(){
	global.vulnView = new VulnerablilityView($("#assessmentId")[0].value);	
})








