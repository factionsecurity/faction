import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
import 'jquery-ui/ui/widgets/datepicker';
import 'jquery-ui/themes/base/core.css';
import 'jquery-ui/themes/base/datepicker.css';
import 'jquery-ui/themes/base/theme.css';
import 'jquery-confirm';
import moment from 'moment';
import 'daterangepicker';
import 'daterangepicker/daterangepicker.css';
import Chart from 'chart.js/auto';

// Make moment available globally for daterangepicker
window.moment = moment;

$(function() {
    // Initialize DataTables
    if ($('#searchResults').length > 0) {
        $('#searchResults').DataTable({
            "paging": true,
            "lengthChange": true,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": false,
            "order": [[6, "desc"]], // Sort by Start date by default
            "pageLength": 25,
            "columnDefs": [
                {
                    "targets": [0, 10], // Action and Findings columns
                    "searchable": false,
                    "orderable": false
                }
            ]
        });
    }

    // Vulnerabilities tab table
    if ($('#vulnResults').length > 0) {
        $('#vulnResults').DataTable({
            "paging": true,
            "lengthChange": true,
            "searching": true,
            "ordering": true,
            "info": true,
            "autoWidth": false,
            "order": [[7, "desc"]], // Sort by Opened date by default
            "pageLength": 25,
            "columnDefs": [
                {
                    "targets": [0], // Action column
                    "searchable": false,
                    "orderable": false
                }
            ]
        });
    }

    // Vulnerability detail slide-out panel (mirrors the assessment history panel).
    // Move the panel to <body> so position:fixed is relative to the viewport
    // regardless of any transformed ancestor.
    $("#vulnDetailPanel, #vulnDetailOverlay").appendTo("body");

    function openVulnDetail(vulnid) {
        $("#vulnDetailBody").html('<div style="text-align:center;padding:40px;"><i class="fa fa-spinner fa-spin fa-2x"></i></div>');
        $("#vulnDetailOverlay").show();
        $("#vulnDetailPanel").addClass("open");
        $.get("ManagerDashboardVulnDetail", "vulnid=" + vulnid).done(function(resp) {
            if (resp && typeof resp == "object") {
                $("#vulnDetailBody").html('<div class="alert alert-danger">' + (resp.message || "Unable to load finding details.") + '</div>');
            } else {
                $("#vulnDetailBody").html(resp);
            }
        }).fail(function() {
            $("#vulnDetailBody").html('<div class="alert alert-danger">Failed to load finding details.</div>');
        });
    }

    function closeVulnDetail() {
        $("#vulnDetailPanel").removeClass("open");
        $("#vulnDetailOverlay").hide();
    }

    // Clicking a vulnerability row opens the detail panel, except when the click
    // lands on a link inside the row (e.g. the Open Assessment icon).
    $(document).on("click", ".vuln-detail-row", function(e) {
        if ($(e.target).closest("a").length)
            return;
        var vulnid = $(this).data("vulnid");
        if (vulnid)
            openVulnDetail(vulnid);
    });

    $(document).on("click", "#vulnDetailClose, #vulnDetailOverlay", closeVulnDetail);
    $(document).on("keydown", function(e) {
        if (e.key === "Escape")
            closeVulnDetail();
    });

    // Date formatting
    var displayFormat = 'mm/dd/yy';
    var submitFormat = 'yy-mm-dd';
    
    function updateHiddenFields() {
        var startDateVal = $('#startDateDisplay').val();
        var endDateVal = $('#endDateDisplay').val();
        
        if (startDateVal) {
            var parsedStart = $.datepicker.parseDate(displayFormat, startDateVal);
            if (parsedStart) {
                $('#startDate').val($.datepicker.formatDate(submitFormat, parsedStart));
            }
        }
        
        if (endDateVal) {
            var parsedEnd = $.datepicker.parseDate(displayFormat, endDateVal);
            if (parsedEnd) {
                $('#endDate').val($.datepicker.formatDate(submitFormat, parsedEnd));
            }
        }
    }
    
    function initDatepicker(inputId) {
        var $input = $('#' + inputId);
        if ($input.data('datepicker')) return;

        $input.datepicker({
            dateFormat: displayFormat,
            beforeShow: function(input, inst) {
                if (inst.dpDiv) {
                    inst.dpDiv.addClass('manager-dashboard-datepicker');
                }
            },
            onSelect: function() {
                updateHiddenFields();
                hideError($input);
            }
        });
    }

    initDatepicker('startDateDisplay');
    initDatepicker('endDateDisplay');
    
    function showError(input, message) {
        var errorId = '#' + $(input).attr('id') + 'Error';
        $(errorId).text(message || 'Please enter a valid date').show();
        $(input).closest('.form-group').addClass('has-error');
    }
    
    function hideError(input) {
        var errorId = '#' + $(input).attr('id') + 'Error';
        $(errorId).hide();
        $(input).closest('.form-group').removeClass('has-error');
    }
    
    function validateDate(input) {
        var value = $(input).val().trim();
        if (!value) {
            showError(input, 'Date is required');
            return false;
        }
        
        try {
            var parsed = $.datepicker.parseDate(displayFormat, value);
            if (!parsed || isNaN(parsed.getTime())) {
                showError(input, 'Please enter a valid date (mm/dd/yyyy)');
                return false;
            }
            hideError(input);
            return true;
        } catch(e) {
            showError(input, 'Please enter a valid date (mm/dd/yyyy)');
            return false;
        }
    }
    
    $('#startDateBtn').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $('#startDateDisplay').datepicker('show');
    });

    $('#endDateBtn').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $('#endDateDisplay').datepicker('show');
    });


    // Form validation on submit
    $('form[action="ManagerDashboard"]').on('submit', function(e) {
        var valid = true;
        
        if (!validateDate($('#startDateDisplay'))) {
            valid = false;
        }
        
        if (!validateDate($('#endDateDisplay'))) {
            valid = false;
        }
        
        var startDateVal = $('#startDateDisplay').val();
        var endDateVal = $('#endDateDisplay').val();
        
        if (startDateVal && endDateVal) {
            var start = $.datepicker.parseDate(displayFormat, startDateVal);
            var end = $.datepicker.parseDate(displayFormat, endDateVal);
            if (start > end) {
                showError($('#endDateDisplay'), 'End date must be after start date');
                valid = false;
            }
        }
        
        if (!valid) {
            e.preventDefault();
            return false;
        }
        
        updateHiddenFields();
    });
    
    // Quick range selection
    $('#rangeDropdown li a').on('click', function(e) {
        e.preventDefault();
        var range = $(this).data('range');
        var today = moment();
        var startDate, endDate;
        
        switch(range) {
            case 'today':
                startDate = today.clone();
                endDate = today.clone();
                $('#rangeLabel').text('Today');
                break;
            case 'yesterday':
                startDate = today.clone().subtract(1, 'days');
                endDate = today.clone().subtract(1, 'days');
                $('#rangeLabel').text('Yesterday');
                break;
            case '7days':
                startDate = today.clone().subtract(6, 'days');
                endDate = today.clone();
                $('#rangeLabel').text('Last 7 Days');
                break;
            case '30days':
                startDate = today.clone().subtract(29, 'days');
                endDate = today.clone();
                $('#rangeLabel').text('Last 30 Days');
                break;
            case 'month':
                startDate = today.clone().startOf('month');
                endDate = today.clone().endOf('month');
                $('#rangeLabel').text('This Month');
                break;
            case 'lastmonth':
                startDate = today.clone().subtract(1, 'month').startOf('month');
                endDate = today.clone().subtract(1, 'month').endOf('month');
                $('#rangeLabel').text('Last Month');
                break;
            case 'year':
                startDate = today.clone().startOf('year');
                endDate = today.clone();
                $('#rangeLabel').text('This Year');
                break;
            case 'alltime':
                startDate = moment('2010-01-01');
                endDate = today.clone();
                $('#rangeLabel').text('All Time');
                break;
            default:
                return;
        }
        
        $('#startDateDisplay').val($.datepicker.formatDate(displayFormat, startDate.toDate()));
        $('#endDateDisplay').val($.datepicker.formatDate(displayFormat, endDate.toDate()));
        updateHiddenFields();
        hideError($('#startDateDisplay'));
        hideError($('#endDateDisplay'));
    });

    // Set default date range if values don't exist
    if (!$('#startDateDisplay').val() && !$('#endDateDisplay').val()) {
        var start = moment().subtract(29, 'days');
        var end = moment();
        $('#startDateDisplay').val($.datepicker.formatDate(displayFormat, start.toDate()));
        $('#endDateDisplay').val($.datepicker.formatDate(displayFormat, end.toDate()));
        updateHiddenFields();
        $('#rangeLabel').text('Last 30 Days');
    }
    
    // Auto-refresh statistics every 5 minutes
    setInterval(function() {
        refreshStatistics();
    }, 300000); // 5 minutes
});

