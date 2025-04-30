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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.WindowConstants;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.EditableAnnotation;
import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.gamta.util.imaging.ImagingConstants;
import de.uka.ipd.idaho.gamta.util.swing.DialogFactory;
import de.uka.ipd.idaho.gamta.util.swing.ProgressMonitorWindow;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.plugins.AnnotationSource;
import de.uka.ipd.idaho.goldenGate.plugins.AnnotationSourceManager;
import de.uka.ipd.idaho.goldenGate.plugins.AnnotationSourceManager.AnnotationSourceResult;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessor;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessorManager;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceSplashScreen;
import de.uka.ipd.idaho.goldenGate.ui.DynamicWindowMenu;
import de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI;
import de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay;
import de.uka.ipd.idaho.goldenGate.ui.NamedElementUsageStatistics;
import de.uka.ipd.idaho.goldenGate.ui.UserInterfaceUtils;
import de.uka.ipd.idaho.goldenGate.ui.WindowMenuOwner;
import de.uka.ipd.idaho.goldenGate.util.AnnotationSourceResultDialog;
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
import de.uka.ipd.idaho.im.gamta.ImDocumentRoot;
import de.uka.ipd.idaho.im.gamta.LazyAnnotation;
import de.uka.ipd.idaho.im.gamta.LazyMutableAnnotation;
import de.uka.ipd.idaho.im.gamta.LazyQueriableAnnotation;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.ClickActionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionListener;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageEditToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ReactionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay;
import de.uka.ipd.idaho.im.imagine.ui.ImageUserInterfaceUtils;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.AtomicActionListener;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
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
public abstract class ImageDocumentMarkupPanel extends JPanel implements ImagingConstants, DisplayExtensionListener, ImageDocumentDisplay {
	final GoldenGATE goldenGate;
	final GoldenGateImagine ggImagine;
	final Map createdColors = Collections.synchronizedMap(new HashMap());
	ImageDocumentMarkupUI parent;
	
//	final SelectionActionUsageStats saUsageStats;
	
	final ImDocumentMarkupPanel idmp;
	final JScrollPane idmpBox;
	Rectangle idmpViewSize;
	
	static final int fastScrollEnterRatioDenom = 20;
	static final int fastScrollMaintainRatioDenom = 20;
	boolean idmpBoxInFastScroll = false;
	
	private ImDocumentListener undoRecorder;
//	final LinkedList undoActions = new LinkedList();
	final ArrayList undoActions = new ArrayList();
	private MultipartUndoAction multipartUndoAction = null;
	boolean inUndoAction = false;
	private int undoMenuMaxSize = 10;
	
	private ImDocumentListener reactionTrigger = null;
	boolean imToolActive = false;
	
	int modCount = 0;
	private int savedModCount = 0;
	
	private static class XmlWrapperCache {
		private HashMap wrappers = new HashMap();
		LazyMutableAnnotation getXmlWrapper(int flags) {
			Integer flagObj = Integer.valueOf(flags);
			WeakReference wrapperRef = ((WeakReference) this.wrappers.get(flagObj));
			if (wrapperRef == null)
				return null;
			LazyMutableAnnotation wrapper = ((LazyMutableAnnotation) wrapperRef.get());
			if (wrapper == null) // reclaimed by GC
				this.wrappers.remove(flagObj);
			return wrapper;
		}
		void addXmlWrapper(LazyMutableAnnotation wrapper) {
			this.wrappers.put(Integer.valueOf(wrapper.getFlags()), new WeakReference(wrapper));
		}
		void invalidateWrappers() {
			ArrayList flagObjs = new ArrayList(this.wrappers.keySet());
			for (int f = 0; f < flagObjs.size(); f++) {
				Integer flagObj = ((Integer) flagObjs.get(f));
				WeakReference wrapperRef = ((WeakReference) this.wrappers.get(flagObj));
				if (wrapperRef == null) {
					this.wrappers.remove(flagObj);
					continue;
				}
				LazyMutableAnnotation wrapper = ((LazyMutableAnnotation) wrapperRef.get());
				if (wrapper == null) // reclaimed by GC
					this.wrappers.remove(flagObj);
				else wrapper.invalidateData();
			}
		}
	}
	
	private int xmlWrapperFlags = (ImDocumentRoot.NORMALIZE_CHARACTERS | ImDocumentRoot.NORMALIZATION_LEVEL_PARAGRAPHS); // TODO load that from display property ... at some point
	private XmlWrapperCache docCache = new XmlWrapperCache();
	private LazyQueriableAnnotation docReadOnly;
	private LazyMutableAnnotation docMutable;
	
	/**
	 * Constructor
	 * @param doc the document to display
	 * @param ggImagine the GoldenGATE Imagine core providing editing functionality
	 * @param ggiConfig the GoldenGATE Imagine configuration
	 */
	protected ImageDocumentMarkupPanel(ImDocument doc, GoldenGateImagine ggImagine) {
		super(new BorderLayout(), true);
		this.goldenGate = ggImagine.getGoldenGATE();
		this.ggImagine = ggImagine;
		
		this.idmp = new ImageDocumentEditorPanel(doc);
		
		//	configure document display panel from central defaults (if any)
		for (int p = 0; p < ImDocumentMarkupPanel.displayPropertyNames.length; p++) {
			Object value = UserInterfaceUtils.getDisplayProperty(ImDocumentMarkupPanel.displayPropertyNames[p]);
			if (value == null)
				continue;
			Object defValue = ImDocumentMarkupPanel.getDisplayPropertyDefault(ImDocumentMarkupPanel.displayPropertyNames[p]);
			if (UserInterfaceUtils.equals(value, defValue))
				continue; // no use setting value to default
			try {
				this.idmp.setDisplayProperty(ImDocumentMarkupPanel.displayPropertyNames[p], value);
			}
			catch (RuntimeException re) {
				System.out.println("Failed to initialize property '" + ImDocumentMarkupPanel.displayPropertyNames[p] + "' from central configuration: " + re.getMessage());
			}
		}
		
		//	make sure to push changes to colors we created to central settings (random color might well be off, or to close to something else)
		this.idmp.addDisplayPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent pce) {
				String propName = pce.getPropertyName();
				Object newValue = pce.getNewValue();
				if (createdColors.containsKey(propName) && (newValue instanceof Color)) {
					Color newColor = ((Color) newValue);
					createdColors.put(propName, newColor);
					String objectType = propName;
					if (objectType.startsWith("annot."))
						objectType = objectType.substring("annot.".length());
					else if (objectType.startsWith("region."))
						objectType = objectType.substring("region.".length());
					else if (objectType.startsWith("textStream."))
						objectType = objectType.substring("textStream.".length());
					else return;
					if (objectType.endsWith(".color"))
						objectType = objectType.substring(0, (objectType.length() - ".color".length()));
					else return;
					if (propName.startsWith("annot."))
						UserInterfaceUtils.setAnnotationColor(objectType, newColor);
					else UserInterfaceUtils.setDisplayProperty(propName, newColor);
				}
				else {
					//	TODO_not store font and background settings automatically ???
					//	==> no, got dedicated menu item for that purpose
				}
			}
		});
