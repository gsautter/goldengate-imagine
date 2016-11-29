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

import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Icon;

import de.uka.ipd.idaho.easyIO.help.HelpChapter;
import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.feedback.FeedbackPanel;
import de.uka.ipd.idaho.gamta.util.feedback.FeedbackPanel.FeedbackService;
import de.uka.ipd.idaho.gamta.util.feedback.html.renderers.BufferedLineWriter;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory.PromptProvider;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationUtils;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePluginDataProvider;
import de.uka.ipd.idaho.goldenGate.util.HelpChapterDataProviderBased;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineServletEditor.ActionThread;
import de.uka.ipd.idaho.im.imagine.web.GoldenGateImagineServletEditor.WebImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImUtils;
import de.uka.ipd.idaho.im.util.ImUtils.CopyManager;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * This servlet provides a browser based version of GoldenGATE Imagine.
 * 
 * @author sautter
 */
public class GoldenGateImagineEditorServlet extends GoldenGateImagineServlet implements LiteratureConstants {
	//	TODO_ne implement web document viewer interface (extending ImageMarkupTool)
	//	TODO_ne implement web document view interface (or abstract class) to show one specific document
	//	TODO_ne use that for document metadata editing
	//	TODO maybe use that for font editing
	//	TODO maybe use that for cluster base OCR correction
	
	private GoldenGateConfiguration ggImagineConfig = null;
	private GoldenGateImagine ggImagine;
	private Settings ggImagineSettings;
	
	private ImageDocumentFileExporter[] exportMenuExporters = null;
	private Map exportersById = Collections.synchronizedMap(new HashMap());
	private File exportCacheFolder;
	
	private ImageMarkupTool[] editMenuImTools = null;
	private ImageMarkupTool[] toolsMenuImTools = null;
	private Map imToolsById = Collections.synchronizedMap(new HashMap());
//	private AsynchronousRequestHandler imToolRunner = null;
	
	private GoldenGatePluginDataProvider helpDataProvider;
	private HelpChapter helpRoot;
	private Map helpContentById = Collections.synchronizedMap(new HashMap());
	
	private Map demoDocList = null;
	private boolean requireAuthentication = true;
	
