import '../loading/js/jquery-loading';
require('./calendar.css');
require('select2/dist/css/select2.min.css')
require('daterangepicker/daterangepicker.css');
import 'jquery';
import 'datatables.net';
import 'datatables.net-bs'   ;
import '../scripts/fileupload/js/fileinput.min';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-confirm';
import '../scripts/jquery.autocomplete.min';
import * as moment from 'moment';
import 'daterangepicker';
import 'select2';

global.calendar = {};
global._token = $("#_token")[0].value;

    $(function () {
        const defaultStart = new Date((new Date()).setDate(1));
        let defaultEnd = new Date((new Date()).setDate(1));
        defaultEnd.setDate(defaultEnd.getDate() + 31);
    	$(".select2").select2();
			$('#reservation').daterangepicker({
				"locale": {

					"format": "MM/DD/YYYY",
					"separator": " to ",
					"applylabel": "apply",
					"cancellabel": "cancel",
					"fromlabel": "from",
					"tolabel": "to",
					"customrangelabel": "custom",
					"weeklabel": "w",
				
					"firstday": 1
				},
				"weekstart": 5,
				"showweeknumbers": true,
				"startDate": defaultStart,
                "endDate" : defaultEnd

			});
			$('#ooo').daterangepicker({
				"locale": {

					"format": "MM/DD/YYYY",
					"separator": " to ",
					"applylabel": "apply",
					"cancellabel": "cancel",
					"fromlabel": "from",
					"tolabel": "to",
					"customrangelabel": "custom",
					"weeklabel": "w",
					"firstday": 1
				},
				"weekstart": 5,
				"showweeknumbers": true,
                "startDate": defaultStart,
                "endDate" : defaultEnd

			});
		global.calendar = new FullCalendar.Calendar(document.getElementById("calendar"), {
            header: {
              left: 'prev,next today',
              center: 'title',
              right: 'month,agendaWeek,agendaDay'
            },
            buttonText: {
              today: 'today',
              month: 'month',
              week: 'week',
              day: 'day'
            },editable: true,
            droppable: true, // this allows things to be dropped onto the calendar !!!
            eventDrop: function (event, delta) { // this function is called when something is dropped
				var range= $("#ooo").val();
				var start=new Date(range.split(" to ")[0]);
				var end=new Date(range.split(" to ")[1]);
				start.setDate(start.getDate() + event.delta.days);
				end.setDate(end.getDate() + event.delta.days);
				var startStr = (start.getMonth()+1) + "/" + start.getDate() + "/" + start.getFullYear();
				var endStr = (end.getMonth()+1) + "/" + end.getDate() + "/" + end.getFullYear();
				$("#ooo").val( startStr + " to " + endStr);
				
              },
            eventResize: function(event, jsEvent, ui, view){
				var range= $("#ooo").val();
				var start=new Date(range.split(" to ")[0]);
				var end=new Date(range.split(" to ")[1]);
				start.setDate(start.getDate() + event.startDelta.days);
				end.setDate(end.getDate() + event.endDelta.days);
				var startStr = (start.getMonth()+1) + "/" + start.getDate() + "/" + start.getFullYear();
				var endStr = (end.getMonth()+1) + "/" + end.getDate() + "/" + end.getFullYear();
				$("#ooo").val( startStr + " to " + endStr);
              },
              eventRender(info){
                  console.log(info);
              },
           eventDidMount(eventInfo){
               let event = eventInfo.event;
               let el = eventInfo.el;
               console.log(el);
                $(el).find(".fc-event-main-frame").append("<i class='glyphicon glyphicon-"+event.extendedProps.icon+"'></i>&nbsp;&nbsp;");
           },
          eventClick: function(calEvent, jsEvent, view) {
                  // change the border color just for fun
                  //$(this).css('border-color', 'red');
                  let event = calEvent.event;
                  if(event.extendedProps.icon == "asterisk"){
                	  $.confirm({
                		  title: "Are you sure?",
                		  type:"yellow",
                		  content: "Delete this event",
                		  buttons: {
                			  "yes" : function(resp){
                				  let id=event.id.replace("o","");
                            	  	let post="action=delete";
                            	  	post+="&oid="+id;
                            	  	post+="&_token=" + _token;
                            	  	$.post("Calendar", post).done((postResp) => {
                                            if(postResp.result == "success"){
                                                    alertRedirect(postResp);
                                            }else{
                                                global._token = postResp.token;
                                                    $.confirm({title:data.message || data.response,content: data.message || data.response});
                                            }
                                	});
                			  },
                			  "cancel":function(){return;}
                		  }
                	  });
                  }

              }

            });
	global.calendar.render();
       $("#search").on('click', function(){
    	   $(".content").loading({overlay: true, base: 0.3});
    	   
	       calendar.removeAllEvents();
    	   let range = $("#reservation").val();
		   let sdate = range.split("to")[0];
		   let edate = range.split("to")[1];
           let post="action=search";
           post+="&start="+sdate.trim();
           post+="&end="+edate.trim();
           post+="&userid="+$("#user").val();
           post+="&team="+$("#team").val();
           getAssessments(post);
           
          });
       $("#ooo, #userooo, #title").on('change', function (){
    	   
		   calendar.removeAllEvents();
    	   let range = $("#ooo").val();
		   let s = range.split("to")[0];
		   let e =range.split("to")[1];
		   if(s.trim() != "" && e.trim() != ""){
			   
			   e = new Date(e);
			   e.setDate(e.getDate() + 1); 
			   let t = $("#title").val();
	    	   var event = {
	    	    	    id : -1,
			            allDay:true,
			            title: t,
			            start: new Date(s),
			            editable:true,
			            end: e,
			            color : edit_color,
			            extendedProps: { icon: "star-empty"}
					};
    	        calendar.addEvent(event, true);

		   }
		   let uid = $("#userooo").val();
		   if(uid != -1){
			   
			   let post="action=search";
			   post += "&userid=" + $("#userooo").val();
			   getAssessments(post);
		   }
           
    	   
       });

       $("#add").on('click', function(){
    	   let range = $("#ooo").val();
		   let s = range.split("to")[0];
		   let e =range.split("to")[1];
		   e = new Date(e);
		   e.setDate(e.getDate()); 
		   let t = $("#title").val();
		   let u = $("#userooo").val();
		   let post="action=add";
		   post+="&start="+s;
		   post+="&end="+(e.getMonth() +1)+ "/" + e.getDate() + "/" + e.getFullYear();
		   post+="&title=" + t;
		   post+="&userid=" + u;
		   post+="&_token=" + _token;
		   $.post("Calendar", post).done((postResp)=>{
               if(postResp.result == "success"){
			        alertRedirect(postResp);
               }else{
                   global._token = postResp.token;
    		        $.confirm({title:data.message || data.response,content: data.message || data.response});
               }
			   	
		   });

       });

      });

    function getAssessments(post){
    	$(".content").loading({overlay: true, base: 0.3});
    	$.post("Calendar",post).done( function (jsonResp) {

                $(".content").loading({ destroy: true });
                let outputdata = [];
                let gnattdata = [];

                jsonResp.assessments.forEach(function (a) {
                    let s = a.start;
                    let tmpdate = new Date(a.end);
                    tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
                    let e = tmpdate;
                    let t = a.username + " - " + a.name;
                    if (s != 'null' && e != 'null') {

                        let event = {
                            allDay: true,
                            title: t,
                            start: new Date(s),
                            editable: false,
                            end: new Date(e),
                            color: asmt_color,
                            icon: "th-list"
                        };
                        calendar.addEvent(event, true);

                    }


                    if (typeof gnattdata[a.username] == "undefined") {
                        gnattdata[a.username] = [];
                    }
                    gnattdata[a.username].push({
                        from: new Date(s),
                        to: new Date(e),
                        label: "<span class='fa fa-th-list'></span>&nbsp;" + a.name,
                        customClass: "assessmentClass"
                    });

                });
                let first = 0;
                for (var key in gnattdata) {
                    if (first == 0) {
                        first = 1;
                        outputdata.push({
                            name: "Assessments",
                            desc: key,
                            values: gnattdata[key]
                        });
                    } else {
                        outputdata.push({
                            name: "",
                            desc: key,
                            values: gnattdata[key]
                        });
                    }

                }



                gnattdata = [];
                jsonResp.ooo.forEach(function (a) {
                    let s = a.start;
                    let tmpdate = new Date(a.end);
                    tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
                    let e = tmpdate;
                    let t = a.title;
                    if (s != 'null' && e != 'null') {

                        var event = {
                            allDay: true,
                            title: a.username + " - " + t,
                            start: new Date(s),
                            editable: false,
                            end: new Date(e),
                            color: ooo_color,
                            extendedProps: {icon: "asterisk"},
                            id: "o" + a.id
                        };
                        calendar.addEvent(event, true);

                    }

                    if (typeof gnattdata[a.username] == "undefined") {
                        gnattdata[a.username] = [];
                    }
                    gnattdata[a.username].push({
                        from: new Date(s),
                        to: new Date(e),
                        label: "<span class='fa fa-asterisk'></span>&nbsp;" + a.title,
                        customClass: "oooClass"
                    });

                });

                first = 0;
                for (let ganttKey in gnattdata) {
                    if (first == 0) {
                        first = 1;
                        outputdata.push({
                            name: "Reserved Time",
                            desc: ganttKey,
                            values: gnattdata[ganttKey]
                        });
                    } else {
                        outputdata.push({
                            name: "",
                            desc: ganttKey,
                            values: gnattdata[ganttKey]
                        });
                    }

                }

                gnattdata = [];
                jsonResp.verifications.forEach(function (a) {
                    let s = a.start;
                    let tmpdate = new Date(a.end);
                    tmpdate = tmpdate.setDate(tmpdate.getDate() + 1);
                    let e = tmpdate;
                    let t = a.username + " - " + a.vuln;
                    if (s != 'null' && e != 'null') {

                        let event = {
                            allDay: true,
                            title: t,
                            start: new Date(s),
                            editable: false,
                            end: new Date(e),
                            color: ver_color,
                            icon: "ok",
                        };
                        calendar.addEvent(event, true);

                    }

                    if (typeof gnattdata[a.username] == "undefined") {
                        gnattdata[a.username] = [];
                    }
                    gnattdata[a.username].push({
                        from: new Date(s),
                        to: new Date(e),
                        label: "<span class='fa fa-ok'></span>&nbsp;" + a.vuln,
                        customClass: "verificationClass"
                    });

                });

                first = 0;
                for (let ganttKey in gnattdata) {
                    if (first == 0) {
                        first = 1;
                        outputdata.push({
                            name: "Verifications",
                            desc: ganttKey,
                            values: gnattdata[ganttKey]
                        });
                    } else {
                        outputdata.push({
                            name: "",
                            desc: ganttKey,
                            values: gnattdata[ganttKey]
                        });
                    }

                }


                $(".fsgannt").gantt({
                    source: outputdata,
                    scale: "days",
                    minScale: "days",
                    maxScale: "months",
                    onItemClick: function (data) { return; },
                    onAddClick: function (dt, rowId) { return; },
                    onRender: function () { return; }
                });

            });

    }
