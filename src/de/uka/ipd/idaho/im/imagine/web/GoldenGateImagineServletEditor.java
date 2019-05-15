/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universität Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITÄT KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.im.imagine.web;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.util.CountingSet;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.feedback.FeedbackPanel;
import de.uka.ipd.idaho.gamta.util.feedback.html.FeedbackFormBuilder;
import de.uka.ipd.idaho.gamta.util.feedback.html.FeedbackFormBuilder.SubmitMode;
import de.uka.ipd.idaho.gamta.util.feedback.html.FeedbackFormPageBuilder;
import de.uka.ipd.idaho.gamta.util.feedback.html.FeedbackPanelHtmlRenderer;
import de.uka.ipd.idaho.gamta.util.feedback.html.FeedbackPanelHtmlRenderer.FeedbackPanelHtmlRendererInstance;
import de.uka.ipd.idaho.gamta.util.feedback.html.renderers.BufferedLineWriter;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.gamta.util.imaging.PageImage;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageInputStream;
import de.uka.ipd.idaho.gamta.util.web.ProgressMonitorHTML;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Html;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImDocument.ImDocumentListener;
import de.uka.ipd.idaho.im.ImObject;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImSupplement;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ReactionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineWebUtils.AttributeEditorPageBuilder;
import de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineWebUtils.IsolatorOutputStream;
import de.uka.ipd.idaho.im.imagine.web.plugins.WebDocumentViewer;
import de.uka.ipd.idaho.im.imagine.web.plugins.WebDocumentViewer.WebDocumentView;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.AtomicActionListener;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.SelectionAction;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.TwoClickSelectionAction;
import de.uka.ipd.idaho.im.util.ImUtils;

/**
 * A single document editor open inside an IMS Document Markup Servlet.
 * 
 * @author sautter
 */
public class GoldenGateImagineServletEditor implements LiteratureConstants, HtmlPageBuilderHost {
	private static final Html html = new Html();
	
	final String id = Gamta.getAnnotationID();
	final boolean isFullDocument;
	final String userName;
	
	final WebImDocumentMarkupPanel idmp;
	private GoldenGateImagineEditorServlet parent;
	
	private ActionThread actionThreadAwaitingConfirm = null;
	private ActionThread actionThreadAwaitingFeedback = null;
	private ActionThread actionThreadEditingWord = null;
	private WebDocumentView docView = null;
	private ActionThread docViewActionThread = null;
	private ActionThread mainMenuActionThread = null;
	private MmActionResult mainMenuActionResult = null;
	
	private Map actionMap = Collections.synchronizedMap(new HashMap());
	private TwoClickSelectionAction pendingTwoClickAction = null;
	
	private Transferable clipboardData = null;
	
	GoldenGateImagineServletEditor(GoldenGateImagineEditorServlet parent, WebImDocumentMarkupPanel idmp, boolean isFullDocument, String userName) {
		this.parent = parent;
		this.idmp = idmp;
		this.isFullDocument = isFullDocument;
		this.userName = userName;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#findFile(java.lang.String)
	 */
	public File findFile(String fileName) {
		return this.parent.findFile(fileName);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getRelativeDataPath()
	 */
	public String getRelativeDataPath() {
		return this.parent.getRelativeDataPath();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getCssStylesheets()
	 */
	public String[] getCssStylesheets() {
		return this.parent.getCssStylesheets();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getJavaScriptFiles()
	 */
	public String[] getJavaScriptFiles() {
		return this.parent.getJavaScriptFiles();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getIcon()
	 */
	public String getIcon() {
		return this.parent.getIcon();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#writePageHeadExtensions(de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	public void writePageHeadExtensions(HtmlPageBuilder hpb) throws IOException {
		this.parent.writePageHeadExtensions(hpb);
		
		hpb.writeLine("<script type=\"text/javascript\">");
		
		//	show alert (in a DIV sitting on top of an overlay, no need for an IFRAME)
		hpb.writeLine("function showAlertDialog(message, title, type) {");
		hpb.writeLine("  var alertOverlay = getOverlay(null, 'alertOverlay', true);");
		hpb.writeLine("  var alertDiv = newElement('div', null, 'alertForm', null);");
		//	add title (if any)
		hpb.writeLine("  if (title != null) {");
		hpb.writeLine("    var titleDiv = newElement('div', null, 'alertTitle', title);");
		hpb.writeLine("    titleDiv.style.align = 'center';");
		hpb.writeLine("    alertDiv.appendChild(titleDiv);");
		hpb.writeLine("  }");
		//	get icon name (if any)
		hpb.writeLine("  var iconName = null;");
		hpb.writeLine("  if (type == " + JOptionPane.QUESTION_MESSAGE + ")");
		hpb.writeLine("    iconName = 'question.png';");
		hpb.writeLine("  else if (type == " + JOptionPane.WARNING_MESSAGE + ")");
		hpb.writeLine("    iconName = 'warning.png';");
		hpb.writeLine("  else if (type == " + JOptionPane.ERROR_MESSAGE + ")");
		hpb.writeLine("    iconName = 'error.png';");
		hpb.writeLine("  else if (type == " + JOptionPane.INFORMATION_MESSAGE + ")");
		hpb.writeLine("    iconName = 'info.png';");
		//	add message, line-wise
		hpb.writeLine("  var messageDiv = newElement('div', null, 'alertMessage', null);");
		hpb.writeLine("  var messageParts = message.split(/(\\r\\n|\\r|\\n)/);");
		hpb.writeLine("  for (var p = 0; p < messageParts.length; p++) {");
		hpb.writeLine("    if (p != 0)");
		hpb.writeLine("      messageDiv.appendChild(newElement('br', null, null, null));");
		hpb.writeLine("    messageDiv.appendChild(document.createTextNode(messageParts[p]));");
		hpb.writeLine("  }");
		//	simply add message if we don't have an icon
		hpb.writeLine("  if (iconName == null)");
		hpb.writeLine("    alertDiv.appendChild(messageDiv);");
		//	add message and icon in table to keep icon to right of message
		hpb.writeLine("  else {");
		hpb.writeLine("    var icon = newElement('img', null, 'alertIcon', null);");
		hpb.writeLine("    icon.src = ('" + hpb.request.getContextPath() + this.parent.getStaticResourcePath() + "/' + iconName);");
		hpb.writeLine("    var iconDiv = newElement('div', null, 'alertIcon', null);");
		hpb.writeLine("    iconDiv.appendChild(icon);");
		hpb.writeLine("    var amtd = newElement('td', null, null, null);");
		hpb.writeLine("    amtd.appendChild(messageDiv);");
		hpb.writeLine("    var aitd = newElement('td', null, null, null);");
		hpb.writeLine("    aitd.appendChild(iconDiv);");
		hpb.writeLine("    var atr = newElement('tr', null, null, null);");
		hpb.writeLine("    atr.appendChild(amtd);");
		hpb.writeLine("    atr.appendChild(aitd);");
		hpb.writeLine("    var at = newElement('table', null, null, null);");
		hpb.writeLine("    at.appendChild(atr);");
		hpb.writeLine("    alertDiv.appendChild(at);");
		hpb.writeLine("  }");
		//	add close button
		hpb.writeLine("  var alertButton = newElement('button', null, 'alertButton', 'OK');");
		hpb.writeLine("  alertButton.onclick = function() {");
		hpb.writeLine("    removeElement(alertDiv);");
		hpb.writeLine("    removeElement(alertOverlay);");
		hpb.writeLine("  }");
		hpb.writeLine("  alertDiv.appendChild(alertButton);");
		//	show alert
		hpb.writeLine("  alertOverlay.appendChild(alertDiv);");
		hpb.writeLine("}");
		
		//	open confirm dialog (in an IFRAME)
		hpb.writeLine("function showConfirmDialog() {");
//		hpb.writeLine("  window.open(('" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + this.id + "/confirm'), 'Please Confirm', 'width=50,height=50,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes');");
		hpb.writeLine("  window.open(('" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + this.id + "/confirm'), 'Please Confirm', 'left=center,top=center,title=no');");
		hpb.writeLine("}");
		
		//	open feedback dialog (in an IFRAME)
		hpb.writeLine("function showFeedbackDialog() {");
//		hpb.writeLine("  window.open(('" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + this.id + "/askFeedback'), 'Please Give Feedback', 'width=50,height=50,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes');");
		hpb.writeLine("  window.open(('" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + this.id + "/askFeedback'), 'Please Give Feedback', 'left=center,top=center,resizable=yes');");
		hpb.writeLine("}");
		
		//	show word editor
		hpb.writeLine("function doEditWord(wordId) {");
		hpb.writeLine("  var query = ((wordId == null) ? '' : ('?wordId=' + wordId));");
		//	no word ID set: modal, no title, fixed size, centered
		hpb.writeLine("  var wWindowFeatures = 'left=center,top=center,title=no';");
		hpb.writeLine("  var word = ((wordId == null) ? null : wordsById[wordId]);");
		//	valid word ID set: volatile, undecorated, top left corner aligned with word
		hpb.writeLine("  if ((word != null) && (word.div != null)) {");
		hpb.writeLine("    var wPos = word.div.getBoundingClientRect();");
		hpb.writeLine("    wWindowFeatures = ('left=' + wPos.left + ',top=' + wPos.top + ',undecorated=yes,volatile=yes');");
		hpb.writeLine("  }");
		hpb.writeLine("  window.open(('" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + id + "/editWord' + query), 'editWordWindow', wWindowFeatures);");
		hpb.writeLine("}");
		
		hpb.writeLine("</script>");
		
		//	include styles for alert and confirm dialogs
		String alertStyles = this.getAlertStyles();
		if (alertStyles != null) {
			hpb.writeLine("<style type=\"text/css\">");
			hpb.writeLine(alertStyles);
			hpb.writeLine("</style>");
		}
	}
	
	private String alertStyles = null;
	private String getAlertStyles() {
		if (this.alertStyles != null)
			return this.alertStyles;
		
		//	load and cache styles from file (if available)
		File alertStylesFile = this.findFile("alert.css");
		if (alertStylesFile != null) try {
			BufferedReader asIn = new BufferedReader(new InputStreamReader(new FileInputStream(alertStylesFile), "UTF-8"));
			StringBuffer asLoader = new StringBuffer();
			char[] asBuffer = new char[1024];
			for (int r; (r = asIn.read(asBuffer, 0, asBuffer.length)) != -1;)
				asLoader.append(asBuffer, 0, r);
			asIn.close();
			this.alertStyles = asLoader.toString();
		} catch (IOException ioe) {}
		
		//	finally ...
		return this.alertStyles;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getOnloadCalls()
	 */
	public String[] getOnloadCalls() {
		return this.parent.getOnloadCalls();
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost#getOnunloadCalls()
	 */
	public String[] getOnunloadCalls() {
		return this.parent.getOnunloadCalls();
	}
	
	boolean handleRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
		
		//	handle request for base page
		if (pathInfo == null) {
			this.sendEditorPage(request, response);
			return true;
		}
		
		//	handle requests to document views
		if ((this.docView != null) && (pathInfo.equals("/view") || pathInfo.startsWith("/view/"))) {
			
			//	request for view page
			if (pathInfo.equals("/view") && "GET".equalsIgnoreCase(request.getMethod())) {
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				HtmlPageBuilder viewPageBuilder = this.docView.getViewPageBuilder(this, request, response);
				Reader viewBasePage = this.docView.getViewBasePage();
				if (viewBasePage == null)
					this.parent.sendPopupHtmlPage(viewPageBuilder);
				else this.parent.sendHtmlPage(viewBasePage, viewPageBuilder);
				return true;
			}
			
			//	request directed at view
			if (pathInfo.startsWith("/view/")) {
				pathInfo = pathInfo.substring("/view".length());
				
				//	closing request directed at view
				if (this.docView.isCloseRequest(request, pathInfo)) {
					this.docView.close();
					
					//	get JavaScript calls, and direct them at parent window
					String fbjsc = this.docViewActionThread.getFinalOrBlockingJavaScriptCall();
					String[] jscs = this.idmp.getJavaScriptCalls();
					String[] aJscs = new String[jscs.length + 1];
					for (int c = 0; c < jscs.length; c++)
						aJscs[c] = ("window.opener." + jscs[c]);
					aJscs[jscs.length] = ("window.opener." + fbjsc);
					
					//	clear document view
					this.docView = null;
					this.docViewActionThread = null;
					
					//	send window closing page, including update script
					response.setContentType("text/html");
					response.setCharacterEncoding("UTF-8");
					this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, aJscs));
					return true;
				}
				
				//	request directed at view, handle in action thread to facilitate confirm() prompts
				ActionThread at = new ActionThread(request, response, pathInfo) {
					public void execute() throws Exception {
						docView.handleRequest(this.docViewRequest, this.docViewResponse, this.docViewPathInfo);
					}
				};
				at.start();
				
				//	wait for action thread to finish or block
				String fbjsc = at.getFinalOrBlockingJavaScriptCall();
				
				//	if request belongs to document view, only send out final call if it is another confirm prompt
				if (at.isDocViewAction()) {
					if ("".equals(fbjsc))
						fbjsc = null; // document view has already written the response
				}
				
				//	send blocking JavaScript call (if any)
				if (fbjsc != null) {
					response.setContentType("text/plain");
					response.setCharacterEncoding("UTF-8");
					Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
					BufferedLineWriter blw = new BufferedLineWriter(out);
					blw.writeLine(fbjsc);
					blw.flush();
					out.flush();
					blw.close();
				}
				
				//	finally ...
				return true;
			}
		}
		
		//	handle requests for attribute editor
		if (pathInfo.equals("/editAttributes")) {
			this.handleEditAttributes(request, response);
			return true;
		}
		
		//	handle requests for attribute editor
		if (pathInfo.equals("/editWord")) {
			this.handleEditWord(request, response);
			return true;
		}
		
		//	handle requests for confirm() prompts
		if (pathInfo.equals("/confirm")) {
			this.sendConfirmPage(request, response);
			return true;
		}
		
		//	handle responses for confirm() prompts
		if (pathInfo.equals("/confirm.js")) {
			this.handleConfirm(request, response);
			return true;
		}
		
		//	handle requests for getFeedback() prompts
		if (pathInfo.equals("/askFeedback")) {
			this.sendFeedbackPage(request, response);
			return true;
		}
		
		//	handle responses for getFeedback() prompts
		if (pathInfo.equals("/giveFeedback")) {
			this.handleFeedback(request, response);
			return true;
		}
		
		//	handle requests for clipboard content
		if (pathInfo.equals("/copy")) {
			this.sendClipboardContent(response);
			return true;
		}
		
		//	handle context menu action
		if (pathInfo.equals("/cmAction.js")) {
			this.handleContextMenuAction(request, response);
			return true;
		}
		
		//	handle requests for context menu options
		if (pathInfo.equals("/cmActions.js")) {
			this.sendContextMenuActions(request, response);
			return true;
		}
		
		//	handle main menu action
		if (pathInfo.equals("/mmAction.js")) {
			this.handleMainMenuAction(request, response);
			return true;
		}
		
		//	handle main menu action status
		if (pathInfo.equals("/mmActionStatus.js")) {
			this.sendMainMenuActionStatus(request, response);
			return true;
		}
		
		//	handle main menu action status page
		if (pathInfo.equals("/mmActionStatus")) {
			this.sendMainMenuActionStatusPage(request, response);
			return true;
		}
		
		//	handle request for menu action result page
		if (pathInfo.equals("/mmActionResult")) {
			this.sendMainMenuActionResultPage(request, response);
			return true;
		}
		
//		//	handle main menu action result
//		if (pathInfo.equals("/mmActionResult.js")) {
//			this.sendMainMenuActionResult(request, response);
//			return true;
//		}
//		
//		//	handle request for menu action result page
//		if (pathInfo.equals("/mmActionResult")) {
//			this.sendMainMenuActionResultPage(request, response);
//			return true;
//		}
//		
		//	handle requests for export download
		if (pathInfo.equals("/export")) {
			this.handleExportAction(request, response);
			return true;
		}
		
		//	handle undo action
		if (pathInfo.equals("/undoAction.js")) {
			this.handleUndoAction(request, response);
			return true;
		}
		
		//	handle drop action
		if (pathInfo.equals("/dropAction.js")) {
			this.handleDropAction(request, response);
			return true;
		}
		
		//	handle requests for main menu content
		if (pathInfo.equals("/mmActions.js")) {
			this.parent.sendMainMenuActions(request, response, this);
			return true;
		}
		
		//	handle requests for UI settings
		if (pathInfo.equals("/settings.js")) {
			this.sendDisplaySettings(request, response);
			return true;
		}
		
		//	handle requests for page image
		if (pathInfo.startsWith("/pageImage/")) {
			this.sendPageImage(pathInfo.substring("/pageImage/".length()), response);
			return true;
		}
		
		//	handle requests for page image
		if (pathInfo.startsWith("/wordImage/")) {
			this.sendWordImage(pathInfo.substring("/wordImage/".length()), response);
			return true;
		}
		
		//	handle requests for documents
		if (pathInfo.equals("/loadDoc.js")) {
			this.sendDocument(request, response);
			return true;
		}
		
		//	handle requests for status (on reload)
		if (pathInfo.equals("/loadDoc.js")) {
			this.sendDocument(request, response);
			return true;
		}
		
		//	indicate we couldn't handle this one
		return false;
	}
	
	private void sendEditorPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	send page, generating loading scripts to end of body
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.parent.sendHtmlPage(new HtmlPageBuilder(this, request, response) {
			protected boolean includeJavaScriptDomHelpers() {
				return true;
			}
			protected void include(String type, String tag) throws IOException {
				if ("includeBody".equals(type)) {
					this.includeFile("editor.html");
					writeJavaScriptTag(this, "settings.js");
					writeJavaScriptTag(this, "mmActions.js");
					parent.writeJavaScriptTag(this, "help.js", true);
					writeJavaScriptTag(this, "loadDoc.js");
					this.writeLine("<script id=\"dynamicActionScript\" type=\"text/javascript\" src=\"toBeSetDynamically\"></script>");
					//this.writeLine("<script id=\"dynamicConfirmScript\" type=\"text/javascript\" src=\"toBeSetDynamically\"></script>");
					
					//	restore UNDO menu entries
					String[] umbJscs = idmp.getUndoMenuBuilderJavaScriptCalls();
					if (umbJscs.length != 0) {
						this.writeLine("<script type=\"text/javascript\">");
						for (int c = 0; c < umbJscs.length; c++)
							this.writeLine(umbJscs[c]);
						this.writeLine("</script>");
					}
					
					//	if we have an IM Tool running, open status page
//					String runningImToolId = parent.getRunningMainMenuActionImToolId(GoldenGateImagineServletEditor.this);
//					if (runningImToolId != null) {
//						this.writeLine("<script type=\"text/javascript\">");
////						this.writeLine("window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/mmActionStatus/" + runningImToolId + "', 'mainMenuImToolStatus', 'width=50,height=50,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes')");
//						this.writeLine("window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/mmActionStatus/" + runningImToolId + "', 'mainMenuImToolStatus', 'left=center,top=center,title=no')");
//						this.writeLine("</script>");
//					}
					if (mainMenuActionThread != null) {
						this.writeLine("<script type=\"text/javascript\">");
						this.writeLine("openProgressMonitor();");
						this.writeLine("</script>");
					}
					
					//	if we have an active action thread waiting for feedback, send its respective call
					if (actionThreadAwaitingFeedback != null) {
						this.writeLine("<script type=\"text/javascript\">");
						this.writeLine("showFeedbackDialog();");
						this.writeLine("</script>");
					}
					
					//	if we have an active action thread waiting for a word edit, send its respective call
					if (actionThreadEditingWord != null) {
						this.writeLine("<script type=\"text/javascript\">");
						this.writeLine("doEditWord(null);");
						this.writeLine("</script>");
					}
					
					//	restore any open document view
					if (docView != null) {
						this.writeLine("<script type=\"text/javascript\">");
//						this.writeLine("window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/view', 'documentView', 'width=50,height=50,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes');");
						this.writeLine("window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/view', 'documentView', 'left=center,top=center,title=no,resizable=yes');");
						this.writeLine("</script>");
					}
					
					//	if we have an active action thread waiting for a confirmation, send its confirm call
					if (actionThreadAwaitingConfirm != null) {
						this.writeLine("<script type=\"text/javascript\">");
						this.writeLine("showConfirmDialog();");
						this.writeLine("</script>");
					}
				}
				else super.include(type, tag);
			}
			protected void writePageHeadExtensions() throws IOException {
				parent.writeJavaScriptTag(this, "dataHelpers.js", false);
				parent.writeJavaScriptTag(this, "menuHelpers.js", false);
				parent.writeJavaScriptTag(this, "displayHelpers.js", false);
				parent.writeJavaScriptTag(this, "updateHelpers.js", false);
				parent.writeJavaScriptTag(this, "interactHelpers.js", false);
				this.writeLine("<script type=\"text/javascript\">");
//				
//				//	open feedback dialog (in an IFRAME)
//				this.writeLine("function showFeedbackDialog() {");
////				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/askFeedback'), 'Please Give Feedback', 'width=50,height=50,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes');");
//				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/askFeedback'), 'Please Give Feedback', 'left=center,top=center,resizable=yes');");
//				this.writeLine("}");
				
				//	perform some dynamic action via script tag replacement
				this.writeLine("function getDynamicActionScript(name, params) {");
				this.writeLine("  var das = getById('dynamicActionScript');");
				this.writeLine("  var dasp = das.parentNode;");
				this.writeLine("  removeElement(das);");
				this.writeLine("  var dasSrc = ('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/' + name + '?time=' + (new Date()).getTime() + params);");
				this.writeLine("  das = newElement('script', 'dynamicActionScript');");
				this.writeLine("  das.type = 'text/javascript';");
				this.writeLine("  das.src = dasSrc;");
				this.writeLine("  dasp.appendChild(das);");
				this.writeLine("  return false;");
				this.writeLine("}");
				
				//	call to cmActions.js
				this.writeLine("function showContextMenu(params) {");
				this.writeLine("  return getDynamicActionScript('cmActions.js', params);");
				this.writeLine("}");
				
				//	call to cmAction.js
				this.writeLine("function doContextMenuAction(actionId) {");
				this.writeLine("  return getDynamicActionScript('cmAction.js', ('&actionId=' + actionId));");
				this.writeLine("}");
				
				//	call to mmAction.js
				this.writeLine("function doMainMenuAction(actionId, params) {");
				this.writeLine("  return getDynamicActionScript('mmAction.js', ('&actionId=' + actionId + ((params == null) ? '' : params)));");
				this.writeLine("}");
				
				//	call to mmActionResult.js
				this.writeLine("function doFinishMainMenuAction() {");
				this.writeLine("  return getDynamicActionScript('mmActionResult.js', '');");
				this.writeLine("}");
				
				//	call to undoAction.js
				this.writeLine("function doUndoAction(actionId) {");
				this.writeLine("  return getDynamicActionScript('undoAction.js', ('&actionId=' + actionId));");
				this.writeLine("}");
				
				//	call to dropAction.js
				this.writeLine("function doDropAction(wordId, dropped, params) {");
				this.writeLine("  return getDynamicActionScript('dropAction.js', ('&wordId=' + wordId + '&dropped=' + encodeURIComponent(dropped) + params));");
				this.writeLine("}");
				
				//	show copied contents TODO_ne maybe refrain from re-using window
				this.writeLine("function showCopiedContent() {");
				//	TODO_not_here: extra window
				this.writeLine("  window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/copy', 'clipboardContentWindow', 'width=500,height=400,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes', true);");
				this.writeLine("}");
				
				//	open progress monitor for main menu action
				this.writeLine("function openProgressMonitor() {");
				this.writeLine("  window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/mmActionStatus', 'mainMenuImToolStatus', 'left=center,top=center,title=no')");
				this.writeLine("}");
				
				//	show attribute editor
				this.writeLine("function doEditAttributes(id) {");
//				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editAttributes?id=' + id), 'editAttributesWindow', 'width=50,height=50,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes');");
				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editAttributes?id=' + id), 'editAttributesWindow', 'left=center,top=center,title=no,resizable=yes');");
				this.writeLine("}");
//				
//				//	show attribute editor
//				this.writeLine("function doEditWord(wordId) {");
//				//	TODO_above word ID set: volatile, undecorated, top left corner aligned with word (get them coordinates, best from word DIV !!!)
//				//	TODO_above no word ID: modal, no title, fixed size, centered
//				//	left=center,top=center,title=no,resizable=yes
//				this.writeLine("  var query = ((wordId == null) ? '' : ('?wordId=' + wordId));");
//				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editWord' + query), 'editWordWindow', 'width=50,height=50,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes');");
//				this.writeLine("}");
				
				this.writeLine("</script>");
				
				//	for born-digital documents, hide OCR overlay control
				if (idmp.documentBornDigital) {
					this.writeLine("<style type=\"text/css\">");
					this.writeLine("#control_ocrOverlay {");
					this.writeLine("  display: none !important;");
					this.writeLine("}");
					this.writeLine("</style>");
				}
			}
		});
	}
	
	private void sendConfirmPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	do we have an action thread waiting for a confirmation?
		if (this.actionThreadAwaitingConfirm == null) {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid document ID '" + this.id + "'"));
			//	fail gracefully
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, null));
			return;
		}
		
		//	send form page
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.parent.sendHtmlPage("confirm.html", this.actionThreadAwaitingConfirm.getConfirmPageBuilder(request, response));
	}
	
