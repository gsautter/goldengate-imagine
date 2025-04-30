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
package de.uka.ipd.idaho.im.imagine;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.PageImage;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageInputStream;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageStore;
import de.uka.ipd.idaho.gamta.util.imaging.PageImageStore.AbstractPageImageStore;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.GoldenGateConstants;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.goldenGate.ui.UserInterfaceUtils;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImSupplement;
import de.uka.ipd.idaho.im.imagine.plugins.ClickActionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionListener;
import de.uka.ipd.idaho.im.imagine.plugins.DisplayExtensionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineAtomicActionListener;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImagineDocumentListener.CancelSavingException;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentIoProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageEditToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider;
import de.uka.ipd.idaho.im.imagine.plugins.ReactionProvider;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.ocr.OcrEngine;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.im.util.ImSupplementCache;

/**
 * @author sautter
 *
 */
public class GoldenGateImagine implements GoldenGateConstants {
//	
//	private static final SimpleDateFormat yearTimestamper = new SimpleDateFormat("yyyy");
//	private static final String ABOUT_TEXT = 
//		"GoldenGATE Imagine " + VERSION_STRING + "\n" +
//		"The easy way to mark up Documents\n" +
//		"Version Date: " + VERSION_DATE + "\n" +
//		"\n" +
//		"\u00A9 by Guido Sautter 2006-" + yearTimestamper.format(new Date()) + "\n" +
//		"IPD Boehm\n" +
//		"Karlsruhe Institute of Technology (KIT)";
	
	private static final int maxInMemorySupplementBytes = (50 * 1024 * 1024); // 50 MB
	
	private GoldenGateConfiguration configuration;
	private GoldenGATE goldenGate;
	private Settings settings;
	
	private File rootFolder;
	private File cacheRootFolder;
	
	private GgiPageImageStore pageImageStore;
	private PdfExtractor pdfExtractor;
	
	private long imfStorageFlags = ImDocumentIO.STORAGE_MODE_CSV; // use CSV by default (at least for now)
	
	private GoldenGateImagine(GoldenGateConfiguration configuration, GoldenGATE gg, File rootFolder) {
		this.configuration = configuration;
		this.goldenGate = gg;
		this.rootFolder = rootFolder;
		
		//	get settings
		this.settings = this.goldenGate.getApplicationSettings("GgImagine.cnfg");
		String[] setNames = this.settings.getKeys();
		for (int n = 0; n < setNames.length; n++) try {
			UserInterfaceUtils.decodeDisplayProperty(setNames[n], this.settings.getSetting(setNames[n]));
		}
		catch (RuntimeException re) {
			System.out.println("Failed to initialize property '" + setNames[n] + "' from central GoldenGATE Imagine settings: " + re.getMessage());
		}
		
		//	read IMF storage flags
		Object imfStorageFlagObj = UserInterfaceUtils.getDisplayProperty("imfStorageFlags");
		String imfStorageFlagStr = ((imfStorageFlagObj instanceof String) ? ((String) imfStorageFlagObj) : null);
		if (imfStorageFlagStr != null) try {
			this.imfStorageFlags = Long.parseLong(imfStorageFlagStr, 16);
		}
		catch (RuntimeException re) {
			imfStorageFlagStr = null; // mark as invalid to hae replaced below
		}
		if (imfStorageFlagStr == null) // not a string, or failed to parse
			UserInterfaceUtils.setDisplayProperty("imfStorageFlags", Long.toString(this.imfStorageFlags, 16).toUpperCase());
		
		//	read cache root path
		String cacheRootFolderName = this.settings.getSetting("cacheRootFolder");
		if (cacheRootFolderName == null)
			this.cacheRootFolder = this.rootFolder;
		else {
			if (cacheRootFolderName.startsWith("/") || (cacheRootFolderName.indexOf(':') != -1))
				this.cacheRootFolder = new File(cacheRootFolderName);
			else this.cacheRootFolder = new File(this.rootFolder, cacheRootFolderName);
			if (!this.cacheRootFolder.exists())
				this.cacheRootFolder.mkdirs();
		}
		
		//	create page image store
		String pageImageFolderName = this.settings.getSetting("pageImageFolder", "./PageImages");
		File pageImageFolder;
		if (pageImageFolderName.startsWith("/") || (pageImageFolderName.indexOf(':') != -1))
			pageImageFolder = new File(pageImageFolderName);
		else if (pageImageFolderName.startsWith("./"))
			pageImageFolder = new File(this.rootFolder, pageImageFolderName.substring("./".length()));
		else pageImageFolder = new File(this.cacheRootFolder, pageImageFolderName);
		if (!pageImageFolder.exists())
			pageImageFolder.mkdirs();
		this.pageImageStore = new GgiPageImageStore(pageImageFolder);
		PageImage.addPageImageSource(this.pageImageStore);
		
		//	get and index applicable plugins (only now, as instance proper is fully initialized)
		GoldenGatePlugin[] ggps = this.goldenGate.getPlugins();
		for (int p = 0; p < ggps.length; p++) {
			if (ggps[p] instanceof GoldenGateImaginePlugin) try {
				((GoldenGateImaginePlugin) ggps[p]).setImagineParent(this);
				((GoldenGateImaginePlugin) ggps[p]).initImagine();
			}
			catch (Throwable t) {
				System.out.println(t.getClass().getName() + " (" + t.getMessage() + ") while initializing " + ggps[p].getClass().getName());
				t.printStackTrace(System.out);
				continue;
			}
			if (ggps[p] instanceof ImageEditToolProvider)
				this.registerImageEditToolProvider((ImageEditToolProvider) ggps[p]);
			if (ggps[p] instanceof ImageMarkupToolProvider)
				this.registerImageMarkupToolProvider((ImageMarkupToolProvider) ggps[p]);
			if (ggps[p] instanceof SelectionActionProvider)
				this.registerSelectionActionProvider((SelectionActionProvider) ggps[p]);
			if (ggps[p] instanceof ClickActionProvider)
				this.registerClickActionProvider((ClickActionProvider) ggps[p]);
			if (ggps[p] instanceof ImageDocumentDropHandler)
				this.registerDropHandler((ImageDocumentDropHandler) ggps[p]);
			if (ggps[p] instanceof ImageDocumentIoProvider)
				this.registerDocumentIoProvider((ImageDocumentIoProvider) ggps[p]);
			if (ggps[p] instanceof ImageDocumentExporter)
				this.registerDocumentExporter((ImageDocumentExporter) ggps[p]);
			if (ggps[p] instanceof ReactionProvider)
				this.registerReactionProvider((ReactionProvider) ggps[p]);
			if (ggps[p] instanceof DisplayExtensionProvider)
				this.registerDisplayExtensionProvider((DisplayExtensionProvider) ggps[p]);
			if (ggps[p] instanceof GoldenGateImagineDocumentListener)
				this.registerDocumentListener((GoldenGateImagineDocumentListener) ggps[p]);
			if (ggps[p] instanceof GoldenGateImagineAtomicActionListener)
				this.registerAtomicActionListener((GoldenGateImagineAtomicActionListener) ggps[p]);
		}
	}
	