//		
//		//	get singleton selection action usage stats
//		this.saUsageStats = getSelectionActionUsageStats(this.ggiConfig);
		
		//	prepare recording UNDO actions
		this.undoRecorder = new UndoRecorder();
		this.idmp.document.addDocumentListener(this.undoRecorder);
		Object undoMenuMaxSizeObj = UserInterfaceUtils.getDisplayProperty("ggImagine.undoMenuMaxSize");
		if (undoMenuMaxSizeObj instanceof Number)
			this.undoMenuMaxSize = ((Number) undoMenuMaxSizeObj).intValue();
		else UserInterfaceUtils.setDisplayProperty("ggImagine.undoMenuMaxSize", new Integer(this.undoMenuMaxSize));
		
		//	get reaction providers
		ReactionProvider[] reactionProviders = this.ggImagine.getReactionProviders();
		System.out.println("Got " + reactionProviders.length + " reaction providers");
		if (reactionProviders.length != 0) {
			this.reactionTrigger = new ReactionTrigger(reactionProviders);
			this.idmp.document.addDocumentListener(this.reactionTrigger);
		}
		
		//	distribute display extension changes to individual editor tabs
		this.ggImagine.addDisplayExtensionListener(this);
		
		//	inform any listeners about atomic actions
		this.idmp.addAtomicActionListener(new AtomicActionNotifier(this.ggImagine, this.idmp));
		
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
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getUserInterface()
	 */
	public GoldenGateUI getUserInterface() {
		return this.parent;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#isRootDisplay()
	 */
	public boolean isRootDisplay() {
		return true; // TODO any cases this might not hold true ???
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getParentDisplay()
	 */
	public DocumentDisplay getParentDisplay() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay#getXmlWrapperFlags()
	 */
	public int getXmlWrapperFlags() {
		return this.xmlWrapperFlags;
	}
	void setXmlWrapperFlags(int flags) {
		this.xmlWrapperFlags = flags;
	}
	
	private boolean ensureXmlWrappers() /* true indicates wrappers already existed */ {
		if (this.docReadOnly != null)
			return true;
		this.docReadOnly = new LazyQueriableAnnotation(this.idmp.document, this.xmlWrapperFlags);
		this.docMutable = new LazyMutableAnnotation(this.docReadOnly);
		return false;
	}
	void invalidateXmlWrappers() {
		if (this.docReadOnly != null)
			this.docReadOnly.invalidateData();
		else if (this.docMutable != null)
			this.docMutable.invalidateData();
		this.docCache.invalidateWrappers();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay#getXmlWrapper(int)
	 */
	public LazyMutableAnnotation getXmlWrapper(int flags) {
//		if (this.ensureXmlWrappers())
//			this.docMutable.setFlags(this.xmlWrapperFlags);
//		if (flags != -1)
//			this.docMutable.setFlags(flags);
//		return this.docMutable;
		if ((flags == -1) || (flags == this.xmlWrapperFlags)) {
			if (this.ensureXmlWrappers())
				this.docMutable.setFlags(this.xmlWrapperFlags);
			return this.docMutable;
		}
		else {
//			return new LazyMutableAnnotation(this.xdmp.document, flags); // TODOne use weak cache for this
			LazyMutableAnnotation wrapper = this.docCache.getXmlWrapper(flags);
			if (wrapper == null) {
				wrapper = new LazyMutableAnnotation(this.idmp.document, flags);
				this.docCache.addXmlWrapper(wrapper);
			}
			else wrapper.setFlags(flags);
			return wrapper;
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#applyAnnotationSource(de.uka.ipd.idaho.goldenGate.plugins.AnnotationSourceManager, de.uka.ipd.idaho.goldenGate.plugins.AnnotationSource, de.uka.ipd.idaho.gamta.Annotation)
	 */
	public void applyAnnotationSource(final AnnotationSourceManager sourceManager, final AnnotationSource annotationSource, final Annotation data) {
		
		//	check if we got anything to work with
		if ((sourceManager == null) && (annotationSource == null))
			return;
		
		//	prepare applying annotation source
//		final QueriableAnnotation qData = ((data == null) ? this.xdmp.document : this.xdmp.document.getAnnotation(data.getAnnotationID()));
//		if (qData == null)
//			return;
		ImObject imObj = ((data == null) ? null : this.idmp.document.getObjectByUUID(data.getAnnotationID()));
		final ImAnnotation imData = ((imObj instanceof ImAnnotation) ? ((ImAnnotation) imObj) : null);
		String pmTitle;
		String pmText;
		if (sourceManager == null) {
			pmTitle = ("Apply " + annotationSource.getTypeLabel() + " '" + annotationSource.getName() + "'");
			pmText = ("Please wait while applying " + annotationSource.getTypeLabel() + " '" + annotationSource.getName() + "' ...");
		}
		else {
			pmTitle = ("Apply " + sourceManager.getResourceTypeLabel() + " ...");
			pmText = ("Please select the " + sourceManager.getResourceTypeLabel() + " to apply");
		}
		final ProgressMonitor pm = this.idmp.getProgressMonitor(pmTitle, pmText, false, false);
		final ProgressMonitorWindow pmw = ((pm instanceof ProgressMonitorWindow) ? ((ProgressMonitorWindow) pm) : null);
		
		//	apply annotation source in dedicated thread
		Thread asThread = new Thread("AnnotationSourceApplicator") {
			public void run() {
				try {
					
					//	wait for splash screen progress monitor to come up (we must not reach the dispose() line before the splash screen even comes up)
					while ((pmw != null) && !pmw.getWindow().isVisible()) try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {}
					
					//	prepare document
					EditableAnnotation eData = ((imData == null) ? ImageDocumentMarkupPanel.this.getDocumentMutable() : new ImDocumentRoot(imData, ImageDocumentMarkupPanel.this.getXmlWrapperFlags()));
					AnnotationSourceResult asr;
					
					//	mark as interactive
					Properties params = new Properties();
					params.setProperty(AnnotationSource.INTERACTIVE_PARAMETER, AnnotationSource.INTERACTIVE_PARAMETER);
					
					//	no source manager given, apply annotation source directly
					if (sourceManager == null) {
//						Annotation[] annots = annotationSource.createAnnotations(qData, params, pm);
						Annotation[] annots = annotationSource.createAnnotations(eData, params, pm);
						if (annots == null)
							return;
						AnnotationSourceResultDialog asrd = new AnnotationSourceResultDialog(("Result of " + annotationSource.getTypeLabel() + " '" + annotationSource.getName() + "'"), annots, null, idmp.document.getAnnotationTypes(), null, null);
						annots = asrd.getSelectedAnnotations();
						if (annots == null)
							return;
						String annotType = asrd.getSelectedAnnotationType();
						if (annotType == null)
							return;
						for (int a = 0; a < annots.length; a++)
							annots[a].changeTypeTo(annotType);
						asr = new AnnotationSourceResult(annots, annotationSource);
					}
					
					//	have manager apply annotation source
					else asr = sourceManager.applyAnnotationSource(((annotationSource == null) ? null : annotationSource.getName()), params, eData, ImageDocumentMarkupPanel.this, pm);
					
					//	anything to work with?
					if (asr == null)
						return;
					
					//	add annotations under atomic action
					idmp.startAtomicAction(("Apply " + asr.annotationSource.getTypeLabel() + " '" + asr.annotationSource.getName() + "'"), null, imData, pm);
					HashSet annotTypes = new HashSet();
					for (int a = 0; a < asr.annotations.length; a++) {
						Annotation annot = eData.addAnnotation(asr.annotations[a].getStartIndex(), asr.annotations[a].getEndIndex(), asr.annotations[a].getType());
						if (annot == null)
							continue;
						annot.copyAttributes(asr.annotations[a]);
						if (annotTypes.add(annot.getType()))
							idmp.setAnnotationsPainted(annot.getType(), true);
					}
					idmp.finishAtomicAction(pm);
				}
				finally {
					if (pmw != null)
						pmw.close();
				}
			}
		};
		asThread.start();
		
		//	open splash screen progress monitor (this waits)
		if (pmw != null)
			pmw.popUp(true);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#applyDocumentProcessor(de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessorManager, de.uka.ipd.idaho.goldenGate.plugins.DocumentProcessor, de.uka.ipd.idaho.gamta.Annotation)
	 */
	public void applyDocumentProcessor(DocumentProcessorManager processorManager, DocumentProcessor documentProcessor, Annotation data) {
		if ((processorManager == null) && (documentProcessor == null))
			return;
		ImObject imObj = ((data == null) ? null : this.idmp.document.getObjectByUUID(data.getAnnotationID()));
		ImAnnotation imAnnot = ((imObj instanceof ImAnnotation) ? ((ImAnnotation) imObj) : null);
		this.applyGenericXmlMarkupTool(new DocumentProcessorMarkupTool(processorManager, documentProcessor), imAnnot);
	}
	private class DocumentProcessorMarkupTool implements ImageMarkupTool {
		private DocumentProcessorManager processorManager;
		private DocumentProcessor documentProcessor;
		private String label;
		DocumentProcessorMarkupTool(DocumentProcessorManager processorManager, DocumentProcessor documentProcessor) {
			this.processorManager = processorManager;
			this.documentProcessor = documentProcessor;
			if (this.documentProcessor == null)
				this.label = (this.processorManager.getResourceTypeLabel() + " ...");
			else this.label = (this.documentProcessor.getTypeLabel() + " '" + this.documentProcessor.getName() + "'");
		}
		public String getLabel() {
			return this.label;
		}
		public String getTooltip() {
			return null; // this is an ad-hoc wrapper, no tooltip needed
		}
		public String getHelpText() {
			return null; // this is an ad-hoc wrapper, no help needed
		}
		public void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			
			//	get target annotation
//			MutableAnnotation data = ((annot == null) ? doc : doc.getMutableAnnotation(annot.getAnnotationID()));
//			if (data == null)
//				return;
//			XmDocumentRoot data = ((annot == null) ? new XmDocumentRoot(xdmp.document, XmlDocumentEditorTab.this.getXmlWrapperFlags()) : new XmDocumentRoot(annot, XmlDocumentEditorTab.this.getXmlWrapperFlags()));
			MutableAnnotation data = ((annot == null) ? ImageDocumentMarkupPanel.this.getDocumentMutable() : new ImDocumentRoot(annot, ImageDocumentMarkupPanel.this.getXmlWrapperFlags()));
			
			//	apply document processor, directly or in manager
			Properties params = new Properties();
			params.setProperty(AnnotationSource.INTERACTIVE_PARAMETER, AnnotationSource.INTERACTIVE_PARAMETER);
			if (this.processorManager == null)
				this.documentProcessor.process(data, params, pm);
			else this.processorManager.applyDocumentProcessor(this.documentProcessor, params, data, ImageDocumentMarkupPanel.this, pm);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay#applyGenericXmlMarkupTool(de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool, de.uka.ipd.idaho.im.ImAnnotation)
	 */
	public void applyGenericXmlMarkupTool(ImageMarkupTool imt, ImAnnotation annot) {
		((ImageDocumentEditorPanel) this.idmp).applyMarkupTool(imt, annot, false);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getDocumentReadOnly()
	 */
	public QueriableAnnotation getDocumentReadOnly() {
		if (this.ensureXmlWrappers())
			this.docReadOnly.setFlags(this.xmlWrapperFlags);
		return this.docReadOnly;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getDocumentMutable()
	 */
	public MutableAnnotation getDocumentMutable() {
		if (this.ensureXmlWrappers())
			this.docMutable.setFlags(this.xmlWrapperFlags);
		return this.docMutable;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getDocumentId()
	 */
	public String getDocumentId() {
		return this.idmp.document.docId;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getSourceDocumentClass()
	 */
	public Class getSourceDocumentClass() {
		return ImDocument.class;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getSourceDocument()
	 */
	public Attributed getSourceDocument() {
		return this.idmp.document;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#areAnnotationsEditable()
	 */
	public boolean areAnnotationsEditable() {
		return true; // annotations are editable in IMF
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#areTokensEditable()
	 */
	public boolean areTokensEditable() {
		return false; // tokens are _not_ editable in via generic XML wrapper in IMF
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#areAnnotationsVisible(java.lang.String)
	 */
	public boolean areAnnotationsVisible(String type) {
		return this.idmp.areAnnotationsPainted(type);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#setAnnotationsVisible(java.lang.String, boolean)
	 */
	public void setAnnotationsVisible(String type, boolean visible) {
		if (this.idmp.document.getAnnotationCount(type) == 0)
			this.idmp.setRegionsPainted(type, visible);
		else this.idmp.setAnnotationsPainted(type, visible);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getAnnotationColor(java.lang.String)
	 */
	public Color getAnnotationColor(String type) {
		return this.idmp.getAnnotationColor(type);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#setAnnotationColor(java.lang.String, java.awt.Color)
	 */
	public void setAnnotationColor(String type, Color color) {
		this.idmp.setAnnotationColor(type, color);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#createAnnotationColor(java.lang.String)
	 */
	protected Color createAnnotationColor(String type) {
		Color annotColor = UserInterfaceUtils.getAnnotationColor(type);
		if (annotColor == null) {
			annotColor = UserInterfaceUtils.createAnnotationColor(type);
			this.createdColors.put(("annot." + type + ".color"), annotColor);
		}
		return annotColor;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#createLayoutObjectColor(java.lang.String)
	 */
	protected Color createLayoutObjectColor(String type) {
		String colorPropName = ("region." + type + ".color");
		Object layoutObjectColorObj = UserInterfaceUtils.getDisplayProperty(colorPropName);
		Color layoutObjectColor = ((layoutObjectColorObj instanceof Color) ? ((Color) layoutObjectColorObj) : null);
		if (layoutObjectColor == null) {
			layoutObjectColor = UserInterfaceUtils.createColor();
			this.createdColors.put(colorPropName, layoutObjectColor);
			UserInterfaceUtils.setDisplayProperty(colorPropName, layoutObjectColor);
		}
		return layoutObjectColor;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel#createTextStreamTypeColor(java.lang.String)
	 */
	protected Color createTextStreamTypeColor(String type) {
		String colorPropName = ("textStream." + type + ".color");
		Object textStreamTypeColorObj = UserInterfaceUtils.getDisplayProperty(colorPropName);
		Color textStreamTypeColor = ((textStreamTypeColorObj instanceof Color) ? ((Color) textStreamTypeColorObj) : null);
		if (textStreamTypeColor == null) {
			textStreamTypeColor = UserInterfaceUtils.createColor();
			this.createdColors.put(colorPropName, textStreamTypeColor);
			UserInterfaceUtils.setDisplayProperty(colorPropName, textStreamTypeColor);
		}
		return textStreamTypeColor;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getDisplayProperty(java.lang.String)
	 */
	public Object getDisplayProperty(String name) {
		/* TODO add support for:
		 * - word selection color and opacity
		 * - box selection color and thickness
		 * - ALSO add display config dialog in IM UI utils
		 */
		return null;//this.idmp.getDisplayProperty(name);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#setDisplayProperty(java.lang.String, java.lang.Object)
	 */
	public void setDisplayProperty(String name, Object value) {
		/* TODO add support for:
		 * - word selection color and opacity
		 * - box selection color and thickness
		 * - ALSO add display config dialog in IM UI utils
		 */
//		this.idmp.setDisplayProperty(name, value);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getAnnotationTypes()
	 */
	public String[] getAnnotationTypes() {
		return this.idmp.document.getAnnotationTypes();
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#highlightAnnotation(de.uka.ipd.idaho.gamta.Annotation)
	 */
	public void highlightAnnotation(Annotation annotation) {
		ImObject imObj = this.idmp.document.getObjectByUUID(annotation.getAnnotationID());
		if (imObj instanceof ImWord)
			this.idmp.setWordSelection((ImWord) imObj);
		else if (imObj instanceof ImAnnotation) {
			this.idmp.setAnnotationsPainted(((ImAnnotation) imObj).getType(), true);
			this.idmp.setWordSelection(((ImAnnotation) imObj).getFirstWord(), ((ImAnnotation) imObj).getLastWord());
		}
		else if (imObj instanceof ImRegion) {
			this.idmp.setRegionsPainted(((ImRegion) imObj).getType(), true);
			this.idmp.setBoxSelection(((ImRegion) imObj).pageId, ((ImRegion) imObj).bounds);
		}
		else if (MutableAnnotation.PARAGRAPH_TYPE.equals(annotation.getType())) /* need to catch paragraphs separately, as those are emulated by GAMTA wrapper */ {
			Object fwObj = annotation.getAttribute(ImAnnotation.FIRST_WORD_ATTRIBUTE);
			Object lwObj = annotation.getAttribute(ImAnnotation.LAST_WORD_ATTRIBUTE);
			if ((fwObj instanceof ImWord) && (lwObj instanceof ImWord)) {
				ImWord firstWord = ((ImWord) fwObj);
				ImWord lastWord = ((ImWord) lwObj);
				if (firstWord.getTextStreamId().equals(lastWord.getTextStreamId()))
					this.idmp.setWordSelection(firstWord, lastWord);
				else this.idmp.setWordSelection(firstWord);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay#getProgressMonitor(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public ProgressMonitor getProgressMonitor(String title, String text, boolean supportPauseResume, boolean supportAbort) {
		return new ResourceSplashScreen(getMainWindow(), title, text, supportPauseResume, supportAbort);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay#getImDocument()
	 */
	public ImDocument getImDocument() {
		return this.idmp.document;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.ui.ImageDocumentDisplay#getImDocumentPanel()
	 */
	public ImDocumentMarkupPanel getImDocumentPanel() {
		return this.idmp;
	}
	
	private class ImageDocumentEditorPanel extends ImDocumentMarkupPanel implements AtomicActionListener {
		ImageDocumentEditorPanel(ImDocument document) {
			super(document);
			this.addAtomicActionListener(this);
		}
		protected Color createAnnotationColor(String type) {
			return ImageDocumentMarkupPanel.this.createAnnotationColor(type);
		}
		protected Color createLayoutObjectColor(String type) {
			return ImageDocumentMarkupPanel.this.createLayoutObjectColor(type);
		}
		protected Color createTextStreamTypeColor(String type) {
			return ImageDocumentMarkupPanel.this.createTextStreamTypeColor(type);
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
			ArrayList actions = new ArrayList(Arrays.asList(super.getActions(start, end)));
			SelectionActionProvider[] saps = ImageDocumentMarkupPanel.this.ggImagine.getSelectionActionProviders();
			for (int p = 0; p < saps.length; p++) try {
				SelectionAction[] sas = saps[p].getActions(start, end, this, ImageDocumentMarkupPanel.this);
				if ((sas != null) && (sas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(sas));
				}
			}
			catch (Exception e) {
				System.out.println("Error getting actions for word selection: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			ImAnnotation spanningAnnot = null; // TODO find single visible spanning annotation
			if (spanningAnnot != null)
				this.addAdvancedAction(actions, spanningAnnot);
			return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
		}
		protected ClickSelectionAction[] getClickActions(ImWord word, int clickCount) {
			ArrayList actions = new ArrayList(Arrays.asList(super.getClickActions(word, clickCount)));
			ClickActionProvider[] caps = ImageDocumentMarkupPanel.this.ggImagine.getClickActionProviders();
			for (int p = 0; p < caps.length; p++) try {
				ClickSelectionAction[] csas = caps[p].getActions(word, clickCount, this, ImageDocumentMarkupPanel.this);
				if ((csas != null) && (csas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(csas));
				}
			}
			catch (Exception e) {
				System.out.println("Error getting actions for word click: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			return ((ClickSelectionAction[]) actions.toArray(new ClickSelectionAction[actions.size()]));
		}
		protected SelectionAction[] getActions(ImPage page, Point start, Point end) {
			ArrayList actions = new ArrayList(Arrays.asList(super.getActions(page, start, end)));
			SelectionActionProvider[] saps = ImageDocumentMarkupPanel.this.ggImagine.getSelectionActionProviders();
			for (int p = 0; p < saps.length; p++) try {
				SelectionAction[] sas = saps[p].getActions(start, end, page, this, ImageDocumentMarkupPanel.this);
				if ((sas != null) && (sas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(sas));
				}
			}
			catch (Exception e) {
				System.out.println("Error getting actions for box selection: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			return ((SelectionAction[]) actions.toArray(new SelectionAction[actions.size()]));
		}
		private void addAdvancedAction(ArrayList actions, ImAnnotation imSelection) {
			if (imSelection == null)
				return;
			Annotation selection = new LazyAnnotation(imSelection, ImageDocumentMarkupPanel.this.getXmlWrapperFlags());
			SelectionAction asa = ImageUserInterfaceUtils.createAdvancedSelectionAction(ImageDocumentMarkupPanel.this.goldenGate, "ggImagine", ImageDocumentMarkupPanel.this, selection);
			if (asa != null)
				actions.add(asa);
		}
		protected ClickSelectionAction[] getClickActions(ImPage page, Point point, int clickCount) {
			ArrayList actions = new ArrayList(Arrays.asList(super.getClickActions(page, point, clickCount)));
			ClickActionProvider[] caps = ImageDocumentMarkupPanel.this.ggImagine.getClickActionProviders();
			for (int p = 0; p < caps.length; p++) try {
				ClickSelectionAction[] csas = caps[p].getActions(page, point, clickCount, this, ImageDocumentMarkupPanel.this);
				if ((csas != null) && (csas.length != 0)) {
					if (actions.size() != 0)
						actions.add(SelectionAction.SEPARATOR);
					actions.addAll(Arrays.asList(csas));
				}
			}
			catch (Exception e) {
				System.out.println("Error getting actions for point click: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			return ((ClickSelectionAction[]) actions.toArray(new ClickSelectionAction[actions.size()]));
		}
		protected JMenuItem getContextMenuItemFor(SelectionAction action) {
			return ImageUserInterfaceUtils.styleContextMenuItem(super.getContextMenuItemFor(action), action);
		}
		protected boolean[] markAdvancedSelectionActions(SelectionAction[] sas) {
//			return ImageDocumentMarkupPanel.this.saUsageStats.markAdvancedSelectionActions(sas);
			return getSelectionActionUsageStats().markAdvancedSelectionActions(sas);
		}
		protected void selectionActionPerformed(SelectionAction sa) {
//			ImageDocumentMarkupPanel.this.saUsageStats.selectionActionUsed(sa);
			getSelectionActionUsageStats().selectionActionUsed(sa);
		}
		protected String getAttributeEditorAnnotationValue(ImAnnotation annotation) {
			return ImageUserInterfaceUtils.getAnnotationLabelValue(annotation);
		}
		protected DisplayExtensionGraphics[] getDisplayExtensionGraphics(ImPage page) {
			ArrayList degs = new ArrayList();
			DisplayExtensionProvider[] deps = ImageDocumentMarkupPanel.this.ggImagine.getDisplayExtensionProviders();
			for (int p = 0; p < deps.length; p++) try {
				DisplayExtension[] des = deps[p].getDisplayExtensions();
				if (des == null)
					continue;
				for (int e = 0; e < des.length; e++) {
					if (des[e].isActive())
						degs.addAll(Arrays.asList(des[e].getExtensionGraphics(page, this)));
				}
			}
			catch (Exception e) {
				System.out.println("Error getting display extension graphics: " + e.getMessage());
				e.printStackTrace(System.out);
			}
			return ((DisplayExtensionGraphics[]) degs.toArray(new DisplayExtensionGraphics[degs.size()]));
		}
		protected ImImageEditTool[] getImageEditTools() {
			ArrayList tools = new ArrayList(Arrays.asList(super.getImageEditTools()));
			ImageEditToolProvider[] ietps = ImageDocumentMarkupPanel.this.ggImagine.getImageEditToolProviders();
			for (int p = 0; p < ietps.length; p++) {
				ImImageEditTool[] iets = ietps[p].getImageEditTools();
				if (iets != null)
					tools.addAll(Arrays.asList(iets));
			}
			return ((ImImageEditTool[]) tools.toArray(new ImImageEditTool[tools.size()]));
		}
		public ProgressMonitor getProgressMonitor(String title, String text, boolean supportPauseResume, boolean supportAbort) {
			return ImageDocumentMarkupPanel.this.getProgressMonitor(title, text, supportPauseResume, supportAbort);
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
			this.ensurePositionVisible(vpPos, wsPos);
			
			//	pass on super class success
			return true;
		}
		public boolean setBoxSelection(int pageId, BoundingBox box) {
			if (!super.setBoxSelection(pageId, box))
				return false;
			
			//	get position of word selection, and compare to current view
			Rectangle vpPos = idmpBox.getViewport().getViewRect();
			Rectangle boxPos = this.getPosition(box, pageId);
			if (boxPos == null)
				return true;
			Rectangle bsPos = new Rectangle(boxPos);
			
			//	box selection doesn't fit view vertically, reduce height
			if (vpPos.height < bsPos.height)
				bsPos.height = vpPos.height;
			
			//	box selection doesn't fit view horizontally, reduce width
			if (vpPos.width < bsPos.width)
				bsPos.width = vpPos.width;
			
			//	scroll selection to view if required (moving near center)
			this.ensurePositionVisible(vpPos, bsPos);
			
			//	pass on super class success
			return true;
		}
		private void ensurePositionVisible(Rectangle vpPos, Rectangle visPos) {
			if (vpPos.contains(visPos))
				return;
//			idmpBox.getViewport().scrollRectToVisible(wsPos); // DOESN'T SEEM TO WORK AS SUPPOSED TO, FOR WHATEVER REASON
			
			//	compute target X coordinate
			int vx;
			if ((vpPos.x <= visPos.x) && ((vpPos.x + vpPos.width) >= (visPos.x + visPos.width))) // selection in bounds horizontally, no need for scrolling
				vx = vpPos.x;
			else /* center selection in viewport */ {
				int wscx = (visPos.x + (visPos.width / 2));
				vx = (wscx - (vpPos.width / 2));
				if (vpPos.x < vx) // scrolling right, don't go all that far
					vx -= (vpPos.width / 4);
				else if (vpPos.x > vx) // scrolling left, don't go all that far
					vx += (vpPos.width / 4);
				if (vx < 0)
					vx = 0;
			}
			
			//	compute target Y coordinate
			int vy;
			if ((vpPos.y <= visPos.y) && ((vpPos.y + vpPos.height) >= (visPos.y + visPos.height))) // selection in bounds vertically, no need for scrolling
				vy = vpPos.y;
			else /* center selection in viewport */ {
				int wscy = (visPos.y + (visPos.height / 2));
				vy = (wscy - (vpPos.height / 2));
				if (vpPos.y < vy) // scrolling down, don't go all that far
					vy -= (vpPos.height / 4);
				else if (vpPos.y > vy) // scrolling up, don't go all that far
					vy += (vpPos.height / 4);
				if (vy < 0)
					vy = 0;
			}
			
			//	perform scroll
			idmpBox.getViewport().setViewPosition(new Point(vx, vy));
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
		public ImPage[] getVisiblePages() {
			ImPage[] sPages = super.getVisiblePages();
			Rectangle vpPos = idmpBox.getViewport().getViewRect();
			ArrayList vPages = new ArrayList();
			for (int p = 0; p < sPages.length; p++) {
				Rectangle pPos = this.getPosition(sPages[p]);
				if (pPos == null)
					continue;
				if (vpPos.intersects(pPos))
					vPages.add(sPages[p]);
			}
			return ((ImPage[]) vPages.toArray(new ImPage[vPages.size()]));
		}
		public void setSideBySidePages(int sbsp) {
			if (sbsp == this.getSideBySidePages())
				return;
			super.setSideBySidePages(sbsp);
			ImageDocumentMarkupPanel.this.validate();
			ImageDocumentMarkupPanel.this.repaint();
		}
//		public void applyMarkupTool(ImageMarkupTool imt, ImAnnotation annot) {
//			try {
//				imToolActive = true;
//				super.applyMarkupTool(imt, annot);
//			}
//			finally {
//				imToolActive = false;
//			}
//		}
		public void applyMarkupTool(ImageMarkupTool imt, ImAnnotation annot) {
			this.applyMarkupTool(imt, annot, true);
		}
		void applyMarkupTool(ImageMarkupTool xmt, ImAnnotation annot, boolean isNativeImMarkupTool) {
			if (isNativeImMarkupTool)
				invalidateXmlWrappers(); // no use tagging along any wrappers, good chance they break at some point anyway
			try {
				imToolActive = true;
				super.applyMarkupTool(xmt, annot);
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
	
	private class AtomicActionNotifier implements AtomicActionListener {
		GoldenGateImagine ggImagine;
		ImDocumentMarkupPanel idmp;
		AtomicActionNotifier(GoldenGateImagine ggImagine, ImDocumentMarkupPanel idmp) {
			this.ggImagine = ggImagine;
			this.idmp = idmp;
		}
		public void atomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ProgressMonitor pm) {
			this.ggImagine.notifyAtomicActionStarted(id, label, imt, annot, this.idmp, pm);
		}
		public void atomicActionFinishing(long id, ProgressMonitor pm) {
			this.ggImagine.notifyAtomicActionFinishing(id, this.idmp, pm);
		}
		public void atomicActionFinished(long id, ProgressMonitor pm) {
			this.ggImagine.notifyAtomicActionFinished(id, this.idmp, pm);
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
	 * Show all pages containing main text words, and hide all others.
	 */
	public void showMainTextPages() {
		ImPage[] pages = this.idmp.document.getPages();
		int[] visiblePageIDs = new int[pages.length];
		for (int p = 0; p < pages.length; p++) {
			visiblePageIDs[p] = -1;
			ImWord[] ptshs = pages[p].getTextStreamHeads();
			for (int h = 0; h < ptshs.length; h++)
				if (ImWord.TEXT_STREAM_TYPE_MAIN_TEXT.equals(ptshs[h].getTextStreamType())) {
					visiblePageIDs[p] = pages[p].pageId;
					break;
				}
		}
		this.idmp.setVisiblePages(visiblePageIDs);
	}
	
	/**
	 * Show all pages, including ones that are currently hidden.
	 */
	public void showAllPages() {
		ImPage[] pages = this.idmp.document.getPages();
		this.idmp.setPagesVisible(pages[0].pageId, pages[pages.length - 1].pageId, true);
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
//			this.undoActions.addFirst(ua);
			this.undoActions.add(ua);
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
//			this.undoActions.addFirst(this.multipartUndoAction);
			this.undoActions.add(this.multipartUndoAction);
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
//	public void updateUndoMenu() {
//		JMenu undoMenu = this.getUndoMenu();
//		if (undoMenu == null)
//			return;
//		undoMenu.removeAll();
//		for (Iterator uait = this.undoActions.iterator(); uait.hasNext();) {
//			final UndoAction ua = ((UndoAction) uait.next());
//			JMenuItem mi = new JMenuItem(ua.label);
//			mi.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent ae) {
//					try {
//						ua.target.inUndoAction = true;
//						long us = System.currentTimeMillis();
//						while (undoActions.size() != 0) {
//							UndoAction eua = ((UndoAction) undoActions.removeFirst());
//							try {
//								if (ua instanceof MultipartUndoAction)
//									ua.target.idmp.startAtomicAction(((MultipartUndoAction) ua).actionId, "UNDO", null, null, null);
//								else ua.target.idmp.startAtomicAction(-1, "UNDO", null, null, null);
//								eua.execute();
//							}
//							finally {
//								ua.target.idmp.endAtomicAction();
//							}
//							if (eua == ua)
//								break;
//						}
//						
//						updateUndoMenu();
//						System.out.println("Executed undo actions in " + (System.currentTimeMillis() - us) + "ms");
//					}
//					finally {
//						ua.target.inUndoAction = false;
//						
//						/* we are on the EDT, so we can repaint right here
//						 * without any risk of incurring a deadlock between
//						 * on synchronized parts of UI or data structures */
//						ua.target.idmp.validate();
//						ua.target.idmp.repaint();
//						ua.target.idmp.validateControlPanel();
//					}
//				}
//			});
//			undoMenu.add(mi);
//			if (undoMenu.getMenuComponentCount() >= 10)
//				break;
//		}
//		undoMenu.setEnabled(this.undoActions.size() != 0);
//	}
	public void updateUndoMenu() {
		System.out.println("ImageDocumentMarkupPanel: updating UNDO menu");
		WindowMenuOwner undoMenuOwner = this.getUndoMenuOwner();
		DynamicWindowMenu undoMenu = this.getUndoMenu();
		if ((undoMenuOwner == null) || (undoMenu == null)) {
			System.out.println(" ==> menu or menu owner is null");
			return;
		}
		undoMenu.clearElements(undoMenuOwner);
//		for (Iterator uait = this.undoActions.iterator(); uait.hasNext();) {
		for (int a = (this.undoActions.size() - 1); a >= 0; a--) {
//			final UndoAction ua = ((UndoAction) uait.next());
			final UndoAction ua = ((UndoAction) this.undoActions.get(a));
			JMenuItem mi = new JMenuItem();
			UserInterfaceUtils.styleDesktopMenuItem(mi, "ggImagine", "undo.option", ua.label, ("Revert all modifications back to '" + ua.label + "'"), null, null);
			mi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						ua.target.inUndoAction = true;
						long us = System.currentTimeMillis();
						while (undoActions.size() != 0) {
//							UndoAction eua = ((UndoAction) undoActions.removeFirst());
							UndoAction eua = ((UndoAction) undoActions.remove(undoActions.size() - 1));
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
			undoMenu.addDesktopElement(mi, undoMenuOwner);
			if (undoMenuMaxSize <= undoMenu.itemCount())
				break;
		}
		System.out.println(" ==> added " + undoMenu.itemCount() + " items");
//		undoMenu.updateItems(undoMenuOwner);
		undoMenuOwner.updateMenu();
		System.out.println(" ==> menu refresh done");
	}
	
	/**
	 * Provide the owner of the 'Undo' menu integrated in a UI for the markup
	 * panel to show its 'Undo' options in. If this method returns null, UI
	 * based 'Undo' will not be accessible.
	 * @return the owner of the 'Undo' menu of the surrounding UI
	 */
	protected abstract WindowMenuOwner getUndoMenuOwner();
	
	/**
	 * Provide the 'Undo' menu integrated in a UI for the markup panel to show
	 * its 'Undo' options in. If this method returns null, UI based 'Undo' will
	 * not be accessible.
	 * @return the 'Undo' menu of the surrounding UI
	 */
//	protected abstract JMenu getUndoMenu();
	protected abstract DynamicWindowMenu getUndoMenu();
	
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
//		
//		if (storeSettings) {
//			Settings annotationColors = this.ggiConfig.getSubset("annotation.color");
//			String[] annotationTypes = this.idmp.getAnnotationTypes();
//			for (int t = 0; t < annotationTypes.length; t++) {
//				Color ac = this.idmp.getAnnotationColor(annotationTypes[t]);
//				if (ac != null)
//					annotationColors.setSetting(annotationTypes[t], GoldenGateImagine.getHex(ac));
//			}
//			Settings layoutObjectColors = this.ggiConfig.getSubset("layoutObject.color");
//			String[] layoutObjectTypes = this.idmp.getLayoutObjectTypes();
//			for (int t = 0; t < layoutObjectTypes.length; t++) {
//				Color loc = this.idmp.getLayoutObjectColor(layoutObjectTypes[t]);
//				if (loc != null)
//					layoutObjectColors.setSetting(layoutObjectTypes[t], GoldenGateImagine.getHex(loc));
//			}
//			Settings textStreamColors = this.ggiConfig.getSubset("textStream.color");
//			String[] textStreamTypes = this.idmp.getTextStreamTypes();
//			for (int t = 0; t < textStreamTypes.length; t++) {
//				Color tsc = this.idmp.getTextStreamTypeColor(textStreamTypes[t]);
//				if (tsc != null)
//					textStreamColors.setSetting(textStreamTypes[t], GoldenGateImagine.getHex(tsc));
//			}
//			this.saUsageStats.storeTo(this.ggiConfig.getSubset("selectionAction"));
//		}
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
	
//	private static class MultipartUndoAction extends UndoAction {
//		final LinkedList parts = new LinkedList();
//		final long actionId;
//		MultipartUndoAction(long id, String label, ImageDocumentMarkupPanel target) {
//			super(label, target);
//			this.actionId = -id;
//		}
//		synchronized void addUndoAction(UndoAction ua) {
//			this.parts.addFirst(ua);
//		}
//		void doExecute() {
//			while (this.parts.size() != 0)
//				((UndoAction) this.parts.removeFirst()).doExecute();
//		}
//	}
	private static class MultipartUndoAction extends UndoAction {
//		final LinkedList parts = new LinkedList();
		final ArrayList parts = new ArrayList();
		final long actionId;
		MultipartUndoAction(long id, String label, ImageDocumentMarkupPanel target) {
			super(label, target);
			this.actionId = -id;
		}
		synchronized void addUndoAction(UndoAction ua) {
//			this.parts.addFirst(ua);
			this.parts.add(ua);
		}
		void doExecute() {
//			while (this.parts.size() != 0)
//				((UndoAction) this.parts.removeFirst()).doExecute();
			for (int p = (this.parts.size() - 1); p >= 0; p--)
				((UndoAction) this.parts.get(p)).doExecute();
			this.parts.clear();
		}
	}
	
//	private static SelectionActionUsageStats selectionActionUsageStats = null;
//	private static SelectionActionUsageStats getSelectionActionUsageStats(Settings ggiConfig) {
//		if (selectionActionUsageStats == null) {
//			selectionActionUsageStats = new SelectionActionUsageStats();
//			selectionActionUsageStats.fillFrom(ggiConfig.getSubset("selectionAction"));
//		}
//		return selectionActionUsageStats;
//	}
//	private static class SelectionActionUsageStats extends TreeMap {
//		private static class SelectionActionUsage {
//			int shown = 0;
//			int used = 0;
//			int usedLast = 0;
//			SelectionActionUsage() {}
//		}
//		
//		private int isSaAdvancedPivotIndex = 10;
//		private int saUseCounter = 1;
//		
//		private SelectionActionUsage getSelectionActionUsage(String saName) {
//			SelectionActionUsage saUsage = ((SelectionActionUsage) this.get(saName));
//			if (saUsage == null) {
//				saUsage = new SelectionActionUsage();
//				this.put(saName, saUsage);
//			}
//			return saUsage;
//		}
//		
//		boolean[] markAdvancedSelectionActions(SelectionAction[] sas) {
//			float[] isSaAdvancedScoresBySa = new float[sas.length];
//			float[] isSaAdvancedScoresByVal = new float[sas.length];
//			for (int a = 0; a < sas.length; a++) {
//				float isSaAdvancedScore = 0;
//				if (sas[a] != SelectionAction.SEPARATOR) {
//					SelectionActionUsage saUsage = this.getSelectionActionUsage(sas[a].name);
//					saUsage.shown++;
//					isSaAdvancedScore += (((float) saUsage.used) / saUsage.shown); // MFU part
//					isSaAdvancedScore += (((float) saUsage.usedLast) / this.saUseCounter); // MRU part
//				}
//				isSaAdvancedScoresBySa[a] = isSaAdvancedScore;
//				isSaAdvancedScoresByVal[a] = isSaAdvancedScore;
//			}
//			
//			Arrays.sort(isSaAdvancedScoresByVal);
//			float isSaAdvancedThreshold = ((sas.length < this.isSaAdvancedPivotIndex) ? 0 : isSaAdvancedScoresByVal[sas.length - this.isSaAdvancedPivotIndex]);
//			
//			boolean[] isSaAdvanced = new boolean[sas.length];
//			for (int a = 0; a < sas.length; a++)
//				isSaAdvanced[a] = (isSaAdvancedScoresBySa[a] < isSaAdvancedThreshold);
//			
//			return isSaAdvanced;
//		}
//		
//		void selectionActionUsed(SelectionAction sa) {
//			SelectionActionUsage saUsage = this.getSelectionActionUsage(sa.name);
//			saUsage.used++;
//			saUsage.usedLast = this.saUseCounter++;
//		}
//		
//		void fillFrom(Settings set) {
//			this.isSaAdvancedPivotIndex = Math.max(1, Integer.parseInt(set.getSetting("isAdvancedPivotIndex", ("" + this.isSaAdvancedPivotIndex))));
//			this.saUseCounter = Math.max(1, Integer.parseInt(set.getSetting("useCounter", "1")));
//			
//			String[] saNames = set.getSubsetPrefixes();
//			for (int n = 0; n < saNames.length; n++) {
//				Settings saUsageSet = set.getSubset(saNames[n]);
//				SelectionActionUsage saUsage = this.getSelectionActionUsage(saNames[n]);
//				saUsage.shown = Integer.parseInt(saUsageSet.getSetting("shown", "0"));
//				saUsage.used = Integer.parseInt(saUsageSet.getSetting("used", "0"));
//				saUsage.usedLast = Integer.parseInt(saUsageSet.getSetting("usedLast", "0"));
//			}
//		}
//		void storeTo(Settings set) {
//			set.setSetting("isAdvancedPivotIndex", ("" + this.isSaAdvancedPivotIndex));
//			set.setSetting("useCounter", ("" + this.saUseCounter));
//			
//			for (Iterator sanit = this.keySet().iterator(); sanit.hasNext();) {
//				String saName = ((String) sanit.next());
//				SelectionActionUsage saUsage = this.getSelectionActionUsage(saName);
//				Settings saUsageSet = set.getSubset(saName);
//				saUsageSet.setSetting("shown", ("" + saUsage.shown));
//				saUsageSet.setSetting("used", ("" + saUsage.used));
//				saUsageSet.setSetting("usedLast", ("" + saUsage.usedLast));
//			}
//		}
//	}
	private static SelectionActionUsageStats selectionActionUsageStats = null;
	private static SelectionActionUsageStats getSelectionActionUsageStats() {
		if (selectionActionUsageStats == null)
			selectionActionUsageStats = new SelectionActionUsageStats();
		return selectionActionUsageStats;
	}
	private static class SelectionActionUsageStats {
		private int advancedActionPivotIndex = 10;
		private NamedElementUsageStatistics stats;
		SelectionActionUsageStats() {
			this.stats = NamedElementUsageStatistics.getElementUsageStatistics("main");
			Object contextMenuBaseSize = UserInterfaceUtils.getDisplayProperty("main.contextMenuBaseSize");
			if (contextMenuBaseSize instanceof Number)
				this.advancedActionPivotIndex = Math.max(((Number) contextMenuBaseSize).intValue(), this.advancedActionPivotIndex);
			else UserInterfaceUtils.setDisplayProperty("main.contextMenuBaseSize", new Integer(this.advancedActionPivotIndex));
		}
		boolean[] markAdvancedSelectionActions(SelectionAction[] actions) {
			String[] actionNames = new String[actions.length];
			for (int a = 0; a < actions.length; a++) {
				if (actions[a] != SelectionAction.SEPARATOR)
					actionNames[a] = actions[a].name;
			}
			
			float[] actionUsageScores = this.stats.getElementUsageScores(actionNames);
			float[] actionUsageScoresSorted = Arrays.copyOf(actionUsageScores, actionUsageScores.length);
			
			Arrays.sort(actionUsageScoresSorted);
			float isSaAdvancedThreshold = ((actions.length < this.advancedActionPivotIndex) ? 0 : actionUsageScoresSorted[actions.length - this.advancedActionPivotIndex]);
			
			boolean[] isAdvancedAction = new boolean[actions.length];
			for (int a = 0; a < actions.length; a++)
				isAdvancedAction[a] = (actionUsageScores[a] < isSaAdvancedThreshold);
			
			return isAdvancedAction;
		}
		void selectionActionUsed(SelectionAction sa) {
			this.stats.elementUsed(sa.name);
		}
	}
}