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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.font.TextLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.WindowConstants;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceSplashScreen;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImDocument.ImDocumentListener;
import de.uka.ipd.idaho.im.ImFont;
import de.uka.ipd.idaho.im.ImObject;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImSupplement;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ClickActionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionListener;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageEditToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ReactionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.AtomicActionListener;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.PagePoint;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.PageThumbnail;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.SelectionAction;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.TwoClickActionMessenger;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.TwoClickSelectionAction;
import de.uka.ipd.idaho.im.util.ImImageEditorPanel.ImImageEditTool;

/**
 * Panel for displaying and editing an Image Markup document, intended for use
 * in the UI of an application built around a GoldenGATE Imagine core. This
 * class handles display control, integration with GoldenGATE Imagine plug-ins
 * (selection actions, reactions, drop handling, and display extensions), and
 * 'Undo' management. The latter reverts atomic actions in corresponding
 * inverse atomic actions whose IDs are the inverse of the IDs of the original
 * actions. The IDs of atomic actions reverting changes made individually are
 * always -1. Further, this class provides mounting points for integration in a
 * window based UI.<br/>
 * By default, the panel contains the document display and its associated view
 * control in the <code>BorderLayout.CENTER</code> and <code>BorderLayout.EAST</code>
 * positions, respectively. Client code, mainly sub classes, may add other
 * components around them if required.
 * 
 * @author sautter
 */
public abstract class ImageDocumentMarkupPanel extends JPanel implements ImagingConstants, DisplayExtensionListener {
	final GoldenGateImagine ggImagine;
	final Settings ggiConfig;
	ImageDocumentMarkupUI parent;
	
	final SelectionActionUsageStats saUsageStats;
	
	final ImDocumentMarkupPanel idmp;
	final JScrollPane idmpBox;
	Rectangle idmpViewSize;
	
	static final int fastScrollEnterRatioDenom = 20;
	static final int fastScrollMaintainRatioDenom = 20;
	boolean idmpBoxInFastScroll = false;
	
	private ImDocumentListener undoRecorder;
	final LinkedList undoActions = new LinkedList();
	private MultipartUndoAction multipartUndoAction = null;
	boolean inUndoAction = false;
	
	private ImDocumentListener reactionTrigger = null;
	boolean imToolActive = false;
	
	int modCount = 0;
	private int savedModCount = 0;
	
