require('jquery-fileinput/fileinput.css');
require('select2/dist/css/select2.min.css')
require('./overview.css');
require('../loading/css/jquery-loading.css');
import Editor from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
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
import CVSS from '../cvss';

class SaveQueue {
	constructor(caller, assessmentId, saveCallback, updateVulnsCallback) {
		this.queue = {};
		this.timer = {};
		this.timeout = 2000;
		this.saveCallback = saveCallback;
		this.caller = caller;
		this.token = caller._token;
		this.locks = new EditLocks(this, assessmentId, updateVulnsCallback);
	}
	push(type, id, key, value) {
		this.locks.setLock(type, id, key);
		let headerKey = key;
		if(type == 'note'){
			headerKey = 'notes'
		}
		if (document.getElementById(`${headerKey}_header`)) {
			document.getElementById(`${headerKey}_header`).innerHTML = "*"
		}
		clearTimeout(this.timer);
		if (type in this.queue && id in this.queue[type]) {
			this.queue[type][id][key] = value;
		}else if (type in this.queue) {
			this.queue[type][id] = {};
			this.queue[type][id][key] = value;
		} else {
			this.queue[type] = {};
			this.queue[type][id] = {};
			this.queue[type][id][key] = value;
		}
		let _this = this;
		this.timer = setTimeout(function() {
			_this.save()
		}, this.timeout);
	}

	save() {
		const types = Object.keys(this.queue);
		let _this = this;
		for (let type of types) {
			const ids = Object.keys(this.queue[type])
			for(let id of ids){
				let data = Object.keys(this.queue[type][id]).map((k) => {
					if(type == 'note'){
						document.getElementById(`notes_header`).innerHTML = ""
					}
					else if (this.caller.vulnId == id && document.getElementById(`${k}_header`)) {
						document.getElementById(`${k}_header`).innerHTML = ""
					}
					setTimeout(function() {
						_this.locks.clearLock(type, id, k);
					}, 5000)
					return `${k}=${this.queue[type][id][k]}`
				})
				if(type == 'vulnerability'){
					data = `vulnid=${id}&` + _this.handleCustom(data).join("&");
				}else if(type == 'note'){
					data = `noteid=${id}&` + data.join("&");
				}
				this.saveCallback(type, data, this.caller);
				delete this.queue[type][id];
			}
		}
	}

	handleCustom(data) {
		let fields = data
			.filter((row) => row.indexOf("type") == 0)
			.map((row) => {
				let tmp = row.replace("type", "");
				let cfId = tmp.split("=")[0];
				let cfValue = tmp.split("=")[1];
				return `{"typeid" : ${cfId}, "value" : "${cfValue}"}`;
			})

		if (fields.length > 0) {
			data.push(`cf=[${fields.join(",")}]`);
		}
		return data;
	}

}

class EditLocks {
	constructor(caller, assessmentId, updateCallback) {
		this.lockIds = {};
		this.caller = caller;
		this.token = caller.token;
		this.assessmentId = assessmentId;
		this.updateCallback = updateCallback;
		this.checkLocks()
		this.errorMessageShown = false;
	}
	setLock(type, id, attr) {
		let _this = this;
		if (typeof this.lockIds[id] == "undefined") {
			this.lockIds[id] = [attr];
			$.get(`${type}/set/lock?id=${id}&attr=${attr}&_token=${this.token}`).done((resp) => {
				if (resp.result == "success") {
				}
			});
		} else {
			if (!this.lockIds[id].some(i => i == attr)) {
				this.lockIds[id].push(attr);
				$.get(`${type}/set/lock?id=${id}&attr=${attr}&_token=${this.token}`).done((resp) => {
					if (resp.result == "success") {
					}
				});
			}

		}
	}
	
	clearLock(type, id, attr) {
		if (this.lockIds[id]) {
			this.lockIds[id] = this.lockIds[id].filter(l => l != attr)
			$.get(`${type}/clear/lock?type=${type}&id=${id}&attr=${attr}&_token=${this.token}`).done();
		} else {
			//console.log("no locks found");
		}
	}
	
	checkLocks() {
		let _this = this;
		setInterval(function() {
			$.get(`vulnerability/check/locks?id=${_this.assessmentId}`).done((resp) => {
				if (resp.result && resp.result == "error") {
					if (!_this.errorMessageShown) {
						_this.errorMessageShown = true
						$.confirm({
							title: resp.message,
							content: 'Do you want to log in?',
							buttons: {
								login: () => {
									_this.errorMessageShown = false;
									window.open("../", '_blank').focus();
								},
								cancel: () => { _this.errorMessageShown = false; }
							}
						});

					}
					return;
				}
				if (resp.token) {
					_this.token = resp.token;
					_this.caller.token = resp.token;
					_this.caller.caller.queue._token = resp.token;
					_this.caller.caller.queue.caller._token = resp.token;
				}
				_this.updateCallback('vulnerability', resp);
			}).catch((e) => {
				if (!_this.errorMessageShown) {
					_this.errorMessageShown = true;
					$.confirm({
						title: "Offline",
						content: 'You appear offline. Would you like to login?',
						buttons: {
							login: () => {
								_this.errorMessageShown = false;
								window.open("../", '_blank').focus();
							},
							cancel: () => { _this.errorMessageShown = false; }
						}
					});
				}
			})
		}, 1000)

	}
}


