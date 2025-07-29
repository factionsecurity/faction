/**
 * Modern Assessment Interface Enhancement
 * Enhances the existing VulnerabilityView class with modern UX features
 */

class ModernAssessmentView {
    constructor() {
        this.init();
        this.setupModernFeatures();
        this.setupAnimations();
        this.setupAccessibility();
    }

    init() {
        // Add modern loading overlay to main content
        this.createLoadingOverlay();
        
        // Initialize toast notification system
        this.initToastSystem();
        
        // Setup modern interactions
        this.setupModernInteractions();
        
        // Initialize performance monitoring
        this.initPerformanceMonitoring();
    }

    createLoadingOverlay() {
        if (!document.getElementById('modern-loading-overlay')) {
            const overlay = document.createElement('div');
            overlay.id = 'modern-loading-overlay';
            overlay.className = 'loading-overlay';
            overlay.innerHTML = `
                <div class="loading-spinner"></div>
                <div style="color: var(--text-primary); margin-top: 16px; font-weight: 500;">
                    Loading assessment data...
                </div>
            `;
            overlay.style.display = 'none';
            document.body.appendChild(overlay);
        }
    }

    showLoadingState(message = 'Loading...') {
        const overlay = document.getElementById('modern-loading-overlay');
        if (overlay) {
            overlay.querySelector('div:last-child').textContent = message;
            overlay.style.display = 'flex';
            overlay.style.animation = 'fadeIn 0.3s ease-in';
        }
    }

