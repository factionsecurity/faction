import 'jquery';
import 'datatables.net';
import 'datatables.net-bs';
import 'bootstrap';
import 'jquery-ui';
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
            "order": [[5, "desc"]], // Sort by start date by default
            "pageLength": 25,
            "columnDefs": [
                {
                    "targets": [8], // Findings column
                    "searchable": false,
                    "orderable": false
                }
            ]
        });
    }
    
    // Date range picker configuration
    var start = moment().subtract(29, 'days');
    var end = moment();
    
    function cb(start, end) {
        $('#daterange-text').html(start.format('MMM D, YYYY') + ' - ' + end.format('MMM D, YYYY'));
        $('#startDate').val(start.format('YYYY-MM-DD'));
        $('#endDate').val(end.format('YYYY-MM-DD'));
    }
    
    $('#daterange-btn').daterangepicker({
        startDate: start,
        endDate: end,
        ranges: {
            'Today': [moment(), moment()],
            'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
            'Last 7 Days': [moment().subtract(6, 'days'), moment()],
            'Last 30 Days': [moment().subtract(29, 'days'), moment()],
            'This Month': [moment().startOf('month'), moment().endOf('month')],
            'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')],
            'This Year': [moment().startOf('year'), moment()],
            'All Time': [moment('2010-01-01'), moment()]
        },
        opens: 'left',
        buttonClasses: ['btn', 'btn-sm'],
        applyClass: 'btn-primary',
        cancelClass: 'btn-default',
        separator: ' to ',
        locale: {
            applyLabel: 'Submit',
            cancelLabel: 'Cancel',
            fromLabel: 'From',
            toLabel: 'To',
            customRangeLabel: 'Custom',
            weekLabel: 'W',
            daysOfWeek: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
            monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
            firstDay: 1
        }
    }, cb);
    
    // Set initial date range if values exist
    if ($('#startDate').val() && $('#endDate').val()) {
        var existingStart = moment($('#startDate').val());
        var existingEnd = moment($('#endDate').val());
        $('#daterange-btn').data('daterangepicker').setStartDate(existingStart);
        $('#daterange-btn').data('daterangepicker').setEndDate(existingEnd);
        cb(existingStart, existingEnd);
    } else {
        cb(start, end);
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