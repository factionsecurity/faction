import 'jquery';
import 'datatables.net';
import 'datatables.net-bs'   ;
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';


    global.goTo = function goTo(id){
    	document.location="SetAssessment?id="+id;
    };

    // Column indexes in the queue table
    var COL_START = 3;
    var COL_END = 4;
    var COL_STATUS = 6;

    var COMPLETED = 'Completed';
    // Statuses Assessment.getStatus() derives. Always offered so the filter reads
    // the same whatever happens to be in the queue right now; anything custom set
    // on an assessment is added on top of these.
    var DERIVED_STATUSES = ['Scheduled', 'In Progress', 'Past Due', COMPLETED];

    // The queue only loads completed assessments when asked to, so picking the
    // Completed status has to go back to the server for them.
    function showingCompleted(){
    	return $('#queueFilters').data('showcompleted') === true;
    }

    function reloadWithCompleted(){
    	var params = ['showCompleted=true', 'status=' + encodeURIComponent(COMPLETED)];
    	var from = $('#fromDateFilter').val();
    	var to = $('#toDateFilter').val();
    	if(from)
    		params.push('from=' + encodeURIComponent(from));
    	if(to)
    		params.push('to=' + encodeURIComponent(to));
    	document.location = 'AssessmentQueue?' + params.join('&');
    }

    function queryParam(name){
    	var match = new RegExp('[?&]' + name + '=([^&]*)').exec(document.location.search);
    	return match ? decodeURIComponent(match[1].replace(/\+/g, ' ')) : '';
    }

    // Dates render as yyyy-MM-dd, so plain string comparison is chronological.
    // Missing dates are treated as unbounded so an assessment without a
    // schedule never silently drops out of the queue.
    function rowMatchesDateRange(start, end, from, to){
    	if(from && end && end < from)
    		return false;
    	if(to && start && start > to)
    		return false;
    	return true;
    }

    $(function () {
        var table = $('#assessment_queue').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "order": [[ 3, "asc" ]],
            columnDefs:[
            	{"targets":[5,7],
            		"searchable":false,
            		"orderable":false}
            ]

          });

            buildStatusFilter(table);
            restoreFiltersFromUrl();

            $.fn.dataTable.ext.search.push(function(settings, data){
            	if(settings.nTable.id !== 'assessment_queue')
            		return true;

            	var status = $('#statusFilter').val();
            	if(status && $.trim(data[COL_STATUS]) !== status)
            		return false;

            	var from = $('#fromDateFilter').val();
            	var to = $('#toDateFilter').val();
            	if(!from && !to)
            		return true;

            	return rowMatchesDateRange($.trim(data[COL_START]), $.trim(data[COL_END]), from, to);
            });

            // The table was drawn before the filter existed, so apply anything
            // restored from the URL now.
            table.draw();

            $('#statusFilter').on('change', function(){
            	if($(this).val() === COMPLETED && !showingCompleted()){
            		reloadWithCompleted();
            		return;
            	}
            	table.draw();
            });

            $('#fromDateFilter, #toDateFilter').on('change', function(){
            	table.draw();
            });

            $('#rangeDropdown li a').on('click', function(e){
            	e.preventDefault();
            	applyQuickRange($(this).data('range'), $(this).text().trim());
            	table.draw();
            });

            $('#clearFilters').on('click', function(){
            	// Clearing returns to the default queue, which means dropping the
            	// completed assessments that were pulled in for the filter.
            	if(showingCompleted()){
            		document.location = 'AssessmentQueue';
            		return;
            	}
            	$('#statusFilter').val('');
            	$('#fromDateFilter').val('');
            	$('#toDateFilter').val('');
            	$('#rangeLabel').text('Select Range');
            	table.draw();
            });

            updateStatus();
            $('#assessment_queue').on( 'draw.dt', function () {
            	updateStatus();
            });

      });

    // The derived statuses are always listed; anything else in the loaded rows is
    // a custom status configured for the instance and gets appended.
    function buildStatusFilter(table){
    	var $select = $('#statusFilter');
    	if($select.length === 0)
    		return;

    	var statuses = DERIVED_STATUSES.slice();
    	table.column(COL_STATUS).data().each(function(value){
    		var status = $.trim(value);
    		if(status !== '' && statuses.indexOf(status) === -1)
    			statuses.push(status);
    	});

    	statuses.forEach(function(status){
    		$select.append($('<option></option>').attr('value', status).text(status));
    	});
    }

    // Filters survive the reload that pulls in completed assessments.
    function restoreFiltersFromUrl(){
    	var status = queryParam('status');
    	if(status)
    		$('#statusFilter').val(status);

    	var from = queryParam('from');
    	if(from)
    		$('#fromDateFilter').val(from);

    	var to = queryParam('to');
    	if(to)
    		$('#toDateFilter').val(to);
    }

    function formatDate(date){
    	var month = date.getMonth() + 1;
    	var day = date.getDate();
    	return date.getFullYear() + '-' + (month < 10 ? '0' : '') + month + '-' + (day < 10 ? '0' : '') + day;
    }

    function applyQuickRange(range, label){
    	var today = new Date();
    	today.setHours(0, 0, 0, 0);
    	var from = new Date(today.getTime());
    	var to = new Date(today.getTime());

    	switch(range){
    		case 'today':
    			break;
    		case '7days':
    			to.setDate(to.getDate() + 6);
    			break;
    		case 'thisweek':
    			from.setDate(from.getDate() - from.getDay());
    			to = new Date(from.getTime());
    			to.setDate(to.getDate() + 6);
    			break;
    		case '30days':
    			to.setDate(to.getDate() + 29);
    			break;
    		case 'month':
    			from.setDate(1);
    			to = new Date(from.getFullYear(), from.getMonth() + 1, 0);
    			break;
    		case 'lastmonth':
    			from = new Date(today.getFullYear(), today.getMonth() - 1, 1);
    			to = new Date(today.getFullYear(), today.getMonth(), 0);
    			break;
    		case 'year':
    			from = new Date(today.getFullYear(), 0, 1);
    			to = new Date(today.getFullYear(), 11, 31);
    			break;
    		case 'alltime':
    			$('#fromDateFilter').val('');
    			$('#toDateFilter').val('');
    			$('#rangeLabel').text(label);
    			return;
    		default:
    			return;
    	}

    	$('#fromDateFilter').val(formatDate(from));
    	$('#toDateFilter').val(formatDate(to));
    	$('#rangeLabel').text(label);
    }

    // The report/peer-review icons are only colored for rows currently on screen,
    // so this reruns on every redraw. The service response is cached so filtering
    // and searching don't hit the server on each redraw.
    var statusCache = null;

    function updateStatus(){
    	  if(statusCache){
    		  applyStatus(statusCache);
    		  return;
    	  }
    	  $.get("../service/status" + (showingCompleted() ? "?showCompleted=true" : "")).done(function(data){
    		  statusCache = data;
    		  applyStatus(data);
          });
    }

    function applyStatus(data){
              data.forEach(function(d){
            	  if(!$("#status"+ d.id).is(':visible'))
            		  return;

  				var span = $("#status"+ d.id).children();

  				if(d.report){
  					var reportStyle = $(span[0]).attr('class');
  					reportStyle = reportStyle.replace("gray","blue");
  					$(span[0]).attr('class', reportStyle);
  				}
  				if(d.submitted){
  					var submitStyle = $(span[1]).attr('class');
  					submitStyle = submitStyle.replace("gray","orange");
  					$(span[1]).attr('class', submitStyle);
  				}
  				if(d.prCompleted){
  					var prStyle = $(span[2]).attr('class');
  					prStyle = prStyle.replace("gray","green");
  					$(span[2]).attr('class', prStyle);
  				}

              });
    }
