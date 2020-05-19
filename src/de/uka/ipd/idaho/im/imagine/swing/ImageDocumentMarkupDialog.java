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
package de.uka.ipd.idaho.im.imagine.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.swing.ImageDocumentMarkupUI.ImageDocumentEditorTab;

/**
 * Dialog for displaying and editing a single Image Markup documents, for use
 * in the UI of an application built around a GoldenGATE Imagine core. This
 * class displays the document, a main menu (including export functionality),
 * and view control. Document IO is up to sub classes, as is adding any button
 * panel if required.<br/>
 * By default, the dialog only contains the document markup UI in the
 * <code>BorderLayout.CENTER</code> position. Client code, mainly sub classes,
 * may add other components around it if required.
 * 
 * @author sautter
 */
public abstract class ImageDocumentMarkupDialog extends DialogPanel implements ImagingConstants, GoldenGateConstants {
	private DialogDoumentMarkupUI ui;
	
	/** Constructor
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 * @param doc the document to display
	 * @param docName the name of the document to display
	 */
	protected ImageDocumentMarkupDialog(GoldenGateImagine ggImagine, Settings ggiConfig, ImDocument doc, String docName) {
		super("GoldenGATE Imagine - " + docName);
		this.init(ggImagine, new DialogDoumentMarkupUI(ggImagine, ggiConfig, doc, docName));
	}
	
	/** Constructor
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 * @param docTag the document tab to display
	 */
	protected ImageDocumentMarkupDialog(GoldenGateImagine ggImagine, Settings ggiConfig, ImageDocumentEditorTab docTab) {
		super("GoldenGATE Imagine - " + docTab.getDocName());
		this.init(ggImagine, new DialogDoumentMarkupUI(ggImagine, ggiConfig, docTab));
	}
	
	private void init(GoldenGateImagine ggImagine, DialogDoumentMarkupUI ui) {
		
		//	use UI in single-document mode
		this.ui = ui;
		
		//	set window icon
		this.getDialog().setIconImage(ggImagine.getGoldenGateIcon());
		
		//	make sure we exit on window closing
		this.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				close();
			}
			public void windowClosing(WindowEvent we) {
				close();
			}
		});
		
		//	assemble major parts
		this.setLayout(new BorderLayout());
		this.add(this.ui, BorderLayout.CENTER);
		this.setSize(1000, 800);
		this.setLocationRelativeTo(null);
	}
	
	/**
	 * Retrieve the enclosed markup UI. This method intentionally returns a
	 * generic JComponent because the only reason sub classes are intended to
	 * access the markup UI is for dialog content layout purposes.
	 * @return the markup UI component
	 */
	protected JComponent getMarkupUI() {
		return this.ui;
	}
	
	void close() {
		if (!this.ui.close())
			return;
		this.dispose();
	}
	
	private class DialogDoumentMarkupUI extends ImageDocumentMarkupUI {
		DialogDoumentMarkupUI(GoldenGateImagine ggImagine, Settings ggiConfig, ImDocument doc, String docName) {
			super(ggImagine, ggiConfig, doc, docName);
		}
		DialogDoumentMarkupUI(GoldenGateImagine ggImagine, Settings ggiConfig, ImageDocumentEditorTab docTab) {
			super(ggImagine, ggiConfig, docTab);
		}
		protected JMenuItem[] getFileMenuItems() {
			return ImageDocumentMarkupDialog.this.getFileMenuItems();
		}
		protected void documentNameChanged(ImageDocumentEditorTab idet) {
			super.documentNameChanged(idet);
			ImageDocumentMarkupDialog.this.setTitle("GoldenGATE Imagine - " + idet.getDocName());
		}
		protected boolean saveDocument(ImageDocumentEditorTab idet) {
			return ImageDocumentMarkupDialog.this.saveDocument(idet);
		}
		public boolean closeDocument(ImageDocumentEditorTab idet) {
			if (!super.closeDocument(idet))
				return false;
			ImageDocumentMarkupDialog.this.close();
			return true;
		}
		protected Window getMainWindow() {
			return ImageDocumentMarkupDialog.this.getDialog();
		}
	}
	
	/**
	 * Retrieve the embedded markup UI.
	 * @return the embedded markup UI
	 */
	public ImageDocumentMarkupUI getMarkupUi() {
		return this.ui;
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
}