function refreshStatistics() {
    // AJAX call to refresh statistics
    $.ajax({
        url: 'ManagerDashboardAjax!getStatistics',
        type: 'GET',
        dataType: 'json',
        success: function(response) {
            // Update assessment statistics
            if (response.assessmentStats) {
                $('.info-box-number').each(function() {
                    var parent = $(this).closest('.info-box');
                    var text = parent.find('.info-box-text').text().toLowerCase();
                    
                    if (text.includes('week') && response.assessmentStats.weekly !== undefined) {
                        $(this).text(response.assessmentStats.weekly);
                    } else if (text.includes('month') && response.assessmentStats.monthly !== undefined) {
                        $(this).text(response.assessmentStats.monthly);
                    } else if (text.includes('year') && response.assessmentStats.yearly !== undefined) {
                        $(this).text(response.assessmentStats.yearly);
                    } else if (text.includes('all time') && response.assessmentStats.total !== undefined) {
                        $(this).text(response.assessmentStats.total);
                    }
                });
            }
            
            // Update vulnerability statistics
            if (response.vulnerabilityStats) {
                // Refresh vulnerability counts
                $('.info-box-content').each(function() {
                    var text = $(this).find('.info-box-text').text().toLowerCase();
                    var number = $(this).find('.info-box-number');
                    
                    if ($(this).closest('.col-md-6').find('h4').text().includes('Vulnerability')) {
                        if (text.includes('week') && response.vulnerabilityStats.weeklyTotal !== undefined) {
                            number.text(response.vulnerabilityStats.weeklyTotal);
                        } else if (text.includes('month') && response.vulnerabilityStats.monthlyTotal !== undefined) {
                            number.text(response.vulnerabilityStats.monthlyTotal);
                        } else if (text.includes('year') && response.vulnerabilityStats.yearlyTotal !== undefined) {
                            number.text(response.vulnerabilityStats.yearlyTotal);
                        } else if (text.includes('all time') && response.vulnerabilityStats.total !== undefined) {
                            number.text(response.vulnerabilityStats.total);
                        }
                    }
                });
                
            }
        },
        error: function(xhr, status, error) {
            console.error('Failed to refresh statistics:', error);
        }
    });
}

// Export functions for global access
global.refreshStatistics = refreshStatistics;