	private void handleConfirm(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	do we have an action thread waiting for a confirm response?
		if (this.actionThreadAwaitingConfirm == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid editor ID '" + this.id + "'"));
			return;
		}
		
		//	remember action thread locally, as confirmation clears field
		ActionThread at = this.actionThreadAwaitingConfirm;
		
		//	if the request belongs to a document view, set its request and response to the argument pair (before injecting input !!!)
		if (at.isDocViewAction())
			at.setRequestData(request, response);
		
		//	inject parameters
		at.confirmed(request);
		
		//	wait for action thread to finish or block again
		String fbjsc = at.getFinalOrBlockingJavaScriptCall();
		String[] jscs;
		
		//	if request belongs to document view, only send out final call if it is another confirm prompt
		if (at.isDocViewAction()) {
			if ("".equals(fbjsc))
				fbjsc = null; // document view has already written the response
			jscs = new String[0];
		}
		
		//	send out all JavaScript calls resulting from action otherwise
		else jscs = this.idmp.getJavaScriptCalls();
		
		//	send JavaScript calls up to this point (if any)
		if (fbjsc != null) {
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			for (int c = 0; c < jscs.length; c++)
				blw.writeLine(jscs[c]);
			blw.writeLine(fbjsc);
			blw.flush();
			out.flush();
			blw.close();
		}
	}
	
	private void sendFeedbackPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	do we have an action thread waiting for a confirmation?
		if (this.actionThreadAwaitingFeedback == null) {
//			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid document ID '" + this.id + "'"));
			//	fail gracefully
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, null));
			return;
		}
		
		//	send form page
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.parent.sendHtmlPage("feedback.html", this.actionThreadAwaitingFeedback.getFeedbackFormPageBuilder(request, response));
	}
	
	private void handleFeedback(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	do we have an action thread waiting for a confirm response?
		if (this.actionThreadAwaitingFeedback == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid editor ID '" + this.id + "'"));
			return;
		}
		
		//	remember action thread locally, as giving feedback clears field
		ActionThread at = this.actionThreadAwaitingFeedback;
		
		//	check if we're in a main menu action (data updates are no good on progress monitor !!)
		boolean isMainMenuAction = (this.mainMenuActionThread != null);
		
		//	inject feedback
		at.processFeedback(request);
		
		/* TODO figure out "Unexpected Token" in page closer
		 * - it actually all looks OK, perfectly fine ...
		 * - ... likely a "DOOH" kind of error
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="/GgiWebTest/resources/popin.js"></script>
</head>
<body>
<script type="text/javascript">
function doExecuteCalls() {
window.opener.showFeedbackDialog();
}
function executeCalls() {
  if (window.opener && (window.opener != null)) {
    doExecuteCalls();
    window.close();
  }
  else window.setTimeout('executeCalls()', 100);
}
window.setTimeout('executeCalls()', 100);
</script>
</body>
</html>
		 */
		
		//	get JavaScript calls, and direct them at parent window
		String[] aJscs;
		if (isMainMenuAction) {
			String fbjsc = at.getFinalOrBlockingJavaScriptCall();
			aJscs = new String[1];
			aJscs[0] = ("window.opener." + fbjsc);
		}
		else {
			String fbjsc = at.getFinalOrBlockingJavaScriptCall();
			String[] jscs = this.idmp.getJavaScriptCalls();
			aJscs = new String[jscs.length + 1];
			for (int c = 0; c < jscs.length; c++)
				aJscs[c] = ("window.opener." + jscs[c]);
			aJscs[jscs.length] = ("window.opener." + fbjsc);
		}
		
		//	send window closing page, including update script
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, aJscs));
	}
	
	private void sendClipboardContent(HttpServletResponse response) throws IOException {
		
		//	check clipboard content
		if (this.clipboardData == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid copy ID '" + this.id + "'");
			return;
		}
		
		//	get data flavors
		DataFlavor[] dfs = this.clipboardData.getTransferDataFlavors();
		if (dfs.length == 0) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not determine data flavor");
			return;
		}
		
		//	TODO offer this stuff as downloads, just like export results ...
		//	... so users can open it in an application right away, ...
		//	... unless it's HTML flavored
		//	==> TODO support more data flavors
		
		//	send plain text or image
		for (int f = 0; f < dfs.length; f++) try {
			if (DataFlavor.imageFlavor.equals(dfs[f])) {
				Object dfo = this.clipboardData.getTransferData(dfs[f]);
				if (dfo instanceof RenderedImage) {
					response.setContentType("image/png");
					BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
					ImageIO.write(((RenderedImage) dfo), "PNG", out);
					out.flush();
					out.close();
					return;
				}
			}
			else if (DataFlavor.stringFlavor.equals(dfs[f])) {
				Object dfo = this.clipboardData.getTransferData(dfs[f]);
				if (dfo instanceof String) {
					response.setContentType("text/plain");
					response.setCharacterEncoding("UTF-8");
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
					out.write((String) dfo);
					out.flush();
					out.close();
					return;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
		//	send error if none of the data flavors matched
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, ("Data flavor '" + dfs[0].getMimeType() + "' currently not supported"));
	}
	
	private void handleEditAttributes(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	request for attribute editor page
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			
			//	get editing target
			Attributed target = GoldenGateImagineWebUtils.getAttributed(this.idmp.document, request.getParameter("id"));
			if (target == null) {
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, null));
				return;
			}
			
			//	send attribute editor form page
			this.sendEditAttributesForm(request, response, target);
		}
		
		//	submission of attribute editor form
		else if ("POST".equalsIgnoreCase(request.getMethod())) {
			
			//	get editing target
			Attributed target = GoldenGateImagineWebUtils.getAttributed(this.idmp.document, request.getParameter("id"));
			
			//	perform attribute updates
			if (target != null) {
				this.idmp.beginAtomicAction(GoldenGateImagineWebUtils.getAttributeEditActionLabel(target));
				if (GoldenGateImagineWebUtils.processAttributeEditorSubmission(target, request))
					this.idmp.endAtomicAction();
			}
			
			//	get next target (if any)
			Attributed nextTarget = GoldenGateImagineWebUtils.getAttributed(this.idmp.document, request.getParameter("nextId"));
			
			//	no next target given, close attribute editor
			if (nextTarget == null) {
				
				//	get JavaScript calls, and direct them at parent window
				String[] jscs = this.idmp.getJavaScriptCalls();
				for (int c = 0; c < jscs.length; c++)
					jscs[c] = ("window.opener." + jscs[c]);
				
				//	send window closing page, including update script
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, jscs));
			}
			
			//	we have a next target, send another editor page
			else this.sendEditAttributesForm(request, response, nextTarget);
		}
		
		//	some weird request
		else response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("'" + request.getMethod() + "' not allowed for attribute editing"));
	}
	
	private void sendEditAttributesForm(HttpServletRequest request, HttpServletResponse response, final Attributed target) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.parent.sendHtmlPage("editAttributes.html", new AttributeEditorPageBuilder(this, request, response, target, (request.getContextPath() + request.getServletPath() + "/" + id + "/editAttributes")) {
			private Attributed[] targetContext;
			private Attributed prevTarget = null;
			private Attributed nextTarget = null;
			protected void writePageHeadExtensions() throws IOException {
				super.writePageHeadExtensions();
				
				//	submit attribute form with mode
				this.writeLine("<script type=\"text/javascript\">");
				this.writeLine("function doSubmitDataAttributes(mode) {");
				this.writeLine("  var attrForm = getById('attributeForm');");
				this.writeLine("  if (mode == 'C') {");
				this.writeLine("    while (attrForm.firstElementChild)");
				this.writeLine("      removeElement(attrForm.firstElementChild);");
				this.writeLine("    attrForm.submit();");
				this.writeLine("    return;");
				this.writeLine("  }");
				this.writeLine("  var nextId = null;");
				if (this.prevTarget != null) {
					this.writeLine("  if (mode == 'P')");
					this.writeLine("    nextId = '" + GoldenGateImagineWebUtils.getAttributedId(this.prevTarget) + "';");
				}
				if (this.nextTarget != null) {
					this.writeLine("  if (mode == 'N')");
					this.writeLine("    nextId = '" + GoldenGateImagineWebUtils.getAttributedId(this.nextTarget) + "';");
				}
				this.writeLine("  if (nextId != null) {");
				this.writeLine("    var nextIdField = newElement('input', null, null, null);");
				this.writeLine("    nextIdField.type = 'hidden';");
				this.writeLine("    nextIdField.name = 'nextId';");
				this.writeLine("    nextIdField.value = nextId;");
				this.writeLine("    attrForm.appendChild(nextIdField);");
				this.writeLine("  }");
				this.writeLine("  submitDataAttributes();");
				this.writeLine("}");
				this.writeLine("</script>");
			}
			protected Attributed[] getContext(Attributed target) {
				this.targetContext = super.getContext(target);
				if (this.targetContext != null) {
					if (target instanceof ImWord) {
						this.prevTarget = ((ImWord) target).getPreviousWord();
						this.nextTarget = ((ImWord) target).getNextWord();
					}
					else if (target instanceof ImAnnotation) {
						for (int c = 0; c < this.targetContext.length; c++)
							if (this.targetContext[c] == target) {
								this.prevTarget = ((c == 0) ? null: this.targetContext[c-1]);
								this.nextTarget = (((c+1) == this.targetContext.length) ? null: this.targetContext[c+1]);
								break;
							}
					}
					else if (target instanceof ImRegion) {
						Arrays.sort(this.targetContext, ImUtils.topDownOrder);
						for (int c = 0; c < this.targetContext.length; c++)
							if (this.targetContext[c] == target) {
								this.prevTarget = ((c == 0) ? null: this.targetContext[c-1]);
								this.nextTarget = (((c+1) == this.targetContext.length) ? null: this.targetContext[c+1]);
								break;
							}
					}
				}
				return this.targetContext;
			}
			protected SubmitButton[] getButtons() {
				SubmitButton[] sbs = {
					((this.prevTarget == null) ? null : new SubmitButton("Previous", "doSubmitDataAttributes('P');")),
					new SubmitButton("OK", "doSubmitDataAttributes('O');"),
					new SubmitButton("Cancel", "doSubmitDataAttributes('C');"),
					((this.nextTarget == null) ? null : new SubmitButton("Next", "doSubmitDataAttributes('N');")),
					new SubmitButton("Reset", "window.location.reload();")
				};
				return sbs;
			}
		});
	}
	
	private void handleEditWord(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	request for word editor page
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			
			//	prepare response (we need to do this before wrapping a page builder around the incoming data)
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			ImWord word;
//			HtmlPageBuilder ewpb;
			
			//	do we have an action thread waiting for a word edit?
			if (this.actionThreadEditingWord != null)
//				ewpb = this.actionThreadEditingWord.getEditWordPageBuilder(request, response);
				word = this.actionThreadEditingWord.getEditingWord();
			
			//	
			else {
				
				//	get target word
//				ImWord word = this.idmp.document.getWord(request.getParameter("wordId"));
				word = this.idmp.document.getWord(request.getParameter("wordId"));
				if (word == null) {
					this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, null));
					return;
				}
//				
//				//	create page builder
//				ewpb = this.getEditWordFormBuilder(request, response, word);
			}
			
			//	send form page
