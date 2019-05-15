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
package de.uka.ipd.idaho.im.imagine.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.uka.ipd.idaho.easyIO.help.Help;
import de.uka.ipd.idaho.easyIO.help.HelpChapter;
import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.GenericGamtaXML;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessor;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessorManager;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentSaveOperation;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentSaver;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePluginDataProvider;
import de.uka.ipd.idaho.goldenGate.plugins.MonitorableDocumentProcessor;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceSplashScreen;
import de.uka.ipd.idaho.goldenGate.util.HelpChapterDataProviderBased;
import de.uka.ipd.idaho.goldenGate.util.ResourceDialog;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.gamta.ImDocumentRootOptionPanel;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageEditToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.SymbolTable;

/**
 * UI for displaying and editing one or multiple Image Markup documents, for
 * use in the UI of an application built around a GoldenGATE Imagine core. This
 * class provides multi-document capability, a main menu (including export
 * functionality), and view control. Document IO is up to sub classes.<br/>
 * By default, the UI panel contains the document markup panel (plain or in
 * tabs) and the main menu with view control in the <code>BorderLayout.CENTER</code>
 * and <code>BorderLayout.NORTH</code> positions, respectively. Client code,
 * mainly sub classes, may add other components around them if required.
 * 
 * @author sautter
 */
public abstract class ImageDocumentMarkupUI extends JPanel implements ImagingConstants, GoldenGateConstants {
	final GoldenGateImagine ggImagine;
	final Settings ggiConfig;
	
	private JMenuBar mainMenu = new JMenuBar();
	final JMenu undoMenu = new JMenu("Undo");
	final JCheckBoxMenuItem allowReactionPrompts = new JCheckBoxMenuItem("Prompt in Reaction to Input");
	
	private GoldenGatePluginDataProvider helpDataProvider;
	private HelpChapter helpContent;
	private Help help;
	private JMenu helpMenu;
	
	final JFileChooser fileChooser = new JFileChooser();
	
	final ViewControl viewControl = new ViewControl();
	private JTabbedPane docTabs = null;
	private ImageDocumentEditorTab docTab = null;
	
	/** Constructor
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 * @param doc the document to display (null activates multi-document mode)
	 * @param docName the name of the document to display
	 */
	protected ImageDocumentMarkupUI(GoldenGateImagine ggImagine, Settings ggiConfig, ImDocument doc, String docName) {
		super(new BorderLayout(), true);
		this.ggImagine = ggImagine;
		this.ggiConfig = ggiConfig;
		this.init((doc == null) ? null : new ImageDocumentEditorTab(this, doc, docName));
	}
	
	/** Constructor
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 * @param docTab the document tab to display (null activates multi-document mode)
	 */
	protected ImageDocumentMarkupUI(GoldenGateImagine ggImagine, Settings ggiConfig, ImageDocumentEditorTab docTab) {
		super(new BorderLayout(), true);
		this.ggImagine = ggImagine;
		this.ggiConfig = ggiConfig;
		if (docTab != null)
			docTab.setParent(this);
		this.init(docTab);
	}
	
