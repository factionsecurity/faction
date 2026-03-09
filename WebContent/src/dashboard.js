import 'jquery'
import 'datatables.net'
import 'bootstrap'
import SUNEDITOR from 'suneditor'
import 'datatables.net-bs'
require('suneditor/dist/css/suneditor.min.css');
import suneditor from 'suneditor';
import SunEditor from 'suneditor/src/lib/core'

function accept(nid, el){
  $.post("Dashboard", "action=gotIt&nid=" + nid).done(function(){
    let table=$('#notify').DataTable();
    table.row( $(el).parents('tr') ).remove().draw();
  });
}
    
$(function(){

	$("#clearNotifications").click(() =>{
		console.log("click");
		$.get("clearNotifications").done( () => {
    	let table=$('#notify').DataTable();
    	table.clear().draw();
		});
	})
      
  let noteConfig = {
    mode: "balloon",
    minHeight: 200,
    width: "100%",
    height: "auto"

  }
    let editors = $("[id^=editor]")
    editors.each( (index, editor) => {
      suneditor.create(editor.id, noteConfig).disable()
    })

      $(".delete").click( (event) => {
        accept(event.target.dataset.id, event.target)
      })
    
    	 $('#vqueue').DataTable({
             "paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false,
             "order": [0, 'asc' ],
             "columns": [
				 {"width": "60px"},
				 null,
				 null,
				 null,
			 ]
           });
    	 $('#aqueue').DataTable({
             "paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false,
             "order": [0, 'asc' ],
             "columns": [
				 {"width": "60px"},
				 {"width": "60px"},
				 null,
				 { "width": "70px"}
			 ]
           });
    	 $('#notify').DataTable({
             "paging": true,
             "lengthChange": false,
             "searching": true,
             "ordering": true,
             "info": true,
             "autoWidth": false,
             "order": [0, 'desc' ]
           });
           
    	 
    	 $('#aqueue tbody').on( 'click', 'tr', function () {
    		 let data = $('#aqueue').DataTable().row( this ).data();
    		 document.location="SetAssessment?id="+ data[4];
    	 });
    	 $('#vqueue tbody').on( 'click', 'tr', function () {
    		 let data = $('#vqueue').DataTable().row( this ).data();
    		 document.location="Verifications?id=" + data[4];
    	 });
		$.get('../services/getAssessments', function(data){
				let assessments=data.assessments;
				let table = $("#aqueue").DataTable();
				assessments.forEach(function(assessment){
					table.row.add([assessment[2], assessment[1], assessment[0], assessment[3], assessment[4]]).draw( false );
				});
		});
		$.get('../services/getVerifications', function(data) {
			let table = $("#vqueue").DataTable();
			let verifications = data.verifications;
			verifications.forEach(function(verification){
				table.row.add([verification[2], verification[0], verification[4], verification[5], verification[3]]).draw(false);
			});
			updateColors();
		});

    });