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
package de.uka.ipd.idaho.im.imagine.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.easyIO.streams.PeekInputStream;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.gamta.util.swing.ProgressMonitorDialog;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceSplashScreen;
import de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI;
import de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay;
import de.uka.ipd.idaho.goldenGate.ui.NamedElementUsageStatistics;
import de.uka.ipd.idaho.goldenGate.ui.UserInterfaceUtils;
import de.uka.ipd.idaho.goldenGate.ui.WindowMenuBar;
import de.uka.ipd.idaho.goldenGate.ui.WindowMenuElement;
import de.uka.ipd.idaho.goldenGate.ui.WindowMenuFunction;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener.CancelSavingException;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentIoProvider;
import de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupUI;
import de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupUI.ImageDocumentEditorTab;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.CustomFontDecoderCharset;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.FontDecoderCharset;
import de.uka.ipd.idaho.im.util.ImDocumentData;
import de.uka.ipd.idaho.im.util.ImDocumentData.DataBackedImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentData.FolderImDocumentData;
import de.uka.ipd.idaho.im.util.ImDocumentIO;

/**
 * Default GUI for GoldenGATE Imagine
 * 
 * @author sautter
 */
public class GoldenGateImagineUI extends JFrame implements ImagingConstants, GoldenGateConstants {
	private GoldenGATE goldenGate;
	private GoldenGateImagine ggImagine;
	
	private GgiDoumentMarkupUI ui;
	
	private JFileChooser fileChooser = new JFileChooser();
	private Dimension fileChooserSize = new Dimension(750, 500);
	private WindowMenuElement[] fileMenuElements = null;
	
	private File docCacheRoot;
	private PdfExtractor pdfExtractor;
	
	private char fontDecoderMode = 'U';
	private FontDecoderCharset fontDecoderCharset = PdfFontDecoder.UNICODE;
	private LazyFontDecoderCharset[] customFontDecoderCharsets = null;
	
	private int pdfLoadFlags = (PdfExtractor.USE_EMBEDDED_OCR | PdfExtractor.META_PAGES | PdfExtractor.ENHANCE_SCANS | PdfExtractor.ENHANCE_SCANS_ALL_OPTIONS); // TODO make this default configurable
	
