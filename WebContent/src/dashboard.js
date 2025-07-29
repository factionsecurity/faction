import 'jquery'
import 'datatables.net'
import 'bootstrap'
import SUNEDITOR from 'suneditor'
import 'datatables.net-bs'
require('suneditor/dist/css/suneditor.min.css');
import suneditor from 'suneditor';
import SunEditor from 'suneditor/src/lib/core'

// Enhanced Modern Dashboard Functionality
class ModernDashboard {
  constructor() {
    this.initializeAnimations();
    this.initializeNotifications();
    this.initializeDataTables();
    this.initializeEditors();
    this.initializeInteractions();
  }

  // Initialize entrance animations
  initializeAnimations() {
    // Stagger card animations
    $('.modern-card').each((index, card) => {
      $(card).css('animation-delay', `${index * 0.1}s`);
    });

    // Add loading states for tables
    this.showLoadingState();
  }

  // Enhanced notifications functionality
  initializeNotifications() {
    const self = this;
    
    $("#clearNotifications").click(function() {
      // Add confirmation with modern styling
      if (confirm('Are you sure you want to clear all notifications?')) {
        $(this).addClass('loading-shimmer').prop('disabled', true);
        
        $.get("clearNotifications").done(() => {
          let table = $('#notify').DataTable();
          table.clear().draw();
          
          // Show success feedback
          self.showToast('All notifications cleared', 'success');
        }).fail(() => {
          self.showToast('Failed to clear notifications', 'error');
        }).always(() => {
          $(this).removeClass('loading-shimmer').prop('disabled', false);
        });
      }
    });

    // Enhanced delete functionality
    $(document).on('click', '.delete', function() {
      const nid = $(this).data('id');
      const row = $(this).closest('tr');
      
      row.addClass('loading-shimmer');
      self.accept(nid, this);
    });
  }

  // Modern DataTables initialization
  initializeDataTables() {
    const commonOptions = {
      "paging": true,
      "lengthChange": false,
      "searching": true,
      "ordering": true,
      "info": true,
      "autoWidth": false,
      "responsive": true,
      "language": {
        "search": "Search:",
        "lengthMenu": "Show _MENU_ entries",
        "info": "Showing _START_ to _END_ of _TOTAL_ entries",
        "paginate": {
          "first": "First",
          "last": "Last",
          "next": "Next",
          "previous": "Previous"
        }
      },
      "drawCallback": function() {
        // Apply modern styling after draw
        $(this.api().table().container()).find('.pagination').addClass('modern-pagination');
      }
    };

    // Assessment Queue table
    $('#aqueue').DataTable({
      ...commonOptions,
      "order": [0, 'asc'],
      "columns": [
        {"width": "60px"},
        {"width": "60px"},
        null,
        {"width": "70px"}
      ]
    });

    // Verification Queue table
    $('#vqueue').DataTable({
      ...commonOptions,
      "order": [0, 'asc'],
      "columns": [
        {"width": "60px"},
        null,
        null,
        null
      ]
    });

    // Notifications table
    $('#notify').DataTable({
      ...commonOptions,
      "order": [0, 'desc']
    });
  }

  // Initialize rich text editors
  initializeEditors() {
    let noteConfig = {
      mode: "balloon",
      minHeight: 200,
      width: "100%",
      height: "auto",
      buttonList: [
        ['undo', 'redo'],
        ['font', 'fontSize'],
        ['bold', 'underline', 'italic'],
        ['fontColor', 'hiliteColor'],
        ['align', 'list'],
        ['table', 'link', 'image']
      ]
    };

    let editors = $("[id^=editor]");
    editors.each((index, editor) => {
      suneditor.create(editor.id, noteConfig).disable();
    });
  }

  // Initialize modern interactions
  initializeInteractions() {
    const self = this;

    // Row click handlers with modern feedback
    $('#aqueue tbody').on('click', 'tr', function() {
      const data = $('#aqueue').DataTable().row(this).data();
      if (data && data[4]) {
        self.navigateWithLoading("SetAssessment?id=" + data[4]);
      }
    });

    $('#vqueue tbody').on('click', 'tr', function() {
      const data = $('#vqueue').DataTable().row(this).data();
      if (data && data[4]) {
        self.navigateWithLoading("Verifications?id=" + data[4]);
      }
    });

    // Load data with modern loading states
    this.loadAssessments();
    this.loadVerifications();
  }

  // Enhanced accept function with modern feedback
  accept(nid, el) {
    $.post("Dashboard", "action=gotIt&nid=" + nid)
      .done(() => {
        let table = $('#notify').DataTable();
        const row = $(el).parents('tr');
        
        // Animate row removal
        row.fadeOut(300, function() {
          table.row(row).remove().draw();
        });
        
        this.showToast('Notification dismissed', 'success');
      })
      .fail(() => {
        $(el).parents('tr').removeClass('loading-shimmer');
        this.showToast('Failed to dismiss notification', 'error');
      });
  }

  // Load assessments with modern loading
  loadAssessments() {
    $.get('../services/getAssessments')
      .done((data) => {
        let assessments = data.assessments;
        let table = $("#aqueue").DataTable();
        
        assessments.forEach((assessment) => {
          table.row.add([
            assessment[2],
            assessment[1],
            assessment[0],
            assessment[3],
            assessment[4]
          ]).draw(false);
        });
        
        this.hideLoadingState('#aqueue');
      })
      .fail(() => {
        this.showToast('Failed to load assessments', 'error');
        this.hideLoadingState('#aqueue');
      });
  }

