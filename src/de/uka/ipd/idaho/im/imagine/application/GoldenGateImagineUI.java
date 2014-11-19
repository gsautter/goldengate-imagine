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
package de.uka.ipd.idaho.im.imagine.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.AnnotationUtils;
import de.uka.ipd.idaho.gamta.util.ControllingProgressMonitor;
import de.uka.ipd.idaho.gamta.util.GenericGamtaXML;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.swing.ProgressMonitorPanel;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessor;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessorManager;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentSaveOperation;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentSaver;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.goldenGate.util.ResourceDialog;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImDocument.ImDocumentListener;
import de.uka.ipd.idaho.im.ImObject;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageEditToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.PagePoint;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.PageThumbnail;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.TwoClickActionMessenger;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.TwoClickSelectionAction;
import de.uka.ipd.idaho.im.util.ImImageEditorPanel.ImImageEditTool;
import de.uka.ipd.idaho.im.util.ImfIO;
import de.uka.ipd.idaho.im.util.SymbolTable;

/**
 * Default GUI for GoldenGATE Imagine
 * 
 * @author sautter
 */
public class GoldenGateImagineUI extends JFrame implements ImagingConstants, GoldenGateConstants {
	
	private Settings config;
	
	private JMenuBar mainMenu = new JMenuBar();
	private JMenu undoMenu = new JMenu("Undo");
	
	private ViewControl viewControl = new ViewControl();
	
	private JFileChooser fileChooser = new JFileChooser();
	
	private PdfExtractor pdfExtractor;
	
	private JTabbedPane docTabs = new JTabbedPane();
	
	private GoldenGateImagine ggImagine;
	
	GoldenGateImagineUI(GoldenGateImagine ggImagine, Settings config) {
		super("GoldenGATE Imagine - " + ggImagine.getConfigurationName());
		this.ggImagine = ggImagine;
		this.config = config;
		
		//	get PDF reader
		this.pdfExtractor = this.ggImagine.getPdfExtractor();
		
		//	configure file chooser
		this.fileChooser.setMultiSelectionEnabled(false);
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.fileChooser.setSelectedFile(new File((this.config.getSetting("lastDocFolder", (new File(".")).getAbsolutePath())), " ")); // we need this dummy file name so the folder is actually opened instead of being selected in its parent folder
		
		//	build main menu
		this.addFileMenu();
		this.addExportMenu();
		this.addEditMenu();
		this.addMenu(this.undoMenu);
		this.addToolsMenu();
		
		//	build zoom control
		this.viewControl = new ViewControl();
		
		//	build menu panel
		JPanel menuPanel = new JPanel(new BorderLayout(), true);
		menuPanel.add(this.mainMenu, BorderLayout.CENTER);
		menuPanel.add(this.viewControl, BorderLayout.EAST);
		
		//	build drop target
		DropTarget dropTarget = new DropTarget(this, new DropTargetAdapter() {
			public void drop(DropTargetDropEvent dtde) {
				dtde.acceptDrop(dtde.getDropAction());
				Transferable transfer = dtde.getTransferable();
				DataFlavor[] dataFlavors = transfer.getTransferDataFlavors();
				for (int d = 0; d < dataFlavors.length; d++) {
					System.out.println(dataFlavors[d].toString());
					System.out.println(dataFlavors[d].getRepresentationClass());
					try {
						Object transferData = transfer.getTransferData(dataFlavors[d]);
						System.out.println(transferData.getClass().getName());
						
						List transferList = ((List) transferData);
						if (transferList.isEmpty())
							return;
						
						for (int t = 0; t < transferList.size(); t++) {
							File droppedFile = ((File) transferList.get(t));
							try {
								FileFilter matchFileFilter;
								if (imfFileFilter.accept(droppedFile))
									matchFileFilter = imfFileFilter;
								else if (genericPdfFileFilter.accept(droppedFile))
									matchFileFilter = genericPdfFileFilter;
								else continue;
								InputStream in = new BufferedInputStream(new FileInputStream(droppedFile));
								loadDocument(droppedFile, matchFileFilter, in);
								in.close();
							}
							catch (IOException ioe) {
								System.out.println("Error opening document '" + droppedFile.getAbsolutePath() + "':\n   " + ioe.getClass().getName() + " (" + ioe.getMessage() + ")");
								ioe.printStackTrace(System.out);
								JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("Could not open file '" + droppedFile.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Opening File", JOptionPane.ERROR_MESSAGE);
							}
							catch (SecurityException se) {
								System.out.println("Error opening document '" + droppedFile.getName() + "':\n   " + se.getClass().getName() + " (" + se.getMessage() + ")");
								se.printStackTrace(System.out);
								JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("Not allowed to open file '" + droppedFile.getName() + "':\n" + se.getMessage() + "\n\nIf you are currently running GoldenGATE Editor as an applet, your\nbrowser's security mechanisms might prevent reading files from your local disc."), "Not Allowed To Open File", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					catch (UnsupportedFlavorException ufe) {
						ufe.printStackTrace(System.out);
					}
					catch (IOException ioe) {
						ioe.printStackTrace(System.out);
					}
					catch (Exception e) {
						e.printStackTrace(System.out);
					}
				}
			}
		});
		dropTarget.setActive(true);
		
		//	update UNDO menu and zoom control on tab changes
		this.docTabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				System.out.println("Tab selected: " + idet.file.getName());
				updateUndoMenu(idet.undoActions);
				updateViewControl(idet);
			}
		});
		
		//	initialize most recently used symbols
		SymbolTable.setMostRecentlyUsedSymbols(this.config.getSetting("mostRecentlyUsedSymbols", ""));
		
		//	assemble major parts
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(menuPanel, BorderLayout.NORTH);
		this.getContentPane().add(this.docTabs, BorderLayout.CENTER);
		this.setSize(1000, 800);
		this.setLocationRelativeTo(null);
	}
	
	private void addFileMenu() {
		JMenu menu = new JMenu("File");
		JMenuItem mi;
		
		mi = new JMenuItem("Open Document");
		mi.addActionListener(new ActionListener() {
			private FileFilter loadFileFilter = null;
			public void actionPerformed(ActionEvent ae) {
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(imfFileFilter);
				fileChooser.addChoosableFileFilter(genericPdfFileFilter);
				fileChooser.addChoosableFileFilter(textPdfFileFilter);
				fileChooser.addChoosableFileFilter(imagePdfFileFilter);
				if (this.loadFileFilter != null)
					fileChooser.setFileFilter(this.loadFileFilter);
				if (fileChooser.showOpenDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				this.loadFileFilter = fileChooser.getFileFilter();
				try {
					InputStream in = new BufferedInputStream(new FileInputStream(file));
					loadDocument(file, this.loadFileFilter, in);
					in.close();
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while loading a document from '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Loading Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("Save Document");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				if (idet.save())
					return;
				idet.saveAs(fileChooser);
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("Save Document As");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				idet.saveAs(fileChooser);
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("Close Document");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeDocument();
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("Exit");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				exit();
			}
		});
		menu.add(mi);
		
		menu.addSeparator();
		mi = new JMenuItem("Select Pages");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet != null)
					idet.selectVisiblePages();
			}
		});
		menu.add(mi);
		
