
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap'
import 'jquery-ui';
import 'jquery-confirm';

function goTo(id) {
	document.location = `Verifications?id=${id}`;
}
$(function() {
	$('#verificationQueue').DataTable({
		"paging": true,
		"lengthChange": false,
		"searching": true,
		"ordering": true,
		"info": true,
		"autoWidth": true,
		"order": [[4, "asc"]]
	});
});