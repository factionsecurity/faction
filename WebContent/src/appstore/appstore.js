require('bootstrap-switch/dist/css/bootstrap3/bootstrap-switch.min.css');
require('../loading/css/jquery-loading.css');
import '../scripts/fileupload/js/fileinput.min';
import 'jquery';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'bootstrap-switch';
import Sortable from 'sortablejs/modular/sortable.core.esm.js';
import { marked } from 'marked';
import '../loading/js/jquery-loading';


class AppStore {
	constructor() {
		this.getApps();
		this.setupSortableLists();
		
		$("#installExtension").on('click',  ( )=> window.location="InstallExtension");
		$("#installUpdate").on('click', function(){
			const uuid=$(this).data("uuid")
			window.location=`UpdateExtension?uuid=${uuid}`
		});
		$("#saveConfigs").on('click', function() {
			const id = $(this).data("id");
			let updates ={};
			Array.from($('input[type="text"], input[type="password"]')).forEach( el => {
				const value = el.value;
				const key = $(el).data("key");
				if(value.trim() != ""){
					updates[key]=value;
				}
			});
			const formData = `id=${id}&configs=${encodeURIComponent(JSON.stringify(updates))}`
			fetch("UpdateConfigs", {
				method: "POST", 
				mode: "cors", 
				cache: "no-cache", 
				credentials: "same-origin", 
				headers: {
				  'Content-Type': 'application/x-www-form-urlencoded',
				},
				redirect: "follow", 
				referrerPolicy: "no-referrer", 
				body: formData, 
			}).then( response => response.json())
			.then( json => console.log(json));
		});

	}