	public GoldenGateImagineEditorServlet() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.client.GgServerClientServlet#doInit()
	 */
	protected void doInit() throws ServletException {
		super.doInit();
		
		//	take over prompting
		DialogFactory.setPromptProvider(new PromptProvider() {
			public void alert(Object message, String title, int messageType, Icon icon) {
				Thread ct = Thread.currentThread();
				if (ct instanceof ActionThread)
					((ActionThread) ct).alert(message, title, messageType, icon);
			}
			public int confirm(Object message, String title, int optionType, int messageType, Icon icon) {
				Thread ct = Thread.currentThread();
				if (ct instanceof ActionThread)
					return ((ActionThread) ct).confirm(message, title, optionType, messageType, icon);
				else throw new RuntimeException("Please prompt from Action Thread !!!");
				//	TODO_not maybe just send an alert and return CANCELLED
				//	and then DON'T, as we don't know the semantics
			}
		});
		
		//	take over copy&paste
		ImUtils.setCopyManager(new CopyManager() {
			public void copy(Transferable data) {
				Thread ct = Thread.currentThread();
				if (ct instanceof ActionThread)
					((ActionThread) ct).copy(data);
			}
		});
		
		//	direct feedback requests to action threads
		FeedbackPanel.addFeedbackService(new FeedbackService() {
			public int getPriority() {
				return 10; // we _do_ want to be asked
			}
			public boolean canGetFeedback(FeedbackPanel fp) {
				return (Thread.currentThread() instanceof ActionThread);
			}
			public void getFeedback(FeedbackPanel fp) {
				Thread ct = Thread.currentThread();
				if (ct instanceof ActionThread)
					((ActionThread) ct).getFeedback(fp);
			}
			public boolean isLocal() { return false; }
			public boolean isMultiFeedbackSupported() { return false; }
			public void getMultiFeedback(FeedbackPanel[] fps) throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
			public void shutdown() {}
		});
//		
//		//	create asynchronous request handler for running markup gizmos
//		this.imToolRunner = new ImToolRunner();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		super.reInit();
//		
//		//	check if any requests running
//		if ((this.imToolRunner != null) && (this.imToolRunner.getRunningRequestCount() != 0))
//			throw new ServletException("Unable to reload GoldenGATE Imagine, there are request running.");
		
		//	check for (list of) demo document(s)
		this.demoDocList = null;
		String demoDocListName = this.getSetting("demoDocList");
		if (demoDocListName != null) try {
			StringVector demoDocList = StringVector.loadList(new File(this.dataFolder, demoDocListName));
			for (int d = 0; d < demoDocList.size(); d++) {
				String demoDocDataStr = demoDocList.get(d).trim();
				
				//	sort out blan lines and comments
				if ((demoDocDataStr.length() == 0) || demoDocDataStr.startsWith("//"))
					continue;
				
				//	parse data
				String[] demoDocData = demoDocDataStr.split("\\t");
				if (demoDocData.length < 2)
					continue;
				
				//	test if document exists (also check entry list for folder documents)
				File demoDocFile = new File(this.dataFolder, demoDocData[1]);
				if (!demoDocFile.exists())
					continue;
				if (demoDocFile.isDirectory()) {
					File demoDocEntriesFile = new File(demoDocFile, "entries.txt");
					if (!demoDocEntriesFile.exists())
						continue;
				}
				
				//	create demo file map only on demand
				if (this.demoDocList == null)
					this.demoDocList = Collections.synchronizedMap(new LinkedHashMap());
				
				//	map demo document
				this.demoDocList.put(demoDocData[0], demoDocFile);
			}
		} catch (Exception e) {}
		
		//	check if we require authentication
		this.requireAuthentication = (this.demoDocList == null);
		
		//	shut down GoldenGATE
		if (this.ggImagine != null) {
			this.ggImagine = null;
			this.ggImagineConfig = null;
			this.ggImagineSettings = null;
		}
		
		//	clean up cached image markup tools
		this.imToolsById.clear();
		this.editMenuImTools = null;
		this.toolsMenuImTools= null;
		
		//	read how to access GoldenGATE config
		String ggiConfigName = this.getSetting("ggImagineConfigName");
		String ggiConfigHost = this.getSetting("ggImagineConfigHost");
		String ggiConfigPath = this.getSetting("ggImagineConfigPath");
		if (ggiConfigName == null)
			throw new ServletException("Unable to access GoldenGATE Configuration.");
		
		//	load configuration
		try {
			this.ggImagineConfig = ConfigurationUtils.getConfiguration(ggiConfigName, ggiConfigPath, ggiConfigHost, this.ggImagineRootPath);
		}
		catch (IOException ioe) {
			throw new ServletException("Unable to access GoldenGATE Configuration.", ioe);
		}
		
		//	check if we got a configuration from somewhere
		if (this.ggImagineConfig == null)
			throw new ServletException("Unable to access GoldenGATE Configuration.");
		
		//	load GG Imagine specific settings via configuration
		try {
			Reader ggImagineSetIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.dataFolder, "GgImagine.cnfg"))));
			this.ggImagineSettings = Settings.loadSettings(ggImagineSetIn);
			ggImagineSetIn.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
			this.ggImagineSettings = new Settings();
		}
		
		//	load GoldenGATE core
		try {
			this.ggImagine = GoldenGateImagine.openGoldenGATE(this.ggImagineConfig, this.ggImagineRootPath, false);
		}
		catch (IOException ioe) {
			throw new ServletException("Unable to load GoldenGATE instance", ioe);
		}
		
		//	create export cache folder
		this.exportCacheFolder = new File(this.cacheRootPath, "export");
		this.exportCacheFolder.mkdirs();
		
		//	create help base infrastructure
		this.helpDataProvider = this.ggImagine.getHelpDataProvider();
		this.helpRoot = this.buildHelpContentRoot();
		
		//	read main menu layout settings
		ArrayList fileMenuItemNames = new ArrayList();
		ArrayList exportMenuItemNames = new ArrayList();
		ArrayList editMenuItemNames = new ArrayList();
		ArrayList toolsMenuItemNames = new ArrayList();
		try {
			ArrayList menuItemNames = null;
			BufferedReader mlIn;
			if (this.ggImagine.getConfiguration().isDataAvailable("GgImagine.menus.cnfg"))
				mlIn = new BufferedReader(new InputStreamReader(this.ggImagine.getConfiguration().getInputStream("GgImagine.menus.cnfg"), "UTF-8"));
			else mlIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.dataFolder, "GgImagine.menus.cnfg")), "UTF-8"));
			for (String mll; (mll = mlIn.readLine()) != null;) {
				mll = mll.trim();
				if ((mll.length() == 0) || mll.startsWith("//"))
					continue;
				if ("FILE-MENU".equals(mll))
					menuItemNames = fileMenuItemNames;
				else if ("EXPORT-MENU".equals(mll))
					menuItemNames = exportMenuItemNames;
				else if ("EDIT-MENU".equals(mll))
					menuItemNames = editMenuItemNames;
				else if ("TOOLS-MENU".equals(mll))
					menuItemNames = toolsMenuItemNames;
				else if (menuItemNames != null)
					menuItemNames.add(mll);
			}
			mlIn.close();
		}
		catch (IOException ioe) {
			System.out.println("Error reading menu layout: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	create help for 'File' menu
		HelpChapter fileHelp = new HelpChapterDataProviderBased("Menu 'File'", this.helpDataProvider, "GgImagine.FileMenu.html");
		this.helpRoot.addSubChapter(fileHelp);
		this.helpContentById.put(new Integer(fileHelp.hashCode()), fileHelp);
		
		//	get 'Export' menu items, and index image document exporters
		ImageDocumentExporter[] ides = this.ggImagine.getDocumentExporters();
		HashMap exportItems = new LinkedHashMap();
		HelpChapter exportHelp = new HelpChapterDataProviderBased("Menu 'Export'", this.helpDataProvider, "GgImagine.ExportMenu.html");
		this.helpRoot.addSubChapter(exportHelp);
		this.helpContentById.put(new Integer(exportHelp.hashCode()), exportHelp);
		for (int e = 0; e < ides.length; e++)
			if (ides[e] instanceof ImageDocumentFileExporter) {
				exportItems.put(ides[e].getExportMenuLabel(), ides[e]);
				this.exportersById.put(new Integer(ides[e].hashCode()), ides[e]);
				
				//	add exporter specific help chapter if available
				HelpChapter ideHelp = ((GoldenGatePlugin) ides[e]).getHelp();
				if (ideHelp != null) {
					exportHelp.addSubChapter(ideHelp);
					this.helpContentById.put(new Integer(ideHelp.hashCode()), ideHelp);
				}
			}
		ArrayList exportMenuExporters = this.orderMenuItems(exportMenuItemNames, exportItems);
		this.exportMenuExporters = ((ImageDocumentFileExporter[]) exportMenuExporters.toArray(new ImageDocumentFileExporter[exportMenuExporters.size()]));
		
		//	index image markup tool providers
		ImageMarkupToolProvider[] imtps = this.ggImagine.getImageMarkupToolProviders();
		
		//	get 'Edit' menu items, and index image markup tools
		HashMap editItems = new LinkedHashMap();
		HelpChapter editHelp = new HelpChapterDataProviderBased("Menu 'Edit'", this.helpDataProvider, "GgImagine.EditMenu.html");
		this.helpRoot.addSubChapter(editHelp);
		this.helpContentById.put(new Integer(editHelp.hashCode()), editHelp);
		for (int p = 0; p < imtps.length; p++) {
			String[] emImtNames = imtps[p].getEditMenuItemNames();
			if ((emImtNames == null) || (emImtNames.length == 0))
				continue;
			for (int n = 0; n < emImtNames.length; n++) {
				ImageMarkupTool emImt = imtps[p].getImageMarkupTool(emImtNames[n]);
				if (emImt == null)
					continue;
				editItems.put(emImt.getLabel(), emImt);
				this.imToolsById.put(("EDT-" + emImt.hashCode()), emImt);
				
				//	add help chapter if available
				String imtHelpText = emImt.getHelpText();
				HelpChapter imtHelp = new HelpChapter(emImt.getLabel(), ((imtHelpText == null) ? "Help is coming soon." : imtHelpText));
				editHelp.addSubChapter(imtHelp);
				this.helpContentById.put(new Integer(imtHelp.hashCode()), imtHelp);
			}
		}
		ArrayList editMenuImTools = this.orderMenuItems(editMenuItemNames, editItems);
		this.editMenuImTools = ((ImageMarkupTool[]) editMenuImTools.toArray(new ImageMarkupTool[editMenuImTools.size()]));
		
		//	get 'Tools' menu items, and index image markup tools
		HashMap toolsItems = new LinkedHashMap();
		HelpChapter toolsHelp = new HelpChapterDataProviderBased("Menu 'Tools'", this.helpDataProvider, "GgImagine.ToolsMenu.html");
		this.helpRoot.addSubChapter(toolsHelp);
		this.helpContentById.put(new Integer(toolsHelp.hashCode()), toolsHelp);
		for (int p = 0; p < imtps.length; p++) {
			String[] tmImtNames = imtps[p].getToolsMenuItemNames();
			if ((tmImtNames == null) || (tmImtNames.length == 0))
				continue;
			for (int n = 0; n < tmImtNames.length; n++) {
				ImageMarkupTool tmImt = imtps[p].getImageMarkupTool(tmImtNames[n]);
				if (tmImt == null)
					continue;
				toolsItems.put(tmImt.getLabel(), tmImt);
				this.imToolsById.put(("TLS-" + tmImt.hashCode()), tmImt);
				
				//	add help chapter if available
				String imtHelpText = tmImt.getHelpText();
				HelpChapter imtHelp = new HelpChapter(tmImt.getLabel(), ((imtHelpText == null) ? "Help is coming soon." : imtHelpText));
				toolsHelp.addSubChapter(imtHelp);
				this.helpContentById.put(new Integer(imtHelp.hashCode()), imtHelp);
			}
		}
		ArrayList toolsMenuImTools = this.orderMenuItems(toolsMenuItemNames, toolsItems);
		this.toolsMenuImTools = ((ImageMarkupTool[]) toolsMenuImTools.toArray(new ImageMarkupTool[toolsMenuImTools.size()]));
	}
	
	private ArrayList orderMenuItems(ArrayList itemNames, HashMap items) {
		ArrayList orderedItems = new ArrayList();
		Object item;
		boolean lastWasItem = false;
		
		//	add configured items first
		for (int i = 0; i < itemNames.size(); i++) {
			String itemName = ((String) itemNames.get(i));
			if ("---".equals(itemName)) {
				if (lastWasItem)
					orderedItems.add(null);
				lastWasItem = false;
				continue;
			}
			item = items.remove(itemName);
			if (item != null) {
				orderedItems.add(item);
				lastWasItem = true;
			}
		}
		
		//	add remaining items
		if (lastWasItem && (items.size() != 0)) {
			orderedItems.add(null);
			lastWasItem = false;
		}
		for (Iterator init = items.keySet().iterator(); init.hasNext();) {
			String itemName = ((String) init.next());
			if ("---".equals(itemName) && init.hasNext()) {
				if (lastWasItem)
					orderedItems.add(null);
				lastWasItem = false;
				continue;
			}
			item = items.get(itemName);
			if (item != null) {
				orderedItems.add(item);
				lastWasItem = true;
			}
		}
		
		//	finally ...
		return orderedItems;
	}
	
	private HelpChapter buildHelpContentRoot() {
		HelpChapter helpRoot = new HelpChapterDataProviderBased("GoldenGATE Imagine", this.helpDataProvider, "GgImagine.html");
		this.helpContentById.put(new Integer(helpRoot.hashCode()), helpRoot);
		
		HelpChapter glossary = new HelpChapterDataProviderBased("Glossary", this.helpDataProvider, "GgImagine.Glossary.html");
		helpRoot.addSubChapter(glossary);
		this.helpContentById.put(new Integer(glossary.hashCode()), glossary);
		
		HelpChapter editorHelp = new HelpChapterDataProviderBased("Editor", this.helpDataProvider, "GgImagine.Editor.html");
		helpRoot.addSubChapter(editorHelp);
		this.helpContentById.put(new Integer(editorHelp.hashCode()), editorHelp);
		SelectionActionProvider[] saps = this.ggImagine.getSelectionActionProviders();
		for (int p = 0; p < saps.length; p++) {
			HelpChapter sapHelp = saps[p].getHelp();
			if (sapHelp != null) {
				editorHelp.addSubChapter(sapHelp);
				this.helpContentById.put(new Integer(sapHelp.hashCode()), sapHelp);
			}
		}
		
		ImageDocumentDropHandler[] dropHandlers = this.ggImagine.getDropHandlers();
		if (dropHandlers.length != 0) {
			HelpChapter dragDropHelp = new HelpChapterDataProviderBased("Drag & Drop", this.helpDataProvider, "GgImagine.DragDrop.html");
			helpRoot.addSubChapter(dragDropHelp);
			for (int h = 0; h < dropHandlers.length; h++) {
				HelpChapter dhHelp = dropHandlers[h].getHelp();
				if (dhHelp != null) {
					dragDropHelp.addSubChapter(dhHelp);
					this.helpContentById.put(new Integer(dhHelp.hashCode()), dhHelp);
				}
			}
		}
		
		return helpRoot;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.WebServlet#exit()
	 */
	protected void exit() {
		//	TODO close any open documents
	}
	
	File getExportCacheFolder() {
		return this.exportCacheFolder;
	}
	
	ImageDocumentFileExporter getExporter(String exportId) {
		return ((ImageDocumentFileExporter) this.exportersById.get(new Integer(exportId)));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.client.GgServerHtmlServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	check authentication and related requests if required
		if (this.requireAuthentication) {
			
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
//		
//		//	loop through requests related to feedback panels
//		if (this.imToolRunner.handleRequest(request, response))
//			return;
		
		//	get path info
		String pathInfo = request.getPathInfo();
		
		//	handle request for base page
		if (pathInfo == null) {
			String docId = request.getParameter(DOCUMENT_ID_ATTRIBUTE);
			if ((docId == null) && (this.demoDocList != null))
				docId = ((String) this.demoDocList.keySet().iterator().next());
			if (docId == null)
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			else this.sendEditorPage(request, response, docId);
			return;
		}
		
		//	check document editors
		if (this.handleEditorRequest(request, response, pathInfo))
			return;
//		
//		//	handle request for menu action status page
//		if (pathInfo.startsWith("/mmActionStatus/")) {
//			String actionId = pathInfo.substring("/mmActionStatus/".length());
//			this.sendMainMenuActionStatusPage(request, actionId, response);
//			return;
//		}
		
		//	provide symbol table (called from editors)
		if (pathInfo.startsWith("/symbolTable")) {
			this.sendSymbolTable(request, response);
			return;
		}
		
		//	handle requests for help structure
		if (pathInfo.equals("/help.js")) {
			this.sendHelpTree(request, response);
			return;
		}
		
		//	handle requests for help chapter
		if (pathInfo.startsWith("/help/")) {
			String helpId = pathInfo.substring("/help/".length());
			this.sendHelpContent(request, helpId, response);
			return;
		}
		
		//	let super class handle whatever other request might come
		super.doGet(request, response);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGateServer.client.GgServerHtmlServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//	check authentication and related requests if required
		if (this.requireAuthentication) {
			
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
//		
//		//	loop through requests related to feedback panels
//		if (this.imToolRunner.handleRequest(request, response))
//			return;
		
		//	check document editors
		if (this.handleEditorRequest(request, response, request.getPathInfo()))
			return;
		
		//	let super class handle whatever other request might come
		super.doPost(request, response);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#sendHtmlPage(de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	public void sendHtmlPage(HtmlPageBuilder pageBuilder) throws IOException {
		super.sendHtmlPage(pageBuilder);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#sendPopupHtmlPage(de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	protected void sendPopupHtmlPage(HtmlPageBuilder pageBuilder) throws IOException {
		super.sendPopupHtmlPage(pageBuilder);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#sendHtmlPage(java.lang.String, de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	protected void sendHtmlPage(String basePageName, HtmlPageBuilder pageBuilder) throws IOException {
		super.sendHtmlPage(basePageName, pageBuilder);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#sendHtmlPage(java.io.Reader, de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder)
	 */
	public void sendHtmlPage(Reader basePageReader, HtmlPageBuilder pageBuilder) throws IOException {
		super.sendHtmlPage(basePageReader, pageBuilder);
	}
	
	private boolean handleEditorRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException {
		if (pathInfo == null)
			return false;
		
		//	split document editor ID off path info
		if (pathInfo.startsWith("/"))
			pathInfo = pathInfo.substring("/".length());
		String docEditorId;
		if (pathInfo.indexOf('/') == -1) {
			docEditorId = pathInfo;
			pathInfo = null;
		}
		else {
			docEditorId = pathInfo.substring(0, pathInfo.indexOf('/'));
			pathInfo = pathInfo.substring(pathInfo.indexOf('/'));
		}
		
		//	get document editor
		GoldenGateImagineServletEditor idme = this.getDocumentEditor(docEditorId);
		if (idme == null)
			return false;
		
		//	let document editor handle request
		return idme.handleRequest(request, response, pathInfo);
	}
	
	private void sendEditorPage(HttpServletRequest request, HttpServletResponse response, String docId) throws IOException {
		
		//	get user name
		String userName = (this.requireAuthentication ? this.webAppHost.getUserName(request) : null);
		
		//	get page IDs (optional)
		String pageIDsStr = request.getParameter("pages");
		if ((pageIDsStr != null) && !pageIDsStr.matches("[0-9]+(\\-[0-9]+)?")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid page ID '" + pageIDsStr + "'"));
			return;
		}
		
		//	parse page IDs (if any)
		int firstPageId = 0;
		int lastPageId = -1;
		if (pageIDsStr == null) {}
		else if (pageIDsStr.indexOf('-') == -1) {
			firstPageId = Integer.parseInt(pageIDsStr);
			lastPageId = firstPageId;
		}
		else {
			firstPageId = Integer.parseInt(pageIDsStr.substring(0, pageIDsStr.indexOf('-')));
			lastPageId = Integer.parseInt(pageIDsStr.substring(pageIDsStr.indexOf('-') + "-".length()));
			if (lastPageId < firstPageId)
				lastPageId = firstPageId;
		}
		
		//	try and find existing editor if user name given
		if (userName != null)
			synchronized (this.documentEditorsById) {
				for (Iterator deit = this.documentEditorsById.keySet().iterator(); deit.hasNext();) {
					GoldenGateImagineServletEditor idme = this.getDocumentEditor((String) deit.next());
					if (!idme.idmp.document.docId.equals(docId))
						continue;
					if (!userName.equals(idme.userName))
						continue;
					if (!idme.idmp.isPageVisible(firstPageId))
						continue;
					if ((firstPageId > 0) && idme.idmp.isPageVisible(firstPageId - 1))
						continue;
					if (lastPageId == -1) {
						if ((lastPageId == -1) && !idme.idmp.isPageVisible(idme.idmp.document.getPageCount()-1))
							continue;
					}
					else {
						if (!idme.idmp.isPageVisible(lastPageId))
							continue;
						if ((lastPageId < (idme.idmp.document.getPageCount()-1)) && idme.idmp.isPageVisible(lastPageId + 1))
							continue;
					}
					
					//	redirect to existing editor page, and we're done
					response.sendRedirect(request.getContextPath() + request.getServletPath() + "/" + idme.id);
					return;
				}
			}
		
		//	load document
		ImDocument doc = this.loadDocument(request, docId);
		if (doc == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid document ID '" + docId + "'"));
			return;
		}
		
		//	TODO also get plug-in, IMT, and menu option filter
		
		//	check last page ID
		if (lastPageId == -1)
			lastPageId = (doc.getPageCount() - 1);
		
		//	create and register editor
		WebImDocumentMarkupPanel idmp = new WebImDocumentMarkupPanel(doc, firstPageId, (lastPageId - firstPageId + 1), this.ggImagine, this.getSettingsFor(userName));
		GoldenGateImagineServletEditor idme = new GoldenGateImagineServletEditor(this, idmp, (pageIDsStr == null), userName);
		this.documentEditorsById.put(idme.id, idme);
		
		//	redirect to editor page (hardens page against reloading)
		response.sendRedirect(request.getContextPath() + request.getServletPath() + "/" + idme.id);
	}
	
	void sendMainMenuActions(HttpServletRequest request, HttpServletResponse response, GoldenGateImagineServletEditor idme) throws IOException {
		
		//	send JavaScript calls populating main menu from JSON arguments
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		
		//	send 'File' menu, containing built-in items
		blw.writeLine("addMainMenuItem({");
		blw.writeLine("  \"label\": \"File\",");
		blw.writeLine("  \"items\": [");
		if (this.requireAuthentication) {
			blw.writeLine("    {");
			blw.writeLine("      \"label\": \"Save Document\",");
			blw.writeLine("      \"tooltip\": \"Save the document on the server\",");
			blw.writeLine("      \"id\": \"FL-save\"");
			blw.writeLine("    }");
			blw.writeLine("    ,");
			if (idme.isFullDocument) {
				blw.writeLine("    {");
				blw.writeLine("      \"label\": \"Save Document Locally\",");
				blw.writeLine("      \"tooltip\": \"Save the document locally on your machine\",");
				blw.writeLine("      \"exportUrl\": \"" + request.getContextPath() + request.getServletPath()+ "/" + idme.id + "/export?exportId=imf\",");
				blw.writeLine("      \"id\": \"EXP-imf\"");
				blw.writeLine("    }");
				blw.writeLine("    ,");
			}
		}
		blw.writeLine("    {");
		blw.writeLine("      \"label\": \"Close Document\",");
		blw.writeLine("      \"tooltip\": \"Close the document and the editor page\",");
		blw.writeLine("      \"id\": \"FL-close\"");
		blw.writeLine("    }");
		blw.writeLine("  ]");
		blw.writeLine("});");
		
		/*
- Save Locally: trigger IMF download, using temporary file as for custom exporters below ...
  - ... but only if whole document is showing (would otherwise bypass treatment-wise access to copyrighted documents) ...
  - ... or build excerpt saving mechanism into ImfIO
    - better not, no way of uploading changes back into system ...
    - ... or build merging mechanism as well (should not be all too hard given the structure of IMF)
  ==> facilitate IMF excerpt downloads, and implement merging, but LATER:
    - merge documents, diffing based on IDs ...
    - ... with the merged-in document taking precedence over the merged-into one in case of conflicts (word additions and removals, in particular, but also attributes)
    - merge order: words (adjust annotations on replacement), then annotations and regions
    - mark excerpts with attributes excerptFirstPageId and excerptLastPageId
      ==> also good for recognition of excerpts on upload respective IMFs
		 */
		
		//	send 'Export' menu
		blw.writeLine("addMainMenuItem({");
		blw.writeLine("  \"label\": \"Export\",");
		blw.writeLine("  \"items\": [");
		blw.writeLine("    {");
		blw.writeLine("      \"label\": \"Export XML Document\",");
		blw.writeLine("      \"tooltip\": \"Export this document as XML, logical paragraphs together\",");
		blw.writeLine("      \"exportUrl\": \"" + request.getContextPath() + request.getServletPath() + "/" + idme.id + "/export?exportId=xml\",");
		blw.writeLine("      \"id\": \"EXP-xml\"");
		blw.writeLine("    }");
		blw.writeLine("    ,");
		blw.writeLine("    {");
		blw.writeLine("      \"label\": \"Export Raw XML Document\",");
		blw.writeLine("      \"tooltip\": \"Export this document as XML, words strictly in document order\",");
		blw.writeLine("      \"exportUrl\": \"" + request.getContextPath() + request.getServletPath() + "/" + idme.id + "/export?exportId=rawXml\",");
		blw.writeLine("      \"id\": \"EXP-rawXml\"");
		blw.writeLine("    }");
		if (this.exportMenuExporters.length != 0) {
			blw.writeLine("    ,");
			blw.writeLine("    {");
			blw.writeLine("      \"id\": \"SEPARATOR\"");
			blw.writeLine("    }");
			for (int e = 0; e < this.exportMenuExporters.length; e++) {
				blw.writeLine("    ,");
				blw.writeLine("    {");
				if (this.exportMenuExporters[e] == null)
					blw.writeLine("      \"id\": \"SEPARATOR\"");
				else {
					blw.writeLine("      \"label\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.exportMenuExporters[e].getExportMenuLabel()) + "\",");
					if (this.exportMenuExporters[e].getExportMenuTooltip() != null)
						blw.writeLine("      \"tooltip\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.exportMenuExporters[e].getExportMenuTooltip()) + "\",");
					blw.writeLine("      \"exportUrl\": \"" + request.getContextPath() + request.getServletPath() + "/" + idme.id + "/export?exportId=" + this.exportMenuExporters[e].hashCode() + "\",");
					blw.writeLine("      \"id\": \"EXP-" + this.exportMenuExporters[e].hashCode() + "\"");
				}
				blw.writeLine("    }");
			}
		}
		blw.writeLine("  ]");
		blw.writeLine("});");
		
		//	send 'Edit' menu, containing IMT items
		blw.writeLine("addMainMenuItem({");
		blw.writeLine("  \"label\": \"Edit\",");
		blw.writeLine("  \"items\": [");
		for (int i = 0; i < this.editMenuImTools.length; i++) {
			if (i != 0)
				blw.writeLine(",");
			blw.writeLine("    {");
			if (this.editMenuImTools[i] == null)
				blw.writeLine("      \"id\": \"SEPARATOR\"");
			else {
				blw.writeLine("      \"label\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.editMenuImTools[i].getLabel()) + "\",");
				if (this.editMenuImTools[i].getTooltip() != null)
					blw.writeLine("      \"tooltip\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.editMenuImTools[i].getTooltip()) + "\",");
				blw.writeLine("      \"id\": \"EDT-" + this.editMenuImTools[i].hashCode() + "\"");
			}
			blw.writeLine("    }");
		}
		blw.writeLine("  ]");
		blw.writeLine("});");
		
		//	send (empty) 'UNDO' menu
		blw.writeLine("var undoMenuItem = {");
		blw.writeLine("  \"label\": \"Undo\",");
		blw.writeLine("  \"items\": []");
		blw.writeLine("};");
		blw.writeLine("var undoMenuItems = [];");
		blw.writeLine("addMainMenuItem(undoMenuItem);");
		blw.writeLine("undoMenuItem.div.style.color = '#888888';");
		
		//	send 'Tools' menu, containing IMT items
		blw.writeLine("addMainMenuItem({");
		blw.writeLine("  \"label\": \"Tools\",");
		blw.writeLine("  \"items\": [");
		for (int i = 0; i < this.toolsMenuImTools.length; i++) {
			if (i != 0)
				blw.writeLine(",");
			blw.writeLine("    {");
			if (this.toolsMenuImTools[i] == null)
				blw.writeLine("      \"id\": \"SEPARATOR\"");
			else {
				blw.writeLine("      \"label\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.toolsMenuImTools[i].getLabel()) + "\",");
				if (this.toolsMenuImTools[i].getTooltip() != null)
					blw.writeLine("      \"tooltip\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(this.toolsMenuImTools[i].getTooltip()) + "\",");
				blw.writeLine("      \"id\": \"TLS-" + this.toolsMenuImTools[i].hashCode() + "\"");
			}
			blw.writeLine("    }");
		}
		blw.writeLine("  ]");
		blw.writeLine("});");
		
		//	send 'Help' menu
		blw.writeLine("addMainMenuItem({");
		blw.writeLine("  \"label\": \"Help\",");
		blw.writeLine("  \"items\": [");
		blw.writeLine("    {");
		blw.writeLine("      \"label\": \"Help\",");
		blw.writeLine("      \"contentPath\": \"" + request.getContextPath() + request.getServletPath() + "/help/" + this.helpRoot.hashCode() + "\",");
		blw.writeLine("      \"id\": \"HLP-" + this.helpRoot.hashCode() + "\"");
		blw.writeLine("    }");
		for (int i = 0; i < this.helpRoot.getChildCount(); i++) {
			blw.writeLine(",");
			HelpChapter hc = ((HelpChapter) this.helpRoot.getChildAt(i));
			blw.writeLine("    {");
			blw.writeLine("      \"label\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(hc.getTitle()) + "\",");
			blw.writeLine("      \"contentPath\": \"" + request.getContextPath() + request.getServletPath() + "/help/" + hc.hashCode() + "\",");
			blw.writeLine("      \"id\": \"HLP-" + hc.hashCode() + "\"");
			blw.writeLine("    }");
		}
		blw.writeLine("  ]");
		blw.writeLine("});");
		
		//	finally ...
		blw.flush();
		out.flush();
		blw.close();
	}
//	
//	private void sendMainMenuActionStatusPage(HttpServletRequest request, String actionId, HttpServletResponse response) throws IOException {
//		response.setContentType("text/html");
//		response.setCharacterEncoding("UTF-8");
//		this.imToolRunner.sendStatusDisplayFrame(request, actionId, response);
//	}
	
	private Settings getSettingsFor(String userName) throws IOException {
		
		//	copy general settings
		Settings set = new Settings();
		set.setSettings(this.ggImagineSettings);
		
		//	try to load user specific settings
		if (userName != null) try {
			Reader userSetIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.dataFolder, ("userConfigs/GgImagine." + userName + ".cnfg")))));
			Settings userSet = Settings.loadSettings(userSetIn);
			userSetIn.close();
			set.setSettings(userSet);
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
		
		//	finally ...
		return set;
	}
	
	void docEditorClosing(GoldenGateImagineServletEditor idme) {
		
		//	store personal settings of user (get colors from request HTTP session)
		if (idme.userName != null) try {
			Reader userSetIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.dataFolder, ("userConfigs/GgImagine." + idme.userName + ".cnfg")))));
			Settings userSet = Settings.loadSettings(userSetIn);
			userSetIn.close();
			
			boolean userSetDirty = false;
			String[] annotTypes = idme.idmp.getAnnotationTypes();
			for (int t = 0; t < annotTypes.length; t++)
				userSetDirty = (this.storeColor(("annotation.color." + annotTypes[t]), idme.idmp.getAnnotationColor(annotTypes[t]), userSet) || userSetDirty);
			String[] layoutObjectTypes = idme.idmp.getLayoutObjectTypes();
			for (int t = 0; t < layoutObjectTypes.length; t++)
				userSetDirty = (this.storeColor(("layoutObject.color." + layoutObjectTypes[t]), idme.idmp.getLayoutObjectColor(layoutObjectTypes[t]), userSet) || userSetDirty);
			String[] textStreamTypes = idme.idmp.getTextStreamTypes();
			for (int t = 0; t < textStreamTypes.length; t++)
				userSetDirty = (this.storeColor(("textStream.color." + textStreamTypes[t]), idme.idmp.getTextStreamTypeColor(textStreamTypes[t]), userSet) || userSetDirty);
			
			if (userSetDirty) {
				Writer userSetOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.dataFolder, ("userConfigs/GgImagine." + idme.userName + ".cnfg")))));
				userSet.storeAsText(userSetOut);
				userSetOut.flush();
				userSetOut.close();
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
		
		//	un-register editor
		this.documentEditorsById.remove(idme.id);
	}
	
	private boolean storeColor(String colorKey, Color color, Settings set) {
		if (color == null)
			return false;
		String colorHex = GoldenGateImagine.getHex(color);
		if (colorHex.equals(set.getSetting(colorKey)))
			return false;
		if (colorHex.equals(this.ggImagineSettings.getSetting(colorKey)))
			return false;
		set.setSetting(colorKey, colorHex);
		return true;
	}
	
	private void sendSymbolTable(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File stFile = this.findFile("symbolTable.html");
		if (stFile == null)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		else {
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			BufferedReader stIn = new BufferedReader(new InputStreamReader(new FileInputStream(stFile), "UTF-8"));
			BufferedWriter stOut = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			char[] buffer = new char[1024];
			for (int r; (r = stIn.read(buffer, 0, buffer.length)) != -1;)
				stOut.write(buffer, 0, r);
			stOut.flush();
			stOut.close();
			stIn.close();
		}
	}
	
	private void sendHelpTree(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		Writer out = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
		BufferedLineWriter blw = new BufferedLineWriter(out);
		blw.write("var helpRootChapter = ");
		this.writeHelpChapter(request, this.helpRoot, blw, "");
		blw.writeLine(";");
		blw.flush();
		out.flush();
		blw.close();
	}
	
	private void writeHelpChapter(HttpServletRequest request, HelpChapter hc, BufferedLineWriter blw, String indent) throws IOException {
		blw.writeLine(indent + "{");
		blw.writeLine(indent + "  \"title\": \"" + GoldenGateImagineWebUtils.escapeForJavaScript(hc.getTitle()) + "\",");
		blw.writeLine(indent + "  \"path\": \"" + request.getContextPath() + request.getServletPath() + "/help/" + hc.hashCode() + "\",");
		if (hc.getChildCount() > 0) {
			blw.writeLine(indent + "  \"subChapters\": [");
			for (int c = 0; c < hc.getChildCount(); c++) {
				if (c != 0)
					blw.writeLine(",");
				this.writeHelpChapter(request, ((HelpChapter) hc.getChildAt(c)), blw, (indent + "  "));
			}
			blw.newLine();
			blw.writeLine(indent + "  ],");
		}
		blw.writeLine(indent + "  \"id\": \"HLP-" + hc.hashCode() + "\"");
		blw.write(indent + "}");
	}
	
	private void sendHelpContent(HttpServletRequest request, final String helpId, HttpServletResponse response) throws IOException {
		
		//	check ID
		if (!helpId.matches("\\-?[0-9]+")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid help content ID '" + helpId + "'"));
			return;
		}
		
		//	get help chapter
		HelpChapter helpContent = ((HelpChapter) this.helpContentById.get(new Integer(helpId)));
		if (helpContent == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, ("Invalid help content ID '" + helpId + "'"));
			return;
		}
		
		//	prepare response
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		//	send help window content
		if ("true".equals(request.getParameter("getHelpWindow")))
			this.sendPopupHtmlPage(new HtmlPageBuilder(this, request, response) {
				protected String getPageTitle(String title) {
					return "GoldenGATE Imagine Online Help";
				}
				protected void include(String type, String tag) throws IOException {
					if ("includeBody".equals(type))
						this.includeFile("help.html");
					else super.include(type, tag);
				}
				protected boolean includeJavaScriptDomHelpers() {
					return true;
				}
				protected void writePageHeadExtensions() throws IOException {
					writeJavaScriptTag(this, "help.js", true);
					writeJavaScriptTag(this, "helpHelpers.js", false);
				}
				protected String[] getOnloadCalls() {
					String[] olcs = {
						"hBuildChapterTree();",
						("hShowChapter('HLP-" + helpId + "');")
					};
					return olcs;
				}
			});
		
		//	send help content
		else {
			Writer helpOut = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
			Reader helpIn = helpContent.getTextReader();
			char[] helpBuffer = new char[1024];
			for (int r; (r = helpIn.read(helpBuffer, 0, helpBuffer.length)) != -1;)
				helpOut.write(helpBuffer, 0, r);
			helpOut.flush();
			helpIn.close();
		}
	}
	
	private Map documentEditorsById = Collections.synchronizedMap(new TreeMap());
	private GoldenGateImagineServletEditor getDocumentEditor(String docId) {
		return ((GoldenGateImagineServletEditor) this.documentEditorsById.get(docId));
	}
	
	ImageMarkupTool getMainMenuActionImTool(String actionImToolId) {
		return ((ImageMarkupTool) this.imToolsById.get(actionImToolId));
	}
	
