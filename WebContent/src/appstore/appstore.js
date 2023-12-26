require('bootstrap-switch/dist/css/bootstrap3/bootstrap-switch.min.css');
import '../scripts/fileupload/js/fileinput.min';
import 'jquery';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'bootstrap-switch';
import Sortable from 'sortablejs/modular/sortable.core.esm.js';



class AppStore {
	constructor(){
		
	}
	
	setupSortableLists(){
		let config = { animation: 100, 
			group: 'assessment', 
			draggable: '.list-group-item', 
			handle: '.list-group-item', 
			filter: '.sortable-disabled', 
			ghostClass: 'active',
			onChoose: function(event){
				$(".list-group-item").removeClass("active");
				$(event.item).addClass("active");
				
			}};
		Sortable.create($("#assessmentExtensions")[0], config);
		config.group = 'vulnerability';
		Sortable.create($("#vulnerabilityExtensions")[0], config);
		config.group = 'verification';
		Sortable.create($("#verificationExtensions")[0], config);
		config.group = 'inventory';
		Sortable.create($("#inventoryExtensions")[0], config);
	}
	
	setupFileUpload(){
		$("#appFile").fileinput({
			overwriteInitial: false,
			uploadUrl: "PreviewApp",
			uploadAsync: true,
			maxFileCount: 1,
			allowedFileExtensions: ['jar'],
			previewFileExtSettings: { 
				'jar': function(ext) {
					return ext.match(/(jar)$/i);
				}
			},
			preferIconicPreview: true,
			previewFileIconSettings: {
				'jar': '<i class="fa fa-file-archive-o text-primary"></i>',
			},
		}).on("filebatchselected", function(event, files) {
			$("#appFile").fileinput("upload");
		});
	}
	
	createCard(name, author, url){
		const li = document.createElement("li");
		li.className="list-group-item";
		li.innerHTML = `
				<div class="appCard row">
					<div class="col-md-1 handle-container">
						<div class="handle">
							<i class="fa-solid fa-grip-lines handle"></i>
						</div>
					</div>
					<div class="col-md-8">
						<b>${name}</b><br> by : ${author} <br> link:
						<a href="${url}">${url}</a>
					</div>
					<div class="col-md-2 handle-container">
						<div class="handle">
							<input type="checkbox" data-size="mini" data-toggle="switch"/>
						</div>
					</div>
					<div class="col-md-1 handle-container">
						<div class="handle">
						<i class="fa fa-trash"></i>
						</div>
					</div>
				</div>`
		return li;
	}
	addCard(id, name, author, url){
		$(id).append(this.createCard(name, author, url));
	}
	
}

const apps = new AppStore();
apps.setupFileUpload();
apps.setupSortableLists();
apps.addCard("#assessmentExtensions", "Procyon lotor Extension", "Rocky", "https://www.google.com");
apps.addCard("#assessmentExtensions", "Test3", "testUse2", "https://www.google.com");
apps.addCard("#verificationExtensions", "Test1", "testUser", "https://www.google.com");
apps.addCard("#vulnerabilityExtensions", "Test3", "testUse2", "https://www.google.com");
apps.addCard("#vulnerabilityExtensions", "Test1", "testUser", "https://www.google.com");
apps.addCard("#verificationExtensions", "Test3", "testUse2", "https://www.google.com");
$('input[type="checkbox"]').bootstrapSwitch("size", "mini");
