<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<div class="fsgannt"></div>

<script>
$(function(){
	$(".fsgannt").gantt({
		source: "ajax/data.json",
		scale: "weeks",
		minScale: "weeks",
		maxScale: "months",
		onItemClick: function(data) {
			alert("Item clicked - show some details");
		},
		onAddClick: function(dt, rowId) {
			alert("Empty space clicked - add an item!");
		},
		onRender: function() {
			console.log("chart rendered");
		}
	});
	
});
</script>