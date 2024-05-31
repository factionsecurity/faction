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
	push(id, key, value) {
		this.locks.setLock(id, key);
		if (document.getElementById(`${key}_header`)) {
			document.getElementById(`${key}_header`).innerHTML = "*"
		}
		clearTimeout(this.timer);
		if (id in this.queue) {
			this.queue[id][key] = value;
		} else {
			this.queue[id] = {};
			this.queue[id][key] = value;
		}
		let _this = this;
		this.timer = setTimeout(function() {
			_this.save()
		}, this.timeout);
	}

	save() {
		const keys = Object.keys(this.queue);
		let _this = this;
		for (let key of keys) {
			let data = Object.keys(this.queue[key]).map((k) => {
				if (this.caller.vulnId == key && document.getElementById(`${k}_header`)) {
					document.getElementById(`${k}_header`).innerHTML = ""
				}
				setTimeout(function() {
					_this.locks.clearLock(key, k);
				}, 5000)
				return `${k}=${this.queue[key][k]}`
			})

			data = `vulnid=${key}&` + _this.handleCustom(data).join("&")
			this.saveCallback(data, this.caller);
			delete this.queue[key];
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
	constructor(caller, assessmentId, updateVulnsCallback) {
		this.lockIds = {};
		this.caller = caller;
		this.token = caller.token;
		this.assessmentId = assessmentId;
		this.updateVulnsCallback = updateVulnsCallback;
		this.checkLocks()
		this.errorMessageShown = false;
	}
	setLock(id, attr) {
		let _this = this;
		console.log(this.lockIds)
		if (typeof this.lockIds[id] == "undefined") {
			this.lockIds[id] = [attr];
			$.get(`SetVulnLock?vulnid=${id}&vulnAttr=${attr}&_token=${this.token}`).done((resp) => {
				if (resp.result == "success") {
				}
			});
		} else {
			if (!this.lockIds[id].some(i => i == attr)) {
				this.lockIds[id].push(attr);
				$.get(`SetVulnLock?vulnid=${id}&vulnAttr=${attr}&_token=${this.token}`).done((resp) => {
					if (resp.result == "success") {
					}
				});
			}

		}

	}
	clearLock(id, attr) {
		if (this.lockIds[id]) {
			this.lockIds[id] = this.lockIds[id].filter(l => l != attr)
			$.get(`ClearVulnLock?vulnid=${id}&vulnAttr=${attr}&_token=${this.token}`).done();
		} else {
			console.log("no locks found");
		}
	}
	checkLocks() {
		let _this = this;
		setInterval(function() {
			$.get(`CheckVulnLocks?id=${_this.assessmentId}`).done((resp) => {
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
				_this.updateVulnsCallback(resp);
			}).catch(() => {
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
		this.queue = new SaveQueue(this, assessmentId, this.saveChanges, (vulns) => { this.updateVulnsCallback(vulns) });
		this.vulnId = -1;
		this.editors = {};
		this.assesssmentId = assessmentId //$("#assessmentId")[0].value;
		let fromMarkdown = {
			name: 'fromMarkdown',
			display: 'command',
			title: 'Convert Markdown',
			buttonClass: '',
			innerHTML: '<i class="fa-brands fa-markdown" style="color:lightgray"></i>',
			add: function(core, targetElement) {
				core.context.fromMarkdown = {
					targetButton: targetElement,
					preElement: null
				}
			},
			active: function(element) {
				if (element) {
					this.util.addClass(this.context.fromMarkdown.targetButton.firstChild, 'mdEnabled');
					this.context.fromMarkdown.preElement = element;
					return true;
				} else {
					this.util.removeClass(this.context.fromMarkdown.targetButton.firstChild, 'mdEnabled');
					this.context.fromMarkdown.preElement = null;
				}
				return false;
			},
			action: function() {
				let selected = this.getSelectedElements();
				const md = selected.reduce((acc, item) => acc + item.innerText + "<br/>", "");
				const html = marked.parse(md);
				const div = document.createElement("p");
				div.innerHTML = html;
				const parent = selected[0].parentNode;
				parent.insertBefore(div, selected[0]);
				for (let i = 0; i < selected.length; i++) {
					selected[i].remove();
				}
				this.history.push(true)
				console.log(this);
			}
		}
		plugins['fromMarkdown'] = fromMarkdown;
		this.editorOptions = {
			codeMirror: CodeMirror,
			plugins: plugins,
			buttonList: [
				['undo', 'redo', 'fontSize', 'formatBlock', 'textStyle'],
				['bold', 'underline', 'italic', 'strike', 'subscript', 'superscript', 'removeFormat'],
				['fontColor', 'hiliteColor', 'outdent', 'indent', 'align', 'horizontalRule', 'list', 'table'],
				['link', 'image', 'fullScreen', 'showBlocks', 'fromMarkdown', 'codeView'],

			],
			defaultStyle: 'font-family: arial; font-size: 18px',
			minHeight: 500,
			height: 'auto'
		};
		this.editorTimeout = {};
		this.clearLockTimeout = {};
		$(".select2").select2();

		this.vulnTable = $('#vulntable').DataTable({
			"paging": false,
			"lengthChange": false,
			"searching": false,
			"ordering": true,
			"info": false,
			"autoWidth": false,
			"order": [[1, "desc"]],
			"columns": [
				{ width: "10px" }, //checkbox
				null, //name
				{ width: "10px" } //controls

			],
			columnDefs: [
				{ orderable: false, targets: '_all' }
			]
		});
		$(window).on('resize', () => {
			this.vulnTable.columns.adjust();
		});
		this.setUpEventHandlers()
		this.updateColors();
		this.editors.description = suneditor.create("description", {...this.editorOptions});
		this.editors.recommendation = suneditor.create("recommendation", {...this.editorOptions});
		this.editors.details = suneditor.create("details", {...this.editorOptions});
		this.setUpVulnAutoComplete()
		this.is40 = $("#isCVSS40").val() == "true"
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
	saveChanges(data, _this) {
		data = `${data}&_token=${_this._token}`
		$.post("updateVulnerability", data, function(resp) {
			if (resp.result != "success") {
				$.alert(resp.message);
			}
			_this._token = resp.token;
		});

	}
	setLockScreen() {
		this.disableAutoSave()
		$("#vulnForm").addClass("disabled")
	}
	clearLockScreen(vulnId) {
		this.getVuln(vulnId)
	}
	updateVulnsCallback(data) {
		let lockedVulns = data.vulns;
		for (let vuln of lockedVulns) {
			if (vuln.id == this.vulnId) {
				this.setLockScreen();
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
		const activeVulns = Array.from($("#vulntable tbody tr")).map(tr => `${$(tr).data("vulnid")}`).filter(tr => tr != "undefined");

		for (let vuln of data.current) {
			//vuln was added by another user so add it to the table
			if (activeVulns.indexOf(vuln.id) == -1) {
				let rowData = `<tr data-vulnid="${vuln.id}"><td class="sev${vuln.severity}">`
				rowData += `<input type="checkbox" id="ckl${vuln.id}"/></td><td data-sort="${vuln.severity}">`
				rowData += `<span class="vulnName">${vuln.title}</span><br>`
				rowData += `<span class="category">${vuln.category}</span><br>`
				rowData += `<span class="severity">${vuln.severityName}</span>`
				rowData += `</td>`
				rowData += `<td><span class="vulnControl vulnControl-delete" id="deleteVuln${vuln.id}">`
				rowData += `<i class="fa fa-trash" title="Delete Vulnerability"></i></span>`
				rowData += `</td></tr>`
				console.log(rowData)
				const row = this.vulnTable.row.add($(rowData)).draw().node()
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
					if (vulnName != vuln.title) {
						$(row[0]).find(".vulnName")[0].innerHTML = vuln.title;
					}
					if (category != vuln.category) {
						$(row[0]).find(".category")[0].innerHTML = vuln.category;
					}
					if (severity != vuln.severityName) {
						$(row[0]).find(".severity")[0].innerHTML = vuln.severityName;
						$(row[0]).children()[0].className = `sev${vuln.severityName}`
						$($(row[0]).children()[1]).attr('data-sort', vuln.severity)
						this.vulnTable.row(row[0]).invalidate()
						this.vulnTable.order([1, 'desc']).draw();
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
				this.vulnTable.row($(row[0])).remove().draw();
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
			_this.vulnTable.row($(".selected")).invalidate()
			_this.vulnTable.order([1, 'desc']).draw();
			_this.updateColors()
			_this.queue.push(_this.vulnId, "cvssScore", score);
			_this.queue.push(_this.vulnId, "cvssString", cvssString);
			_this.queue.push(_this.vulnId, "overall", overall); 

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
				_this.queue.push(_this.vulnId, "dvulnerability", vulnid)
				_this.queue.push(_this.vulnId, "title", splits[1])
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
										_this.editors.description.setContents(marked.parse(_this.b64DecodeUnicode(data.desc)));
										_this.editors.recommendation.setContents(marked.parse(_this.b64DecodeUnicode(data.rec)));
										_this.setIntVal(data.likelyhood, 'likelyhood');
										_this.setIntVal(data.impact, 'impact');
										_this.setIntVal(data.overall, 'overall');
										_this.setIntVal(data.category, 'dcategory');
										const severity = $("#overall").select2('data')[0].text;
										$(".selected").find(".severity")[0].innerHTML = severity;
										$(".selected").children()[0].className = `sev${severity}`
										$($(".selected").children()[1]).attr('data-sort', data.overall)
										_this.vulnTable.row($(".selected")).invalidate()
										_this.vulnTable.order([1, 'desc']).draw();
										_this.updateColors()
										postMessage({ "type": "updateStats" })

										$(data.cf).each(function(a, b) {
											let el = $("#type" + b.typeid);
											if (el.type == 'checkbox' && b.value == 'true') {
												$(el).prop('checked', true)
											}
											else if (el.type == 'checkbox' && b.value == 'false') {
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
							_this.editors.description.setContents(marked.parse(_this.b64DecodeUnicode(data.desc)).replace(/\n/g, " "));
							_this.editors.recommendation.setContents(marked.parse(_this.b64DecodeUnicode(data.rec)).replace(/\n/g, " "));
							_this.setIntVal(data.likelyhood, 'likelyhood');
							_this.setIntVal(data.impact, 'impact');
							_this.setIntVal(data.overall, 'overall');
							const severity = $("#overall").select2('data')[0].text;
							$(".selected").find(".severity")[0].innerHTML = severity;
							$(".selected").children()[0].className = `sev${severity}`
							$($(".selected").children()[1]).attr('data-sort', data.overall)
							_this.vulnTable.row($(".selected")).invalidate()
							_this.vulnTable.order([1, 'desc']).draw();
							_this.updateColors()
							$(data.cf).each(function(a, b) {
								let el = $("#type" + b.typeid);
								if (el.type == 'checkbox' && b.value == 'true') {
									$(el).prop('checked', true)
								}
								else if (el.type == 'checkbox' && b.value == 'false') {
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
			const desc = _this.getEditorText("description");
			const rec = _this.getEditorText("recommendation");
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
					const row = _this.vulnTable.row.add($(respData[0])).draw().node()
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
						_this.vulnTable.row(row).remove().draw();
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
					autoClose: 'ok|100'
				}
			);
		else
			$.alert(
				{
					title: "Error",
					type: "red",
					content: resp.message,
					columnClass: 'small',
					autoClose: 'ok|100'
				}
			);

		this._token = resp.token;
	}

	deleteVulnForm() {
		this.disableAutoSave()
		$("#vulnForm").addClass("disabled")
		this.editors.description.setContents("")
		this.editors.recommendation.setContents("");
		this.editors.details.setContents("");
		$('[id*="header"]').each((_a, h) => h.innerHTML = "");
		$("#title").val("");
		$("#cvssString").val("")
		this.cvss.updateCVSSScore(this.cvss.updateCVSSString(""))
		$("#impact").attr("intVal", "-1");
		$("#impact").val("").trigger("change");
		$("#likelyhood").val("").trigger("change");
		$("#overall").val("").trigger("change");
		$("#category").val("");
		$("#title").attr("intVal", "-1");
		$("#title").val("");
		$("#dcategory").val("").trigger('change')
		$('[id^="type"]').each((_index, el) => {
			let value = $(el).data('default');
			$(el).val(value);
		}
		);

	}
	disableAutoSave() {
		console.log("disable autosave")
		$('[id*="header"]').each((_a, h) => h.innerHTML = "");

		this.editors.recommendation.onChange = function() { };
		this.editors.recommendation.onChange = function() { };
		this.editors.description.onChange = function() { };
		this.editors.description.onInput = function() { };
		this.editors.details.onChange = function() { };
		this.editors.details.onInput = function() { };
		$("#title").unbind('input');
		$("#overall").unbind('input');
		$("#impact").unbind('input');
		$("#likelyhood").unbind('input');
		$("#dcategory").unbind('input');
		$("#cvssString").unbind('input');
		$("#cvssString").unbind('change');
		$('[id^="type"]').each((_index, el) => $(el).unbind('input'));
	}

	enableAutoSave() {
		let _this = this;
		this.editors.description.onInput = function(contents, core) {
			_this.queue.push(_this.vulnId, "description", encodeURIComponent(contents));
		}
		this.editors.description.onChange = function(contents, core) {
			if (!contents.endsWith("</p>")) {
				_this.editors.description.setContents(contents + "<p><br></p>");
			}
			contents = marked.parse(contents)
			_this.queue.push(_this.vulnId, "description", encodeURIComponent(contents));
		}
		this.editors.recommendation.onInput = function(contents, core) {
			_this.queue.push(_this.vulnId, "recommendation", encodeURIComponent(contents));
		}
		this.editors.recommendation.onChange = function(contents, core) {
			if (!contents.endsWith("</p>")) {
				_this.editors.recommendation.setContents(contents + "<p><br></p>");
			}
			_this.queue.push(_this.vulnId, "recommendation", encodeURIComponent(contents));
		}
		this.editors.details.onInput = function(contents, core) {
			_this.queue.push(_this.vulnId, "details", encodeURIComponent(contents));
		}
		this.editors.details.onChange = function(contents, core) {
			if (!contents.endsWith("</p>")) {
				_this.editors.details.setContents(contents + "<p><br></p>");
			}
			_this.queue.push(_this.vulnId, "details", encodeURIComponent(contents));
		}
		$("#title").on('input', function(event) {
			$(".selected").find(".vulnName")[0].innerHTML = $(this).val()
			_this.queue.push(_this.vulnId, "title", $(this).val());
		});
		$("#overall").on('input', function(event) {
			const severity = $(this).select2('data')[0].text
			$(".selected").find(".severity")[0].innerHTML = severity
			$(".selected").children()[0].className = `sev${severity}`
			$($(".selected").children()[1]).attr('data-sort', $(this).val())
			_this.vulnTable.row($(".selected")).invalidate()
			_this.vulnTable.order([1, 'desc']).draw();
			_this.updateColors()
			_this.queue.push(_this.vulnId, "overall", $(this).val());
			_this.queue.push(_this.vulnId, "likelyhood", $(this).val());
			_this.queue.push(_this.vulnId, "impact", $(this).val());
		});
		$("#impact").on('input', function(event) {
			_this.queue.push(_this.vulnId, "impact", $(this).val());
		});
		$("#likelyhood").on('input', function(event) {
			_this.queue.push(_this.vulnId, "likelyhood", $(this).val());
		});
		$("#dcategory").on('input', function(event) {
			const catName = $(this).select2('data')[0].text
			$(".selected").find(".category")[0].innerHTML = catName
			window.postMessage({ "type": "updateStats" });
			_this.queue.push(_this.vulnId, "dcategory", $(this).val());
		});
		$('[id^="type"]').each((_index, el) => $(el).on('input', function(event) {
			let val = "";
			if (this.type == 'checkbox') {
				val = $(this).is(":checked");
			} else {
				val = $(this).val();
			}
			_this.queue.push(_this.vulnId, this.id, `${val}`);
		}));
		this.setUpCVSSScoreEvents();

	}
	setEditorContents(type, data) {
		let decoded = this.b64DecodeUnicode(data);
		if (decoded.endsWith("</div>")) {
			decoded = decoded + "<p><br></p>";
		}
		this.editors[type].setContents(marked.parse(decoded));
		console.log(this.editors[type])
		this.editors[type].core.history.reset();
		this.editors[type].core.history.push(true);
	}
	getVuln(id) {
		this.vulnId = id;
		this.disableAutoSave()
		$(`#deleteVuln${id}`).show();
		$("#vulntable tbody tr").each((_a, el) => {
			if ($(el).data('vulnid') == id) {
				$(el).find(".userEdit").each((_a, el) => el.remove())
			}
		});
		let _this = this;
		$("#vulnForm").removeClass("disabled");
		$.get('AddVulnerability?vulnid=' + id + '&action=get').done(function(data) {
			$("#title").val($("<div/>").html(data.name).text());
			$("#cvssString").val($("<div/>").html(data.cvssString).text())
			let vector = _this.cvss.updateCVSSString(data.cvssString);
			_this.cvss.updateCVSSScore(vector);
			_this.setEditorContents("description", data.description);
			_this.setEditorContents("recommendation", data.recommendation);
			_this.setEditorContents("details", data.details);
			_this.setIntVal(data.overall, 'overall');
			_this.setIntVal(data.likelyhood, 'likelyhood');
			_this.setIntVal(data.impact, 'impact');
			_this.setIntVal(data.catid, 'dcategory');
			$(data.cf).each(function(a, b) {
				let el = $("#type" + b.typeid);
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
			content: "Do you want to delete " + _this.vulnTable.row(row).data()[3],
			buttons: {
				"yes, delete it": function() {
					let data = 'vulnid=' + id + '&action=delete';
					data += "&_token=" + _this._token;
					$.post('AddVulnerability', data).done(function(resp) {
						const isError = getData(resp);
						window.postMessage({ "type": "updateStats" })
						if (isError != "error") {
							_this.vulnTable.row(row).remove().draw();
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
	global.vulnView = new VulnerablilityView($("#assessmentId")[0].value);
})








