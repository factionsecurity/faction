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
		
		let terms = [];
		if(pastDue){
			terms.push("Past Due")
		}
		if(almostDue){
			terms.push("Approaching")
			terms.push("Almost")
		}
		if(inRetest){
			terms.push("In Retest")
		}
		if(completedRetest){
			terms.push("Passed")
			terms.push("Failed")
		}
		if(terms.length == 0){
			vulnQueue.columns([7]).search("").draw();
		}else{
			let regex = terms.join("|");
			vulnQueue.columns([7]).search(regex, true, false).draw();
		}
		
	});
	
	function setUpEdits(){
		$('#queue tbody').off('click', 'tr');
		$('#queue tbody').on('click', 'tr', function() {
			let vid = `${$(this).data("vulnid")}`
			 if(vid[0] == "-"){
				 vid=vid.replace("-","");
				 document.location="VerificationEdit?searchId=" + vid;
			 }else{
				 document.location="Remediation?searchId=" + vid;
			 }
		});
		
	}
	setUpEdits();

	
	vulnQueue.on('draw', ()=>{
		setUpEdits();
	});
});