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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.easyIO.EasyIO;
import de.uka.ipd.idaho.easyIO.IoProvider;
import de.uka.ipd.idaho.easyIO.SqlQueryResult;
import de.uka.ipd.idaho.easyIO.sql.TableDefinition;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.feedback.html.renderers.BufferedLineWriter;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineWebUtils.IsolatorOutputStream;
import de.uka.ipd.idaho.im.util.ImDocumentData.ImDocumentEntry;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefUtils.RefData;

/**
 * This servlet provides the document management facilities accompanying the
 * browser based version of GoldenGATE Imagine.
 * 
 * @author sautter
 */
public class GoldenGateImagineDocumentServlet extends GoldenGateImagineServlet implements LiteratureConstants {

	private static final DateFormat TIMESTAMP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final String DOCUMENT_TABLE_NAME = "GgImagineDocs";
	private static final String DOCUMENT_ID_COLUMN_NAME = "docId";
	private static final String DOCUMENT_ID_HASH_COLUMN_NAME = "docIdHash";
	private static final String USER_NAME_COLUMN_NAME = "userName";
	private static final String DOCUMENT_NAME_COLUMN_NAME = "docName";
	private static final String CREATE_TIME_COLUMN_NAME = "createTime";
	private static final String UPDATE_TIME_COLUMN_NAME = "updateTime";
	private static final String DOCUMENT_DESCRIPTION_COLUMN_NAME = "docDescription";
	private static final int DOCUMENT_DESCRIPTION_COLUMN_LENGTH = 332;
	
	private static class DocumentData {
		final String id;
		final String name;
		final String description;
		final String created;
		final String updated;
		DocumentData(String id, String name, String description, String created, String updated) {
			this.id = id;
			this.name = name;
			this.description = description;
			this.created = created;
			this.updated = updated;
		}
		String toJsonString() {
			StringBuffer json = new StringBuffer("{");
			json.append("\"id\": \"" + this.id + "\",");
			json.append("\"name\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.name) + "\",");
			json.append("\"description\": \"" + ((this.description == null) ? "" : GoldenGateImagineWebUtils.escapeForJavaScript(this.description)) + "\",");
			json.append("\"created\": \"" + this.created + "\",");
			json.append("\"updated\": \"" + this.updated + "\"");
			json.append("}");
			return json.toString();
		}
	}
	
	private IoProvider io;
	
	private File docRootPath;
	
	/** the usual zero-argument constructor */
	public GoldenGateImagineDocumentServlet() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		super.reInit();
		
		//	connect to database
		this.io = this.webAppHost.getIoProvider();
		if (!this.io.isJdbcAvailable())
			throw new ServletException("Cannot work without database access");
		
		//	get document storage root
		String docRootPath = this.getSetting("docStorageRootPath");
		if (docRootPath == null)
			this.docRootPath = new File(this.webInfFolder, "documents");
		else if (docRootPath.startsWith("./"))
			this.docRootPath = new File(this.webInfFolder, docRootPath.substring("./".length()));
		else this.docRootPath = new File(docRootPath);
		this.docRootPath.mkdirs();
		
		//	create document table (512 bytes per record)
		TableDefinition td = new TableDefinition(DOCUMENT_TABLE_NAME);
		td.addColumn(DOCUMENT_ID_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, 32);
		td.addColumn(DOCUMENT_ID_HASH_COLUMN_NAME, TableDefinition.INT_DATATYPE, 0);
		td.addColumn(USER_NAME_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, 64);
		td.addColumn(DOCUMENT_NAME_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, 64);
		td.addColumn(CREATE_TIME_COLUMN_NAME, TableDefinition.BIGINT_DATATYPE, 0);
		td.addColumn(UPDATE_TIME_COLUMN_NAME, TableDefinition.BIGINT_DATATYPE, 0);
		td.addColumn(DOCUMENT_DESCRIPTION_COLUMN_NAME, TableDefinition.VARCHAR_DATATYPE, DOCUMENT_DESCRIPTION_COLUMN_LENGTH);
		if (!this.io.ensureTable(td, true))
			return;
		