	GoldenGateImagineUI(GoldenGateImagine ggImagine, String specVersionDate, File docCacheRoot) {
		super("GoldenGATE Imagine" + ((specVersionDate == null) ? "" : (" (version " + specVersionDate + ")")) + " - " + ggImagine.getConfigurationName());
		this.goldenGate = ggImagine.getGoldenGATE();
		this.ggImagine = ggImagine;
		
		//	set window icon
		this.setIconImage(this.goldenGate.getGoldenGateIcon());
		
		//	set folder for caching IMF contents
		this.docCacheRoot = docCacheRoot;
		
		//	get PDF reader
		this.pdfExtractor = this.ggImagine.getPdfExtractor();
		
		//	configure file chooser
		this.fileChooser.setMultiSelectionEnabled(false);
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		Object lastDocFolder = UserInterfaceUtils.getDisplayProperty("lastDocFolder");
		if (lastDocFolder instanceof String)
			this.fileChooser.setSelectedFile(new File(lastDocFolder.toString(), " ")); // we need this dummy file name so the folder is actually opened instead of being selected in its parent folder
		else this.fileChooser.setSelectedFile(new File((new File(".")).getAbsolutePath(), " ")); // we need this dummy file name so the folder is actually opened instead of being selected in its parent folder
		
		//	read font decoder charset for born-digital PDFs
		//	TODO revisit loading options for font decoding (include custom charstets, etc.)
		try {
			BufferedReader fcIn;
			if (this.goldenGate.getConfiguration().isDataAvailable("GgImagine.pdfDecoderCharset.cnfg"))
				fcIn = new BufferedReader(new InputStreamReader(this.goldenGate.getConfiguration().getInputStream("GgImagine.pdfDecoderCharset.cnfg"), "UTF-8"));
			else fcIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File("./GgImagine.pdfDecoderCharset.cnfg")), "UTF-8"));
			this.fontDecoderCharset = CustomFontDecoderCharset.readCharSet("", fcIn);
		}
		catch (IOException ioe) {
			System.out.println("Error reading font decoder charset: " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		
		//	initialize basis for selection action usage statistics
		Settings actionUsageData = this.goldenGate.getApplicationSettings("GgImagine.actionUsage.cnfg");
		NamedElementUsageStatistics.initializeData((actionUsageData == null) ? new Settings() : actionUsageData);
		
		//	create UI
		this.ui = new GgiDoumentMarkupUI(this.ggImagine);
		
		//	make sure we exit on window closing
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				exit();
			}
		});
		this.addWindowFocusListener(this.ui);
		
		//	register UI to GoldenGATE core
		this.goldenGate.setUserInterface(this.ui);
		
		//	assemble major parts
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(this.ui, BorderLayout.CENTER);
		this.setSize(1000, 800);
		this.setLocationRelativeTo(null);
	}
	
	WindowMenuElement[] getFileMenuElements() {
		if (this.fileMenuElements == null)
			this.initFileMenuElements();
		return this.fileMenuElements;
	}
	private void initFileMenuElements() {
		ArrayList fmes = new ArrayList();
		int fmeFlags;
		
		//	get document IO providers
		ImageDocumentIoProvider[] docIoProviders = this.ggImagine.getDocumentIoProviders();
		
		//	add built-in loading options
		fmeFlags = 0;
		fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
		fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
		fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
		fmes.add(new WindowMenuFunction("ggImagine", "openDoc", "Open Document", "Load a document from a local file", fmeFlags) {
			private FileFilter loadFileFilter = imfFileFilter;
			private char fontMode = fontDecoderMode;
			private FontDecoderCharset fontCharset = fontDecoderCharset;
			private int loadFlags = pdfLoadFlags;
			public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
				return true;
			}
			public void execute(GoldenGateUI ggui, DocumentDisplay display) {
				this.showLoadDialog(true);
			}
			private void showLoadDialog(boolean confirmExoticPdf) {
				clearFileFilters(fileChooser);
				fileChooser.addChoosableFileFilter(imfFileFilter);
				fileChooser.addChoosableFileFilter(imdFileFilter);
				fileChooser.addChoosableFileFilter(batchCacheDocFileFilter);
				fileChooser.addChoosableFileFilter(genericPdfFileFilter);
				fileChooser.addChoosableFileFilter(textPdfFileFilter);
				fileChooser.addChoosableFileFilter(imagePdfFileFilter);
				fileChooser.addChoosableFileFilter(hybridPdfFileFilterD);
				fileChooser.addChoosableFileFilter(hybridPdfFileFilterV);
				fileChooser.setFileFilter((this.loadFileFilter == null) ? imfFileFilter : this.loadFileFilter);
				
				PdfLoadOptionPanel lop = new PdfLoadOptionPanel(this.fontMode, this.fontCharset, this.loadFlags, true);
				lop.fileFilterSelected(fileChooser.getFileFilter());
				fileChooser.setAccessory(lop);
				fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, lop);
				fileChooser.setPreferredSize(fileChooserSize);
				
				int choice = fileChooser.showOpenDialog(GoldenGateImagineUI.this);
				
				fileChooser.removePropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, lop);
				fileChooser.setAccessory(null);
				fileChooser.getSize(fileChooserSize);
				
				if (choice != JFileChooser.APPROVE_OPTION)
					return;
				
				File file = fileChooser.getSelectedFile();
				this.loadFileFilter = fileChooser.getFileFilter();
				this.fontMode = lop.getFontMode();
				this.fontCharset = lop.getFontCharset();
				this.loadFlags = lop.getFlags();
				this.loadFile(file, confirmExoticPdf);
			}
			private void loadFile(File file, boolean confirmExoticPdf) {
				try {
					if (this.loadFileFilter == imdFileFilter) {
						UserInterfaceUtils.setDisplayProperty("lastDocFolder", file.getParentFile().getAbsolutePath());
						loadDocument(file.getName(), file, null, this.loadFileFilter, this.fontMode, this.fontCharset, this.loadFlags, null, -1);
					}
					else if (this.loadFileFilter == batchCacheDocFileFilter) {
						UserInterfaceUtils.setDisplayProperty("lastDocFolder", file.getParentFile().getAbsolutePath());
						loadDocument(file.getParentFile().getName(), file, null, this.loadFileFilter, this.fontMode, this.fontCharset, this.loadFlags, null, -1);
					}
					else if (confirmExoticPdf && (this.loadFileFilter == hybridPdfFileFilterD) || (this.loadFileFilter == hybridPdfFileFilterV)) {
						int choice = JOptionPane.showConfirmDialog(GoldenGateImagineUI.this, ("<HTML>" +
								"Are you sure you are opening a hybrid PDF whose <I>scans do not contain any text anymore</I>?<BR/>" +
								"Use these modes only for PDFs whose scans have their <I>text layer completely removed and replaced</I><UL>" +
								"<LI>with font based vector graphics (e.g. DjVu compression)<BR/><TT>PDF documents (scanned, text layer vectorized)</TT></LI>" +
								"<LI>with regular PDF text rendered on top of page backgrounds<BR/><TT>PDF documents (scanned, text layer digitized)</TT></LI>" +
								"</UL>" +
								"For PDFs whose scans still contain the text, embedded OCR or not<BR/><TT>PDF documents (scanned, with or without OCR)</TT>" +
								"</HTML>"), "Confirm Hybrid PDF Mode", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						if (choice == JOptionPane.YES_OPTION) {
							UserInterfaceUtils.setDisplayProperty("lastDocFolder", file.getParentFile().getAbsolutePath());
							this.loadFile(file, false);
						}
						else if (choice == JOptionPane.NO_OPTION)
							this.showLoadDialog(false);
					}
					else {
						UserInterfaceUtils.setDisplayProperty("lastDocFolder", file.getParentFile().getAbsolutePath());
						InputStream in = new BufferedInputStream(new FileInputStream(file));
						loadDocument(file.getName(), file, null, this.loadFileFilter, this.fontMode, this.fontCharset, this.loadFlags, in, file.length());
						in.close();
					}
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("An error occurred while loading a document from '" + file.getAbsolutePath() + "':\n" + e.getMessage()), "Error Loading Document", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace(System.out);
				}
			}
		});
		
		fmeFlags = 0;
		fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
		fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
		fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
		fmes.add(new WindowMenuFunction("ggImagine", "loadUrl", "Load Document from URL", "Load a document from a URL", fmeFlags) {
			private FileFilter loadFormat = genericPdfFileFilter;
			private char fontMode = fontDecoderMode;
			private FontDecoderCharset fontCharset = fontDecoderCharset;
			private int loadFlags = pdfLoadFlags;
			public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
				return true;
			}
			public void execute(GoldenGateUI ggui, DocumentDisplay display) {
				UrlLoadDialog uld = new UrlLoadDialog(null, this.loadFormat, this.fontMode, this.fontCharset, this.loadFlags);
				uld.setVisible(true);
				this.loadFormat = uld.getFormat();
				this.fontMode = uld.getFontMode();
				this.fontCharset = uld.getFontCharset();
				this.loadFlags = uld.getFlags();
			}
		});
		
		//	add loading items from custom document IO providers
		for (int p = 0; p < docIoProviders.length; p++) {
			String sourceName = docIoProviders[p].getLoadSourceName();
			if (sourceName == null)
				continue;
			final ImageDocumentIoProvider idip = docIoProviders[p];
			fmeFlags = 0;
			fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
			fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
			fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
			fmes.add(new WindowMenuFunction(idip, "load", ("Load Document from " + sourceName), ("Load a document from " + sourceName), fmeFlags) {
				public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
					return true;
				}
				public void execute(GoldenGateUI ggui, DocumentDisplay display) {
					loadDocument(idip);
				}
			});
		}
		
		//	add built-in saving options
		fmeFlags = 0;
		fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
		fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
		fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_DOCUMENT;
		fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
		fmes.add(new WindowMenuFunction("ggImagine", "saveDocAs", "Save Document As", "Save the current document to a local file in a new format", fmeFlags) {
			public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
				return (display instanceof GgiDocumentEditorTab);
			}
			public void execute(GoldenGateUI ggui, DocumentDisplay display) {
				if (display instanceof GgiDocumentEditorTab)
					((GgiDocumentEditorTab) display).saveAs(fileChooser, fileChooserSize);
			}
		});
		
		//	add saving items from custom document IO providers
		for (int p = 0; p < docIoProviders.length; p++) {
			String destName = docIoProviders[p].getSaveDestinationName();
			if (destName == null)
				continue;
			final ImageDocumentIoProvider idip = docIoProviders[p];
			fmeFlags = 0;
			fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
			fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
			fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_DOCUMENT;
			fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
			fmes.add(new WindowMenuFunction(idip, "save", ("Save Document to " + destName), ("Save the current document to " + destName), fmeFlags) {
				public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
					return (display instanceof GgiDocumentEditorTab);
				}
				public void execute(GoldenGateUI ggui, DocumentDisplay display) {
					if (display instanceof GgiDocumentEditorTab)
						((GgiDocumentEditorTab) display).saveAs(idip);
				}
			});
		}
		
		//	add 'exit' option
		fmeFlags = 0;
		fmeFlags |= WindowMenuElement.PROPERTY_AVAILABLE_DESKTOP;
		fmeFlags |= WindowMenuElement.PROPERTY_REQUIRES_MAIN_WINDOW;
		fmeFlags = WindowMenuElement.encodePreferredMenuName(WindowMenuBar.FILE_MENU_NAME, fmeFlags);
		fmes.add(new WindowMenuFunction("ggImagine", "exit", "Exit", "Close GoldenGATE Imagine", fmeFlags) {
			public boolean checkAvailable(GoldenGateUI ggui, DocumentDisplay display) {
				return true;
			}
			public void execute(GoldenGateUI ggui, DocumentDisplay display) {
				exit();
			}
		});
		
		//	finally ...
		this.fileMenuElements = ((WindowMenuElement[]) fmes.toArray(new WindowMenuElement[fmes.size()]));
	}
	
	private void handleDrop(Transferable transfer) {
		DataFlavor[] dataFlavors = transfer.getTransferDataFlavors();
		for (int f = 0; f < dataFlavors.length; f++) {
			System.out.println("Trying data flavor " + dataFlavors[f].toString());
			System.out.println(" - MIME type is " + dataFlavors[f].getMimeType());
			System.out.println(" - representation class is " + dataFlavors[f].getRepresentationClass());
			
			//	nothing to work with
			if (dataFlavors[f].getMimeType() == null)
				continue;
			
			//	get basic data
			String mimeType = dataFlavors[f].getMimeType();
			Class representationClass = dataFlavors[f].getRepresentationClass();
			
			//	file drop
			if (("application/x-java-file-list".equalsIgnoreCase(mimeType) || mimeType.toLowerCase().startsWith("application/x-java-file-list; class=")) && List.class.isAssignableFrom(representationClass)) try {
				List droppedFileList = ((List) transfer.getTransferData(dataFlavors[f]));
				for (int t = 0; t < droppedFileList.size(); t++)
					loadDroppedFile((File) droppedFileList.get(t));
				return;
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
			}
			
			//	URL drop
			if (("application/x-java-url".equalsIgnoreCase(mimeType) || mimeType.toLowerCase().startsWith("application/x-java-url; class=")) && URL.class.isAssignableFrom(representationClass)) try {
				URL droppedUrl = ((URL) transfer.getTransferData(dataFlavors[f]));
				String droppedUrlString = droppedUrl.toString();
				FileFilter matchFileFilter = null;
				if (droppedUrlString.toLowerCase().matches("http\\:\\/\\/(.+\\/)+.+\\.imf"))
					matchFileFilter = imfFileFilter;
				else if (droppedUrlString.toLowerCase().matches("http\\:\\/\\/(.+\\/)+.+\\.pdf"))
					matchFileFilter = genericPdfFileFilter;
				UrlLoadDialog uld = new UrlLoadDialog(droppedUrlString, matchFileFilter, fontDecoderMode, fontDecoderCharset, pdfLoadFlags);
				uld.setVisible(true);
				return;
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
	
	private class UrlLoadDialog extends DialogPanel {
		private JTextField urlInput = new JTextField("http://");
		private JComboBox formatChooser = new JComboBox();
		private JSpinner timeoutChooser = new JSpinner(new SpinnerNumberModel(15, 5, 300, 5));
		private PdfLoadOptionPanel loadOptionPanel;
		
		UrlLoadDialog(String urlString, FileFilter format, char fontMode, FontDecoderCharset fontCharset, int scanFlags) {
			super("Open Document from URL", true);
			
			if (urlString != null)
				this.urlInput.setText(urlString);
			this.urlInput.setBorder(BorderFactory.createLoweredBevelBorder());
			this.urlInput.setFont(new Font("Monospaced", Font.PLAIN, 12));
			this.urlInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					loadUrl();
				}
			});
			
			this.formatChooser.addItem(imfFileFilter);
			this.formatChooser.addItem(genericPdfFileFilter);
			this.formatChooser.addItem(textPdfFileFilter);
			this.formatChooser.addItem(imagePdfFileFilter);
			this.formatChooser.addItem(hybridPdfFileFilterD);
			this.formatChooser.addItem(hybridPdfFileFilterV);
			this.formatChooser.setSelectedItem((format == null) ? genericPdfFileFilter : format);
			this.formatChooser.setBorder(BorderFactory.createLoweredBevelBorder());
			this.formatChooser.setEditable(false);
			
			this.timeoutChooser.setBorder(BorderFactory.createLoweredBevelBorder());
			
			JPanel selectorPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.weighty = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.insets.top = 5;
			gbc.insets.left = 5;
			gbc.insets.right = 5;
			gbc.insets.bottom = 5;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.weightx = 0;
			selectorPanel.add(new JLabel("URL"), gbc.clone());
			gbc.gridx = 1;
			gbc.weightx = 1;
			selectorPanel.add(this.urlInput, gbc.clone());
			
			gbc.gridy = 1;
			gbc.gridx = 0;
			gbc.weightx = 0;
			selectorPanel.add(new JLabel("Format"), gbc.clone());
			gbc.gridx = 1;
			gbc.weightx = 1;
			selectorPanel.add(this.formatChooser, gbc.clone());
			
			//	initialize timeout selector
			JPanel timeoutPanel = new JPanel(new BorderLayout(), true);
			timeoutPanel.add(new JLabel("Timeout (seconds) "), BorderLayout.WEST);
			timeoutPanel.add(this.timeoutChooser, BorderLayout.CENTER);
			
			//	initialize load option panel
			this.loadOptionPanel = new PdfLoadOptionPanel(fontMode, fontCharset, scanFlags, false);
			this.loadOptionPanel.fileFilterSelected(this.getFormat());
			this.formatChooser.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					loadOptionPanel.fileFilterSelected(getFormat());
				}
			});
			
			//	initialize buttons
			JButton commitButton = new JButton("Open Document");
			commitButton.setBorder(BorderFactory.createRaisedBevelBorder());
			commitButton.setPreferredSize(new Dimension(100, 21));
			commitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					loadUrl();
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBorder(BorderFactory.createRaisedBevelBorder());
			cancelButton.setPreferredSize(new Dimension(100, 21));
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			
			//	assemble button panel
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			buttonPanel.add(timeoutPanel);
			buttonPanel.add(commitButton);
			buttonPanel.add(cancelButton);
			this.loadOptionPanel.helpButton.setPreferredSize(new Dimension(100, 21));
			buttonPanel.add(this.loadOptionPanel.helpButton);
			
			//	put the whole stuff together
			this.add(selectorPanel, BorderLayout.NORTH);
			this.add(this.loadOptionPanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			//	configure dialog proper
			this.setResizable(true);
			this.setSize(new Dimension(500, 240));
			this.setLocationRelativeTo(GoldenGateImagineUI.this);
			
			//	if we have a URL and a file format, we can start loading right after dialog comes up (load from EDT, though)
			if ((urlString != null) && (format != null)) {
				Thread loadTrigger = new Thread() {
					public void run() {
						while (!getDialog().isVisible()) try {
							Thread.sleep(100);
						} catch (InterruptedException ie) {}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								loadUrl();
							}
						});
					}
				};
				loadTrigger.start();
			}
		}
		
		void loadUrl() {
			String urlString = this.urlInput.getText();
			try {
				this.loadUrl(urlString);
			}
			catch (Exception e) {
				e.printStackTrace(System.out);
				JOptionPane.showMessageDialog(this, ("An error occurred while loading a document from '" + urlString + "':\n  " + e.getMessage() + "\nIf the error was due to a read timeout, increasing the timeout might fix it."), "Error Loading Document", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		void loadUrl(final String urlString) throws IOException {
			final URL url = new URL(urlString);
			
			final int timeoutMillis = (Integer.parseInt(this.timeoutChooser.getValue().toString()) * 1000);
			final long[] lastReadMillis = {System.currentTimeMillis()};
			
			final HttpURLConnection[] urlCon = {null};
			final InputStream[] urlIn = {null};
			final byte[][] urlData = {null};
			final IOException[] error = {null};
			
			//	use progress monitor
			final ProgressMonitorDialog urlLoadPm = new ProgressMonitorDialog(this.getDialog(), ("Buffering Data from '" + urlString + "'"));
			urlLoadPm.setBaseProgress(0);
			urlLoadPm.setMaxProgress(100);
			urlLoadPm.setSize(this.getSize());
			urlLoadPm.setLocationRelativeTo(this.getDialog());
			
			//	use extra thread and progress dialog to buffer document
			final Thread urlLoader = new Thread("UrlLoaderThread") {
				public void run() {
					while (!urlLoadPm.getWindow().isVisible()) try {
						Thread.sleep(50);
					} catch (InterruptedException ie) {}
					
					try {
						urlLoadPm.setInfo("Connecting to source");
						urlCon[0] = ((HttpURLConnection) url.openConnection());
						urlCon[0].connect();
						urlLoadPm.setInfo("Connection established");
						urlIn[0] = new BufferedInputStream(urlCon[0].getInputStream());
						urlLoadPm.setInfo("Got input stream");
						if (error[0] != null) // we might have timed out by now
							return;
						lastReadMillis[0] = System.currentTimeMillis();
						int dataBytesTotal = ((urlCon[0].getContentLength() == -1) ? (1024 * 1024) : urlCon[0].getContentLength()); // start with estimate of 1MB if content length missing
						int dataBytesRead = 0;
						ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();
						byte[] dataByteBuffer = new byte[1024];
						for (int r; (r = urlIn[0].read(dataByteBuffer, 0, dataByteBuffer.length)) != -1;) {
							dataBytes.write(dataByteBuffer, 0, r);
							lastReadMillis[0] = System.currentTimeMillis();
							if (error[0] != null)
								break;
							dataBytesRead += r;
							urlLoadPm.setInfo("Read " + r + " more bytes");
							while (dataBytesTotal < dataBytesRead)
								dataBytesTotal += (1024 * 512); // increment estimate by 512KB
							urlLoadPm.setProgress((dataBytesRead * 100) / dataBytesTotal);
						}
						urlIn[0].close();
						urlData[0] = dataBytes.toByteArray();
						urlLoadPm.setInfo("Data buffered completely");
						urlLoadPm.setProgress(100);
					}
					catch (IOException ioe) {
						error[0] = ioe;
						urlLoadPm.setInfo("Error buffering data: " + ioe.getMessage());
					}
					finally {
						urlLoadPm.close();
					}
				}
			};
			urlLoader.start();
			
			final Thread timeoutGuard = new Thread("UrlReadTimeoutGuard") {
				public void run() {
					while (!urlLoadPm.getWindow().isVisible()) try {
						Thread.sleep(50);
					} catch (InterruptedException ie) {}
					
					try {
						while (true) {
							try {
								Thread.sleep(timeoutMillis / 5);
							} catch (InterruptedException ie) {}
							
							//	we're done reading
							if (urlData[0] != null)
								return;
							
							//	we're on time
							if (System.currentTimeMillis() < (lastReadMillis[0] + timeoutMillis))
								continue;
							
							//	connection timeout
							if (urlIn[0] == null) {
								urlLoader.interrupt();
								error[0] = new IOException("Timeout establishing connection to '" + urlString + "'");
							}
							
							//	read timeout (exception is thrown from reader in that case)
							else try {
								urlLoader.interrupt();
								urlIn[0].close();
							} catch (IOException ioe) {}
							
							//	whichever way, we're done here
							return;
						}
					}
					finally {
						urlLoadPm.close();
					}
				}
			};
			timeoutGuard.start();
			
			//	open progress monitor (waits for buffering to time out or complete)
			urlLoadPm.popUp(true);
			
			//	throw any exception that might have occurred
			if (error[0] != null)
				throw error[0];
			
			//	delegate to main loading method
			this.dispose();
			String docName = urlString;
			if (docName.indexOf("//") != -1)
				docName = docName.substring(docName.indexOf("//") + "//".length());
			docName = docName.replaceAll("[\\/\\:]+", "_");
			loadDocument(docName, null, urlString, this.getFormat(), this.loadOptionPanel.getFontMode(), this.loadOptionPanel.getFontCharset(), this.loadOptionPanel.getFlags(), new ByteArrayInputStream(urlData[0]), urlData[0].length);
		}
		
		FileFilter getFormat() {
			return ((FileFilter) this.formatChooser.getSelectedItem());
		}
		
		int getFlags() {
			return this.loadOptionPanel.getFlags();
		}
		
		char getFontMode() {
			return this.loadOptionPanel.getFontMode();
		}
		
		FontDecoderCharset getFontCharset() {
			return this.loadOptionPanel.getFontCharset();
		}
	}
	
	private class PdfLoadOptionPanel extends JPanel implements PropertyChangeListener {
		private JPanel fontOptionPanel;
		private JComboBox fontModeSelector;
		private JComboBox fontCharsetSelector;
		
		private JPanel scanOptionPanel;
		private JCheckBox useEmbeddedOcr;
		private JComboBox embeddedOcrModeSelector;
		private JCheckBox enhanceScans;
		private JButton enhanceScansOptionButton;
		private JCheckBox enhanceScansInvertWhiteBlack;
		private JCheckBox enhanceScansSmoothLetters;
		private JCheckBox enhanceScansEliminateBackground;
		private JCheckBox enhanceScansWhiteBalance;
		private JCheckBox enhanceScansCleanPageEdges;
		private JCheckBox enhanceScansFeatherDust;
		private JCheckBox enhanceScansCorrectRotation;
		private JCheckBox enhanceScansCorrectSkew;
		private JCheckBox enhanceScansLevelGradients;
		private JCheckBox metaPages;
		
		private JPanel pageFormatPanel;
		private JRadioButton autoPages;
		private JRadioButton singlePages;
		private JRadioButton doublePages;
		private JCheckBox useFixedDpi;
		private JTextField fixedDpi;
		
		final JButton helpButton = new JButton("Help");
		private Color helpButtonDefaultColor = this.helpButton.getBackground();
		private Color helpButtonWarningColor = new Color(0xFF, 0x80, 0x80, 0xFF);
		
		PdfLoadOptionPanel(char fontMode, FontDecoderCharset fontCharset, int scanFlags, boolean forFileChooser) {
			super((forFileChooser ? new BorderLayout() : new GridLayout(1, 0)), true);
			
			//	initialize font mode selector
			FontModeTray[] fmts = getFontModeOptions();
			FontModeTray sfmt = null;
			for (int t = 0; t < fmts.length; t++)
				if (fmts[t].mode == fontMode) {
					sfmt = fmts[t];
					break;
				}
			this.fontModeSelector = new JComboBox(fmts);
			this.fontModeSelector.setEditable(false);
			if (sfmt != null)
				this.fontModeSelector.setSelectedItem(sfmt);
			this.fontModeSelector.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 4, 0, this.getBackground()), this.fontModeSelector.getBorder()));
			
			//	initialize font charset selector
			FontCharsetTray[] fcts = getFontCharsetOptions();
			FontCharsetTray sfct = null;
			for (int t = 0; t < fcts.length; t++)
				if (fcts[t].charset == fontCharset) {
					sfct = fcts[t];
					break;
				}
			this.fontCharsetSelector = new JComboBox(fcts);
			this.fontCharsetSelector.setEditable(false);
			if (sfct != null)
				this.fontCharsetSelector.setSelectedItem(sfct);
			this.fontCharsetSelector.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 4, 0, this.getBackground()), this.fontCharsetSelector.getBorder()));
			
			//	toggle charset selector dependent on mode selector
			this.fontModeSelector.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fontCharsetSelector.setEnabled(((FontModeTray) fontModeSelector.getSelectedItem()).charset == null);
				}
			});
			if (sfmt != null)
				this.fontCharsetSelector.setEnabled(sfmt.charset == null);
			
			//	assemble option panel for born-digital PDFs
			this.fontOptionPanel = new JPanel(new GridLayout(0, 1), true);
			this.fontOptionPanel.add(new JLabel(("<HTML><B>" + (forFileChooser ? "" : "&nbsp;&nbsp;") + "Options for Font Decoding</B></HTML>"), (forFileChooser ? JLabel.CENTER : JLabel.LEFT)));
			this.fontOptionPanel.add(this.fontModeSelector);
			this.fontOptionPanel.add(this.fontCharsetSelector);
			
			//	initialize options for scanned PDFs
			this.useEmbeddedOcr = new JCheckBox("Use Embedded OCR", ((scanFlags & PdfExtractor.USE_EMBEDDED_OCR) != 0));
			this.useEmbeddedOcr.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					embeddedOcrModeSelector.setEnabled(useEmbeddedOcr.isSelected());
				}
			});
			this.embeddedOcrModeSelector = new JComboBox(embeddedOcrModes);
			this.embeddedOcrModeSelector.setSelectedItem(new EmbeddedOcrModeTray("<dummy>", (scanFlags & PdfExtractor.ADJUST_EMBEDDED_OCR_NONE)));
			this.addIndentBorder(this.embeddedOcrModeSelector);
			this.enhanceScans = new JCheckBox("Enhance Scans", ((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0));
			this.enhanceScansOptionButton = new JButton("...");
			this.enhanceScansOptionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					showEnhanceScansOptions();
				}
			});
			this.enhanceScans.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					enhanceScansOptionButton.setEnabled(enhanceScans.isSelected());
				}
			});
			this.metaPages = new JCheckBox("Expect Meta Pages", ((scanFlags & PdfExtractor.META_PAGES) != 0));
			
			//	initialize options for scan enhancement
			this.enhanceScansInvertWhiteBlack = new JCheckBox("Check Inversion (white on black)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_INVERT_WHITE_ON_BLACK) != 0)));
			this.enhanceScansSmoothLetters = new JCheckBox("Smooth Letters (and other edges)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_SMOOTH_LETTERS) != 0)));
			this.enhanceScansEliminateBackground = new JCheckBox("Remove Background (for grayscale only)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_ELIMINATE_BACKGROUND) != 0)));
			this.enhanceScansWhiteBalance = new JCheckBox("White Balance (for grayscale only)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_WHITE_BALANCE) != 0)));
			this.enhanceScansCleanPageEdges = new JCheckBox("Remove Dark Page Edges (usually scan artifacts)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_CLEAN_PAGE_EDGES) != 0)));
			this.enhanceScansFeatherDust = new JCheckBox("Remove Speckles (spots too small for any meaning)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_REMOVE_SPECKLES) != 0)));
			this.enhanceScansCorrectRotation = new JCheckBox("Correct Rotation (above 2°)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_CORRECT_ROTATION) != 0)));
			this.enhanceScansCorrectSkew = new JCheckBox("Correct Skew (below 2°)", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_CORRECT_SKEW) != 0)));
			this.enhanceScansLevelGradients = new JCheckBox("Level Brightness Gradients", (((scanFlags & PdfExtractor.ENHANCE_SCANS) != 0) && ((scanFlags & PdfExtractor.ENHANCE_SCANS_LEVEL_GRADIENTS) != 0)));
			
			//	assemble option panel for scanned PDFs
			this.scanOptionPanel = new JPanel(new GridLayout(0, 1), true);
			this.scanOptionPanel.add(new JLabel(("<HTML><B>" + (forFileChooser ? "" : "&nbsp;&nbsp;") + "Options for Scanned PDFs</B></HTML>"), (forFileChooser ? JLabel.CENTER : JLabel.LEFT)));
			this.scanOptionPanel.add(this.useEmbeddedOcr);
			this.scanOptionPanel.add(this.embeddedOcrModeSelector);
			JPanel enhanceScansPanel = new JPanel(new BorderLayout(), true);
			enhanceScansPanel.add(this.enhanceScans, BorderLayout.CENTER);
			enhanceScansPanel.add(this.enhanceScansOptionButton, BorderLayout.EAST);
			this.scanOptionPanel.add(enhanceScansPanel);
			this.scanOptionPanel.add(this.metaPages);
			
			//	initialize page format options
			boolean singlePages = ((scanFlags & PdfExtractor.SINGLE_PAGE_SCANS) != 0);
			boolean doublePages = ((scanFlags & PdfExtractor.DOUBLE_PAGE_SCANS) != 0);
			this.autoPages = new JRadioButton("Auto-detect", (singlePages == doublePages));
			this.singlePages = new JRadioButton("Single Pages", (singlePages && !doublePages));
			this.doublePages = new JRadioButton("Double Pages", (!singlePages && doublePages));
			ButtonGroup pageFormatButtons = new ButtonGroup();
			pageFormatButtons.add(this.autoPages);
			pageFormatButtons.add(this.singlePages);
			pageFormatButtons.add(this.doublePages);
			this.useFixedDpi = new JCheckBox("Fix DPI", ((scanFlags & PdfExtractor.USE_FIXED_RESOLUTION) != 0));
			this.useFixedDpi.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					fixedDpi.setEnabled(useFixedDpi.isSelected());
				}
			});
			this.fixedDpi = new JTextField();
			
			//	assemble option panel for scanned PDFs
			this.pageFormatPanel = new JPanel(new GridLayout(0, 1), true);
			this.pageFormatPanel.add(new JLabel(("<HTML><B>" + (forFileChooser ? "" : "&nbsp;&nbsp;") + "Scanned Page Format</B></HTML>"), (forFileChooser ? JLabel.CENTER : JLabel.LEFT)));
			this.pageFormatPanel.add(this.autoPages);
			this.pageFormatPanel.add(this.singlePages);
			this.pageFormatPanel.add(this.doublePages);
			JPanel fixedDpiPanel = new JPanel(new BorderLayout(), true);
			fixedDpiPanel.add(this.useFixedDpi, BorderLayout.WEST);
			fixedDpiPanel.add(this.fixedDpi, BorderLayout.CENTER);
			this.pageFormatPanel.add(fixedDpiPanel);
			if (forFileChooser)
				this.pageFormatPanel.add(this.helpButton);
			
			//	format help button
			this.helpButton.setOpaque(true);
			this.helpButton.setToolTipText("What do all of these PDF decode options mean?");
			this.helpButton.setBorder(BorderFactory.createRaisedBevelBorder());
			this.helpButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					showHelp();
				}
			});
			
			//	assemble the whole thing
			if (forFileChooser) {
				this.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, this.getBackground()));
				JPanel contentPanel = new JPanel(new BorderLayout(), true);
				contentPanel.add(this.fontOptionPanel, BorderLayout.NORTH);
				contentPanel.add(this.scanOptionPanel, BorderLayout.CENTER);
				contentPanel.add(this.pageFormatPanel, BorderLayout.SOUTH);
				this.add(contentPanel, BorderLayout.SOUTH);
			}
			else {
				JPanel fontOptionPanel = new JPanel(new BorderLayout(), true);
				fontOptionPanel.add(this.fontOptionPanel, BorderLayout.NORTH);
				this.add(fontOptionPanel);
				JPanel scanOptionPanel = new JPanel(new BorderLayout(), true);
				scanOptionPanel.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 5, scanOptionPanel.getBackground()));
				scanOptionPanel.add(this.scanOptionPanel, BorderLayout.NORTH);
				this.add(scanOptionPanel);
				JPanel pageFormatPanel = new JPanel(new BorderLayout(), true);
				pageFormatPanel.add(this.pageFormatPanel, BorderLayout.NORTH);
				this.add(pageFormatPanel);
			}
		}
		
		private void addIndentBorder(JComponent jc) {
			jc.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 20, 0, 0, this.getBackground()), jc.getBorder()));
		}
		
		void showEnhanceScansOptions() {
			JPanel enhanceScansOptionPanel = new JPanel(new GridLayout(0, 1), true);
			enhanceScansOptionPanel.add(this.enhanceScansInvertWhiteBlack);
			enhanceScansOptionPanel.add(this.enhanceScansSmoothLetters);
			enhanceScansOptionPanel.add(this.enhanceScansEliminateBackground);
			enhanceScansOptionPanel.add(this.enhanceScansWhiteBalance);
			enhanceScansOptionPanel.add(this.enhanceScansCleanPageEdges);
			enhanceScansOptionPanel.add(this.enhanceScansFeatherDust);
			enhanceScansOptionPanel.add(this.enhanceScansCorrectRotation);
			enhanceScansOptionPanel.add(this.enhanceScansCorrectSkew);
			enhanceScansOptionPanel.add(this.enhanceScansLevelGradients);
			JOptionPane.showMessageDialog(this.enhanceScans, enhanceScansOptionPanel, "Scan Enhancement Options", JOptionPane.PLAIN_MESSAGE);
		}
		
		public void propertyChange(PropertyChangeEvent pce) {
			Object newValue = pce.getNewValue();
			if (newValue instanceof FileFilter)
				this.fileFilterSelected((FileFilter) newValue);
		}
		
		void fileFilterSelected(FileFilter fileFilter) {
			boolean bornDigitalPDFs = ((fileFilter == textPdfFileFilter) || (fileFilter == genericPdfFileFilter));
			boolean hybridPDFs = ((fileFilter == hybridPdfFileFilterD) || (fileFilter == hybridPdfFileFilterV));
			boolean scannedPDFs = ((fileFilter == imagePdfFileFilter) || (fileFilter == genericPdfFileFilter));
			this.fontModeSelector.setEnabled(bornDigitalPDFs);
			this.fontCharsetSelector.setEnabled(bornDigitalPDFs && (((FontModeTray) fontModeSelector.getSelectedItem()).charset == null));
			this.fontOptionPanel.setEnabled(bornDigitalPDFs);
			this.useEmbeddedOcr.setEnabled(scannedPDFs);
			this.embeddedOcrModeSelector.setEnabled(scannedPDFs && this.useEmbeddedOcr.isSelected());
			this.enhanceScans.setEnabled(scannedPDFs || hybridPDFs);
			this.enhanceScansOptionButton.setEnabled((scannedPDFs || hybridPDFs) && this.enhanceScans.isSelected());
			this.metaPages.setEnabled(scannedPDFs || hybridPDFs);
			this.scanOptionPanel.setEnabled(scannedPDFs || hybridPDFs);
			this.autoPages.setEnabled(scannedPDFs || hybridPDFs);
			this.singlePages.setEnabled(scannedPDFs || hybridPDFs);
			this.doublePages.setEnabled(scannedPDFs || hybridPDFs);
			this.useFixedDpi.setEnabled(scannedPDFs || hybridPDFs);
			this.fixedDpi.setEnabled((scannedPDFs || hybridPDFs) && this.useFixedDpi.isSelected());
			this.pageFormatPanel.setEnabled(scannedPDFs || hybridPDFs);
			this.helpButton.setEnabled(bornDigitalPDFs || scannedPDFs || hybridPDFs);
			this.helpButton.setBackground(hybridPDFs ? this.helpButtonWarningColor : this.helpButtonDefaultColor);
		}
		
		char getFontMode() {
			return ((FontModeTray) this.fontModeSelector.getSelectedItem()).mode;
		}
		
		FontDecoderCharset getFontCharset() {
			FontModeTray sfmt = ((FontModeTray) this.fontModeSelector.getSelectedItem());
			if (sfmt.charset != null)
				return sfmt.charset;
			FontDecoderCharset fdc = ((FontCharsetTray) this.fontCharsetSelector.getSelectedItem()).charset;
			if (fdc instanceof LazyFontDecoderCharset)
				((LazyFontDecoderCharset) fdc).ensureLoaded();
			return fdc;
		}
		
		int getFlags() {
			int flags = 0;
			if (this.useEmbeddedOcr.isSelected()) {
				flags |= PdfExtractor.USE_EMBEDDED_OCR;
				flags |= ((EmbeddedOcrModeTray) this.embeddedOcrModeSelector.getSelectedItem()).flagPair;
			}
			if (this.enhanceScans.isSelected()) {
				flags |= PdfExtractor.ENHANCE_SCANS;
				if (this.enhanceScansInvertWhiteBlack.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_INVERT_WHITE_ON_BLACK;
				if (this.enhanceScansSmoothLetters.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_SMOOTH_LETTERS;
				if (this.enhanceScansEliminateBackground.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_ELIMINATE_BACKGROUND;
				if (this.enhanceScansWhiteBalance.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_WHITE_BALANCE;
				if (this.enhanceScansCleanPageEdges.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_CLEAN_PAGE_EDGES;
				if (this.enhanceScansFeatherDust.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_REMOVE_SPECKLES;
				if (this.enhanceScansCorrectRotation.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_CORRECT_ROTATION;
				if (this.enhanceScansCorrectSkew.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_CORRECT_SKEW;
				if (this.enhanceScansLevelGradients.isSelected())
					flags |= PdfExtractor.ENHANCE_SCANS_LEVEL_GRADIENTS;
			}
			if (this.metaPages.isSelected())
				flags |= PdfExtractor.META_PAGES;
			if (this.singlePages.isSelected())
				flags |= PdfExtractor.SINGLE_PAGE_SCANS;
			else if (this.doublePages.isSelected())
				flags |= PdfExtractor.DOUBLE_PAGE_SCANS;
			if (this.useFixedDpi.isSelected()) try {
				int fixedDpi = Integer.parseInt(this.fixedDpi.getText());
				if (fixedDpi > 0) {
					flags |= PdfExtractor.USE_FIXED_RESOLUTION;
					flags |= (fixedDpi << 20);
				}
			} catch (Exception e) {}
			return flags;
		}
		
		private String helpHtml = null;
		private Dimension helpSize = new Dimension(800, 800);
		private Point helpPosition = null;
		private int helpScrollPosition = 0;
		void showHelp() {
			if ((this.helpHtml == null) || GoldenGateImagineUI.this.goldenGate.getConfiguration().isMasterConfiguration()) try {
				File helpFile = GoldenGateImagineUI.this.goldenGate.getLocalFile("./Data/GgImagine.pdfLoadOption.html");
				BufferedReader helpBr = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(helpFile)), "UTF-8"));
				StringBuffer helpBuffer = new StringBuffer();
				char[] buffer = new char[1024];
				for (int r = 0; (r = helpBr.read(buffer, 0, buffer.length)) != -1;)
					helpBuffer.append(buffer, 0, r);
				helpBr.close();
				this.helpHtml = helpBuffer.toString();
			}
			catch (IOException ioe) {
				System.out.println("Could not open PDF load option help: " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
			if (this.helpHtml == null)
				return;
			JEditorPane helpDisplay = new JEditorPane();
			helpDisplay.setEditable(false);
			helpDisplay.setContentType("text/html");
			helpDisplay.setText(this.helpHtml);
			final JScrollPane helpBox = new JScrollPane(helpDisplay);
			helpBox.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			helpBox.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			Window topWindow = DialogPanel.getTopWindow();
			DialogPanel helpDialog = new DialogPanel(topWindow, "", true);
			helpDialog.add(helpBox, BorderLayout.CENTER);
			helpDialog.setSize(this.helpSize);
			if (this.helpPosition == null)
				helpDialog.setLocationRelativeTo(topWindow);
			else helpDialog.getDialog().setLocation(this.helpPosition);
			helpDialog.addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent we) {
					helpBox.getVerticalScrollBar().setValue(helpScrollPosition); // scroll up to page top
				}
			});
			helpDialog.setVisible(true);
			this.helpSize = helpDialog.getSize();
			this.helpPosition = helpDialog.getDialog().getLocation();
			this.helpScrollPosition = helpBox.getVerticalScrollBar().getValue();
		}
	}
	
	private static class FontModeTray {
		final String name;
		final char mode;
		final FontDecoderCharset charset;
		FontModeTray(String name, char mode, FontDecoderCharset charset) {
			this.name = name;
			this.mode = mode;
			this.charset = charset;
		}
		FontModeTray(String name, char mode) {
			this(name, mode, null);
		}
		public String toString() {
			return this.name;
		}
		public boolean equals(Object obj) {
			return ((obj instanceof FontModeTray) && (((FontModeTray) obj).mode == this.mode));
		}
	}
	
	FontModeTray[] getFontModeOptions() {
		ArrayList fcts = new ArrayList();
		fcts.add(new FontModeTray("Do Not Decode Fonts", 'Q', PdfFontDecoder.NO_DECODING));
		fcts.add(new FontModeTray("Render Glyphs Only", 'R', PdfFontDecoder.RENDER_ONLY));
		fcts.add(new FontModeTray("Decode Unmapped Glyphs", 'U'));
		fcts.add(new FontModeTray("Verify Mapped Glyphs", 'V'));
		fcts.add(new FontModeTray("Decode All Glyphs", 'D'));
		return ((FontModeTray[]) fcts.toArray(new FontModeTray[fcts.size()]));
	}
	
	private static class LazyFontDecoderCharset extends FontDecoderCharset {
		private FontDecoderCharset charset = null;
		LazyFontDecoderCharset(String name) {
			super(name);
		}
		public boolean containsChar(char ch) {
			return ((this.charset != null) && this.charset.containsChar(ch));
		}
		void ensureLoaded() {
			if (this.charset != null)
				return;
			try {
				//	load via loading ad-hoc charset that does nothing but include the wrapped one
				this.charset = CustomFontDecoderCharset.readCharSet(this.name, new StringReader("@" + this.name));
			}
			catch (IOException ioe) {
				System.out.println("Error loading custom font decoder charset '" + this.name + "': " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
	}
	
	private LazyFontDecoderCharset[] getCustomFontDecoderCharsets() {
		if (this.customFontDecoderCharsets == null) {
			String[] cfdcns = CustomFontDecoderCharset.getProviderCharsetNames();
			this.customFontDecoderCharsets = new LazyFontDecoderCharset[cfdcns.length];
			for (int c = 0; c < cfdcns.length; c++)
				this.customFontDecoderCharsets[c] = new LazyFontDecoderCharset(cfdcns[c]);
		}
		return this.customFontDecoderCharsets;
	}
	
	private static class FontCharsetTray {
		final String name;
		final FontDecoderCharset charset;
		FontCharsetTray(String name, FontDecoderCharset charset) {
			this.name = name;
			this.charset = charset;
		}
		public String toString() {
			return this.name;
		}
		public boolean equals(Object obj) {
			return ((obj instanceof FontCharsetTray) && ((FontCharsetTray) obj).name.equals(this.name));
		}
	}
	
	FontCharsetTray[] getFontCharsetOptions() {
		ArrayList fcts = new ArrayList();
		fcts.add(new FontCharsetTray("Default Charset", this.fontDecoderCharset));
		fcts.add(new FontCharsetTray("Basic Latin", PdfFontDecoder.LATIN_BASIC));
		fcts.add(new FontCharsetTray("Extended Latin", PdfFontDecoder.LATIN));
		fcts.add(new FontCharsetTray("Full Latin", PdfFontDecoder.LATIN_FULL));
		fcts.add(new FontCharsetTray("Full Unicode", PdfFontDecoder.UNICODE));
		LazyFontDecoderCharset[] cfdcs = this.getCustomFontDecoderCharsets();
		for (int c = 0; c < cfdcs.length; c++)
			fcts.add(new FontCharsetTray(cfdcs[c].name, cfdcs[c]));
		return ((FontCharsetTray[]) fcts.toArray(new FontCharsetTray[fcts.size()]));
	}
	
	private static class EmbeddedOcrModeTray {
		final String label;
		final int flagPair;
		EmbeddedOcrModeTray(String label, int flagPair) {
			this.label = label;
			this.flagPair = flagPair;
		}
		public String toString() {
			return this.label;
		}
		public boolean equals(Object obj) {
			return ((obj instanceof EmbeddedOcrModeTray) && (((EmbeddedOcrModeTray) obj).flagPair == this.flagPair));
		}
	}
	
	private static EmbeddedOcrModeTray[] embeddedOcrModes = {
		new EmbeddedOcrModeTray("Adjust to Blocks", PdfExtractor.ADJUST_EMBEDDED_OCR_BLOCKS),
		new EmbeddedOcrModeTray("Adjust to Lines", PdfExtractor.ADJUST_EMBEDDED_OCR_LINES),
		new EmbeddedOcrModeTray("Adjust to Words", PdfExtractor.ADJUST_EMBEDDED_OCR_WORDS),
		new EmbeddedOcrModeTray("No Adjustment", PdfExtractor.ADJUST_EMBEDDED_OCR_NONE)
	};
	
	void loadDocument(final String docName, final File docSource, final String docSourceUrl, final FileFilter fileFilter, final char fontMode, final FontDecoderCharset fontCharset, final int scanFlags, final InputStream in, final long inLength) throws IOException {
		
		//	load IMF
		if (fileFilter == imfFileFilter) {
			final IOException[] loadException = {null};
			final ResourceSplashScreen loadScreen = new ResourceSplashScreen(this, ("Loading IMF Archive '" + docName + "'"), "", true, false);
			System.out.println("Creating load thread");
			Thread loadThread = new Thread("LoaderThread") {
				public void run() {
					try {
						loadScreen.setStep("Loading IMF Archive");
						ImDocument doc;
						File docDataCache = null;
						
						//	wait for load screen to show
						while (!loadScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						 // 50 MB should OK to hold in memory
						if (inLength < (1024 * 1024 * 50))
							doc = ImDocumentIO.loadDocument(in, loadScreen, inLength);
						
						//	use disk based cache if IMF is larger
						else {
							docDataCache = new File(docCacheRoot, docName);
							docDataCache.mkdirs();
							doc = ImDocumentIO.loadDocument(in, docDataCache, loadScreen, inLength);
						}
						in.close();
						
						//	add document name and (if any) source URL attributes
						doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, docName);
						if ((docSourceUrl != null) && !doc.hasAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE))
							doc.setAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE, docSourceUrl);
						
						//	register cache folder for cleanup when document closed
						if (docDataCache != null)
							registerDocDataCache(doc.docId, docDataCache);
						
						//	open document in UI
						ggImagine.notifyDocumentOpened(doc, ((docSource == null) ? docSourceUrl : docSource), loadScreen);
						ui.openDocument(new GgiDocumentEditorTab(doc, docName, docSource, fileFilter, null));
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
		
		//	load IMD
		if (fileFilter == imdFileFilter) {
			final IOException[] loadException = {null};
			final ResourceSplashScreen loadScreen = new ResourceSplashScreen(this, ("Loading IMF Directory '" + docName + "'"), "", true, false);
			System.out.println("Creating load thread");
			Thread loadThread = new Thread("LoaderThread") {
				public void run() {
					try {
						
						//	wait for load screen to show
						while (!loadScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	get entry folder and load document
						File docFolder = new File(docSource.getAbsolutePath() + "ir");
						if (!docFolder.exists())
							throw new FileNotFoundException("Data directory not found for " + docSource.getName());
						loadScreen.setStep("Loading IMF Directory");
						ImDocument doc = ImDocumentIO.loadDocument(docFolder, loadScreen);
						
						//	add document name and (if any) source URL attributes
						doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, docName);
						if ((docSourceUrl != null) && !doc.hasAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE))
							doc.setAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE, docSourceUrl);
						
						//	open document in UI
						ggImagine.notifyDocumentOpened(doc, docFolder, loadScreen);
						ui.openDocument(new GgiDocumentEditorTab(doc, docName, docSource, fileFilter, null));
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
		
		//	load batch cached IMD (helps with recovery)
		if (fileFilter == batchCacheDocFileFilter) {
			final IOException[] loadException = {null};
			final ResourceSplashScreen loadScreen = new ResourceSplashScreen(this, ("Loading Batch Cached Document '" + docName + "'"), "", true, false);
			System.out.println("Creating load thread");
			Thread loadThread = new Thread("LoaderThread") {
				public void run() {
					try {
						
						//	wait for load screen to show
						while (!loadScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
//						
//						//	load list of document entries
//						ArrayList docEntries = new ArrayList();
//						BufferedReader docEntryIn = new BufferedReader(new InputStreamReader(new FileInputStream(docSource), "UTF-8"));
//						for (String docEntryLine; (docEntryLine = docEntryIn.readLine()) != null;) {
//							ImDocumentEntry docEntry = ImDocumentEntry.fromTabString(docEntryLine);
//							if (docEntry != null)
//								docEntries.add(docEntry);
//						}
//						docEntryIn.close();
						
						//	load document from entry folder
						loadScreen.setStep("Loading Batch Cached Document");
//						ImDocument doc = ImDocumentIO.loadDocument(docSource.getParentFile(), ((ImDocumentEntry[]) docEntries.toArray(new ImDocumentEntry[docEntries.size()])), loadScreen);
						ImDocument doc = ImDocumentIO.loadDocument(docSource.getParentFile(), loadScreen);
						
						//	add document name and (if any) source URL attributes
						doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, docName);
						if ((docSourceUrl != null) && !doc.hasAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE))
							doc.setAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE, docSourceUrl);
						
						//	open document in UI
						ggImagine.notifyDocumentOpened(doc, docSource.getParentFile(), loadScreen);
						ui.openDocument(new GgiDocumentEditorTab(doc, docName, docSource, fileFilter, null));
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
		
		//	load PDF
		if (docName.toLowerCase().endsWith(".pdf")) {
			final IOException[] loadException = {null};
			final ResourceSplashScreen loadScreen = new ResourceSplashScreen(this, ("Loading PDF '" + docName + "'"), "", true, false);
			Thread loadThread = new Thread("LoaderThread") {
				public void run() {
					try {
						loadScreen.setStep("Loading PDF Document");
						ImDocument doc;
						
						//	wait for load screen to show
						while (!loadScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	load in slave JVM
						if ("slave".equals(UserInterfaceUtils.getDisplayProperty("pdfDecodeMode"))) {
							
							String pdfConverterCommand = "java -jar -Dorg.icepdf.core.streamcache.enabled=false ImageMarkupPDF.jar -l M -o O";
							
							if (docSource != null)
								pdfConverterCommand += (" -s \"" + docSource.getAbsolutePath() + "\"");
							
							if (fileFilter == textPdfFileFilter) {
								pdfConverterCommand += " -t D";
								pdfConverterCommand += (" -f " + fontMode);
								if ("DVU".indexOf(fontMode) == -1) {} // we're in mode Q or R, no need for charset
								else if (fontCharset == fontDecoderCharset)
									pdfConverterCommand += " -cs C -cp ./GgImagine.pdfDecoderCharset.cnfg";
								else if (fontCharset == PdfFontDecoder.UNICODE)
									pdfConverterCommand += " -cs U";
								else if (fontCharset == PdfFontDecoder.LATIN_FULL)
									pdfConverterCommand += " -cs F";
								else if (fontCharset == PdfFontDecoder.LATIN)
									pdfConverterCommand += " -cs L";
								else if (fontCharset == PdfFontDecoder.LATIN_BASIC)
									pdfConverterCommand += " -cs B";
							}
							else if (fileFilter == hybridPdfFileFilterD)
								pdfConverterCommand += " -t H";
							else if (fileFilter == hybridPdfFileFilterV)
								pdfConverterCommand += " -t V";
							else if (fileFilter == imagePdfFileFilter) {
								if ((scanFlags & PdfExtractor.USE_EMBEDDED_OCR) != 0)
									pdfConverterCommand += " -t O";
								else if ((scanFlags & PdfExtractor.META_PAGES) != 0)
									pdfConverterCommand += " -t M";
								else pdfConverterCommand += " -t S";
							}
							
							System.out.println("PDF converter command is " + pdfConverterCommand);
							Process pdfConverter = Runtime.getRuntime().exec(pdfConverterCommand, new String[0], new File("."));
							System.out.println("PDF converter started");
							
							if (docSource == null) {
								OutputStream toConverter = new BufferedOutputStream(pdfConverter.getOutputStream());
								byte[] buffer = new byte[1024];
								for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
									toConverter.write(buffer, 0, r);
								in.close();
								toConverter.flush();
								toConverter.close();
								System.out.println("Buffered PDF data sent");
							}
							
							final InputStream fromConverterError = new BufferedInputStream(pdfConverter.getErrorStream());
							new Thread() {
								public void run() {
									try {
										for (int b; (b = fromConverterError.read()) != -1;)
											System.err.print((char) b);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}.start();
							LineInputStream fromConverter = new LineInputStream(new BufferedInputStream(pdfConverter.getInputStream()));
							System.out.println("Start receiving converter response");
							for (byte[] line; (line = fromConverter.readLine()) != null;) {
								if (line.length < 3)
									continue;
								if ((line[0] == 'S') && (line[1] == ':'))
									loadScreen.setStep(new String(line, "S:".length(), (line.length - "S:".length())).trim());
								else if ((line[0] == 'I') && (line[1] == ':'))
									loadScreen.setInfo(new String(line, "I:".length(), (line.length - "I:".length())).trim());
								else if ((line[0] == 'P') && (line[1] == ':'))
									loadScreen.setProgress(Integer.parseInt(new String(line, "P:".length(), (line.length - "P:".length())).trim()));
								else if ((line[0] == 'B') && (line[1] == 'P') && (line[2] == ':'))
									loadScreen.setBaseProgress(Integer.parseInt(new String(line, "BP:".length(), (line.length - "BP:".length())).trim()));
								else if ((line[0] == 'M') && (line[1] == 'P') && (line[2] == ':'))
									loadScreen.setMaxProgress(Integer.parseInt(new String(line, "MP:".length(), (line.length - "MP:".length())).trim()));
								else break;
							}
							
							System.out.println("Converter process response finished, reading document");
							doc = ImDocumentIO.loadDocument(fromConverter, loadScreen);
							fromConverter.close();
						}
						
						//	load in local JVM
						else {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							byte[] buffer = new byte[1024];
							for (int r; (r = in.read(buffer, 0, buffer.length)) != -1;)
								baos.write(buffer, 0, r);
							in.close();
							
							if (fileFilter == textPdfFileFilter) {
								FontDecoderCharset useFontCharset;
								if (fontMode == 'U')
									useFontCharset = FontDecoderCharset.union(fontCharset, PdfFontDecoder.DECODE_UNMAPPED);
								else if (fontMode == 'V')
									useFontCharset = FontDecoderCharset.union(fontCharset, PdfFontDecoder.VERIFY_MAPPED);
								else useFontCharset = fontCharset;
								doc = pdfExtractor.loadTextPdf(baos.toByteArray(), useFontCharset, loadScreen);
							}
							else if (fileFilter == hybridPdfFileFilterD)
								doc = pdfExtractor.loadHybridPdf(baos.toByteArray(), scanFlags, false, loadScreen);
							else if (fileFilter == hybridPdfFileFilterV)
								doc = pdfExtractor.loadHybridPdf(baos.toByteArray(), scanFlags, true, loadScreen);
							else if (fileFilter == imagePdfFileFilter)
								doc = pdfExtractor.loadImagePdf(baos.toByteArray(), scanFlags, loadScreen);
							else doc = pdfExtractor.loadGenericPdf(baos.toByteArray(), loadScreen);
						}
						
						//	add document
						doc.setAttribute(DOCUMENT_NAME_ATTRIBUTE, docName);
						if ((docSourceUrl != null) && !doc.hasAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE))
							doc.setAttribute(DOCUMENT_SOURCE_LINK_ATTRIBUTE, docSourceUrl);
						ggImagine.notifyDocumentOpened(doc, ((docSource == null) ? docSourceUrl : docSource), loadScreen);
						ui.openDocument(new GgiDocumentEditorTab(doc, docName, docSource, null, null));
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
	
	private static class LineInputStream extends PeekInputStream {
		LineInputStream(InputStream in) throws IOException {
			super(in, 2048);
		}
		//	returns a line of bytes, INCLUDING its terminal line break bytes
		byte[] readLine() throws IOException {
			if (this.peek() == -1)
				return null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int b; (b = this.peek()) != -1;) {
				baos.write(this.read());
				if (b == '\r') {
					if (this.peek() == '\n')
						baos.write(this.read());
					break;
				}
				else if (b == '\n')
					break;
			}
			return baos.toByteArray();
		}
	}
	
	void loadDroppedFile(String droppedFilePath) {
		this.loadDroppedFile(new File(droppedFilePath));
	}
	
	void loadDroppedFile(File droppedFile) {
		try {
			FileFilter matchFileFilter;
			if (imfFileFilter.accept(droppedFile))
				matchFileFilter = imfFileFilter;
			else if (imdFileFilter.accept(droppedFile))
				matchFileFilter = imfFileFilter;
			else if (genericPdfFileFilter.accept(droppedFile))
				matchFileFilter = genericPdfFileFilter;
			else return;
			InputStream in = new BufferedInputStream(new FileInputStream(droppedFile));
			this.loadDocument(droppedFile.getName(), droppedFile, null, matchFileFilter, fontDecoderMode, fontDecoderCharset, pdfLoadFlags, in, droppedFile.length());
			in.close();
		}
		catch (SecurityException se) {
			System.out.println("Error opening document '" + droppedFile.getName() + "':\n   " + se.getClass().getName() + " (" + se.getMessage() + ")");
			se.printStackTrace(System.out);
			JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("Not allowed to open file '" + droppedFile.getName() + "':\n" + se.getMessage() + "\n\nIf you are currently running GoldenGATE Editor as an applet, your\nbrowser's security mechanisms might prevent reading files from your local disc."), "Not Allowed To Open File", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e) {
			System.out.println("Error opening document '" + droppedFile.getAbsolutePath() + "':\n   " + e.getClass().getName() + " (" + e.getMessage() + ")");
			e.printStackTrace(System.out);
			JOptionPane.showMessageDialog(GoldenGateImagineUI.this, ("Could not open file '" + droppedFile.getAbsolutePath() + "':\n" + e.getMessage()), "Error Opening File", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	void loadDocument(final ImageDocumentIoProvider idip) {
		final ResourceSplashScreen loadScreen = new ResourceSplashScreen(this, ("Loading Document from " + idip.getLoadSourceName()), "", true, false);
		System.out.println("Creating load thread");
		Thread loadThread = new Thread("LoaderThread") {
			public void run() {
				try {
					loadScreen.setStep("Loading Document");
					while (!loadScreen.isVisible()) try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {}
					
					ImDocument doc = idip.loadDocument(loadScreen);
					if (doc == null)
						return;
					String docName = ((String) doc.getAttribute(DOCUMENT_NAME_ATTRIBUTE));
					
					ggImagine.notifyDocumentOpened(doc, idip, loadScreen);
					ui.openDocument(new GgiDocumentEditorTab(doc, docName, null, null, ((idip.getSaveDestinationName() == null) ? null : idip)));
				}
				finally {
					loadScreen.dispose();
				}
			}
		};
		loadThread.start();
		loadScreen.setVisible(true);
	}
	
	private Map docDataCachePathsById = Collections.synchronizedMap(new HashMap());
	void registerDocDataCache(String docId, File docDataCache) {
		this.docDataCachePathsById.put(docId, docDataCache.getAbsolutePath());
	}
	
	void exit() {
		if (!this.ui.close())
			return;
		Settings actionUsageData = new Settings();
		NamedElementUsageStatistics.storeData(actionUsageData);
		try {
			this.goldenGate.storeApplicationSettings("GgImagine.actionUsage.cnfg", actionUsageData);
		}
		catch (IOException ioe) {
			System.out.println("Failed to store central GoldenGATE Imagine action usage statistics; " + ioe.getMessage());
			ioe.printStackTrace(System.out);
		}
		this.ggImagine.exit(ProgressMonitor.dummy);
		this.dispose();
	}
	
	private class GgiDoumentMarkupUI extends ImageDocumentMarkupUI {
//		GgiDoumentMarkupUI(GoldenGateImagine ggImagine, Settings ggiConfig) {
		GgiDoumentMarkupUI(GoldenGateImagine ggImagine) {
//			super(ggImagine, ggiConfig, null, null);
			super(ggImagine, null, null);
		}
//		protected FileMenuItem[] getFileMenuItems() {
//			return GoldenGateImagineUI.this.getFileMenuItems();
//		}
		protected WindowMenuElement[] getFileMenuElements() {
			return GoldenGateImagineUI.this.getFileMenuElements();
		}
		protected Window getMainWindow() {
			return GoldenGateImagineUI.this;
		}
		protected void handleDrop(Transferable dropped) {
			GoldenGateImagineUI.this.handleDrop(dropped);
		}
		protected File getLikelyExportDestination(ImageDocumentEditorTab idet) {
			File file = ((GgiDocumentEditorTab) idet).docSource;
			return ((file == null) ? GoldenGateImagineUI.this.fileChooser.getSelectedFile() : file);
		}
		protected boolean saveDocument(ImageDocumentEditorTab idet) {
			return false; // we never get here because our editor tab provides its own saving logic
		}
		public boolean closeDocument(ImageDocumentEditorTab idet) {
			if (!super.closeDocument(idet))
				return false;
			
			//	dispose document
			idet.getImDocumentPanel().document.dispose();
			
			//	clean up any cached files
			String docDataCachePath = ((String) GoldenGateImagineUI.this.docDataCachePathsById.get(idet.getImDocumentPanel().document.docId));
			if (docDataCachePath != null) {
				File docDataCache = new File(docDataCachePath);
				if (docDataCache.exists()) {
					File[] docDataFiles = docDataCache.listFiles();
					if (docDataFiles != null) {
						for (int f = 0; f < docDataFiles.length; f++)
							docDataFiles[f].delete();
					}
					docDataCache.delete();
				}
			}
			
			//	finally ...
			return true;
		}
		public boolean close() {
			return super.close(); // need to overwrite here to make it accessible
		}
	}
	
	GgiDocumentEditorTab getActiveDocument() {
		return ((GgiDocumentEditorTab) this.ui.getActiveDocument());
	}
	
	private class GgiDocumentEditorTab extends ImageDocumentEditorTab {
		File docSource;
		FileFilter docFormat = imfFileFilter;
		ImageDocumentIoProvider docIo;
		GgiDocumentEditorTab(ImDocument doc, String docName, File docSource, FileFilter docFormat, ImageDocumentIoProvider docIo) {
			super(GoldenGateImagineUI.this.ui, doc, docName);
			this.docSource = docSource;
			this.docFormat = ((docFormat == null) ? imfFileFilter : docFormat);
			this.docIo = docIo;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupUI.ImageDocumentEditorTab#save()
		 */
		public boolean save() {
			if (!this.isDirty())
				return true;
			else if (this.docSource != null)
				return this.saveAs(this.docSource, this.docFormat);
			else if (this.docIo != null)
				return this.saveAs(this.docIo);
			else return this.saveAs(GoldenGateImagineUI.this.fileChooser, GoldenGateImagineUI.this.fileChooserSize);
		}

		boolean saveAs(JFileChooser fileChooser, Dimension fileChooserSize) {
			clearFileFilters(fileChooser);
			fileChooser.addChoosableFileFilter(imfFileFilter);
			fileChooser.addChoosableFileFilter(imdFileFilter);
			//	TODO make sure to clear file name and populate with current document name
			fileChooser.setFileFilter(this.docFormat);
			if (this.docSource != null)
				fileChooser.setSelectedFile(this.docSource);
			else {
				File docFolder = fileChooser.getSelectedFile();
				if (docFolder != null)
					docFolder = docFolder.getAbsoluteFile();
				if ((docFolder != null) && docFolder.isFile())
					docFolder = docFolder.getParentFile();
				if (docFolder != null)
					fileChooser.setSelectedFile(new File(docFolder, this.getDocName()));
			}
			fileChooser.setPreferredSize(fileChooserSize);
			int choice = fileChooser.showSaveDialog(this);
			fileChooser.getSize(fileChooserSize);
			if (choice != JFileChooser.APPROVE_OPTION)
				return false;
			File file = fileChooser.getSelectedFile();
			if (file.isDirectory())
				return false;
			return this.saveAs(file, fileChooser.getFileFilter());
		}
		
		boolean saveAs(File file, final FileFilter fileFormat) {
			
			//	check file name
			if ((fileFormat == imfFileFilter) && !file.getName().endsWith(".imf")) {
				String fileName = file.getAbsolutePath();
				if (fileName.endsWith(".imd"))
					fileName = fileName.substring(0, (fileName.length() - ".imd".length()));
				file = new File(fileName + ".imf");
			}
			if ((fileFormat == imdFileFilter) && !file.getName().endsWith(".imd")) {
				String fileName = file.getAbsolutePath();
				if (fileName.endsWith(".imf"))
					fileName = fileName.substring(0, (fileName.length() - ".imf".length()));
				file = new File(fileName + ".imd");
			}
			
			//	create splash screen
			final ResourceSplashScreen saveScreen = new ResourceSplashScreen(GoldenGateImagineUI.this, "Saving Document, Please Wait", "", false, false);
			
			//	save document, in separate thread
			final boolean[] saveSuccess = {false};
			final File[] saveFile = {file};
			final String[] saveFileName = {file.getName()};
			Thread saveThread = new Thread() {
				public void run() {
					try {
						
						//	wait for splash screen to come up (we must not reach the dispose() line before the splash screen even comes up)
						while (!saveScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	notify listeners that saving is imminent
						GoldenGateImagineUI.this.ggImagine.notifyDocumentSaving(GgiDocumentEditorTab.this.getImDocumentPanel().document, saveFile[0], saveScreen);
						
						//	make way
						if (saveFile[0].exists()) {
							String fileName = saveFile[0].getAbsolutePath();
							saveFile[0].renameTo(new File(fileName + "." + System.currentTimeMillis() + ".old"));
							saveFile[0] = new File(fileName);
						}
						
						//	get document and initialize storage flags
//						long storageFlags = ImDocumentIO.STORAGE_MODE_CSV;
						ImDocument doc = GgiDocumentEditorTab.this.getImDocumentPanel().document;
						long storageFlags = ggImagine.getImfStorageFlags();
						
						//	save document to folder, and entry list as file
						if (fileFormat == imdFileFilter) {
							File docFolder = new File(saveFile[0].getAbsolutePath() + "ir");
//							if (!docFolder.exists())
//								docFolder.mkdirs();
							FolderImDocumentData docData;
							if (docFolder.exists()) {
								if (doc instanceof DataBackedImDocument) {
									ImDocumentData exDocData = ((DataBackedImDocument) doc).getDocumentData();
									if ((exDocData instanceof FolderImDocumentData) && exDocData.canStoreDocument() && docFolder.getAbsolutePath().equals(exDocData.getDocumentDataId()))
										docData = ((FolderImDocumentData) exDocData);
									else docData = new FolderImDocumentData(docFolder, storageFlags);
								}
								else docData = new FolderImDocumentData(docFolder, storageFlags);
							}
							else {
								docFolder.mkdirs();
								docData = new FolderImDocumentData(docFolder, storageFlags);
							}
//							ImDocumentEntry[] docEntries = ImDocumentIO.storeDocument(GgiDocumentEditorTab.this.getImDocumentPanel().document, docFolder, saveScreen);
//							ImDocumentEntry[] docEntries = ImDocumentIO.storeDocument(doc, docFolder, storageFlags, saveScreen);
							ImDocumentIO.storeDocument(doc, docData, saveScreen);
							BufferedWriter eOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile[0]), "UTF-8"));
//							for (int e = 0; e < docEntries.length; e++) {
////								eOut.write(docEntries[e].toTabString());
//								eOut.write(docEntries[e].toTabString(0 < storageFlags));
//								eOut.newLine();
//							}
							docData.writeEntryList(eOut, false); // in TSV mode, this add storage flags that were actually used (just like we need)
							eOut.flush();
							eOut.close();
						}
						
						//	save document to batch cache folder (internal entry list only)
						else if (fileFormat == batchCacheDocFileFilter) {
//							ImDocumentIO.storeDocument(GgiDocumentEditorTab.this.getImDocumentPanel().document, saveFile[0].getParentFile(), saveScreen);
							ImDocumentIO.storeDocument(doc, saveFile[0].getParentFile(), storageFlags /* will usually use existing flags from cache */, saveScreen);
							saveFileName[0] = saveFile[0].getParentFile().getName();
						}
						
						//	save document to zip archive
						else {
							OutputStream dOut = new BufferedOutputStream(new FileOutputStream(saveFile[0]));
//							ImDocumentIO.storeDocument(GgiDocumentEditorTab.this.getImDocumentPanel().document, dOut, saveScreen);
							ImDocumentIO.storeDocument(doc, dOut, storageFlags, saveScreen);
							dOut.flush();
							dOut.close();
						}
						
						//	remember saving
						GgiDocumentEditorTab.this.savedAs(saveFileName[0], saveFile[0], fileFormat);
						saveSuccess[0] = true;
						
						//	notify listeners of saving success
						GoldenGateImagineUI.this.ggImagine.notifyDocumentSaved(GgiDocumentEditorTab.this.getImDocumentPanel().document, saveFile[0], saveScreen);
					}
					
					//	catch saving cancellation at hands of listener
					catch (CancelSavingException cse) {
						if (!cse.isUserDecision) {
							cse.printStackTrace(System.out);
							JOptionPane.showMessageDialog(GgiDocumentEditorTab.this, ("An error occurred while saving the document to '" + saveFile[0].getAbsolutePath() + "':\n" + cse.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
						}
					}
					
					//	catch whatever might happen
					catch (Throwable t) {
						JOptionPane.showMessageDialog(GgiDocumentEditorTab.this, ("An error occurred while saving the document to '" + saveFile[0].getAbsolutePath() + "':\n" + t.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
						t.printStackTrace(System.out);
					}
					
					//	dispose splash screen
					finally {
						saveScreen.dispose();
					}
				}
			};
			saveThread.start();
			
			//	open splash screen (this waits)
			saveScreen.setVisible(true);
			
			//	finally ...
			return saveSuccess[0];
		}
		
		boolean saveAs(final ImageDocumentIoProvider idip) {
			
			//	create splash screen
			final ResourceSplashScreen saveScreen = new ResourceSplashScreen(GoldenGateImagineUI.this, "Saving Document, Please Wait", "", false, false);
			
			//	save document, in separate thread
			final boolean[] saveSuccess = {false};
			Thread saveThread = new Thread() {
				public void run() {
					try {
						
						//	wait for splash screen to come up (we must not reach the dispose() line before the splash screen even comes up)
						while (!saveScreen.isVisible()) try {
							Thread.sleep(10);
						} catch (InterruptedException ie) {}
						
						//	notify listeners that saving is imminent
						GoldenGateImagineUI.this.ggImagine.notifyDocumentSaving(GgiDocumentEditorTab.this.getImDocumentPanel().document, idip, saveScreen);
						
						//	save document
						String docName = idip.saveDocument(GgiDocumentEditorTab.this.getImDocumentPanel().document, GgiDocumentEditorTab.this.getDocName(), saveScreen);
						
						//	check success
						if (docName != null) {
							
							//	remember saving
							GgiDocumentEditorTab.this.savedAs(docName, idip);
							saveSuccess[0] = true;
							
							//	notify listeners of saving success
							GoldenGateImagineUI.this.ggImagine.notifyDocumentSaved(GgiDocumentEditorTab.this.getImDocumentPanel().document, idip, saveScreen);
						}
					}
					
					//	catch saving cancellation at hands of listener
					catch (CancelSavingException cse) {
						if (!cse.isUserDecision) {
							cse.printStackTrace(System.out);
							JOptionPane.showMessageDialog(GgiDocumentEditorTab.this, ("An error occurred while saving the document to " + idip.getSaveDestinationName() + ":\n" + cse.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
						}
					}
					
					//	catch whatever might happen
					catch (Throwable t) {
						t.printStackTrace(System.out);
						JOptionPane.showMessageDialog(GgiDocumentEditorTab.this, ("An error occurred while saving the document to " + idip.getSaveDestinationName() + ":\n" + t.getMessage()), "Error Saving Document", JOptionPane.ERROR_MESSAGE);
					}
					
					//	dispose splash screen
					finally {
						saveScreen.dispose();
					}
				}
			};
			saveThread.start();
			
			//	open splash screen (this waits)
			saveScreen.setVisible(true);
			
			//	finally ...
			return saveSuccess[0];
		}
		
		void savedAs(String saveDocName, File saveFile, FileFilter saveFileFormat) {
			this.savedAs(saveDocName, saveFile, saveFileFormat, null);
		}
		void savedAs(String saveDocName, ImageDocumentIoProvider saveDest) {
			this.savedAs(saveDocName, null, this.docFormat, saveDest);
		}
		private void savedAs(String saveDocName, File saveFile, FileFilter saveFileFormat, ImageDocumentIoProvider saveDest) {
			this.savedAs(saveDocName);
			this.docSource = saveFile;
			this.docFormat = saveFileFormat;
			this.docIo = saveDest;
		}
	}
	
	private static final FileFilter imfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".imf"));
		}
		public String getDescription() {
			return "Image Markup Files";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter imdFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".imd"));
		}
		public String getDescription() {
			return "Image Markup Directories";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter batchCacheDocFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || (file.getName().startsWith("entries.") && (file.getName().endsWith(".txt") || file.getName().endsWith(".tsv"))));
		}
		public String getDescription() {
			return "Batch Cached Documents";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter genericPdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter textPdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (born-digital)";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter hybridPdfFileFilterD = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (scanned, text layer digitized)";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter hybridPdfFileFilterV = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (scanned, text layer vectorized)";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static final FileFilter imagePdfFileFilter = new FileFilter() {
		public boolean accept(File file) {
			return (file.isDirectory() || file.getName().toLowerCase().endsWith(".pdf"));
		}
		public String getDescription() {
			return "PDF Documents (scanned, with or without OCR)";
		}
		public String toString() {
			return this.getDescription();
		}
	};
	private static void clearFileFilters(JFileChooser fileChooser) {
		FileFilter[] fileFilters = fileChooser.getChoosableFileFilters();
		for (int f = 0; f < fileFilters.length; f++)
			fileChooser.removeChoosableFileFilter(fileFilters[f]);
	}
}