  // Load verifications with modern loading
  loadVerifications() {
    $.get('../services/getVerifications')
      .done((data) => {
        let table = $("#vqueue").DataTable();
        let verifications = data.verifications;
        
        verifications.forEach((verification) => {
          table.row.add([
            verification[2],
            verification[0],
            verification[4],
            verification[5],
            verification[3]
          ]).draw(false);
        });
        
        this.updateColors();
        this.hideLoadingState('#vqueue');
      })
      .fail(() => {
        this.showToast('Failed to load verifications', 'error');
        this.hideLoadingState('#vqueue');
      });
  }

  // Show loading state
  showLoadingState() {
    // Only show loading for tables that will be populated via AJAX
    const ajaxTables = ['#aqueue', '#vqueue'];
    
    ajaxTables.forEach(tableId => {
      const $table = $(tableId);
      if ($table.length > 0) {
        const $card = $table.closest('.modern-card');
        const $body = $card.find('.modern-card-body');
        
        $body.append(`
          <div class="loading-overlay" style="position: absolute; top: 0; left: 0; right: 0; bottom: 0;
               background: rgba(26, 35, 50, 0.8); display: flex; align-items: center; justify-content: center;
               backdrop-filter: blur(2px); border-radius: 12px;">
            <div class="loading-spinner" style="width: 40px; height: 40px; border: 3px solid var(--border-color);
                 border-top: 3px solid var(--primary-blue); border-radius: 50%; animation: spin 1s linear infinite;"></div>
          </div>
        `);
      }
    });
  }

  // Hide loading state
  hideLoadingState(selector) {
    const $container = selector ? $(selector).closest('.modern-card') : $('.modern-card');
    $container.find('.loading-overlay').fadeOut(300, function() {
      $(this).remove();
    });
  }

  // Navigate with loading feedback
  navigateWithLoading(url) {
    // Show loading indication
    $('body').append(`
      <div id="navigation-loader" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0;
           background: rgba(11, 20, 38, 0.8); display: flex; align-items: center; justify-content: center;
           z-index: 9999; backdrop-filter: blur(4px);">
        <div style="text-align: center; color: var(--text-primary);">
          <div class="loading-spinner" style="width: 50px; height: 50px; border: 4px solid var(--border-color);
               border-top: 4px solid var(--primary-blue); border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto 16px;"></div>
          <p>Loading...</p>
        </div>
      </div>
    `);
    
    // Navigate after short delay for smooth transition
    setTimeout(() => {
      document.location = url;
    }, 300);
  }

  // Modern toast notifications
  showToast(message, type = 'info') {
    const toastId = 'toast-' + Date.now();
    const typeColors = {
      success: 'var(--success-green)',
      error: 'var(--danger-red)',
      warning: 'var(--warning-orange)',
      info: 'var(--info-cyan)'
    };

    const toast = $(`
      <div id="${toastId}" class="modern-toast" style="position: fixed; top: 20px; right: 20px;
           background: rgba(26, 35, 50, 0.95); color: var(--text-primary); padding: 16px 20px;
           border-radius: 8px; box-shadow: 0 8px 32px var(--shadow-heavy); z-index: 1000;
           border-left: 4px solid ${typeColors[type]}; backdrop-filter: blur(10px);
           transform: translateX(100%); transition: transform 0.3s ease;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'times-circle' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"
             style="color: ${typeColors[type]};"></i>
          <span>${message}</span>
        </div>
      </div>
    `);

    $('body').append(toast);
    
    // Animate in
    setTimeout(() => toast.css('transform', 'translateX(0)'), 100);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
      toast.css('transform', 'translateX(100%)');
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  }

  // Update colors for severity indicators
  updateColors() {
    let colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6",
                  "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
    
    // Apply colors to severity indicators
    $("td").each(function() {
      const text = $(this).text().trim();
      const colorMap = {
        'Critical': '#dd4b39',
        'High': '#f39c12',
        'Medium': '#00c0ef',
        'Low': '#39cccc',
        'Informational': '#00a65a'
      };
      
      if (colorMap[text]) {
        $(this).html(`<span class="status-indicator ${text.toLowerCase()}">${text}</span>`);
      }
    });
  }
}

// Legacy function for backward compatibility
function accept(nid, el) {
  window.modernDashboard.accept(nid, el);
}

// Initialize modern dashboard
$(function() {
  // Add CSS animations
  $('<style>').text(`
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
    .modern-pagination .paginate_button {
      background: var(--secondary-dark) !important;
      border: 1px solid var(--border-color) !important;
      color: var(--text-secondary) !important;
    }
    .modern-pagination .paginate_button:hover {
      background: var(--primary-blue) !important;
      color: white !important;
    }
  `).appendTo('head');

  // Initialize the modern dashboard
  window.modernDashboard = new ModernDashboard();
});

// Enhanced color updating function for backward compatibility
function updateColors(){
  let colors = ["#8E44AD", "#9B59B6", "#2C3E50", "#34495E", "#95A5A6", "#00a65a", "#39cccc", "#00c0ef", "#f39c12", "#dd4b39"];
  
  // Modern severity color mapping
  const severityColors = {
    'Critical': '#dd4b39',
    'High': '#f39c12',
    'Medium': '#00c0ef',
    'Low': '#39cccc',
    'Informational': '#00a65a',
    'None': '#00a65a'
  };

  // Apply modern status indicators
  $("td").each(function() {
    const text = $(this).text().trim();
    if (severityColors[text]) {
      $(this).html(`<span class="status-indicator ${text.toLowerCase()}">${text}</span>`);
    }
  });

  // Legacy color application for backward compatibility
  let count = 9;
  $('.risk-level').each(function() {
    const risk = $(this).text().trim();
    if (risk && risk !== 'Unassigned' && risk !== '') {
      $(this).css({
        "color": colors[count],
        "font-weight": "bold"
      });
      count--;
    }
  });
}