	setupSortableLists() {
		let _this = this;
		let config = {
			animation: 100,
			group: 'assessment',
			draggable: '.list-group-item',
			handle: '.list-group-item',
			filter: '.sortable-disabled',
			ghostClass: 'active',
			onChoose: function(event) {
				$(".list-group-item").removeClass("active");
				$(event.item).addClass("active");
				let id = $(event.item).data("id");
				$('#appBox').addClass("disabled");
				$('#appLoading').show();
				$("#loadingbox").loading({ overlay: true, base: 0.3 });
				fetch(`GetDetails?id=${id}`)
				.then( response => response.json())
				.then( json => {
					$('#appLoading').hide();
					$('#appBox').removeClass("disabled");
					$("#appConfigs").html("");
					$('#appDescription').html(marked.parse(_this.b64DecodeUnicode(json.description)));
					$('#appTitle').html(`${json.title} <br\><small>Version: ${json.version}</small>`);
					$('#appAuthor').html(json.author);
					$('#appURL').html(json.url);
					$('#appHash').html(json.hash);
					$('#appURL').attr("href", json.url);
					$('#installUpdate').attr("data-uuid", json.uuid);
					let logo = json.logo;
					if(logo==""){
						logo="../app-default.png";
					}else{
						logo=`data:image/png;base64, ${logo}`;
					}
					$('#appLogo').attr("src", logo);
					let hasConfigs = json.configs.length>0;
					if(hasConfigs){
						const hr = document.createElement("hr");
						$("#appConfigs").append(hr);
						const h1 = document.createElement("h1");
						h1.innerHTML="Settings"
						$("#appConfigs").append(h1);
						$("#saveConfigs").show();
						$("#saveConfigs").data('id', id);
						for(config of json.configs){
							const key = Object.keys(config)[0];
							const value = config[key]['value'];
							const type = config[key]['type'];
							const div = _this.createConfig(key,value, type);
							$("#appConfigs").append(div);
						}
					}else{
						$("#saveConfigs").hide();
					}
						
				})
			},
			onEnd: function (evt) {
				const itemEl = evt.item;
				const appType = $(itemEl).parent()[0].id.replace("Extensions", "");
				
				const children = $(itemEl).parent().children();
				let index=0;
				const appIds = Array.from(children).map( c => c.dataset.id);
				const appList = `appList=${appIds.join(",")}&appType=${appType}`;
				fetch("ChangeOrder", {
					method: "POST", 
					mode: "cors", 
					cache: "no-cache", 
					credentials: "same-origin", 
					headers: {
					  'Content-Type': 'application/x-www-form-urlencoded',
					},
					redirect: "follow", 
					referrerPolicy: "no-referrer", 
					body: appList, 
				
				}).then( response => response.json())
				.then( json => console.log(json));
			}
		};
		Sortable.create($("#assessmentExtensions")[0], config);
		config.group = 'vulnerability';
		Sortable.create($("#vulnerabilityExtensions")[0], config);
		config.group = 'verification';
		Sortable.create($("#verificationExtensions")[0], config);
		config.group = 'inventory';
		Sortable.create($("#inventoryExtensions")[0], config);
		config.group = 'disabled';
		config.onEnd = function(){};
		Sortable.create($("#disabledExtensions")[0], config);
	}
	b64DecodeUnicode(str) {
		str=decodeURIComponent(str);
		return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
			return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
		}).join(''));
	} 

	
	createConfig(key, value, type){
		let div = document.createElement("div");
		div.className="col-md-4";
		let inputType ='text';
		let placeholder=""
		if(type == "password"){
			inputType = "password"
			placeholder = "***************************"
		}
		div.innerHTML = `<div class="form-group">
			<label>${key}:</label>
			<input type="${inputType}" data-key="${key}" class="form-control pull-right" value="${value}" autocomplete="new-password" placeholder="${placeholder}">
		</div>`;
		return div;
		
	}

	createCard(id, name, version, author, url, logo, enabled) {
		if(logo==""){
			logo="../app-default.png";
		}else{
			logo=`data:image/png;base64, ${logo}`
		}
		let checked = "";
		if(enabled){
			checked = "checked"
		}
		const li = document.createElement("li");
		li.className = "list-group-item";
		li.setAttribute("data-id", id);
		li.innerHTML = `
				<div class="appCard row">
					<div class="col-md-1 handle-container" style="min-width: 100px">
						<div class="handle">
							<i class="fa-solid fa-grip-lines handle"></i>
					 		<img class="appLogo-small" src="${logo}"/>
						</div>
					</div>
					<div class="col-md-7 handle-container">
						<div class="handle">
							<b>${name}</b> Version: ${version}<br> by: ${author} <br> link:
							<a href="${url}">${url}</a>
						</div>
					</div>
					<div class="col-md-3 handle-container pull-right" style="width:140px">
						<div class="handle">
							<input type="checkbox" data-id="${id}" ${checked}/>
							&nbsp;&nbsp;<span data-id="${id}" class="vulnControl vulnControl-delete">
								<i class="fa fa-trash"></i>
							</span>
						</div>
					</div>
				</div>`
		return li;
	}
	addCard(elId, id, name, version, author, url, logo, enabled) {
		$(elId).append(this.createCard(id, name, version, author, url, logo, enabled));
	}
	
	getApps(){
		fetch("GetApps")
		.then( (response) => response.json())
		.then( (json) => {
			this.updateLists("#disabledExtensions", json.disabled);
			this.updateLists("#assessmentExtensions", json.assessment);
			this.updateLists("#vulnerabilityExtensions", json.vulnerability);
			this.updateLists("#verificationExtensions", json.verification);
			this.updateLists("#inventoryExtensions", json.inventory);
			$('input[type="checkbox"]').bootstrapSwitch("size", "mini");
			$('input:checked').bootstrapSwitch("state", true, true);
			$('input[type="checkbox"]').on('switchChange.bootstrapSwitch', function(event, state) {
			  const id = $(this).data("id");
			  if(state){
			  	fetch(`EnableApp?id=${id}`)
			  	.then( response => response.json())
			  	.then( json => location.reload())
			  }
			  else{
			  	fetch(`DisableApp?id=${id}`)
			  	.then( response => response.json())
			  	.then( json => location.reload())
			  }
			});
			$('.vulnControl-delete').on('click', function(){
			  	const id = $(this).data("id");
				$.confirm({
					title: "Delete App",
					content: "Are you sure you want to delete?",
					buttons: {
						"yes": () => {
							fetch(`DeleteApp?id=${id}`)
							.then( response => response.json())
							.then( json => location.reload());
							
						},
						"cancel" : () => {}
					}
				})
			});
		})
	}
	updateLists(elId,list){
		if(list.length == 0){
			const li = document.createElement("li");
			li.className = "list-group-item";
			li.innerHTML = "No Extensions"
			$(elId).append(li);
			
		}else{
			list.forEach( data =>{
				this.addCard(elId, data.id, data.title, data.version, data.author, data.url, data.logo, data.enabled)	
			});
		}
	}

}

new AppStore();
