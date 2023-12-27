require('bootstrap-switch/dist/css/bootstrap3/bootstrap-switch.min.css');
import '../scripts/fileupload/js/fileinput.min';
import 'jquery';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import 'bootstrap-switch';
import Sortable from 'sortablejs/modular/sortable.core.esm.js';



class AppStore {
	constructor() {

	}

	setupSortableLists() {
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

			}
		};
		Sortable.create($("#assessmentExtensions")[0], config);
		config.group = 'vulnerability';
		Sortable.create($("#vulnerabilityExtensions")[0], config);
		config.group = 'verification';
		Sortable.create($("#verificationExtensions")[0], config);
		config.group = 'inventory';
		Sortable.create($("#inventoryExtensions")[0], config);
	}

	setupFileUpload() {
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

	createCard(name, version, author, url) {
		const li = document.createElement("li");
		li.className = "list-group-item";
		li.innerHTML = `
				<div class="appCard row">
					<div class="col-md-1 handle-container" style="min-width: 100px">
						<div class="handle">
							<i class="fa-solid fa-grip-lines handle"></i>
					 		<img class="appLogo-small" src="../app-default.png"/>
						</div>
					</div>
					<div class="col-md-8 handle-container">
						<div class="handle">
							<b>${name}</b> Version: ${version}<br> by : ${author} <br> link:
							<a href="${url}">${url}</a>
						</div>
					</div>
					<div class="col-md-2 handle-container pull-right" style="width:120px">
						<div class="handle">
							<input type="checkbox" data-size="mini" data-toggle="switch"/>
							&nbsp;&nbsp;<i class="fa fa-trash"></i>
						</div>
					</div>
				</div>`
		return li;
	}
	addCard(id, name, version, author, url) {
		$(id).append(this.createCard(name, version, author, url));
	}

}

const apps = new AppStore();
apps.setupFileUpload();
apps.setupSortableLists();
apps.addCard("#assessmentExtensions", "Procyon lotor Extension",  "1.0","Rocky", "https://www.google.com");
apps.addCard("#assessmentExtensions",  "Test3",  "1.0","testUse2", "https://www.google.com");
apps.addCard("#verificationExtensions",  "Test1",  "1.0","testUser", "https://www.google.com");
apps.addCard("#vulnerabilityExtensions",  "Test3",  "1.0","testUse2", "https://www.google.com");
apps.addCard("#vulnerabilityExtensions",  "Test1",  "1.0","testUser", "https://www.google.com");
apps.addCard("#verificationExtensions",  "Test3",  "1.0","testUse2", "https://www.google.com");
$('input[type="checkbox"]').bootstrapSwitch("size", "mini");