//			this.parent.sendHtmlPage("editWord.html", ewpb);
			this.parent.sendHtmlPage("editWord.html", this.getEditWordFormBuilder(request, response, word));
		}
		
		//	submission of word editor form
		else if ("POST".equalsIgnoreCase(request.getMethod())) {
			String[] cpJscs;
			
			//	do we have an action thread waiting for a word edit?
			if (this.actionThreadEditingWord != null) {
				
				//	remember action thread locally, as injecting result clears field
				ActionThread at = this.actionThreadEditingWord;
				
				//	inject word edit
				at.wordEdited(request);
				
				//	get JavaScript calls, and direct them at parent window
				String fbjsc = at.getFinalOrBlockingJavaScriptCall();
				String[] jscs = this.idmp.getJavaScriptCalls();
				cpJscs = new String[jscs.length + 1];
				for (int c = 0; c < jscs.length; c++)
					cpJscs[c] = ("window.opener." + jscs[c]);
				cpJscs[jscs.length] = ("window.opener." + fbjsc);
			}
			
			//	handle UI initiated word edit
			else {
				
				//	get target word if dialog committed
				ImWord word = ("true".equals(request.getParameter("wordEdited")) ? this.idmp.document.getWord(request.getParameter("wordId")) : null);
				
				//	perform word updates
				if (word != null) {
					this.idmp.beginAtomicAction("Edit Word '" + word.getString() + "'");
					String string = request.getParameter(ImWord.STRING_ATTRIBUTE);
					boolean bold = "true".equals(request.getParameter(ImWord.BOLD_ATTRIBUTE));
					boolean italics = "true".equals(request.getParameter(ImWord.ITALICS_ATTRIBUTE));
					if (string != null) {
						string = string.trim();
						if (!string.equals(word.getString()))
							word.setString(string);
					}
					if (bold != word.hasAttribute(ImWord.BOLD_ATTRIBUTE))
						word.setAttribute(ImWord.BOLD_ATTRIBUTE, (bold ? "true" : null));
					if (italics != word.hasAttribute(ImWord.ITALICS_ATTRIBUTE))
						word.setAttribute(ImWord.ITALICS_ATTRIBUTE, (italics ? "true" : null));
					this.idmp.endAtomicAction();
				}
				
				//	get JavaScript calls, and direct them at parent window
				cpJscs = this.idmp.getJavaScriptCalls();
				for (int c = 0; c < cpJscs.length; c++)
					cpJscs[c] = ("window.opener." + cpJscs[c]);
			}
			
			//	send window closing page, including update script
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, cpJscs));
		}
		
		//	some weird request
		else response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("'" + request.getMethod() + "' not allowed for word editing"));
	}
	
	private HtmlPageBuilder getEditWordFormBuilder(HttpServletRequest request, HttpServletResponse response, final ImWord word) throws IOException {
		return new HtmlPageBuilder(this, request, response) {
			protected void include(String type, String tag) throws IOException {
				if ("includeTitle".equals(type))
					this.write(html.escape("Edit Word '" + word.getString() + "'"));
				else if ("includeWordImage".equals(type))
					this.write("<img src=\"" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/wordImage/" + word.getLocalID() + ".png\" width=\"" + (word.bounds.right - word.bounds.left) + "\" height=\"" + (word.bounds.bottom - word.bounds.top) + "\" />");
				else if ("includeForm".equals(type)) {
					this.writeLine("<form id=\"wordAttributeForm\" method=\"POST\" action=\"" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editWord\" style=\"display: none;\">");
					this.writeLine("<input type=\"hidden\" name=\"wordId\" value=\"" + word.getLocalID() + "\"/>");
					this.writeLine("<input type=\"hidden\" id=\"wordString\" name=\"" + ImWord.STRING_ATTRIBUTE + "\" value=\"" + html.escape(word.getString()) + "\"/>");
					this.writeLine("<input type=\"hidden\" id=\"wordIsBold\" name=\"" + ImWord.BOLD_ATTRIBUTE + "\" value=\"" + (word.hasAttribute(ImWord.BOLD_ATTRIBUTE) ? "true" : "false") + "\"/>");
					this.writeLine("<input type=\"hidden\" id=\"wordIsItalics\" name=\"" + ImWord.ITALICS_ATTRIBUTE + "\" value=\"" + (word.hasAttribute(ImWord.ITALICS_ATTRIBUTE) ? "true" : "false") + "\"/>");
					this.writeLine("<input type=\"hidden\" id=\"wordEdited\" name=\"wordEdited\" value=\"false\"/>");
					this.writeLine("</form>");
				}
				else if ("includeInitCalls".equals(type)) {
					this.writeLine("<script type=\"text/javascript\">");
					this.writeLine("getById('wordStringField').value = getById('wordString').value;");
					this.writeLine("getById('wordIsBoldField').checked = ((getById('wordIsBold').value == 'true') ? 'checked' : null);");
					this.writeLine("getById('wordIsItalicsField').checked = ((getById('wordIsItalics').value == 'true') ? 'checked' : null);");
					this.writeLine("</script>");
				}
				else super.include(type, tag);
			}
			protected boolean includeJavaScriptDomHelpers() {
				return true;
			}
			protected void writePageHeadExtensions() throws IOException {
				
				//	open script and create attribute value index
				this.writeLine("<script type=\"text/javascript\">");
				
				//	open 'symbolTable' from main servlet ...
				this.writeLine("var symbolTable = null;");
				this.writeLine("function openSymbolTable() {");
				this.writeLine("  var iframes = window.opener.document.getElementsByTagName('iframe');");
				this.writeLine("  var left = 0;");
				this.writeLine("  var top = 0;");
				this.writeLine("  for (var i = 0; i < iframes.length; i++)");
				this.writeLine("    if (iframes[i].src.indexOf('/editWord') != -1) {");
				this.writeLine("      var pos = getSymbolTablePostion(iframes[i]);");
				this.writeLine("      left = (pos.left + iframes[i].offsetWidth);");
				this.writeLine("      top = pos.top;");
				this.writeLine("      break;");
				this.writeLine("    }");
				this.writeLine("  symbolTable = window.open('" + this.request.getContextPath() + this.request.getServletPath() + "/symbolTable', 'symbolTable', ('left=' + left + ',top=' + top + ',resizable=no,volatile=yes,title=no'));");
				this.writeLine("  window.setTimeout('linkSymbolTable()', 50);");
				this.writeLine("}");
				
				//	... to right of word dialog ...
				this.writeLine("function getSymbolTablePostion(elem) {");
				this.writeLine("  var left = 0;");
				this.writeLine("  var top = 0;");
				this.writeLine("  while(elem != null) {");
				this.writeLine("    left += elem.offsetLeft;");
				this.writeLine("    top += elem.offsetTop;");
				this.writeLine("    elem = elem.offsetParent;");
				this.writeLine("  }");
				this.writeLine("  var pos = new Object();");
				this.writeLine("  pos.left = left;");
				this.writeLine("  pos.top = top;");
				this.writeLine("  return pos;");
				this.writeLine("}");
				
				//	... and replace its useSymbol() function with something useful
				this.writeLine("function linkSymbolTable() {");
				this.writeLine("  if (!symbolTable.useSymbol) {");
				this.writeLine("    window.setTimeout('linkSymbolTable()', 50);");
				this.writeLine("    return;");
				this.writeLine("  }");
				this.writeLine("  symbolTable.useSymbol = function(symbol) {");
				this.writeLine("    var wsf = getById('wordStringField');");
				this.writeLine("    wsf.value = (wsf.value + symbol);");
				this.writeLine("  };");
				this.writeLine("  symbolTable.onunload = function() {");
				this.writeLine("    symbolTable = null;");
				this.writeLine("  };");
				this.writeLine("}");
				
				//	close script
				this.writeLine("</script>");
			}
		};
	}
	
	private void sendContextMenuActions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get word selection
		String startWordId = request.getParameter("selStartWordId");
		String endWordId = request.getParameter("selEndWordId");
		
		//	get pending two-click action
		final TwoClickSelectionAction ptca = this.pendingTwoClickAction;
		this.pendingTwoClickAction = null;
		
		//	get pending two-click action (might be second click for two-click action)
		if ((startWordId != null) && (endWordId != null) && startWordId.equals(endWordId) && (ptca != null)) {
			final ImWord secondWord = this.idmp.document.getWord(startWordId);
			if (secondWord != null) {
				
				//	start action thread to execute two-click action
				ActionThread at = new ActionThread() {
					public void execute() throws Exception {
						ptca.performAction(secondWord);
					}
				};
				at.start();
				
				//	wait for action thread to finish or block
				String fbjsc = at.getFinalOrBlockingJavaScriptCall();
				String[] jscs = this.idmp.getJavaScriptCalls();
				
				//	send JavaScript calls up to this point
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
				BufferedLineWriter blw = new BufferedLineWriter(out);
				for (int c = 0; c < jscs.length; c++)
					blw.writeLine(jscs[c]);
				blw.writeLine(fbjsc);
				blw.flush();
				out.flush();
				blw.close();
				return;
			}
		}
		
		//	adjust display settings
		this.idmp.setTextStreamsPainted("true".equals(request.getAttribute("textStreamsPainted")));
		String tspStr = request.getParameter("ocrOverlayPercent");
		if (tspStr != null) try {
			this.idmp.setTextStringPercentage(Integer.parseInt(tspStr));
		} catch (NumberFormatException nfe) {}
		TreeSet paintedAnnotTypes = new TreeSet();
		String patStr = request.getParameter("paintedAnnotTypes");
		if (patStr != null)
			paintedAnnotTypes.addAll(Arrays.asList(patStr.split("\\;")));
		String[] allAnnotTypes = this.idmp.getAnnotationTypes();
		for (int t = 0; t < allAnnotTypes.length; t++)
			this.idmp.setAnnotationsPainted(allAnnotTypes[t], paintedAnnotTypes.contains(allAnnotTypes[t]));
		TreeSet paintedRegionTypes = new TreeSet();
		String prtStr = request.getParameter("paintedRegionTypes");
		if (prtStr != null)
			paintedRegionTypes.addAll(Arrays.asList(prtStr.split("\\;")));
		String[] allRegionTypes = this.idmp.getLayoutObjectTypes();
		for (int t = 0; t < allRegionTypes.length; t++)
			this.idmp.setRegionsPainted(allRegionTypes[t], paintedRegionTypes.contains(allRegionTypes[t]));
		
		//	get box selection
		String pageIdStr = request.getParameter("selPageId");
		String boundsStr = request.getParameter("selBounds");
		
		//	get actions from plugins
		SelectionAction[] actions = null;
		if ((startWordId != null) && (endWordId != null)) {
			ImWord start = this.idmp.document.getWord(startWordId);
			ImWord end = this.idmp.document.getWord(endWordId);
			if ((start != null) && (end != null)) {
				System.out.println("Getting word selection actions for '" + start.getLocalID() + "' to '" + end.getLocalID() + "'");
				System.out.println("  Painted annotation types are " + paintedAnnotTypes.toString());
				System.out.println("  Painted region types are " + paintedRegionTypes.toString());
				System.out.println("  Text streams are " + (idmp.areTextStreamsPainted() ? "" : " not") + " painted");
				actions = this.idmp.getActions(start, end);
			}
		}
		else if ((pageIdStr != null) && pageIdStr.matches("[0-9]+") && (boundsStr != null)) {
			ImPage page = this.idmp.document.getPage(Integer.parseInt(pageIdStr));
			BoundingBox bounds = BoundingBox.parse(boundsStr);
			if ((page != null) && (bounds != null)) {
				Point start = new Point(bounds.left, bounds.top);
				Point end = new Point(bounds.right, bounds.bottom);
				System.out.println("Getting box selection actions for '" + start.toString() + "' to '" + end.toString() + "' on page " + page.pageId);
				System.out.println("  Painted annotation types are " + paintedAnnotTypes.toString());
				System.out.println("  Painted region types are " + paintedRegionTypes.toString());
				System.out.println("  Text streams are " + (idmp.areTextStreamsPainted() ? "" : " not") + " painted");
				actions = this.idmp.getActions(page, start, end);
			}
		}
		
		//	index actions and send JavaScript call opening context menu from JSON argument
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		blw.writeLine("iShowContextMenu([");
		if (actions != null) {
			System.out.println("  Got " + actions.length + " actions:");
			for (int a = 0; a < actions.length; a++) {
				if (a != 0)
					blw.writeLine(",");
				this.sendContextMenuAction(actions[a], actions[a].getMenuItem(idmp), blw, "  ");
				System.out.println(" - " + actions[a].label);
			}
		}
		blw.writeLine("]);");
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private void sendContextMenuAction(SelectionAction action, JMenuItem mi, BufferedLineWriter blw, String indent) throws IOException {
		blw.writeLine(indent + "{");
		blw.writeLine(indent + "  \"label\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(mi.getText()) + "\",");
		String tooltip = mi.getToolTipText();
		if (tooltip != null)
			blw.writeLine(indent + "  \"tooltip\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(tooltip) + "\",");
		if (mi instanceof JMenu) {
			Component[] sMis = ((JMenu) mi).getMenuComponents();
			blw.writeLine(indent + "  \"items\": [");
			boolean isNonFirst = false;
			for (int c = 0; c < sMis.length; c++)
				if (sMis[c] instanceof JMenuItem) {
					if (isNonFirst)
						blw.writeLine(",");
					else isNonFirst = true;
					this.sendContextMenuAction(action, ((JMenuItem) sMis[c]), blw, (indent + "    "));
				}
				else if (sMis[c] instanceof JSeparator) {
					if (isNonFirst)
						blw.writeLine(",");
					else isNonFirst = true;
					blw.writeLine(indent + "  {");
					blw.writeLine(indent + "    \"label\": \"SEPARATOR\",");
					blw.writeLine(indent + "    \"id\": \"SEPARATOR\"");
					blw.writeLine(indent + "  }");
				}
			blw.writeLine("]");
		}
		else if (action instanceof TwoClickSelectionAction) {
			blw.writeLine(indent + "  \"twoClickLabel\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(((TwoClickSelectionAction) action).getActiveLabel()) + "\",");
			ImWord hw = ((TwoClickSelectionAction) action).getFirstWord();
			blw.writeLine(indent + "  \"twoClickHighlight\": {");
			blw.writeLine(indent + "    \"left\": " + hw.bounds.left + ",");
			blw.writeLine(indent + "    \"right\": " + hw.bounds.right + ",");
			blw.writeLine(indent + "    \"top\": " + hw.bounds.top + ",");
			blw.writeLine(indent + "    \"bottom\": " + hw.bounds.bottom + ",");
			blw.writeLine(indent + "    \"pageId\": " + hw.pageId + "");
			blw.writeLine(indent + "  },");
			blw.writeLine(indent + "  \"id\": " + action.hashCode() + "");
			this.actionMap.put(new Integer(action.hashCode()), action);
		}
		else if (action == SelectionAction.SEPARATOR)
			blw.writeLine(indent + "  \"id\": \"SEPARATOR\"");
		else {
			blw.writeLine(indent + "  \"id\": " + mi.hashCode() + "");
			if (mi.isOpaque() && (mi.getBackground() != null))
				blw.writeLine(indent + "  \"background\": \"" + FeedbackPanel.getRGB(mi.getBackground()) + "\"");
			this.actionMap.put(new Integer(mi.hashCode()), mi);
		}
		blw.writeLine(indent + "}");
	}
	
	private void handleContextMenuAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get selected action
		String actionId = request.getParameter("actionId");
		if (actionId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action ID");
			return;
		}
		if (!actionId.matches("\\-?[0-9]+")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid action ID '" + actionId + "'"));
			return;
		}
		
		//	get selected action, and clean up the rest
		final Object action = this.actionMap.get(new Integer(actionId));
		if (action == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid action ID '" + actionId + "'");
			return;
		}
		this.actionMap.clear();
		
		//	mark two-click action as pending
		if (action instanceof TwoClickSelectionAction) {
			this.pendingTwoClickAction = ((TwoClickSelectionAction) action);
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			blw.writeLine("uFinishUpdate();");
			blw.flush();
			out.flush();
			blw.close();
		}
		
		//	regular action
		else if (action instanceof JMenuItem) {
			
			//	start action thread to execute UNDO action
			ActionThread at = new ActionThread() {
				public void execute() throws Exception {
					ActionEvent ae = new ActionEvent(idmp, 0, "DOIT", System.currentTimeMillis(), 0);
					ActionListener[] als = ((JMenuItem) action).getActionListeners();
					for (int l = 0; l < als.length; l++)
						als[l].actionPerformed(ae);
				}
			};
			at.start();
			
			//	wait for action thread to finish or block
			String fbjsc = at.getFinalOrBlockingJavaScriptCall();
			String[] jscs = this.idmp.getJavaScriptCalls();
			
			//	send JavaScript calls up to this point
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			for (int c = 0; c < jscs.length; c++)
				blw.writeLine(jscs[c]);
			blw.writeLine(fbjsc);
			blw.flush();
			out.flush();
			blw.close();
			return;
		}
		
		//	strange action ...
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid action ID '" + actionId + "'");
			return;
		}
	}
	
	private void handleMainMenuAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get undo action ID
		String actionId = request.getParameter("actionId");
		if (actionId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action ID");
			return;
		}
		
		//	save request
		if ("FL-save".equals(actionId)) {
			
			//	read color settings
			this.readColorSettings(request);
			
			//	start action thread to close document (so we can get confirmation on unsaved changes)
			ActionThread at = new ActionThread("Saving Document", false) {
				public void execute() throws Exception {
					if (idmp.isDirty()) try {
						String[] storageLog = parent.storeDocument(idmp.document, userName, this.mmActionPmh);
						idmp.setNotDirty();
						mainMenuActionResult = new StorageLogActionResult(storageLog, false);
					}
					catch (IOException ioe) {
						ioe.printStackTrace(System.out);
						mainMenuActionResult = new JavaScriptActionResult("showAlertDialog(" +
								"'" + GoldenGateImagineWebUtils.escapeForJavaScript("Error saving document '" + idmp.document.getAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, idmp.document.docId) + "':\r\n" + ioe.getMessage()) + "', " +
								"'Error Saving Document', " +
								"" + JOptionPane.ERROR_MESSAGE + "" +
								");");
						return;
					}
				}
			};
			at.start();
			
			//	send JavaScript opening progress monitor
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			blw.writeLine("openProgressMonitor();");
			blw.flush();
			out.flush();
			blw.close();
//			
//			//	save document
//			IOException saveException = null;
//			try {
//				if (this.idmp.isDirty())
//					this.parent.storeDocument(this.idmp.document, this.userName, ProgressMonitor.dummy);
//				this.idmp.setNotDirty();
//			}
//			catch (IOException ioe) {
//				ioe.printStackTrace(System.out);
//				saveException = ioe;
//			}
//			
//			//	send JavaScript call informing user of success or error
//			response.setContentType("text/plain");
//			response.setCharacterEncoding("UTF-8");
//			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
//			if (saveException == null)
//				bw.write("showAlertDialog('" + GoldenGateImagineWebUtils.escapeForJavaScript("Document '" + idmp.document.getAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, idmp.document.docId) + "' saved sucessfully.") + "', 'Document Saved Successfully', " + JOptionPane.PLAIN_MESSAGE + ");");
//			else bw.write("showAlertDialog('" + GoldenGateImagineWebUtils.escapeForJavaScript("Error saving document '" + idmp.document.getAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, idmp.document.docId) + "':\r\n" + saveException.getMessage()) + "', 'Error Saving Document', " + JOptionPane.ERROR_MESSAGE + ");");
//			bw.flush();
//			bw.close();
			return;
		}
		
		//	close request
		if ("FL-close".equals(actionId)) {
			
			//	read color settings
			this.readColorSettings(request);
			
			//	start action thread to close document (so we can get confirmation on unsaved changes)
			ActionThread at = new ActionThread("Saving Document", false) {
				public void execute() throws Exception {
					String[] storageLog = null;
					if (idmp.isDirty()) {
						int choice = this.confirm("The document has been modified. Save changes befor closing?", "Save Before Closing?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
						if (choice == JOptionPane.YES_OPTION) {
							try {
								storageLog = parent.storeDocument(idmp.document, userName, this.mmActionPmh);
								idmp.setNotDirty();
							}
							catch (IOException ioe) {
								ioe.printStackTrace(System.out);
								mainMenuActionResult = new JavaScriptActionResult("showAlertDialog(" +
										"'" + GoldenGateImagineWebUtils.escapeForJavaScript("Error saving document '" + idmp.document.getAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, idmp.document.docId) + "':\r\n" + ioe.getMessage()) + "', " +
										"'Error Saving Document', " +
										"" + JOptionPane.ERROR_MESSAGE + "" +
										");");
								return;
							}
						}
						else if (choice != JOptionPane.NO_OPTION)
							return;
					}
					parent.docEditorClosing(GoldenGateImagineServletEditor.this);
					idmp.close();
					mainMenuActionResult = new StorageLogActionResult(storageLog, true);
				}
			};
			at.start();
			
			//	send JavaScript opening progress monitor
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			blw.writeLine("openProgressMonitor();");
			blw.flush();
			out.flush();
			blw.close();
//			
//			//	start action thread to close document (so we can get confirmation on unsaved changes)
//			ActionThread at = new ActionThread() {
//				public void execute() throws Exception {
//					if (idmp.isDirty()) {
//						int choice = this.confirm("The document has been modified. Save changes befor closing?", "Save Before Closing?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
//						if (choice == JOptionPane.YES_OPTION) {
//							try {
//								parent.storeDocument(idmp.document, userName, ProgressMonitor.dummy);
//							}
//							catch (IOException ioe) {
//								ioe.printStackTrace(System.out);
//								this.alert(("Error saving document '" + idmp.document.getAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, idmp.document.docId) + "':\r\n" + ioe.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE, null);
//								return;
//							}
//							idmp.setNotDirty();
//						}
//						else if (choice != JOptionPane.NO_OPTION)
//							return;
//					}
//					parent.docEditorClosing(GoldenGateImagineServletEditor.this);
//					idmp.close();
//					idmp.addJavaScriptCall("closeEditor();");
//				}
//			};
//			at.start();
//			
//			//	wait for action thread to finish or block
//			String fbjsc = at.getFinalOrBlockingJavaScriptCall();
//			String[] jscs = this.idmp.getJavaScriptCalls();
//			
//			//	send JavaScript calls up to this point
//			response.setContentType("text/plain");
//			response.setCharacterEncoding("UTF-8");
//			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
//			BufferedLineWriter blw = new BufferedLineWriter(out);
//			for (int c = 0; c < jscs.length; c++)
//				blw.writeLine(jscs[c]);
//			blw.writeLine(fbjsc);
//			blw.flush();
//			out.flush();
//			blw.close();
			return;
		}
		
		//	get image markup tool
		final ImageMarkupTool actionImTool = this.parent.getMainMenuActionImTool(actionId);
		
		//	handle viewer actions
		if (actionImTool instanceof WebDocumentViewer) {
			String viewBaseUrl = (request.getContextPath() + request.getServletPath() + "/" + this.id + "/view");
			this.docView = ((WebDocumentViewer) actionImTool).getWebDocumentView(viewBaseUrl + "/");
			
			//	start action thread to process document
			this.docViewActionThread = new ActionThread() {
				public void execute() throws Exception {
					idmp.applyMarkupTool(docView, null, ProgressMonitor.dummy);
				}
			};
			this.docViewActionThread.start();
			
			//	send JavaScript call opening dialog
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
//			blw.writeLine("window.open('" + viewBaseUrl + "', 'documentView', 'width=50,height=50,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes');");
			blw.writeLine("window.open('" + viewBaseUrl + "', 'documentView', 'left=center,top=center,title=no,resizable=yes');");
			blw.flush();
			out.flush();
			blw.close();
			return;
		}
		
		//	run plugin based 'Edit' menu actions in plain ActionThread
		if (actionId.startsWith("EDT-")) {
			
			//	start action thread to process document
			ActionThread at = new ActionThread() {
				public void execute() throws Exception {
					if (actionImTool != null)
						idmp.applyMarkupTool(actionImTool, null, ProgressMonitor.dummy);
					}
			};
			at.start();
			
			//	wait for action thread to finish or block
			String fbjsc = at.getFinalOrBlockingJavaScriptCall();
			String[] jscs = this.idmp.getJavaScriptCalls();
			
			//	send JavaScript calls up to this point
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			for (int c = 0; c < jscs.length; c++)
				blw.writeLine(jscs[c]);
			blw.writeLine(fbjsc);
			blw.flush();
			out.flush();
			blw.close();
			return;
		}