	private void init(ImageDocumentEditorTab docTab) {
		
		//	configure file chooser
		this.fileChooser.setMultiSelectionEnabled(false);
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.fileChooser.setSelectedFile(new File((this.ggiConfig.getSetting("lastDocFolder", (new File(".")).getAbsolutePath())), " ")); // we need this dummy file name so the folder is actually opened instead of being selected in its parent folder
		
		//	build help first, as entries in other menus have to link up to it
		this.helpDataProvider = this.ggImagine.getHelpDataProvider();
		this.helpContent = this.buildHelpContentRoot();
		this.helpMenu = this.createHelpMenu();
		
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
			else mlIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./GgImagine.menus.cnfg")), "UTF-8"));
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
		
		//	build main menu
		this.addFileMenu(fileMenuItemNames);
		this.addExportMenu(exportMenuItemNames);
		this.addEditMenu(editMenuItemNames);
		this.addMenu(this.undoMenu);
		this.addToolsMenu(toolsMenuItemNames);
		
		//	finish help
		this.finishHelpMenu();
		this.help = new Help("GoldenGATE Imagine", this.helpContent, this.ggImagine.getGoldenGateIcon());
		
		//	build menu panel
		JPanel menuPanel = new JPanel(new BorderLayout(), true);
		menuPanel.add(this.mainMenu, BorderLayout.CENTER);
		menuPanel.add(this.viewControl, BorderLayout.EAST);
		
		//	build drop target
		DropTarget dropTarget = new DropTarget(this, new DropTargetAdapter() {
			public void drop(DropTargetDropEvent dtde) {
				dtde.acceptDrop(dtde.getDropAction());
				handleDrop(dtde.getTransferable());
			}
		});
		dropTarget.setActive(true);
		
		//	prepare document display
		final JComponent docComp;
		
		//	we are in multi-document mode, add tabs
		if (docTab == null) {
			this.docTabs = new JTabbedPane();
			docComp = this.docTabs;
			
			//	update UNDO menu and zoom control on tab changes, and notify listeners
			this.docTabs.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					ImageDocumentEditorTab idet = getActiveDocument();
					if (idet == null)
						return;
					idet.updateUndoMenu();
					viewControl.update(idet);
					ImageDocumentMarkupUI.this.ggImagine.notifyDocumentSelected(idet.getMarkupPanel().document);
				}
			});
		}
		
		//	we're in single-document mode, show document right away
		else {
			this.docTab = docTab;
			docComp = this.docTab;
		}
		
		//	make sure to focus document, not zoom control
		this.setFocusTraversalPolicy(new FocusTraversalPolicy() {
			public Component getComponentAfter(Container aContainer, Component aComponent) {
				return docComp;
			}
			public Component getComponentBefore(Container aContainer, Component aComponent) {
				return docComp;
			}
			public Component getFirstComponent(Container aContainer) {
				return docComp;
			}
			public Component getLastComponent(Container aContainer) {
				return docComp;
			}
			public Component getDefaultComponent(Container aContainer) {
				return docComp;
			}
		});
		
		//	make document views scroll on page-up and page-down
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "docScrollUp");
		docComp.getActionMap().put("docScrollUp", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					idet.scrollUp();
			}
		});
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "docScrollDown");
		docComp.getActionMap().put("docScrollDown", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					idet.scrollDown();
			}
		});
		
		//	trigger UNDO on Ctrl-Z
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "docUndo");
		docComp.getActionMap().put("docUndo", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				if (undoMenu.getMenuComponentCount() == 0)
					return;
				JMenuItem mi = ((JMenuItem) undoMenu.getMenuComponent(0));
				ActionListener[] miAls = mi.getActionListeners();
				for (int l = 0; l < miAls.length; l++)
					miAls[l].actionPerformed(ae);
			}
		});
		
		//	zoom in and out on Ctrl-<plus> and Ctrl-<minus>
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "docZoomIn");
		docComp.getActionMap().put("docZoomIn", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				viewControl.zoomIn();
			}
		});
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "docZoomOut");
		docComp.getActionMap().put("docZoomOut", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				viewControl.zoomOut();
			}
		});
		
		//	set document layout using Ctrl+<arrow-keys>
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK), "docPagesHorizontal");
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK), "docPagesHorizontal");
		docComp.getActionMap().put("docPagesHorizontal", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				viewControl.setSideBySidePages(0);
			}
		});
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK), "docPagesVertical");
		docComp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK), "docPagesVertical");
		docComp.getActionMap().put("docPagesVertical", new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				viewControl.setSideBySidePages(1);
			}
		});
		
		//	initialize most recently used symbols
		SymbolTable.setMostRecentlyUsedSymbols(this.ggiConfig.getSetting("mostRecentlyUsedSymbols", ""));
		
		//	assemble major parts
		this.add(menuPanel, BorderLayout.NORTH);
		this.add(docComp, BorderLayout.CENTER);
	}
	
	private void addFileMenu(ArrayList itemNames) {
		HelpChapter menuHelp = new HelpChapterDataProviderBased("Menu 'File'", this.helpDataProvider, "GgImagine.FileMenu.html");
		this.helpContent.addSubChapter(menuHelp);
		JMenuItem helpMi = new JMenuItem("Menu 'File'");
		helpMi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showHelp("Menu 'File'");
			}
		});
		this.helpMenu.add(helpMi);
		
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.ggImagine.getConfigurationName()))
			System.out.println("FILE-MENU");
		HashMap items = new LinkedHashMap() {
			public Object put(Object key, Object value) {
				if (LOCAL_MASTER_CONFIG_NAME.equals(ggImagine.getConfigurationName()))
					System.out.println(key);
				return super.put(key, value);
			}
		};
		JMenuItem mi;
		
		//	add built-in saving options
		mi = new JMenuItem("Save Document");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					idet.save();
			}
		});
		items.put(mi.getText(), mi);
		
		mi = new JMenuItem("Close Document");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					closeDocument(idet);
			}
		});
		items.put(mi.getText(), mi);
		
		//	offer selecting visible pages
		mi = new JMenuItem("Select Pages");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					idet.selectVisiblePages();
			}
		});
		items.put(mi.getText(), mi);
		
		//	add custom items
		JMenuItem[] mis = this.getFileMenuItems();
		for (int i = 0; i < mis.length; i++)
			items.put(mis[i].getText(), mis[i]);
		
		//	finally ...
		this.addMenu("File", itemNames, items);
	}
	
	/**
	 * Provide custom options for the 'File' menu. By default, the 'File' menu
	 * only contains three options, namely "Save Document", "Close Document",
	 * and "Select Pages". The former two delegating to the respective methods
	 * of this class with the selected document tab as the argument, the last
	 * delegates to the respective method of the displaying document. This
	 * default implementation returns an empty array, sub classes are welcome
	 * to overwrite it as needed.
	 * @return an array holding the menu items
	 */
	protected JMenuItem[] getFileMenuItems() {
		return new JMenuItem[0];
	}
	
	private void addExportMenu(ArrayList itemNames) {
		HelpChapter menuHelp = new HelpChapterDataProviderBased("Menu 'Export'", this.helpDataProvider, "GgImagine.ExportMenu.html");
		this.helpContent.addSubChapter(menuHelp);
		JMenuItem helpMi = new JMenuItem("Menu 'Export'");
		helpMi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showHelp("Menu 'Export'");
			}
		});
		this.helpMenu.add(helpMi);
		
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.ggImagine.getConfigurationName()))
			System.out.println("EXPORT-MENU");
		HashMap items = new LinkedHashMap() {
			public Object put(Object key, Object value) {
				if (LOCAL_MASTER_CONFIG_NAME.equals(ggImagine.getConfigurationName()))
					System.out.println(key);
				return super.put(key, value);
			}
		};
		JMenuItem mi;
		
		mi = new JMenuItem("Export XML");
		mi.addActionListener(new ActionListener() {
			private int xmlWrapperFlags = (ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS | ImDocumentRoot.NORMALIZE_CHARACTERS);
			private boolean exportIDs = false;
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				File likelyDest = getLikelyExportDestination(idet);
				if (likelyDest != null)
					fileChooser.setSelectedFile(likelyDest);
				
				ImDocumentRootOptionPanel idrop = new ImDocumentRootOptionPanel(this.xmlWrapperFlags);
				JCheckBox exportIDs = new JCheckBox("Export Annotation IDs", this.exportIDs);
				idrop.add(exportIDs);
				JPanel idropPosPanel = new JPanel(new BorderLayout());
				idropPosPanel.add(idrop, BorderLayout.SOUTH);
				fileChooser.setAccessory(idropPosPanel);
				
				int choice = fileChooser.showSaveDialog(ImageDocumentMarkupUI.this);
				fileChooser.setAccessory(null);
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				this.xmlWrapperFlags = idrop.getFlags();
				this.exportIDs = exportIDs.isSelected();
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					exportXml(idet.getMarkupPanel().document, file, this.xmlWrapperFlags, this.exportIDs);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		items.put(mi.getText(), mi);
//		
//		mi = new JMenuItem("As XML (without element IDs)");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				ImageDocumentEditorTab idet = getActiveDocument();
//				if (idet == null)
//					return;
//				clearFileFilters(fileChooser);
//				fileChooser.addChoosableFileFilter(xmlFileFilter);
//				File likelyFile = getLikelyExportDestination(idet);
//				if (likelyFile != null)
//					fileChooser.setSelectedFile(likelyFile);
//				if (fileChooser.showSaveDialog(ImageDocumentMarkupUI.this) != JFileChooser.APPROVE_OPTION)
//					return;
//				File file = fileChooser.getSelectedFile();
//				if (file.isDirectory())
//					return;
//				try {
//					exportXml(idet.getMarkupPanel().document, file, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS, false);
//				}
//				catch (IOException ioe) {
//					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
//					ioe.printStackTrace(System.out);
//				}
//			}
//		});
//		items.put(mi.getText(), mi);
//		
//		mi = new JMenuItem("As XML (with element IDs)");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				ImageDocumentEditorTab idet = getActiveDocument();
//				if (idet == null)
//					return;
//				clearFileFilters(fileChooser);
//				fileChooser.addChoosableFileFilter(xmlFileFilter);
//				File likelyFile = getLikelyExportDestination(idet);
//				if (likelyFile != null)
//					fileChooser.setSelectedFile(likelyFile);
//				if (fileChooser.showSaveDialog(ImageDocumentMarkupUI.this) != JFileChooser.APPROVE_OPTION)
//					return;
//				File file = fileChooser.getSelectedFile();
//				if (file.isDirectory())
//					return;
//				try {
//					exportXml(idet.getMarkupPanel().document, file, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS, true);
//				}
//				catch (IOException ioe) {
//					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
//					ioe.printStackTrace(System.out);
//				}
//			}
//		});
//		items.put(mi.getText(), mi);
//		
//		mi = new JMenuItem("As Raw XML (without element IDs)");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				ImageDocumentEditorTab idet = getActiveDocument();
//				if (idet == null)
//					return;
//				clearFileFilters(fileChooser);
//				fileChooser.addChoosableFileFilter(xmlFileFilter);
//				File likelyFile = getLikelyExportDestination(idet);
//				if (likelyFile != null)
//					fileChooser.setSelectedFile(likelyFile);
//				if (fileChooser.showSaveDialog(ImageDocumentMarkupUI.this) != JFileChooser.APPROVE_OPTION)
//					return;
//				File file = fileChooser.getSelectedFile();
//				if (file.isDirectory())
//					return;
//				try {
//					exportXml(idet.getMarkupPanel().document, file, (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS), false);
//				}
//				catch (IOException ioe) {
//					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
//					ioe.printStackTrace(System.out);
//				}
//			}
//		});
//		items.put(mi.getText(), mi);
//		
//		mi = new JMenuItem("As Raw XML (with element IDs)");
//		mi.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				ImageDocumentEditorTab idet = getActiveDocument();
//				if (idet == null)
//					return;
//				clearFileFilters(fileChooser);
//				fileChooser.addChoosableFileFilter(xmlFileFilter);
//				File likelyFile = getLikelyExportDestination(idet);
//				if (likelyFile != null)
//					fileChooser.setSelectedFile(likelyFile);
//				if (fileChooser.showSaveDialog(ImageDocumentMarkupUI.this) != JFileChooser.APPROVE_OPTION)
//					return;
//				File file = fileChooser.getSelectedFile();
//				if (file.isDirectory())
//					return;
//				try {
//					exportXml(idet.getMarkupPanel().document, file, (ImDocumentRoot.NORMALIZATION_LEVEL_RAW | ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS), true);
//				}
//				catch (IOException ioe) {
//					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
//					ioe.printStackTrace(System.out);
//				}
//			}
//		});
//		items.put(mi.getText(), mi);
		
		mi = new JMenuItem("Export GAMTA XML");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImageDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				File likelyDest = getLikelyExportDestination(idet);
				if (likelyDest != null)
					fileChooser.setSelectedFile(likelyDest);
				if (fileChooser.showSaveDialog(ImageDocumentMarkupUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					
					//	make sure file has appropriate extension
					if (!file.getName().toLowerCase().endsWith(".xml"))
						file = new File(file.toString() + ".xml");
					
					//	make way
					if (file.exists()) {
						String fileName = file.toString();
						File oldFile = new File(fileName + "." + System.currentTimeMillis() + ".old");
						file.renameTo(oldFile);
						file = new File(fileName);
					}
					
					//	export document
					ImDocumentRoot doc = new ImDocumentRoot(idet.getMarkupPanel().document, (ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS | ImDocumentRoot.SHOW_TOKENS_AS_WORD_ANNOTATIONS));
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
					GenericGamtaXML.storeDocument(doc, out);
					out.close();
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		items.put(mi.getText(), mi);
		
		//	add document exports from configuration
		DocumentSaver[] docSavers = this.ggImagine.getDocumentSavers();
		if (docSavers.length != 0) {
			for (int s = 0; s < docSavers.length; s++) {
				final DocumentSaver docSaver = docSavers[s];
				JMenuItem dsmi = docSavers[s].getSaveDocumentMenuItem();
				mi = new JMenuItem(dsmi.getText().replaceAll("Save", "Export"));
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImageDocumentEditorTab idet = getActiveDocument();
						if (idet == null)
							return;
						try {
							exportDocument(idet.getMarkupPanel().document, docSaver, idet.docName);
						}
						catch (IOException ioe) {
							JOptionPane.showMessageDialog(ImageDocumentMarkupUI.this, ("An error occurred while exporting the document via " + ((GoldenGatePlugin) docSaver).getPluginName() + ":\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
							ioe.printStackTrace(System.out);
						}
					}
				});
				items.put(mi.getText(), mi);
				
				//	add plugin specific help chapter if available
				HelpChapter docSaverHelp = ((GoldenGatePlugin) docSavers[s]).getHelp();
				if (docSaverHelp != null)
					menuHelp.addSubChapter(docSaverHelp);
			}
		}
		
		//	add dedicated exporters
		ImageDocumentExporter[] ides = this.ggImagine.getDocumentExporters();
		if (ides.length != 0) {
			for (int e = 0; e < ides.length; e++) {
				final ImageDocumentExporter ide = ides[e];
				mi = new JMenuItem(ide.getExportMenuLabel());
				mi.setToolTipText(ide.getExportMenuTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImageDocumentEditorTab idet = getActiveDocument();
						if (idet == null)
							return;
						File likelyDest = ((ide instanceof ImageDocumentFileExporter) ? getLikelyExportDestination(idet) : null);
						exportDocument(likelyDest, idet.getMarkupPanel().document, ide);
					}
				});
				items.put(mi.getText(), mi);
				
				//	add exporter specific help chapter if available
				HelpChapter ideHelp = ((GoldenGatePlugin) ides[e]).getHelp();
				if (ideHelp != null)
					menuHelp.addSubChapter(ideHelp);
			}
		}
		
		//	finally ...
		this.addMenu("Export", itemNames, items);
	}
	
	void exportDocument(final File likelyDest, final ImDocument doc, final ImageDocumentExporter ide) {
		
		//	get progress monitor
		final ResourceSplashScreen ss = new ResourceSplashScreen(this.getMainWindow(), "Exporting Document", "Plaease wait while exporting the document.", true, true);
		
		//	apply document processor, in separate thread
		Thread ideThread = new Thread() {
			public void run() {
				try {
					
					//	wait for splash screen progress monitor to come up (we must not reach the dispose() line before the splash screen even comes up)
					while (!ss.isVisible()) try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {}
					
					//	perform export
					if (ide instanceof ImageDocumentFileExporter)
						((ImageDocumentFileExporter) ide).exportDocument(likelyDest, doc, ss);
					else ide.exportDocument(doc, ss);
				}
				
				//	catch whatever might happen
				catch (Throwable t) {
					t.printStackTrace(System.out);
					JOptionPane.showMessageDialog(DialogFactory.getTopWindow(), ("An error occurred while exporting the document:\n" + t.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
				}
				
				//	clean up
				finally {
					
					//	dispose splash screen progress monitor
					ss.dispose();
				}
			}
		};
		ideThread.start();
		
		//	open splash screen progress monitor (this waits)
		ss.setVisible(true);
	}
	
	void exportXml(ImDocument doc, File file, int configFlags, boolean exportIDs) throws IOException {
		
		//	make sure file has appropriate extension
		if (!file.getName().toLowerCase().endsWith(".xml"))
			file = new File(file.toString() + ".xml");
		
		//	make way
		if (file.exists()) {
			String fileName = file.toString();
			File oldFile = new File(fileName + "." + System.currentTimeMillis() + ".old");
			file.renameTo(oldFile);
			file = new File(fileName);
		}
		
		//	export document
		ImDocumentRoot xmlDoc = new ImDocumentRoot(doc, configFlags);
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		AnnotationUtils.writeXML(xmlDoc, out, exportIDs);
		out.flush();
		out.close();
	}
	
	void exportDocument(ImDocument doc, DocumentSaver docSaver, String docName) throws IOException {
		
		//	obtain document save operation
		DocumentSaveOperation dso = docSaver.getSaveOperation(docName, null);
		if (dso == null)
			return;
		
		//	export file
		ImDocumentRoot xmlDoc = new ImDocumentRoot(doc, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS);
		xmlDoc.setShowTokensAsWordsAnnotations(true);
		dso.saveDocument(xmlDoc);
	}
	
	private void addEditMenu(ArrayList itemNames) {
		HelpChapter menuHelp = new HelpChapterDataProviderBased("Menu 'Edit'", this.helpDataProvider, "GgImagine.EditMenu.html");
		this.helpContent.addSubChapter(menuHelp);
		JMenuItem helpMi = new JMenuItem("Menu 'Edit'");
		helpMi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showHelp("Menu 'Edit'");
			}
		});
		this.helpMenu.add(helpMi);
		
		
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.ggImagine.getConfigurationName()))
			System.out.println("EDIT-MENU");
		HashMap items = new LinkedHashMap() {
			public Object put(Object key, Object value) {
				if (LOCAL_MASTER_CONFIG_NAME.equals(ggImagine.getConfigurationName()))
					System.out.println(key);
				return super.put(key, value);
			}
		};
		JMenuItem mi;
		
		items.put(this.allowReactionPrompts.getText(), this.allowReactionPrompts);
		
		ImageMarkupToolProvider[] imtps = this.ggImagine.getImageMarkupToolProviders();
		for (int p = 0; p < imtps.length; p++) {
			String[] emImtNames = imtps[p].getEditMenuItemNames();
			if ((emImtNames == null) || (emImtNames.length == 0))
				continue;
			for (int n = 0; n < emImtNames.length; n++) {
				final ImageMarkupTool emImt = imtps[p].getImageMarkupTool(emImtNames[n]);
				mi = new JMenuItem(emImt.getLabel());
				mi.setToolTipText(emImt.getTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImageDocumentEditorTab idet = getActiveDocument();
						if (idet != null)
							idet.getMarkupPanel().applyMarkupTool(emImt, null);
					}
				});
				items.put(mi.getText(), mi);
				
				//	add help chapter if available
				String imtHelpText = emImt.getHelpText();
				menuHelp.addSubChapter(new HelpChapter(emImt.getLabel(), ((imtHelpText == null) ? "Help is coming soon." : imtHelpText)));
			}
		}
		
		//	finally ...
		this.addMenu("Edit", itemNames, items);
	}
	
	private void addToolsMenu(ArrayList itemNames) {
		HelpChapter menuHelp = new HelpChapterDataProviderBased("Menu 'Tools'", this.helpDataProvider, "GgImagine.ToolsMenu.html");
		this.helpContent.addSubChapter(menuHelp);
		JMenuItem helpMi = new JMenuItem("Menu 'Tools'");
		helpMi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showHelp("Menu 'Tools'");
			}
		});
		this.helpMenu.add(helpMi);
		helpMi = null; // set to null to mark first entry of custom tool section
		
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.ggImagine.getConfigurationName()))
			System.out.println("TOOLS-MENU");
		HashMap items = new LinkedHashMap() {
			public Object put(Object key, Object value) {
				if (LOCAL_MASTER_CONFIG_NAME.equals(ggImagine.getConfigurationName()))
					System.out.println(key);
				return super.put(key, value);
			}
		};
		JMenuItem mi;
		
		ImageMarkupToolProvider[] imtps = this.ggImagine.getImageMarkupToolProviders();
		for (int p = 0; p < imtps.length; p++) {
			String[] tmImtNames = imtps[p].getToolsMenuItemNames();
			if ((tmImtNames == null) || (tmImtNames.length == 0))
				continue;
			for (int n = 0; n < tmImtNames.length; n++) {
				final ImageMarkupTool tmImt = imtps[p].getImageMarkupTool(tmImtNames[n]);
				mi = new JMenuItem(tmImt.getLabel());
				mi.setToolTipText(tmImt.getTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImageDocumentEditorTab idet = getActiveDocument();
						if (idet != null)
							idet.getMarkupPanel().applyMarkupTool(tmImt, null);
					}
				});
				items.put(mi.getText(), mi);
				
				//	add help menu entry (with separator before first IMT specific entry)
				if (helpMi == null)
					this.helpMenu.addSeparator();
				helpMi = new JMenuItem(tmImt.getLabel());
				helpMi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						showHelp(tmImt.getLabel());
					}
				});
				this.helpMenu.add(helpMi);
				
				//	add help chapter if available
				String imtHelpText = tmImt.getHelpText();
				menuHelp.addSubChapter(new HelpChapter(tmImt.getLabel(), ((imtHelpText == null) ? "Help is coming soon." : imtHelpText)));
			}
		}
		
		if (LOCAL_MASTER_CONFIG_NAME.equals(this.ggImagine.getConfigurationName())) {
			
			//	TODO make Plugins menu available (Analyzer hot reload, etc.) ==> simplifies testing
			
			final int[] xmlWrapperFlags = {(ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS | ImDocumentRoot.NORMALIZE_CHARACTERS)};
			mi = new JMenuItem("Configure XML Wrapper");
			mi.setToolTipText("Configure the XML wrapper document processors will work on");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					ImDocumentRootOptionPanel idrop = new ImDocumentRootOptionPanel(xmlWrapperFlags[0]);
					int choice = DialogFactory.confirm(idrop, "Configure XML Wrapper", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (choice == JOptionPane.OK_OPTION)
						xmlWrapperFlags[0] = idrop.getFlags();
				}
			});
			items.put(mi.getText(), mi);
			
			DocumentProcessorManager[] dpms = this.ggImagine.getDocumentProcessorProviders();
			for (int m = 0; m < dpms.length; m++) {
				final DocumentProcessorManager dpm = dpms[m];
				final String toolsMenuLabel = dpm.getToolsMenuLabel();
				if (toolsMenuLabel == null)
					continue;
				mi = new JMenuItem(toolsMenuLabel + " " + dpm.getResourceTypeLabel());
				mi.setToolTipText(toolsMenuLabel + " a " + dpm.getResourceTypeLabel() + " " + ("Run".equals(toolsMenuLabel) ? "on" : "to") + " the document");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImageDocumentEditorTab idet = getActiveDocument();
						if (idet == null)
							return;
						ResourceDialog rd = ResourceDialog.getResourceDialog(dpm, ("Select " + dpm.getResourceTypeLabel() + " To " + toolsMenuLabel), toolsMenuLabel);
						rd.setVisible(true);
						final String dpName = rd.getSelectedResourceName();
						if (dpName == null)
							return;
						idet.getMarkupPanel().applyMarkupTool(new ImageMarkupTool() {
							public String getLabel() {
								return (dpm.getResourceTypeLabel() + " '" + dpName + "'");
							}
							public String getTooltip() {
								return null; // no need for a tooltip here
							}
							public String getHelpText() {
								return null; // no need for a help text here
							}
							public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
								
								//	wrap document (or annotation)
								if (pm != null)
									pm.setStep("Wrapping document");
								ImDocumentRoot wrappedDoc = new ImDocumentRoot(doc, xmlWrapperFlags[0]);
								
								//	get document processor from manager
								if (pm != null)
									pm.setStep("Loading document processor");
								DocumentProcessor dp = dpm.getDocumentProcessor(dpName);
								
								//	create parameters
								Properties parameters = new Properties();
								parameters.setProperty(DocumentProcessor.INTERACTIVE_PARAMETER, DocumentProcessor.INTERACTIVE_PARAMETER);
								
								//	process document (or annotation)
								if (pm != null)
									pm.setStep("Processing document");
								if (dp instanceof MonitorableDocumentProcessor)
									((MonitorableDocumentProcessor) dp).process(wrappedDoc, parameters, pm);
								else dp.process(wrappedDoc, parameters);
							}
						}, null);
					}
				});
				items.put(mi.getText(), mi);
				
