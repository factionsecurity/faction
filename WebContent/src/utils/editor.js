import { Editor } from '@toast-ui/editor';
import colorSyntax from '@toast-ui/editor-plugin-color-syntax';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell';
import '@toast-ui/editor-plugin-table-merged-cell/dist/toastui-editor-plugin-table-merged-cell.css';
import '@toast-ui/editor/dist/toastui-editor.css';
import DOMPurify from 'dompurify';
import 'tui-color-picker/dist/tui-color-picker.css';
require('./editor.css')

export class FactionEditor {
    constructor(assessmentId) {
        this.editors = {}
        this.initialHTML = {}
        this.assessmentId = assessmentId
        this.vulnId = null
        this.showExecutiveSummary = true
    }

    setVulnId(vulnId) {
        this.vulnId = vulnId;
    }

    customHTMLSanitizer = (html) => {
        return DOMPurify.sanitize(html, {
            ADD_ATTR: ['rel', 'target', 'hreflang', 'type'],
            ALLOWED_TAGS: [
                'u', 'ins', 'b', 'i', 'strong', 'em', 'p', 'br', 'h1', 'h2', 'h3', 'h4',
                'ul', 'ol', 'li', 'a', 'img', 'code', 'pre', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
                'span', 'hr', 's', 'del', 'blockquote', 'center'
                // Add other tags Toast UI uses
            ],
            ALLOWED_ATTR: ['href', 'src', 'alt', 'class', 'style', 'text-decoration', 'colspan'], // Optional
        });
    };
    createUnderlineButton(id) {
        const button = document.createElement('button');
        button.className = 'underline-button';
        button.type = 'button';
        button.innerHTML = `<u>U</u>`;

        // Add click event
        button.addEventListener('click', () => {
            this.toggleUnderline(id);
            // Toggle active state
            button.classList.toggle('active');
        });

        return button;
    }
    createAIDropdownButton(id) {
        const _this = this;

        const wrapper = document.createElement('div');
        wrapper.style.cssText = 'position: relative; display: inline-block;';

        const button = document.createElement('button');
        button.className = 'ai-summary-button';
        button.type = 'button';
        button.title = 'AI Tools';
        button.innerHTML = `<i class="fa fa-robot" style="font-size: 14px;"></i> <i class="fa fa-caret-down" style="font-size: 10px; margin-left: 2px;"></i>`;

        const menu = document.createElement('div');
        menu.className = 'ai-toolbar-dropdown';
        menu.style.cssText = [
            'display: none',
            'position: absolute',
            'top: calc(100% + 4px)',
            'left: 0',
            'z-index: 9999',
            'background: #1e2a3a',
            'border: 1px solid #3a5068',
            'border-radius: 4px',
            'box-shadow: 0 4px 12px rgba(0,0,0,0.4)',
            'min-width: 190px',
            'padding: 4px 0'
        ].join(';');

        const items = [];
        if (_this.showExecutiveSummary) {
            items.push({
                label: '<i class="fa fa-file-text-o" style="margin-right:7px;"></i>Create Executive Summary',
                action: () => { _this.showAISummaryModal(id); }
            });
        }
        items.push({
            label: '<i class="fa fa-pencil" style="margin-right:7px;"></i>Update This Text',
            action: () => { _this.showAICustomModal(id); }
        });

        items.forEach(item => {
            const menuItem = document.createElement('div');
            menuItem.innerHTML = item.label;
            menuItem.style.cssText = 'padding: 8px 14px; cursor: pointer; font-size: 13px; color: #cdd9e5; white-space: nowrap;';
            menuItem.addEventListener('mouseenter', () => { menuItem.style.background = '#2d4a6b'; });
            menuItem.addEventListener('mouseleave', () => { menuItem.style.background = ''; });
            menuItem.addEventListener('click', (e) => {
                e.stopPropagation();
                menu.style.display = 'none';
                item.action();
            });
            menu.appendChild(menuItem);
        });

        button.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = menu.style.display === 'block';
            document.querySelectorAll('.ai-toolbar-dropdown').forEach(m => { m.style.display = 'none'; });
            menu.style.display = isOpen ? 'none' : 'block';
        });

        document.addEventListener('click', () => { menu.style.display = 'none'; });

        wrapper.appendChild(button);
        wrapper.appendChild(menu);
        return wrapper;
    }
    showAICustomModal(id) {
        const _this = this;
        $.confirm({
            title: 'Ask AI',
            content: 'What would you like to do?<br><textarea id="customPrompt" cols="50" rows="4" placeholder="e.g. Summarize from the details field, combine with another vulnerability, rewrite in simpler language..."></textarea><br>',
            type: 'blue',
            buttons: {
                confirm: {
                    text: 'Generate',
                    btnClass: 'btn-primary',
                    action: function () {
                        _this.generateAIWithTools(id);
                    }
                },
                cancel: {
                    text: 'Cancel',
                    btnClass: 'btn-secondary'
                }
            }
        });
    }

    generateAIWithTools(id) {
        const _this = this;

        const editorEl = document.querySelector(`#${id}`);
        const prompt = document.querySelector("#customPrompt").value;
        const overlay = document.createElement('div');
        overlay.className = 'editor-upload-overlay';
        overlay.innerHTML = `
            <div class="editor-upload-spinner">
                <div class="spinner-icon"></div>
                <div class="spinner-text">AI is thinking...</div>
            </div>
        `;
        editorEl.style.position = 'relative';
        editorEl.appendChild(overlay);

        const data = {
            assessmentId: this.assessmentId,
            prompt: prompt,
            context: this.getEditorText(id),
            vulnId: this.vulnId || '',
            _token: global._token
        };

        $.post('GenerateAIWithTools', data)
            .done(function (response) {
                overlay.remove();
                if (response.result === 'success') {
                    const decodedSummary = _this.b64DecodeUnicode(response.summary);
                    _this.editors[id].setHTML(decodedSummary, false);

                    $.alert({
                        title: 'Done',
                        content: 'AI response applied.',
                        type: 'green'
                    });
                } else {
                    $.alert({
                        title: 'Error',
                        content: response.message || 'Failed to generate AI response',
                        type: 'red'
                    });
                }
            })
            .fail(function () {
                overlay.remove();
                $.alert({
                    title: 'Error',
                    content: 'Network error occurred',
                    type: 'red'
                });
            });
    }

    showAISummaryModal(id) {
        const _this = this;
        $.confirm({
            title: 'Generate AI Summary',
            content: 'This will generate a high-level summary of all vulnerabilities and replace the current content. Are you sure you want to continue?',
            type: 'blue',
            buttons: {
                confirm: {
                    text: 'Generate Summary',
                    btnClass: 'btn-primary',
                    action: function () {
                        _this.generateAISummary(id);
                    }
                },
                cancel: {
                    text: 'Cancel',
                    btnClass: 'btn-secondary'
                }
            }
        });
    }

    generateAISummary(id) {
        const _this = this;

        // Show loading overlay
        const editorEl = document.querySelector(`#${id}`);
        const overlay = document.createElement('div');
        overlay.className = 'editor-upload-overlay';
        overlay.innerHTML = `
            <div class="editor-upload-spinner">
                <div class="spinner-icon"></div>
                <div class="spinner-text">Generating AI summary...</div>
            </div>
        `;
        editorEl.style.position = 'relative';
        editorEl.appendChild(overlay);

        // Make AJAX call to generate AI summary
        const data = {
            assessmentId: this.assessmentId,
            _token: global._token
        };

        $.post('GenerateAISummary', data)
            .done(function (response) {
                overlay.remove();
                if (response.result === 'success') {
                    // Decode Base64 response and replace editor content with AI-generated summary
                    const decodedSummary = _this.b64DecodeUnicode(response.summary);
                    _this.editors[id].setHTML(decodedSummary, false);

                    $.alert({
                        title: 'Success',
                        content: 'AI summary generated successfully!',
                        type: 'green'
                    });
                } else {
                    $.alert({
                        title: 'Error',
                        content: response.message || 'Failed to generate AI summary',
                        type: 'red'
                    });
                }
            })
            .fail(function () {
                overlay.remove();
                $.alert({
                    title: 'Error',
                    content: 'Network error occurred while generating AI summary',
                    type: 'red'
                });
            });
    }

    toggleUnderline(id) {
        const editor = this.editors[id];
        const selection = window.getSelection();

        if (selection.rangeCount === 0) return;

        const range = selection.getRangeAt(0);
        const selectedText = range.toString();

        if (selectedText) {
            if (editor.getCurrentModeEditor().editorType === 'wysiwyg') {
                // WYSIWYG mode: Check if already underlined
                const parentElement = range.commonAncestorContainer.parentElement;

                if (parentElement && parentElement.tagName === 'U') {
                    // Remove underline - unwrap the <u> tag
                    const textNode = document.createTextNode(parentElement.textContent);
                    parentElement.parentNode.replaceChild(textNode, parentElement);
                } else {
                    // Add underline - wrap with <u> tags
                    try {
                        const underlineElement = document.createElement('u');
                        range.surroundContents(underlineElement);
                    } catch (e) {
                        // If surroundContents fails, use alternative approach
                        range.deleteContents();
                        const underlinedContent = document.createElement('u');
                        underlinedContent.textContent = selectedText;
                        range.insertNode(underlinedContent);
                    }
                }
            } else {
                // Markdown mode: Check if already has ++ syntax
                const beforeRange = range.cloneRange();
                beforeRange.setStart(range.startContainer, Math.max(0, range.startOffset - 2));
                beforeRange.setEnd(range.startContainer, range.startOffset);

                const afterRange = range.cloneRange();
                afterRange.setStart(range.endContainer, range.endOffset);
                afterRange.setEnd(range.endContainer, Math.min(range.endContainer.textContent.length, range.endOffset + 2));

                const beforeText = beforeRange.toString();
                const afterText = afterRange.toString();

                if (beforeText === '++' && afterText === '++') {
                    // Remove underline syntax
                    beforeRange.deleteContents();
                    afterRange.deleteContents();
                } else {
                    // Add underline syntax
                    const markdownUnderline = `++${selectedText}++`;
                    range.deleteContents();
                    range.insertNode(document.createTextNode(markdownUnderline));
                }
            }

            // Clear selection
            selection.removeAllRanges();
        }
    }

    processUnderlineDirectDOM(id) {
        const editor = this.editors[id];

        if (editor.getCurrentModeEditor().editorType === 'wysiwyg') {
            // Get the WYSIWYG editor's DOM element
            const wysiwygEditor = editor.getEditorElements().wwEditor;

            if (wysiwygEditor) {
                let content = wysiwygEditor.innerHTML;
                const processedContent = content.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');

                if (content !== processedContent) {
                    // Store cursor position
                    const selection = window.getSelection();
                    let range = null;
                    if (selection.rangeCount > 0) {
                        range = selection.getRangeAt(0);
                    }

                    // Direct DOM manipulation
                    wysiwygEditor.innerHTML = processedContent;

                    // Restore cursor position
                    if (range) {
                        try {
                            selection.removeAllRanges();
                            selection.addRange(range);
                        } catch (e) {
                            // Cursor restoration failed
                        }
                    }
                }
            }
        }
    }
    createReadOnly(id, initialHTML) {
        $(`#${id}`).html("");
        this.editors[id] = new Editor({
            el: document.querySelector(`#${id}`),
            toolbarItems: [],
            previewStyle: 'vertical',
            autofocus: false,
            viewer: true,
            height: '520px',
            initialEditType: 'wysiwyg'
        });
        this.editors[id].setHTML(initialHTML, false);
        this.editors[id].on('keydown', function (t, e) {
            if (!((e.ctrlKey || e.metaKey) && e.key == 'c')) {
                e.preventDefault();
                throw new Error("Prevent Edit");
            }

        });
    }

    createEditor(id, offloadImages, onChangeCallback) {
        if (typeof onChangeCallback == 'undefined') {
            onChangeCallback = function () { }
        }
        this.initialHTML[id] = entityDecode($(`#${id}`).html()).replace(/<u>([^<]+)<\/u>/g, '++$1++');
        $(`#${id}`).html("");
        let _this = this;
        Editor.setLanguage('en-US', {
            'Blockquote': 'Center'
        });
        this.editors[id] = new Editor({
            el: document.querySelector(`#${id}`),
            previewStyle: 'vertical',
            height: 'auto',
            autofocus: false,
            height: '560px',
            plugins: [colorSyntax, tableMergedCell],
            toolbarItems: [
                ['heading', 'bold', 'italic',
                    {
                        el: this.createUnderlineButton(id),
                        name: 'underline',
                        tooltip: 'Underline'
                    },
                    'quote'
                ],
                ['hr', 'ul', 'ol', 'indent', 'outdent'],
                ['table', 'image', 'link'],
                ['code', 'codeblock'],
                [
                    {
                        el: this.createAIDropdownButton(id),
                        name: 'aiTools',
                        tooltip: 'AI Tools'
                    }
                ],
                ['scrollSync']
            ],
            i18n: {
                'Blockquote': 'Center',
                'Quote': 'Center',
            },
            customHTMLSanitizer: this.customHTMLSanitizer,
            events: {
                beforePreviewRender: (html) => {
                    html = html.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
                    return html;
                },
                beforeConvertWysiwygToMarkdown: (html) => {
                    html = html.replace(/<u>([^<]+)<\/u>/g, '++$1++');
                    return html;
                },
                changeMode: (mode) => {
                    if (mode === 'wysiwyg') {
                        setTimeout(() => {
                            _this.processUnderlineDirectDOM(id);
                        }, 100);
                    }
                },
                change: () => {
                    if (_this.editors[id].getCurrentModeEditor().editorType === 'wysiwyg') {
                        // Debounce the processing to avoid performance issues
                        clearTimeout(window.underlineTimeout);
                        window.underlineTimeout = setTimeout(() => {
                            _this.processUnderlineDirectDOM(id);
                        }, 300);
                    }
                }
            }
        });

        if (offloadImages) {
            this.editors[id].addHook("addImageBlobHook", async (blob, callback, source) => {
                // Create overlay
                const editorEl = document.querySelector(`#${id}`);
                const overlay = document.createElement('div');
                overlay.className = 'editor-upload-overlay';
                overlay.innerHTML = `
						<div class="editor-upload-spinner">
							<div class="spinner-icon"></div>
							<div class="spinner-text">Uploading image...</div>
						</div>
					`;
                editorEl.style.position = 'relative';
                editorEl.appendChild(overlay);

                const encodedImage = await imageToURL(blob)
                let data = "encodedImage=" + encodeURIComponent(encodedImage);
                data += "&assessmentId=" + _this.assessmentId;
                $.post("UploadImage", data).done(function (resp) {
                    let uuid = resp.message;
                    callback("getImage?id=" + uuid);
                    // Remove overlay after callback
                    overlay.remove();
                }).fail(function () {
                    // Remove overlay on error too
                    overlay.remove();
                });

            });
        }
        this.editors[id].hide();
        this.editors[id].setHTML(this.initialHTML[id], false);
        this.initialHTML[id] = this.editors[id].getHTML();
        this.editors[id].show();
        this.editors[id].on('change', () => {
            onChangeCallback(id, this.editors[id])
        });

        /// This is a hack becuase toastui does not have inital undo history set correctly
        /// https://github.com/nhn/tui.editor/issues/3195
        this.editors[id].on('keydown', function (a, e) {
            const html = _this.editors[id].getHTML()
            if ((e.ctrlKey || e.metaKey) && e.key == 'z' && html == _this.initialHTML[id]) {
                e.preventDefault();
                throw new Error("Prevent Undo");
            }
        })

    }
    getEditors() {
        return this.editors;
    }
    hide(id) {
        this.editors[id].hide();
    }
    show(id) {
        this.editors[id].show();
    }
    setHTML(id, html, cursorToEnd) {
        html = html.replace(/<u>([^<]+)<\/u>/g, '++$1++');
        this.editors[id].setHTML(html, cursorToEnd);
    }
    getHTML(id) {
        let content = this.editors[id].getHTML();
        content = content.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
        return content
    }
    getEditorText(id) {
        let html = this.editors[id].getHTML();
        html = html.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
        return html
    }
    changeOff(id) {
        this.editors[id].off('change')
    }
    setOnChangeCallBack(id, callback) {
        this.editors[id].on('change', callback);
    }
    b64DecodeUnicode(str) {
        str = decodeURIComponent(str);
        return decodeURIComponent(Array.prototype.map.call(atob(str), function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''));
    }
    b64EncodeUnicode(str) {
        // First we encode to UTF-8, then Base64
        return btoa(encodeURIComponent(str).replace(/%([0-9A-F]{2})/g,
            (match, p1) => String.fromCharCode('0x' + p1)
        ));
    }
    setEditorContents(id, contents, isEncoded) {
        if (isEncoded) {
            contents = this.b64DecodeUnicode(contents)
        }
        //contents = contents.replaceAll("<br />", "\n");
        contents = contents.replace(/<u>([^<]+)<\/u>/g, '++$1++');
        this.editors[id].hide();
        this.editors[id].setHTML(contents, false);
        this.editors[id].moveCursorToStart(false);
        this.editors[id].show();
    }

    recreateEditor(id, contents, offloadImages, isEncoded, callback) {
        if (typeof callback == 'undefined') {
            callback = function () { }
        }
        this.changeOff(id);
        this.editors[id].destroy();
        this.createEditor(id, offloadImages, () => { });
        this.editors[id].hide();
        if (isEncoded) {
            contents = this.b64DecodeUnicode(contents)
        }
        //contents = contents.replaceAll("<br />", "\n");
        contents = contents.replace(/<u>([^<]+)<\/u>/g, '++$1++');
        this.editors[id].setHTML(contents, false);
        this.editors[id].moveCursorToStart(false);
        this.initialHTML[id] = this.editors[id].getHTML();
        this.setOnChangeCallBack(id, callback);
        this.editors[id].show();
    }
    entityDecode(encoded) {
        let textArea = document.createElement("textarea");
        textArea.innerHTML = encoded;
        return textArea.innerText;

    }
}