//		
//		//	run plugin based 'Tools' menu action in parent asynchronous request handler (feedback requests !!!)
//		this.parent.runToolsMenuActionImTool(request, response, this, actionId);
		
		//	run all other plugin based menu actions in progress monitored ActionThread
		else {
			
			//	start action thread to process document
			ActionThread at = new ActionThread("'" + actionImTool.getLabel() + "' Running", false) {
				public void execute() throws Exception {
					if (actionImTool != null)
						idmp.applyMarkupTool(actionImTool, null, this.mmActionPmh);
					}
			};
			at.start();
			
			//	send JavaScript call opening progress monitor
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			bw.write("openProgressMonitor();");
			bw.flush();
			bw.close();
		}
	}
	
	private void readColorSettings(HttpServletRequest request) {
		for (Enumeration pne = request.getParameterNames(); pne.hasMoreElements();) {
			String pn = ((String) pne.nextElement());
			String pv = request.getParameter(pn);
			if (pv.startsWith("#"))
				pv = pv.substring("#".length());
			if (pn.startsWith("ac.")) {
				Color ac = GoldenGateImagine.getColor(pv);
				if (ac != null)
					this.idmp.setAnnotationColor(pn.substring("ac.".length()), ac);
			}
			else if (pn.startsWith("rc.")) {
				Color rc = GoldenGateImagine.getColor(pv);
				if (rc != null)
					this.idmp.setLayoutObjectColor(pn.substring("rc.".length()), rc);
			}
			else if (pn.startsWith("tsc.")) {
				Color tsc = GoldenGateImagine.getColor(pv);
				if (tsc != null)
					this.idmp.setTextStreamTypeColor(pn.substring("tsc.".length()), tsc);
			}
		}
	}
	
	private void sendMainMenuActionStatusPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		//	atomically get running action thread (might finish while we're in here)
		final ActionThread at = this.mainMenuActionThread;
		
		//	no action thread running (might have terminated)
		if (at == null) {
			
			//	get any pending JavaScript calls, plus update finisher
			String[] jscs = this.idmp.getJavaScriptCalls();
			String[] aJscs = new String[jscs.length + 1];
			for (int c = 0; c < jscs.length; c++)
				aJscs[c] = ("window.opener." + jscs[c]);
			aJscs[jscs.length] = ("window.opener.uFinishUpdate();");
			
			//	send pop-in closer page
			this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, aJscs));
		}
		
		//	if action thread running, get its status page
		else this.parent.sendHtmlPage("mmActionStatus.html", new HtmlPageBuilder(this, request, response) {
				protected boolean includeJavaScriptDomHelpers() {
					return true;
				}
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type)) {
						
						//	use this for style development !!!
						if (this.host.findFile("WebDocumentView.dev.html") != null)
							this.includeFile("WebDocumentView.dev.html");
						
						//	write progress monitoring HTML code
						at.mmActionPmh.writeHtml(this.asWriter(), (at.mmActionLabel + ", Please Wait ..."));
					}
					else super.include(type, tag);
				}
				protected void writePageHeadExtensions() throws IOException {
					at.mmActionPmh.writeJavaScripts(this.asWriter(), (this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/mmActionStatus.js"));
					this.writeLine("<script type=\"text/javascript\">");
					this.writeLine("function closeProgressMonitor() {");
					this.writeLine("  window.setTimeout('doCloseProgressMonitor()', 100);");
					this.writeLine("}");
					
					//	call to result or close (not a JavaScript !!!)
					this.writeLine("function doCloseProgressMonitor() {");
					this.writeLine("  window.location.href = '" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/mmActionResult';");
					this.writeLine("}");
					this.writeLine("</script>");
				}
			});
	}
	
	private void sendMainMenuActionStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	atomically get running action thread (might finish while we're in here)
		final ActionThread at = this.mainMenuActionThread;
		
		//	prepare response
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		
		//	no action thread running (might have terminated)
		if (at == null) {
			
			//	get any pending JavaScript calls
			String[] jscs = this.idmp.getJavaScriptCalls();
			
			//	send JavaScript calls, including update finisher, and close progress monitor
			for (int c = 0; c < jscs.length; c++)
				blw.writeLine("window.opener." + jscs[c]);
			blw.writeLine("window.opener.uFinishUpdate();");
			blw.writeLine("closeProgressMonitor();");
		}
		
		//	if action thread running, get its status page
		else {
			
			//	send HTML progress monitor status page
			String[] jscs = at.mmActionPmh.getStatusUpdateCalls();
			for (int c = 0; c < jscs.length; c++)
				blw.writeLine(jscs[c]);
			
			//	add call for feedback, word edit, or generic confirm
			if (at.hasBlockingJavaScriptCall())
				blw.writeLine(at.getFinalOrBlockingJavaScriptCall());
		}
		
		//	finish sending response
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private void sendMainMenuActionResultPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	prepare response
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		//	if we don't have a result, simply close progress monitor
		if (this.mainMenuActionResult == null)
			this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, null));
		
		//	if we have a result, use it and void it afterward
		else {
			this.parent.sendHtmlPage("mmActionResult.html", this.mainMenuActionResult.getResultPageBuilder(request, response));
			this.mainMenuActionResult = null;
		}
	}
	