	private static class GgiPageImageStore extends AbstractPageImageStore {
		private File pageImageFolder;
		GgiPageImageStore(File pageImageFolder) {
			this.pageImageFolder = pageImageFolder;
		}
		public boolean isPageImageAvailable(String name) {
			if (!name.endsWith(IMAGE_FORMAT))
				name += ("." + IMAGE_FORMAT);
			File pif = new File(this.pageImageFolder, name);
			return pif.exists();
		}
		public PageImageInputStream getPageImageAsStream(String name) throws IOException {
			if (!name.endsWith(IMAGE_FORMAT))
				name += ("." + IMAGE_FORMAT);
			File pif = new File(this.pageImageFolder, name);
			if (pif.exists())
				return new PageImageInputStream(new BufferedInputStream(new FileInputStream(pif)), this);
			else return null;
		}
		public boolean storePageImage(String name, PageImage pageImage) throws IOException {
			if (!name.endsWith(IMAGE_FORMAT))
				name += ("." + IMAGE_FORMAT);
			try {
				File pif = new File(this.pageImageFolder, name);
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
		void cleanup(final String docId) {
			 try {
				File[] docPageImages = this.pageImageFolder.listFiles(new FileFilter() {
					public boolean accept(File file) {
						return (file.isFile() && file.getName().startsWith(docId + "."));
					}
				});
				for (int s = 0; s < docPageImages.length; s++)
					docPageImages[s].delete();
			}
			catch (Exception e) {
				System.out.println("Error cleaning up import page image cache for document '" + docId + "': " + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
	}
	
	private Map docSupplementFoldersById = Collections.synchronizedMap(new HashMap());
	private class GgiPdfExtractor extends PdfExtractor {
		private File supplementFolder;
		GgiPdfExtractor(File basePath, File cachePath, PageImageStore imageStore, boolean useMultipleCores, File supplementFolder) {
			super(basePath, cachePath, imageStore, useMultipleCores);
			this.supplementFolder = supplementFolder;
		}
		protected ImDocument createDocument(String docId) {
			File docSupplementFolder = new File(this.supplementFolder, ("doc" + docId));
			docSupplementFoldersById.put(docId, docSupplementFolder);
			return new GgiImDocument(docId, docSupplementFolder);
		}
	}
	
	private class GgiImDocument extends ImDocument {
		private ImSupplementCache supplementCache;
		GgiImDocument(String docId, File supplementFolder) {
			super(docId);
			this.supplementCache = new ImSupplementCache(this, supplementFolder, maxInMemorySupplementBytes);
		}
		public ImSupplement addSupplement(ImSupplement ims) {
			ims = this.supplementCache.cacheSupplement(ims);
			return super.addSupplement(ims);
		}
		public void removeSupplement(ImSupplement ims) {
			this.supplementCache.deleteSupplement(ims);
			super.removeSupplement(ims);
		}
	}
	
	private void ensurePdfExtractor() {
		if (this.pdfExtractor != null)
			return;
		
		//	create PDF reader caching supplements on disc
		String supplementFolderName = this.settings.getSetting("supplementFolder", "./Supplements");
		File supplementFolder;
		if (supplementFolderName.startsWith("/") || (supplementFolderName.indexOf(':') != -1))
			supplementFolder = new File(supplementFolderName);
		else if (supplementFolderName.startsWith("./"))
			supplementFolder = new File(this.rootFolder, supplementFolderName.substring("./".length()));
		else supplementFolder = new File(this.cacheRootFolder, supplementFolderName);
		if (!supplementFolder.exists())
			supplementFolder.mkdirs();
		this.pdfExtractor = new GgiPdfExtractor(this.rootFolder, this.cacheRootFolder, this.pageImageStore, true, supplementFolder);
	}
	
	/**
	 * Retrieve the PDF Extractor embedded in this GoldenGATE Imagine instance.
	 * @return the PDF Extractor
	 */
	public PdfExtractor getPdfExtractor() {
		this.ensurePdfExtractor();
		return this.pdfExtractor;
	}
	
	/**
	 * Retrieve the OCR Engine embedded in this GoldenGATE Imagine instance.
	 * @return the OCR Engine
	 */
	public OcrEngine getOcrEngine() {
		this.ensurePdfExtractor();
		return this.pdfExtractor.getOcrEngine();
	}
	
	/**
	 * Retrieve the storage flags to use for persisting Image Markup documents
	 * on the local file system.
	 * @return the storage flag vector
	 */
	public long getImfStorageFlags() {
		return this.imfStorageFlags;
	}
	
	/**
	 * Update the storage flags to use for persisting Image Markup documents on
	 * the local file system.
	 * @param isf the storage flag vector to set
	 */
	public void setImfStorageFlags(long isf) {
		this.imfStorageFlags = isf;
		UserInterfaceUtils.setDisplayProperty("imfStorageFlags", Long.toString(this.imfStorageFlags, 16).toUpperCase());
	}
	
	/**
	 * @return the GoldenGATE icon as provided by the current configuration
	 */
	public Image getGoldenGateIcon() {
		return this.goldenGate.getGoldenGateIcon();
	}
	
	/**
	 * @return the name of the configuration wrapped in this GoldenGATE Imagine instance
	 */
	public String getConfigurationName() {
		return this.goldenGate.getConfigurationName();
	}
	
	/**
	 * @return the enclosed GoldenGATE instance
	 */
	public GoldenGATE getGoldenGATE() {
		return this.goldenGate;
	}
	
	//	register and lookup method for drop handlers
	private HashMap dropHandlersByClassName = new LinkedHashMap();
	
	private void registerDropHandler(ImageDocumentDropHandler iddh) {
		if (iddh != null)
			this.dropHandlersByClassName.put(iddh.getClass().getName(), iddh);
	}
	
	/**
	 * Find a document drop handler by its class name.
	 * @param pluginClassName the class name of the desired drop handler
	 * @return the drop handler with the specified class name
	 */
	public ImageDocumentDropHandler getDropHandler(String pluginClassName) {
		return ((ImageDocumentDropHandler) this.dropHandlersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all drop handlers that are currently available.
	 * @return an array holding all drop handlers registered
	 */
	public ImageDocumentDropHandler[] getDropHandlers() {
		ArrayList iddhs = new ArrayList(this.dropHandlersByClassName.values());
		return ((ImageDocumentDropHandler[]) iddhs.toArray(new ImageDocumentDropHandler[iddhs.size()]));
	}
	
	//	register and lookup method for document IO providers
	private HashMap docmentIoProvidersByClassName = new LinkedHashMap();
	
	private void registerDocumentIoProvider(ImageDocumentIoProvider idip) {
		if (idip != null)
			this.docmentIoProvidersByClassName.put(idip.getClass().getName(), idip);
	}
	
	/**
	 * Find a document IO provider by its class name.
	 * @param pluginClassName the class name of the desired IO provider
	 * @return the document IO provider with the specified class name
	 */
	public ImageDocumentIoProvider getDocumentIoProvider(String pluginClassName) {
		return ((ImageDocumentIoProvider) this.docmentIoProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all document IO providers that are currently available.
	 * @return an array holding all document IO providers registered
	 */
	public ImageDocumentIoProvider[] getDocumentIoProviders() {
		ArrayList idips = new ArrayList(this.docmentIoProvidersByClassName.values());
		return ((ImageDocumentIoProvider[]) idips.toArray(new ImageDocumentIoProvider[idips.size()]));
	}
	
	//	register and lookup method for document exporters
	private HashMap documentExportersByClassName = new LinkedHashMap();
	
	private void registerDocumentExporter(ImageDocumentExporter ide) {
		if (ide != null)
			this.documentExportersByClassName.put(ide.getClass().getName(), ide);
	}
	
	/**
	 * Find a document exporter by its class name.
	 * @param pluginClassName the class name of the desired exporter
	 * @return the exporter with the specified class name
	 */
	public ImageDocumentExporter getDocumentExporter(String pluginClassName) {
		return ((ImageDocumentExporter) this.documentExportersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all document exporters that are currently available.
	 * @return an array holding all document exporters registered
	 */
	public ImageDocumentExporter[] getDocumentExporters() {
		ArrayList ides = new ArrayList(this.documentExportersByClassName.values());
		return ((ImageDocumentExporter[]) ides.toArray(new ImageDocumentExporter[ides.size()]));
	}
	
	//	register and lookup method for image edit tool providers
	private HashMap imageEditToolProvidersByClassName = new LinkedHashMap();
	
	private void registerImageEditToolProvider(ImageEditToolProvider ietp) {
		if (ietp  != null)
			this.imageEditToolProvidersByClassName.put(ietp.getClass().getName(), ietp);
	}
	
	/**
	 * Find an image edit tool provider by its class name.
	 * @param pluginClassName the class name of the desired image edit tool provider
	 * @return the image edit tool provider with the specified class name
	 */
	public ImageEditToolProvider getImageEditToolProvider(String pluginClassName) {
		return ((ImageEditToolProvider) this.imageEditToolProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all image edit tool providers that are currently available.
	 * @return an array holding all image edit tool providers registered
	 */
	public ImageEditToolProvider[] getImageEditToolProviders() {
		ArrayList ietps = new ArrayList(this.imageEditToolProvidersByClassName.values());
		return ((ImageEditToolProvider[]) ietps.toArray(new ImageEditToolProvider[ietps.size()]));
	}
	
	//	register and lookup method for image edit tool providers
	private HashMap imageMarkupToolProvidersByClassName = new LinkedHashMap();
	
	private void registerImageMarkupToolProvider(ImageMarkupToolProvider imtp) {
		if (imtp != null)
			this.imageMarkupToolProvidersByClassName.put(imtp.getClass().getName(), imtp);
	}
	
	/**
	 * Get all image markup tool providers that are currently available.
	 * @return an array holding all image markup tool providers registered
	 */
	public ImageMarkupToolProvider[] getImageMarkupToolProviders() {
		ArrayList imtps = new ArrayList(this.imageMarkupToolProvidersByClassName.values());
		return ((ImageMarkupToolProvider[]) imtps.toArray(new ImageMarkupToolProvider[imtps.size()]));
	}
	
	/**
	 * Find an image markup tool provider by its class name.
	 * @param pluginClassName the class name of the desired image markup tool provider
	 * @return the image markup tool provider with the specified class name
	 */
	public ImageMarkupToolProvider getImageMarkupToolProvider(String pluginClassName) {
		return ((ImageMarkupToolProvider) this.imageMarkupToolProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Retrieve an image markup tool by its name. The name may be fully
	 * qualified, i.e., include the providerClassName, but need not. In the
	 * latter case, all image markup tool providers will be asked for an image
	 * markup tool with the specified name, and the first one found will be
	 * returned.
	 * @param name the name of the image markup tool
	 * @return the image markup tool with the specified name, or null, if there
	 *         is no such image markup tool
	 */
	public ImageMarkupTool getImageMarkupToolForName(String name) {
		int nameSplit = ((name == null) ? -1 : name.indexOf('@'));
		if ((nameSplit == -1) || ((nameSplit + 1) == name.length()))
			return this.getImageMarkupToolForName(name, null);
		else return this.getImageMarkupToolForName(name.substring(0, nameSplit), name.substring(nameSplit + 1));
	}
	
	/**
	 * Retrieve an image markup tool by its name. The providerClassName may be
	 * null. In this latter case, all image markup tool providers will be asked
	 * for an image markup tool with the specified name, and the first one found
	 * will be returned.
	 * @param name the name of the image markup tool
	 * @param providerClassName the class name of the desired image markup tool
	 *            provider to ask for the image markup tool
	 * @return the image markup tool with the specified name, or null, if there
	 *         is no such image markup tool
	 */
	public ImageMarkupTool getImageMarkupToolForName(String name, String providerClassName) {
		if (providerClassName == null) {
			ImageMarkupToolProvider[] imtps = this.getImageMarkupToolProviders();
			for (int p = 0; p < imtps.length; p++) {
				ImageMarkupTool imt = imtps[p].getImageMarkupTool(name);
				if (imt != null) return imt;
			}
			return null;
		}
		else {
			ImageMarkupToolProvider imtp = this.getImageMarkupToolProvider(providerClassName);
			return ((imtp == null) ? null : imtp.getImageMarkupTool(name));
		}
	}
	
	//	register and lookup method for selection action providers
	private HashMap selectionActionProvidersByClassName = new LinkedHashMap();
	
	private void registerSelectionActionProvider(SelectionActionProvider sap) {
		if (sap != null)
			this.selectionActionProvidersByClassName.put(sap.getClass().getName(), sap);
	}
	
	/**
	 * Find a GoldenGatePlugin by its class name.
	 * @param pluginClassName the class name of the desired GoldenGatePlugin
	 * @return the GoldenGatePlugin with the specified class name
	 */
	public SelectionActionProvider getSelectionActionProvider(String pluginClassName) {
		return ((SelectionActionProvider) this.selectionActionProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all GoldenGatePlugins that are currently available.
	 * @return an array holding all GoldenGatePlugins registered
	 */
	public SelectionActionProvider[] getSelectionActionProviders() {
		ArrayList saps = new ArrayList(this.selectionActionProvidersByClassName.values());
		return ((SelectionActionProvider[]) saps.toArray(new SelectionActionProvider[saps.size()]));
	}
	
	//	register and lookup method for click action providers
	private HashMap clickActionProvidersByClassName = new LinkedHashMap();
	
	private void registerClickActionProvider(ClickActionProvider cap) {
		if (cap != null)
			this.clickActionProvidersByClassName.put(cap.getClass().getName(), cap);
	}
	
	/**
	 * Find a GoldenGatePlugin by its class name.
	 * @param pluginClassName the class name of the desired GoldenGatePlugin
	 * @return the GoldenGatePlugin with the specified class name
	 */
	public ClickActionProvider getClickActionProvider(String pluginClassName) {
		return ((ClickActionProvider) this.clickActionProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all GoldenGatePlugins that are currently available.
	 * @return an array holding all GoldenGatePlugins registered
	 */
	public ClickActionProvider[] getClickActionProviders() {
		ArrayList caps = new ArrayList(this.clickActionProvidersByClassName.values());
		return ((ClickActionProvider[]) caps.toArray(new ClickActionProvider[caps.size()]));
	}
	
	//	register and lookup method for reaction providers
	private HashMap reactionProvidersByClassName = new LinkedHashMap();
	
	private void registerReactionProvider(ReactionProvider rp) {
		if (rp != null)
			this.reactionProvidersByClassName.put(rp.getClass().getName(), rp);
	}
	
	/**
	 * Find a reaction provider by its class name.
	 * @param pluginClassName the class name of the desired reaction provider
	 * @return the reaction provider with the specified class name
	 */
	public ReactionProvider getReactionProvider(String pluginClassName) {
		return ((ReactionProvider) this.reactionProvidersByClassName.get(pluginClassName));
	}
	
	/**
	 * Get all reaction providers that are currently available.
	 * @return an array holding all reaction providers registered
	 */
	public ReactionProvider[] getReactionProviders() {
		ArrayList rps = new ArrayList(this.reactionProvidersByClassName.values());
		return ((ReactionProvider[]) rps.toArray(new ReactionProvider[rps.size()]));
	}
	
	//	register and lookup method for display extension providers
	private ArrayList displayExtensionProviders = new ArrayList();
	
	private void registerDisplayExtensionProvider(DisplayExtensionProvider dep) {
		if (dep != null)
			this.displayExtensionProviders.add(dep);
	}
	
	/**
	 * Get all display extension providers that are currently available.
	 * @return an array holding all registered display extension providers
	 */
	public DisplayExtensionProvider[] getDisplayExtensionProviders() {
		return ((DisplayExtensionProvider[]) this.displayExtensionProviders.toArray(new DisplayExtensionProvider[this.displayExtensionProviders.size()]));
	}
	
	//	register for document listeners
	private ArrayList displayExtensionListeners = new ArrayList();
	
	/**
	 * Register a listener for changes to document display extensions.
	 * @param del the listener to register
	 */
	public void addDisplayExtensionListener(DisplayExtensionListener del) {
		if (del != null)
			this.displayExtensionListeners.add(del);
	}
	
	/**
	 * Remove a listener for changes to document display extensions.
	 * @param del the listener to remove
	 */
	public void removeDisplayExtensionListener(DisplayExtensionListener del) {
		if (del != null)
			this.displayExtensionListeners.remove(del);
	}
	
	//	register for document listeners
	private ArrayList documentListeners = new ArrayList();
	
	private void registerDocumentListener(GoldenGateImagineDocumentListener ggidl) {
		if (ggidl != null)
			this.documentListeners.add(ggidl);
	}
	
	/**
	 * Get all document listeners that are currently available. This getter is
	 * mainly intended for applications that prefer to implement their own
	 * notification mechanisms instead of using the ones provided by this class.
	 * @return an array holding all document listeners registered
	 */
	public GoldenGateImagineDocumentListener[] getDocumentListeners() {
		return ((GoldenGateImagineDocumentListener[]) this.documentListeners.toArray(new GoldenGateImagineDocumentListener[this.documentListeners.size()]));
	}
	
	/**
	 * Notify all registered document listeners that an Image Markup document
	 * has been opened in an application built around this GoldenGATE Imagine
	 * core. This method should be called by client code right after an Image
	 * Markup document has been loaded.
	 * @param doc the document that was opened
	 * @param source the source the document was loaded from
	 * @param pm a progress monitor observing post-load processing
	 */
	public void notifyDocumentOpened(ImDocument doc, Object source, ProgressMonitor pm) {
		for (int l = 0; l < this.documentListeners.size(); l++)
			((GoldenGateImagineDocumentListener) this.documentListeners.get(l)).documentOpened(doc, source, pm);
	}
	
	/**
	 * Notify all registered document listeners that an Image Markup document
	 * has been selected for editing in an application built around this
	 * GoldenGATE Imagine core. This method should be called by client code
	 * right after an Image Markup document has been selected (most importantly
	 * after changing between documents in a multi-document UI).
	 * @param doc the document that was selected
	 */
	public void notifyDocumentSelected(ImDocument doc) {
		for (int l = 0; l < this.documentListeners.size(); l++)
			((GoldenGateImagineDocumentListener) this.documentListeners.get(l)).documentSelected(doc);
	}
	
	/**
	 * Notify all registered document listeners that an Image Markup document
	 * is about to be saved to persistent storage in an application built
	 * around this GoldenGATE Imagine core. This method should only be called
	 * if the Image Markup document is stored as such, rather than exported in
	 * another format. 
	 * If an implementor of the <code>documentSaving()</code> method decides to
	 * cancel the saving process, this method throws a
	 * <code>CancelSavingException</code>, with the reason for the cancellation
	 * in the message, e.g. to write it to a log file or display it in a UI.
	 * @param doc the document that is about to be saved
	 * @param dest the destination the document will be saved to
	 * @param pm a progress monitor observing saving preparations
	 * @param throws CancelSavingException any of the registered listeners
	 *            desires to cancel the saving process
	 */
	public void notifyDocumentSaving(ImDocument doc, Object dest, ProgressMonitor pm) throws CancelSavingException {
		for (int l = 0; l < this.documentListeners.size(); l++)
			((GoldenGateImagineDocumentListener) this.documentListeners.get(l)).documentSaving(doc, dest, pm);
	}
	
	/**
	 * Notify all registered document listeners that an Image Markup document
	 * has been saved to persistent storage in an application built around this
	 * GoldenGATE Imagine core. This method should only be called if the Image
	 * Markup document has been stored as such, rather than exported in another
	 * format, and only if the saving process has completed successfully.
	 * @param doc the document that has been saved
	 * @param dest the destination the document was saved to
	 * @param pm a progress monitor observing post-save preparations
	 */
	public void notifyDocumentSaved(ImDocument doc, Object dest, ProgressMonitor pm) {
		for (int l = 0; l < this.documentListeners.size(); l++)
			((GoldenGateImagineDocumentListener) this.documentListeners.get(l)).documentSaved(doc, dest, pm);
	}
	
	/**
	 * Notify all registered document listeners that an Image Markup document
	 * has been closed in an application built around this GoldenGATE Imagine
	 * core. This method should be called by client code right after the
	 * document is disposed.
	 * @param docId the ID of the document that was closed
	 */
	public void notifyDocumentClosed(String docId) {
		for (int l = 0; l < this.documentListeners.size(); l++)
			((GoldenGateImagineDocumentListener) this.documentListeners.get(l)).documentClosed(docId);
		
		//	clean up page image cache
		if (this.pageImageStore != null)
			this.pageImageStore.cleanup(docId);
		
		//	clean up supplement cache folder
		File docSupplementFolder = ((File) this.docSupplementFoldersById.get(docId));
		if ((docSupplementFolder != null) && docSupplementFolder.exists()) try {
			File[] docSupplements = docSupplementFolder.listFiles();
			for (int s = 0; s < docSupplements.length; s++)
				docSupplements[s].delete();
			docSupplementFolder.delete();
		}
		catch (Exception e) {
			System.out.println("Error cleaning up import supplement cache for document '" + docId + "': " + e.getMessage());
			e.printStackTrace(System.out);
		}
	}
	
	//	register for atomic action listeners
	private ArrayList atomicActionListeners = new ArrayList();
	
	private void registerAtomicActionListener(GoldenGateImagineAtomicActionListener ggiaal) {
		if (ggiaal != null)
			this.atomicActionListeners.add(ggiaal);
	}
	
	/**
	 * Get all document listeners that are currently available. This getter is
	 * mainly intended for applications that prefer to implement their own
	 * notification mechanisms instead of using the ones provided by this class.
	 * @return an array holding all document listeners registered
	 */
	public GoldenGateImagineAtomicActionListener[] getAtomicActionListeners() {
		return ((GoldenGateImagineAtomicActionListener[]) this.atomicActionListeners.toArray(new GoldenGateImagineAtomicActionListener[this.atomicActionListeners.size()]));
	}
	
	/**
	 * Notify all registered atomic action listeners that an atomic action is
	 * starting on an Image Document markup panel in a UI application built
	 * around this GoldenGATE Imagine core. This method should be called by
	 * client UI code right after an atomic action has been started on an Image
	 * Image Document markup panel.
	 * @param id the unique ID of the started action
	 * @param label the label of the action
	 * @param imt the Image Markup Tool performing the action
	 * @param annot the annotation being processed
	 * @param idmp the document editor panel the atomic action is starting on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public void notifyAtomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
		for (int l = 0; l < this.atomicActionListeners.size(); l++)
			((GoldenGateImagineAtomicActionListener) this.atomicActionListeners.get(l)).atomicActionStarted(id, label, imt, annot, idmp, pm);
	}
	/**
	 * Notify all registered atomic action listeners that the running atomic
	 * action is finishing on an Image Document markup panel in a UI
	 * application built around this GoldenGATE Imagine core. This method
	 * should be called by client UI code when an atomic action is finishing
	 * on an Image Document markup panel.
	 * @param id the unique ID of the finishing action
	 * @param idmp the document editor panel the atomic action is finishing on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public void notifyAtomicActionFinishing(long id, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
		for (int l = 0; l < this.atomicActionListeners.size(); l++)
			((GoldenGateImagineAtomicActionListener) this.atomicActionListeners.get(l)).atomicActionFinishing(id, idmp, pm);
	}
	
	/**
	 * Notify all registered atomic action listeners that the running atomic
	 * action has finished on an Image Document markup panel in a UI
	 * application built around this GoldenGATE Imagine core. This method
	 * should be called by client UI code when an atomic action has finished
	 * on an Image Document markup panel.
	 * @param id the unique ID of the finished action
	 * @param idmp the document editor panel the atomic action was finished on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public void notifyAtomicActionFinished(long id, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
		for (int l = 0; l < this.atomicActionListeners.size(); l++)
			((GoldenGateImagineAtomicActionListener) this.atomicActionListeners.get(l)).atomicActionFinished(id, idmp, pm);
	}
	
	/**
	 * Issue a notification of a change to the display extensions in a given
	 * document markup panel. Use a null argument to indicate a change that
	 * affects all document markup panels current in use.
	 * @param idmp the document markup panel affected by the change
	 */
	public void notifyDisplayExtensionsModified(ImDocumentMarkupPanel idmp) {
		for (int l = 0; l < this.displayExtensionListeners.size(); l++) try {
			((DisplayExtensionListener) this.displayExtensionListeners.get(l)).displayExtensionsModified(idmp);
		}
		catch (Exception e) {
			System.out.println("Error issuing display extension change notification: " + e.getMessage());
			e.printStackTrace(System.out);
		}
	}
	
	/**
	 * Shut down the GoldenGATE Imagine instance.
	 * @param pm a progress monitor receiving information on the shutdown
	 *            process
	 */
	public void exit(ProgressMonitor pm) {
		
		//	gather and store settings
		if (this.configuration.isDataEditable()) {
			String[] dpNames = UserInterfaceUtils.getDisplayPropertyNames();
			for (int n = 0; n < dpNames.length; n++) {
//				if (dpNames[n].startsWith("annot.") && dpNames[n].endsWith(".color"))
//					continue; // annotation colors are stored in core
//				else if (dpNames[n].startsWith("core."))
//					continue; // something else labeled as core setting
				if (GoldenGATE.isCoreDisplayProperty(dpNames[n]))
					continue;
				this.settings.setSetting(dpNames[n], UserInterfaceUtils.encodeDisplayProperty(dpNames[n]));
			}
			try {
				this.goldenGate.storeApplicationSettings("GgImagine.cnfg", this.settings);
			}
			catch (IOException ioe) {
				System.out.println("Failed to store central GoldenGATE Imagine settings; " + ioe.getMessage());
				ioe.printStackTrace(System.out);
			}
		}
		
		//	shut down underlying GoldenGATE core
		this.goldenGate.exit(pm);
		if (this.pdfExtractor != null)
			this.pdfExtractor.shutdown();
		if (this.pageImageStore != null)
			PageImage.removePageImageStore(this.pageImageStore);
	}
//	
//	/**
//	 * Retrieve the current status of local application settings for
//	 * GoldenGATE Editor, i.e., the contents of the local version of
//	 * <code>GgImagine.cnfg</code> if the application was to exit right now.
//	 * Because this method is exclusively intended for exporting user
//	 * configurations, it only works in local master mode.
//	 * @return the contents of the local version of the settings file
//	 */
//	public Settings getLocalApplicationSettings() {
//		if (!this.configuration.isMasterConfiguration())
//			throw new IllegalStateException("Local settings can be accessed only in local master mode");
//		Settings set = new Settings();
//		String[] dpNames = UserInterfaceUtils.getDisplayPropertyNames();
//		for (int n = 0; n < dpNames.length; n++) {
//			if (dpNames[n].startsWith("annot.") && dpNames[n].endsWith(".color"))
//				continue; // annotation colors are stored in core
//			else if (dpNames[n].startsWith("core."))
//				continue; // something else labeled as core setting
//			set.setSetting(dpNames[n], UserInterfaceUtils.encodeDisplayProperty(dpNames[n]));
//		}
//		return this.goldenGate.getLocalApplicationSettings("GgImagine.cnfg", set);
//	}
	/*
DO NOT DO THIS, using local settings makes preciously little sense (display preferences of exporting user ... might be color blind or like their dark mode or something)
- instead, add support for 'GgImagine.<configName>.cnfg' (to be held in data folder of configuration manager) ...
- ... as well as for export dedicated 'GgImagine.cnfg' (to be held in data folder of configuration manager)
==> 'GgImagine.cnfg' and 'GgImagine.local.cnfg' still belong to installation (use might be color blind or like their dark mode or something) ...
==> ... so those local settings need to prevail (and do, with new defaulting approach)
==> 'GgImagine.cnfg' from configuration provides means to inject configuration specific additions over installed defaults ...
==> ... e.g. special annotation types or attribute suggestions
	 */
	
	/**
	 * Create an instance of the GoldenGATE Imagine core with a specific
	 * configuration.
	 * @param path the base path of the GoldenGATE Imagine installation
	 * @param configuration the GoldenGateConfiguration to use
	 * @param pm a progress monitor receiving information on the startup
	 *            process
	 * @return a new GoldenGATE Imagine instance to work with the specified
	 *         configuration
	 */
	public static synchronized GoldenGateImagine openGoldenGATE(File path, GoldenGateConfiguration configuration, ProgressMonitor pm) throws IOException {
		return new GoldenGateImagine(configuration, GoldenGATE.openGoldenGATE(path, configuration, pm), path);
	}
	
	/**
	 * Create an instance of the GoldenGATE Imagine core with a GoldenGATE core
	 * wrapping a specific configuration.
	 * @param path the base path of the GoldenGATE Imagine installation
	 * @param goldenGate the GoldenGATE core to use
	 * @return a new GoldenGATE Imagine instance to work with the specified
	 *         configuration
	 */
	public static synchronized GoldenGateImagine openGoldenGATE(File path, GoldenGATE goldenGate) throws IOException {
		return new GoldenGateImagine(goldenGate.getConfiguration(), goldenGate, path);
	}
}