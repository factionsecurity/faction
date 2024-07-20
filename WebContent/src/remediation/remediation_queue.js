import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';

$(function() {
	$("#showAll").change(function() {
		const checked = $("#showAll").is(":checked");
		document.location = `RemediationQueue?all=${all}`;
	});
	let vulnQueue = $("#queue").DataTable({ 'iDisplayLength': 10 });
	$("#showAlmostDue,#showPastDue,#showInRetest,#showCompletedRetest").change(function() {
		const almostDue = $("#showAlmostDue").is(":checked");
		const pastDue = $("#showPastDue").is(":checked");
		const inRetest = $("#showInRetest").is(":checked");
		const completedRetest = $("#showCompletedRetest").is(":checked");
	});
});