//	private void sendMainMenuActionResultPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		response.setContentType("text/html");
//		response.setCharacterEncoding("UTF-8");
//		String[] javaScriptCalls = {"window.opener.doFinishMainMenuAction();"};
//		this.parent.sendHtmlPage("popup.close.html", this.parent.getClosePopinWindowPageBuilder(request, response, javaScriptCalls));
//	}
//	
//	private void sendMainMenuActionResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		
//		//	get JavaScript calls (no need for waiting like with ActionThread, as we only get here once IMT has finished)
//		String[] jscs = this.idmp.getJavaScriptCalls();
//		
//		//	send JavaScript calls
//		response.setContentType("text/plain");
//		response.setCharacterEncoding("UTF-8");
//		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
//		BufferedLineWriter blw = new BufferedLineWriter(out);
//		for (int c = 0; c < jscs.length; c++)
//			blw.writeLine(jscs[c]);
//		blw.writeLine("uFinishUpdate();");
//		blw.flush();
//		out.flush();
//		blw.close();
//	}
//	
	private void handleExportAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get exporter
		String exportId = request.getParameter("exportId");
		if (exportId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid export ID");
			return;
		}
		
		//	export XML
		if ("xml".equals(exportId) || "rawXml".equals(exportId)) {
			
			//	set headers
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-Disposition", ("attachment; filename=" + this.idmp.document.getAttribute(DOCUMENT_NAME_ATTRIBUTE, this.idmp.document.docId) + ("rawXml".equals(exportId) ? ".raw" : "") + ".xml"));
			
			//	export document
			//	TODO set annotation nesting order
			BufferedWriter xmlOut = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			ImDocumentRoot xmlDoc = new ImDocumentRoot(this.idmp.document, ("rawXml".equals(exportId) ? (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS) : ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS));
			AnnotationUtils.writeXML(xmlDoc, xmlOut, false);
			xmlOut.flush();
			xmlOut.close();
			return;
		}
		
		//	export IMF (it's an export, even if resident in 'File' menu)
		if ("imf".equals(exportId)) {
			
			//	set headers
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", ("attachment; filename=" + this.idmp.document.getAttribute(DOCUMENT_NAME_ATTRIBUTE, this.idmp.document.docId) + ".imf"));
			
			//	export document (making sure not to flush response stream prematurely)
			OutputStream out = response.getOutputStream();
			ImDocumentIO.storeDocument(this.idmp.document, new BufferedOutputStream(new IsolatorOutputStream(out)), ProgressMonitor.dummy);
			out.flush();
			return;
		}
		
		//	get exporter
		final ImageDocumentFileExporter exporter = this.parent.getExporter(exportId);
		if (exporter == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid export ID '" + exportId + "'"));
			return;
		}
		
		//	export document to temporary file
		File expFile = new File(this.parent.getExportCacheFolder(), (this.idmp.document.docId + "." + exportId));
		try {
			expFile.getParentFile().mkdirs();
			expFile = exporter.exportDocument(this.idmp.document, expFile, ProgressMonitor.dummy);
		}
		catch (final IOException ioe) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			
			//	create error page builder (we're OK with parent as page builder host, we _build_ an alert(), and won't confirm() inside it)
			this.parent.sendHtmlPage("exportError.html", new HtmlPageBuilder(this.parent, request, response) {
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type))
						this.includeBody();
					else super.include(type, tag);
				}
				private void includeBody() throws IOException {
					this.writeLine("<div class=\"alertTitle\" style=\"align: center;\">Error Exporting Document</div>");
					this.writeLine("<table><tr>");
					this.writeLine("<td><div class=\"alertMessage\">An error occurred while exporting the document via '" + exporter.getPluginName() + "':<br/>" + html.escape(ioe.getMessage()) + "</div></td>");
					this.writeLine("<td><img class=\"alertIcon\" src=\"" + this.request.getContextPath() + parent.getStaticResourcePath() + "/error.png\"/></td>");
					this.writeLine("</tr></table>");
					this.writeLine("<button class=\"alertButton\" onclick=\"window.close();\">OK</button>");
				}
			});
			return;
		}
		
		//	did the export succeed?
		if (expFile == null) {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			
			//	create error page builder (we're OK with parent as page builder host, we _build_ an alert(), and won't confirm() inside it)
			this.parent.sendHtmlPage("exportError.html", new HtmlPageBuilder(this.parent, request, response) {
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type))
						this.includeBody();
					else super.include(type, tag);
				}
				private void includeBody() throws IOException {
					this.writeLine("<div class=\"alertTitle\" style=\"align: center;\">Document Could Not Be Exported</div>");
					this.writeLine("<table><tr>");
					this.writeLine("<td><div class=\"alertMessage\">The document could not be exported via '" + exporter.getPluginName() + "'</div></td>");
					this.writeLine("<td><img class=\"alertIcon\" src=\"" + this.request.getContextPath() + parent.getStaticResourcePath() + "/error.png\"/></td>");
					this.writeLine("</tr></table>");
					this.writeLine("<button class=\"alertButton\" onclick=\"window.close();\">OK</button>");
				}
			});
			return;
		}
		
		//	try and determine MIME type from file extension
		String expContentType;
		if (expFile.getName().toLowerCase().endsWith(".zip"))
			expContentType = "application/zip";
		else if (expFile.getName().toLowerCase().endsWith(".xml"))
			expContentType = "text/xml";
		else if (expFile.getName().toLowerCase().endsWith(".htm") || expFile.getName().toLowerCase().endsWith(".html"))
			expContentType = "text/html";
		else expContentType = "application/octet-stream";
		
		//	adjust file name (document ID ...)
		String expFileName = expFile.getName();
		if (expFileName.startsWith(this.idmp.document.docId))
			expFileName = expFileName.substring(this.idmp.document.docId.length());
		if (expFileName.startsWith("." + exportId))
			expFileName = expFileName.substring(".".length() + exportId.length());
		expFileName = (this.idmp.document.getAttribute(DOCUMENT_NAME_ATTRIBUTE, this.idmp.document.docId) + expFileName);
		
		//	set headers
		response.setContentType(expContentType);
		if (expContentType.startsWith("text/"))
			response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-Disposition", ("attachment; filename=" + expFileName));
		
		//	send file contents
		InputStream expIn = new BufferedInputStream(new FileInputStream(expFile));
		OutputStream expOut = new BufferedOutputStream(response.getOutputStream());
		byte[] expBuf = new byte[1024];
		for (int r; (r = expIn.read(expBuf, 0, expBuf.length)) != -1;)
			expOut.write(expBuf, 0, r);
		expOut.flush();
		expIn.close();
		
		//	clean up
		expFile.delete();
	}
	
	private void handleUndoAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get undo action ID
		String actionIdStr = request.getParameter("actionId");
		if (actionIdStr == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action ID");
			return;
		}
		if (actionIdStr.startsWith("UNDO-"))
			actionIdStr = actionIdStr.substring("UNDO-".length());
		if (!actionIdStr.matches("\\-?[0-9]+")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid action ID '" + actionIdStr + "'"));
			return;
		}
		final int actionId = Integer.parseInt(actionIdStr);
		
		//	start action thread to execute UNDO action
		ActionThread at = new ActionThread() {
			public void execute() throws Exception {
				idmp.undo(actionId);
			}
		};
		at.start();
		
		//	wait for action thread to finish or block
		String fbjsc = at.getFinalOrBlockingJavaScriptCall();
		String[] jscs = this.idmp.getJavaScriptCalls();
		
		//	send JavaScript calls up to this point
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		for (int c = 0; c < jscs.length; c++)
			blw.writeLine(jscs[c]);
		blw.writeLine(fbjsc);
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private void handleDropAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get word ID and dropped data
		String dropWordId = request.getParameter("wordId");
		if (dropWordId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid word ID");
			return;
		}
		final ImWord dropWord = this.idmp.document.getWord(dropWordId);
		if (dropWord == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid word ID");
			return;
		}
		final String droppedData = request.getParameter("dropped");
		if (droppedData == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid drop payload");
			return;
		}
		
		//	adjust display settings
		this.idmp.setTextStreamsPainted("true".equals(request.getAttribute("textStreamsPainted")));
		String tspStr = request.getParameter("ocrOverlayPercent");
		if (tspStr != null) try {
			this.idmp.setTextStringPercentage(Integer.parseInt(tspStr));
		} catch (NumberFormatException nfe) {}
		TreeSet paintedAnnotTypes = new TreeSet();
		String patStr = request.getParameter("paintedAnnotTypes");
		if (patStr != null)
			paintedAnnotTypes.addAll(Arrays.asList(patStr.split("\\;")));
		String[] allAnnotTypes = this.idmp.getAnnotationTypes();
		for (int t = 0; t < allAnnotTypes.length; t++)
			this.idmp.setAnnotationsPainted(allAnnotTypes[t], paintedAnnotTypes.contains(allAnnotTypes[t]));
		TreeSet paintedRegionTypes = new TreeSet();
		String prtStr = request.getParameter("paintedRegionTypes");
		if (prtStr != null)
			paintedRegionTypes.addAll(Arrays.asList(prtStr.split("\\;")));
		String[] allRegionTypes = this.idmp.getLayoutObjectTypes();
		for (int t = 0; t < allRegionTypes.length; t++)
			this.idmp.setRegionsPainted(allRegionTypes[t], paintedRegionTypes.contains(allRegionTypes[t]));
		
		//	start action thread to execute drop (we might have a confirm prompt ...)
		System.out.println("Handling drop action on '" + dropWord.getLocalID() + "', data is '" + droppedData + "'");
		System.out.println("  Painted annotation types are " + paintedAnnotTypes.toString());
		System.out.println("  Painted region types are " + paintedRegionTypes.toString());
		System.out.println("  Text streams are " + (idmp.areTextStreamsPainted() ? "" : " not") + " painted");
		ActionThread at = new ActionThread() {
			public void execute() throws Exception {
				Transferable transfer = new PlainTextDropTransfer(droppedData);
				idmp.handleDrop(dropWord, transfer);
			}
		};
		at.start();
		
		//	wait for action thread to finish or block
		String fbjsc = at.getFinalOrBlockingJavaScriptCall();
		String[] jscs = this.idmp.getJavaScriptCalls();
		
		//	send JavaScript calls up to this point
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		for (int c = 0; c < jscs.length; c++)
			blw.writeLine(jscs[c]);
		blw.writeLine(fbjsc);
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private static class PlainTextDropTransfer implements Transferable {
		private static DataFlavor plainTextDataFlavor = null;
		static {
			try {
				plainTextDataFlavor = new DataFlavor("text/plain;class=" + String.class.getName());
			} catch (Exception e) {}
		}
		private String data;
		PlainTextDropTransfer(String data) {
			this.data = data;
		}
		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] dfs = {plainTextDataFlavor};
			return dfs;
		}
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor == plainTextDataFlavor);
		}
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (flavor == plainTextDataFlavor)
				return this.data;
			else throw new UnsupportedFlavorException(flavor);
		}
	}
	
	private void sendDisplaySettings(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	send JavaScript calls initializing display colors
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		String[] annotTypes = this.idmp.getAnnotationTypes();
		for (int t = 0; t < annotTypes.length; t++) {
			Color ac = this.idmp.getAnnotationColor(annotTypes[t]);
			if (ac != null)
				blw.writeLine("uSetAnnotColor('" + annotTypes[t] + "', '#" + GoldenGateImagine.getHex(ac) + "');");
		}
		String[] layoutObjectTypes = this.idmp.getLayoutObjectTypes();
		for (int t = 0; t < layoutObjectTypes.length; t++) {
			Color loc = this.idmp.getLayoutObjectColor(layoutObjectTypes[t]);
			if (loc != null)
				blw.writeLine("uSetRegionColor('" + layoutObjectTypes[t] + "', '#" + GoldenGateImagine.getHex(loc) + "');");
		}
		String[] textStreamTypes = this.idmp.getTextStreamTypes();
		for (int t = 0; t < textStreamTypes.length; t++) {
			Color tsc = this.idmp.getTextStreamTypeColor(textStreamTypes[t]);
			if (tsc != null)
				blw.writeLine("uSetTextStreamColor('" + textStreamTypes[t] + "', '#" + GoldenGateImagine.getHex(tsc) + "');");
		}
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private void sendPageImage(String pageIdStr, HttpServletResponse response) throws IOException {
		
		//	get page ID and image
		pageIdStr = pageIdStr.substring(0, pageIdStr.indexOf('.'));
		if (!pageIdStr.matches("\\-?[0-9]+")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid page ID '" + pageIdStr + "'"));
			return;
		}
		int pageId = Integer.parseInt(pageIdStr);
		ImPage page = this.idmp.document.getPage(pageId);
		if (page == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid page ID '" + this.idmp.document.docId + "/" + pageId + "'"));
			return;
		}
		
		//	send page image
		PageImageInputStream piis = this.idmp.document.getPageImageAsStream(pageId);
		response.setContentType("image/png");
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		byte[] buffer = new byte[1024];
		for (int r; (r = piis.read(buffer, 0, buffer.length)) != -1;)
			out.write(buffer, 0, r);
		out.flush();
		piis.close();
	}
	
	private void sendWordImage(String wordId, HttpServletResponse response) throws IOException {
		
		//	get word
		wordId = wordId.substring(0, wordId.lastIndexOf('.'));
		ImWord word = this.idmp.document.getWord(wordId);
		if (word == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid word ID '" + this.idmp.document.docId + "/" + wordId + "'"));
			return;
		}
		
		//	send word image
		PageImage pi = word.getImage();
		response.setContentType("image/png");
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		pi.writeImage(out);
		out.flush();
	}
	
	private void sendDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	discard all pending JavaScript calls, we're sending the current document status
		this.idmp.getJavaScriptCalls();
		
		//	send JavaScript call loading document from JSON argument
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		blw.writeLine("setDocument({");
		blw.writeLine("  \"id\": \"" + this.idmp.document.docId + "\",");
		if (!this.idmp.documentBornDigital)
			blw.writeLine("  \"isOcr\": true,");
		blw.writeLine("  \"pages\": [");
		ImPage[] pages = this.idmp.document.getPages();
		boolean isFirstPage = true;
		for (int p = 0; p < pages.length; p++) {
			if (!this.idmp.isPageVisible(pages[p].pageId))
				continue;
			if (isFirstPage)
				isFirstPage = false;
			else blw.writeLine(",");
			blw.writeLine("  {");
			blw.writeLine("    \"id\": " + pages[p].pageId + ",");
			blw.writeLine("    \"imageDpi\": " + pages[p].getImageDPI() + ",");
			blw.writeLine("    \"imagePath\": \"" + request.getContextPath() + request.getServletPath() + "/" + this.id + "/pageImage/" + pages[p].pageId + ".png\",");
			blw.writeLine("    \"bounds\": {");
			blw.writeLine("      \"left\": " + pages[p].bounds.left + ",");
			blw.writeLine("      \"right\": " + pages[p].bounds.right + ",");
			blw.writeLine("      \"top\": " + pages[p].bounds.top + ",");
			blw.writeLine("      \"bottom\": " + pages[p].bounds.bottom + "");
			blw.writeLine("    },");
			blw.writeLine("    \"words\": [");
			ImWord[] words = pages[p].getWords();
			Arrays.sort(words, ImUtils.textStreamOrder);
			for (int w = 0; w < words.length; w++) {
				if (w != 0)
					blw.writeLine(",");
				blw.writeLine("    {");
				blw.writeLine("      \"bounds\": {");
				blw.writeLine("        \"left\": " + words[w].bounds.left + ",");
				blw.writeLine("        \"right\": " + words[w].bounds.right + ",");
				blw.writeLine("        \"top\": " + words[w].bounds.top + ",");
				blw.writeLine("        \"bottom\": " + words[w].bounds.bottom + "");
				blw.writeLine("      },");
				if (words[w].getPreviousWord() == null)
					blw.writeLine("      \"textStreamType\": \"" + words[w].getTextStreamType() + "\",");
				else blw.writeLine("      \"prevWordId\": \"" + words[w].getPreviousWord().getLocalID() + "\",");
				if (words[w].getNextWord() != null)
					blw.writeLine("      \"nextRelation\": \"" + words[w].getNextRelation() + "\",");
				if (!this.idmp.documentBornDigital) /* we need this for the OCR overlay */ {
					if (words[w].hasAttribute(ImWord.BOLD_ATTRIBUTE))
						blw.writeLine("      \"bold\": true,");
					if (words[w].hasAttribute(ImWord.ITALICS_ATTRIBUTE))
						blw.writeLine("      \"italics\": true,");
				}
				blw.writeLine("      \"str\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(words[w].getString(), '"') + "\"");
				blw.writeLine("    }");
			}
			blw.writeLine("    ],");
			blw.writeLine("    \"regions\": [");
			ImRegion[] regions = pages[p].getRegions();
			for (int r = 0; r < regions.length; r++) {
				if (r != 0)
					blw.writeLine(",");
				blw.writeLine("    {");
				blw.writeLine("      \"type\": \"" + regions[r].getType() + "\",");
				blw.writeLine("      \"bounds\": {");
				blw.writeLine("        \"left\": " + regions[r].bounds.left + ",");
				blw.writeLine("        \"right\": " + regions[r].bounds.right + ",");
				blw.writeLine("        \"top\": " + regions[r].bounds.top + ",");
				blw.writeLine("        \"bottom\": " + regions[r].bounds.bottom + "");
				blw.writeLine("      }");
				blw.writeLine("    }");
			}
			blw.writeLine("    ]");
			blw.writeLine("  }");
		}
		blw.writeLine("  ],");
		blw.writeLine("  \"annotations\": [");
		ImAnnotation[] annots = this.idmp.document.getAnnotations();
		Arrays.sort(annots, jsonAnnotOrder);
		for (int a = 0; a < annots.length; a++) {
			if (a != 0)
				blw.writeLine(",");
			blw.writeLine("    {");
			blw.writeLine("      \"type\": \"" + annots[a].getType() + "\",");
			blw.writeLine("      \"firstWordId\": \"" + annots[a].getFirstWord().getLocalID() + "\",");
			blw.writeLine("      \"lastWordId\": \"" + annots[a].getLastWord().getLocalID() + "\"");
			blw.writeLine("    }");
		}
		blw.writeLine("  ]");
		blw.writeLine("});");
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private static final Comparator jsonAnnotOrder = new Comparator() {
		public int compare(Object obj1, Object obj2) {
			ImAnnotation annot1 = ((ImAnnotation) obj1);
			ImAnnotation annot2 = ((ImAnnotation) obj2);
			ImWord fWord1 = annot1.getFirstWord();
			ImWord fWord2 = annot2.getFirstWord();
			if (fWord1.pageId != fWord2.pageId)
				return (fWord1.pageId - fWord2.pageId);
			ImWord lWord1 = annot1.getLastWord();
			ImWord lWord2 = annot2.getLastWord();
			if (lWord1.pageId != lWord2.pageId)
				return (lWord2.pageId - lWord1.pageId);
			int c = ImUtils.textStreamOrder.compare(fWord1, fWord2);
			if (c != 0)
				return c;
			c = ImUtils.textStreamOrder.compare(lWord2, lWord1);
			if (c != 0)
				return c;
			return annot1.getType().compareTo(annot2.getType());
		}
	};
	
	private static class MmActionProgressMonitorHTML extends ProgressMonitorHTML {
		private ArrayList enqueuedJavaScriptCalls = new ArrayList(1);
		MmActionProgressMonitorHTML(boolean supportPauseResume, boolean supportAbort) {
			super(supportPauseResume, supportAbort);
		}
		void enqueueJavaScriptCall(String jsc) {
			this.enqueuedJavaScriptCalls.add(jsc);
		}
		public String[] getStatusUpdateCalls() {
			String[] sucs = super.getStatusUpdateCalls();
			if (this.enqueuedJavaScriptCalls.isEmpty())
				return sucs;
			String[] aSucs = new String[sucs.length + this.enqueuedJavaScriptCalls.size()];
			System.arraycopy(sucs, 0, aSucs, 0, sucs.length);
			for (int c = 0; c < this.enqueuedJavaScriptCalls.size(); c++)
				aSucs[sucs.length + c] = ((String) this.enqueuedJavaScriptCalls.get(c));
			this.enqueuedJavaScriptCalls.clear();
			return aSucs;
		}
	}
	
	private static abstract class MmActionResult {
		abstract HtmlPageBuilder getResultPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException;
	}
	
	private class StorageLogActionResult extends MmActionResult {
		private String[] storageLog;
		private boolean close;
		StorageLogActionResult(String[] storageLog, boolean close) {
			this.storageLog = storageLog;
			this.close = close;
		}
		HtmlPageBuilder getResultPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException {
			
			//	nothing to display, simply close action result or main editor
			if (this.storageLog == null)
				return new HtmlPageBuilder(GoldenGateImagineServletEditor.this, request, response) {
					protected void include(String type, String tag) throws IOException {
						if ("includeBody".equals(type)) {
							this.writeLine("<script type=\"text/javascript\">");
							if (close)
								this.writeLine("window.opener.closeEditor();");
							else this.writeLine("window.close();");
							this.writeLine("</script>");
						}
						else super.include(type, tag);
					}
				};
			//	TODO implement this
			return null;
		}
	}
	
	private class JavaScriptActionResult extends MmActionResult {
		private String[] jsCalls;
		JavaScriptActionResult(String jsCall) {
			this.jsCalls = new String[1];
			this.jsCalls[0] = jsCall;
		}
		JavaScriptActionResult(String[] jsCalls) {
			this.jsCalls = jsCalls;
		}
		HtmlPageBuilder getResultPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException {
			return new HtmlPageBuilder(GoldenGateImagineServletEditor.this, request, response) {
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type)) {
						this.writeLine("<script type=\"text/javascript\">");
						for (int c = 0; c < jsCalls.length; c++)
							this.writeLine(jsCalls[c]);
						this.writeLine("</script>");
					}
					else super.include(type, tag);
				}
			};
		}
	}
	
	abstract class ActionThread extends Thread {
		final String mmActionLabel;
		final MmActionProgressMonitorHTML mmActionPmh;
		final MultiActionServletRequest docViewRequest;
		final MultiActionServletResponse docViewResponse;
		final String docViewPathInfo;
		
		ActionThread() {
			super(id);
			this.mmActionLabel = null;
			this.mmActionPmh = null;
			this.docViewRequest = null;
			this.docViewResponse = null;
			this.docViewPathInfo = null;
		}
		ActionThread(String actionLabel, boolean actionAbortable) {
			super(id);
			this.mmActionLabel = actionLabel;
			this.mmActionPmh = new MmActionProgressMonitorHTML(false, actionAbortable);
			this.docViewRequest = null;
			this.docViewResponse = null;
			this.docViewPathInfo = null;
		}
		ActionThread(HttpServletRequest request, HttpServletResponse response, String docViewPathInfo) {
			super(id);
			this.mmActionLabel = null;
			this.mmActionPmh = null;
			this.docViewRequest = new MultiActionServletRequest(request);
			this.docViewResponse = new MultiActionServletResponse(response);
			this.docViewPathInfo = docViewPathInfo;
		}
		
		boolean isMainMenuAction() {
			return (this.mmActionLabel != null);
		}
		
		boolean isDocViewAction() {
			return (this.docViewPathInfo != null);
		}
		void setRequestData(HttpServletRequest request, HttpServletResponse response) {
			this.docViewRequest.setRequest(request);
			this.docViewResponse.setResponse(response);
		}
		
		public void run() {
			if (this.isMainMenuAction())
				mainMenuActionThread = this;
			try {
				this.execute();
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
				if (mainMenuActionThread == this)
					mainMenuActionResult = new JavaScriptActionResult("showAlertDialog(" +
							"'" + GoldenGateImagineWebUtils.escapeForJavaScript("The operation produced an error:\r\n" + e.getMessage()) + "', " +
							"'" + GoldenGateImagineWebUtils.escapeForJavaScript("Error " + this.mmActionLabel) + "', " +
							"" + JOptionPane.ERROR_MESSAGE + "" +
							");");
			}
			finally {
				/* If we're handling a request from a document view, we just
				 * have to make the waiting thread continue, not provide any
				 * actual action, as the script body is written by the document
				 * view itself. And we definitely don't call a document update
				 * on the document view page. */
				/* If we're handling a main menu action, we just have to make
				 * the waiting thread continue, not provide any actual action,
				 * as the closing call of the progress monitor will perform the
				 * pending document updates. */
				this.setFinalOrBlockingJavaScriptCall((this.isMainMenuAction() || this.isDocViewAction()) ? "" : "uFinishUpdate();");
				if (mainMenuActionThread == this)
					mainMenuActionThread = null;
			}
		}
		public abstract void execute() throws Exception;
		
		private String finalOrBlockingJavaScriptCall = null;
		synchronized void setFinalOrBlockingJavaScriptCall(String fbjsc) {
			this.finalOrBlockingJavaScriptCall = fbjsc;
			this.notify();
		}
		synchronized boolean hasBlockingJavaScriptCall() {
			/* there might be a slight delay between run() finishes and
			 * isAlive() returns to returning false, but this should be good
			 * enough for starters */
			return (this.isAlive() && (this.finalOrBlockingJavaScriptCall != null));
		}
		synchronized String getFinalOrBlockingJavaScriptCall() {
			if (this.finalOrBlockingJavaScriptCall == null) try {
				this.wait();
			} catch (InterruptedException ie) {}
			String fbjsc = this.finalOrBlockingJavaScriptCall;
			this.finalOrBlockingJavaScriptCall = null;
			return fbjsc;
		}
		
		void copy(Transferable data) {
			clipboardData = data;
			idmp.addJavaScriptCall("showCopiedContent();");
		}
		
		void alert(Object message, String title, int messageType, Icon icon) {
			String alertJsc = ("showAlertDialog('" + GoldenGateImagineWebUtils.escapeForJavaScript(message.toString()) + "', " + ((title == null) ? "null" : ("'" + GoldenGateImagineWebUtils.escapeForJavaScript(title) + "'")) + ", " + messageType + ");");
			if (this.isDocViewAction())
				this.docViewResponse.enqueueJavaScriptCall(alertJsc);
			else if (this.isMainMenuAction())
				this.mmActionPmh.enqueueJavaScriptCall(alertJsc);
			else idmp.addJavaScriptCall(alertJsc);
		}
		
		private Object confirmMessage = null;
		private Map confirmFieldMappings = null;
		private String confirmMessageHtml = null;
		private String confirmTitle = null;
		private int confirmOptionType = -1;
		private int confirmMessageType = -1;
		private int confirmResult = -1;
		int confirm(Object message, String title, int optionType, int messageType, Icon icon) {
			
			//	store message
			this.confirmMessage = message;
			this.confirmTitle = title;
			this.confirmOptionType = optionType;
			this.confirmMessageType = messageType;
			
			//	string message, this one's easy
			if (message instanceof CharSequence)
				this.confirmMessageHtml = ("<span class=\"confirmFormLabel\">" + html.escape(this.confirmMessage.toString()).replaceAll("[\\r\\n]+", "<br/>") + "</span>");
			
			//	Swing message, convert
			else if (message instanceof JComponent) {
				this.confirmFieldMappings = new LinkedHashMap();
				this.confirmMessageHtml = convertSwingMessage(((JComponent) message), this.confirmFieldMappings);
			}
			
			//	block until confirmed() gets the answer
			synchronized (this) {
				try {
					actionThreadAwaitingConfirm = this;
					this.setFinalOrBlockingJavaScriptCall("showConfirmDialog();");
					this.wait();
					actionThreadAwaitingConfirm = null;
				} catch (InterruptedException ie) {}
			}
			
			//	clear and return result
			this.confirmMessage = null;
			this.confirmMessageHtml = null;
			if (this.confirmFieldMappings != null)
				this.confirmFieldMappings.clear();
			this.confirmFieldMappings = null;
			this.confirmTitle = null;
			this.confirmOptionType = -1;
			this.confirmMessageType = -1;
			int cr = this.confirmResult;
			this.confirmResult = -1;
			return cr;
		}
		HtmlPageBuilder getConfirmPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException {
			
			//	create confirm form page builder (we're OK with parent as page builder host, won't need alert(), and we _build_ the confirm() dialog)
			return new HtmlPageBuilder(parent, request, response) {
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type))
						this.includeBody();
					else super.include(type, tag);
				}
				private void includeBody() throws IOException {
					
					//	open form (we have the URL parts only here ... but then, we won't need them anyway)
					this.writeLine("<div class=\"confirmForm\">");
					this.writeLine("<form id=\"confirmForm\" method=\"POST\" action=\"" + this.request.getContextPath() + this.request.getServletPath() + "/weWillSubmitViaJavaScript" + "\" onsubmit=\"return submitConfirmForm(" + JOptionPane.OK_OPTION + ");\">");
					
					//	write title
					if (confirmTitle != null)
						this.writeLine("<div class=\"confirmFormTitle\" style=\"align: center;\">" + html.escape(confirmTitle) + "</div>");
					
					//	get icon representing message type
					String iconName = null;
					if (confirmMessageType == JOptionPane.QUESTION_MESSAGE)
						iconName = "question.png";
					else if (confirmMessageType == JOptionPane.WARNING_MESSAGE)
						iconName = "warning.png";
					else if (confirmMessageType == JOptionPane.ERROR_MESSAGE)
						iconName = "error.png";
					else if (confirmMessageType == JOptionPane.INFORMATION_MESSAGE)
						iconName = "info.png";
					
					//	simply write HTML form representing confirm() message
					if (iconName == null)
						this.writeLine(confirmMessageHtml);
					
					//	wrap message and icon in table to keep icon to right of message
					else {
						this.writeLine("<table><tr>");
						this.writeLine("<td>");
						this.writeLine(confirmMessageHtml);
						this.writeLine("</td><td>");
						this.writeLine("<img class=\"confirmFormIcon\" src=\"" + this.request.getContextPath() + parent.getStaticResourcePath() + "/" + iconName + "\"/>");
						this.writeLine("</td>");
						this.writeLine("</tr></table>");
					}
					
					//	write submit buttons, depending on option type ...
					this.writeLine("<div class=\"confirmFormButtons\" style=\"align: center;\">");
					if (confirmOptionType == JOptionPane.YES_NO_OPTION) {
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.YES_OPTION + ");\">Yes</button>");
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.NO_OPTION + ");\">No</button>");
					}
					else if (confirmOptionType == JOptionPane.YES_NO_CANCEL_OPTION) {
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.YES_OPTION + ");\">Yes</button>");
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.NO_OPTION + ");\">No</button>");
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.CANCEL_OPTION + ");\">Cancel</button>");
					}
					else if (confirmOptionType == JOptionPane.OK_CANCEL_OPTION) {
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.OK_OPTION + ");\">OK</button>");
						this.writeLine("<button class=\"confirmFormButton\" onclick=\"return submitConfirmForm(" + JOptionPane.CANCEL_OPTION + ");\">Cancel</button>");
					}
					this.writeLine("</div>");
					
					//	... and make sure 'result' gets set to appropriate values for each button
					this.writeLine("<input id=\"confirmFormResult\" type=\"hidden\" name=\"result\" value=\"" + JOptionPane.CLOSED_OPTION + "\"/>");
					
					//	close form
					this.writeLine("</form>");
					this.writeLine("</div>");
				}
				protected boolean includeJavaScriptDomHelpers() {
					return true;
				}
				protected String getPageTitle(String title) {
					return ((confirmTitle == null) ? super.getPageTitle(title) : confirmTitle);
				}
				protected void writePageHeadExtensions() throws IOException {
					this.writeLine("<script type=\"text/javascript\">");
					
					this.writeLine("function submitConfirmForm(result) {");
					this.writeLine("  getById('confirmFormResult').value = result;");
					this.writeLine("  var query = '" + DOCUMENT_ID_ATTRIBUTE + "=" + id + "';");
					this.writeLine("  query += ('&result=' + result);");
					if (confirmFieldMappings != null)
						for (Iterator fnit = confirmFieldMappings.keySet().iterator(); fnit.hasNext();) {
							String fieldName = ((String) fnit.next());
							Object field = confirmFieldMappings.get(fieldName);
							if (field instanceof JRadioButton) {
								this.writeLine("  var " + fieldName + " = getById('" + fieldName + "');");
								this.writeLine("  if (" + fieldName + ".checked)");
								this.writeLine("    query += ('&' + " + fieldName + ".name + '=' + " + fieldName + ".value);");
							}
							else if (field instanceof JCheckBox) {
								this.writeLine("  var " + fieldName + " = getById('" + fieldName + "');");
								this.writeLine("  if (" + fieldName + ".checked)");
								this.writeLine("    query += ('&' + " + fieldName + ".name + '=true');");
							}
							else if ((field instanceof JTextField) || (field instanceof JComboBox)) {
								this.writeLine("  var " + fieldName + " = getById('" + fieldName + "');");
								this.writeLine("  query += ('&' + " + fieldName + ".name + '=' + encodeURIComponent(" + fieldName + ".value));");
							}
						}
					this.writeLine("  var cs = window.opener.document.getElementById('dynamicConfirmScript');");
					this.writeLine("  var csp;");
					this.writeLine("  if (cs == null)");
					this.writeLine("    csp = window.opener.document.getElementsByTagName('body')[0];");
					this.writeLine("  else {");
					this.writeLine("    var csp = cs.parentNode;");
					this.writeLine("    removeElement(cs);");
					this.writeLine("  }");
					this.writeLine("  var csSrc = ('" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/confirm.js?');");
					this.writeLine("  csSrc = (csSrc + query);");
					this.writeLine("  csSrc = (csSrc + '&time=' + (new Date()).getTime());");
					this.writeLine("  cs = newElement('script', 'dynamicConfirmScript');");
					this.writeLine("  cs.type = 'text/javascript';");
					this.writeLine("  cs.src = csSrc;");
					this.writeLine("  csp.appendChild(cs);");
					this.writeLine("  window.close();");
					this.writeLine("  return false;");
					this.writeLine("}");
					
					this.writeLine("</script>");
				}
			};
		}
		void confirmed(HttpServletRequest request) {
			System.out.println("Processing confirm response:");
			
			//	store return value
			this.confirmResult = Integer.parseInt(request.getParameter("result"));
			System.out.println(" - result is " + this.confirmResult);
			
			//	modify message field values based on request parameters (even if cancelled, as we don't know the semantics)
			if (this.confirmFieldMappings != null)
				for (Iterator fnit = this.confirmFieldMappings.keySet().iterator(); fnit.hasNext();) {
					String fieldName = ((String) fnit.next());
					System.out.println(" - handling field " + fieldName);
					String fieldValue = request.getParameter(fieldName);
					System.out.println("   - value is " + fieldValue);
					Object field = this.confirmFieldMappings.get(fieldName);
					if (field instanceof ButtonGroup) {
						System.out.println("   - field is radio button group");
						if (fieldValue == null) {
							System.out.println("     ==> cleared");
							((ButtonGroup) field).clearSelection();
						}
						else {
							Object selField = this.confirmFieldMappings.get(fieldValue);
							System.out.print("     ==> selecting " + fieldValue + " ... ");
							if (selField instanceof JRadioButton) {
								System.out.println("selected");
								((JRadioButton) selField).setSelected(true);
							}
							else System.out.println("not found");
						}
					}
					else if (field instanceof JCheckBox) {
						System.out.println("   - field is checkbox, selected set to " + "true".equals(fieldValue));
						((JCheckBox) field).setSelected("true".equals(fieldValue));
					}
					else if (field instanceof JTextField) {
						System.out.println("   - field is text field, text set to " + fieldValue);
						((JTextField) field).setText((fieldValue == null) ? "" : fieldValue);
					}
					else if (field instanceof JComboBox) {
						System.out.println("   - field is combobox");
						Map itemMap = ((Map) this.confirmFieldMappings.get("itemsFor" + field.hashCode()));
						if (fieldValue == null) {
							System.out.println("     ==> cleared");
							((JComboBox) field).setSelectedItem(null);
						}
						else if (itemMap == null) {
							System.out.println("     ==> value directly set to " + fieldValue);
							((JComboBox) field).setSelectedItem(fieldValue);
						}
						else {
							System.out.println("     ==> value mapped set to " + (itemMap.containsKey(fieldValue) ? itemMap.get(fieldValue) : fieldValue));
							((JComboBox) field).setSelectedItem(itemMap.containsKey(fieldValue) ? itemMap.get(fieldValue) : fieldValue);
						}
					}
					else System.out.println("   ==> field of unknown type");
				}
			
			//	un-block waiting confirm() call
			synchronized (this) {
				this.notify();
			}
		}
		
		private FeedbackPanel feedbackPanel = null;
		private FeedbackPanelHtmlRendererInstance feedbackPanelRenderer = null;
		void getFeedback(FeedbackPanel fp) {
			
			//	remember feedback panel
			this.feedbackPanel = fp;
			this.feedbackPanelRenderer = FeedbackPanelHtmlRenderer.getRenderer(fp);
			
			//	block until processFeedback() gets the answer
			synchronized (this) {
				try {
					actionThreadAwaitingFeedback = this;
					this.setFinalOrBlockingJavaScriptCall("showFeedbackDialog();");
					this.wait();
					actionThreadAwaitingFeedback = null;
				} catch (InterruptedException ie) {}
			}
			
			//	clean up
			this.feedbackPanelRenderer = null;
			this.feedbackPanel = null;
		}
		HtmlPageBuilder getFeedbackFormPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException {
			
			//	create form page builder (we're OK with parent as page builder host, won't need alert() and confirm() in feedback dialog)
			return new FeedbackFormPageBuilder(parent, request, response, this.feedbackPanel, this.feedbackPanelRenderer, actionThreadFeedbackSubmitModes, id) {
				protected String getFormActionPathInfo() {
					return ("/" + id + "/giveFeedback");
				}
			};
		}
		void processFeedback(HttpServletRequest request) throws IOException {
			
			//	cancelled
			if (request == null)
				this.feedbackPanel.setStatusCode("Cancel");
			
			//	answered
			else {
				
				//	extract data from HTTP request ...
				Properties responseData = new Properties();
				Enumeration paramNames = request.getParameterNames();
				while (paramNames.hasMoreElements()) {
					String paramName = paramNames.nextElement().toString();
					String paramValue = request.getParameter(paramName);
					if (paramValue != null)
						responseData.setProperty(paramName, paramValue);
				}
				
				//	... and inject it into feedback request
				this.feedbackPanel.setStatusCode(responseData.getProperty(FeedbackFormBuilder.SUBMIT_MODE_PARAMETER, "OK"));
				this.feedbackPanelRenderer.readResponse(responseData);
			}
			
			//	continue waiting thread
			synchronized (this) {
				this.notify();
			}
		}
		
		private ImWord editingWord;
		private boolean wordEdited = false;
		boolean editWord(ImWord word) {
			
			//	remember word
			this.editingWord = word;
			this.wordEdited = false;
			
			//	block until processFeedback() gets the answer
			synchronized (this) {
				try {
					actionThreadEditingWord = this;
					this.setFinalOrBlockingJavaScriptCall("doEditWord(null);");
					this.wait();
					actionThreadEditingWord = null;
				} catch (InterruptedException ie) {}
			}
			
			//	clean up and return edit result
			this.editingWord = null;
			return this.wordEdited;
			
		}
		ImWord getEditingWord() {
			return this.editingWord;
		}
