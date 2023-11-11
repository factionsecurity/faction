import 'jquery';
import 'datatables.net';
import 'datatables.net-bs'   ;
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
    
    
    global.goTo = function goTo(id){
    	document.location="SetAssessment?id="+id;
    };
    $(function () {
        $('#assessment_queue').DataTable({
            "paging": true,
            "lengthChange": false,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": true,
            "order": [[ 3, "asc" ]],
            columnDefs:[
            	{"targets":[5,6],
            		"searchable":false,
            		"orderable":false}
            ]
          
          });
            
            updateStatus();
            $('#assessment_queue').on( 'page.dt', function () {
            	updateStatus();
            });
            
      });
    function updateStatus(){
    	  $.get("../service/status").done(function(data){
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
          });
    }