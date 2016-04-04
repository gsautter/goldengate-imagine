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
package de.uka.ipd.idaho.im.imagine.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.gamta.util.imaging.DocumentStyle;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.FileConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.UrlConfiguration;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagineConstants;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.util.ImDocumentData.ImDocumentEntry;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Batch runner utility for GoldenGATE Imagine. This command line tool fully
 * automatically converts PDF documents into IMF documents, optionally running
 * a series of Image Markup tools in between. This fully automated conversion
 * tool works best if document style templates exist for the PDF documents to
 * process.
 * 
 * @author sautter
 */
public class GoldenGateImagineBatch implements GoldenGateImagineConstants {
	private static final String CACHE_PATH_PARAMETER = "CACHE";
	private static final String DATA_PARAMETER = "DATA";
	private static final String DATA_TYPE_PARAMETER = "DT";
	private static final String OUT_PARAMETER = "OUT";
	private static final String OUT_TYPE_PARAMETER = "OT";
	private static final String HELP_PARAMETER = "HELP";
	
	private static File BASE_PATH = null;
	
	private static Settings PARAMETERS = new Settings();
	
	private static final String LOG_TIMESTAMP_DATE_FORMAT = "yyyyMMdd-HHmm";
	private static final DateFormat LOG_TIMESTAMP_FORMATTER = new SimpleDateFormat(LOG_TIMESTAMP_DATE_FORMAT);
	
