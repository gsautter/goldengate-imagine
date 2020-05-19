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
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.easyIO.web.FormDataReceiver;
import de.uka.ipd.idaho.gamta.Gamta;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.SgmlDocumentReader;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.feedback.html.AsynchronousRequestHandler;
import de.uka.ipd.idaho.gamta.util.feedback.html.AsynchronousRequestHandler.AsynchronousRequest;
import de.uka.ipd.idaho.gamta.util.feedback.html.renderers.BufferedLineWriter;
import de.uka.ipd.idaho.gamta.util.imaging.PageImage;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageInputStream;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageStore;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageStore.AbstractPageImageStore;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImSupplement;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.util.ImSupplementCache;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefConstants;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefEditorFormHandler;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefTypeSystem;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData;
import de.uka.ipd.idaho.plugins.bibRefs.refBank.RefBankClient;
import de.uka.ipd.idaho.plugins.bibRefs.refBank.RefBankClient.BibRef;
import de.uka.ipd.idaho.plugins.bibRefs.refBank.RefBankClient.BibRefIterator;

/**
 * This servlet provides the facilities to get PDF documents into the browser
 * based version of GoldenGATE Imagine.
 * 
 * @author sautter
 */
public class GoldenGateImagineUploadServlet extends GoldenGateImagineServlet implements LiteratureConstants, BibRefConstants {
	private static final int defaultMaxUploadFileSize = (10 * 1024 * 1024); // 10MB for starters
	private static final int defaultMaxInMemorySupplementSize = (50 * 1024 * 1024); // 50MB
	
	private BibRefTypeSystem refTypeSystem = BibRefTypeSystem.getDefaultInstance();
	private String[] refIdTypes = {};
	private RefBankClient refBankClient;
	
	private PdfExtractor pdfExtractor;
	
	private int maxUploadFileSize = defaultMaxUploadFileSize;
	private int maxInMemorySupplementSize = defaultMaxInMemorySupplementSize;
	
	private UploadRequestHandler uploadHandler;
	private File uploadCacheFolder;
	
	private static class UploadFileFormat {
		final String name;
		final String label;
		UploadFileFormat(String name, String label) {
			this.name = name;
			this.label = label;
		}
	}
	private UploadFileFormat[] uploadFileFormats = {
		new UploadFileFormat("BD", "Born-Digital PDF Documents"),
		new UploadFileFormat("S", "Scanned PDF Documents"),
		new UploadFileFormat("SM", "Scanned PDF Documents with Born-Digital Meta Pages"),
		new UploadFileFormat("G", "Generic PDF Documents (Scanned or Born-Digital)"),
	};
	
	/** the usual zero-argument constructor */
	public GoldenGateImagineUploadServlet() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		super.reInit();
		
		//	connect to RefBank
		String refBankUrl = this.getSetting("refBankUrl");
		if (refBankUrl != null)
			this.refBankClient = new RefBankClient(refBankUrl);
		
		//	get publication ID types
		LinkedHashSet refIdTypes = new LinkedHashSet();
		refIdTypes.addAll(Arrays.asList((" " + this.getSetting("refIdTypes", "DOI Handle ISBN ISSN")).split("\\s+")));
		this.refIdTypes = ((String[]) refIdTypes.toArray(new String[refIdTypes.size()]));
		
		//	TODO_later load custom reference type system if configured
		this.refTypeSystem = BibRefTypeSystem.getDefaultInstance();
		
		//	read maximum upload file size
		String mufsStr = this.getSetting("maxUploadFileSize", ("" + this.maxUploadFileSize)).toUpperCase();
		int mufsFactor = 1;
		if (mufsStr.endsWith("K")) {
			mufsFactor = 1024;
			mufsStr = mufsStr.substring(0, (mufsStr.length() - "K".length())).trim();
		}
		else if (mufsStr.endsWith("M")) {
			mufsFactor = (1024 * 1024);
			mufsStr = mufsStr.substring(0, (mufsStr.length() - "M".length())).trim();
		}
		try {
			this.maxUploadFileSize = (Integer.parseInt(mufsStr) * mufsFactor);
		} catch (NumberFormatException nfe) {}
		