//	void runToolsMenuActionImTool(HttpServletRequest request, HttpServletResponse response, GoldenGateImagineServletEditor idme, String actionImToolId) throws IOException {
//		
//		//	run plugin based action in asynchronous request handler (feedback requests !!!)
//		ImageMarkupTool actionImTool = ((ImageMarkupTool) this.imToolsById.get(actionImToolId));
//		if (actionImTool == null) {
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, ("Invalid action ID '" + actionImToolId + "'"));
//			return;
//		}
//		
//		//	create and start asynchronous request
//		ImToolRun imtRun = new ImToolRun(actionImTool, idme);
//		this.imToolRunner.enqueueRequest(imtRun, idme.id);
//		
//		//	send JavaScript call opening progress monitor
//		response.setContentType("text/plain");
//		response.setCharacterEncoding("UTF-8");
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
//		bw.write("window.open('" + request.getContextPath() + request.getServletPath() + "/mmActionStatus/" + imtRun.id + "', '" + actionImTool.getLabel() + " Running ...', 'width=50,height=50,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes')");
//		bw.flush();
//		bw.close();
//	}
//	
//	String getRunningMainMenuActionImToolId(GoldenGateImagineServletEditor idme) {
//		String[] imToolRunIDs = this.imToolRunner.getRequestIDs(idme.id);
//		return ((imToolRunIDs.length == 0) ? null : imToolRunIDs[0]);
//	}
//	
//	private class ImToolRunner extends AsynchronousRequestHandler {
//		ImToolRunner() {
//			super(false);
//		}
//		public AsynchronousRequest buildAsynchronousRequest(HttpServletRequest request) throws IOException {
//			return null;
//		}
//		protected boolean retainAsynchronousRequest(AsynchronousRequest ar, int finishedArCount) {
//			if (!ar.isFinishedStatusSent())
//				return (System.currentTimeMillis() < (ar.getLastAccessTime() + (1000 * 60 * 5)));
//			else return (System.currentTimeMillis() < (ar.getLastAccessTime() + (1000 * 60)));
//		}
//		protected void sendHtmlPage(HtmlPageBuilder hpb) throws IOException {
//			GoldenGateImagineEditorServlet.this.sendHtmlPage(hpb);
//		}
//		protected void sendPopupHtmlPage(HtmlPageBuilder hpb) throws IOException {
//			GoldenGateImagineEditorServlet.this.sendPopupHtmlPage(hpb);
//		}
//		protected HtmlPageBuilderHost getPageBuilderHost() {
//			return GoldenGateImagineEditorServlet.this;
//		}
//		protected void sendStatusDisplayIFramePage(HtmlPageBuilder hpb) throws IOException {
//			GoldenGateImagineEditorServlet.this.sendHtmlPage("imtStatus.html", hpb);
//		}
//		protected void sendFeedbackFormPage(HtmlPageBuilder hpb) throws IOException {
//			GoldenGateImagineEditorServlet.this.sendHtmlPage("imtFeedback.html", hpb);
//		}
//	}
//	
//	private class ImToolRun extends AsynchronousRequest implements ProgressMonitor {
//		private ImageMarkupTool imt;
//		private GoldenGateImagineServletEditor idme;
//		ImToolRun(ImageMarkupTool imt, GoldenGateImagineServletEditor idme) {
//			super(imt.getLabel());
//			this.imt = imt;
//			this.idme = idme;
//		}
//		protected void process() throws Exception {
//			this.idme.idmp.applyMarkupTool(this.imt, null, this);
//		}
//		
//		private String step;
//		private ArrayList info = new ArrayList();
//		public void setStep(String step) {
//			this.step = step;
//			this.setStatus();
//		}
//		public void setInfo(String info) {
//			this.info.add(info);
//			while (this.info.size() > 3)
//				this.info.remove(0);
//			this.setStatus();
//		}
//		private void setStatus() {
//			StringBuffer status = new StringBuffer("<HTML>");
//			if (this.step != null)
//				status.append("<p class=\"imtStepLabel\">" + html.escape(this.step) + "</p>");
//			for (int i = 0; i < this.info.size(); i++)
//				status.append("<p class=\"imtInfoLabel\">" + html.escape((String) this.info.get(i)) + "</p>");
//			status.append("</HTML>");
//			this.setStatus(status.toString());
//		}
//		
//		private int baseProgress = 0;
//		private int maxProgress = 0;
//		private int progress = 0;
//		public void setBaseProgress(int baseProgress) {
//			this.baseProgress = baseProgress;
//			this.setProgress();
//		}
//		public void setMaxProgress(int maxProgress) {
//			this.maxProgress = maxProgress;
//			this.setProgress();
//		}
//		public void setProgress(int progress) {
//			this.progress = progress;
//			this.setProgress();
//		}
//		private void setProgress() {
//			this.setPercentFinished(this.baseProgress + (((this.maxProgress - this.baseProgress) * this.progress) / 100));
//		}
//		public String getResultLink(HttpServletRequest request) {
//			return (request.getContextPath() + request.getServletPath() + "/" + this.idme.id + "/mmActionResult");
//		}
//		public boolean doImmediateResultForward() {
//			return true;
//		}
//	}
//	
	/**
	 * Load a document with a given ID. This default implementation relies on a
	 * <code>GoldenGateImagineDocumentServlet</code> to do this. Sub classes
	 * willing to load documents from other sources may overwrite this method
	 * to do so.
	 * @param request the HTTP request loading the document
	 * @param docId the ID of the document to load
	 * @return the document with the argument ID
	 * @throws IOException
	 */
	protected ImDocument loadDocument(HttpServletRequest request, String docId) throws IOException {
		
		//	if not in demo mode, load document from central facility
		if (this.demoDocList == null) {
			GoldenGateImagineDocumentServlet docHost = ((GoldenGateImagineDocumentServlet) this.webAppHost.getServlet("DocumentServlet"));
			if (docHost == null)
				throw new IOException("Cannot access document servlet");
			else return docHost.loadDocument(request, docId);
		}
		
		//	try to load demo document
		else {
			File demoDocFile = ((File) this.demoDocList.get(docId));
			if (demoDocFile == null)
				throw new IOException("Document '" + docId + "' not found");
			else return ImDocumentIO.loadDocument(demoDocFile);
		}
		
		/* TODO facilitate filtering plug-ins for each specific document:
		 * - facilitates restricting menus in Demo mode ...
		 * - ... and thus simplifies finding the right menu entries in hands-on tutorials
		 * - facilitates restricting menus in Community/Wiki version ...
		 * - ... and thus creates nice slim UI for treatment editing ...
		 * - ... while also enabling fine-grained permission management:
		 *   - offering dialog-based gizmos only, but no generic functionality, prevents messing up data
		 *   - who needs to parse a bibliography in a treatment? or edit document metadata?
		 * - facilitates restricting individual users in Application mode ...
		 * - ... and thus enables fine-grained permission management:
		 *   - can hide Plazi server export unless some test is passed, for instance
		 * 
		 * ==> add protected (and thus overwritable) getUiFunctionFilter(HttpServletRequest request, String docId) method ...
		 *     ... providing custom UiFunctionFilter object
		 *     - allowPlugin(GoldenGateImaginePlugin), filtering on class name (observe super classes !!!)
		 *     - allowImageMarkupTool(ImageMarkupTool), filtering on IMT names
		 *     - allowSelectionAction(SelectionAction), filtering on SA names
		 *   - the filter depending on
		 *     - the (demo) document ID by default
		 *     - and also the user session in Community and Application modes
		 * ==> extend demo document list to facilitate specifying filter with demo document IDs
		 * ==> allow everything by default, though, to simplify all-out demos
		 * ==> move demo document list to XML to simplify filter specification ...
		 * ==> ... or allow both, deciding based on file extension of demoDocList parameter
		 * ==> in XML demo (Tutorial) mode, also allow to include _possibly_ interesting further features ...
		 * ==> ... flagged as 'advanced', and showing on respective context menu click ...
		 *   ==> under the hood, add menu item calling function setting UiFunctionFilter to advanced mode
		 *   ==> integrates seamlessly with current implementation
		 * ==> ... to allow eager users to fix somewhat off-topic problems they might spot and want to try fixing
		 * ==> in XML demo (tutorial) mode, also allow specifying tests:
		 *   - evaluate and report assignment success
		 *   - also helps with user qualification in Application and Community/Wiki modes
		 *   - execute and show on custom, off-menu button ...
		 *   - ... calling web document viewer under the hood
		 *   - use XML wrapper and ProcessTron for checks
		 *   ==> OR BETTER put 'Check Document' item in 'File' menu ...
		 *   ==> ... or 'Tools' menu, depending on mode
		 */
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