	/**	the main method to run GoldenGATE Imagine as a batch application
	 */
	public static void main(String[] args) throws Exception {
		
		//	adjust basic parameters
		String basePath = "./";
		String logFileName = ("GgImagineBatch." + LOG_TIMESTAMP_FORMATTER.format(new Date()) + ".log");
		String cacheRootPath = null;
		String dataBaseName = null;
		String dataType = "G";
		String dataOutPath = null;
		String dataOutType = "F";
		boolean printHelpImplicit = true;
		boolean printHelpExplicit = false;
		
		//	parse remaining args
		for (int a = 0; a < args.length; a++) {
			if (args[a] == null)
				continue;
			if (args[a].startsWith(BASE_PATH_PARAMETER + "="))
				basePath = args[a].substring((BASE_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(CACHE_PATH_PARAMETER + "="))
				cacheRootPath = args[a].substring((CACHE_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(DATA_PARAMETER + "=")) {
				dataBaseName = args[a].substring((DATA_PARAMETER + "=").length());
				printHelpImplicit = false;
			}
			else if (args[a].equals(HELP_PARAMETER)) {
				printHelpExplicit = true;
				break;
			}
			else if (args[a].startsWith(DATA_TYPE_PARAMETER + "="))
				dataType = args[a].substring((DATA_TYPE_PARAMETER + "=").length());
			else if (args[a].startsWith(OUT_PARAMETER + "="))
				dataOutPath = args[a].substring((OUT_PARAMETER + "=").length());
			else if (args[a].startsWith(OUT_TYPE_PARAMETER + "="))
				dataOutType = args[a].substring((OUT_TYPE_PARAMETER + "=").length());
			else if (args[a].equals(LOG_PARAMETER + "=IDE") || args[a].equals(LOG_PARAMETER + "=NO"))
				logFileName = null;
			else if (args[a].startsWith(LOG_PARAMETER + "="))
				logFileName = args[a].substring((LOG_PARAMETER + "=").length());
		}
		
		//	print help and exit if asked to
		if (printHelpExplicit || printHelpImplicit) {
			System.out.println("GoldenGATE Imagine Batch can take the following parameters:");
			System.out.println("");
			System.out.println("PATH:\tthe folder to run GoldenGATE Imagine Batch in (defaults to the\r\n\tinstallation folder)");
			System.out.println("CACHE:\tthe root folder for all data caching folders (defaults to the path\r\n\tfolder, useful for directing caching to a RAM disc, etc.)");
			System.out.println("DATA:\tthe PDF files to process:");
			System.out.println("\t- set to PDF file path and name to process that file");
			System.out.println("\t- set to folder path and name to process all PDF files in that folder");
			System.out.println("\t- set to TXT file to process all PDF files listed in that file");
			System.out.println("DT:\tthe type of the PDF files to process (defaults to 'G' for 'generic'):");
			System.out.println("\t- set to 'D' or 'BD' to indicate born-digital PDF files");
			System.out.println("\t- set to 'S' to indicate scanned PDF files");
			System.out.println("\t- set to 'G' or omit to indicate generic PDF files (expects both\r\n\t  born-digital and scanned, determining type on a per-file basis)");
			System.out.println("OUT:\tthe folder to store the produced IMF files in (defaults to the folder\r\n\teach individual source PDF file was loaded from)");
			System.out.println("OT:\tthe way of storing the produced IMF files (defaults to 'F' for 'file'):");
			System.out.println("\t- set to 'F' or omit to indicate (zipped) single file storage");
			System.out.println("\t- set to 'D' to indicate indicate (non-zipped) folder storage");
			System.out.println("LOG:\tthe name for the log files to write respective information to (file\r\n\tnames are suffixed with '.out.log' and '.err.log', set to 'IDE' or 'NO'\r\n\tto log directly to the console)");
			System.out.println("HELP:\tprint this help text");
			System.out.println("");
			System.out.println("The file 'GgImagineBatch.cnfg' specifies what to do to PDF documents after\r\ndecoding:");
			System.out.println("- imageMarkupTools: a space separated list of the Image Markup Tools to run");
			System.out.println("- configName: the name of the GoldenGATE Imagine configuration to load the\r\n  Image Markup Tools from");
			System.exit(0);
		}
		
		//	get list of files to process (either all PDFs in some folder, or the ones listed in some TXT file, or some already-decoded files)
		File[] dataInFiles = null;
		
		//	folder to process
		File dataInBase = new File(dataBaseName);
		if (dataInBase.isDirectory()) {
			dataInFiles = dataInBase.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return (file.isFile() && file.getName().toLowerCase().endsWith(".pdf"));
				}
			});
		}
		else if (dataInBase.getName().toLowerCase().endsWith(".pdf")) {
			dataInFiles = new File[1];
			dataInFiles[0] = dataInBase;
		}
		else if (dataInBase.getName().toLowerCase().endsWith(".txt")) {
			StringVector dataInNames = StringVector.loadList(dataInBase);
			ArrayList dataInFileList = new ArrayList();
			for (int d = 0; d < dataInNames.size(); d++) {
				File dataInFile = new File(dataInNames.get(d));
				if (dataInFile.isDirectory())
					dataInFileList.addAll(Arrays.asList(dataInFile.listFiles(new FileFilter() {
						public boolean accept(File file) {
							return (file.isFile() && file.getName().toLowerCase().endsWith(".pdf"));
						}
					})));
				else if (dataInFile.getName().toLowerCase().endsWith(".pdf"))
					dataInFileList.add(dataInFile);
			}
			dataInFiles = ((File[]) dataInFileList.toArray(new File[dataInFileList.size()]));
		}
		
		//	anything to work on?
		if ((dataInFiles == null) || (dataInFiles.length == 0)) {
			System.out.println("No data specified to work with, use 'DATA' parameter:");
			System.out.println("- set to PDF file name: process that file");
			System.out.println("- set to folder name: process all PDF files in that folder");
			System.out.println("- set to TXT file: process all PDF files listed in there");
			System.exit(0);
		}
		
		//	remember program base path
		BASE_PATH = new File(basePath);
		
		//	load parameters
		System.out.println("Loading parameters");
		try {
			StringVector parameters = StringVector.loadList(new File(BASE_PATH, PARAMETER_FILE_NAME));
			for (int p = 0; p < parameters.size(); p++) try {
				String param = parameters.get(p);
				int split = param.indexOf('=');
				if (split != -1) {
					String key = param.substring(0, split).trim();
					String value = param.substring(split + 1).trim();
					if ((key.length() != 0) && (value.length() != 0))
						PARAMETERS.setSetting(key, value);
				}
			} catch (Exception e) {}
		} catch (Exception e) {}
		