		//	read maximum supplement size before caching to disc
		String mimssStr = this.getSetting("maxInMemorySupplementSize", ("" + this.maxInMemorySupplementSize)).toUpperCase();
		int mimssFactor = 1;
		if (mimssStr.endsWith("K")) {
			mimssFactor = 1024;
			mimssStr = mimssStr.substring(0, (mimssStr.length() - "K".length())).trim();
		}
		else if (mimssStr.endsWith("M")) {
			mimssFactor = (1024 * 1024);
			mimssStr = mimssStr.substring(0, (mimssStr.length() - "M".length())).trim();
		}
		try {
			this.maxInMemorySupplementSize = (Integer.parseInt(mimssStr) * mimssFactor);
		} catch (NumberFormatException nfe) {}
		
		//	create upload handler
		this.uploadHandler = new UploadRequestHandler();
		
		//	create cache folder
		this.uploadCacheFolder = new File(this.cacheRootPath, "upload");
		
		//	create page image store to put page images in shared cache path
		final File pageImageFolder = new File(this.cacheRootPath, "pageImages");
		if (!pageImageFolder.exists())
			pageImageFolder.mkdirs();
		PageImageStore pageImageStore = new AbstractPageImageStore() {
			public boolean isPageImageAvailable(String name) {
				if (!name.endsWith(IMAGE_FORMAT))
					name += ("." + IMAGE_FORMAT);
				File pif = new File(pageImageFolder, name);
				return pif.exists();
			}
			public PageImageInputStream getPageImageAsStream(String name) throws IOException {
				if (!name.endsWith(IMAGE_FORMAT))
					name += ("." + IMAGE_FORMAT);
				File pif = new File(pageImageFolder, name);
				if (pif.exists())
					return new PageImageInputStream(new BufferedInputStream(new FileInputStream(pif)), this);
				else return null;
			}
			public boolean storePageImage(String name, PageImage pageImage) throws IOException {
				if (!name.endsWith(IMAGE_FORMAT))
					name += ("." + IMAGE_FORMAT);
				try {
					File pif = new File(pageImageFolder, name);
					if (pif.exists()) {
						String pifName = pif.getAbsolutePath();
						pif.renameTo(new File(pifName + "." + System.currentTimeMillis() + ".old"));
						pif = new File(pifName);
					}
					OutputStream imageOut = new BufferedOutputStream(new FileOutputStream(pif));
					pageImage.write(imageOut);
					imageOut.close();
					return true;
				}
				catch (IOException ioe) {
					ioe.printStackTrace(System.out);
					return false;
				}
			}
			public int getPriority() {
				return 0; // we're a general page image store, yield to more specific ones
			}
		};
		PageImage.addPageImageSource(pageImageStore);
		