    hideLoadingState() {
        const overlay = document.getElementById('modern-loading-overlay');
        if (overlay) {
            overlay.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                overlay.style.display = 'none';
            }, 300);
        }
    }

    initToastSystem() {
        // Create toast container if it doesn't exist
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            container.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 10000;
                display: flex;
                flex-direction: column;
                gap: 12px;
                pointer-events: none;
            `;
            document.body.appendChild(container);
        }
    }

    showToast(message, type = 'info', duration = 4000) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        
        const typeStyles = {
            success: 'background: rgba(39, 174, 96, 0.9); border-left: 4px solid #27ae60;',
            error: 'background: rgba(231, 76, 60, 0.9); border-left: 4px solid #e74c3c;',
            warning: 'background: rgba(243, 156, 18, 0.9); border-left: 4px solid #f39c12;',
            info: 'background: rgba(52, 152, 219, 0.9); border-left: 4px solid #3498db;'
        };

        toast.style.cssText = `
            ${typeStyles[type] || typeStyles.info}
            color: white;
            padding: 16px 20px;
            border-radius: 8px;
            backdrop-filter: blur(20px);
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
            font-weight: 500;
            font-size: 14px;
            max-width: 400px;
            pointer-events: auto;
            cursor: pointer;
            animation: slideInRight 0.3s ease-out;
        `;
        
        toast.textContent = message;
        
        // Click to dismiss
        toast.addEventListener('click', () => {
            this.removeToast(toast);
        });
        
        container.appendChild(toast);
        
        // Auto dismiss
        setTimeout(() => {
            if (toast.parentNode) {
                this.removeToast(toast);
            }
        }, duration);
    }

    removeToast(toast) {
        toast.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }

    setupModernInteractions() {
        // Enhance form interactions
        this.enhanceFormElements();
        
        // Add modern hover effects
        this.addHoverEffects();
        
        // Enhance table interactions
        this.enhanceTableInteractions();
        
        // Add keyboard shortcuts
        this.setupKeyboardShortcuts();
    }

    enhanceFormElements() {
        // Add floating label effect to form inputs
        const inputs = document.querySelectorAll('.form-control');
        inputs.forEach(input => {
            this.addFloatingLabel(input);
            this.addFocusRipple(input);
        });

        // Enhance select elements
        const selects = document.querySelectorAll('select.form-control');
        selects.forEach(select => {
            this.enhanceSelect(select);
        });

        // Add modern button interactions
        const buttons = document.querySelectorAll('.btn');
        buttons.forEach(button => {
            this.addButtonRipple(button);
        });
    }

    addFloatingLabel(input) {
        const parent = input.parentElement;
        const label = parent.querySelector('label');
        
        if (label && !label.classList.contains('floating-label')) {
            label.classList.add('floating-label');
            
            const checkFloating = () => {
                if (input.value || input === document.activeElement) {
                    label.classList.add('floating');
                } else {
                    label.classList.remove('floating');
                }
            };
            
            input.addEventListener('focus', checkFloating);
            input.addEventListener('blur', checkFloating);
            input.addEventListener('input', checkFloating);
            checkFloating();
        }
    }

    addFocusRipple(element) {
        element.addEventListener('focus', (e) => {
            element.style.transform = 'scale(1.02)';
            element.style.transition = 'all 0.2s ease';
        });
        
        element.addEventListener('blur', (e) => {
            element.style.transform = 'scale(1)';
        });
    }

    enhanceSelect(select) {
        // Add custom styling for select elements
        select.addEventListener('change', (e) => {
            select.style.animation = 'pulse 0.3s ease';
            setTimeout(() => {
                select.style.animation = '';
            }, 300);
        });
    }

    addButtonRipple(button) {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    }

    addHoverEffects() {
        // Add subtle hover effects to cards and interactive elements
        const cards = document.querySelectorAll('.box');
        cards.forEach(card => {
            card.style.transition = 'all 0.3s ease';
            
            card.addEventListener('mouseenter', () => {
                card.style.transform = 'translateY(-2px)';
                card.style.boxShadow = 'var(--shadow-lg)';
            });
            
            card.addEventListener('mouseleave', () => {
                card.style.transform = 'translateY(0)';
                card.style.boxShadow = 'var(--shadow-md)';
            });
        });
    }

    enhanceTableInteractions() {
        // Add smooth row selection animations
        const tableRows = document.querySelectorAll('#vulntable tbody tr');
        tableRows.forEach(row => {
            row.style.transition = 'all 0.2s ease';
            
            row.addEventListener('click', () => {
                // Add selection animation
                row.style.animation = 'pulse 0.3s ease';
                setTimeout(() => {
                    row.style.animation = '';
                }, 300);
            });
        });

        // Add loading state to table operations
        this.enhanceTableOperations();
    }

    enhanceTableOperations() {
        // Intercept DataTable operations and add loading states
        const table = $('#vulntable').DataTable();
        
        // Override draw function to add loading animation
        const originalDraw = table.draw;
        table.draw = function(...args) {
            const tableContainer = document.querySelector('#vulntable_wrapper');
            if (tableContainer) {
                tableContainer.style.position = 'relative';
                const loader = document.createElement('div');
                loader.className = 'table-loading';
                loader.innerHTML = '<div class="loading-spinner"></div>';
                loader.style.cssText = `
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0, 0, 0, 0.3);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 1000;
                `;
                tableContainer.appendChild(loader);
                
                setTimeout(() => {
                    if (loader.parentNode) {
                        loader.parentNode.removeChild(loader);
                    }
                }, 500);
            }
            
            return originalDraw.apply(this, args);
        };
    }

    setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // Ctrl/Cmd + S to save (prevent default browser save)
            if ((e.ctrlKey || e.metaKey) && e.key === 's') {
                e.preventDefault();
                this.showToast('Assessment auto-saves your changes', 'info');
            }
            
            // Ctrl/Cmd + N to add new vulnerability
            if ((e.ctrlKey || e.metaKey) && e.key === 'n') {
                e.preventDefault();
                const addButton = document.getElementById('addVuln');
                if (addButton && !addButton.disabled) {
                    addButton.click();
                    this.showToast('Adding new vulnerability', 'info');
                }
            }
            
            // Escape to clear selection
            if (e.key === 'Escape') {
                const selected = document.querySelector('.selected');
                if (selected) {
                    selected.classList.remove('selected');
                    this.showToast('Selection cleared', 'info');
                }
            }
        });
    }

    setupAnimations() {
        // Add entrance animations to major sections
        this.animateOnLoad();
        
        // Setup intersection observer for scroll animations
        this.setupScrollAnimations();
    }

    animateOnLoad() {
        // Animate header
        const header = document.querySelector('.content-header');
        if (header) {
            header.style.animation = 'fadeInDown 0.6s ease-out';
        }

        // Animate navigation tabs
        const tabs = document.querySelector('.nav-tabs-custom');
        if (tabs) {
            tabs.style.animation = 'fadeInUp 0.6s ease-out 0.2s both';
        }

        // Animate main content
        const content = document.querySelector('.tab-content');
        if (content) {
            content.style.animation = 'fadeIn 0.8s ease-out 0.4s both';
        }
    }

    setupScrollAnimations() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.animation = 'slideUp 0.6s ease-out';
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });

        // Observe boxes and tables
        const elements = document.querySelectorAll('.box, .small-box');
        elements.forEach(el => observer.observe(el));
    }

    setupAccessibility() {
        // Add ARIA labels and improve keyboard navigation
        this.improveAccessibility();
        
        // Add focus indicators
        this.addFocusIndicators();
        
        // Setup screen reader announcements
        this.setupScreenReader();
    }

    improveAccessibility() {
        // Add ARIA labels to interactive elements
        const buttons = document.querySelectorAll('.btn, .vulnControl');
        buttons.forEach(button => {
            if (!button.getAttribute('aria-label')) {
                const title = button.getAttribute('title') || button.textContent.trim();
                if (title) {
                    button.setAttribute('aria-label', title);
                }
            }
        });

        // Improve table accessibility
        const table = document.querySelector('#vulntable');
        if (table) {
            table.setAttribute('role', 'grid');
            table.setAttribute('aria-label', 'Vulnerability findings table');
        }

        // Add skip links
        this.addSkipLinks();
    }

    addSkipLinks() {
        const skipNav = document.createElement('a');
        skipNav.href = '#main-content';
        skipNav.textContent = 'Skip to main content';
        skipNav.style.cssText = `
            position: absolute;
            top: -40px;
            left: 6px;
            background: var(--primary-color);
            color: white;
            padding: 8px;
            border-radius: 4px;
            text-decoration: none;
            font-weight: 500;
            z-index: 10000;
            transition: top 0.3s;
        `;
        
        skipNav.addEventListener('focus', () => {
            skipNav.style.top = '6px';
        });
        
        skipNav.addEventListener('blur', () => {
            skipNav.style.top = '-40px';
        });
        
        document.body.insertBefore(skipNav, document.body.firstChild);
    }

    addFocusIndicators() {
        const style = document.createElement('style');
        style.textContent = `
            .modern-focus:focus-visible {
                outline: 2px solid var(--primary-color);
                outline-offset: 2px;
                border-radius: 4px;
            }
        `;
        document.head.appendChild(style);

        // Add focus class to interactive elements
        const focusableElements = document.querySelectorAll('button, input, select, textarea, a[href], [tabindex]');
        focusableElements.forEach(el => {
            el.classList.add('modern-focus');
        });
    }

    setupScreenReader() {
        // Create ARIA live region for announcements
        const liveRegion = document.createElement('div');
        liveRegion.id = 'screen-reader-announcements';
        liveRegion.setAttribute('aria-live', 'polite');
        liveRegion.setAttribute('aria-atomic', 'true');
        liveRegion.style.cssText = `
            position: absolute;
            left: -10000px;
            width: 1px;
            height: 1px;
            overflow: hidden;
        `;
        document.body.appendChild(liveRegion);
    }

    announce(message) {
        const liveRegion = document.getElementById('screen-reader-announcements');
        if (liveRegion) {
            liveRegion.textContent = message;
            setTimeout(() => {
                liveRegion.textContent = '';
            }, 1000);
        }
    }

    initPerformanceMonitoring() {
        // Monitor page load performance
        if ('performance' in window) {
            window.addEventListener('load', () => {
                setTimeout(() => {
                    const perfData = performance.getEntriesByType('navigation')[0];
                    const loadTime = perfData.loadEventEnd - perfData.loadEventStart;
                    
                    if (loadTime > 3000) {
                        console.warn('Assessment page load time is slow:', loadTime + 'ms');
                    }
                }, 0);
            });
        }

        // Monitor memory usage periodically
        if ('memory' in performance) {
            setInterval(() => {
                const memory = performance.memory;
                const memoryUsage = memory.usedJSHeapSize / memory.jsHeapSizeLimit;
                
                if (memoryUsage > 0.8) {
                    console.warn('High memory usage detected:', (memoryUsage * 100).toFixed(1) + '%');
                }
            }, 30000); // Check every 30 seconds
        }
    }

    // Public methods for integration with existing code
    enhanceExistingFeatures() {
        // Hook into existing VulnerabilityView events
        this.enhanceVulnOperations();
        this.enhanceNoteOperations();
        this.enhanceFormValidation();
    }

    enhanceVulnOperations() {
        // Add loading states to vulnerability operations
        const originalSaveChanges = window.global?.vulnView?.saveChanges;
        if (originalSaveChanges) {
            window.global.vulnView.saveChanges = (...args) => {
                this.showToast('Saving vulnerability changes...', 'info', 2000);
                const result = originalSaveChanges.apply(window.global.vulnView, args);
                setTimeout(() => this.showToast('Changes saved successfully', 'success'), 1000);
                return result;
            };
        }
    }

    enhanceNoteOperations() {
        // Add visual feedback for note operations
        const noteButtons = document.querySelectorAll('#createNote, #deleteNote');
        noteButtons.forEach(button => {
            button.addEventListener('click', () => {
                button.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    button.style.transform = 'scale(1)';
                }, 150);
            });
        });
    }

    enhanceFormValidation() {
        // Add real-time validation feedback
        const requiredFields = document.querySelectorAll('input[required], select[required]');
        requiredFields.forEach(field => {
            field.addEventListener('blur', () => {
                if (!field.value.trim()) {
                    field.style.borderColor = 'var(--danger-color)';
                    field.style.animation = 'shake 0.5s ease-in-out';
                } else {
                    field.style.borderColor = 'var(--success-color)';
                    field.style.animation = '';
                }
            });
        });
    }
}

// CSS Animations
const modernAnimationsCSS = `
@keyframes ripple {
    to {
        transform: scale(4);
        opacity: 0;
    }
}