class VulnerablilityView {

	constructor(assessmentId) {
		this._token = $("#_token")[0].value;
		this.queue = new SaveQueue(this, assessmentId, this.saveChanges, (type, vulns) => { this.updateCallback(type, vulns) });
		this.vulnId = -1;
		this.editors = {};
		this.descUndoCount=0;
		this.initialHTML={};
		this.assesssmentId = assessmentId //$("#assessmentId")[0].value;
		this.editorTimeout = {};
		this.clearLockTimeout = {};
		$(".select2").select2();

		this.vulntable = $('#vulntable').DataTable({
			"paging": false,
			"lengthchange": false,
			"searching": true,
			language: { search: "" },
			"ordering": true,
			"info": false,
			"autowidth": false,
			"order": [[1, "desc"]],
			"columns": [
				{ width: "10px" }, //checkbox
				null, //name
				{ width: "10px" } //controls

			],
			columndefs: [
				{ orderable: false, targets: '_all' }
			]
		});
		$(window).on('resize', () => {
			this.vulntable.columns.adjust();
		});
		this.setUpEventHandlers()
		this.updateColors();
		this.editors.description = new Editor({
				el: document.querySelector('#description'),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false,
				hooks: {
					addImageBlobHook: async (blob,callback, source)=>{
						const encodedImage = await imageToURL(blob)
						let data = "encodedImage=" + encodeURIComponent(encodedImage);
						data += "&assessmentId="+$("#appid")[0].value;
						$.post("UploadImage",data).done(function(resp) {
							let uuid = resp.message;
							callback("getImage?id=" + uuid);
						});
							
					}
				}
			});
			
		this.editors.recommendation =  new Editor({
				el: document.querySelector('#recommendation'),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false,
				hooks: {
					addImageBlobHook: async (blob,callback, source)=>{
						const encodedImage = await imageToURL(blob)
						let data = "encodedImage=" + encodeURIComponent(encodedImage);
						data += "&assessmentId="+$("#appid")[0].value;
						$.post("UploadImage",data).done(function(resp) {
							let uuid = resp.message;
							callback("getImage?id=" + uuid);
						});
							
					}
				}
			});
		this.editors.details = new Editor({
				el: document.querySelector('#details'),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false,
				hooks: {
					addImageBlobHook: async (blob,callback, source)=>{
						const encodedImage = await imageToURL(blob)
						let data = "encodedImage=" + encodeURIComponent(encodedImage);
						data += "&assessmentId="+$("#appid")[0].value;
						$.post("UploadImage",data).done(function(resp) {
							let uuid = resp.message;
							callback("getImage?id=" + uuid);
						});
							
					}
				}
			});
		const initialHTML = entityDecode($("#notes").html());
		this.editors.notes = new Editor({
				el: document.querySelector('#notes'),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false,
				hooks: {
					addImageBlobHook: async (blob,callback, source)=>{
						const encodedImage = await imageToURL(blob)
						let data = "encodedImage=" + encodeURIComponent(encodedImage);
						data += "&assessmentId="+$("#appid")[0].value;
						$.post("UploadImage",data).done(function(resp) {
							let uuid = resp.message;
							callback("getImage?id=" + uuid);
						});
							
					}
				}
			});
		this.editors.notes.hide();
		this.editors.notes.setHTML(initialHTML, false);
		this.editors.notes.show();
		this.setUpNoteChangeEvent();
		this.setUpVulnAutoComplete()
		this.is40 = $("#isCVSS40").val() == "true"
		this.is31 = $("#isCVSS31").val() == "true"
		this.cvss = new CVSS("", this.is40);
		this.cvss.setUpCVSSModal("cvssModal", "cvssString", (resultCVSSString) =>{
			$("#cvssString").val(resultCVSSString).trigger("change")
		});


	}