		this.addMenu(menu);
	}
	
	private void addExportMenu() {
		JMenu menu = new JMenu("Export");
		JMenuItem mi;
		
		mi = new JMenuItem("As XML (without element IDs)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				if (fileChooser.showSaveDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					exportXml(idet.idmp.document, file, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS, false, false);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("As XML (with element IDs)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				if (fileChooser.showSaveDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					exportXml(idet.idmp.document, file, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS, true, false);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("As Raw XML (without element IDs)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				if (fileChooser.showSaveDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					exportXml(idet.idmp.document, file, ImDocumentRoot.NORMALIZATION_LEVEL_RAW, false, true);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("As Raw XML (with element IDs)");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				if (fileChooser.showSaveDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					exportXml(idet.idmp.document, file, ImDocumentRoot.NORMALIZATION_LEVEL_RAW, true, true);
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		mi = new JMenuItem("As GAMTA XML");
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				ImDocumentEditorTab idet = getActiveDocument();
				if (idet == null)
					return;
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(xmlFileFilter);
				if (fileChooser.showSaveDialog(GoldenGateImagineUI.this) != JFileChooser.APPROVE_OPTION)
					return;
				File file = fileChooser.getSelectedFile();
				if (file.isDirectory())
					return;
				try {
					ImDocumentRoot doc = new ImDocumentRoot(idet.idmp.document, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS);
					doc.setUseRandomAnnotationIDs(false);
					doc.setShowTokensAsWordsAnnotations(true);
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
					GenericGamtaXML.storeDocument(doc, out);
					out.close();
				}
				catch (IOException ioe) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document to '" + file.getAbsolutePath() + "':\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace(System.out);
				}
			}
		});
		menu.add(mi);
		
		//	add document exports from configuration
		DocumentSaver[] docSavers = this.ggImagine.getDocumentSavers();
		if (docSavers.length != 0) {
			menu.addSeparator();
			for (int s = 0; s < docSavers.length; s++) {
				final DocumentSaver docSaver = docSavers[s];
				JMenuItem dsmi = docSavers[s].getSaveDocumentMenuItem();
				mi = new JMenuItem(dsmi.getText().replaceAll("Save", "Export"));
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImDocumentEditorTab idet = getActiveDocument();
						if (idet == null)
							return;
						try {
							exportDocument(idet.idmp.document, docSaver, idet.file.getName());
						}
						catch (IOException ioe) {
							JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while exporting the document via " + ((GoldenGatePlugin) docSaver).getPluginName() + ":\n" + ioe.getMessage()), "Error Exporting Document", JOptionPane.ERROR_MESSAGE);
							ioe.printStackTrace(System.out);
						}
					}
				});
				menu.add(mi);
			}
		}
		
		//	add dedicated exporters
		ImageDocumentExporter[] ides = this.ggImagine.getDocumentExporters();
		if (ides.length != 0) {
			menu.addSeparator();
			for (int e = 0; e < ides.length; e++) {
				final ImageDocumentExporter ide = ides[e];
				mi = new JMenuItem(ide.getExportMenuLabel());
				mi.setToolTipText(ide.getExportMenuTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImDocumentEditorTab idet = getActiveDocument();
						if (idet != null)
							exportDocument(idet.idmp.document, ide);
					}
				});
				menu.add(mi);
			}
		}
		
		//	finally ...
		this.addMenu(menu);
	}
	
	private void exportDocument(final ImDocument doc, final ImageDocumentExporter ide) {
		
		//	get progress monitor
		final SplashScreen ss = new SplashScreen(this, "Exporting Document", "Plaease wait while exporting the document.", true, true);
		
		//	apply document processor, in separate thread
		Thread ideThread = new Thread() {
			public void run() {
				try {
					
					//	wait for splash screen progress monitor to come up (we must not reach the dispose() line before the splash screen even comes up)
					while ((ss instanceof Dialog) && !((Dialog) ss).isVisible()) try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {}
					
					//	apply image markup tool
					ide.exportDocument(doc, ss);
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
	
	private void exportXml(ImDocument doc, File file, int normalizationLevel, boolean exportIDs, boolean exportWords) throws IOException {
		ImDocumentRoot xmlDoc = new ImDocumentRoot(doc, normalizationLevel);
		xmlDoc.setUseRandomAnnotationIDs(false);
		xmlDoc.setShowTokensAsWordsAnnotations(exportWords);
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		AnnotationUtils.writeXML(xmlDoc, out, exportIDs);
		out.close();
	}
	
	private void exportDocument(ImDocument doc, DocumentSaver docSaver, String docName) throws IOException {
		DocumentSaveOperation dso = docSaver.getSaveOperation(docName, null);
		if (dso == null)
			return;
		ImDocumentRoot xmlDoc = new ImDocumentRoot(doc, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS);
		xmlDoc.setUseRandomAnnotationIDs(false);
		xmlDoc.setShowTokensAsWordsAnnotations(true);
		dso.saveDocument(xmlDoc);
	}
	
	private void addEditMenu() {
		JMenu menu = new JMenu("Edit");
		JMenuItem mi;
		
		ImageMarkupToolProvider[] imtps = this.ggImagine.getImageMarkupToolProviders();
		for (int p = 0; p < imtps.length; p++) {
			String[] emImtNames = imtps[p].getEditMenuItemNames();
			if ((emImtNames == null) || (emImtNames.length == 0))
				continue;
			if (menu.getMenuComponentCount() != 0)
				menu.addSeparator();
			for (int n = 0; n < emImtNames.length; n++) {
				final ImageMarkupTool emImt = imtps[p].getImageMarkupTool(emImtNames[n]);
				mi = new JMenuItem(emImt.getLabel());
				mi.setToolTipText(emImt.getTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImDocumentEditorTab idet = getActiveDocument();
						if (idet != null)
							idet.idmp.applyMarkupTool(emImt, null);
					}
				});
				menu.add(mi);
			}
		}
		
		if (menu.getMenuComponentCount() != 0)
			this.addMenu(menu);
	}
	
	private void addToolsMenu() {
		JMenu menu = new JMenu("Tools");
		JMenuItem mi;
		
		ImageMarkupToolProvider[] imtps = this.ggImagine.getImageMarkupToolProviders();
		for (int p = 0; p < imtps.length; p++) {
			String[] tmImtNames = imtps[p].getToolsMenuItemNames();
			if ((tmImtNames == null) || (tmImtNames.length == 0))
				continue;
			if (menu.getMenuComponentCount() != 0)
				menu.addSeparator();
			for (int n = 0; n < tmImtNames.length; n++) {
				final ImageMarkupTool tmImt = imtps[p].getImageMarkupTool(tmImtNames[n]);
				mi = new JMenuItem(tmImt.getLabel());
				mi.setToolTipText(tmImt.getTooltip());
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ImDocumentEditorTab idet = getActiveDocument();
						if (idet != null)
							idet.idmp.applyMarkupTool(tmImt, null);
					}
				});
				menu.add(mi);
			}
		}
		
		DocumentProcessorManager[] dpms = this.ggImagine.getDocumentProcessorProviders();
		if ((dpms.length != 0 ) && (menu.getMenuComponentCount() != 0))
			menu.addSeparator();
		for (int m = 0; m < dpms.length; m++) {
			final DocumentProcessorManager dpm = dpms[m];
			final String toolsMenuLabel = dpm.getToolsMenuLabel();
			if (toolsMenuLabel == null)
				continue;
			mi = new JMenuItem(toolsMenuLabel + " " + dpm.getResourceTypeLabel());
			mi.setToolTipText(toolsMenuLabel + " a " + dpm.getResourceTypeLabel() + " " + ("Run".equals(toolsMenuLabel) ? "on" : "to") + " the document");
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					ImDocumentEditorTab idet = getActiveDocument();
					if (idet == null)
						return;
					ResourceDialog rd = ResourceDialog.getResourceDialog(dpm, ("Select " + dpm.getResourceTypeLabel() + " To " + toolsMenuLabel), toolsMenuLabel);
					rd.setVisible(true);
					final String dpName = rd.getSelectedResourceName();
					if (dpName == null)
						return;
					idet.idmp.applyMarkupTool(new ImageMarkupTool() {
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
							ImDocumentRoot wrappedDoc = new ImDocumentRoot(doc, ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS);
							
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
							dp.process(wrappedDoc, parameters);
						}
					}, null);
				}
			});
			menu.add(mi);
		}
		
		if (menu.getMenuComponentCount() != 0)
			this.addMenu(menu);
	}
	
	private class ViewControl extends JPanel {
		private int pageImageDpi = ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI;
		private JComboBox zoomSelector = new JComboBox();
		private JComboBox layoutSelector = new JComboBox();
		ViewControl() {
			super(new BorderLayout(), true);
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
			
			this.add(this.zoomSelector, BorderLayout.WEST);
			this.add(this.layoutSelector, BorderLayout.EAST);
		}
		public void zoomChanged() {
			if (this.inUpdate)
				return;
			ImDocumentEditorTab idet = getActiveDocument();
			if (idet == null)
				return;
			ZoomLevel zl = ((ZoomLevel) this.zoomSelector.getSelectedItem());
			if (zl.dpi == 0)
				idet.setRenderingDpi(this.pageImageDpi);
			else idet.setRenderingDpi(zl.dpi);
		}
		int getRenderingDpi() {
			return ((ZoomLevel) this.zoomSelector.getSelectedItem()).dpi;
		}
		public void layoutChanged() {
			if (this.inUpdate)
				return;
			ImDocumentEditorTab idet = getActiveDocument();
			if (idet == null)
				return;
			idet.setSideBySidePages("Pages Top-Down".equals(this.layoutSelector.getSelectedItem()) ? 1 : 0);
		}
		boolean isLeftRightLayout() {
			return "Pages Left-Right".equals(this.layoutSelector.getSelectedItem());
		}
		boolean inUpdate = false;
		void update(ImDocumentEditorTab idet) {
			this.inUpdate = true;
			this.pageImageDpi = idet.idmp.getMaxPageImageDpi();
			this.zoomSelector.setSelectedItem(new ZoomLevel(idet.idmp.getRenderingDpi()));
			this.layoutSelector.setSelectedItem((idet.idmp.getSideBySidePages() == 1) ? "Pages Top-Down" : "Pages Left-Right");
			this.inUpdate = false;
		}
		private class ZoomLevel {
			final int dpi;
			ZoomLevel(int dpi) {
				this.dpi = dpi;
			}
			public String toString() {
				if (this.dpi == 0)
					return "Scanned Resolution";
				else return (((this.dpi * 100) / ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI) + "%");
			}
			public boolean equals(Object obj) {
				return ((obj instanceof ZoomLevel) && (((ZoomLevel) obj).dpi == this.dpi));
			}
		}
	}
	
	void addMenu(JMenu menu) {
		this.mainMenu.add(menu);
	}
	
	void updateViewControl(ImDocumentEditorTab idet) {
		this.viewControl.update(idet);
	}
	
	void updateUndoMenu(final LinkedList undoActions) {
		this.undoMenu.removeAll();
		for (Iterator uait = undoActions.iterator(); uait.hasNext();) {
			final UndoAction ua = ((UndoAction) uait.next());
			JMenuItem mi = new JMenuItem(ua.label);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					while (undoActions.size() != 0) {
						UndoAction eua = ((UndoAction) undoActions.removeFirst());
						eua.execute();
						if (eua == ua)
							break;
					}
					updateUndoMenu(undoActions);
					ua.target.idmp.validate();
					ua.target.idmp.repaint();
					ua.target.idmp.validateControlPanel();
				}
			});
			this.undoMenu.add(mi);
			if (this.undoMenu.getMenuComponentCount() >= 10)
				break;
		}
		this.undoMenu.setEnabled(undoActions.size() != 0);
	}
	
	void loadDocument(final File file, final FileFilter fileFilter, final InputStream in) throws IOException {
		this.config.setSetting("lastDocFolder", file.getParentFile().getAbsolutePath());
		
		if (file.getName().toLowerCase().endsWith(".imf")) {
			ImDocument doc = ImfIO.loadDocument(in);
			doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, file.getName());
			this.addDocument(new ImDocumentEditorTab(GoldenGateImagineUI.this, file, doc));
			in.close();
			return;
		}
		
		if (file.getName().toLowerCase().endsWith(".pdf")) {
			final IOException[] loadException = {null};
			final SplashScreen loadScreen = new SplashScreen(this, ("Loading PDF '" + file.getName() + "'"), "", true, false);
			Thread loadThread = new Thread() {
				public void run() {
					try {
						loadScreen.setStep("Loading PDF data");
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int read;
						while ((read = in.read(buffer, 0, buffer.length)) != -1)
							baos.write(buffer, 0, read);
						in.close();
						
						ImDocument doc;
						if (fileFilter == textPdfFileFilter)
							doc = pdfExtractor.loadTextPdf(baos.toByteArray(), loadScreen);
						else if (fileFilter == imagePdfFileFilter)
							doc = pdfExtractor.loadImagePdf(baos.toByteArray(), loadScreen);
						else doc = pdfExtractor.loadGenericPdf(baos.toByteArray(), loadScreen);
						doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, file.getName());
						addDocument(new ImDocumentEditorTab(GoldenGateImagineUI.this, file, doc));
					}
					catch (IOException ioe) {
						loadException[0] = ioe;
					}
					finally {
						loadScreen.dispose();
					}
				}
			};
			loadThread.start();
			loadScreen.setVisible(true);
			if (loadException[0] == null)
				return;
			else throw loadException[0];
		}
	}
	
	void addDocument(ImDocumentEditorTab idet) {
		this.docTabs.addTab(idet.getDocumentName(), idet);
	}
	
	void removeDocument(ImDocumentEditorTab idet) {
		this.docTabs.remove(idet);
	}
	
	ImDocumentEditorTab getActiveDocument() {
		return ((this.docTabs.getComponentCount() == 0) ? null : ((ImDocumentEditorTab) this.docTabs.getSelectedComponent()));
	}
	
	boolean closeDocument() {
		ImDocumentEditorTab idet = getActiveDocument();
		if (idet == null)
			return true;
		if (idet.isDirty()) {
			int choice = JOptionPane.showConfirmDialog(GoldenGateImagineUI.this, ("Document '" + idet.getDocumentName() + "' has un-saved changes. Save them before closing it?"), "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.CANCEL_OPTION)
				return false;
			if (choice == JOptionPane.YES_OPTION) {
				if (!idet.save() && !idet.saveAs(this.fileChooser))
					return false;
			}
		}
		idet.close();
		return true;
	}
	
	void exit() {
		while (this.getActiveDocument() != null) {
			if (!this.closeDocument())
				return;
		}
		this.ggImagine.exit();
		this.config.setSetting("mostRecentlyUsedSymbols", SymbolTable.getMostRecentlyUsedSymbols());
		this.dispose();
	}
	
	private static abstract class UndoAction {
		final String label;
		final ImDocumentEditorTab target;
		final int modCount;
		UndoAction(String label, ImDocumentEditorTab target) {
			this.label = label;
			this.target = target;
			this.modCount = this.target.modCount;
		}
		final void execute() {
			try {
				this.target.inUndoAction = true;
				this.doExecute();
				this.target.modCount = this.modCount;
			}
			finally {
				this.target.inUndoAction = false;
			}
		}
		abstract void doExecute();
	}
	
	private static class MultipartUndoAction extends UndoAction {
		LinkedList parts = new LinkedList();
		MultipartUndoAction(String label, ImDocumentEditorTab target) {
			super(label, target);
		}
		void addUndoAction(UndoAction ua) {
			this.parts.addFirst(ua);
		}
		void doExecute() {
			while (this.parts.size() != 0)
				((UndoAction) this.parts.removeFirst()).doExecute();
		}
	}
	
	private static class ImDocumentEditorTab extends JPanel {
		GoldenGateImagineUI parent;
		File file;
		ImDocumentMarkupPanel idmp;
		JScrollPane idmpBox;
		
		ImDocumentListener undoRecorder;
		LinkedList undoActions = new LinkedList();
		MultipartUndoAction multipartUndoAction = null;
		boolean inUndoAction = false;
		
		int modCount = 0;
		int savedModCount = 0;
		
		ImDocumentEditorTab(GoldenGateImagineUI parent, File source, ImDocument doc) {
			super(new BorderLayout(), true);
			this.parent = parent;
			this.file = source;
			this.idmp = new ImDocumentMarkupPanel(doc) {
				public void beginAtomicAction(String label) {
					super.beginAtomicAction(label);
					startMultipartUndoAction(label);
				}
				public void endAtomicAction() {
					super.endAtomicAction();
					finishMultipartUndoAction();
				}
				protected SelectionAction[] getActions(ImWord start, ImWord end) {
					LinkedList actions = new LinkedList(Arrays.asList(super.getActions(start, end)));
					SelectionActionProvider[] saps = ImDocumentEditorTab.this.parent.ggImagine.getSelectionActionProviders();
					for (int p = 0; p < saps.length; p++) {
						SelectionAction[] sas = saps[p].getActions(start, end, this);
						if ((sas != null) && (sas.length != 0)) {
							if (actions.size() != 0)
								actions.add(SelectionAction.SEPARATOR);
							actions.addAll(Arrays.asList(sas));
						}
					}
					return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
				}
				protected SelectionAction[] getActions(ImPage page, Point start, Point end) {
					LinkedList actions = new LinkedList(Arrays.asList(super.getActions(page, start, end)));
					SelectionActionProvider[] saps = ImDocumentEditorTab.this.parent.ggImagine.getSelectionActionProviders();
					for (int p = 0; p < saps.length; p++) {
						SelectionAction[] sas = saps[p].getActions(start, end, page, this);
						if ((sas != null) && (sas.length != 0)) {
							if (actions.size() != 0)
								actions.add(SelectionAction.SEPARATOR);
							actions.addAll(Arrays.asList(sas));
						}
					}
					return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
				}
				protected ImImageEditTool[] getImageEditTools() {
					LinkedList tools = new LinkedList(Arrays.asList(super.getImageEditTools()));
					ImageEditToolProvider[] ietps = ImDocumentEditorTab.this.parent.ggImagine.getImageEditToolProviders();
					for (int p = 0; p < ietps.length; p++) {
						ImImageEditTool[] iets = ietps[p].getImageEditTools();
						if (iets != null)
							tools.addAll(Arrays.asList(iets));
					}
					return ((ImImageEditTool[]) tools.toArray(new ImImageEditTool[tools.size()]));
				}
				public ProgressMonitor getProgressMonitor(String title, String text, boolean supportPauseResume, boolean supportAbort) {
					return new SplashScreen(ImDocumentEditorTab.this.parent, title, text, supportPauseResume, supportAbort);
				}
				public void setPageVisible(int pageId, boolean pv) {
					if (pv == this.isPageVisible(pageId))
						return;
					super.setPageVisible(pageId, pv);
					ImDocumentEditorTab.this.validate();
					ImDocumentEditorTab.this.repaint();
				}
				public void setPagesVisible(int fromPageId, int toPageId, boolean pv) {
					boolean pageVisibilityUnchanged = true;
					for (int p = fromPageId; p <= toPageId; p++)
						if (pv != this.isPageVisible(p)) {
							pageVisibilityUnchanged = false;
							break;
						}
					if (pageVisibilityUnchanged)
						return;
					super.setPagesVisible(fromPageId, toPageId, pv);
					ImDocumentEditorTab.this.validate();
					ImDocumentEditorTab.this.repaint();
				}
				public void setVisiblePages(int[] visiblePageIDs) {
					boolean pageVisibilityUnchanged = true;
					HashSet visiblePageIdSet = new HashSet();
					for (int i = 0; i < visiblePageIDs.length; i++)
						visiblePageIdSet.add(new Integer(visiblePageIDs[i]));
					for (int p = 0; p < this.document.getPageCount(); p++)
						if (this.isPageVisible(p) != visiblePageIdSet.contains(new Integer(p))) {
							pageVisibilityUnchanged = false;
							break;
						}
					if (pageVisibilityUnchanged)
						return;
					super.setVisiblePages(visiblePageIDs);
					ImDocumentEditorTab.this.validate();
					ImDocumentEditorTab.this.repaint();
				}
				public void setSideBySidePages(int sbsp) {
					if (sbsp == this.getSideBySidePages())
						return;
					super.setSideBySidePages(sbsp);
					ImDocumentEditorTab.this.validate();
					ImDocumentEditorTab.this.repaint();
				}
			};
			
			//	inject highlight colors for annotations, regions, and text streams
			Settings annotationColors = this.parent.config.getSubset("annotation.color");
			String[] annotationTypes = annotationColors.getKeys();
			for (int t = 0; t < annotationTypes.length; t++) {
				Color ac = GoldenGateImagine.getColor(annotationColors.getSetting(annotationTypes[t]));
				if (ac != null)
					this.idmp.setAnnotationColor(annotationTypes[t], ac);
			}
			Settings layoutObjectColors = this.parent.config.getSubset("layoutObject.color");
			String[] layoutObjectTypes = layoutObjectColors.getKeys();
			for (int t = 0; t < layoutObjectTypes.length; t++) {
				Color loc = GoldenGateImagine.getColor(layoutObjectColors.getSetting(layoutObjectTypes[t]));
				if (loc != null)
					this.idmp.setLayoutObjectColor(layoutObjectTypes[t], loc);
			}
			Settings textStreamColors = this.parent.config.getSubset("textStream.color");
			String[] textStreamTypes = textStreamColors.getKeys();
			for (int t = 0; t < textStreamTypes.length; t++) {
				Color tsc = GoldenGateImagine.getColor(textStreamColors.getSetting(textStreamTypes[t]));
				if (tsc != null)
					this.idmp.setTextStreamTypeColor(textStreamTypes[t], tsc);
			}
			
			//	TODO also use this for REDO logging
			//	prepare recording UNDO actions
			this.undoRecorder = new ImDocumentListener() {
				public void typeChanged(final ImObject object, final String oldType) {
					if (inUndoAction)
						return;
					addUndoAction(new UndoAction(("Change Object Type to '" + object.getType() + "'"), ImDocumentEditorTab.this) {
						void doExecute() {
							object.setType(oldType);
						}
					});
				}
				public void regionAdded(final ImRegion region) {
					if (inUndoAction)
						return;
					addUndoAction(new UndoAction(("Add '" + region.getType() + "' Region"), ImDocumentEditorTab.this) {
						void doExecute() {
							idmp.document.getPage(region.pageId).removeRegion(region);
						}
					});
				}
				public void regionRemoved(final ImRegion region) {
					if (inUndoAction)
						return;
					if (region instanceof ImWord)
						addUndoAction(new UndoAction(("Remove Word '" + region.getAttribute(ImWord.STRING_ATTRIBUTE) + "'"), ImDocumentEditorTab.this) {
							void doExecute() {
								idmp.document.getPage(region.pageId).addWord((ImWord) region);
							}
						});
					else addUndoAction(new UndoAction(("Remove '" + region.getType() + "' Region"), ImDocumentEditorTab.this) {
						void doExecute() {
							idmp.document.getPage(region.pageId).addRegion(region);
						}
					});
				}
				public void attributeChanged(final ImObject object, final String attributeName, final Object oldValue) {
					if (inUndoAction)
						return;
					if (oldValue == null)
						addUndoAction(new UndoAction(("Add " + attributeName + " Attribute to " + object.getType()), ImDocumentEditorTab.this) {
							void doExecute() {
								object.removeAttribute(attributeName);
							}
						});
					else if (object.getAttribute(attributeName) == null)
						addUndoAction(new UndoAction(("Remove '" + attributeName + "' Attribute from " + object.getType()), ImDocumentEditorTab.this) {
							void doExecute() {
								object.setAttribute(attributeName, oldValue);
							}
						});
					else addUndoAction(new UndoAction(("Change '" + attributeName + "' Attribute of " + object.getType() + " to '" + object.getAttribute(attributeName).toString() + "'"), ImDocumentEditorTab.this) {
						void doExecute() {
							object.setAttribute(attributeName, oldValue);
						}
					});
				}
				public void annotationAdded(final ImAnnotation annotation) {
					if (inUndoAction)
						return;
					addUndoAction(new UndoAction(("Add '" + annotation.getType() + "' Annotation"), ImDocumentEditorTab.this) {
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
								idmp.document.removeAnnotation(annots[a]);
								break;
							}
						}
					});
				}
				public void annotationRemoved(final ImAnnotation annotation) {
					if (inUndoAction)
						return;
					addUndoAction(new UndoAction(("Remove '" + annotation.getType() + "' Annotation"), ImDocumentEditorTab.this) {
						void doExecute() {
							idmp.document.addAnnotation(annotation.getFirstWord(), annotation.getLastWord(), annotation.getType()).copyAttributes(annotation);
						}
					});
				}
			};
			this.idmp.document.addDocumentListener(this.undoRecorder);
			
			//	get drop handlers
			final ImageDocumentDropHandler[] dropHandlers = this.parent.ggImagine.getDropHandlers();
			
			//	add drop target if any drop handlers present
			if (dropHandlers.length != 0) {
				DropTarget dropTarget = new DropTarget(this.idmp, new DropTargetAdapter() {
					public void drop(DropTargetDropEvent dtde) {
						PagePoint dpp = idmp.pagePointAt(dtde.getLocation().x, dtde.getLocation().y);
						if (dpp == null)
							return;
						dtde.acceptDrop(dtde.getDropAction()); // we can do this only once, and we have to do it before inspecting data
						for (int h = 0; h < dropHandlers.length; h++) try {
							if (dropHandlers[h].handleDrop(idmp, dpp.page, dpp.x, dpp.y, dtde))
								break;
						}
						catch (Exception e) {
							e.printStackTrace(System.out);
						}
					}
				});
				dropTarget.setActive(true);
			}
			
			//	assemble UI components
			int renderingDpi = this.parent.viewControl.getRenderingDpi();
			if ((0 < renderingDpi) && (renderingDpi != ImDocumentMarkupPanel.DEFAULT_RENDERING_DPI))
				this.idmp.setRenderingDpi(renderingDpi);
			if (this.parent.viewControl.isLeftRightLayout())
				this.idmp.setSideBySidePages(0);
			this.idmpBox = new JScrollPane();
			this.idmpBox.getVerticalScrollBar().setUnitIncrement(50);
			this.idmpBox.getVerticalScrollBar().setBlockIncrement(50);
			this.idmpBox.setViewport(new IdvpViewport(this.idmp));
			this.add(this.idmpBox, BorderLayout.CENTER);
			this.add(this.idmp.getControlPanel(), BorderLayout.EAST);
		}
		
		void setRenderingDpi(int renderingDpi) {
			int oldRenderingDpi = this.idmp.getRenderingDpi();
			if (renderingDpi == oldRenderingDpi)
				return;
			Dimension viewSize = this.idmpBox.getViewport().getExtentSize();
			Point oldViewPos = this.idmpBox.getViewport().getViewPosition();
			Point oldViewCenter = new Point((oldViewPos.x + (viewSize.width / 2)), (oldViewPos.y + (viewSize.height / 2)));
			this.idmp.setRenderingDpi(renderingDpi);
			this.validate();
			this.repaint();
			Point newViewCenter = new Point(((oldViewCenter.x * renderingDpi) / oldRenderingDpi), ((oldViewCenter.y * renderingDpi) / oldRenderingDpi));
			Point newViewPos = new Point(Math.max((newViewCenter.x - (viewSize.width / 2)), 0), Math.max((newViewCenter.y - (viewSize.height / 2)), 0));
			this.idmpBox.getViewport().setViewPosition(newViewPos);
		}
		
		void setSideBySidePages(int sbsp) {
			int oldSbsp = this.idmp.getSideBySidePages();
			if (sbsp == oldSbsp)
				return;
			Dimension viewSize = this.idmpBox.getViewport().getExtentSize();
			Point viewPos = this.idmpBox.getViewport().getViewPosition();
			Point viewCenter = new Point((viewPos.x + (viewSize.width / 2)), (viewPos.y + (viewSize.height / 2)));
			Component viewCenterPage = this.idmp.getComponentAt(viewCenter);
			while (((viewCenterPage == null) || (viewCenterPage == this.idmp)) && (viewCenter.x > 0) && (viewCenter.y > 0)) {
				viewCenter = new Point((viewCenter.x - 20), (viewCenter.y - 20));
				viewCenterPage = this.idmp.getComponentAt(viewCenter);
			}
			this.idmp.setSideBySidePages(sbsp);
			this.validate();
			this.repaint();
			if (viewCenterPage != null)
				this.idmpBox.getViewport().setViewPosition(viewCenterPage.getLocation());
		}
		
		void selectVisiblePages() {
			
			//	create selector tiles and compute size
			PageSelectorTile[] psts = new PageSelectorTile[idmp.document.getPageCount()];
			int ptWidth = 0;
			int ptHeight = 0;
			for (int p = 0; p < psts.length; p++) {
				PageThumbnail pt = this.idmp.getPageThumbnail(p);
				psts[p] = new PageSelectorTile(pt, this.idmp.isPageVisible(p));
				ptWidth = Math.max(ptWidth, pt.getPreferredSize().width);
				ptHeight = Math.max(ptHeight, pt.getPreferredSize().height);
			}
			
			//	set selector tile size (adding 4 for border width)
			for (int p = 0; p < psts.length; p++)
				psts[p].setPreferredSize(new Dimension(((ptWidth * 2) + 4), ((ptHeight * 2) + 4)));
			
			//	create dialog
			final DialogPanel vps = new DialogPanel("Select Visible Pages", true);
			vps.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			vps.setSize(vps.getOwner().getSize());
			vps.setLocationRelativeTo(vps.getOwner());
			
			//	compute number of selector tiles that fit side by side
			int sideBySidePsts = ((vps.getSize().width + 10) / (((ptWidth * 2) + 4) + 10));
			
			//	line up selector tiles
			JPanel pstPanel = new JPanel(new GridBagLayout(), true);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets.left = 5;
			gbc.insets.right = 5;
			gbc.insets.top = 5;
			gbc.insets.bottom = 5;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.gridx = 0;
			gbc.gridy = 0;
			for (int p = 0; p < psts.length; p++) {
				pstPanel.add(psts[p], gbc.clone());
				gbc.gridx++;
				if (gbc.gridx == sideBySidePsts) {
					gbc.gridx = 0;
					gbc.gridy++;
				}
			}
			gbc.gridwidth = Math.min(psts.length, sideBySidePsts);
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy++;
			pstPanel.add(new JPanel(), gbc.clone());
			JScrollPane pstPanelBox = new JScrollPane(pstPanel);
			pstPanelBox.getVerticalScrollBar().setUnitIncrement(50);
			pstPanelBox.getVerticalScrollBar().setBlockIncrement(50);
			
			//	add buttons
			final boolean[] cancelled = {false};
			JButton ok = new JButton("OK");
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					vps.dispose();
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					cancelled[0] = true;
					vps.dispose();
				}
			});
			JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
			buttons.add(ok);
			buttons.add(cancel);
			
			//	assemble dialog content
			vps.add(pstPanelBox, BorderLayout.CENTER);
			vps.add(buttons, BorderLayout.SOUTH);
			
			//	show dialog
			vps.setVisible(true);
			
			//	cancelled
			if (cancelled[0])
				return;
			
			//	select visible pages
			int[] visiblePageIDs = new int[psts.length];
			for (int p = 0; p < psts.length; p++)
				visiblePageIDs[p] = (psts[p].pageVisible ? p : -1);
			this.idmp.setVisiblePages(visiblePageIDs);
		}
		
		private class PageSelectorTile extends JPanel {
			private PageThumbnail pt;
			boolean pageVisible;
			PageSelectorTile(PageThumbnail pt, boolean pageVisible) {
				super(new BorderLayout(), true);
				this.pt = pt;
				this.pageVisible = pageVisible;
				this.setBorder();
				this.setToolTipText(this.pt.getTooltipText());
				this.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent me) {
						togglePageVisible();
					}
				});
			}
			void setBorder() {
				this.setBorder(BorderFactory.createLineBorder((this.pageVisible ? Color.DARK_GRAY : Color.LIGHT_GRAY), 2));
			}
			public void paint(Graphics g) {
				super.paint(g);
				this.pt.paint(g, 2, 2, (this.getWidth()-4), (this.getHeight()-4), this);
			}
			void togglePageVisible() {
				this.pageVisible = !this.pageVisible;
				this.setBorder();
				this.validate();
				this.repaint();
			}
		}
		
		private void addUndoAction(UndoAction ua) {
			if (this.inUndoAction)
				return;
			if (this.multipartUndoAction == null) {
				this.modCount++;
				this.undoActions.addFirst(ua);
				this.parent.updateUndoMenu(this.undoActions);
			}
			else this.multipartUndoAction.addUndoAction(ua);
		}
		
		private void startMultipartUndoAction(String label) {
			this.multipartUndoAction = new MultipartUndoAction(label, this);
		}
		
		private void finishMultipartUndoAction() {
			if ((this.multipartUndoAction != null) && (this.multipartUndoAction.parts.size() != 0)) {
				this.modCount++;
				this.undoActions.addFirst(this.multipartUndoAction);
				this.parent.updateUndoMenu(this.undoActions);
			}
			this.multipartUndoAction = null;
		}
		
		private static class IdvpViewport extends JViewport implements TwoClickActionMessenger {
			private static Color halfTransparentRed = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
			private ImDocumentMarkupPanel idvp;
			private String tcaMessage = null;
			IdvpViewport(ImDocumentMarkupPanel idvp) {
				this.idvp = idvp;
				this.idvp.setTwoClickActionMessenger(this);
				this.setView(this.idvp);
				this.setOpaque(false);
			}
			public void twoClickActionChanged(TwoClickSelectionAction tcsa) {
				this.tcaMessage = ((tcsa == null) ? null : tcsa.getActiveLabel());
				this.validate();
				this.repaint();
			}
			public void paint(Graphics g) {
				super.paint(g);
				if (this.tcaMessage == null)
					return;
				Font f = new Font("Helvetica", Font.PLAIN, 20);
				g.setFont(f);
				TextLayout wtl = new TextLayout(this.tcaMessage, f, ((Graphics2D) g).getFontRenderContext());
				g.setColor(halfTransparentRed);
				g.fillRect(0, 0, this.getViewRect().width, ((int) Math.ceil(wtl.getBounds().getHeight() + (wtl.getDescent() * 3))));
				g.setColor(Color.white);
				((Graphics2D) g).drawString(this.tcaMessage, ((this.getViewRect().width - wtl.getAdvance()) / 2), ((int) Math.ceil(wtl.getBounds().getHeight() + wtl.getDescent())));
			}
		}
		
		String getDocumentName() {
			if (this.file != null)
				return this.file.getName();
			else return ((String) this.idmp.document.getAttribute(DOCUMENT_NAME_ATTRIBUTE, "Unknown Document"));
		}
		
		boolean isDirty() {
			return (this.modCount != this.savedModCount);
		}
		
		boolean save() {
			return (this.isDirty() ? this.saveAs(this.file) : true);
		}
		
		boolean saveAs(JFileChooser fileChooser) {
			clearFileFilters(fileChooser);
			fileChooser.addChoosableFileFilter(imfFileFilter);
			if (this.file != null)
				fileChooser.setSelectedFile(this.file);
			if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return false;
			File file = fileChooser.getSelectedFile();
			if (file.isDirectory())
				return false;
			return this.saveAs(file);
		}
		
		boolean saveAs(File file) {
			
			//	check file name
			if (!file.getName().endsWith(".imf"))
				file = new File(file.getAbsolutePath() + ".imf");
			
			//	create splash screen
			final SplashScreen saveSplashScreen = new SplashScreen(this.parent, "Saving Document, Please Wait", "", false, false);
			
			//	save document, in separate thread
			final boolean[] saveSuccess = {false};
			final File[] saveFile = {file};
			Thread saveThread = new Thread() {
				public void run() {
					try {
						
						//	wait for splash screen to come up (we must not reach the dispose() line before the splash screen even comes up)
						while (!saveSplashScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	make way
						if (saveFile[0].exists()) {
							String fileName = saveFile[0].getAbsolutePath();
							saveFile[0].renameTo(new File(fileName + "." + System.currentTimeMillis() + ".old"));
							saveFile[0] = new File(fileName);
						}
						
						//	save document
						OutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile[0]));
						ImfIO.storeDocument(ImDocumentEditorTab.this.idmp.document, out);
						out.flush();
						out.close();
						ImDocumentEditorTab.this.savedModCount = ImDocumentEditorTab.this.modCount;
						ImDocumentEditorTab.this.file = saveFile[0];
						ImDocumentEditorTab.this.parent.docTabs.setTitleAt(ImDocumentEditorTab.this.parent.docTabs.indexOfComponent(ImDocumentEditorTab.this), ImDocumentEditorTab.this.file.getName());
						saveSuccess[0] = true;
					}
					
					//	catch whatever might happen
					catch (Throwable t) {
						JOptionPane.showMessageDialog(ImDocumentEditorTab.this, ("An error occurred while saving the document to '" + saveFile[0].getAbsolutePath() + "':\n" + t.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
						t.printStackTrace(System.out);
					}
					
					//	dispose splash screen
					finally {
						saveSplashScreen.dispose();
					}
				}
			};
			saveThread.start();
			
			//	open splash screen (this waits)
			saveSplashScreen.setVisible(true);
			
			//	finally ...
			return saveSuccess[0];
		}
		
		void close() {
			this.idmp.document.removeDocumentListener(this.undoRecorder);
			this.parent.removeDocument(this);
			Settings annotationColors = this.parent.config.getSubset("annotation.color");
			String[] annotationTypes = this.idmp.getAnnotationTypes();
			for (int t = 0; t < annotationTypes.length; t++) {
				Color ac = this.idmp.getAnnotationColor(annotationTypes[t]);
				if (ac != null)
					annotationColors.setSetting(annotationTypes[t], GoldenGateImagine.getHex(ac));
			}
			Settings layoutObjectColors = this.parent.config.getSubset("layoutObject.color");
			String[] layoutObjectTypes = this.idmp.getLayoutObjectTypes();
			for (int t = 0; t < layoutObjectTypes.length; t++) {
				Color loc = this.idmp.getLayoutObjectColor(layoutObjectTypes[t]);
				if (loc != null)
					layoutObjectColors.setSetting(layoutObjectTypes[t], GoldenGateImagine.getHex(loc));
			}
			Settings textStreamColors = this.parent.config.getSubset("textStream.color");
			String[] textStreamTypes = this.idmp.getTextStreamTypes();
			for (int t = 0; t < textStreamTypes.length; t++) {
				Color tsc = this.idmp.getTextStreamTypeColor(textStreamTypes[t]);
				if (tsc != null)
					textStreamColors.setSetting(textStreamTypes[t], GoldenGateImagine.getHex(tsc));
			}
		}
	}
	
	private static final FileFilter imfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".imf"));
		}
		public String getDescription() {
			return "Image Markup Files";
		}
	};
	private static final FileFilter genericPdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents";
		}
	};
	private static final FileFilter textPdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (born-digital)";
		}
	};
	private static final FileFilter imagePdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (scanned)";
		}
	};
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
	
	private static class SplashScreen extends JDialog implements ControllingProgressMonitor {
		private JLabel textLabel = new JLabel("Please wait while GoldenGATE Resource is running ...", JLabel.LEFT);
		private ProgressMonitorPanel pmp;
		
		SplashScreen(JFrame owner, String title, String text, boolean supportPauseResume, boolean supportAbort) {
			super(owner, title, true);
			this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			this.pmp = new ProgressMonitorPanel(supportPauseResume, supportAbort);
			
			this.setLayout(new BorderLayout());
			this.add(this.textLabel, BorderLayout.NORTH);
			this.add(this.pmp, BorderLayout.CENTER);
			
			this.setSize(new Dimension(400, ((supportPauseResume || supportAbort) ? 150 : 130)));
			this.setLocationRelativeTo(owner);
			if (text != null)
				this.setText(text);
		}
		
		public void setAbortExceptionMessage(String aem) {
			this.pmp.setAbortExceptionMessage(aem);
		}
		public boolean supportsAbort() {
			return this.pmp.supportsAbort();
		}
		public void setAbortEnabled(boolean ae) {
			this.pmp.setAbortEnabled(ae);
		}
		public boolean supportsPauseResume() {
			return this.pmp.supportsPauseResume();
		}
		public void setPauseResumeEnabled(boolean pre) {
			this.pmp.setPauseResumeEnabled(pre);
		}
		public void setStep(String step) {
			this.pmp.setStep(step);
		}
		public void setInfo(String info) {
			this.pmp.setInfo(info);
		}
		public void setBaseProgress(int baseProgress) {
			this.pmp.setBaseProgress(baseProgress);
		}
		public void setMaxProgress(int maxProgress) {
			this.pmp.setMaxProgress(maxProgress);
		}
		public void setProgress(int progress) {
			this.pmp.setProgress(progress);
		}
		/**
		 * Set the text displayed on the label of the splash screen
		 * @param text the new text
		 */
		public void setText(String text) {
			this.textLabel.setText((text == null) ? "" : text);
		}
	}
}