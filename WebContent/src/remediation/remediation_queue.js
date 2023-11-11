import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';

$(function() {
	$("#showall").change(function() {
		var checked = $("#showall").is(":checked");
		document.location = "RemediationQueue?all=" + checked;

	});
	$("#queue").DataTable({ 'iDisplayLength': 25 });

});