		//	index log table
		this.io.indexColumn(DOCUMENT_TABLE_NAME, DOCUMENT_ID_COLUMN_NAME);
		this.io.indexColumn(DOCUMENT_TABLE_NAME, DOCUMENT_ID_HASH_COLUMN_NAME);
		this.io.indexColumn(DOCUMENT_TABLE_NAME, USER_NAME_COLUMN_NAME);
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
		
		//	check type of request
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			this.sendDocumentListPage(request, response);
			return;
		}
		
		//	handle document list request
		if ("/listDocs.js".equals(pathInfo)) {
			this.sendDocumentList(request, response);
			return;
		}
		
		//	document download request
		if (pathInfo.startsWith("/download/")) {
			
			//	get document ID
			String docId = pathInfo.substring("/download/".length());
			
			//	get document name
			String docName = this.getDocumentName(request, docId);
			
			//	check if user has access
			if (docName == null) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			
			//	get document
			File docFolder = this.getDocFolder(docId, false);
			if (docFolder == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			ImDocument doc = ImDocumentIO.loadDocument(docFolder);
			if (doc == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			//	set headers
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", ("attachment; filename=" + docName + (docName.endsWith(".imf") ? "" : ".imf")));
			
			//	export document (making sure not to flush response stream prematurely)
			OutputStream out = response.getOutputStream();
			ImDocumentIO.storeDocument(doc, new BufferedOutputStream(new IsolatorOutputStream(out)), ProgressMonitor.dummy);
			out.flush();
			return;
		}
		
		//	handle document deletion
		if ("/delete.js".equals(pathInfo)) {
			
			//	get document ID
			String docId = pathInfo.substring("/download/".length());
			
			//	get document name
			String docName = this.getDocumentName(request, docId);
			
			//	check if user has access
			if (docName == null) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
			
			//	delete document TODO check if open for editing by this user, and send error if so
			boolean deleted = this.deleteDocument(request, docId);
			
			//	send back JavaScript ...
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
			BufferedLineWriter blw = new BufferedLineWriter(out);
			if (deleted) {
				blw.writeLine("removeDocument('" + docId + "');");
				blw.writeLine("alert('Document \\'" + docName + "\\' deleted successfully.');");
			}
			else blw.writeLine("alert('Could not delete document \\'" + docName + "\\'.');");
			blw.flush();
			out.flush();
			blw.close();
			return;
		}
		
		//	send document list
		this.sendDocumentListPage(request, response);
		return;
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
	}
	
	private void sendDocumentListPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	send page, generating loading scripts to end of body
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		this.sendHtmlPage(new HtmlPageBuilder(this, request, response) {
			protected boolean includeJavaScriptDomHelpers() {
				return true;
			}
			protected void include(String type, String tag) throws IOException {
				if ("includeBody".equals(type)) {
					
					//	add title DIV "My Documents" with 'Refresh' and 'Upload' buttons
					this.writeLine("<div class=\"documentListTitleBox\">");
					this.writeLine("<span class=\"documentListTitle\">My Documents</span>");
					this.writeLine("<button class=\"documentListTitleButton\" onclick=\"return refreshDocuments();\">Refresh</button>");
					this.writeLine("<button class=\"documentListTitleButton\" onclick=\"return uploadDocument();\">Upload</button>");
					this.writeLine("</div>");
					
					//	add document table (initially empty)
					this.writeLine("<table id=\"documentListTable\">");
					this.writeLine("</table>");
					
					//	trigger document list to be populated
					this.writeLine("<script type=\"text/javascript\">");
					this.writeLine("refreshDocuments();");
					this.writeLine("</script>");
					this.writeLine("<script id=\"dynamicScript\" type=\"text/javascript\" src=\"toBeSetDynamically\"></script>");
				}
				else super.include(type, tag);
			}
			protected void writePageHeadExtensions() throws IOException {
				writeJavaScriptTag(this, "docListHelpers.js", false);
				this.writeLine("<script type=\"text/javascript\">");
				
				//	call to delete.js
				this.writeLine("function downloadDocument(docId) {");
				this.writeLine("  window.open(('" + this.request.getContextPath() + this.request.getServletPath() + "/download/' + docId), 'downloadWindow', 'width=300,height=200,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes', true);");
				this.writeLine("}");
				
				//	open document for editing
				this.writeLine("function editDocument(docId) {");
				this.writeLine("  window.open(('" + this.request.getContextPath() + "/edit?docId=' + docId), '_blank', '', true);");
				this.writeLine("}");
				
				//	open upload form
				this.writeLine("function uploadDocument() {");
				this.writeLine("  window.open('" + this.request.getContextPath() + "/upload', 'uploadWindow', 'width=50,height=50,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes');");
				this.writeLine("}");
				
				//	perform some dynamic action via script tag replacement
				this.writeLine("function getDynamicScript(name, params) {");
				this.writeLine("  var das = getById('dynamicScript');");
				this.writeLine("  var dasp = das.parentNode;");
				this.writeLine("  removeElement(das);");
				this.writeLine("  var dasSrc = ('" + this.request.getContextPath() + this.request.getServletPath() + "/' + name + '?time=' + (new Date()).getTime() + params);");
				this.writeLine("  das = newElement('script', 'dynamicScript');");
				this.writeLine("  das.type = 'text/javascript';");
				this.writeLine("  das.src = dasSrc;");
				this.writeLine("  dasp.appendChild(das);");
				this.writeLine("  return false;");
				this.writeLine("}");
				
				//	call to delete.js
				this.writeLine("function deleteDocument(docId) {");
				this.writeLine("  return getDynamicScript('delete.js', ('&docId=' + docId));");
				this.writeLine("}");
				
				//	call to listDocs.js
				this.writeLine("function refreshDocuments() {");
				this.writeLine("  return getDynamicScript('listDocs.js', '');");
				this.writeLine("}");
				
				this.writeLine("</script>");
			}
		});
	}
	
	private void sendDocumentList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//	get user name
		String userName = this.webAppHost.getUserName(request);
		if (userName == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		
		//	read document list
		String query = "SELECT " + DOCUMENT_ID_COLUMN_NAME + ", " + DOCUMENT_NAME_COLUMN_NAME + ", " + DOCUMENT_DESCRIPTION_COLUMN_NAME + ", " + CREATE_TIME_COLUMN_NAME + ", " + UPDATE_TIME_COLUMN_NAME + 
				" FROM " + DOCUMENT_TABLE_NAME + 
				" WHERE " + USER_NAME_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(userName) + "'" +
				"";
		SqlQueryResult sqr = null;
		ArrayList docListData = new ArrayList();
		try {
			sqr = this.io.executeSelectQuery(query, true);
			while (sqr.next()) try {
				docListData.add(new DocumentData(sqr.getString(0), sqr.getString(1), sqr.getString(2), TIMESTAMP_DATE_FORMAT.format(new Date(sqr.getLong(3))), TIMESTAMP_DATE_FORMAT.format(new Date(sqr.getLong(4)))));
			} catch (NumberFormatException nfe) {}
		}
		catch (SQLException sqle) {
			System.out.println("Exception getting document list for user '" + userName + "': " + sqle.getMessage());
			System.out.println("  query was " + query);
		}
		finally {
			if (sqr != null)
				sqr.close();
		}
		
		//	send JavaScript updating document list
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		blw.writeLine("clearDocuments();");
		for (int d = 0; d < docListData.size(); d++)
			blw.writeLine("addDocument(" + ((DocumentData) docListData.get(d)).toJsonString() + ");");
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private boolean hasDocumentAccess(HttpServletRequest request, String docId) {
		return (this.getDocumentName(request, docId) != null);
	}
	
	private String getDocumentName(HttpServletRequest request, String docId) {
		
		//	get user name
		String userName = this.webAppHost.getUserName(request);
		if (userName == null)
			return null;
		
		//	check database
		String query = "SELECT " + DOCUMENT_NAME_COLUMN_NAME + 
				" FROM " + DOCUMENT_TABLE_NAME + 
				" WHERE " + DOCUMENT_ID_HASH_COLUMN_NAME + " = " + docId.hashCode() + 
				" AND " + DOCUMENT_ID_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(docId) + "'" +
				" AND " + USER_NAME_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(userName) + "'" +
				"";
		SqlQueryResult sqr = null;
		try {
			sqr = this.io.executeSelectQuery(query, true);
			if (sqr.next())
				return sqr.getString(0);
			else return null;
		}
		catch (SQLException sqle) {
			System.out.println("Exception getting name for document '" + docId + "' and user '" + userName + "': " + sqle.getMessage());
			System.out.println("  query was " + query);
			return null;
		}
		finally {
			if (sqr != null)
				sqr.close();
		}
	}
	
	private boolean deleteDocument(HttpServletRequest request, String docId) {
		//	we DO NOT delete the document data proper, as it might also belong to another user
		
		//	get user name
		String userName = this.webAppHost.getUserName(request);
		if (userName == null)
			return false;
		
		//	delete database entry
		String query = "DELETE" + 
				" FROM " + DOCUMENT_TABLE_NAME + 
				" WHERE " + DOCUMENT_ID_HASH_COLUMN_NAME + " = " + docId.hashCode() + 
				" AND " + DOCUMENT_ID_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(docId) + "'" +
				" AND " + USER_NAME_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(userName) + "'" +
				"";
		try {
			int deleted = this.io.executeUpdateQuery(query);
			return (deleted != 0);
		}
		catch (SQLException sqle) {
			System.out.println("Exception deleting document '" + docId + "' for user '" + userName + "': " + sqle.getMessage());
			System.out.println("  query was " + query);
			return false;
		}
	}
	
	ImDocument loadDocument(HttpServletRequest request, String docId) throws IOException {
		
		//	check if user has permission to edit argument document ID
		if (!this.hasDocumentAccess(request, docId))
			throw new IOException("Invalid document ID, or not allowed to access to document");
		
		//	get document folder
		File docFolder = this.getDocFolder(docId, false);
		if (!docFolder.exists() || !docFolder.isDirectory())
			throw new IOException("Invalid document ID, or not allowed to access to document");
		
		//	get user name (we already know it exists, for otherwise the access check would fail)
		String userName = this.webAppHost.getUserName(request);
		
		//	get user specific entry list (see below)
		ArrayList imfEntries = new ArrayList();
		BufferedReader imfEntryIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(docFolder, ("entries." + userName + ".txt"))), "UTF-8"));
		for (String imfEntryLine; (imfEntryLine = imfEntryIn.readLine()) != null;) {
			ImDocumentEntry ime = ImDocumentEntry.fromTabString(imfEntryLine);
			if (ime != null)
				imfEntries.add(ime);
		}
		imfEntryIn.close();
		
		//	load document from folder
		return ImDocumentIO.loadDocument(docFolder, ((ImDocumentEntry[]) imfEntries.toArray(new ImDocumentEntry[imfEntries.size()])));
	}
	
	String[] storeDocument(ImDocument doc, String userName, ProgressMonitor pm) throws IOException {
		
		//	get update time
		long updateTime = System.currentTimeMillis();
		
		//	get document name
		String docName = ((String) doc.getAttribute(DOCUMENT_NAME_ATTRIBUTE, doc.docId));
		
		//	create document description as "<firstAuthor> 'et al.'? (<year>) <title>"
		StringBuffer ddb = new StringBuffer();
		RefData rd = BibRefUtils.modsAttributesToRefData(doc);
		String[] authors = rd.getAttributeValues(BibRefUtils.AUTHOR_ANNOTATION_TYPE);
		if (authors.length != 0) {
			ddb.append(authors[0]);
			if (authors.length != 1)
				ddb.append(" et al.");
		}
		String year = rd.getAttribute(BibRefUtils.YEAR_ANNOTATION_TYPE);
		if (year != null)
			ddb.append(" (" + year + ")");
		String title = rd.getAttribute(BibRefUtils.TITLE_ANNOTATION_TYPE);
		if (title != null)
			ddb.append(" " + title);
		String docDescription = ((ddb.length() <= DOCUMENT_DESCRIPTION_COLUMN_LENGTH) ? ddb.toString() : (ddb.substring(0, (DOCUMENT_DESCRIPTION_COLUMN_LENGTH - " ...".length())) + " ..."));
		
		//	try and update database record
		String query = "UPDATE " + DOCUMENT_TABLE_NAME + 
				" SET" +
					" " + UPDATE_TIME_COLUMN_NAME + " = " + updateTime + 
					", " + DOCUMENT_DESCRIPTION_COLUMN_NAME + " = '" + EasyIO.sqlEscape(docDescription) + "'" + 
				" WHERE " + DOCUMENT_ID_HASH_COLUMN_NAME + " = " + doc.docId.hashCode() + 
				" AND " + DOCUMENT_ID_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(doc.docId) + "'" +
				" AND " + USER_NAME_COLUMN_NAME + " LIKE '" + EasyIO.sqlEscape(userName) + "'" +
				";";
		try {
			int updated = this.io.executeUpdateQuery(query);
			if (updated == 0) {
				query = "INSERT INTO " + DOCUMENT_TABLE_NAME + 
						" (" + 
							DOCUMENT_ID_COLUMN_NAME + 
							", " + 
							DOCUMENT_ID_HASH_COLUMN_NAME + 
							", " + 
							DOCUMENT_NAME_COLUMN_NAME + 
							", " + 
							USER_NAME_COLUMN_NAME + 
							", " + 
							CREATE_TIME_COLUMN_NAME + 
							", " + 
							UPDATE_TIME_COLUMN_NAME + 
							", " + 
							DOCUMENT_DESCRIPTION_COLUMN_NAME + 
						") VALUES (" +
							"'" + EasyIO.sqlEscape(doc.docId) + "'" + 
							", " + 
							"" + doc.docId.hashCode() + "" + 
							", " + 
							"'" + EasyIO.sqlEscape(docName) + "'" + 
							", " + 
							"'" + EasyIO.sqlEscape(userName) + "'" + 
							", " + 
							"" + updateTime + "" + 
							", " + 
							"" + updateTime + "" + 
							", " + 
							"'" + EasyIO.sqlEscape(docDescription) + "'" + 
						");";
				updated = this.io.executeUpdateQuery(query);
				System.out.println(" - " + updated + " document status records created.");
			}
		}
		catch (SQLException sqle) {
			System.out.println("Exception storing document '" + doc.docId + "' for user '" + userName + "': " + sqle.getMessage());
			System.out.println("  query was " + query);
			throw new IOException(sqle.getMessage());
		}
		
		/* TODO do keep entry list, and facilitate specifying it to ImfIO so
		 * - document can exist _independently_ for multiple users in same folder ...
		 * - ... share all identical entries ...
		 * - ... and still appear totally independent to the outside
		 */
		//	try and store document in local storage
		File docFolder = this.getDocFolder(doc.docId, true);
		ImDocumentEntry[] docEntries = ImDocumentIO.storeDocument(doc, docFolder);
		
		//	write user version specific entry list (in that way, multiple users can have the document without interference, but sharing all entries that are identical)
		File entryListFile = new File(docFolder, ("entries." + userName + ".txt"));
		if (entryListFile.exists()) {
			entryListFile.renameTo(new File(docFolder, ("entries." + userName + ".txt." + System.currentTimeMillis() + ".old")));
			entryListFile = new File(docFolder, ("entries." + userName + ".txt"));
		}
		BufferedWriter entryListOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(entryListFile)));
		for (int e = 0; e < docEntries.length; e++) {
			entryListOut.write(docEntries[e].toTabString());
			entryListOut.newLine();
		}
		entryListOut.flush();
		entryListOut.close();
		
		/* TODO for IMS write-through:
		 * - simply create document data object from entry list ...
		 * - ... and use IMS client updateDocumentFromData() to send updates to backing IMS
		 *   - use configured community account to authenticate ...
		 *   - ... and give <userName>@<communityName> as user to credit
		 */
		//	OR
		//	TODO add IMF write-through as extra function in document list
		//	OR
		//	TODO add IMF write-through as option in 'Export' menu ...
		//	TODO ... showing export log to user
		//	==> sort of abuse of file export facilities, but nicely workable way
		
		//	return empty storage log (nothing to log here)
		return new String[0];
	}
	
	/**
	 * Retrieve a pointer to the folder in which to store a document with a
	 * specific ID. If the <code>create</code> argument is false and the folder
	 * does not already exist, this method returns null.
	 * @param docId the ID of the document
	 * @param create create the folder if it does not exist?
	 * @return the folder to store the document in
	 */
	protected File getDocFolder(String docId, boolean create) {
		String firstLevel = (docId.substring(0, 2));
		String secondLevel = (docId.substring(2, 4));
		File docFolder = new File(this.docRootPath, (firstLevel + "/" + secondLevel + "/" + docId));
		if (create)
			docFolder.mkdirs();
		return (docFolder.exists() ? docFolder : null);
	}
}