@keyframes pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.05); }
    100% { transform: scale(1); }
}

@keyframes shake {
    0%, 100% { transform: translateX(0); }
    10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
    20%, 40%, 60%, 80% { transform: translateX(5px); }
}

@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes slideOutRight {
    from {
        transform: translateX(0);
        opacity: 1;
    }
    to {
        transform: translateX(100%);
        opacity: 0;
    }
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeOut {
    from { opacity: 1; }
    to { opacity: 0; }
}

.floating-label {
    transition: all 0.2s ease;
    pointer-events: none;
}

.floating-label.floating {
    transform: translateY(-20px) scale(0.85);
    color: var(--primary-color) !important;
}
`;

// Inject animations CSS
const styleSheet = document.createElement('style');
styleSheet.textContent = modernAnimationsCSS;
document.head.appendChild(styleSheet);

// Initialize modern assessment view when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // Wait for existing JS to initialize first
    setTimeout(() => {
        window.modernAssessmentView = new ModernAssessmentView();
        
        // Enhance existing features if VulnerabilityView exists
        if (window.global?.vulnView) {
            window.modernAssessmentView.enhanceExistingFeatures();
        }
        
        console.log('Modern Assessment View initialized');
    }, 1000);
});

// Export for manual initialization if needed
window.ModernAssessmentView = ModernAssessmentView;