		//	create PDF reader caching supplements on disc
		final File supplementCacheFolder = new File(this.cacheRootPath, "supplements");
		if (!supplementCacheFolder.exists())
			supplementCacheFolder.mkdirs();
		this.pdfExtractor = new PdfExtractor(this.ggImagineRootPath, pageImageStore, true) {
			protected ImDocument createDocument(String docId) {
				return new DiscCachingImDocument(docId, supplementCacheFolder);
			}
		};
	}
	
	/**
	 * This custom implementation of an Image Markup document caches the binary
	 * content of supplements on disc if their combined size exceeds the
	 * configures threshold. This behavior is restricted to supplements of
	 * built-in types, namely Source, Scan, and Figure.
	 * 
	 * @author sautter
	 */
	private class DiscCachingImDocument extends ImDocument {
		private ImSupplementCache supplementCache;
		DiscCachingImDocument(String docId, File supplementCacheFolder) {
			super(docId);
			this.supplementCache = new ImSupplementCache(this, supplementCacheFolder, maxInMemorySupplementSize);
		}
		public ImSupplement addSupplement(ImSupplement ims) {
			ims = this.supplementCache.cacheSupplement(ims);
			return super.addSupplement(ims);
		}
		public void removeSupplement(ImSupplement ims) {
			this.supplementCache.deleteSupplement(ims);
			super.removeSupplement(ims);
		}
		public void dispose() {
			this.supplementCache.clear();
			super.dispose();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	catch requests directed at authentication provider
		if (this.webAppHost.handleRequest(request, response))
			return;
		
		//	check authentication
		if (!this.webAppHost.isAuthenticated(request)) {
			StringBuffer lfu = new StringBuffer(request.getContextPath() + request.getServletPath());
			if (request.getPathInfo() != null)
				lfu.append(request.getPathInfo());
			if (request.getQueryString() != null)
				lfu.append("?" + request.getQueryString());
			this.sendHtmlPage(this.webAppHost.getLoginPageBuilder(this, request, response, "includeBody", lfu.toString()));
			return;
		}
		
		//	catch requests directed at upload handler
		if (this.uploadHandler.handleRequest(request, response))
			return;
		
		//	check type of request
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			this.sendUploadForm(request, response);
			return;
		}
		
		//	request for status page of pending upload
		if (pathInfo.startsWith("/status/")) {
			String requestId = pathInfo.substring("/status/".length());
			this.uploadHandler.sendStatusDisplayFrame(request, requestId, response);
			return;
		}
		
		//	request for upload result page
		if (pathInfo.startsWith("/finished/")) {
			String docId = pathInfo.substring("/finished/".length());
			String[] jscs = {
				//	open newly uploaded document for editing in new tag
				("window.parent.open('" + request.getContextPath() + "/edit?docId=" + docId + "', '_blank', '', true);"),
				//	refresh backing document list
				"window.parent.loaction.reload();",
			};
			this.sendPopupHtmlPage(this.getClosePopinWindowPageBuilder(request, response, jscs));
			return;
		}
		
		//	handle reference search request
		if (pathInfo.equals("/searchRefs.js")) {
			
			//	read request and perform search
			RefData query = new RefData();
			for (Enumeration pne = request.getParameterNames(); pne.hasMoreElements();) {
				String pn = ((String) pne.nextElement());
				if (!"time".equals(pn) && !"status".equals(pn))
					query.setAttribute(pn, request.getParameter(pn));
			}
			Vector refs = this.searchRefData(query);
			
			//	send JavaScript calls up to this point
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			if (refs.size() == 0)
				blw.writeLine("searchRefData_emptyResult();");
			else for (int r = 0; r < refs.size(); r++) {
				RefData ref = ((RefData) refs.get(r));
				blw.writeLine("var srRef = new Object();");
				BibRefEditorFormHandler.writeRefDataAsJavaScriptObject(blw, ref, "srRef");
				blw.writeLine("srRef['displayString'] = '" + BibRefEditorFormHandler.escapeForJavaScript(BibRefUtils.toRefString(ref)) + "';");
				blw.writeLine("searchRefData_showResult(srRef);");
			}
			blw.flush();
			out.flush();
			blw.close();
		}
	}
	
	private Vector searchRefData(RefData query) {
		
		//	can we search?
		if (this.refBankClient == null)
			return null;
		
		//	get search data
		String author = query.getAttribute(AUTHOR_ANNOTATION_TYPE);
		String title = query.getAttribute(TITLE_ANNOTATION_TYPE);
		String origin = this.refTypeSystem.getOrigin(query);
		String year = query.getAttribute(YEAR_ANNOTATION_TYPE);
		
		//	test year
		if (year != null) try {
			Integer.parseInt(year);
		}
		catch (NumberFormatException nfe) {
			year = null;
		}
		
		//	get identifiers
		String[] extIdTypes = query.getIdentifierTypes();
		String extIdType = (((extIdTypes == null) || (extIdTypes.length == 0)) ? null : extIdTypes[0]);
		String extId = ((extIdType == null) ? null : query.getIdentifier(extIdType));
		
		//	got something to search for?
		if ((extId == null) && (author == null) && (title == null) && (year == null) && (origin == null))
			return null;
		
		//	perform search
		Vector refs = new Vector();
		try {
			BibRefIterator brit = this.refBankClient.findRefs(null, author, title, ((year == null) ? -1 : Integer.parseInt(year)), origin, extId, extIdType, 0, false);
			while (brit.hasNextRef()) {
				BibRef ps = brit.getNextRef();
				String rs = ps.getRefParsed();
				if (rs == null)
					continue;
				try {
					refs.add(BibRefUtils.modsXmlToRefData(SgmlDocumentReader.readDocument(new StringReader(rs))));
				} catch (IOException ioe) { /* never gonna happen, but Java don't know ... */ }
			}
		} catch (IOException ioe) { /* let's not bother with exceptions for now, just return null ... */ }
		
		//	finally ...
		return refs;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	catch requests directed at authentication provider
		if (this.webAppHost.handleRequest(request, response))
			return;
		
		//	check authentication
		if (!this.webAppHost.isAuthenticated(request)) {
			StringBuffer lfu = new StringBuffer(request.getContextPath() + request.getServletPath());
			if (request.getPathInfo() != null)
				lfu.append(request.getPathInfo());
			if (request.getQueryString() != null)
				lfu.append("?" + request.getQueryString());
			this.sendHtmlPage(this.webAppHost.getLoginPageBuilder(this, request, response, "includeBody", lfu.toString()));
			return;
		}
		
		//	catch requests directed at upload handler
		if (this.uploadHandler.handleRequest(request, response))
			return;
		
		//	get user name
		String userName = this.webAppHost.getUserName(request);
		
		//	check type of request
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		//	get upload ID and check validity
		if (!pathInfo.startsWith("/upload/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String uploadId = pathInfo.substring("/upload/".length());
		if (!this.validUploadIDs.contains(uploadId)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			
			//	accept document upload
			HashSet fileFieldSet = new HashSet(2);
			fileFieldSet.add("uploadFile");
			FormDataReceiver uploadData = FormDataReceiver.receive(request, defaultMaxUploadFileSize, this.uploadCacheFolder, 1024, fileFieldSet);
			System.out.println("MarkupWizardServlet: document upload data encoding is " + uploadData.getContentType());
			
			//	check upload data and build title for importer job
			String uploadUrlString = uploadData.getFieldValue("uploadUrl");
			String uploadTitle;
			if ((uploadUrlString != null) && (uploadUrlString.trim().length() != 0))
				uploadTitle = ("Importing " + uploadUrlString);
			else if (uploadData.hasField("uploadFile"))
				uploadTitle = ("Importing " + uploadData.getSourceFileName("uploadFile"));
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			//	start asynchronous decoding
			UploadRequest ur = new UploadRequest(uploadId, uploadTitle, userName, uploadData);
			this.uploadHandler.enqueueRequest(ur, userName);
			
			/*
			 * we have to redirect to the status page instead of sending it
			 * directly in order to get back to GET, as reloading on POST tends
			 * to cause trouble like re-sending data, respective browser
			 * prompts, etc.
			 */
			response.sendRedirect(request.getContextPath() + request.getServletPath() + "/status/" + uploadId);
			
			//	we're done with this one ...
			return;
		}
		
		//	invalidate upload ID in any case
		finally {
			this.validUploadIDs.remove(uploadId);
		}
	}
	
	private void sendUploadForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	generate upload ID
		final String uploadId = Gamta.getAnnotationID();
		this.validUploadIDs.add(uploadId);
		
		//	send upload form
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.sendPopupHtmlPage(new HtmlPageBuilder(this, request, response) {
			protected boolean includeJavaScriptDomHelpers() {
				return true;
			}
			protected void include(String type, String tag) throws IOException {
				if ("includeBody".equals(type)) {
					
					//	start form
					this.writeLine("<form" +
							" id=\"uploadForm\"" +
							" method=\"POST\"" +
							" action=\"" + this.request.getContextPath() + this.request.getServletPath() + "/upload/" + uploadId + "\"" +
							" onsubmit=\"return checkUploadData();\"" +
						">");
					
					//	add fields
					this.writeLine("<table class=\"uploadTable\">");
					this.writeLine("<tr>");
					this.writeLine("<td class=\"uploadTableHeader\">");
					this.writeLine("Upload a New Document");
					this.writeLine("</td>");
					this.writeLine("</tr>");
					this.writeLine("</table>");
					
					this.writeLine("<div id=\"uploadRefDataFields\">");
					BibRefEditorFormHandler.createHtmlForm(this.asWriter(), false, refTypeSystem, refIdTypes);
					this.writeLine("</div>");
					
					this.writeLine("<div id=\"uploadDataFields\">");
					this.writeLine("<table class=\"bibRefEditorTable\">");
					this.writeLine("<tr>");
					this.writeLine("<td class=\"uploadFieldLabel\" style=\"text-align: right;\">Document URL:</td>");
					this.writeLine("<td colspan=\"3\" class=\"uploadFieldCell\"><input class=\"uploadField\" style=\"width: 100%;\" id=\"uploadUrl_field\" name=\"uploadUrl\"></td>");
					this.writeLine("</tr>");
					this.writeLine("<tr>");
					this.writeLine("<td class=\"uploadFieldLabel\" style=\"text-align: right;\">Document File:</td>");
					this.writeLine("<td colspan=\"3\" class=\"uploadFieldCell\"><input type=\"file\" class=\"uploadField\" style=\"width: 100%;\" id=\"uploadFile_field\" name=\"uploadFile\" onchange=\"uploadFileChanged();\"></td>");
					this.writeLine("</tr>");
					
					this.writeLine("<tr>");
					this.writeLine("<td class=\"uploadFieldLabel\" style=\"text-align: right;\">Document Format:</td>");
					this.writeLine("<td colspan=\"3\" class=\"uploadFieldCell\">");
					this.writeLine("<select class=\"uploadField\" style=\"width: 100%;\" id=\"uploadFileFormat_field\" name=\"uploadFileFormat\">");
					for (int f = 0; f < uploadFileFormats.length; f++)
						this.writeLine("<option value=\"" + uploadFileFormats[f].name + "\">" + uploadFileFormats[f].label + "</option>");
					this.writeLine("</select>");
					this.writeLine("</td>");
					this.writeLine("</tr>");
					
					this.writeLine("</table>");
					this.writeLine("</div>");
					
					this.writeLine("<div id=\"uploadButtons\">");
					this.writeLine("<input type=\"button\" class=\"uploadButton\" id=\"searchRefs_button\" value=\"Search References\" onclick=\"searchRefs();\">");
					this.writeLine("<input type=\"button\" class=\"uploadButton\" id=\"checkRef_button\" value=\"Check Reference\" onclick=\"validateRefData();\">");
					this.writeLine("<input type=\"submit\" class=\"uploadButton\" id=\"doUpload_button\" value=\"Import Document\">");
					this.writeLine("</div>");
					
					this.writeLine("</form>");
					this.writeLine("<script id=\"dynamicSearchScript\" type=\"text/javascript\" src=\"toBeSetDynamically\"></script>");
				}
				else super.include(type, tag);
			}
			protected void writePageHeadExtensions() throws IOException {
				BibRefEditorFormHandler.writeJavaScripts(this.asWriter(), refTypeSystem, refIdTypes);
				this.writeLine("<script type=\"text/javascript\">");
				
				//	add validation function
				this.writeLine("function validateRefData() {");
				this.writeLine("  var errors = bibRefEditor_getRefErrors();");
				this.writeLine("  var message = '';");
				this.writeLine("  if (errors == null)");
				this.writeLine("    message = 'The bibliographic metadata is valid.';");
				this.writeLine("  else {");
				this.writeLine("    message = 'The bibliographic metadata has errors:';");
				this.writeLine("    for (var e = 0;; e++) {");
				this.writeLine("      if (!errors['' + e])");
				this.writeLine("        break;");
				this.writeLine("      message += '\\r\\n';");
				this.writeLine("      message += ('- ' + errors['' + e]);");
				this.writeLine("    }");
				this.writeLine("  }");
				this.writeLine("  alert(message);");
				this.writeLine("}");
				
				//	add search functions
				this.writeLine("var searchRefDataOverlay = null;");
				this.writeLine("var searchRefDataDialog = null;");
				this.writeLine("var searchRefDataLabel = null;");
				this.writeLine("var searchRefDataResultList = null;");
				this.writeLine("function searchRefData() {");
				this.writeLine("  var ref = bibRefEditor_getRef();");
				this.writeLine("  var params = '';");
				this.writeLine("  for (var an in ref)");
				this.writeLine("    params += ('&' + an + '=' + encodeURIComponent(ref[an]));");
				this.writeLine("  if (params == '') {");
				this.writeLine("    alert('Please specify one or more attributes as search values.\\r\\nA part of the title or last name of a author will do,\\r\\nas will the journal name and year, or one of the identifiers.');");
				this.writeLine("    return;");
				this.writeLine("  }");
				//	TODO put styles in template page
				this.writeLine("  var searchRefDataTitle = newElement('div', null, 'bibDataSearchTitle', 'Search Bibliographic Data');");
				this.writeLine("  searchRefDataLabel = newElement('div', null, 'bibDataSearchLabel', 'Searching ...');");
				this.writeLine("  searchRefDataResultList = newElement('div', null, 'bibDataSearchResultList', null);");
				this.writeLine("  setAttribute(searchRefDataDialog, 'style', 'height: 300px; cursor: default; overflow: auto;');");
				this.writeLine("  searchRefDataResultList.appendChild(searchRefDataLabel);");
				this.writeLine("  var searchRefDataCancel = newElement('button', null, 'bibDataSearchCancel', 'Cancel');");
				this.writeLine("  searchRefDataCancel.onclick = function() {");
				this.writeLine("    removeElement(searchRefDataOverlay);");
				this.writeLine("    searchRefDataOverlay = null;");
				this.writeLine("    searchRefDataDialog = null;");
				this.writeLine("    searchRefDataLabel = null;");
				this.writeLine("    searchRefDataResultList = null;");
				this.writeLine("  };");
				this.writeLine("  var searchRefDataButtons = newElement('div', null, 'bibDataSearchButtons', null);");
				this.writeLine("  searchRefDataButtons.appendChild(searchRefDataCancel);");
				this.writeLine("  searchRefDataDialog = newElement('div', null, 'bibDataSearchResultDialog', null);");
				this.writeLine("  setAttribute(searchRefDataDialog, 'style', 'position: fixed; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(255, 255, 255, 0.75); cursor: default; overflow: auto;');");
				this.writeLine("  searchRefDataDialog.appendChild(searchRefDataTitle);");
				this.writeLine("  searchRefDataDialog.appendChild(searchRefDataResultList);");
				this.writeLine("  searchRefDataDialog.appendChild(searchRefDataButtons);");
				this.writeLine("  searchRefDataOverlay = getOverlay('searchRefDataOverlay', null, true);");
				this.writeLine("  searchRefDataOverlay.appendChild(searchRefDataDialog);");
				this.writeLine("  var dss = getById('dynamicSearchScript');");
				this.writeLine("  var dssp = dss.parentNode;");
				this.writeLine("  removeElement(dss);");
				this.writeLine("  var dssSrc = ('" + this.request.getContextPath() + this.request.getServletPath() + "/searchRefs.js?time=' + (new Date()).getTime() + params);");
				this.writeLine("  dss = newElement('script', 'dynamicSearchScript');");
				this.writeLine("  dss.type = 'text/javascript';");
				this.writeLine("  dss.src = dssSrc;");
				this.writeLine("  dssp.appendChild(dss);");
				this.writeLine("}");
				this.writeLine("function searchRefData_emptyResult() {");
				this.writeLine("  while (searchRefDataLabel.firstChild)");
				this.writeLine("    searchRefDataLabel.removeChild(searchRefDataLabel.firstChild);");
				this.writeLine("  searchRefDataLabel.appendChild(document.createTextNode('Your search did not return any results, sorry.'));");
				this.writeLine("}");
				this.writeLine("function searchRefData_showResult(ref) {");
				this.writeLine("  if (searchRefDataLabel != null) {");
				this.writeLine("    removeElement(searchRefDataLabel);");
				this.writeLine("    searchRefDataLabel = null;");
				this.writeLine("  }");
				this.writeLine("  var refDiv = newElement('div', null, 'bibDataSearchResult', ref.displayString);");
				this.writeLine("  refDiv.onclick = function() {");
				this.writeLine("    bibRefEditor_setRef(ref);");
				this.writeLine("    removeElement(searchRefDataOverlay);");
				this.writeLine("    searchRefDataOverlay = null;");
				this.writeLine("    searchRefDataDialog = null;");
				this.writeLine("    searchRefDataLabel = null;");
				this.writeLine("    searchRefDataResultList = null;");
				this.writeLine("  };");
				this.writeLine("  searchRefDataResultList.appendChild(refDiv);");
				this.writeLine("}");
				
				//	write JavaScripts used by document upload form
				this.writeLine("function uploadFileChanged() {");
				this.writeLine("  var uff = $('uploadFile_field');");
				this.writeLine("  if ((uff.value == null) || (uff.value.length == 0))");
				this.writeLine("    return;");
				this.writeLine("  var uuf = $('uploadUrl_field');");
				this.writeLine("  uuf.value = '';");
				this.writeLine("}");
				this.writeLine("function checkUploadData() {");
				this.writeLine("  var refErrors = bibRefEditor_getRefErrors();");
				this.writeLine("  if (refErrors != null) {");
				this.writeLine("    var em = 'The import cannot be processed due to incomplete meta data:';");
				this.writeLine("    for (var e = 0;; e++) {");
				this.writeLine("      var refError = refErrors[e];");
				this.writeLine("      if (refError == null)");
				this.writeLine("        break;");
				this.writeLine("      em += '\\n - ';");
				this.writeLine("      em += refError;");
				this.writeLine("    }");
				this.writeLine("    alert(em);");
				this.writeLine("    return false;");
				this.writeLine("  }");
				this.writeLine("  var uf = $('uploadForm');");
				this.writeLine("  if (uf == null)");
				this.writeLine("    return false;");
				this.writeLine("  var uuf = $('uploadUrl_field');");
				this.writeLine("  var uploadUrl = ((uuf == null) ? '' : uuf.value);");
				this.writeLine("  if (uploadUrl != '') {");
				this.writeLine("    uf.enctype = 'application/x-www-form-urlencoded';");
				this.writeLine("    return true;");
				this.writeLine("  }");
				this.writeLine("  var uff = $('uploadFile_field');");
				this.writeLine("  var uploadFile = ((uff == null) ? '' : uff.value);");
				this.writeLine("  if (uploadFile != '') {");
				this.writeLine("    uf.enctype = 'multipart/form-data';");
				this.writeLine("    return true;");
				this.writeLine("  }");
				this.writeLine("  alert('Please specify a URL to retrieve the document from, or select a file from you computer to upload.');");
				this.writeLine("  return false;");
				this.writeLine("}");
				
				this.writeLine("</script>");
			}
		});
	}
	private Set validUploadIDs = Collections.synchronizedSet(new HashSet());
	
	private class UploadRequestHandler extends AsynchronousRequestHandler {
		UploadRequestHandler() {
			super(false);
		}
		public AsynchronousRequest buildAsynchronousRequest(HttpServletRequest request) throws IOException {
			return null; // we're creating the requests ourselves
		}
		protected boolean retainAsynchronousRequest(AsynchronousRequest ar, int finishedArCount) {
			
			/* client not yet notified that request is complete, we have to hold
			 * on to this one, unless last status update was more than 10 minutes
			 * ago, which indicates the client side is likely dead */
			if (!ar.isFinishedStatusSent())
				return (System.currentTimeMillis() < (ar.getLastAccessTime() + (1000 * 60 * 10)));
			
			/* client has not yet retrieved result, we have to hold on to this
			 * one, unless last status update was more than 10 minutes ago,
			 * which indicates the client side is likely dead */
			if (!ar.isResultSent())
				return (System.currentTimeMillis() < (ar.getLastAccessTime() + (1000 * 60 * 10)));
			
			//	no need to retain any requests after client notified that it's finished, as document is stored by now
			return false;
		}
		protected HtmlPageBuilderHost getPageBuilderHost() {
			return GoldenGateImagineUploadServlet.this;
		}
		protected void sendHtmlPage(HtmlPageBuilder hpb) throws IOException {
			GoldenGateImagineUploadServlet.this.sendHtmlPage(hpb);
		}
		protected void sendPopupHtmlPage(HtmlPageBuilder hpb) throws IOException {
			GoldenGateImagineUploadServlet.this.sendPopupHtmlPage(hpb);
		}
	}
	
	private class UploadRequest extends AsynchronousRequest implements ProgressMonitor {
		private String userName;
		private FormDataReceiver uploadData;
		private String docId;
		private ImDocument doc;
		UploadRequest(String id, String title, String userName, FormDataReceiver uploadData) {
			super(id, title);
			this.userName = userName;
			this.uploadData = uploadData;
		}
		private int baseProgress = 0;
		private int maxProgress = 100;
		public void setBaseProgress(int baseProgress) {
			this.baseProgress = baseProgress;
		}
		public void setMaxProgress(int maxProgress) {
			this.maxProgress = maxProgress;
		}
		public void setProgress(int progress) {
			this.setPercentFinished(this.baseProgress + (((this.maxProgress - this.baseProgress) * progress) / 100));
		}
		public void setStep(String step) {
			this.setStatus(step);
		}
		public void setInfo(String info) {
			// let's ignore this one for now, we only have one status string
		}
		protected void process() throws Exception {
			
			//	prepare document import
			final InputStream docIn;
			final String docName;
			
			//	cache data from URL
			String uploadUrlString = this.uploadData.getFieldValue("uploadUrl");
			if ((uploadUrlString != null) && (uploadUrlString.trim().length() != 0)) {
				URL uploadUrl = new URL(uploadUrlString);
				docIn = new BufferedInputStream(uploadUrl.openStream());
				while (uploadUrlString.endsWith("/"))
					uploadUrlString = uploadUrlString.substring(0, (uploadUrlString.length() - 1));
				if (uploadUrlString.indexOf('/') == -1)
					docName = uploadUrlString;
				else docName = uploadUrlString.substring(uploadUrlString.lastIndexOf('/') + 1);
			}
			
			//	open uploaded file
			else {
				docIn = this.uploadData.getFieldByteStream("uploadFile");
				docName = this.uploadData.getSourceFileName("uploadFile");
			}
			
			//	load data into memory
			ByteArrayOutputStream docByteOut = new ByteArrayOutputStream();
			byte[] docBytes = new byte[2048];
			for (int r; (r = docIn.read(docBytes, 0, docBytes.length)) != -1;)
				docByteOut.write(docBytes, 0, r);
			docIn.close();
			docBytes = docByteOut.toByteArray();
			
			//	get file format
			String uploadFileFormat = this.uploadData.getFieldValue("uploadFileFormat");
			
			//	convert PDF and store document
			this.setBaseProgress(0);
			this.setMaxProgress(90);
			if ("BD".equals(uploadFileFormat))
				this.doc = pdfExtractor.loadTextPdf(docBytes, this);
			else if ("S".equals(uploadFileFormat))
				this.doc = pdfExtractor.loadImagePdf(docBytes, this);
			else if ("SM".equals(uploadFileFormat))
				this.doc = pdfExtractor.loadImagePdf(docBytes, true, this);
			else this.doc = pdfExtractor.loadGenericPdf(docBytes, this);
			this.doc.setAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, docName);
			this.docId = this.doc.docId;
			
			//	add document meta data
			RefData docRefData = this.getRefData();
			BibRefUtils.toModsAttributes(docRefData, doc);
			
			//	store document
			this.setStep("Storing document ...");
			this.setBaseProgress(90);
			this.setMaxProgress(100);
			storeDocument(this.doc, this.userName, this);
			this.setProgress(100);
			this.setStep("Document stored, finished.");
		}
		private RefData getRefData() throws IOException {
			RefData rd = new RefData();
			this.addRefDataAttribute(rd, AUTHOR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, YEAR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, TITLE_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, JOURNAL_NAME_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, PUBLISHER_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, LOCATION_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, EDITOR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, VOLUME_TITLE_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, PAGINATION_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, VOLUME_DESIGNATOR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, ISSUE_DESIGNATOR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, NUMERO_DESIGNATOR_ANNOTATION_TYPE);
			this.addRefDataAttribute(rd, PUBLICATION_URL_ANNOTATION_TYPE);
			if (!rd.hasAttribute(PUBLICATION_URL_ANNOTATION_TYPE) && this.uploadData.hasField("uploadUrl"))
				rd.setAttribute(PUBLICATION_URL_ANNOTATION_TYPE, this.uploadData.getFieldValue("uploadUrl"));
			if (!rd.hasAttribute(PUBLICATION_TYPE_ATTRIBUTE)) {
				String type = refTypeSystem.classify(rd);
				if (type != null)
					rd.setAttribute(PUBLICATION_TYPE_ATTRIBUTE, type);
			}
			for (int i = 0; i < refIdTypes.length; i++)
				this.addRefDataAttribute(rd, ("ID-" + refIdTypes[i]));
			return rd;
		}
		private void addRefDataAttribute(RefData rd, String attribute) throws IOException {
			String value = this.uploadData.getFieldValue(attribute);
			if (value == null)
				return;
			value = value.trim();
			if (value.length() == 0)
				return;
			if (AUTHOR_ANNOTATION_TYPE.equals(attribute) || EDITOR_ANNOTATION_TYPE.equals(attribute)) {
				String[] values = value.split("\\s*\\&\\s*");
				for (int v = 0; v < values.length; v++)
					rd.addAttribute(attribute, values[v]);
			}
			else rd.setAttribute(attribute, value);
		}
		protected void cleanup() throws Exception {
			this.uploadData.dispose();
			if (this.doc != null) {
				this.doc.dispose();
				this.doc = null;
			}
		}
		public boolean doImmediateResultForward() {
			return true;
		}
		public String getResultLink(HttpServletRequest request) {
			return (request.getContextPath() + request.getServletPath() + "/finished/" + this.docId);
		}
	}
	
	/**
	 * Store a document for a given user name. This default implementation
	 * relies on a <code>GoldenGateImagineDocumentServlet</code> to do this.
	 * Sub classes willing to store documents in other places may overwrite
	 * this method to do so.
	 * @param doc the document to store
	 * @param userName the name of the user to store the document for
	 * @param pm a progress monitor observing the storage process
	 * @return an array of string messages detailing on the storage process
	 * @throws IOException
	 */
	protected String[] storeDocument(ImDocument doc, String userName, ProgressMonitor pm) throws IOException {
		GoldenGateImagineDocumentServlet docHost = ((GoldenGateImagineDocumentServlet) this.webAppHost.getServlet("DocumentServlet"));
		if (docHost == null)
			throw new IOException("Cannot access document servlet");
		else return docHost.storeDocument(doc, userName, pm);
	}
}