//		HtmlPageBuilder getEditWordPageBuilder(HttpServletRequest request, HttpServletResponse response) throws IOException {
//			return new HtmlPageBuilder(parent, request, response) {
//				protected void include(String type, String tag) throws IOException {
//					if ("includeTitle".equals(type))
//						this.write(html.escape("Edit Word '" + editingWord.getString() + "'"));
//					else if ("includeWordImage".equals(type))
//						this.write("<img src=\"" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/wordImage/" + editingWord.getLocalID() + ".png\" />");
//					else if ("includeForm".equals(type)) {
//						this.writeLine("<form id=\"wordAttributeForm\" method=\"POST\" action=\"" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editWord\" style=\"display: none;\">");
//						this.writeLine("<input type=\"hidden\" id=\"wordString\" name=\"" + ImWord.STRING_ATTRIBUTE + "\" value=\"" + html.escape(editingWord.getString()) + "\"/>");
//						this.writeLine("<input type=\"hidden\" id=\"wordIsBold\" name=\"" + ImWord.BOLD_ATTRIBUTE + "\" value=\"" + (editingWord.hasAttribute(ImWord.BOLD_ATTRIBUTE) ? "true" : "false") + "\"/>");
//						this.writeLine("<input type=\"hidden\" id=\"wordIsItalics\" name=\"" + ImWord.ITALICS_ATTRIBUTE + "\" value=\"" + (editingWord.hasAttribute(ImWord.ITALICS_ATTRIBUTE) ? "true" : "false") + "\"/>");
//						this.writeLine("<input type=\"hidden\" id=\"wordEdited\" name=\"wordEdited\" value=\"false\"/>");
//						this.writeLine("</form>");
//					}
//					else if ("includeInitCalls".equals(type)) {
//						this.writeLine("<script type=\"text/javascript\">");
//						this.writeLine("getById('wordStringField').value = getById('wordString').value;");
//						this.writeLine("getById('wordIsBoldField').checked = ((getById('wordIsBold').value == 'true') ? 'checked' : null);");
//						this.writeLine("getById('wordIsItalicsField').checked = ((getById('wordIsItalics').value == 'true') ? 'checked' : null);");
//						this.writeLine("</script>");
//					}
//					else super.include(type, tag);
//				}
//				protected boolean includeJavaScriptDomHelpers() {
//					return true;
//				}
//				protected void writePageHeadExtensions() throws IOException {
//					
//					//	open script and create attribute value index
//					this.writeLine("<script type=\"text/javascript\">");
//					
//					//	TODO_above somehow provide the symbol table
//					//	TODO_above ==> try a non-modal (or volatile), fixed-position, fixed-size dialog for this ...
//					//	TODO_above ... somehow aligned with word edit dialog
//					//	TODO_above open 'symbolTable' from main servlet ...
//					//	TODO_above ... and replace its useSymbol() function with something useful
//					
//					//	close script
//					this.writeLine("</script>");
//				}
//			};
//		}
		void wordEdited(HttpServletRequest request) {
			
			//	check form submission status
			this.wordEdited = "true".equals(request.getParameter("wordEdited"));
			
			//	perform word updates
			if (this.wordEdited) {
				String string = request.getParameter(ImWord.STRING_ATTRIBUTE);
				boolean bold = "true".equals(request.getParameter(ImWord.BOLD_ATTRIBUTE));
				boolean italics = "true".equals(request.getParameter(ImWord.ITALICS_ATTRIBUTE));
				if (string != null) {
					string = string.trim();
					if (!string.equals(this.editingWord.getString()))
						this.editingWord.setString(string);
				}
				if (bold != this.editingWord.hasAttribute(ImWord.BOLD_ATTRIBUTE))
					this.editingWord.setAttribute(ImWord.BOLD_ATTRIBUTE, (bold ? "true" : null));
				if (italics != this.editingWord.hasAttribute(ImWord.ITALICS_ATTRIBUTE))
					this.editingWord.setAttribute(ImWord.ITALICS_ATTRIBUTE, (italics ? "true" : null));
			}
			
			//	continue waiting thread
			synchronized (this) {
				this.notify();
			}
		}
	}
	
	private static SubmitMode[] actionThreadFeedbackSubmitModes = {
		new SubmitMode("OK", "OK", "Submit feedback"),
	};
	
	private static class MultiActionServletRequest implements HttpServletRequest {
		private HttpServletRequest request;
		MultiActionServletRequest(HttpServletRequest request) {
			this.request = request;
		}
		void setRequest(HttpServletRequest request) {
			this.request = request;
		}
		public Object getAttribute(String name) {
			return this.request.getAttribute(name);
		}
		public Enumeration getAttributeNames() {
			return this.request.getAttributeNames();
		}
		public String getCharacterEncoding() {
			return this.request.getCharacterEncoding();
		}
		public int getContentLength() {
			return this.request.getContentLength();
		}
		public String getContentType() {
			return this.request.getContentType();
		}
		public ServletInputStream getInputStream() throws IOException {
			return this.request.getInputStream();
		}
		public String getLocalAddr() {
			return this.request.getLocalAddr();
		}
		public String getLocalName() {
			return this.request.getLocalName();
		}
		public int getLocalPort() {
			return this.request.getLocalPort();
		}
		public Locale getLocale() {
			return this.request.getLocale();
		}
		public Enumeration getLocales() {
			return this.request.getLocales();
		}
		public String getParameter(String name) {
			return this.request.getParameter(name);
		}
		public Map getParameterMap() {
			return this.request.getParameterMap();
		}
		public Enumeration getParameterNames() {
			return this.request.getParameterNames();
		}
		public String[] getParameterValues(String name) {
			return this.request.getParameterValues(name);
		}
		public String getProtocol() {
			return this.request.getProtocol();
		}
		public BufferedReader getReader() throws IOException {
			return this.request.getReader();
		}
		public String getRealPath(String path) {
			return this.request.getRealPath(path);
		}
		public String getRemoteAddr() {
			return this.request.getRemoteAddr();
		}
		public String getRemoteHost() {
			return this.request.getRemoteHost();
		}
		public int getRemotePort() {
			return this.request.getRemotePort();
		}
		public RequestDispatcher getRequestDispatcher(String path) {
			return this.request.getRequestDispatcher(path);
		}
		public String getScheme() {
			return this.request.getScheme();
		}
		public String getServerName() {
			return this.request.getServerName();
		}
		public int getServerPort() {
			return this.request.getServerPort();
		}
		public boolean isSecure() {
			return this.request.isSecure();
		}
		public void removeAttribute(String name) {
			this.request.removeAttribute(name);
		}
		public void setAttribute(String name, Object value) {
			this.request.setAttribute(name, value);
		}
		public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
			this.request.setCharacterEncoding(enc);
		}
		public String getAuthType() {
			return this.request.getAuthType();
		}
		public String getContextPath() {
			return this.request.getContextPath();
		}
		public Cookie[] getCookies() {
			return this.request.getCookies();
		}
		public long getDateHeader(String name) {
			return this.request.getDateHeader(name);
		}
		public String getHeader(String name) {
			return this.request.getHeader(name);
		}
		public Enumeration getHeaderNames() {
			return this.request.getHeaderNames();
		}
		public Enumeration getHeaders(String name) {
			return this.request.getHeaders(name);
		}
		public int getIntHeader(String name) {
			return this.request.getIntHeader(name);
		}
		public String getMethod() {
			return this.request.getMethod();
		}
		public String getPathInfo() {
			return this.request.getPathInfo();
		}
		public String getPathTranslated() {
			return this.request.getPathTranslated();
		}
		public String getQueryString() {
			return this.request.getQueryString();
		}
		public String getRemoteUser() {
			return this.request.getRemoteUser();
		}
		public String getRequestURI() {
			return this.request.getRequestURI();
		}
		public StringBuffer getRequestURL() {
			return this.request.getRequestURL();
		}
		public String getRequestedSessionId() {
			return this.request.getRequestedSessionId();
		}
		public String getServletPath() {
			return this.request.getServletPath();
		}
		public HttpSession getSession() {
			return this.request.getSession();
		}
		public HttpSession getSession(boolean create) {
			return this.request.getSession(create);
		}
		public Principal getUserPrincipal() {
			return this.request.getUserPrincipal();
		}
		public boolean isRequestedSessionIdFromCookie() {
			return this.request.isRequestedSessionIdFromCookie();
		}
		public boolean isRequestedSessionIdFromURL() {
			return this.request.isRequestedSessionIdFromURL();
		}
		public boolean isRequestedSessionIdFromUrl() {
			return this.request.isRequestedSessionIdFromUrl();
		}
		public boolean isRequestedSessionIdValid() {
			return this.request.isRequestedSessionIdValid();
		}
		public boolean isUserInRole(String role) {
			return this.request.isUserInRole(role);
		}
	}
	private static class MultiActionServletResponse implements HttpServletResponse {
		private HttpServletResponse response;
		private ArrayList enqueuedJavaScriptCalls = new ArrayList(1);
		private String characterEncoding = null;
		MultiActionServletResponse(HttpServletResponse response) {
			this.response = response;
		}
		void setResponse(HttpServletResponse response) {
			this.response = response;
		}
		void enqueueJavaScriptCall(String jsc) {
			this.enqueuedJavaScriptCalls.add(jsc);
		}
		public void flushBuffer() throws IOException {
			this.response.flushBuffer();
		}
		public int getBufferSize() {
			return this.response.getBufferSize();
		}
		public String getCharacterEncoding() {
			return this.response.getCharacterEncoding();
		}
		public String getContentType() {
			return this.response.getContentType();
		}
		public Locale getLocale() {
			return this.response.getLocale();
		}
		public ServletOutputStream getOutputStream() throws IOException {
			ServletOutputStream sos = this.response.getOutputStream();
			for (int c = 0; c < this.enqueuedJavaScriptCalls.size(); c++) {
				sos.write(((String) this.enqueuedJavaScriptCalls.get(c)).getBytes((this.characterEncoding == null) ? this.getCharacterEncoding() : this.characterEncoding));
				sos.write("\r\n".getBytes((this.characterEncoding == null) ? this.getCharacterEncoding() : this.characterEncoding));
			}
			return sos;
		}
		public PrintWriter getWriter() throws IOException {
			PrintWriter pw = this.response.getWriter();
			for (int c = 0; c < this.enqueuedJavaScriptCalls.size(); c++)
				pw.write(((String) this.enqueuedJavaScriptCalls.get(c)) + "\r\n");
			return pw;
		}
		public boolean isCommitted() {
			return this.response.isCommitted();
		}
		public void reset() {
			this.response.reset();
		}
		public void resetBuffer() {
			this.response.resetBuffer();
		}
		public void setBufferSize(int size) {
			this.response.setBufferSize(size);
		}
		public void setCharacterEncoding(String enc) {
			this.characterEncoding = enc;
			this.response.setCharacterEncoding(enc);
		}
		public void setContentLength(int length) {
			this.response.setContentLength(length);
		}
		public void setContentType(String type) {
			this.response.setContentType(type);
		}
		public void setLocale(Locale locale) {
			this.response.setLocale(locale);
		}
		public void addCookie(Cookie cookie) {
			this.response.addCookie(cookie);
		}
		public void addDateHeader(String name, long value) {
			this.response.addDateHeader(name, value);
		}
		public void addHeader(String name, String value) {
			this.response.addHeader(name, value);
		}
		public void addIntHeader(String name, int value) {
			this.response.addIntHeader(name, value);
		}
		public boolean containsHeader(String name) {
			return this.response.containsHeader(name);
		}
		public String encodeRedirectURL(String url) {
			return this.response.encodeRedirectURL(url);
		}
		public String encodeRedirectUrl(String url) {
			return this.response.encodeRedirectUrl(url);
		}
		public String encodeURL(String url) {
			return this.response.encodeURL(url);
		}
		public String encodeUrl(String url) {
			return this.response.encodeUrl(url);
		}
		public void sendError(int sc, String msg) throws IOException {
			this.response.sendError(sc, msg);
		}
		public void sendError(int sc) throws IOException {
			this.response.sendError(sc);
		}
		public void sendRedirect(String location) throws IOException {
			this.response.sendRedirect(location);
		}
		public void setDateHeader(String name, long value) {
			this.response.setDateHeader(name, value);
		}
		public void setHeader(String name, String value) {
			this.response.setHeader(name, value);
		}
		public void setIntHeader(String name, int value) {
			this.response.setIntHeader(name, value);
		}
		public void setStatus(int sc, String msg) {
			this.response.setStatus(sc, msg);
		}
		public void setStatus(int sc) {
			this.response.setStatus(sc);
		}
	}
	
	private void writeJavaScriptTag(HtmlPageBuilder hpb, String jsName) throws IOException {
		hpb.writeLine("<script type=\"text/javascript\" src=\"" + hpb.request.getContextPath() + hpb.request.getServletPath() + "/" + this.id + "/" + jsName + "\"></script>");
	}
	
	private static String convertSwingMessage(JComponent comp, Map fields) {
		
		//	panel, deal with layout of children
		if (comp instanceof JPanel) {
			LayoutManager lm = ((JPanel) comp).getLayout();
			Component[] sComps = ((JPanel) comp).getComponents();
			StringBuffer sb = new StringBuffer();
			if (lm instanceof FlowLayout) {
				
				//	get alignment and generate CSS class
				String cssClass = "flowLayout";
				int a = ((FlowLayout) lm).getAlignment();
				if ((a == FlowLayout.LEFT) || (a == FlowLayout.LEADING))
					cssClass += "Left";
				else if ((a == FlowLayout.RIGHT) || (a == FlowLayout.TRAILING))
					cssClass += "Right";
				else cssClass += "Center";
				
				//	wrap children in DIV
				sb.append("<div class=\"" + cssClass + "\">");
				for (int c = 0; c < sComps.length; c++) {
					if (sComps[c] instanceof JComponent)
						sb.append(convertSwingMessage(((JComponent) sComps[c]), fields));
				}
				sb.append("</div>");
			}
			else if (lm instanceof BoxLayout) {
				
				//	wrap children in single table row
				sb.append("<table class=\"boxLayout\"><tr class=\"boxLayoutRow\">");
				for (int c = 0; c < sComps.length; c++)
					if (sComps[c] instanceof JComponent) {
						sb.append("<td class=\"boxLayoutCell\">");
						sb.append(convertSwingMessage(((JComponent) sComps[c]), fields));
						sb.append("</td>");
					}
				sb.append("</tr></table>");
			}
			else if (lm instanceof GridLayout) {
				
				//	compute grid dimensions
				int cols = ((GridLayout) lm).getColumns();
				int rows = ((GridLayout) lm).getRows();
				if (cols == 0)
					cols = ((sComps.length + rows - 1) / rows);
				else if (rows == 0)
					rows = ((sComps.length + cols - 1) / cols);
				
				//	wrap children in table
				sb.append("<table class=\"gridLayout\">");
				for (int r = 0; r < rows; r++) {
					sb.append("<tr class=\"gridLayoutRow\">");
					for (int c = 0; c < cols; c++) {
						sb.append("<td class=\"gridLayoutCell\">");
						if ((((r * cols) + c) < sComps.length) && (sComps[(r * cols) + c] instanceof JComponent))
							sb.append(convertSwingMessage(((JComponent) sComps[(r * cols) + c]), fields));
						else sb.append("&nbsp;");
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				sb.append("</table>");
			}
			else if (lm instanceof BorderLayout) {
				
				//	get occupied positions
				Component north = ((BorderLayout) lm).getLayoutComponent(BorderLayout.NORTH);
				Component west = ((BorderLayout) lm).getLayoutComponent(BorderLayout.WEST);
				Component center = ((BorderLayout) lm).getLayoutComponent(BorderLayout.CENTER);
				Component east = ((BorderLayout) lm).getLayoutComponent(BorderLayout.EAST);
				Component south = ((BorderLayout) lm).getLayoutComponent(BorderLayout.SOUTH);
				
				//	compute number of table columns
				int nsColspan = (((west instanceof JComponent) ? 1 : 0) + ((center instanceof JComponent) ? 1 : 0) + ((east instanceof JComponent) ? 1 : 0));
				
				//	wrap children in table
				sb.append("<table class=\"borderLayout\">");
				if (north instanceof JComponent) {
					sb.append("<tr class=\"borderLayoutNorthRow\"><td class=\"borderLayoutNorthCell\"" + ((nsColspan > 1) ? (" colspan=\"" + nsColspan + "\"") : "") + ">");
					sb.append(convertSwingMessage(((JComponent) north), fields));
					sb.append("</td></tr>");
				}
				if (nsColspan != 0) {
					sb.append("<tr class=\"borderLayoutCenterRow\">");
					if (west instanceof JComponent) {
						sb.append("<td class=\"borderLayoutWestCell\">");
						sb.append(convertSwingMessage(((JComponent) west), fields));
						sb.append("</td>");
					}
					if (center instanceof JComponent) {
						sb.append("<td class=\"borderLayoutCenterCell\">");
						sb.append(convertSwingMessage(((JComponent) center), fields));
						sb.append("</td>");
					}
					if (east instanceof JComponent) {
						sb.append("<td class=\"borderLayoutEastCell\">");
						sb.append(convertSwingMessage(((JComponent) east), fields));
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				if (south instanceof JComponent) {
					sb.append("<tr class=\"borderLayoutSouthRow\"><td class=\"borderLayoutSouthCell\"" + ((nsColspan > 1) ? (" colspan=\"" + nsColspan + "\"") : "") + ">");
					sb.append(convertSwingMessage(((JComponent) south), fields));
					sb.append("</td></tr>");
				}
				sb.append("</table>");
			}
			else if (lm instanceof GridBagLayout) {
				
				//	compute grid dimensions from constraints associated with each child
				int cols = 0;
				int rows = 0;
				for (int s = 0; s < sComps.length; s++) {
					GridBagConstraints gbc = ((GridBagLayout) lm).getConstraints(sComps[s]);
					cols = Math.max(cols, (gbc.gridx + gbc.gridwidth));
					rows = Math.max(rows, (gbc.gridy + gbc.gridheight));
				}
				
				//	sort children into array, copying for grid width and height > 1
				JComponent[][] sCompGrid = new JComponent[rows][cols];
				for (int s = 0; s < sComps.length; s++)
					if (sComps[s] instanceof JComponent) {
						GridBagConstraints gbc = ((GridBagLayout) lm).getConstraints(sComps[s]);
						for (int c = gbc.gridx; c < (gbc.gridx + gbc.gridwidth); c++) {
							for (int r = gbc.gridy; r < (gbc.gridy + gbc.gridheight); r++)
								sCompGrid[r][c] = ((JComponent) sComps[s]);
						}
					}
				
				//	wrap children in table
				sb.append("<table class=\"gridBagLayout\">");
				for (int r = 0; r < rows; r++) {
					sb.append("<tr class=\"gridBagLayoutRow\">");
					for (int c = 0; c < cols; c++) {
						
						//	gap, or not a JComponent
						if (sCompGrid[r][c] == null) {
							sb.append("<td class=\"gridBagLayoutCell\">");
							sb.append("&nbsp;");
							sb.append("</td>");
							continue;
						}
						
						//	multi-row or multi-column component, we've already dealt with this one
						if ((c != 0) && (sCompGrid[r][c-1] == sCompGrid[r][c]))
							continue;
						if ((r != 0) && (sCompGrid[r-1][c] == sCompGrid[r][c]))
							continue;
						
						//	use constraints for row span and column span
						GridBagConstraints gbc = ((GridBagLayout) lm).getConstraints(sCompGrid[r][c]);
						sb.append("<td class=\"gridBagLayoutCell\"" + ((gbc.gridwidth > 1) ? (" colspan=\"" + gbc.gridwidth + "\"") : "") + ((gbc.gridheight > 1) ? (" rowspan=\"" + gbc.gridheight + "\"") : "") + ">");
						sb.append(convertSwingMessage(sCompGrid[r][c], fields));
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				sb.append("</table>");
			}
			else {
				sb.append("<div>");
				for (int c = 0; c < sComps.length; c++) {
					if (sComps[c] instanceof JComponent)
						sb.append(convertSwingMessage(((JComponent) sComps[c]), fields));
				}
				sb.append("</div>");
			}
			return sb.toString();
		}
		
		//	label, add it as is
		else if (comp instanceof JLabel) {
			String label = ((JLabel) comp).getText();
			String tooltip = ((JLabel) comp).getToolTipText();
			return ("<span" +
					" class=\"confirmFormLabel\"" + 
					((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + ">" +
					"" + html.escape(label).replaceAll("[\\r\\n]+", "<br/>") + "" +
					"</span>");
		}
		
		//	radio button, add together with label
		else if (comp instanceof JRadioButton) {
			ButtonGroup bg = ((JRadioButton.ToggleButtonModel) ((JRadioButton) comp).getModel()).getGroup();
			String fieldName = ("f" + bg.hashCode());
			fields.put(fieldName, bg);
			String fieldValue = ("f" + comp.hashCode());
			fields.put(fieldValue, comp);
			String label = ((JRadioButton) comp).getText();
			String tooltip = ((JRadioButton) comp).getToolTipText();
			boolean selected = ((JRadioButton) comp).isSelected();
			return ("<input" +
						" type=\"radio\"" +
						" id=\"" + fieldValue + "\"" +
						" name=\"" + fieldName + "\"" +
						" value=\"" + fieldValue + "\"" +
						(selected ? " checked=\"checked\"" : "") + 
						((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + "" +
					"/>" +
					"<span" +
						" class=\"confirmFormLabel\"" + 
						" onclick=\"return select" + fieldValue + "();\"" + 
						((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + 
						">" +
						html.escape(label).replaceAll("[\\r\\n]+", "<br/>") + "" +
					"</span>" +
					"<script type=\"text/javascript\">" +
					"function select" + fieldValue + "() {" +
						"var rb = getById('" + fieldValue + "');" +
						"rb.checked = (rb.checked ? null : 'checked');" +
						"return false;" +
					"}" +
					"</script>");
		}
		
		//	checkbox, add together with label
		else if (comp instanceof JCheckBox) {
			String fieldName = ("f" + comp.hashCode());
			fields.put(fieldName, comp);
			String label = ((JCheckBox) comp).getText();
			String tooltip = ((JCheckBox) comp).getToolTipText();
			boolean selected = ((JCheckBox) comp).isSelected();
			return ("<input" +
						" type=\"checkbox\"" +
						" id=\"" + fieldName + "\"" +
						" name=\"" + fieldName + "\"" +
						" value=\"true\"" + 
						(selected ? " checked=\"checked\"" : "") + "" + 
						((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + 
					"/>" +
					"<span" +
						" class=\"confirmFormLabel\"" + 
						" onclick=\"return select" + fieldName + "();\"" + 
						((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + 
						">" +
						html.escape(label).replaceAll("[\\r\\n]+", "<br/>") +
						"</span>" +
						"<script type=\"text/javascript\">" +
						"function select" + fieldName + "() {" +
							"var cb = getById('" + fieldName + "');" +
							"cb.checked = (cb.checked ? null : 'checked');" +
							"return false;" +
						"}" +
						"</script>");
		}
		
		//	text field, add as is
		else if (comp instanceof JTextField) {
			String fieldName = ("f" + comp.hashCode());
			fields.put(fieldName, comp);
			String fieldValue = ((JTextField) comp).getText();
			String tooltip = ((JTextField) comp).getToolTipText();
			return ("<input" +
						" type=\"text\"" +
						" id=\"" + fieldName + "\"" +
						" name=\"" + fieldName + "\"" +
						" value=\"" + ((fieldValue == null) ? "" : html.escape(fieldValue)) + "\"" + 
						((tooltip == null) ? "" : (" title=\"" + html.escape(tooltip) + "\"")) + 
					"/>");
		}
		
		//	combo box, add as is
		else if (comp instanceof JComboBox) {
			String fieldName = ("f" + comp.hashCode());
			fields.put(fieldName, comp);
			Object[] items = new Object[((JComboBox) comp).getItemCount()];
			Map itemMap = new LinkedHashMap();
			for (int i = 0; i < items.length; i++) {
				items[i] = ((JComboBox) comp).getItemAt(i);
				itemMap.put(items[i].toString(), items[i]);
			}
			fields.put(("itemsFor" + comp.hashCode()), itemMap);
			StringBuffer sb = new StringBuffer();
			if (((JComboBox) comp).isEditable()) {
				sb.append("<input" +
						" type=\"text\"" +
						" id=\"" + fieldName + "\"" +
						" name=\"" + fieldName + "\"" +
						" list=\"optsFor" + comp.hashCode() + "\"" +
						"/>");
				sb.append("<datalist id=\"optsFor" + comp.hashCode() + "\">");
				for (Iterator isit = itemMap.keySet().iterator(); isit.hasNext();) {
					String is = ((String) isit.next());
					sb.append("<option value=\"" + html.escape(is) + "\"/>");
				}
				sb.append("</datalist>");
			}
			else {
				sb.append("<select" +
						" id=\"" + fieldName + "\"" +
						" name=\"" + fieldName + "\"" +
						">");
				for (Iterator isit = itemMap.keySet().iterator(); isit.hasNext();) {
					String is = ((String) isit.next());
					sb.append("<option value=\"" + html.escape(is) + "\">" + html.escape(is) + "</option>");
				}
				sb.append("</select>");
			}
			return sb.toString();
		}
		
		//	something else, ignore it
		else return "";
	}
	
	/* Sub class of actual markup panel, preventing all rendering, but still
	 * managing display settings, and reflecting document updates in respective
	 * JavaScript calls that update the projected browser-side model. */
	static class WebImDocumentMarkupPanel extends ImDocumentMarkupPanel implements AtomicActionListener {
		
		private ImDocumentListener undoRecorder;
		private LinkedList undoActions = new LinkedList();
		private MultipartUndoAction multipartUndoAction = null;
		private boolean inUndoAction = false;
		
		private ImDocumentListener reactionTrigger = null;
		private boolean imToolActive = false;
		private HashSet inReactionObjects = new HashSet();
		
		private ImDocumentListener updateRelayer;
		private LinkedList javaScriptCalls = new LinkedList();
		private ArrayList undoMenuBuilderJavaScriptCalls = new ArrayList();
		private String[] initJavaScriptCalls = null;
		
		private GoldenGateImagine ggImagine;
		private Settings ggImagineSettings;
		
		private ImageDocumentDropHandler[] dropHandlers = new ImageDocumentDropHandler[0];
		
		private SelectionActionProvider[] selectionActionProviders = new SelectionActionProvider[0];
		
		private int modCount = 0;
		private int savedModCount = 0;
		
		WebImDocumentMarkupPanel(ImDocument document, GoldenGateImagine ggImagine, Settings ggImagineSettings) {
			this(document, 0, document.getPageCount(), ggImagine, ggImagineSettings);
		}
		
		WebImDocumentMarkupPanel(ImDocument document, int fvp, int vpc, GoldenGateImagine ggImagine, Settings ggImagineSettings) {
			super(document, fvp, vpc);
			this.ggImagine = ggImagine;
			this.ggImagineSettings = ggImagineSettings;
			
			//	inject highlight colors for annotations, regions, and text streams
			Settings annotationColors = this.ggImagineSettings.getSubset("annotation.color");
			String[] annotationTypes = annotationColors.getKeys();
			for (int t = 0; t < annotationTypes.length; t++) {
				Color ac = GoldenGateImagine.getColor(annotationColors.getSetting(annotationTypes[t]));
				if (ac != null)
					this.setAnnotationColor(annotationTypes[t], ac);
			}
			Settings layoutObjectColors = this.ggImagineSettings.getSubset("layoutObject.color");
			String[] layoutObjectTypes = layoutObjectColors.getKeys();
			for (int t = 0; t < layoutObjectTypes.length; t++) {
				Color loc = GoldenGateImagine.getColor(layoutObjectColors.getSetting(layoutObjectTypes[t]));
				if (loc != null)
					this.setLayoutObjectColor(layoutObjectTypes[t], loc);
			}
			Settings textStreamColors = this.ggImagineSettings.getSubset("textStream.color");
			String[] textStreamTypes = textStreamColors.getKeys();
			for (int t = 0; t < textStreamTypes.length; t++) {
				Color tsc = GoldenGateImagine.getColor(textStreamColors.getSetting(textStreamTypes[t]));
				if (tsc != null)
					this.setTextStreamTypeColor(textStreamTypes[t], tsc);
			}
			
			//	keep tabs on atomic actions
			this.addAtomicActionListener(this);
			
			//	prepare recording UNDO actions
			this.undoRecorder = new UndoRecorder();
			this.document.addDocumentListener(this.undoRecorder);
			
			//	get data drop handlers
			this.dropHandlers = this.ggImagine.getDropHandlers();
			
			//	get selection action providers
			this.selectionActionProviders = this.ggImagine.getSelectionActionProviders();
			
			//	get reaction providers
			ReactionProvider[] reactionProviders = this.ggImagine.getReactionProviders();
			if (reactionProviders.length != 0) {
				this.reactionTrigger = new ReactionTrigger(reactionProviders);
				this.document.addDocumentListener(this.reactionTrigger);
			}
			
			//	prepare recording edits to generate JavaScript calls and forward edits to web page
			this.updateRelayer = new UpdateRelayer();
			this.document.addDocumentListener(this.updateRelayer);
			
			//	remember initialization JavaScript (also clears register)
			this.initJavaScriptCalls = this.getJavaScriptCalls();
		}
		
		/**
		 * Retrieve the JavaScript calls initializing a web page editor front-end,
		 * e.g. with highlight colors.
		 * @return an array holding the JavaScript calls
		 */
		synchronized String[] getInitJavaScriptCalls() {
			return this.initJavaScriptCalls;
		}
		
		private int nextJavaScriptCallId = 0;
		synchronized void addJavaScriptCall(String jsc) {
			if (this.javaScriptCalls == null)
				return; // some color setters are called from super constructor, before list is created by our own constructor
			if (jsc.endsWith("();"))
				jsc = (jsc.substring(0, (jsc.length() - ");".length())) + (this.nextJavaScriptCallId++) + ");");
			else jsc = (jsc.substring(0, (jsc.length() - ");".length())) + ", " + (this.nextJavaScriptCallId++) + ");");
			this.javaScriptCalls.addLast(jsc);
			if (jsc.startsWith("updateUndoMenu("))
				this.undoMenuBuilderJavaScriptCalls.add(jsc);
		}
		
		private static String escapeForJavaScript(String str) {
			StringBuffer escaped = new StringBuffer();
			char ch;
			for (int c = 0; c < str.length(); c++) {
				ch = str.charAt(c);
				if ((ch == '\\') || (ch == '\''))
					escaped.append('\\');
				if (ch < 32)
					escaped.append(' ');
				else escaped.append(ch);
			}
			return escaped.toString();
		}
		
		/**
		 * Retrieve the JavaScript calls updating a web page editor front-end that
		 * were collected since the last call to this method.
		 * @return an array holding the JavaScript calls
		 */
		synchronized String[] getJavaScriptCalls() {
			String[] jscs = ((String[]) this.javaScriptCalls.toArray(new String[this.javaScriptCalls.size()]));
			this.javaScriptCalls.clear();
			return jscs;
		}
		
		synchronized String[] getUndoMenuBuilderJavaScriptCalls() {
			return ((String[]) this.undoMenuBuilderJavaScriptCalls.toArray(new String[this.undoMenuBuilderJavaScriptCalls.size()]));
		}
		
		boolean isDirty() {
			return (this.modCount != this.savedModCount);
		}
		
		void setNotDirty() {
			this.savedModCount = this.modCount;
		}
		
		/**
		 * Close the document markup panel, unregister listeners, etc.
		 */
		void close() {
			this.document.removeDocumentListener(this.undoRecorder);
			if (this.reactionTrigger != null)
				this.document.removeDocumentListener(this.reactionTrigger);
			this.document.removeDocumentListener(this.updateRelayer);
			this.ggImagine.notifyDocumentClosed(this.document.docId);
		}
		
		private class UndoRecorder implements ImDocumentListener {
			public void typeChanged(final ImObject object, final String oldType) {
				if (inUndoAction)
					return;
				addUndoAction(new UndoAction("Change Object Type to '" + object.getType() + "'") {
					void doExecute() {
						object.setType(oldType);
					}
				});
			}
			public void regionAdded(final ImRegion region) {
				if (inUndoAction)
					return;
				addUndoAction(new UndoAction("Add '" + region.getType() + "' Region") {
					void doExecute() {
						WebImDocumentMarkupPanel.this.document.getPage(region.pageId).removeRegion(region);
					}
				});
			}
			public void regionRemoved(final ImRegion region) {
				if (inUndoAction)
					return;
				if (region instanceof ImWord)
					addUndoAction(new UndoAction("Remove Word '" + region.getAttribute(ImWord.STRING_ATTRIBUTE) + "'") {
						void doExecute() {
							WebImDocumentMarkupPanel.this.document.getPage(region.pageId).addWord((ImWord) region);
						}
					});
				else addUndoAction(new UndoAction("Remove '" + region.getType() + "' Region") {
					void doExecute() {
						WebImDocumentMarkupPanel.this.document.getPage(region.pageId).addRegion(region);
					}
				});
			}
			public void attributeChanged(final ImObject object, final String attributeName, final Object oldValue) {
				if (inUndoAction)
					return;
				if (oldValue == null)
					addUndoAction(new UndoAction("Add '" + attributeName + "' Attribute to " + object.getType()) {
						void doExecute() {
							object.setAttribute(attributeName, oldValue); // we need to set here instead of removing, as some objects have built-in special attributes (ImWord !!!)
						}
					});
				else if (object.getAttribute(attributeName) == null)
					addUndoAction(new UndoAction("Remove '" + attributeName + "' Attribute from " + object.getType()) {
						void doExecute() {
							object.setAttribute(attributeName, oldValue);
						}
					});
				else addUndoAction(new UndoAction("Change '" + attributeName + "' Attribute of " + object.getType() + " to '" + object.getAttribute(attributeName).toString() + "'") {
					void doExecute() {
						object.setAttribute(attributeName, oldValue);
					}
				});
			}
			public void supplementChanged(final String supplementId, final ImSupplement oldValue) {
				if (inUndoAction)
					return;
				if (oldValue == null) {
					final ImSupplement newValue = document.getSupplement(supplementId);
					addUndoAction(new UndoAction("Add '" + supplementId + "' Supplement") {
						void doExecute() {
							document.removeSupplement(newValue);
						}
					});
				}
				else if (document.getSupplement(supplementId) == null)
					addUndoAction(new UndoAction("Remove '" + supplementId + "' Supplement") {
						void doExecute() {
							document.addSupplement(oldValue);
						}
					});
				else addUndoAction(new UndoAction("Change '" + supplementId + "' Supplemen") {
					void doExecute() {
						document.addSupplement(oldValue);
					}
				});
			}
			public void annotationAdded(final ImAnnotation annotation) {
				if (inUndoAction)
					return;
				addUndoAction(new UndoAction("Add '" + annotation.getType() + "' Annotation") {
					void doExecute() {
						/* We need to re-get annotation and make our own
						 * comparison, as removing and re-adding thwarts
						 * this simple approach */
						ImAnnotation[] annots = annotation.getDocument().getAnnotations(annotation.getFirstWord(), null);
						for (int a = 0; a < annots.length; a++) {
							if (!annots[a].getLastWord().getLocalID().equals(annotation.getLastWord().getLocalID()))
								continue;
							if (!annots[a].getType().equals(annotation.getType()))
								continue;
							WebImDocumentMarkupPanel.this.document.removeAnnotation(annots[a]);
							break;
						}
					}
				});
			}
			public void annotationRemoved(final ImAnnotation annotation) {
				if (inUndoAction)
					return;
				addUndoAction(new UndoAction("Remove '" + annotation.getType() + "' Annotation") {
					void doExecute() {
						ImAnnotation reAnnot = WebImDocumentMarkupPanel.this.document.addAnnotation(annotation.getFirstWord(), annotation.getLastWord(), annotation.getType());
						if (reAnnot != null)
							reAnnot.copyAttributes(annotation);
					}
				});
			}
		}
		
		private int undoActionId = 0;
		private synchronized int getUndoActionId() {
			return this.undoActionId++;
		}
		private abstract class UndoAction {
			final String label;
			final int id;
			final int modCount;
			UndoAction(String label) {
				this.label = label;
				this.id = getUndoActionId();
				this.modCount = WebImDocumentMarkupPanel.this.modCount;
			}
			final void execute() {
				this.doExecute();
				WebImDocumentMarkupPanel.this.modCount = this.modCount;
			}
			abstract void doExecute();
		}
		
		private void addUndoAction(UndoAction ua) {
			if (this.inUndoAction)
				return;
			if (this.multipartUndoAction == null) {
				this.modCount++;
				this.undoActions.addFirst(ua);
				this.addJavaScriptCall("updateUndoMenu('" + escapeForJavaScript(ua.label) + "', " + ua.id + ");");
			}
			else this.multipartUndoAction.addUndoAction(ua);
		}
		
		private class MultipartUndoAction extends UndoAction {
			LinkedList parts = new LinkedList();
			MultipartUndoAction(String label) {
				super(label);
			}
			synchronized void addUndoAction(UndoAction ua) {
				this.parts.addFirst(ua);
			}
			void doExecute() {
				while (this.parts.size() != 0)
					((UndoAction) this.parts.removeFirst()).doExecute();
			}
		}
		
		private void startMultipartUndoAction(String label) {
			this.multipartUndoAction = new MultipartUndoAction(label);
		}
		
		private void finishMultipartUndoAction() {
			if ((this.multipartUndoAction != null) && (this.multipartUndoAction.parts.size() != 0)) {
				this.modCount++;
				this.undoActions.addFirst(this.multipartUndoAction);
				this.addJavaScriptCall("updateUndoMenu('" + escapeForJavaScript(this.multipartUndoAction.label) + "', " + this.multipartUndoAction.id + ");");
			}
			this.multipartUndoAction = null;
		}
		
		/**
		 * Undo editing up to a specific ID.
		 * @param undoId the ID to stop after
		 */
		void undo(int undoId) {
			this.inUndoAction = true;
			this.beginAtomicAction("UNDO");
			while (this.undoActions.size() != 0) {
				UndoAction ua = ((UndoAction) this.undoActions.removeFirst());
				this.undoMenuBuilderJavaScriptCalls.remove(this.undoMenuBuilderJavaScriptCalls.size()-1);
				ua.execute();
				if (ua.id == undoId)
					break;
			}
			this.endAtomicAction();
			this.inUndoAction = false;
		}
		
		private class ReactionTrigger implements ImDocumentListener {
			private ReactionProvider[] reactionProviders;
			ReactionTrigger(ReactionProvider[] reactionProviders) {
				this.reactionProviders = reactionProviders;
			}
			public void typeChanged(final ImObject object, final String oldType) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(object))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].typeChanged(object, oldType, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to object type change: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(object);
				}
			}
			public void regionAdded(final ImRegion region) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(region))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].regionAdded(region, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to region addition: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(region);
				}
			}
			public void regionRemoved(final ImRegion region) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(region))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].regionRemoved(region, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to region removal: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(region);
				}
			}
			public void attributeChanged(final ImObject object, final String attributeName, final Object oldValue) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(object))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].attributeChanged(object, attributeName, oldValue, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to object attribute change: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(object);
				}
			}
			public void supplementChanged(String supplementId, ImSupplement oldValue) {
				//	no reaction triggering for supplement modifications
			}
			public void annotationAdded(final ImAnnotation annotation) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(annotation))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].annotationAdded(annotation, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to annotation addition: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(annotation);
				}
			}
			public void annotationRemoved(final ImAnnotation annotation) {
				if (inUndoAction || imToolActive || !inReactionObjects.add(annotation))
					return;
				try {
					for (int p = 0; p < this.reactionProviders.length; p++)
						this.reactionProviders[p].annotationRemoved(annotation, WebImDocumentMarkupPanel.this, false);
				}
				catch (Throwable t) {
					System.out.println("Error reacting to annotation removal: " + t.getMessage());
					t.printStackTrace(System.out);
				}
				finally {
					inReactionObjects.remove(annotation);
				}
			}
		}
		
		private class UpdateRelayer implements ImDocumentListener {
			public void typeChanged(final ImObject object, final String oldType) {
				if (object instanceof ImRegion)
					addJavaScriptCall("uSetRegionType('" + oldType + "', " + ((ImRegion) object).pageId + ", '" + ((ImRegion) object).bounds.toString() + "', '" + ((ImRegion) object).getType() + "');");
				else if (object instanceof ImAnnotation)
					addJavaScriptCall("uSetAnnotType('" + oldType + "', '" + ((ImAnnotation) object).getFirstWord().getLocalID() + "', '" + ((ImAnnotation) object).getLastWord().getLocalID() + "', '" + ((ImAnnotation) object).getType() + "');");
			}
			public void attributeChanged(final ImObject object, final String attributeName, final Object oldValue) {
				if (object instanceof ImWord) {
					if (ImWord.PREVIOUS_WORD_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordPredecessor('" + ((ImWord) object).getLocalID() + "', " + ((((ImWord) object).getPreviousWord() == null) ? "null" : ("'" + ((ImWord) object).getPreviousWord().getLocalID() + "'")) + ");");
					else if (ImWord.NEXT_WORD_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordSuccessor('" + ((ImWord) object).getLocalID() + "', " + ((((ImWord) object).getNextWord() == null) ? "null" : ("'" + ((ImWord) object).getNextWord().getLocalID() + "'")) + ");");
					else if (ImWord.NEXT_RELATION_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordRelation('" + ((ImWord) object).getLocalID() + "', '" + ((ImWord) object).getNextRelation() + "');");
					else if (ImWord.TEXT_STREAM_TYPE_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordStreamType('" + ((ImWord) object).getLocalID() + "', '" + ((ImWord) object).getTextStreamType() + "');");
					else if (ImWord.STRING_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordString('" + ((ImWord) object).getLocalID() + "', '" + escapeForJavaScript(((ImWord) object).getString()) + "');");
					else if (ImWord.BOLD_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordBold('" + ((ImWord) object).getLocalID() + "', " + object.hasAttribute(ImWord.BOLD_ATTRIBUTE) + ");");
					else if (ImWord.ITALICS_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetWordItalics('" + ((ImWord) object).getLocalID() + "', " + object.hasAttribute(ImWord.ITALICS_ATTRIBUTE) + ");");
				}
				else if (object instanceof ImAnnotation) {
					if (ImAnnotation.FIRST_WORD_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetAnnotFirstWord('" + ((ImAnnotation) object).getType() + "', '" + ((ImWord) oldValue).getLocalID() + "', '" + ((ImAnnotation) object).getLastWord().getLocalID() + "', '" + ((ImAnnotation) object).getFirstWord().getLocalID() + "');");
					else if (ImAnnotation.LAST_WORD_ATTRIBUTE.equals(attributeName))
						addJavaScriptCall("uSetAnnotLastWord('" + ((ImAnnotation) object).getType() + "', '" + ((ImAnnotation) object).getFirstWord().getLocalID() + "', '" + ((ImWord) oldValue).getLocalID() + "', '" + ((ImAnnotation) object).getLastWord().getLocalID() + "');");
				}
				//	no other attributes used in HTML page (outside attribute editor dialog)
			}
			public void supplementChanged(String supplementId, ImSupplement oldValue) {
				//	nothing to relay about supplement modifications
			}
			public void regionAdded(final ImRegion region) {
				StringBuffer jsonRegion = new StringBuffer("{");
				jsonRegion.append("\"bounds\": {");
				jsonRegion.append("\"left\": " + region.bounds.left + ",");
				jsonRegion.append("\"right\": " + region.bounds.right + ",");
				jsonRegion.append("\"top\": " +region.bounds.top + ",");
				jsonRegion.append("\"bottom\": " + region.bounds.bottom + "");
				jsonRegion.append("},");
				if (region instanceof ImWord) {
					if (((ImWord) region).getPreviousWord() == null)
						jsonRegion.append("\"textStreamType\": \"" + ((ImWord) region).getTextStreamType() + "\",");
					else jsonRegion.append("\"prevWordId\": \"" + ((ImWord) region).getPreviousWord().getLocalID() + "\",");
					if (((ImWord) region).getNextWord() != null) {
						jsonRegion.append("\"nextWordId\": \"" + ((ImWord) region).getNextWord().getLocalID() + "\",");
						jsonRegion.append("\"nextRelation\": \"" + ((ImWord) region).getNextRelation() + "\",");
					}
					jsonRegion.append("\"str\": \"" + ((ImWord) region).getString() + "\"");
				}
				else jsonRegion.append("\"type\": \"" + region.getType() + "\"");
				jsonRegion.append("}");
				if (region instanceof ImWord)
					addJavaScriptCall("uAddWord(" + region.pageId + ", " + jsonRegion.toString() + ");");
				else addJavaScriptCall("uAddRegion(" + region.pageId + ", " + jsonRegion.toString() + ");");
			}
			public void regionRemoved(final ImRegion region) {
				if (region instanceof ImWord)
					addJavaScriptCall("uRemoveWord('" + ((ImWord) region).getLocalID() + "');");
				else addJavaScriptCall("uRemoveRegion('" + region.getType() + "', " + region.pageId + ", '" + region.bounds.toString() + "');");
			}
			public void annotationAdded(final ImAnnotation annotation) {
				StringBuffer jsonAnnot = new StringBuffer("{");
				jsonAnnot.append("\"type\": \"" + annotation.getType() + "\",");
				jsonAnnot.append("\"firstWordId\": \"" + annotation.getFirstWord().getLocalID() + "\",");
				jsonAnnot.append("\"lastWordId\": \"" + annotation.getLastWord().getLocalID() + "\"");
				jsonAnnot.append("}");
				addJavaScriptCall("uAddAnnot(" + jsonAnnot.toString() + ");");
			}
			public void annotationRemoved(final ImAnnotation annotation) {
				addJavaScriptCall("uRemoveAnnot('" + annotation.getType() + "', '" + annotation.getFirstWord().getLocalID() + "', '" + annotation.getLastWord().getLocalID() + "');");
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#setTextStreamTypeColor(java.lang.String, java.awt.Color)
		 */
		public void setTextStreamTypeColor(String type, Color color) {
			super.setTextStreamTypeColor(type, color);
			this.addJavaScriptCall("uSetTextStreamColor('" + type + "', '#" + GoldenGateImagine.getHex(color) + "');");
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#setLayoutObjectColor(java.lang.String, java.awt.Color)
		 */
		public void setLayoutObjectColor(String type, Color color) {
			super.setLayoutObjectColor(type, color);
			this.addJavaScriptCall("uSetRegionColor('" + type + "', '#" + GoldenGateImagine.getHex(color) + "');");
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#setAnnotationColor(java.lang.String, java.awt.Color)
		 */
		public void setAnnotationColor(String type, Color color) {
			super.setAnnotationColor(type, color);
			this.addJavaScriptCall("uSetAnnotColor('" + type + "', '#" + GoldenGateImagine.getHex(color) + "');");
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#setRegionsPainted(java.lang.String, boolean)
		 */
		public void setRegionsPainted(String type, boolean paint) {
			super.setRegionsPainted(type, paint);
			if (paint) {
				Color color = this.getLayoutObjectColor(type);
				if (color == null) {
					color = new Color(Color.HSBtoRGB(((float) Math.random()), 0.7f, 1.0f));
					this.setLayoutObjectColor(type, color);
				}
				this.addJavaScriptCall("uSetPaintRegions('" + type + "');");
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#setAnnotationsPainted(java.lang.String, boolean)
		 */
		public void setAnnotationsPainted(String type, boolean paint) {
			super.setAnnotationsPainted(type, paint);
			if (paint) {
				Color color = this.getAnnotationColor(type);
				if (color == null) {
					color = new Color(Color.HSBtoRGB(((float) Math.random()), 0.7f, 1.0f));
					this.setAnnotationColor(type, color);
				}
				this.addJavaScriptCall("uSetPaintAnnots('" + type + "');");
			}
		}
		
		boolean handleDrop(ImWord dropWord, Transferable droppedData) {
			for (int h = 0; h < this.dropHandlers.length; h++) {
				System.out.println("Testing drop handler " + dropHandlers[h].getPluginName());
				if (this.dropHandlers[h].handleDrop(this, dropWord.getPage(), dropWord.centerX, dropWord.centerY, droppedData)) {
					System.out.println(" ==> Accepted");
					return true;
				}
				else System.out.println(" ==> Rejected");
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#getActions(de.uka.ipd.idaho.im.ImWord, de.uka.ipd.idaho.im.ImWord)
		 */
		public SelectionAction[] getActions(final ImWord start, ImWord end) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getActions(start, end)));
//			if (start == end) // TODO_ne_temporarily remove this test function !!!
//				actions.add(new SelectionAction("editWordTest", "Edit Word (Test)", "Edit word (universally included for test purposes only)") {
//					public boolean performAction(ImDocumentMarkupPanel invoker) {
//						editWord(start);
//						return true;
//					}
//				});
			for (int p = 0; p < this.selectionActionProviders.length; p++) {
				SelectionAction[] sas = this.selectionActionProviders[p].getActions(start, end, this);
				if ((sas != null) && (sas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(sas));
				}
			}
			return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#getActions(de.uka.ipd.idaho.im.ImPage, java.awt.Point, java.awt.Point)
		 */
		public SelectionAction[] getActions(ImPage page, Point start, Point end) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getActions(page, start, end)));
			for (int p = 0; p < this.selectionActionProviders.length; p++) {
				SelectionAction[] sas = this.selectionActionProviders[p].getActions(start, end, page, this);
				if ((sas != null) && (sas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(sas));
				}
			}
			return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
		}
		
		public void atomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ProgressMonitor pm) {
			if ("UNDO".equals(label))
				return;
			startMultipartUndoAction(label);
			//	TODO_not_(we're collecting everything) start collecting JavaScript calls to make changes visible in web page
		}
		public void atomicActionFinishing(long id, ProgressMonitor pm) { /* no follow-up actions here */ }
		public void atomicActionFinished(long id, ProgressMonitor pm) {
			finishMultipartUndoAction();
			//	TODO_not_(we're collecting everything)  finish collecting JavaScript calls to make changes visible in web page
		}
//		
//		/* (non-Javadoc)
//		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#beginAtomicAction(java.lang.String)
//		 */
//		public void beginAtomicAction(String label) {
//			startMultipartUndoAction(label);
//			//	TODO_not_(we're collecting everything) start collecting JavaScript calls to make changes visible in web page
//		}
//		
//		/* (non-Javadoc)
//		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#endAtomicAction()
//		 */
//		public void endAtomicAction() {
//			finishMultipartUndoAction();
//			//	TODO_not_(we're collecting everything)  finish collecting JavaScript calls to make changes visible in web page
//		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#paint(java.awt.Graphics)
		 */
		public void paint(Graphics graphics) {
			//	we're not painting, as instances of this class are never displayed
		}
		
		/* (non-Javadoc)
		 * @see java.awt.Component#repaint()
		 */
		public void repaint() {
			//	we're not repainting, as instances of this class are never displayed
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#validate()
		 */
		public void validate() {
			//	we're not validating, as instances of this class are never displayed
		}
		
		public void applyMarkupTool(ImageMarkupTool imt, ImAnnotation annot) {
			this.applyMarkupTool(imt, annot, ProgressMonitor.dummy);
		}
		
		void applyMarkupTool(ImageMarkupTool imt, ImAnnotation annot, ProgressMonitor pm) {
			
			//	lock reaction triggers
			this.imToolActive = true;
			
			//	initialize atomic UNDO
			this.startAtomicAction(("Apply " + imt.getLabel()), imt, annot, pm);
			
			//	listen for newly added annotation and region types
			ImDocumentListener idl = null;
			try {
				
				//	count what is added and removed
				final CountingSet regionCss = new CountingSet();
				final CountingSet annotCss = new CountingSet();
				
				//	listen for annotations being added, but do not update display control for every change
				idl = new ImDocumentListener() {
					public void typeChanged(ImObject object, String oldType) {
						if (object instanceof ImAnnotation) {
							annotCss.remove(oldType);
							annotCss.add(object.getType());
						}
						else if (object instanceof ImRegion) {
							regionCss.remove(oldType);
							regionCss.add(object.getType());
						}
					}
					public void attributeChanged(ImObject object, String attributeName, Object oldValue) {}
					public void supplementChanged(String supplementId, ImSupplement oldValue) {}
					public void regionAdded(ImRegion region) {
						regionCss.add(region.getType());
					}
					public void regionRemoved(ImRegion region) {
						regionCss.remove(region.getType());
					}
					public void annotationAdded(ImAnnotation annotation) {
						annotCss.add(annotation.getType());
					}
					public void annotationRemoved(ImAnnotation annotation) {
						annotCss.remove(annotation.getType());
					}
				};
				this.document.addDocumentListener(idl);
				
				//	apply image markup tool
				imt.process(this.document, annot, this, pm);
				
				//	make sure newly added objects are visible
				for (Iterator rtit = regionCss.iterator(); rtit.hasNext();)
					setRegionsPainted(((String) rtit.next()), true);
				for (Iterator atit = annotCss.iterator(); atit.hasNext();)
					setAnnotationsPainted(((String) atit.next()), true);
			}
			
			//	catch whatever might happen
			catch (Throwable t) {
				t.printStackTrace(System.out);
			}
			
			//	clean up
			finally {
				
				//	stop listening
				if (idl != null)
					this.document.removeDocumentListener(idl);
				
				//	finish atomic UNDO
				this.finishAtomicAction(pm);
				
				//	unlock reaction triggers
				this.imToolActive = false;
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#editWord(de.uka.ipd.idaho.im.ImWord, boolean)
		 */
		public boolean editWord(ImWord word, boolean waitForResult) {
			if (waitForResult) {
				Thread ct = Thread.currentThread();
				if (ct instanceof ActionThread)
					return ((ActionThread) ct).editWord(word);
				else throw new RuntimeException("Please modally edit words from Action Thread !!!");
			}
			else {
				this.addJavaScriptCall("doEditWord('" + word.getLocalID() + "');");
				return false;
			}
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#editAttributes(de.uka.ipd.idaho.gamta.Attributed, java.lang.String, java.lang.String)
		 */
		public void editAttributes(Attributed attributed, String type, String value) {
			String attributedType = null;
			String attributedId = null;
			if (attributed instanceof ImWord) {
				attributedType = "W";
				attributedId = ((ImWord) attributed).getLocalID();
			}
			else if (attributed instanceof ImPage) {
				attributedType = "P";
				attributedId = ("" + ((ImPage) attributed).pageId);
			}
			else if (attributed instanceof ImRegion) {
				attributedType = "R";
				attributedId = (((ImRegion) attributed).getType() + "@" + ((ImRegion) attributed).pageId + "." + ((ImRegion) attributed).bounds.toString());
			}
			else if (attributed instanceof ImAnnotation) {
				attributedType = "A";
				attributedId = (((ImAnnotation) attributed).getType() + "@" + ((ImAnnotation) attributed).getFirstWord().getLocalID() + "-" + ((ImAnnotation) attributed).getLastWord().getLocalID());
			}
			else if (attributed instanceof ImDocument) {
				attributedType = "D";
				attributedId = (((ImDocument) attributed).docId);
			}
			if ((attributedType != null) && (attributedId != null))
				this.addJavaScriptCall("doEditAttributes('" + attributedType + ":" + attributedId + "');");
		}
	}
}