	/**
	 * Constructor
	 * @param doc the document to display
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 */
	protected ImageDocumentMarkupPanel(ImDocument doc, GoldenGateImagine ggImagine, Settings ggiConfig) {
		super(new BorderLayout(), true);
		this.ggImagine = ggImagine;
		this.ggiConfig = ggiConfig;
		
		this.idmp = new ImageDocumentEditorPanel(doc);
		
		//	inject highlight colors for annotations, regions, and text streams
		Settings annotationColors = this.ggiConfig.getSubset("annotation.color");
		String[] annotationTypes = annotationColors.getKeys();
		for (int t = 0; t < annotationTypes.length; t++) {
			Color ac = GoldenGateImagine.getColor(annotationColors.getSetting(annotationTypes[t]));
			if (ac != null)
				this.idmp.setAnnotationColor(annotationTypes[t], ac);
		}
		Settings layoutObjectColors = this.ggiConfig.getSubset("layoutObject.color");
		String[] layoutObjectTypes = layoutObjectColors.getKeys();
		for (int t = 0; t < layoutObjectTypes.length; t++) {
			Color loc = GoldenGateImagine.getColor(layoutObjectColors.getSetting(layoutObjectTypes[t]));
			if (loc != null)
				this.idmp.setLayoutObjectColor(layoutObjectTypes[t], loc);
		}
		Settings textStreamColors = this.ggiConfig.getSubset("textStream.color");
		String[] textStreamTypes = textStreamColors.getKeys();
		for (int t = 0; t < textStreamTypes.length; t++) {
			Color tsc = GoldenGateImagine.getColor(textStreamColors.getSetting(textStreamTypes[t]));
			if (tsc != null)
				this.idmp.setTextStreamTypeColor(textStreamTypes[t], tsc);
		}
		
		//	also get legacy highlight colors from XML document editor panel
		Settings legacyAnnotationColors = this.ggImagine.getConfiguration().getSettings().getSubset("AEP.ACS");
		String[] legacyAnnotationTypes = legacyAnnotationColors.getKeys();
		for (int t = 0; t < legacyAnnotationTypes.length; t++) {
			if (this.idmp.getAnnotationColor(legacyAnnotationTypes[t]) != null)
				continue;
			Color ac = GoldenGateImagine.getColor(legacyAnnotationColors.getSetting(legacyAnnotationTypes[t]));
			if (ac != null)
				this.idmp.setAnnotationColor(legacyAnnotationTypes[t], ac);
		}
		
		//	get singleton selection action usage stats
		this.saUsageStats = getSelectionActionUsageStats(this.ggiConfig);
		
		//	prepare recording UNDO actions
		this.undoRecorder = new UndoRecorder();
		this.idmp.document.addDocumentListener(this.undoRecorder);
		
		//	get reaction providers
		ReactionProvider[] reactionProviders = this.ggImagine.getReactionProviders();
		System.out.println("Got " + reactionProviders.length + " reaction providers");
		if (reactionProviders.length != 0) {
			this.reactionTrigger = new ReactionTrigger(reactionProviders);
			this.idmp.document.addDocumentListener(this.reactionTrigger);
		}
		
		//	distribute display extension changes to individual editor tabs
		this.ggImagine.addDisplayExtensionListener(this);
		
		//	get drop handlers
		final ImageDocumentDropHandler[] dropHandlers = this.ggImagine.getDropHandlers();
		
		//	add drop target if any drop handlers present
		if (dropHandlers.length != 0) {
			DropTarget dropTarget = new DropTarget(this.idmp, new DropTargetAdapter() {
				public void drop(DropTargetDropEvent dtde) {
					dtde.acceptDrop(dtde.getDropAction()); // we can do this only once, and we have to do it before inspecting data
					if (!this.handleDrop(dtde))
						ImageDocumentMarkupPanel.this.handleDrop(dtde.getTransferable());
				}
				private boolean handleDrop(DropTargetDropEvent dtde) {
					PagePoint dpp = idmp.pagePointAt(dtde.getLocation().x, dtde.getLocation().y);
					if (dpp == null)
						return false;
					for (int h = 0; h < dropHandlers.length; h++) try {
						if (dropHandlers[h].handleDrop(idmp, dpp.page, dpp.x, dpp.y, dtde))
							return true;
					}
					catch (Exception e) {
						e.printStackTrace(System.out);
					}
					return false;
				}
			});
			dropTarget.setActive(true);
		}
		
		//	make document view scrollable
		this.idmpBox = new JScrollPane();
		this.idmpBox.setViewport(new IdmpViewport(this.idmp));
		this.idmpViewSize = this.idmpBox.getViewport().getVisibleRect();
		
		//	adjust primary target of mouse wheel to page alignment, and zoom with Ctrl plus mouse wheel
		this.idmpBox.setWheelScrollingEnabled(false);
		this.idmpBox.addMouseWheelListener(new MouseAdapter() {
			public void mouseWheelMoved(MouseWheelEvent mwe) {
				if (mwe.isControlDown()) {
					if (parent == null)
						return;
					boolean zoomIn = (mwe.getWheelRotation() < 0);
					int zoomSteps = Math.abs(mwe.getWheelRotation());
					for (int s = 0; s < zoomSteps; s++) {
						if (zoomIn)
							parent.viewControl.zoomIn();
						else parent.viewControl.zoomOut();
					}
					parent.viewControl.requestFocusInWindow();
				}
				else {
					JScrollBar tsb = (((idmp.getSideBySidePages() == 1) != mwe.isShiftDown()) ? idmpBox.getVerticalScrollBar() : idmpBox.getHorizontalScrollBar());
					if (!tsb.isVisible())
						tsb = (((idmp.getSideBySidePages() == 1) != mwe.isShiftDown()) ? idmpBox.getHorizontalScrollBar() : idmpBox.getVerticalScrollBar());
					if (!tsb.isVisible())
						return;
					int valueDelta = (tsb.getBlockIncrement() * mwe.getWheelRotation());
					if (valueDelta < 0)
						tsb.setValue(Math.max(tsb.getMinimum(), (tsb.getValue() + valueDelta)));
					else if (valueDelta > 0)
						tsb.setValue(Math.min(tsb.getMaximum(), (tsb.getValue() + valueDelta)));
				}
			}
		});
		
		//	set scroll distances
		final JScrollBar vsb = this.idmpBox.getVerticalScrollBar();
		vsb.setUnitIncrement(this.idmpViewSize.height / 10);
		vsb.setBlockIncrement(this.idmpViewSize.height / 3);
		final JScrollBar hsb = this.idmpBox.getHorizontalScrollBar();
		hsb.setUnitIncrement(this.idmpViewSize.width / 10);
		hsb.setBlockIncrement(this.idmpViewSize.width / 3);
		
		//	track window resizing
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent ce) {
				idmpViewSize = idmpBox.getViewport().getViewRect();
				vsb.setUnitIncrement(idmpViewSize.height / 10);
				vsb.setBlockIncrement(idmpViewSize.height / 3);
				hsb.setUnitIncrement(idmpViewSize.width / 10);
				hsb.setBlockIncrement(idmpViewSize.width / 3);
			}
		});
		
		//	make scroll tractable, and enable fast scrolling (disables page rendering when scrolling at high speed)
		vsb.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				setIdmpBoxFastScroll(false);
			}
		});
		vsb.addAdjustmentListener(new AdjustmentListener() {
			private AdjustmentEvent lastAe = null;
			private long lastAeTime = -1;
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (idmp.getSideBySidePages() != 1) {
					this.lastAe = null;
					this.lastAeTime = -1;
					return;
				}
				updateScrollPosition();
				long aeTime = System.currentTimeMillis();
				//	valueIsAdjusting is only true if mouse button held down in scrollbar _outside_ the buttons at the ends (on either side of the know, or on knob proper)
				if (ae.getValueIsAdjusting()) {
					float valueDelta = ((this.lastAe == null) ? ae.getValue() : (ae.getValue() - this.lastAe.getValue()));
					int timeDelta = ((this.lastAe == null) ? 10 : Math.max(10, ((int) (aeTime - this.lastAeTime))));
					setIdmpBoxFastScroll(Math.abs(valueDelta / timeDelta) > Math.max(1, (idmpViewSize.height / (idmpBoxInFastScroll ? fastScrollMaintainRatioDenom : fastScrollEnterRatioDenom))));
				}
				else setIdmpBoxFastScroll(false);
				this.lastAe = ae;
				this.lastAeTime = aeTime;
			}
		});
		hsb.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				setIdmpBoxFastScroll(false);
			}
		});
		hsb.addAdjustmentListener(new AdjustmentListener() {
			private AdjustmentEvent lastAe = null;
			private long lastAeTime = -1;
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if (idmp.getSideBySidePages() == 1) {
					this.lastAe = null;
					this.lastAeTime = -1;
					return;
				}
				updateScrollPosition();
				long aeTime = System.currentTimeMillis();
				//	valueIsAdjusting is only true if mouse button held down in scrollbar _outside_ the buttons at the ends
				if (ae.getValueIsAdjusting()) {
					float valueDelta = ((this.lastAe == null) ? ae.getValue() : (ae.getValue() - this.lastAe.getValue()));
					int timeDelta = ((this.lastAe == null) ? 10 : Math.max(10, ((int) (aeTime - this.lastAeTime))));
					setIdmpBoxFastScroll(Math.abs(valueDelta / timeDelta) > Math.max(1, (idmpViewSize.width / (idmpBoxInFastScroll ? fastScrollMaintainRatioDenom : fastScrollEnterRatioDenom))));
				}
				else setIdmpBoxFastScroll(false);
				this.lastAe = ae;
				this.lastAeTime = aeTime;
			}
		});
		
		//	assemble UI components
		this.add(this.idmpBox, BorderLayout.CENTER);
		this.add(this.idmp.getControlPanel(), BorderLayout.EAST);
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
	
	/**
	 * Update the scroll position indicator of the surrounding UI, e.g. when a
	 * markup panel is newly opened, or when it is selected in a multi-document
	 * UI.
	 */
	public void updateScrollPosition() {
		Rectangle viewRect = this.idmpBox.getViewport().getViewRect();
		int viewCenterX = ((int) (viewRect.getMinX() + (viewRect.getWidth() / 2)));
		int viewCenterY = ((int) (viewRect.getMinY() + (viewRect.getHeight() / 2)));
		PagePoint viewPagePoint = this.idmp.pagePointAt(viewCenterX, viewCenterY);
		ImPage viewPage;
		if (viewPagePoint == null) // happens on opening, before actually becoming visible
			viewPage = this.idmp.document.getPage(this.idmp.document.getFirstPageId());
		else viewPage = viewPagePoint.page;
		Object pageNumber = viewPage.getAttribute(PAGE_NUMBER_ATTRIBUTE);
		this.scrollPositionChanged("Page " + ((viewPage.pageId - this.idmp.document.getFirstPageId()) + 1) + " / " + this.idmp.document.getPageCount() + ((pageNumber == null) ? "" : (" (Nr. " + pageNumber + ")")));
	}
	
	void setIdmpBoxFastScroll(boolean ibfs) {
		if (this.idmpBoxInFastScroll == ibfs)
			return;
		if (ibfs) {
//			System.out.println("Entering fast scroll mode");
			this.idmpBoxInFastScroll = true;
		}
		else {
//			System.out.println("Quitting fast scroll mode");
			this.idmpBoxInFastScroll = false;
			this.idmp.validate();
			this.idmp.repaint();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionListener#displayExtensionsModified(de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel)
	 */
	public void displayExtensionsModified(ImDocumentMarkupPanel idmp) {
		if ((idmp == null) || (idmp == this.idmp))
			this.idmp.setDisplayExtensionsModified();
	}
	
	private class ImageDocumentEditorPanel extends ImDocumentMarkupPanel implements AtomicActionListener {
		ImageDocumentEditorPanel(ImDocument document) {
			super(document);
			this.addAtomicActionListener(this);
		}
		public void atomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ProgressMonitor pm) {
			if (inUndoAction)
				return; // no 'Undo' recording on 'Undo' ... TODO maybe use this for 'Redo' ...
			startMultipartUndoAction(id, label);
		}
		public void atomicActionFinishing(long id, ProgressMonitor pm) { /* no follow-up actions from our end */ }
		public void atomicActionFinished(long id, ProgressMonitor pm) {
			if (inUndoAction)
				return; // no 'Undo' recording on 'Undo' ... TODO maybe use this for 'Redo' ...
			finishMultipartUndoAction();
		}
		protected SelectionAction[] getActions(ImWord start, ImWord end) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getActions(start, end)));
			SelectionActionProvider[] saps = ImageDocumentMarkupPanel.this.ggImagine.getSelectionActionProviders();
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
		protected ClickSelectionAction[] getClickActions(ImWord word, int clickCount) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getClickActions(word, clickCount)));
			ClickActionProvider[] caps = ImageDocumentMarkupPanel.this.ggImagine.getClickActionProviders();
			for (int p = 0; p < caps.length; p++) {
				ClickSelectionAction[] csas = caps[p].getActions(word, clickCount, this);
				if ((csas != null) && (csas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(csas));
				}
			}
			return ((ClickSelectionAction[]) actions.toArray(new ClickSelectionAction[actions.size()]));
		}
		protected SelectionAction[] getActions(ImPage page, Point start, Point end) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getActions(page, start, end)));
			SelectionActionProvider[] saps = ImageDocumentMarkupPanel.this.ggImagine.getSelectionActionProviders();
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
		protected ClickSelectionAction[] getClickActions(ImPage page, Point point, int clickCount) {
			LinkedList actions = new LinkedList(Arrays.asList(super.getClickActions(page, point, clickCount)));
			ClickActionProvider[] caps = ImageDocumentMarkupPanel.this.ggImagine.getClickActionProviders();
			for (int p = 0; p < caps.length; p++) {
				ClickSelectionAction[] csas = caps[p].getActions(page, point, clickCount, this);
				if ((csas != null) && (csas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(csas));
				}
			}
			return ((ClickSelectionAction[]) actions.toArray(new ClickSelectionAction[actions.size()]));
		}
		protected boolean[] markAdvancedSelectionActions(SelectionAction[] sas) {
			return ImageDocumentMarkupPanel.this.saUsageStats.markAdvancedSelectionActions(sas);
		}
		protected void selectionActionPerformed(SelectionAction sa) {
			ImageDocumentMarkupPanel.this.saUsageStats.selectionActionUsed(sa);
		}
		protected DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
			LinkedList degs = new LinkedList();
			DisplayExtensionProvider[] deps = ImageDocumentMarkupPanel.this.ggImagine.getDisplayExtensionProviders();
			for (int p = 0; p < deps.length; p++) {
				DisplayExtension[] des = deps[p].getDisplayExtensions();
				for (int e = 0; e < des.length; e++) {
					if (des[e].isActive())
						degs.addAll(Arrays.asList(des[e].getExtensionGraphics(page, this)));
				}
			}
			return ((DisplayExtensionGraphics[]) degs.toArray(new DisplayExtensionGraphics[degs.size()]));
		}
		protected ImImageEditTool[] getImageEditTools() {
			LinkedList tools = new LinkedList(Arrays.asList(super.getImageEditTools()));
			ImageEditToolProvider[] ietps = ImageDocumentMarkupPanel.this.ggImagine.getImageEditToolProviders();
			for (int p = 0; p < ietps.length; p++) {
				ImImageEditTool[] iets = ietps[p].getImageEditTools();
				if (iets != null)
					tools.addAll(Arrays.asList(iets));
			}
			return ((ImImageEditTool[]) tools.toArray(new ImImageEditTool[tools.size()]));
		}
		public ProgressMonitor getProgressMonitor(String title, String text, boolean supportPauseResume, boolean supportAbort) {
			return new ResourceSplashScreen(getMainWindow(), title, text, supportPauseResume, supportAbort);
		}
		public boolean setDisplayOverlay(DisplayOverlay overlay, int pageId) {
			if (!super.setDisplayOverlay(overlay, pageId))
				return false;
			
			//	scroll to show this thing
			Point ol = overlay.getOnPageLocation();
			Dimension os = overlay.getOnPageSize();
			int pid = overlay.getPageId();
			
			//	get position of overlay, and compare to current view
			Rectangle vpPos = idmpBox.getViewport().getViewRect();
			Rectangle oPos = this.getPosition(new BoundingBox(ol.x, (ol.x + os.width), ol.y, (ol.y + os.height)), pid);
			
			//	scroll selection to view if required (moving near center)
			if (!vpPos.contains(oPos)) {
//				idmpBox.getViewport().scrollRectToVisible(wsPos); // DOESN'T SEEM TO WORK AS SUPPOSED TO, FOR WHATEVER REASON
				int vx;
				if ((vpPos.x <= oPos.x) && ((vpPos.x + vpPos.width) >= (oPos.x + oPos.width))) // selection in bounds horizontally, no need for scrolling
					vx = vpPos.x;
				else /* center selection in viewport */ {
					int ocx = (oPos.x + (oPos.width / 2));
					vx = (ocx - (vpPos.width / 2));
					if (vx < 0)
						vx = 0;
				}
				int vy;
				if ((vpPos.y <= oPos.y) && ((vpPos.y + vpPos.height) >= (oPos.y + oPos.height))) // selection in bounds vertically, no need for scrolling
					vy = vpPos.y;
				else /* center selection in viewport */ {
					int ocy = (oPos.y + (oPos.height / 2));
					vy = (ocy - (vpPos.height / 2));
					if (vy < 0)
						vy = 0;
				}
				idmpBox.getViewport().setViewPosition(new Point(vx, vy));
			}
			
			//	pass on super class success
			return true;
		}
		public boolean setWordSelection(ImWord startWord, ImWord endWord) {
			if (!super.setWordSelection(startWord, endWord))
				return false;
			
			//	get position of word selection, and compare to current view
			Rectangle vpPos = idmpBox.getViewport().getViewRect();
			Rectangle swPos = this.getPosition(startWord);
			if (swPos == null)
				return true;
			Rectangle wsPos;
			if ((endWord == null) || (endWord == startWord))
				wsPos = swPos;
			else {
				Rectangle ewPos = this.getPosition(endWord);
				
				//	word selection doesn't fit view vertically, use start word
				if (vpPos.height < (ewPos.y + ewPos.height - swPos.y))
					wsPos = swPos;
				
				//	word selection doesn't fit view horizontally, use start word
				else if (vpPos.width < (Math.max((swPos.x + swPos.width), (ewPos.x + ewPos.width)) - Math.min(swPos.x, ewPos.x)))
					wsPos = swPos;
				
				//	word selection fits view
				else wsPos = swPos.union(ewPos);
			}
			
			//	scroll selection to view if required (moving near center)
			if (!vpPos.contains(wsPos)) {
//				idmpBox.getViewport().scrollRectToVisible(wsPos); // DOESN'T SEEM TO WORK AS SUPPOSED TO, FOR WHATEVER REASON
				int vx;
				if ((vpPos.x <= wsPos.x) && ((vpPos.x + vpPos.width) >= (wsPos.x + wsPos.width))) // selection in bounds horizontally, no need for scrolling
					vx = vpPos.x;
				else /* center selection in viewport */ {
					int wscx = (wsPos.x + (wsPos.width / 2));
					vx = (wscx - (vpPos.width / 2));
					if (vpPos.x < vx) // scrolling right, don't go all that far
						vx -= (vpPos.width / 4);
					else if (vpPos.x > vx) // scrolling left, don't go all that far
						vx += (vpPos.width / 4);
					if (vx < 0)
						vx = 0;
				}
				int vy;
				if ((vpPos.y <= wsPos.y) && ((vpPos.y + vpPos.height) >= (wsPos.y + wsPos.height))) // selection in bounds vertically, no need for scrolling
					vy = vpPos.y;
				else /* center selection in viewport */ {
					int wscy = (wsPos.y + (wsPos.height / 2));
					vy = (wscy - (vpPos.height / 2));
					if (vpPos.y < vy) // scrolling down, don't go all that far
						vy -= (vpPos.height / 4);
					else if (vpPos.y > vy) // scrolling up, don't go all that far
						vy += (vpPos.height / 4);
					if (vy < 0)
						vy = 0;
				}
				idmpBox.getViewport().setViewPosition(new Point(vx, vy));
			}
			
			//	pass on super class success
			return true;
		}
		public void setPageVisible(int pageId, boolean pv) {
			if (pv == this.isPageVisible(pageId))
				return;
			super.setPageVisible(pageId, pv);
			ImageDocumentMarkupPanel.this.validate();
			ImageDocumentMarkupPanel.this.repaint();
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
			ImageDocumentMarkupPanel.this.validate();
			ImageDocumentMarkupPanel.this.repaint();
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
			ImageDocumentMarkupPanel.this.validate();
			ImageDocumentMarkupPanel.this.repaint();
		}
		public void setSideBySidePages(int sbsp) {
			if (sbsp == this.getSideBySidePages())
				return;
			super.setSideBySidePages(sbsp);
			ImageDocumentMarkupPanel.this.validate();
			ImageDocumentMarkupPanel.this.repaint();
		}
		public void applyMarkupTool(ImageMarkupTool imt, ImAnnotation annot) {
			try {
				imToolActive = true;
				super.applyMarkupTool(imt, annot);
			}
			finally {
				imToolActive = false;
			}
		}
		public void paint(Graphics graphics) {
			if (idmpBoxInFastScroll)
				return;
			super.paint(graphics);
		}
		public void validate() {
			if (idmpBoxInFastScroll)
				return;
			super.validate();
		}
		public void repaint() {
			if (idmpBoxInFastScroll)
				return;
			super.repaint();
		}
	}
	
	private class UndoRecorder implements ImDocumentListener {
		public void typeChanged(final ImObject object, final String oldType) {
			if (inUndoAction)
				return;
			addUndoAction(new UndoAction(("Change Object Type to '" + object.getType() + "'"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					object.setType(oldType);
				}
//				int doExecute() {
//					object.setType(oldType);
//					return 1;
//				}
			});
		}
		public void regionAdded(final ImRegion region) {
			if (inUndoAction)
				return;
			addUndoAction(new UndoAction(("Add '" + region.getType() + "' Region"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					idmp.document.getPage(region.pageId).removeRegion(region);
				}
//				int doExecute() {
//					idmp.document.getPage(region.pageId).removeRegion(region);
//					return 1;
//				}
			});
		}
		public void regionRemoved(final ImRegion region) {
			if (inUndoAction)
				return;
			if (region instanceof ImWord)
				addUndoAction(new UndoAction(("Remove Word '" + region.getAttribute(ImWord.STRING_ATTRIBUTE) + "'"), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						idmp.document.getPage(region.pageId).addWord((ImWord) region);
					}
//					int doExecute() {
//						idmp.document.getPage(region.pageId).addWord((ImWord) region);
//						return 1;
//					}
				});
			else addUndoAction(new UndoAction(("Remove '" + region.getType() + "' Region"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					idmp.document.getPage(region.pageId).addRegion(region);
				}
//				int doExecute() {
//					idmp.document.getPage(region.pageId).addRegion(region);
//					return 1;
//				}
			});
		}
		public void attributeChanged(final ImObject object, final String attributeName, final Object oldValue) {
			if (inUndoAction)
				return;
			if (oldValue == null)
				addUndoAction(new UndoAction(("Add " + attributeName + " Attribute to " + object.getType()), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						object.setAttribute(attributeName, oldValue); // we need to set here instead of removing, as some objects have built-in special attributes (ImWord !!!)
					}
//					int doExecute() {
//						object.setAttribute(attributeName, oldValue); // we need to set here instead of removing, as some objects have built-in special attributes (ImWord !!!)
//						return 1;
//					}
				});
			else if (object.getAttribute(attributeName) == null)
				addUndoAction(new UndoAction(("Remove '" + attributeName + "' Attribute from " + object.getType()), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						object.setAttribute(attributeName, oldValue);
					}
//					int doExecute() {
//						object.setAttribute(attributeName, oldValue);
//						return 1;
//					}
				});
			else addUndoAction(new UndoAction(("Change '" + attributeName + "' Attribute of " + object.getType() + " to '" + object.getAttribute(attributeName).toString() + "'"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					object.setAttribute(attributeName, oldValue);
				}
//				int doExecute() {
//					object.setAttribute(attributeName, oldValue);
//					return 1;
//				}
			});
		}
		public void supplementChanged(final String supplementId, final ImSupplement oldValue) {
			if (inUndoAction)
				return;
			if (oldValue == null) {
				final ImSupplement newValue = idmp.document.getSupplement(supplementId);
				addUndoAction(new UndoAction(("Add '" + supplementId + "' Supplement"), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						idmp.document.removeSupplement(newValue);
					}
//					int doExecute() {
//						idmp.document.removeSupplement(newValue);
//						return 1;
//					}
				});
			}
			else if (idmp.document.getSupplement(supplementId) == null)
				addUndoAction(new UndoAction(("Remove '" + supplementId + "' Supplement"), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						idmp.document.addSupplement(oldValue);
					}
//					int doExecute() {
//						idmp.document.addSupplement(oldValue);
//						return 1;
//					}
				});
			else addUndoAction(new UndoAction(("Change '" + supplementId + "' Supplemen"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					idmp.document.addSupplement(oldValue);
				}
//				int doExecute() {
//					idmp.document.addSupplement(oldValue);
//					return 1;
//				}
			});
		}
		public void fontChanged(final String fontName, final ImFont oldValue) {
			if (inUndoAction)
				return;
			if (oldValue == null) {
				final ImFont newValue = idmp.document.getFont(fontName);
				addUndoAction(new UndoAction(("Add Font '" + fontName + "'"), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						idmp.document.removeFont(newValue);
					}
//					int doExecute() {
//						idmp.document.removeSupplement(newValue);
//						return 1;
//					}
				});
			}
			else if (idmp.document.getFont(fontName) == null)
				addUndoAction(new UndoAction(("Remove Font '" + fontName + "'"), ImageDocumentMarkupPanel.this) {
					void doExecute() {
						idmp.document.addFont(oldValue);
					}
//					int doExecute() {
//						idmp.document.addSupplement(oldValue);
//						return 1;
//					}
				});
			else addUndoAction(new UndoAction(("Replace Font '" + fontName + "'"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
					idmp.document.addFont(oldValue);
				}
//				int doExecute() {
//					idmp.document.addSupplement(oldValue);
//					return 1;
//				}
			});
		}
		public void annotationAdded(final ImAnnotation annotation) {
			if (inUndoAction)
				return;
			addUndoAction(new UndoAction(("Add '" + annotation.getType() + "' Annotation"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
//					/* We need to re-get annotation and make our own
//					 * comparison, as removing and re-adding thwarts
//					 * this simple approach */
//					ImAnnotation[] annots = annotation.getDocument().getAnnotations(annotation.getFirstWord(), null);
//					for (int a = 0; a < annots.length; a++) {
//						if (!annots[a].getLastWord().getLocalID().equals(annotation.getLastWord().getLocalID()))
//							continue;
//						if (!annots[a].getType().equals(annotation.getType()))
//							continue;
//						idmp.document.removeAnnotation(annots[a]);
//						break;
//					}
					idmp.document.removeAnnotation(annotation);
				}
//				int doExecute() {
////					/* We need to re-get annotation and make our own
////					 * comparison, as removing and re-adding thwarts
////					 * this simple approach */
////					ImAnnotation[] annots = annotation.getDocument().getAnnotations(annotation.getFirstWord(), null);
////					for (int a = 0; a < annots.length; a++) {
////						if (!annots[a].getLastWord().getLocalID().equals(annotation.getLastWord().getLocalID()))
////							continue;
////						if (!annots[a].getType().equals(annotation.getType()))
////							continue;
////						idmp.document.removeAnnotation(annots[a]);
////						break;
////					}
//					idmp.document.removeAnnotation(annotation);
//					return 1;
//				}
			});
		}
		public void annotationRemoved(final ImAnnotation annotation) {
			if (inUndoAction)
				return;
			addUndoAction(new UndoAction(("Remove '" + annotation.getType() + "' Annotation"), ImageDocumentMarkupPanel.this) {
				void doExecute() {
//					ImAnnotation reAnnot = idmp.document.addAnnotation(annotation.getFirstWord(), annotation.getLastWord(), annotation.getType());
//					if (reAnnot != null)
//						reAnnot.copyAttributes(annotation);
					idmp.document.addAnnotation(annotation);
				}
//				int doExecute() {
////					ImAnnotation reAnnot = idmp.document.addAnnotation(annotation.getFirstWord(), annotation.getLastWord(), annotation.getType());
////					if (reAnnot != null)
////						reAnnot.copyAttributes(annotation);
//					idmp.document.addAnnotation(annotation);
//					return 1;
//				}
			});
		}
	}
	
	private class ReactionTrigger implements ImDocumentListener {
		private ReactionProvider[] reactionProviders;
		private HashSet inReactionObjects = new HashSet();
		ReactionTrigger(ReactionProvider[] reactionProviders) {
			this.reactionProviders = reactionProviders;
		}
		public void typeChanged(ImObject object, String oldType) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(object))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].typeChanged(object, oldType, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to object type change: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(object);
			}
		}
		public void regionAdded(ImRegion region) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(region))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].regionAdded(region, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to region addition: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(region);
			}
		}
		public void regionRemoved(ImRegion region) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(region))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].regionRemoved(region, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to region removal: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(region);
			}
		}
		public void attributeChanged(ImObject object, String attributeName, Object oldValue) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(object))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].attributeChanged(object, attributeName, oldValue, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to object attribute change: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(object);
			}
		}
		public void supplementChanged(String supplementId, ImSupplement oldValue) {
			//	no reaction triggering for supplement modifications
		}
		public void fontChanged(String fontName, ImFont oldValue) {
			//	no reaction triggering for font modifications
		}
		public void annotationAdded(ImAnnotation annotation) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(annotation))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].annotationAdded(annotation, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to annotation addition: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(annotation);
			}
		}
		public void annotationRemoved(ImAnnotation annotation) {
			if (inUndoAction || imToolActive || !this.inReactionObjects.add(annotation))
				return;
			try {
				for (int p = 0; p < this.reactionProviders.length; p++)
					this.reactionProviders[p].annotationRemoved(annotation, idmp, allowReactionPrompts());
			}
			catch (Throwable t) {
				System.out.println("Error reacting to annotation removal: " + t.getMessage());
				t.printStackTrace(System.out);
			}
			finally {
				this.inReactionObjects.remove(annotation);
			}
		}
	}
	
	/**
	 * Scroll up (or left) by one page, e.g. in reaction to a press of the
	 * 'Page Up' button.
	 */
	public void scrollUp() {
		if (this.idmp.getSideBySidePages() == 1) {
			JScrollBar vsb = this.idmpBox.getVerticalScrollBar();
			vsb.setValue(Math.max(vsb.getMinimum(), (vsb.getValue() - this.idmpBox.getViewport().getViewRect().height)));
		}
		else {
			JScrollBar hsb = this.idmpBox.getHorizontalScrollBar();
			hsb.setValue(Math.max(hsb.getMinimum(), (hsb.getValue() - this.idmpBox.getViewport().getViewRect().width)));
		}
	}
	
	/**
	 * Scroll down (or right) by one page, e.g. in reaction to a press of the
	 * 'Page Down' button.
	 */
	public void scrollDown() {
		if (this.idmp.getSideBySidePages() == 1) {
			JScrollBar vsb = this.idmpBox.getVerticalScrollBar();
			vsb.setValue(Math.min(vsb.getMaximum(), (vsb.getValue() + this.idmpBox.getViewport().getViewRect().height)));
		}
		else {
			JScrollBar hsb = this.idmpBox.getHorizontalScrollBar();
			hsb.setValue(Math.min(hsb.getMaximum(), (hsb.getValue() + this.idmpBox.getViewport().getViewRect().width)));
		}
	}
	
	/**
	 * Handle a change to the scroll position of this markup panel, e.g. in a
	 * status bar. The argument scroll position label takes the form "Page X
	 * of Y", followed by the page number if the latter is available. This
	 * default implementation does nothing. Sub classes are welcome to overwrite
	 * it as needed.
	 * @param posLabel the scroll position label
	 */
	protected void scrollPositionChanged(String posLabel) {}
	
	/**
	 * Set the rendering DPI. This method also affects the zoom percentage;
	 * namely, this method sets the zoom percentage to <code>renderingDpi
	 * * 100 / 96</code>.
	 * @param renderingDpi the new rendering DPI
	 */
	public void setRenderingDpi(int renderingDpi) {
		int oldRenderingDpi = this.idmp.getRenderingDpi();
		if (renderingDpi == oldRenderingDpi)
			return;
		
		//	we're not visible, just set resolution and we're done
		if (!this.isVisible()) {
			this.idmp.setRenderingDpi(renderingDpi);
			this.validate();
			this.repaint();
			return;
		}
		
		//	get current view center point
		Dimension viewSize = this.idmpBox.getViewport().getExtentSize();
		Point oldViewPos = this.idmpBox.getViewport().getViewPosition();
		Point oldViewCenter = new Point((oldViewPos.x + (viewSize.width / 2)), (oldViewPos.y + (viewSize.height / 2)));
		
		//	find page panel at view center for use as anchor (will be null before we're added to UI)
		Component centerComp = this.idmp.getComponentAt(oldViewCenter);
		if (centerComp == null) {
			this.idmp.setRenderingDpi(renderingDpi);
			this.validate();
			this.repaint();
			return;
		}
		
		//	seek page panel if view center in main document panel proper
		if ((this.idmp.getSideBySidePages() < 1) && (centerComp.getLocation().x < 0)) /* horizontal page arrangement */ {
			Point seekViewCenter = new Point(oldViewCenter.x, oldViewCenter.y);
			while ((centerComp.getLocation().x < 0) && (oldViewPos.x < seekViewCenter.x)) /* this is the markup panel proper in its parent scrolling viewport */ {
				seekViewCenter.x--;
				centerComp = this.idmp.getComponentAt(seekViewCenter);
			}
		}
		else if ((this.idmp.getSideBySidePages() > 0) && (centerComp.getLocation().y < 0)) /* vertical page arrangement */ {
			Point seekViewCenter = new Point(oldViewCenter.x, oldViewCenter.y);
			while ((centerComp.getLocation().y < 0) && (oldViewPos.y < seekViewCenter.y)) /* this is the markup panel proper in its parent scrolling viewport */ {
				seekViewCenter.y--;
				centerComp = this.idmp.getComponentAt(seekViewCenter);
			}
		}
		
		//	compute position relative to anchor component
		Point oldCenterCompPos = centerComp.getLocation();
		Point oldRelViewCenter = new Point((oldViewCenter.x - oldCenterCompPos.x), (oldViewCenter.y - oldCenterCompPos.y));
		
		//	change zoom level
		this.idmp.setRenderingDpi(renderingDpi);
		this.validate();
		this.repaint();
		
		//	compute zoomed view center from anchor component
		Point newCenterCompPos = centerComp.getLocation();
		Point newRelViewCenter = new Point(((oldRelViewCenter.x * renderingDpi) / oldRenderingDpi), ((oldRelViewCenter.y * renderingDpi) / oldRenderingDpi));
		Point newViewCenter = new Point((newCenterCompPos.x + newRelViewCenter.x), (newCenterCompPos.y + newRelViewCenter.y));
		Point newViewPos = new Point(Math.max((newViewCenter.x - (viewSize.width / 2)), 0), Math.max((newViewCenter.y - (viewSize.height / 2)), 0));
		
		//	adjust scroll position
		this.idmpBox.getViewport().setViewPosition(newViewPos);
	}
	
	/**
	 * Set the number of pages displayed side by side before breaking into a
	 * new row. If the argument number is less than 1, all pages are lain out
	 * in one single row left to right.
	 * @param sbsp the number of pages per row
	 */
	public void setSideBySidePages(int sbsp) {
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
	
	/**
	 * Open a dialog offering the user to show or hide individual pages.
	 */
	public void selectVisiblePages() {
		
		//	create selector tiles and compute size
		ImPage[] pages = this.idmp.document.getPages();
		PageSelectorTile[] psts = new PageSelectorTile[pages.length];
		int ptWidth = 0;
		int ptHeight = 0;
		for (int p = 0; p < pages.length; p++) {
			PageThumbnail pt = this.idmp.getPageThumbnail(pages[p].pageId);
			psts[p] = new PageSelectorTile(pt, pages[p].pageId, this.idmp.isPageVisible(pages[p].pageId));
			ptWidth = Math.max(ptWidth, pt.getPreferredSize().width);
			ptHeight = Math.max(ptHeight, pt.getPreferredSize().height);
		}
		
		//	set selector tile size (adding 4 for border width)
		for (int p = 0; p < psts.length; p++)
			psts[p].setPreferredSize(new Dimension(((ptWidth * 2) + 4), ((ptHeight * 2) + 4)));
		
		//	create dialog
		final DialogPanel vps = new DialogPanel("Select Visible Pages", true);
		vps.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
			visiblePageIDs[p] = (psts[p].pageVisible ? psts[p].pageId : -1);
		this.idmp.setVisiblePages(visiblePageIDs);
	}
	
	private static class PageSelectorTile extends JPanel {
		private final PageThumbnail pt;
		int pageId;
		boolean pageVisible;
		PageSelectorTile(PageThumbnail pt, int pageId, boolean pageVisible) {
			super(new BorderLayout(), true);
			this.pt = pt;
			this.pageId = pageId;
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
	
	private StackTraceElement[] lastAtomicActionStarter = null;
	private StackTraceElement[] lastAtomicActionFinisher = null;
	private void printStackTrace(StackTraceElement[] stackTrace) {
		if (stackTrace == null)
			return;
		for (int e = 0; e < stackTrace.length; e++)
			System.err.println("  at " + stackTrace[e].toString());
	}
	
	void addUndoAction(UndoAction ua) {
		if (this.inUndoAction)
			return;
		if (this.multipartUndoAction == null) {
			this.modCount++;
			this.undoActions.addFirst(ua);
			System.err.println("NO GOOD: Got UNDO outside atomic action, last one finished from");
			this.printStackTrace(this.lastAtomicActionFinisher);
			this.updateUndoMenu();
		}
		else this.multipartUndoAction.addUndoAction(ua);
	}
	
	void startMultipartUndoAction(long id, String label) {
		if (this.multipartUndoAction != null) {
			System.err.println("NO GOOD: Started nested atomic action, running one started from");
			this.printStackTrace(this.lastAtomicActionStarter);
			System.err.println("NO GOOD: Started nested atomic action, call coming from");
			this.printStackTrace(Thread.currentThread().getStackTrace());
		}
		this.multipartUndoAction = new MultipartUndoAction(id, label, this);
		this.lastAtomicActionStarter = Thread.currentThread().getStackTrace();
	}
	
	void finishMultipartUndoAction() {
		if ((this.multipartUndoAction == null) && !this.inUndoAction) {
			System.err.println("NO GOOD: Finishing non-existing atomic action, last one finished from");
			this.printStackTrace(this.lastAtomicActionFinisher);
			System.err.println("NO GOOD: Finishing non-existing atomic action, call coming from");
			this.printStackTrace(Thread.currentThread().getStackTrace());
		}
		if ((this.multipartUndoAction != null) && (this.multipartUndoAction.parts.size() != 0)) {
			this.modCount++;
			this.undoActions.addFirst(this.multipartUndoAction);
			this.updateUndoMenu();
		}
		this.multipartUndoAction = null;
		this.lastAtomicActionFinisher = Thread.currentThread().getStackTrace();
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
	 * Update the 'Undo' menu of the surrounding UI, e.g. when a markup panel
	 * is newly opened, or when it is selected in a multi-document UI.
	 */
	public void updateUndoMenu() {
		JMenu undoMenu = this.getUndoMenu();
		if (undoMenu == null)
			return;
		undoMenu.removeAll();
		for (Iterator uait = this.undoActions.iterator(); uait.hasNext();) {
			final UndoAction ua = ((UndoAction) uait.next());
			JMenuItem mi = new JMenuItem(ua.label);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						ua.target.inUndoAction = true;
						long us = System.currentTimeMillis();
						while (undoActions.size() != 0) {
							UndoAction eua = ((UndoAction) undoActions.removeFirst());
							try {
								if (ua instanceof MultipartUndoAction)
									ua.target.idmp.startAtomicAction(((MultipartUndoAction) ua).actionId, "UNDO", null, null, null);
								else ua.target.idmp.startAtomicAction(-1, "UNDO", null, null, null);
								eua.execute();
							}
							finally {
								ua.target.idmp.endAtomicAction();
							}
							if (eua == ua)
								break;
						}
						
						updateUndoMenu();
						System.out.println("Executed undo actions in " + (System.currentTimeMillis() - us) + "ms");
					}
					finally {
						ua.target.inUndoAction = false;
						
						/* we are on the EDT, so we can repaint right here
						 * without any risk of incurring a deadlock between
						 * on synchronized parts of UI or data structures */
						ua.target.idmp.validate();
						ua.target.idmp.repaint();
						ua.target.idmp.validateControlPanel();
					}
				}
			});
			undoMenu.add(mi);
			if (undoMenu.getMenuComponentCount() >= 10)
				break;
		}
		undoMenu.setEnabled(this.undoActions.size() != 0);
	}
	
	/**
	 * Provide the 'Undo' menu integrated in a UI for the markup panel to show
	 * its 'Undo' options in. If this method returns null, UI based 'Undo' will
	 * not be accessible.
	 * @return the 'Undo' menu of the surrounding UI
	 */
	protected abstract JMenu getUndoMenu();
	
	/**
	 * Check whether or not the Image Markup document displayed in this panel
	 * has been modified since the last call to <code>markClean()</code>. This
	 * is mostly to track whether or not the document needs saving.
	 * @return true if the document has been modified
	 */
	public boolean isDirty() {
		return (this.modCount != this.savedModCount);
	}
	
	/**
	 * Mark the Image Markup document displayed in this panel as clean. Client
	 * code will mostly call this method after saving a document to persistent
	 * storage.
	 */
	public void markClean() {
		this.savedModCount = this.modCount;
	}
	
	/**
	 * Retrieve the wrapped markup panel.
	 * @return the wrapped markup panel
	 */
	public ImDocumentMarkupPanel getMarkupPanel() {
		return this.idmp;
	}
	
	private static class IdmpViewport extends JViewport implements TwoClickActionMessenger {
		private static Color halfTransparentRed = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
		private ImDocumentMarkupPanel idmp;
		private String tcaMessage = null;
		IdmpViewport(ImDocumentMarkupPanel idmp) {
			this.idmp = idmp;
			this.idmp.setTwoClickActionMessenger(this);
			this.setView(this.idmp);
			this.setOpaque(true); // we need some explicit background because in some look&feels window background is white
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
			Font f = new Font("SansSerif", Font.PLAIN, 20);
			g.setFont(f);
			TextLayout wtl = new TextLayout(this.tcaMessage, f, ((Graphics2D) g).getFontRenderContext());
			g.setColor(halfTransparentRed);
			g.fillRect(0, 0, this.getViewRect().width, ((int) Math.ceil(wtl.getBounds().getHeight() + (wtl.getDescent() * 3))));
			g.setColor(Color.white);
			((Graphics2D) g).drawString(this.tcaMessage, ((this.getViewRect().width - wtl.getAdvance()) / 2), ((int) Math.ceil(wtl.getBounds().getHeight() + wtl.getDescent())));
		}
	}
	
	/**
	 * Handle a drop on the markup panel that did not go to any of the present
	 * drop handlers. This default implementation does nothing. Sub classes are
	 * welcome to overwrite it as needed.
	 * @param dropped the dropped data
	 */
	protected void handleDrop(Transferable dropped) {}
	
	/**
	 * Indicate whether or no reaction providers are allowed to prompt the user
	 * for input.
	 * @return true to allow reactions, false to disallow them
	 */
	protected abstract boolean allowReactionPrompts();
	
	/**
	 * Dispose of the markup panel, cleaning up inner data structures,
	 * unregistering listeners, etc.
	 * @param storeSettings store annotation and region color settings?
	 */
	public void dispose(boolean storeSettings) {
		this.ggImagine.removeDisplayExtensionListener(this);
		this.idmp.document.removeDocumentListener(this.undoRecorder);
		if (this.reactionTrigger != null)
			this.idmp.document.removeDocumentListener(this.reactionTrigger);
		this.ggImagine.notifyDocumentClosed(this.idmp.document.docId);
		
		if (storeSettings) {
			Settings annotationColors = this.ggiConfig.getSubset("annotation.color");
			String[] annotationTypes = this.idmp.getAnnotationTypes();
			for (int t = 0; t < annotationTypes.length; t++) {
				Color ac = this.idmp.getAnnotationColor(annotationTypes[t]);
				if (ac != null)
					annotationColors.setSetting(annotationTypes[t], GoldenGateImagine.getHex(ac));
			}
			Settings layoutObjectColors = this.ggiConfig.getSubset("layoutObject.color");
			String[] layoutObjectTypes = this.idmp.getLayoutObjectTypes();
			for (int t = 0; t < layoutObjectTypes.length; t++) {
				Color loc = this.idmp.getLayoutObjectColor(layoutObjectTypes[t]);
				if (loc != null)
					layoutObjectColors.setSetting(layoutObjectTypes[t], GoldenGateImagine.getHex(loc));
			}
			Settings textStreamColors = this.ggiConfig.getSubset("textStream.color");
			String[] textStreamTypes = this.idmp.getTextStreamTypes();
			for (int t = 0; t < textStreamTypes.length; t++) {
				Color tsc = this.idmp.getTextStreamTypeColor(textStreamTypes[t]);
				if (tsc != null)
					textStreamColors.setSetting(textStreamTypes[t], GoldenGateImagine.getHex(tsc));
			}
			this.saUsageStats.storeTo(this.ggiConfig.getSubset("selectionAction"));
		}
	}
	
	private static abstract class UndoAction {
		final String label;
		final ImageDocumentMarkupPanel target;
		final int modCount;
		UndoAction(String label, ImageDocumentMarkupPanel target) {
			this.label = label;
			this.target = target;
			this.modCount = this.target.modCount;
		}
		final void execute() {
			this.doExecute();
			this.target.modCount = this.modCount;
		}
		abstract void doExecute();
	}
	
	private static class MultipartUndoAction extends UndoAction {
		final LinkedList parts = new LinkedList();
		final long actionId;
		MultipartUndoAction(long id, String label, ImageDocumentMarkupPanel target) {
			super(label, target);
			this.actionId = -id;
		}
		synchronized void addUndoAction(UndoAction ua) {
			this.parts.addFirst(ua);
		}
		void doExecute() {
			while (this.parts.size() != 0)
				((UndoAction) this.parts.removeFirst()).doExecute();
		}
	}
	
	private static SelectionActionUsageStats selectionActionUsageStats = null;
	private static SelectionActionUsageStats getSelectionActionUsageStats(Settings ggiConfig) {
		if (selectionActionUsageStats == null) {
			selectionActionUsageStats = new SelectionActionUsageStats();
			selectionActionUsageStats.fillFrom(ggiConfig.getSubset("selectionAction"));
		}
		return selectionActionUsageStats;
	}
	private static class SelectionActionUsageStats extends TreeMap {
		private static class SelectionActionUsage {
			int shown = 0;
			int used = 0;
			int usedLast = 0;
			SelectionActionUsage() {}
		}
		
		private int isSaAdvancedPivotIndex = 10;
		private int saUseCounter = 1;
		
		private SelectionActionUsage getSelectionActionUsage(String saName) {
			SelectionActionUsage saUsage = ((SelectionActionUsage) this.get(saName));
			if (saUsage == null) {
				saUsage = new SelectionActionUsage();
				this.put(saName, saUsage);
			}
			return saUsage;
		}
		
		boolean[] markAdvancedSelectionActions(SelectionAction[] sas) {
			float[] isSaAdvancedScoresBySa = new float[sas.length];
			float[] isSaAdvancedScoresByVal = new float[sas.length];
			for (int a = 0; a < sas.length; a++) {
				float isSaAdvancedScore = 0;
				if (sas[a] != SelectionAction.SEPARATOR) {
					SelectionActionUsage saUsage = this.getSelectionActionUsage(sas[a].name);
					saUsage.shown++;
					isSaAdvancedScore += (((float) saUsage.used) / saUsage.shown); // MFU part
					isSaAdvancedScore += (((float) saUsage.usedLast) / this.saUseCounter); // MRU part
				}
				isSaAdvancedScoresBySa[a] = isSaAdvancedScore;
				isSaAdvancedScoresByVal[a] = isSaAdvancedScore;
			}
			
			Arrays.sort(isSaAdvancedScoresByVal);
			float isSaAdvancedThreshold = ((sas.length < this.isSaAdvancedPivotIndex) ? 0 : isSaAdvancedScoresByVal[sas.length - this.isSaAdvancedPivotIndex]);
			
			boolean[] isSaAdvanced = new boolean[sas.length];
			for (int a = 0; a < sas.length; a++)
				isSaAdvanced[a] = (isSaAdvancedScoresBySa[a] < isSaAdvancedThreshold);
			
			return isSaAdvanced;
		}
		
		void selectionActionUsed(SelectionAction sa) {
			SelectionActionUsage saUsage = this.getSelectionActionUsage(sa.name);
			saUsage.used++;
			saUsage.usedLast = this.saUseCounter++;
		}
		
		void fillFrom(Settings set) {
			this.isSaAdvancedPivotIndex = Math.max(1, Integer.parseInt(set.getSetting("isAdvancedPivotIndex", ("" + this.isSaAdvancedPivotIndex))));
			this.saUseCounter = Math.max(1, Integer.parseInt(set.getSetting("useCounter", "1")));
			
			String[] saNames = set.getSubsetPrefixes();
			for (int n = 0; n < saNames.length; n++) {
				Settings saUsageSet = set.getSubset(saNames[n]);
				SelectionActionUsage saUsage = this.getSelectionActionUsage(saNames[n]);
				saUsage.shown = Integer.parseInt(saUsageSet.getSetting("shown", "0"));
				saUsage.used = Integer.parseInt(saUsageSet.getSetting("used", "0"));
				saUsage.usedLast = Integer.parseInt(saUsageSet.getSetting("usedLast", "0"));
			}
		}
		void storeTo(Settings set) {
			set.setSetting("isAdvancedPivotIndex", ("" + this.isSaAdvancedPivotIndex));
			set.setSetting("useCounter", ("" + this.saUseCounter));
			
			for (Iterator sanit = this.keySet().iterator(); sanit.hasNext();) {
				String saName = ((String) sanit.next());
				SelectionActionUsage saUsage = this.getSelectionActionUsage(saName);
				Settings saUsageSet = set.getSubset(saName);
				saUsageSet.setSetting("shown", ("" + saUsage.shown));
				saUsageSet.setSetting("used", ("" + saUsage.used));
				saUsageSet.setSetting("usedLast", ("" + saUsage.usedLast));
			}
		}
	}
}