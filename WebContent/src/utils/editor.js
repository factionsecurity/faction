import { Editor, sanitizeHTML } from '@toast-ui/editor'
import codeSyntaxHighlight from '@toast-ui/editor-plugin-code-syntax-highlight'
import colorSyntax from '@toast-ui/editor-plugin-color-syntax'
import tableMergedCell from '@toast-ui/editor-plugin-table-merged-cell'
import '@toast-ui/editor/dist/toastui-editor.css';
import 'tui-color-picker/dist/tui-color-picker.css';
import '@toast-ui/editor-plugin-color-syntax/dist/toastui-editor-plugin-color-syntax.css';
import '@toast-ui/editor-plugin-table-merged-cell/dist/toastui-editor-plugin-table-merged-cell.css'
import { marked } from 'marked';
import TurndownService from 'turndown'
let html2md = new TurndownService()
import markdownit from 'markdown-it';
import DOMPurify from 'dompurify';

export class FactionEditor {
	constructor(assessmentId){
		this.editors = {}
		this.initialHTML = {}
		this.assessmentId=assessmentId
	}

	customHTMLSanitizer = (html) => {
		return DOMPurify.sanitize(html, {
			ADD_ATTR: ['rel', 'target', 'hreflang', 'type'],
			ALLOWED_TAGS: [
				'u', 'ins', 'b', 'i', 'strong', 'em', 'p', 'br', 'h1', 'h2', 'h3', 'h4',
				'ul', 'ol', 'li', 'a', 'img', 'code', 'pre', 'table', 'thead', 'tbody', 'tr', 'th', 'td',
				'span', 'hr', 's', 'del', 'blockquote'
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
    
    toggleUnderline(id) {
        const editor = this.editors[id];
        const selection = window.getSelection();
        
        if (selection.rangeCount === 0) return;
        
        const range = selection.getRangeAt(0);
        const selectedText = range.toString();
        
        if (selectedText) {
            if (editor.getCurrentModeEditor().editorType  === 'wysiwyg') {
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
	createReadOnly(id,initialHTML){
		$(`#${id}`).html("");
		this.editors[id]=new Editor({
					el: document.querySelector(`#${id}`),
					toolbarItems:[],
					previewStyle: 'vertical',
					autofocus: false,
					viewer: true,
					height: '520px',
					initialEditType: 'wysiwyg'
				});
		this.editors[id].setHTML(initialHTML, false);
		this.editors[id].on('keydown', function(t,e) {
			if ( !((e.ctrlKey || e.metaKey) && e.key == 'c')) {
				e.preventDefault();
				throw new Error("Prevent Edit");
			 }
			
		});
	}
	
	createEditor(id, offloadImages, onChangeCallback) {
		if(typeof onChangeCallback == 'undefined'){
			onChangeCallback = function(){}
		}
		this.initialHTML[id] = entityDecode($(`#${id}`).html());
		$(`#${id}`).html("");
		let _this = this;
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
                    tooltip: 'Underline (Ctrl+U / Cmd+U)'
                }
            ],
            ['hr', 'quote'],
            ['ul', 'ol', 'task', 'indent', 'outdent'],
            ['table', 'image', 'link'],
            ['code', 'codeblock'],
            ['scrollSync']
        ],
			customHTMLSanitizer: this.customHTMLSanitizer,
			events: {
				beforePreviewRender: (html) => {
					return html.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
				},
				beforeConvertWysiwygToMarkdown: (html) => {
					return html.replace(/<u>([^<]+)<\/u>/g, '++$1++');
				},
				changeMode: (mode) => {
					if (mode === 'wysiwyg') {
						setTimeout(() => {
							_this.processUnderlineDirectDOM(id);
						}, 100);
					}
				},
				change: () => {
					if (this.editors[id].getCurrentModeEditor().editorType === 'wysiwyg') {
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
			this.editors[id].addHook( "addImageBlobHook", async (blob, callback, source) => {
					const encodedImage = await imageToURL(blob)
					let data = "encodedImage=" + encodeURIComponent(encodedImage);
					data += "&assessmentId=" + this.assessmentId;
					$.post("UploadImage", data).done(function(resp) {
						let uuid = resp.message;
						callback("getImage?id=" + uuid);
					});

				});
		}
		this.editors[id].hide();
		this.editors[id].setHTML(this.initialHTML[id], false);
		this.initialHTML[id] = this.editors[id].getHTML();
		this.editors[id].show();
		this.editors[id].on('change',() =>{
			onChangeCallback(id, this.editors[id])
		});

		/// This is a hack becuase toastui does not have inital undo history set correctly
		/// https://github.com/nhn/tui.editor/issues/3195
		this.editors[id].on('keydown', function(a, e) {
			const html = _this.editors[id].getHTML()
			if ((e.ctrlKey || e.metaKey) && e.key == 'z' && html == _this.initialHTML[id]) {
				e.preventDefault();
				throw new Error("Prevent Undo");
			}
		})

	}
	getEditors(){
		return this.editors;
	}	
	hide(id){
		this.editors[id].hide();	
	}
	show(id){
		this.editors[id].show();	
	}
	setHTML(id, html, cursorToEnd){
		html = html.replace(/<u>([^<]+)<\/u>/g, '++$1++');
        this.editors[id].setHTML(html, cursorToEnd);
	}
	getHTML(id){
        let content = this.editors[id].getHTML();
        return content.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
	}
	getEditorText(id) {
		let html = this.editors[id].getHTML();
        html = html.replace(/\+\+([^+\n]+)\+\+/g, '<u>$1</u>');
		//return Array.from($(html)).filter(a => a.innerHTML != "<br>").map(a => a.outerHTML).join("")
		return html
	}
	changeOff(id){
		this.editors[id].off('change')
	}
	setOnChangeCallBack(id,callback){
		this.editors[id].on('change', callback);
	}
	b64DecodeUnicode(str) {
		str = decodeURIComponent(str);
		return decodeURIComponent(Array.prototype.map.call(atob(str), function(c) {
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
		this.editors[id].setHTML(contents, false);
		this.editors[id].moveCursorToStart(false);
	}
	
	recreateEditor(id, contents, offloadImages, isEncoded, callback){
		if(typeof callback == 'undefined'){
			callback = function(){}
		}
		this.changeOff(id);
		this.editors[id].destroy();
		this.createEditor(id,offloadImages,()=>{});
		this.editors[id].hide();	
		if (isEncoded) {
			contents = this.b64DecodeUnicode(contents)
		}
		//contents = contents.replaceAll("<br />", "\n");
		contents = contents.replace(/<u>([^<]+)<\/u>/g, '++$1++');
		this.editors[id].setHTML(contents, false);
		this.editors[id].moveCursorToStart(false);
		this.initialHTML[id] = this.editors[id].getHTML();
		this.setOnChangeCallBack(id,callback);
		this.editors[id].show();	
	}
	entityDecode(encoded){
		let textArea = document.createElement("textarea");
		textArea.innerHTML = encoded;
		return textArea.innerText;
		
	}
}