		//	configure web access
		if (PARAMETERS.containsKey(PROXY_NAME)) {
			System.getProperties().put("proxySet", "true");
			System.getProperties().put("proxyHost", PARAMETERS.getSetting(PROXY_NAME));
			if (PARAMETERS.containsKey(PROXY_PORT))
				System.getProperties().put("proxyPort", PARAMETERS.getSetting(PROXY_PORT));
			
			if (PARAMETERS.containsKey(PROXY_USER) && PARAMETERS.containsKey(PROXY_PWD)) {
				//	initialize proxy authentication
			}
		}
		
		//	preserve original System.out and write major steps there
		final PrintStream systemOut = new PrintStream(System.out) {
			public void println(String str) {
				super.println(str);
				if (System.out != this.out)
					System.out.println(str);
			}
		};
		
		//	create log files if required
		File logFolder = null;
		File logFileOut = null;
		File logFileErr = null;
		if (logFileName != null) try {
			
			//	truncate log file extension
			if (logFileName.endsWith(".log"))
				logFileName = logFileName.substring(0, (logFileName.length() - ".log".length()));
			
			//	create absolute log files
			if (logFileName.startsWith("/") || (logFileName.indexOf(':') != -1)) {
				logFileOut = new File(logFileName + ".out.log");
				logFileErr = new File(logFileName + ".err.log");
				logFolder = logFileOut.getAbsoluteFile().getParentFile();
			}
			
			//	create relative log files (the usual case)
			else {
				
				//	get log path
				String logFolderName = PARAMETERS.getSetting(LOG_PATH, LOG_FOLDER_NAME);
				if (logFolderName.startsWith("/") || (logFolderName.indexOf(':') != -1))
					logFolder = new File(logFolderName);
				else logFolder = new File(BASE_PATH, logFolderName);
				logFolder = logFolder.getAbsoluteFile();
				logFolder.mkdirs();
				
				//	create log files
				logFileOut = new File(logFolder, (logFileName + ".out.log"));
				logFileErr = new File(logFolder, (logFileName + ".err.log"));
			}
			
			//	redirect System.out
			logFileOut.getAbsoluteFile().getParentFile().mkdirs();
			logFileOut.createNewFile();
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFileOut)), true, "UTF-8"));
			
			//	redirect System.err
			logFileErr.getAbsoluteFile().getParentFile().mkdirs();
			logFileErr.createNewFile();
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFileErr)), true, "UTF-8"));
		}
		catch (Exception e) {
			systemOut.println("Could not create log files in folder '" + logFolder.getAbsolutePath() + "':" + e.getMessage());
			e.printStackTrace(systemOut);
		}
		
		//	load GoldenGATE Imagine specific settings
		final Settings ggiSettings = Settings.loadSettings(new File(BASE_PATH, "GgImagineBatch.cnfg"));
		
		//	get list of image markup tools to run
		String imtNameString = ggiSettings.getSetting("imageMarkupTools");
		if (imtNameString == null) {
			systemOut.println("No Image Markup Tools configured to run, check entry" +
					"\r\n'imageMarkupTools' in GgImagineBatch.cnfg");
			System.exit(0);
		}
		String[] imtNames = imtNameString.split("\\s+");
		
		//	get exporters to use
		String exporterNames = ggiSettings.getSetting("documentExporters");
		
		//	use configuration specified in settings (default to 'Default.imagine' for now)
		String ggiConfigName = ggiSettings.getSetting("configName");
		
		//	open GoldenGATE Imagine window
		GoldenGateConfiguration ggiConfig = null;
		
		//	local master configuration selected
		if (ggiConfigName == null)
			ggiConfig = new FileConfiguration("Local Master Configuration", BASE_PATH, true, true, null);
		
		//	other local configuration selected
		else if (ggiConfigName.startsWith("http://"))
			ggiConfig = new UrlConfiguration(ggiConfigName);
		
		//	remote configuration selected
		else ggiConfig = new FileConfiguration(ggiConfigName, new File(new File(BASE_PATH, CONFIG_FOLDER_NAME), ggiConfigName), false, true, null);
		
		//	if cache path set, add settings for page image and supplement cache
		if (cacheRootPath != null) {
			if (!cacheRootPath.endsWith("/"))
				cacheRootPath += "/";
			Settings set = ggiConfig.getSettings();
			set.setSetting("cacheRootFolder", cacheRootPath);
			set.setSetting("pageImageFolder", (cacheRootPath + "PageImages"));
			set.setSetting("supplementFolder", (cacheRootPath + "Supplements"));
		}
		
		//	instantiate GoldenGATE Imagine
		GoldenGateImagine goldenGateImagine = GoldenGateImagine.openGoldenGATE(ggiConfig, BASE_PATH, false);
		systemOut.println("GoldenGATE Imagine core created, configuration is " + ggiConfigName);
		
		//	get individual image markup tools
		ImageMarkupTool[] imts = new ImageMarkupTool[imtNames.length];
		for (int t = 0; t < imtNames.length; t++) {
			imts[t] = goldenGateImagine.getImageMarkupToolForName(imtNames[t]);
			if (imts[t] == null) {
				systemOut.println("Image Markup Tool '" + imtNames[t] + "' not found," +
						"\r\ncheck entry 'imageMarkupTools' in GgImagineBatch.cnfg");
				System.exit(0);
			}
			else systemOut.println("Image Markup Tool '" + imtNames[t] + "' loaded");
		}
		
		//	get document exporters for additional output
		ImageDocumentFileExporter[] idfes = getFileExporters(ggiConfig.getPlugins(), exporterNames);
		
		//	create progress monitor forking steps to console
		ProgressMonitor pm = new ProgressMonitor() {
			public void setStep(String step) {
				systemOut.println(step);
			}
			public void setInfo(String info) {
				System.out.println(info);
			}
			public void setBaseProgress(int baseProgress) {}
			public void setMaxProgress(int maxProgress) {}
			public void setProgress(int progress) {}
		};
		
		//	get PDF converter
		PdfExtractor pdfExtractor = goldenGateImagine.getPdfExtractor();
		
		//	process files
		for (int d = 0; d < dataInFiles.length; d++) try {
			
			//	determine where to store document
			String dataOutName = (dataInFiles[d].getName() + ("D".equals(dataOutType) ? ".imd" : ".imf"));
			File dataOutFile;
			if (dataOutPath == null)
				dataOutFile = new File(dataInFiles[d].getAbsoluteFile().getParentFile(), dataOutName);
			else dataOutFile = new File(dataOutPath, dataOutName);
			
			//	we've processed this one before
			if (dataOutFile.exists()) {
				systemOut.println("Document '" + dataInFiles[d].getAbsolutePath() + "' processed before, skipping");
				continue;
			}
			else systemOut.println("Processing document '" + dataInFiles[d].getAbsolutePath() + "'");
			
			//	load PDF bytes
			InputStream in = new BufferedInputStream(new FileInputStream(dataInFiles[d]));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer, 0, buffer.length)) != -1)
				baos.write(buffer, 0, read);
			in.close();
			systemOut.println(" - loaded PDF of " + baos.size() + " bytes");
			
			//	convert PDF
			ImDocument doc;
			if ("D".equalsIgnoreCase(dataType) || "BD".equalsIgnoreCase(dataType) || "T".equalsIgnoreCase(dataType))
				doc = pdfExtractor.loadTextPdf(baos.toByteArray(), pm);
			else if ("S".equalsIgnoreCase(dataType))
				doc = pdfExtractor.loadImagePdf(baos.toByteArray(), true, pm);
			else doc = pdfExtractor.loadGenericPdf(baos.toByteArray(), pm);
			systemOut.println(" - PDF converted, document ID is '" + doc.docId + "'");
			
			//	add document name
			doc.setAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, dataInFiles[d].getName());
			
			//	test if document style detected
			if (DocumentStyle.getStyleFor(doc) == null) {
				systemOut.println(" - unable to assign document style");
				continue;
			}
			else systemOut.println(" - assigned document style '" + ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE)) + "'");
			
			//	process document
			for (int t = 0; t < imts.length; t++) {
				systemOut.println("Running Image Markup Tool '" + imts[t].getLabel() + "'");
				imts[t].process(doc, null, null, pm);
			}
			
			//	store document to directory ...
			if ("D".equals(dataOutType)) {
				dataOutFile.getAbsoluteFile().getParentFile().mkdirs();
				File dataOutFolder = new File(dataOutFile.getAbsolutePath() + "ir");
				if (!dataOutFolder.exists())
					dataOutFolder.mkdirs();
				systemOut.println("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
				ImDocumentEntry[] entries = ImDocumentIO.storeDocument(doc, dataOutFile, pm);
				systemOut.println("Document entries stored");
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataOutFile), "UTF-8"));
				for (int e = 0; e < entries.length; e++) {
					out.write(entries[e].toTabString());
					out.newLine();
				}
				out.flush();
				out.close();
				systemOut.println("Document stored");
			}
			
			//	... or file
			else {
				dataOutFile.getAbsoluteFile().getParentFile().mkdirs();
				OutputStream out = new BufferedOutputStream(new FileOutputStream(dataOutFile));
				systemOut.println("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
				ImDocumentIO.storeDocument(doc, out, pm);
				out.flush();
				out.close();
				systemOut.println("Document stored");
			}
			
			//	export additional data formats
			for (int e = 0; e < idfes.length; e++) try {
				idfes[e].exportDocument(doc, dataOutFile, pm);
			}
			
			//	don't let any additional export error disturb main process
			catch (Throwable t) {
				systemOut.println("Error exporting document '" + dataInFiles[d].getAbsolutePath() + "' via '" + idfes[e].getExportMenuLabel() + "': " + t.getMessage());
				t.printStackTrace(systemOut);
			}
		}
		
		//	catch and log whatever might go wrong
		catch (Throwable t) {
			systemOut.println("Error processing document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
			t.printStackTrace(systemOut);
		}
		
		//	clean up, error or not
		finally {
			
			//	if cache root set, clean up cache (fast but small RAM discs will run out of space quickly otherwise)
			if (cacheRootPath != null)
				cleanCacheFolder(new File(cacheRootPath));
			
			//	garbage collect whatever is left
			System.gc();
		}
		
		//	shut down whatever threads are left
		System.exit(0);
	}
	
	private static void cleanCacheFolder(File folder) {
		File[] folderContent = folder.listFiles();
		for (int c = 0; c < folderContent.length; c++) try {
			if (folderContent[c].isDirectory())
				cleanCacheFolder(folderContent[c]);
			else folderContent[c].delete();
		}
		catch (Throwable t) {
			System.out.println("Error cleaning up cached file '" + folderContent[c].getAbsolutePath() + "': " + t.getMessage());
			t.printStackTrace(System.out);
		}
	}
	
	private static ImageDocumentFileExporter[] getFileExporters(GoldenGatePlugin[] ggPlugins, String exporterClassNames) {
		ArrayList idfeList = new ArrayList();
		for (int p = 0; p < ggPlugins.length; p++)
			if (ggPlugins[p] instanceof ImageDocumentFileExporter) {
				if (exporterClassNames == null)
					idfeList.add(ggPlugins[p]);
				else {
					String exporterClassName = ggPlugins[p].getClass().getName();
					exporterClassName = exporterClassName.substring(exporterClassName.lastIndexOf('.') + 1);
					if (exporterClassNames.indexOf(exporterClassName) != -1)
						idfeList.add(ggPlugins[p]);
				}
			}
		return ((ImageDocumentFileExporter[]) idfeList.toArray(new ImageDocumentFileExporter[idfeList.size()]));
	}
}