//				//	add help chapter if available SKIP THOSE, TOO GENERIC (MOSTLY ADMIN DOCUMENTATION)
//				HelpChapter dpmHelp = dpms[m].getHelp();
//				if (dpmHelp != null)
//					menuHelp.addSubChapter(dpmHelp);
			}
			
		}
		
		//	finally ...
		this.addMenu("Tools", itemNames, items);
	}
	
	private HelpChapter buildHelpContentRoot() {
		HelpChapter helpRoot = new HelpChapterDataProviderBased("GoldenGATE Imagine", this.helpDataProvider, "GgImagine.html");
		helpRoot.addSubChapter(new HelpChapterDataProviderBased("Glossary", this.helpDataProvider, "GgImagine.Glossary.html"));
		
		HelpChapter editorHelp = new HelpChapterDataProviderBased("Editor", this.helpDataProvider, "GgImagine.Editor.html");
		helpRoot.addSubChapter(editorHelp);
		SelectionActionProvider[] saps = this.ggImagine.getSelectionActionProviders();
		for (int p = 0; p < saps.length; p++) {
			HelpChapter sapHelp = saps[p].getHelp();
			if (sapHelp != null)
				editorHelp.addSubChapter(sapHelp);
		}
		
		ImageDocumentDropHandler[] dropHandlers = this.ggImagine.getDropHandlers();
		if (dropHandlers.length != 0) {
			HelpChapter dragDropHelp = new HelpChapterDataProviderBased("Drag & Drop", this.helpDataProvider, "GgImagine.DragDrop.html");
			helpRoot.addSubChapter(dragDropHelp);
			for (int h = 0; h < dropHandlers.length; h++) {
				HelpChapter dhHelp = dropHandlers[h].getHelp();
				if (dhHelp != null)
					dragDropHelp.addSubChapter(dhHelp);
			}
		}
		
		HelpChapter pageImageHelp = new HelpChapterDataProviderBased("Page Image Editing", this.helpDataProvider, "GgImagine.PageImageEditing.html");
		helpRoot.addSubChapter(pageImageHelp);
		ImageEditToolProvider[] ietps = this.ggImagine.getImageEditToolProviders();
		for (int p = 0; p < ietps.length; p++) {
			HelpChapter ietpHelp = ietps[p].getHelp();
			if (ietpHelp != null)
				pageImageHelp.addSubChapter(ietpHelp);
		}
		
		return helpRoot;
	}
	
	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem mi = new JMenuItem("Help");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				showHelp(null);
			}
		});
		helpMenu.add(mi);
		
		helpMenu.addSeparator();
		
		return helpMenu;
	}
	
	private void finishHelpMenu() {
		this.helpMenu.addSeparator();
		
		JMenuItem ami = new JMenuItem("About");
		ami.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ggImagine.showAbout();
			}
		});
		this.helpMenu.add(ami);
		
		JMenuItem rmi = new JMenuItem("View Readme");
		rmi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ggImagine.showReadme();
			}
		});
		this.helpMenu.add(rmi);
		
		this.addMenu(this.helpMenu);
	}
	
	/**
	 * Show some help information.
	 * @param on the subject of the desired help information
	 */
	public void showHelp(String on) {
		if (this.help != null)
			this.help.showHelp(on);
	}
	
	private class ViewControl extends JPanel {
		private JLabel scrollPosition = new JLabel("Page 0 of 0", JLabel.CENTER);
		private int pageImageDpi = ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI;
		private JComboBox zoomSelector = new JComboBox();
		private JComboBox layoutSelector = new JComboBox();
		ViewControl() {
			super(new GridLayout(1, 0), true);
			
			this.scrollPosition.setOpaque(true);
			this.scrollPosition.setBackground(Color.WHITE);
			this.scrollPosition.setBorder(BorderFactory.createLoweredBevelBorder());
			
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI / 4));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI / 3));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI / 2));
			this.zoomSelector.addItem(new ZoomLevel((ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI * 2) / 3));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI));
			this.zoomSelector.addItem(new ZoomLevel((ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI * 3) / 2));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI * 2));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI * 3));
			this.zoomSelector.addItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI * 4));
			this.zoomSelector.addItem(new ZoomLevel(0));
			this.zoomSelector.setSelectedItem(new ZoomLevel(ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI));
			this.zoomSelector.setEditable(false);
			this.zoomSelector.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					zoomChanged();
				}
			});
			
			this.layoutSelector.addItem("Pages Top-Down");
			this.layoutSelector.addItem("Pages Left-Right");
			this.layoutSelector.setSelectedItem("Pages Top-Down");
			this.layoutSelector.setEditable(false);
			this.layoutSelector.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					layoutChanged();
				}
			});
			
			this.add(this.scrollPosition);
			this.add(this.zoomSelector);
			this.add(this.layoutSelector);
		}
		
		void zoomIn() {
			int szi = this.zoomSelector.getSelectedIndex();
			if ((szi + 2) < this.zoomSelector.getItemCount()) // we have to block 'Original Resolution' at end of list
				this.zoomSelector.setSelectedIndex(szi + 1);
		}
		void zoomOut() {
			int szi = this.zoomSelector.getSelectedIndex();
			if (szi > 0)
				this.zoomSelector.setSelectedIndex(szi - 1);
		}
		void zoomChanged() {
			if (this.inUpdate)
				return;
			ImageDocumentEditorTab idet = getActiveDocument();
			if (idet == null)
				return;
			ZoomLevel zl = ((ZoomLevel) this.zoomSelector.getSelectedItem());
			this.inNotification = true;
			if (zl.dpi == 0)
				idet.setRenderingDpi(this.pageImageDpi);
			else idet.setRenderingDpi(zl.dpi);
			this.inNotification = false;
			KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
		}
		int getRenderingDpi() {
			return ((ZoomLevel) this.zoomSelector.getSelectedItem()).dpi;
		}
		
		void setSideBySidePages(int sbsp) {
			this.layoutSelector.setSelectedItem((sbsp == 1) ? "Pages Top-Down" : "Pages Left-Right");
		}
		void layoutChanged() {
			if (this.inUpdate)
				return;
			ImageDocumentEditorTab idet = getActiveDocument();
			if (idet == null)
				return;
			this.inNotification = true;
			idet.setSideBySidePages("Pages Top-Down".equals(this.layoutSelector.getSelectedItem()) ? 1 : 0);
			this.inNotification = false;
			KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
		}
		boolean isLeftRightLayout() {
			return "Pages Left-Right".equals(this.layoutSelector.getSelectedItem());
		}
		
		private boolean inUpdate = false;
		private boolean inNotification = false;
		void update(ImageDocumentEditorTab idet) {
			if (this.inNotification)
				return;
			this.inUpdate = true;
			idet.updateScrollPosition();
			this.pageImageDpi = idet.getMarkupPanel().getMaxPageImageDpi();
			this.zoomSelector.setSelectedItem(new ZoomLevel(idet.getMarkupPanel().getRenderingDpi()));
			this.layoutSelector.setSelectedItem((idet.getMarkupPanel().getSideBySidePages() == 1) ? "Pages Top-Down" : "Pages Left-Right");
			this.inUpdate = false;
		}
		
		private class ZoomLevel {
			final int dpi;
			ZoomLevel(int dpi) {
				this.dpi = dpi;
			}
			public String toString() {
				if (this.dpi == 0)
					return "Original Resolution";
				else return (((this.dpi * 100) / ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI) + "%");
			}
			public boolean equals(Object obj) {
				return ((obj instanceof ZoomLevel) && (((ZoomLevel) obj).dpi == this.dpi));
			}
		}
	}
	
	private void addMenu(String name, ArrayList itemNames, HashMap items) {
		
		//	build menu
		JMenu menu = new JMenu(name);
		JMenuItem mi;
		boolean lastWasItem = false;
		
		//	add configured items first
		for (int i = 0; i < itemNames.size(); i++) {
			String itemName = ((String) itemNames.get(i));
			
			//	add separator
			if ("---".equals(itemName)) {
				if (lastWasItem)
					menu.addSeparator();
				lastWasItem = false;
				continue;
			}
			
			//	add menu item
			mi = ((JMenuItem) items.remove(itemName));
			if (mi != null) {
				menu.add(mi);
				lastWasItem = true;
			}
		}
		
		//	add remaining items
		if (lastWasItem && (items.size() != 0)) {
			menu.addSeparator();
			lastWasItem = false;
		}
		for (Iterator init = items.keySet().iterator(); init.hasNext();) {
			String itemName = ((String) init.next());
			
			//	add separator
			if ("---".equals(itemName) && init.hasNext()) {
				if (lastWasItem)
					menu.addSeparator();
				lastWasItem = false;
				continue;
			}
			
			//	add menu item
			mi = ((JMenuItem) items.get(itemName));
			if (mi != null) {
				menu.add(mi);
				lastWasItem = true;
			}
		}
		
		//	finally ...
		this.addMenu(menu);
	}
	
	void addMenu(JMenu menu) {
		this.mainMenu.add(menu);
	}
	
	/* TODO add page navigator to editor tabs
	 * - represent pages a thumbnails
	 * - pages stacked vertical --> navigator on left edge, scrolling top down
	 * - pages side-by-side --> navigator at bottom, scrolling left right
	 * - click on page thumbnail --> scroll directly to that page
	 * 
	 * - when document navigator visible, scroll along with main editor window ...
	 * - ... but let document navigator scroll by itself without scrolling main editor window (obviously ...)
	 *
	 * - when page numbers are added to or removed from pages, update page thumbnails in document navigator
	 */
	
	/* TODO allow users a look at some page even when in a sub dialog
	 * - offer showPage() method in ImDocumentMarkupPanel (doing nothing by default) ...
	 * - ... and overwrite that in GgImagineUI to actually scroll to that page
	 * - JavaDoc: "if an instance of this class sits inside a JScrollPane, this method can be overwritten to trigger a scroll" 
	 */
	
	/**
	 * Display tab for a single document in the markup UI.
	 * 
	 * @author sautter
	 */
	public static class ImageDocumentEditorTab extends ImageDocumentMarkupPanel {
		private ImageDocumentMarkupUI parent;
		private String docName;
		
		/** Constructor
		 * @param parent the parent UI (for callbacks)
		 * @param doc the document to display
		 * @param docName the name of the document
		 */
		protected ImageDocumentEditorTab(ImDocument doc, String docName, GoldenGateImagine ggImagine, Settings ggiConfig) {
			super(doc, ggImagine, ggiConfig);
			this.docName = docName;
		}
		
		/** Constructor
		 * @param parent the parent UI (for callbacks)
		 * @param doc the document to display
		 * @param docName the name of the document
		 */
		protected ImageDocumentEditorTab(ImageDocumentMarkupUI parent, ImDocument doc, String docName) {
			super(doc, parent.ggImagine, parent.ggiConfig);
			this.docName = docName;
			this.setParent(parent);
		}
		
		void setParent(ImageDocumentMarkupUI parent) {
			this.parent = parent;
			
			//	set document view to current configuration
			int renderingDpi = this.parent.viewControl.getRenderingDpi();
			if ((0 < renderingDpi) && (renderingDpi != ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI))
				this.setRenderingDpi(renderingDpi);
			if (this.parent.viewControl.isLeftRightLayout())
				this.setSideBySidePages(0);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#setRenderingDpi(int)
		 */
		public void setRenderingDpi(int renderingDpi) {
			super.setRenderingDpi(renderingDpi);
			this.parent.viewControl.update(this);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#setSideBySidePages(int)
		 */
		public void setSideBySidePages(int sbsp) {
			super.setSideBySidePages(sbsp);
			this.parent.viewControl.update(this);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#getUndoMenu()
		 */
		protected JMenu getUndoMenu() {
			return this.parent.undoMenu;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#allowReactionPrompts()
		 */
		protected boolean allowReactionPrompts() {
			return this.parent.allowReactionPrompts.isSelected();
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#scrollPositionChanged(java.lang.String)
		 */
		protected void scrollPositionChanged(String posLabel) {
			this.parent.viewControl.scrollPosition.setText(posLabel);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#getMainWindow()
		 */
		protected Window getMainWindow() {
			return this.parent.getMainWindow();
		}
		
		/**
		 * Get the (current) name of the content document.
		 * @return the document name
		 */
		public String getDocName() {
			return this.docName;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#handleDrop(java.awt.datatransfer.Transferable)
		 */
		protected void handleDrop(Transferable dropped) {
			this.parent.handleDrop(dropped);
		}
		
		/**
		 * Save the content document of the editor tab. This default
		 * implementation delegates to the <code>saveDocument()</code> method
		 * of the surrounding markup UI. Sub classes may provide more options.
		 * @return true if the document was saved, false otherwise
		 */
		public boolean save() {
			if (!this.isDirty())
				return true;
			else return this.parent.saveDocument(this);
		}
		
		/**
		 * Notify the editor tab that its content document was saved under a
		 * specific name. This implementation updates the editor tab title and
		 * marks the document as clean. Sub classes overwriting this method to
		 * take further actions thus have to make the super call.
		 * @param saveDocName the name the content document was saved under
		 */
		public void savedAs(String saveDocName) {
			this.markClean();
			this.docName = saveDocName;
			this.parent.documentNameChanged(this);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupPanel#dispose(boolean)
		 */
		public void dispose(boolean storeSettings) {
			super.dispose(storeSettings);
			this.parent.removeDocument(this);
		}
	}
	
	/**
	 * Open a document editor tab to show in the markup UI.
	 * @param idet the editor tab to add
	 */
	public void openDocument(ImageDocumentEditorTab idet) {
		if (this.docTabs == null)
			throw new IllegalStateException("Cannot add document editor tab in single-document mode.");
		idet.setParent(this);
		this.docTabs.addTab(idet.getDocName(), idet);
		this.docTabs.setSelectedComponent(idet);
	}
	
	void removeDocument(ImageDocumentEditorTab idet) {
		if (this.docTabs != null)
			this.docTabs.remove(idet);
		else if (idet == this.docTab)
			this.docTab = null;
	}
	
	/**
	 * Retrieve the editor tab holding currently selected document. The runtime
	 * type of the argument editor tab is whatever sub classes hand to
	 * <code>openDocument()</code> or the constructor that takes a tab as an
	 * argument.
	 * @return the editor tab holding the currently selected document
	 */
	public ImageDocumentEditorTab getActiveDocument() {
		if (this.docTabs == null)
			return this.docTab;
		else return ((this.docTabs.getComponentCount() == 0) ? null : ((ImageDocumentEditorTab) this.docTabs.getSelectedComponent()));
	}
	
	/**
	 * Close the displaying document. This implementation handles UI cleanup
	 * as well as on-demand saving. Sub classes overwriting this method to take
	 * further action thus must make the super call, best before performing any
	 * cleanup themselves.
	 * @return true if the document was actually closed, false otherwise
	 */
	public boolean closeDocument(ImageDocumentEditorTab idet) {
		if (idet == null)
			return true;
		
		//	save document if dirty
		if (idet.isDirty()) {
			int choice = JOptionPane.showConfirmDialog(ImageDocumentMarkupUI.this, ("Document '" + idet.getDocName() + "' has un-saved changes. Save them before closing it?"), "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			if (choice == JOptionPane.YES_OPTION) {
				if (!idet.save())
					return false;
			}
		}
		idet.dispose(true);
		
		//	finally ...
		return true;
	}
	
	/**
	 * Close the markup UI. This method first closes all open documents and
	 * aborts if one remains open. Sub classes overwriting this method to take
	 * further action thus must make the super call, best before performing any
	 * cleanup themselves.
	 */
	public boolean close() {
		while (this.getActiveDocument() != null) {
			if (!this.closeDocument(this.getActiveDocument()))
				return false;
		}
		this.ggiConfig.setSetting("mostRecentlyUsedSymbols", SymbolTable.getMostRecentlyUsedSymbols());
		return true;
	}
	
	/**
	 * Retrieve the application main window, i.e., the one to set pop-ups and
	 * splash screens modal to. This default implementation simply returns the
	 * current top window. Sub classes are welcome to overwrite this behavior
	 * with a more sophisticated approach.
	 * @return the application main window
	 */
	protected Window getMainWindow() {
		return DialogFactory.getTopWindow();
	}
	
	/**
	 * Handle a drop on the markup panel that did not go to any of the present
	 * drop handlers. This default implementation does nothing. Sub classes are
	 * welcome to overwrite it as needed.
	 * @param dropped the dropped data
	 */
	protected void handleDrop(Transferable dropped) {}
	
	/**
	 * Handle a change to a document name, mainly after saving. The default
	 * implementation updates the title of the argument tab. Sub classes may
	 * overwrite it to take other actions. The runtime type of the argument
	 * editor tab is whatever sub classes hand to <code>openDocument()</code>
	 * or the constructor that takes a tab as an argument.
	 * @param idet the editor tab whose content name changed
	 */
	protected void documentNameChanged(ImageDocumentEditorTab idet) {
		if (this.docTabs != null)
			this.docTabs.setTitleAt(this.docTabs.indexOfComponent(idet), idet.getDocName());
	}
	
	/**
	 * Save the contents of a document editor tab. Implementations may only
	 * return true if the content of the argument editor tab was actually saved
	 * to persistent storage. The runtime type of the argument editor tab is
	 * whatever sub classes hand to <code>openDocument()</code> or the
	 * constructor that takes a tab as an argument.
	 * @param idet the document editor tab whose content to save
	 * @return true if the document was saved, false otherwise
	 */
	protected abstract boolean saveDocument(ImageDocumentEditorTab idet);
	
	/**
	 * Predict the export destination for the document displayed in an editor
	 * tab. This is used to initialize the file selection dialog. This default
	 * implementation returns null, indicating "no prediction possible". Sub
	 * classes are welcome to overwrite it as needed. The runtime type of the
	 * argument editor tab is whatever sub classes hand to
	 * <code>openDocument()</code> or the constructor that takes a tab as an
	 * argument.
	 * @param idet the document editor tab whose content will be exported
	 * @return a file pointing to the likely export destination
	 */
	protected File getLikelyExportDestination(ImageDocumentEditorTab idet) {
		return null;
	}
	
	private static final FileFilter xmlFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".xml"));
		}
		public String getDescription() {
			return "XML Documents";
		}
	};
	private static void clearFileFilters(JFileChooser fileChooser) {
		FileFilter[] fileFilters = fileChooser.getChoosableFileFilters();
		for (int f = 0; f < fileFilters.length; f++)
			fileChooser.removeChoosableFileFilter(fileFilters[f]);
	}
}