	catHearder(params, data) {
		if ($.trim(params.term) === '') {
			return data;
		}
		if (data.text.toUpperCase().indexOf(params.term.toUpperCase()) != -1) {
			return data
		}
		return null;
	}

	
	setUpEventHandlers() {
		let _this = this;
		$("#overall").on('change', (event) => {
			const sev = event.target.value;
			$("#likelyhood").val(sev).trigger("change");
			$("#impact").val(sev).trigger("change");

		});
		$("#reportSection").on('change', (event) => {
			$("#reportSection").next().removeClass("field-error");
		});

		$("#vulntable tbody tr").on('click', function(event) {
			_this.vulnId = $(this).data("vulnid");
			$(".selected").each((_a, s) => $(s).removeClass("selected"))
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
		this.setUpCVSSScoreEvents();
		
		//Note Events
		
		$("#createNote").on('click', ()=>{
			let buttons = {
				save: function() {
					const name = $("#newNoteName").val();
					let data = `noteName=${name.trim()}`
					data += "&note=";
					data += "&_token=" + global._token;
					$.post("createNote", data).done(function(resp) {
						_token = resp.token;
						const note = resp;
						if (!Array.from($(`#notebook option`))
							.some((t) => $(t).val() == note.id)) {
							let option = document.createElement("option");
							$(option).addClass("globalTemplate");
							$(option).val(note.id);
							$(option).html(note.name);
							$(`#notebook`).append(option).trigger("change");
							$(option).on("click", async (event)=>{
								await _this.getNoteFromEvent(event)
							})
						}
						alertMessage(resp, "Note Created.");
					});

				},
				cancel: function() { }
			}
			let contentMessage = "Enter a Template name: <input id='newNoteName' class='form-control'></input>";
			$.confirm({
				title: "Create New Note",
				content: contentMessage,
				buttons: buttons
			})
			
		});
		$("#deleteNote").on('click', async (event) => {
			let selected = Array.from($(`#notebook option:selected`));
			if (selected.length == 0) {
				alertMessage({ message: "No Valid Notes to Delete" }, null)
				return;
			}
			const name = $("#noteName").val();
			$.confirm({
				title: "Confirm?",
				content: "Are you sure you want to delete these templates?<br><b>" + name + "</b>",
				buttons: {
					confirm: async function() {
						let messages = []
						for await (const option of selected) {
							await $.post(`deleteNote`, `noteid=${$(option).val()}`).done(function(resp) {
								_token = resp.token;
								messages.push(resp);
								option.remove();
							});
						}
						alertMessage(messages[0], "Note Deleted");
					},
					cancel: function() { }
				}
			});
		});
		
		$("#noteName").on('keyup change', () =>{
			const name = $("#noteName").val();
			const selected = $(`#notebook option:selected`);
			const id = selected.val();
			$(selected).html(name);
			this.queue.push('note',id, 'noteName', name);
			
		})
		$(".globalNote").on("click", async (event)=>{
			this.getNoteFromEvent(event)
		})
		

	}
	setUpNoteChangeEvent(){
		this.editors.notes.on('change', ()=>{
			let noteId = "";
			const selected = $(`#notebook option:selected`);
			noteId = selected.val()
			this.queue.push('note',noteId, 'noteText', encodeURIComponent(this.getEditorText('notes')));
		});
	}
	getNote(id){
		let _this = this;
		$.get('getNote?noteid=' + id)
			.done(function(note) {
				$("#createdBy").html(note.createdBy);
				$("#createdAt").html(note.createdAt);
				$("#updatedBy").html(note.updatedBy);
				$("#updatedAt").html(note.updatedAt);
				_this.setEditorContents("notes", entityDecode(note.note), true);
				$("#noteName").val(note.name);
				_this.setUpNoteChangeEvent();
				$("#notes").removeClass("disabled");
			});
	}
	getNoteFromEvent(event) {
		this.editors.notes.off('change');
		let value = event.target.value;
		this.getNote(value);
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
		const html = this.editors[name].getHTML();
		return Array.from($(html)).filter(a => a.innerHTML != "<br>").map(a => a.outerHTML).join("")
	}
	updateColors() {
		const colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
		const boxCount = $("#infobar").find("div.row").find("[class^=col-sm]").length;
		const width = 100 / boxCount;
		$("#infobar").find("div.row").find("[class^=col-sm]").css("width", width + "%").css("min-width", "100px");
		const boxes = $("#infobar").find("[class=small-box]");
		let colorCount = 9;
		boxes.each((index, box) => {
			this.updateSeverityCount(box, boxCount - 1 - index);
			const risk = $(box).find("p")[0].innerText;
			$(`.severity:contains('${risk}')`).css("color", colors[colorCount]).css("font-weight", "bold");
			$(box).css("border-color", colors[colorCount]);
			$(`.sev${risk}`).css("border-left-color", `${colors[colorCount]}`)
			$(box).css("color", colors[colorCount--]);
		});
	}
	updateSeverityCount(box, sevId) {
		let count = Array.from($("#vulntable td")).filter(td => $(td).attr('data-sort') == sevId).length;
		window.postMessage({ "type": "updateVuln", "sevId": sevId, "count": count })
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
						_this.vulntable.row(row).remove().draw();
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
	saveChanges(type, data, _this) {
		//data = data.replaceAll("%3C%2Fp%3E%3Cp%3E%3Cbr%3E%3C%2Fp%3E%3Cp%3E", "%3Cbr%3E%3C%2Fp%3E%3Cp%3E");
		//data = data.replaceAll("%3C%2Fp%3E%3Cp%3E%3Cbr%3E%3C%2Fp%3E%3Cp%3E", "%3C%2Fp%3E%3Cbr%3E%3Cp%3E");
		data = `${data}&_token=${_this._token}`
		if(type == 'vulnerability'){
			$.post("updateVulnerability", data, function(resp) {
				if (resp.result != "success") {
					$.alert(resp.message);
				}
				_this._token = resp.token;
			});
		}else if (type == 'note'){
			$.post("updateNote", data, function(resp) {
				if (resp.result != "success") {
					$.alert(resp.message);
				}
				_this._token = resp.token;
			});
			
		}

	}
	setLockScreen(type) {
		if(type == 'vulnerability'){
			this.disableAutoSave()
			$("#vulnForm").addClass("disabled");
		}else{
			$("#notes").addClass("disabled");
		}
	}
	
	lockNoteEditor(notes){
		const selected = $(`#notebook option:selected`);
		for(let note of notes){
			if(note.id == selected.val()){
				this.setLockScreen('note');
			}
		}  
		const notebook = $(`#notebook option`);
		const lockedIds = notes.map(v => v.id);
		for(let note of notebook){
			const id = $(note).val();
			if( lockedIds.indexOf(id) == -1 && $("#notes").hasClass("disabled") && selected.val() == id){
				this.getNote(id);
			}
		}
	}
	updateCallback(type,data) {
		let lockedVulns = data.vulns;
		let lockedNotes = data.notes;
		this.lockNoteEditor(lockedNotes);
		for (let vuln of lockedVulns) {
			if (vuln.id == this.vulnId) {
				this.setLockScreen('vulnerability');
			}
			$(`#deleteVuln${vuln.id}`).hide();
			$("#vulntable tbody tr").each((_a, el) => {
				if ($(el).data('vulnid') == vuln.id) {
					if ($(el).find(".userEdit").length == 0) {
						let vulnName = $(el).find(".vulnName")[0].outerHTML;
						vulnName = vulnName + "<span class='userEdit'>" + vuln.lockby + " is making changes</span>";
						$(el).find(".vulnName")[0].outerHTML = vulnName;
					}
				}
			});
		}
		const vulnIds = lockedVulns.map(v => v.id);
		$("#vulntable tbody tr").each((_a, el) => {
			let vulnId = $(el).data('vulnid');
			if (vulnIds.indexOf(`${vulnId}`) == -1 && this.vulnId == vulnId && $("#vulnForm").hasClass("disabled")) {
				this.getVuln(vulnId);
			} else if (vulnIds.indexOf(`${vulnId}`) == -1) {
				$(el).find(".userEdit").each((_a, edit) => edit.remove());
				$(`#deleteVuln${vulnId}`).show();
			}
		});
		//const activeVulns = Array.from($("#vulntable tbody tr")).map(tr => `${$(tr).data("vulnid")}`).filter(tr => tr != "undefined");
		
		const activeVulns= Array.from(this.vulntable.data().map(function(value, index){
			let col = value[0];
			if(col.indexOf('id="ckl') == -1){
				return "";
			}else{
				return col.split('"')[3].replace("ckl","")
			}
		}))
		
		for (let vuln of data.current) {
			//vuln was added by another user so add it to the table
			if (activeVulns.indexOf(vuln.id) == -1) {
				let rowData = `<tr data-vulnid="${vuln.id}"><td class="sev${entityEncode(vuln.severityName)}">`
				rowData += `<input type="checkbox" id="ckl${vuln.id}"/></td><td data-sort="${vuln.severity}">`
				rowData += `<span class="vulnName">${entityEncode(this.b64DecodeUnicode(vuln.title))}</span><br>`
				rowData += `<span class="category">${entityEncode(vuln.category)}</span><br>`
				rowData += `<span class="severity">${entityEncode(vuln.severityName)}</span>`
				rowData += `</td>`
				rowData += `<td><span class="vulnControl vulnControl-delete" id="deleteVuln${vuln.id}">`
				rowData += `<i class="fa fa-trash" title="Delete Vulnerability"></i></span>`
				rowData += `</td></tr>`
				const row = this.vulntable.row.add($(rowData)).draw().node()
				this.rebindTable();
				this.updateColors();
			} else {
				let row = Array.from($("#vulntable tbody tr")).filter((el) => $(el).data('vulnid') == vuln.id);
				if (row.length > 0 && vuln.id != this.vulnId) {

					let vulnName = $(row[0]).find(".vulnName")[0].innerHTML;
					let category = $(row[0]).find(".category")[0].innerHTML;
					let severity = $(row[0]).find(".severity")[0].innerHTML;
					//Titles or severity was changed by another user. 
					// update the table an resort it
					if (vulnName != this.b64DecodeUnicode(vuln.title)) {
						$(row[0]).find(".vulnName")[0].innerHTML = this.b64DecodeUnicode(vuln.title);
					}
					if (category != vuln.category) {
						$(row[0]).find(".category")[0].innerHTML = vuln.category;
					}
					if (severity != vuln.severityName) {
						$(row[0]).find(".severity")[0].innerHTML = vuln.severityName;
						$(row[0]).children()[0].className = `sev${vuln.severityName}`
						$($(row[0]).children()[1]).attr('data-sort', vuln.severity)
						this.vulntable.row(row[0]).invalidate()
						this.vulntable.order([1, 'desc']).draw();
						this.updateColors();

					}
				}

			}

		}
		const serverVulns = data.current.map(c => c.id);
		// delete rows of vulns that have been deleted by another user
		for (let vuln of activeVulns) {
			if (serverVulns.indexOf(vuln) == -1) {
				let row = Array.from($("#vulntable tbody tr")).filter((el) => $(el).data('vulnid') == vuln);
				this.vulntable.row($(row[0])).remove().draw();
				window.postMessage({ "type": "updateStats" })
				if (this.vulnId == vuln) {
					this.vulnId = -1;
					this.deleteVulnForm();
				}
			}
		}
	}
	getOnGoingAssessmentsToReassign() {
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
	
	
	setUpCVSSScoreEvents(){
		let _this = this;
		$("#cvssString").on("change input", () => {
			let cvssString = $("#cvssString").val();
			_this.cvss.updateCVSSString(cvssString);
			let score = _this.cvss.getCVSSScore();
			let severity = _this.cvss.getCVSSSeverity(score);
			let overall = _this.cvss.convertCVSSSeverity(severity)
			$("#overall").val(overall);
			this.cvss.updateCVSSScore();
			$(".selected").find(".severity")[0].innerHTML = severity == "None"? "Recommended" : severity;
			$(".selected").children()[0].className = `sev${severity== "None"? "Recommended" : severity}`
			$($(".selected").children()[1]).attr('data-sort', score)
			_this.vulntable.row($(".selected")).invalidate()
			_this.vulntable.order([1, 'desc']).draw();
			_this.updateColors()
			_this.queue.push('vulnerability', _this.vulnId, "cvssScore", score);
			_this.queue.push('vulnerability', _this.vulnId, "cvssString", cvssString);
			_this.queue.push('vulnerability', _this.vulnId, "overall", overall); 

		})
	}

	setUpVulnAutoComplete() {
		let _this = this;
		$("#title").autoComplete({
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
				let d = _this.getEditorText("description");
				let r = _this.getEditorText("recommendation");
				const splits = term.split(" :: ");
				$("#title").val(splits[1]);
				$(".selected").find(".vulnName")[0].innerHTML = splits[1]
				$(".selected").find(".category")[0].innerHTML = splits[2]
				$("#dvulnerability").val(splits[0].trim())
				//_this.queueSave(_this.vulnId, "dvulnerability", false)
				let val = $("#dcategory").find("option:contains('" + splits[2].trim() + "')").val()
				_this.setIntVal(val, "dcategory");
				$("#dcategory").val(val).trigger('change');
				const vulnid = splits[0];
				_this.queue.push('vulnerability', _this.vulnId, "dvulnerability", vulnid)
				_this.queue.push('vulnerability', _this.vulnId, "title", splits[1])
				$("#title").attr("intVal", vulnid);
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
										_this.setEditorContents("description", data.desc)
										_this.setEditorContents("recommendation", data.rec)
										_this.setIntVal(data.category, 'dcategory');
										let severity = ""
										if(_this.is40 && data.cvss40String != ""){
											$("#cvssString").val($("<div/>").html(data.cvss40String).text()).trigger("change")
											severity = _this.cvss.getCVSSSeverity(data.cvss40Score);
											let overall = _this.cvss.convertCVSSSeverity(severity)
											$("#overall").val(overall);
											$($(".selected").children()[1]).attr('data-sort', data.cvss41Score)
										}else if(_this.is31 && data.cvss31String != ""){
											$("#cvssString").val($("<div/>").html(data.cvss31String).text()).trigger("change")
											severity = _this.cvss.getCVSSSeverity(data.cvss31Score);
											let overall = _this.cvss.convertCVSSSeverity(severity)
											$("#overall").val(overall);
											$($(".selected").children()[1]).attr('data-sort', data.cvss31Score)
										}else{
											_this.setIntVal(data.likelyhood, 'likelyhood');
											_this.setIntVal(data.impact, 'impact');
											_this.setIntVal(data.overall, 'overall');
											severity = $("#overall").select2('data')[0].text;
											$($(".selected").children()[1]).attr('data-sort', data.overall)
										}
										$(".selected").find(".severity")[0].innerHTML = severity;
										$(".selected").children()[0].className = `sev${severity}`
										_this.vulntable.row($(".selected")).invalidate()
										_this.vulntable.order([1, 'desc']).draw();
										_this.updateColors()
										postMessage({ "type": "updateStats" })

										$(data.cf).each(function(a, b) {
											let el = $("#type" + b.typeid);
											if(el.length == 0 )
												return;
											if (el[0].type == 'checkbox' && b.value == 'true') {
												$(el).prop('checked', true)
											}
											else if (el[0].type == 'checkbox' && b.value == 'false') {
												$(el).prop('checked', false)
											}
											else {
												$(el).val(b.value).trigger('change');
											}
										});
									});

							},
							"cancel": function() { }
						}
					});

				} else {
					$.get('DefaultVulns?action=getvuln&vulnId=' + vulnid)
						.done(function(data) {
							_this.setEditorContents("description", data.desc)
							_this.setEditorContents("recommendation", data.rec)
							_this.setIntVal(data.category, 'dcategory');
							let severity = ""
							if(_this.is40 && data.cvss40String != ""){
								$("#cvssString").val($("<div/>").html(data.cvss40String).text()).trigger("change")
								severity = _this.cvss.getCVSSSeverity(data.cvss40Score);
								let overall = _this.cvss.convertCVSSSeverity(severity)
								$("#overall").val(overall);
								$($(".selected").children()[1]).attr('data-sort', data.cvss41Score)
							}else if(_this.is31 && data.cvss31String != ""){
								$("#cvssString").val($("<div/>").html(data.cvss31String).text()).trigger("change")
								severity = _this.cvss.getCVSSSeverity(data.cvss31Score);
								let overall = _this.cvss.convertCVSSSeverity(severity)
								$("#overall").val(overall);
								$($(".selected").children()[1]).attr('data-sort', data.cvss31Score)
							}else{
								_this.setIntVal(data.likelyhood, 'likelyhood');
								_this.setIntVal(data.impact, 'impact');
								_this.setIntVal(data.overall, 'overall');
								severity = $("#overall").select2('data')[0].text;
								$($(".selected").children()[1]).attr('data-sort', data.overall)
							}
							$(".selected").find(".severity")[0].innerHTML = severity;
							$(".selected").children()[0].className = `sev${severity}`
							_this.vulntable.row($(".selected")).invalidate()
							_this.vulntable.order([1, 'desc']).draw();
							_this.updateColors()
							$(data.cf).each(function(a, b) {
								let el = $("#type" + b.typeid);
								if(el.length == 0 )
									return;
								if (el[0].type == 'checkbox' && b.value == 'true') {
									$(el).prop('checked', true)
								}
								else if (el[0].type == 'checkbox' && b.value == 'false') {
									$(el).prop('checked', false)
								}
								else {
									$(el).val(b.value).trigger('change');
								}
							});
						});
				}
			}
		});

	}

	setUpAddVulnBtn() {
		let _this = this;
		$("#addVuln").click(function() {
			_this.disableAutoSave();
			const desc = "";
			const rec = "";
			let data = "id=" + _this.assesssmentId;
			data += "&description=" + encodeURIComponent(desc);
			data += "&recommendation=" + encodeURIComponent(rec);
			data += "&title=";
			data += "&impact="
			data += "&likelyhood="
			data += "&overall="
			data += "&category="
			data += "&defaultTitle="
			data += "&feedMsg="
			data += "&cvssScore="
			data += "&cvssString="
			data += "section="
			let fields = [];
			for (let id of customFields) {
				let value = $(`#type${id}`).data('default');
				$(`#type${id}`).val(value).trigger('change');
				fields.push(`{"typeid" : ${id}, "value" : "${value}"}`);
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
					const row = _this.vulntable.row.add($(respData[0])).draw().node()
					_this.vulnId = resp.vulnId;
					const vulnId = resp.vulnId;
					$(".selected").each((_a, s) => $(s).removeClass("selected"));
					$(row).addClass("selected");
					_this.rebindTable();
					_this.enableAutoSave();
					_this.updateColors();
				} else if (text.Text == "WO-nnnn Name") {
					text.Text = "Maintenance";
				}

			});

		});
	}
	rebindTable() {
		let _this = this;
		$("#vulntable tbody tr").unbind()
		$("#vulntable span[id^=deleteVuln]").each((_index, element) => {
			$(element).unbind();
		});
		$("#vulntable tbody tr").on('click', function(event) {
			_this.vulnId = $(this).data("vulnid");
			$(".selected").each((_a, s) => $(s).removeClass("selected"))
			$(this).addClass("selected");
			_this.getVuln(_this.vulnId);
		});
		$("#vulntable span[id^=deleteVuln]").each((_index, element) => {
			$(element).on('click', event => {
				const vulnId = parseInt(event.currentTarget.id.replace("deleteVuln", ""));
				_this.deleteVuln(event.currentTarget, vulnId);
			});
		});

	}
	deleteMulti() {
		let _this = this;
		this.disableAutoSave()
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
						_this.vulntable.row(row).remove().draw();
					});

					rows.push("_token=" + _this._token);
					$.post('DeleteVulns', rows.join("&")).done(function(resp) {
						_this._token = resp.token;
						_this.deleteVulnForm();
						_this.alertMessage(resp, "Vulnerabilties Deleted Successfully");
						window.postMessage({ "type": "updateStats" })
					});

				},
				"no": function() { _this.enableAutoSave(); }
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
					columnClass: 'small',
					autoClose: 'ok|100',
					backgroundDismiss: 'OK',
					buttons: {
						OK: ()=>{}
					}
				}
			);
		else
			$.alert(
				{
					title: "Error",
					type: "red",
					content: resp.message,
					columnClass: 'small',
					autoClose: 'ok|100',
					backgroundDismiss: 'OK',
					buttons: {
						OK: ()=>{}
					}
				}
			);

		this._token = resp.token;
	}

	deleteVulnForm() {
		this.disableAutoSave()
		$("#vulnForm").addClass("disabled")
		this.setEditorContents("recommendation", "", true);
		this.setEditorContents("details", "", true);
		this.setEditorContents("description", "", true);
		$('[id*="header"]').each((_a, h) => h.innerHTML = "");
		$("#title").val("");
		$("#cvssString").val("")
		this.cvss.updateCVSSScore(this.cvss.updateCVSSString(""))
		$("#impact").attr("intVal", "-1");
		$("#impact").val("").trigger("change");
		$("#likelyhood").val("").trigger("change");
		$("#overall").val("").trigger("change");
		$("#reportSection").val("Default").trigger("change")
		$("#category").val("");
		$("#title").attr("intVal", "-1");
		$("#title").val("");
		$("#dcategory").val("").trigger('change');
		$('[id^="type"]').each((_index, el) => {
			if(el.id.indexOf("header") != -1)
				return;
				
			let value = $(el).data('default');
			if(el.length == 0 )
				return;
			if (el.type == 'checkbox' && value == 'true') {
				$(el).prop('checked', true)
			}
			else if (el.type == 'checkbox') {
				$(el).prop('checked', false)
			}
			else {
				$(el).val(value).trigger('change');
			}
		}
		);

	}
	disableAutoSave() {
		$('[id*="header"]').each((_a, h) => h.innerHTML = "");

		this.editors.recommendation.off('change');
		this.editors.description.off('change')
		this.editors.details.off('change')
		$("#title").unbind('input');
		$("#overall").unbind('input');
		$("#impact").unbind('input');
		$("#likelyhood").unbind('input');
		$("#reportSection").unbind('input');
		$("#dcategory").unbind('input');
		$("#cvssString").unbind('input');
		$("#cvssString").unbind('change');
		$('[id^="type"]').each((_index, el) => $(el).unbind('input'));
	}

	enableAutoSave() {
		let _this = this;
		this.descUndoCount=0;
		/// This is a hack becuase toastui does not have inital undo history set correctly
		/// https://github.com/nhn/tui.editor/issues/3195
		
		this.editors.description.on( 'keydown', function(a,e){
			const html = _this.editors.description.getHTML()
			if ((e.ctrlKey || e.metaKey) && e.key == 'z' && html == _this.initialHTML.description) {
				e.preventDefault();
				throw new Error("Prevent Undo");
			 }
		})
		
		this.editors.description.on( 'change', function(){
			_this.descUndoCount++;
			let contents = _this.editors.description.getHTML()
			_this.queue.push('vulnerability', _this.vulnId, "description", encodeURIComponent(contents));
		});
		this.editors.recommendation.on( 'change', function() {
			let contents = _this.editors.recommendation.getHTML()
			_this.queue.push('vulnerability', _this.vulnId, "recommendation", encodeURIComponent(contents));
		});
		this.editors.details.on( 'change', function() {
			let contents = _this.editors.details.getHTML()
			_this.queue.push('vulnerability', _this.vulnId, "details", encodeURIComponent(contents));
		});
		$("#title").on('input', function(event) {
			$(".selected").find(".vulnName")[0].innerHTML = entityEncode($(this).val())
			_this.queue.push('vulnerability', _this.vulnId, "title", $(this).val());
		});
		$("#overall").on('input', function(event) {
			const severity = $(this).select2('data')[0].text
			$(".selected").find(".severity")[0].innerHTML = severity
			$(".selected").children()[0].className = `sev${severity}`
			$($(".selected").children()[1]).attr('data-sort', $(this).val())
			_this.vulntable.row($(".selected")).invalidate()
			_this.vulntable.order([1, 'desc']).draw();
			_this.updateColors()
			_this.queue.push('vulnerability', _this.vulnId, "overall", $(this).val());
			_this.queue.push('vulnerability', _this.vulnId, "likelyhood", $(this).val());
			_this.queue.push('vulnerability', _this.vulnId, "impact", $(this).val());
		});
		$("#impact").on('input', function(event) {
			_this.queue.push('vulnerability', _this.vulnId, "impact", $(this).val());
		});
		$("#likelyhood").on('input', function(event) {
			_this.queue.push('vulnerability', _this.vulnId, "likelyhood", $(this).val());
		});
		$("#reportSection").on('input', function(event) {
			_this.queue.push('vulnerability', _this.vulnId, "reportSection", $(this).val());
		});
		$("#dcategory").on('input', function(event) {
			const catName = $(this).select2('data')[0].text
			$(".selected").find(".category")[0].innerHTML = catName
			window.postMessage({ "type": "updateStats" });
			_this.queue.push('vulnerability', _this.vulnId, "dcategory", $(this).val());
		});
		$('[id^="type"]').each((_index, el) => $(el).on('input', function(event) {
			let val = "";
			if (this.type == 'checkbox') {
				val = $(this).is(":checked");
			} else {
				val = $(this).val();
			}
			_this.queue.push('vulnerability', _this.vulnId, this.id, `${val}`);
		}));
		this.setUpCVSSScoreEvents();

	}
	setEditorContents(type, data, createNew=false) {
		let decoded = this.b64DecodeUnicode(data);
		decoded = decoded.replaceAll("<br />", "\n");
		if(createNew){
			this.editors[type].destroy();
			this.editors[type] = new Editor({
				el: document.querySelector('#' + type),
				previewStyle: 'vertical',
				height: '600px',
				autofocus: false,
				plugins: [colorSyntax, tableMergedCell],
				hooks: {
					addImageBlobHook: async (blob,callback, source)=>{
						const encodedImage = await imageToURL(blob)
						let data = "encodedImage=" + encodeURIComponent(encodedImage);
						data += "&assessmentId="+$("#appid")[0].value;
						$.post("UploadImage",data).done(function(resp) {
							let uuid = resp.message;
							callback("getImage?id=" + uuid);
						});
							
					}
				}
			});
		}
		this.editors[type].hide();	
		this.editors[type].setHTML(decoded, false);
		this.initialHTML[type] = this.editors[type].getHTML();
		this.editors[type].show();	
	}
	getVuln(id) {
		this.vulnId = id;
		
		this.disableAutoSave()
		this.deleteVulnForm();
		$(`#deleteVuln${id}`).show();
		$("#vulntable tbody tr").each((_a, el) => {
			if ($(el).data('vulnid') == id) {
				$(el).find(".userEdit").each((_a, el) => el.remove())
			}
		});
		let _this = this;
		$("#vulnForm").removeClass("disabled");
		$.get('AddVulnerability?vulnid=' + id + '&action=get').done(function(data) {
			$("#title").val($("<div/>").html(_this.b64DecodeUnicode(data.name)).text());
			$("#cvssString").val($("<div/>").html(data.cvssString).text())
			let vector = _this.cvss.updateCVSSString(data.cvssString);
			_this.cvss.updateCVSSScore(vector);
			_this.setEditorContents("recommendation", data.recommendation, true);
			_this.setEditorContents("details", data.details, true);
			_this.setEditorContents("description", data.description, true);
			_this.setIntVal(data.overall, 'overall');
			_this.setIntVal(data.likelyhood, 'likelyhood');
			_this.setIntVal(data.impact, 'impact');
			_this.setIntVal(data.catid, 'dcategory');
			if(data.section && data.section != ""){
				$("#reportSection").val(data.section).trigger("change");
				if($("#reportSection").val()==null){
					$("#reportSection").next().addClass("field-error");
				}
			}else{
				$("#reportSection").val("Default").trigger("change");
				$("#reportSection").next().removeClass("field-error");
			}
			$(data.cf).each(function(a, b) {
				let el = $("#type" + b.typeid);
				if(el.length == 0 )
					return;
					
				if (el[0].type == 'checkbox' && b.value == 'true') {
					$(el).prop('checked', true);
				}
				else if (el[0].type == 'checkbox' && b.value == 'false') {
					$(el).prop('checked', false);
				}
				else {
					$(el).val(b.value).trigger('change');
				}

			});
			_this.enableAutoSave()
		});

	}
	deleteVuln(el, id) {
		let _this = this;
		const row = $(el).parents("tr");
		_this.disableAutoSave();

		$.confirm({
			type: "red",
			title: "Are you sure?",
			content: "Do you want to delete " + _this.vulntable.row(row).data()[3],
			buttons: {
				"yes, delete it": function() {
					let data = 'vulnid=' + id + '&action=delete';
					data += "&_token=" + _this._token;
					$.post('AddVulnerability', data).done(function(resp) {
						const isError = getData(resp);
						window.postMessage({ "type": "updateStats" })
						if (isError != "error") {
							_this.vulntable.row(row).remove().draw();
							_this.deleteVulnForm();
						}
					});
				},
				cancel: function() { _this.enableAutoSave() }
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

$(function() {
	var url = document.location.toString();
	if (url.match('#')) {
		$('.nav-tabs a[href="' + location.hash + '"]').tab('show');
	}
	$("a").click(evt => {
		if (evt.target.href.indexOf("VulnView") != -1) {
			location.href = "#VulnView"
			setTimeout(function(){
				global.vulnView.vulntable.columns.adjust();
			}, 500)
		} else if (evt.target.href.indexOf("NoteView") != -1) {
			location.href = "#NoteView"
		}
	})
	global.vulnView = new VulnerablilityView($("#assessmentId")[0].value);
})








