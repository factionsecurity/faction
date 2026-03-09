import * as $ from 'jquery'

function accept(nid, el) {
	$.post("Dashboard", "action=gotIt&nid=" + nid).done(function() {
		table = $('#notify').DataTable();
		table.row($(el).parents('tr')).remove().draw();
	});


}

$(function() {

	$('#vqueue').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": false,
		"order": [0, 'asc']
	});
	$('#aqueue').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": false,
		"order": [0, 'asc'],
		"columnDefs": [
			null,
			null,
			null,
			{ "width": "3px" }
		]
	});
	$('#notify').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": false,
		"order": [0, 'desc']
	});

	$('#aqueue tbody').on('click', 'tr', function() {
		data = $('#aqueue').DataTable().row(this).data();
		document.location = "SetAssessment?id=" + data[4];
	});
	$('#vqueue tbody').on('click', 'tr', function() {
		data = $('#vqueue').DataTable().row(this).data();
		document.location = "Verifications?id=" + data[4];
	});



	/*$.get('../services/getAssessments', function(json) {
		assessments = json.assessments;
		var table = $("#aqueue").DataTable();
		for (i = 0; i < json.count; i++) {
			table.row.add([assessments[i][2], assessments[i][1], assessments[i][0], assessments[i][3], assessments[i][4]]).draw(false);
		}
	});*/
	$.get('../services/getVerifications', function(json) {
		//json = JSON.parse(data);
		verifications = json.verifications;
		var table = $("#vqueue").DataTable();
		for (i = 0; i < json.count; i++) {
			table.row.add([verifications[i][2], verifications[i][0], verifications[i][4], verifications[i][5], verifications[i][3]]).draw(false);
		}
		colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
		$("th:contains('Vulnerabilities')").css("width", "20px");
		$("th:contains('Severity')").css("width", "20px");
		$("th:contains('Start')").css("width", "50px");
		$("th:contains('ID')").css("width", "50px");
	});
});