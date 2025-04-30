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
package de.uka.ipd.idaho.im.imagine.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.ParallelJobRunner;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.applications.ApplicationRuntimeUtils;
import de.uka.ipd.idaho.goldenGate.applications.ApplicationRuntimeUtils.ConsoleApplicationINterface;
import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationRuntimeUtils;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagineConstants;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.CustomFontDecoderCharset;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.FontDecoderCharset;
import de.uka.ipd.idaho.im.util.ImDocumentData;
import de.uka.ipd.idaho.im.util.ImDocumentData.DataBackedImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentData.FolderImDocumentData;
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
	private static final String CONFIG_PATH_PARAMETER = "CONF";
	private static final String CACHE_PATH_PARAMETER = "CACHE";
	private static final String DATA_PARAMETER = "DATA";
	private static final String DATA_TYPE_PARAMETER = "DT";
	private static final String FONT_MODE_PARAMETER = "FM";
	private static final String FONT_CHARSET_PARAMETER = "CS";
	private static final String FONT_CHARSET_PATH_PARAMETER = "CP";
	private static final String OUT_PARAMETER = "OUT";
	private static final String OUT_TYPE_PARAMETER = "OT";
	private static final String HELP_PARAMETER = "HELP";
	private static final String SINGLE_THREAD_PARAMETER = "ST";
	private static final String VERBOSE_CONSOLE_PARAMETER = "VC";
	
	private static final String LOG_TIMESTAMP_DATE_FORMAT = "yyyyMMdd-HHmm";
	private static final DateFormat LOG_TIMESTAMP_FORMATTER = new SimpleDateFormat(LOG_TIMESTAMP_DATE_FORMAT);
	
	public static void main(String[] args) throws Exception {
		if ("GgImagine".equals(System.getProperty("gg." + APPLICATION_FAMILY_NAME_APPLICATION_PROPERTY)))
			mainApplication(args); // got application family name from starter or IDE, good to go
		else mainStarter(args); // loop through to starter routine otherwise
	}
	
	private static void mainStarter(String[] args) throws Exception {
		ApplicationRuntimeUtils.startApplication(new File("."), "GgImagine", "GgImagineBatch.ggApp.cnfg", args, false, true);
	}
	
	/**	the main method to run GoldenGATE Imagine as a batch application
	 */
	private static void mainApplication(String[] args) throws Exception {
		
		//	adjust basic parameters
		boolean online = false;
		String basePath = "./";
		String logFileName = ("GgImagineBatch." + LOG_TIMESTAMP_FORMATTER.format(new Date()) + ".log");
		String ggiConfigPath = "GgImagineBatch.cnfg";
		String fontMode = "U";
		String fontCharSet = "S";
		String fontCharSetPath = null;
		String cacheRootPath = null;
		String dataBaseName = null;
		String dataType = "G";
		String dataOutPath = null;
		String dataOutType = "F";
		boolean useSingleThread = false;
		boolean verboseConsoleOutput = false;
		boolean printHelpImplicit = true;
		boolean printHelpExplicit = false;
		
		//	parse remaining args
		for (int a = 0; a < args.length; a++) {
			if (args[a] == null)
				continue;
			if (args[a].startsWith(BASE_PATH_PARAMETER + "="))
				basePath = args[a].substring((BASE_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(CONFIG_PATH_PARAMETER + "="))
				ggiConfigPath = args[a].substring((CONFIG_PATH_PARAMETER + "=").length());
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
			else if (args[a].equals(SINGLE_THREAD_PARAMETER))
				useSingleThread = true;
			else if (args[a].equals(VERBOSE_CONSOLE_PARAMETER))
				verboseConsoleOutput = true;
			else if (ONLINE_PARAMETER.equals(args[a]))
				online = true;
			else if (args[a].startsWith(DATA_TYPE_PARAMETER + "="))
				dataType = args[a].substring((DATA_TYPE_PARAMETER + "=").length());
			else if (args[a].startsWith(FONT_MODE_PARAMETER + "="))
				fontMode = args[a].substring((FONT_MODE_PARAMETER + "=").length());
			else if (args[a].startsWith(FONT_CHARSET_PARAMETER + "="))
				fontCharSet = args[a].substring((FONT_CHARSET_PARAMETER + "=").length());
			else if (args[a].startsWith(FONT_CHARSET_PATH_PARAMETER + "="))
				fontCharSetPath = args[a].substring((FONT_CHARSET_PATH_PARAMETER + "=").length());
			else if (args[a].startsWith(OUT_PARAMETER + "="))
				dataOutPath = args[a].substring((OUT_PARAMETER + "=").length());
			else if (args[a].startsWith(OUT_TYPE_PARAMETER + "="))
				dataOutType = args[a].substring((OUT_TYPE_PARAMETER + "=").length());
			else if (args[a].equals(LOG_PARAMETER + "=DOC"))
				logFileName = "DOC";
			else if (args[a].equals(LOG_PARAMETER + "=IDE") || args[a].equals(LOG_PARAMETER + "=NO"))
				logFileName = null;
			else if (args[a].startsWith(LOG_PARAMETER + "="))
				logFileName = args[a].substring((LOG_PARAMETER + "=").length());
		}
		
		//	set up console interface first thing (we need that for sending help or error messages as well)
		ConsoleApplicationINterface console = (verboseConsoleOutput ? new ConsoleApplicationINterface() : new ConsoleApplicationINterface() {
			public void setInfo(String info) {
				System.out.println(info); // send as normal log file entry in non-verbose mode
			}
		});
		
		//	print help and exit if asked to
		if (printHelpExplicit || printHelpImplicit) {
			console.sendMessage("GoldenGATE Imagine Batch can take the following parameters:");
			console.sendMessage("");
			console.sendMessage("PATH:\tthe folder to run GoldenGATE Imagine Batch in (defaults to the");
			console.sendMessage("\tinstallation folder)");
			console.sendMessage("CONF:\tthe (path and) name of the configuration file to run GoldenGATE");
			console.sendMessage("\tImagine Batch with (defaults to 'GgImagineBatch.cnfg' in the folder");
			console.sendMessage("\tGoldenGATE Imagine Batch is running in)");
			console.sendMessage("CACHE:\tthe root folder for all data caching folders (defaults to the path");
			console.sendMessage("\tfolder, useful for directing caching to a RAM disc, etc.)");
			console.sendMessage("DATA:\tthe PDF files to process:");
			console.sendMessage("\t- set to PDF file path and name to process that file");
			console.sendMessage("\t- set to folder path and name to process all PDF files in that folder");
			console.sendMessage("\t- set to TXT file to process all PDF files listed in that file");
			console.sendMessage("DT:\tthe type of the PDF files to process (defaults to 'G' for 'generic'):");
			console.sendMessage("\t- set to 'D' or 'BD' to indicate born-digital PDF files");
			console.sendMessage("\t- set to 'S' to indicate scanned PDF files");
			console.sendMessage("\t- set to 'G' or omit to indicate generic PDF files (expects both");
			console.sendMessage("\t  born-digital and scanned, determining type on a per-file basis)");
			console.sendMessage("FM:\tthe way of handling embedded fonts (relevant only for 'DT=D' and ");
			console.sendMessage("\t'DT=G'):");
			console.sendMessage("\t- set to D to completely decode embedded fonts");
			console.sendMessage("\t- set to V to decode un-mapped characters from embedded fonts, i.e.,");
			console.sendMessage("\t  ones without a Unicode mapping, and verify existing Unicode mappings");
			console.sendMessage("\t- set to U to decode un-mapped characters from embedded fonts, i.e.,");
			console.sendMessage("\t  ones without a Unicode mapping (the default)");
			console.sendMessage("\t- set to R to only render embedded fonts, but do not decode glyphs");
			console.sendMessage("\t- set to Q for quick mode, using Unicode mapping only");
			console.sendMessage("CS:\tthe char set for decoding embedded fonts (relevant only for 'FM=D' and");
			console.sendMessage("\t'FM=U'):");
			console.sendMessage("\t- set to U to use all of Unicode");
			console.sendMessage("\t- set to S to use Latin characters and scientific symbols only (the");
			console.sendMessage("\t  default)");
			console.sendMessage("\t- set to M to use Latin characters and mathematical symbols only");
			console.sendMessage("\t- set to F to use Full Latin and derived characters only");
			console.sendMessage("\t- set to L to use Extended Latin characters only");
			console.sendMessage("\t- set to B to use Basic Latin characters only");
			console.sendMessage("\t- set to C for custom, using 'CP' parameter to specify path (file or");
			console.sendMessage("\t  URL) to load from, or name of a named charset to load from a provider");
			console.sendMessage("CP:\tthe file or URL to load the charset for embedded font decoding from");
			console.sendMessage("\t(relevant only for 'CS=C', and required then; implies 'CS=C' if 'CS'");
			console.sendMessage("\tparameter omitted); can also be the name of a named charset to resolve");
			console.sendMessage("\tvia some provider (prefix with '@' to indicate such a name)");
			console.sendMessage("OUT:\tthe folder to store the produced IMF files in (defaults to the folder");
			console.sendMessage("\teach individual source PDF file was loaded from)");
			console.sendMessage("OT:\tthe way of storing the produced IMF files (defaults to 'F' for 'file'):");
			console.sendMessage("\t- set to 'F' or omit to indicate (zipped) single file storage");
			console.sendMessage("\t- set to 'D' to indicate indicate (non-zipped) folder storage");
			console.sendMessage("LOG:\tthe name for the log files to write respective information to (file");
			console.sendMessage("\tnames are suffixed with '.out.log' and '.err.log', set to 'IDE' or 'NO'");
			console.sendMessage("\tto log directly to the console, or to DOC to create one log file per");
			console.sendMessage("\tdocument, located next to the IMF)");
			console.sendMessage("ST:\tno value, just add this token to the command to make the batch run on a");
			console.sendMessage("\tsingle core (e.g. if resources required for other simultaneous tasks)");
			console.sendMessage("VC:\tno value, just add this token to the command to make the batch produce");
			console.sendMessage("\tverbose console output");
			console.sendMessage("HELP:\tprint this help text");
			console.sendMessage("");
			console.sendMessage("PDF decoding parameters can also be specified via the configuration files");
			console.sendMessage("'GgImagineBatch.cnfg' (comes with installation) and 'GgImagineBatch.local.cnfg'");
			console.sendMessage("(remains untouched by installation and updates), with the latter amending the");
			console.sendMessage("former.");
			console.sendMessage("The configuration file 'GgImagineBatch.ggApp.cnfg' specifies how to process PDF");
			console.sendMessage("documents after decoding. It can be amended and modified via its supplementary");
			console.sendMessage("counterpart 'GgImagineBatch.ggApp.local.cnfg'");
			console.sendMessage("Documentation inside these configuration files details out individual parameters");
			console.sendMessage("and their possible values.");
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
		else if (dataInBase.getName().toLowerCase().endsWith(".imf")) {
			dataInFiles = new File[1];
			dataInFiles[0] = dataInBase;
		}
		else if (dataInBase.getName().toLowerCase().endsWith(".imd")) {
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
			console.sendMessage("No data specified to work with, use 'DATA' parameter:");
			console.sendMessage("- set to PDF file name: process that file");
			console.sendMessage("- set to IMF file name: process that file");
			console.sendMessage("- set to folder name: process all PDF files in that folder");
			console.sendMessage("- set to TXT file: process all PDF files listed in there");
			System.exit(0);
		}
		
		//	remember program base path
		final File rootFolder = new File(basePath);
		
		//	load application properties
		Properties appProperties = ApplicationRuntimeUtils.loadApplicationProperties(rootFolder);
		
		//	configure web access
//		if (online)
		ApplicationRuntimeUtils.setUpWebAccess(rootFolder, appProperties, false);
		
		//	create log files if required
		if (logFileName != null)
			ApplicationRuntimeUtils.setUpLogFiles(rootFolder, logFileName);
//		
//		//	load GoldenGATE Imagine specific settings
//		//	TODOne load settings via GG core
//		//	TODOne OR BETTER, get this from app configuration
//		File ggiSettingsFile;
//		if (ggiConfigPath.startsWith("/") || (ggiConfigPath.indexOf(":\\") == 1) || (ggiConfigPath.indexOf(":/") == -1))
//			ggiSettingsFile = new File(ggiConfigPath);
//		else ggiSettingsFile = new File(rootFolder, ggiConfigPath);
//		Settings ggiSettings = Settings.loadSettings(ggiSettingsFile);
		
		//	get list of image markup tools to run
//		String imtNameString = ggiSettings.getSetting("imageMarkupTools");
		String imtNameString = appProperties.getProperty("imageMarkupTools");
		if (imtNameString == null) {
			console.sendError("No Image Markup Tools configured to run, check entry");
//			console.sendError("'imageMarkupTools' in GgImagineBatch.cnfg");
			console.sendError("'@imageMarkupTools' in GgImagineBatch.ggApp.local.cnfg");
			System.exit(0);
		}
		String[] imtNames = imtNameString.split("\\s+");
		
		//	get exporters to use
//		String exporterNames = ggiSettings.getSetting("documentExporters");
		String exporterNames = appProperties.getProperty("documentExporters");
		
		//	use configuration specified in settings (default to 'Default.imagine' for now)
//		String ggiConfigName = ggiSettings.getSetting("configName");
		String ggiConfigName = appProperties.getProperty(CONFIGURATION_NAME_APPLICATION_PROPERTY);
		
		//	get GoldenGATE Imagine configuration
		String[] ggiConfigHosts = (online ? ConfigurationRuntimeUtils.getConfigHosts(rootFolder) : new String[0]);
		GoldenGateConfiguration ggiConfig = ConfigurationRuntimeUtils.loadConfiguration(ggiConfigName, ggiConfigHosts, rootFolder, ProgressMonitor.dummy);
		
		//	anything to work with?
		if (ggiConfig == null) {
			console.sendError("Cannot " + ((ggiConfigName == null) ? "work without configuration" : ("find configuration '" + ggiConfigName + "', please check config files")));
			System.exit(0);
		}
		
		//	create GoldenGATE core
		GoldenGATE goldenGate = GoldenGATE.openGoldenGATE(rootFolder, ggiConfig, ProgressMonitor.dummy);
		
		//	check for config file specified cache root
		if (cacheRootPath == null)
//			cacheRootPath = ggiSettings.getSetting("cacheRootFolder");
			cacheRootPath = appProperties.getProperty("cacheRootFolder");
		
		//	folder for temporarily storing documents during batch processing
		File tempDocRootFolder = null;
		
		//	if cache path set, add settings for page image and supplement cache
		if (cacheRootPath != null) {
			
			//	make sure cache path denotes folder
			if (!cacheRootPath.endsWith("/"))
				cacheRootPath += "/";
			
			//	add PDF decoder cache settings (GG core caches settings, so we only need to access them before creating GGI core)
			Settings set = goldenGate.getApplicationSettings("GgImagine.cnfg");
			set.setSetting("cacheRootFolder", cacheRootPath);
			set.setSetting("pageImageFolder", (cacheRootPath + "PageImages"));
			set.setSetting("supplementFolder", (cacheRootPath + "Supplements"));
			
			//	set up temporary document storage for batch crash recovery
			String tempDocRootFolderName = (cacheRootPath + "TempDocs");
			if (tempDocRootFolderName.startsWith("/") || (tempDocRootFolderName.indexOf(':') != -1))
				tempDocRootFolder = new File(tempDocRootFolderName);
			else if (tempDocRootFolderName.startsWith("./"))
				tempDocRootFolder = new File(rootFolder, tempDocRootFolderName.substring("./".length()));
			else tempDocRootFolder = new File(rootFolder, tempDocRootFolderName);
			if (!tempDocRootFolder.exists())
				tempDocRootFolder.mkdirs();
		}
		
		//	instantiate GoldenGATE Imagine
		GoldenGateImagine goldenGateImagine = GoldenGateImagine.openGoldenGATE(rootFolder, goldenGate);
		console.sendMessage("GoldenGATE Imagine core created, configuration is " + ggiConfigName);
		
		//	get individual image markup tools
		ImageMarkupTool[] imts = new ImageMarkupTool[imtNames.length];
		for (int t = 0; t < imtNames.length; t++) {
			imts[t] = goldenGateImagine.getImageMarkupToolForName(imtNames[t]);
			if (imts[t] == null) {
				console.sendError("Image Markup Tool '" + imtNames[t] + "' not found,");
				console.sendError("check entry '@imageMarkupTools' in GgImagineBatch.ggApp.local.cnfg");
				System.exit(0);
			}
			else console.sendMessage("Image Markup Tool '" + imtNames[t] + "' loaded");
		}
		
		//	get document exporters for additional output
		ImageDocumentFileExporter[] idfes = getFileExporters(ggiConfig.getPlugins(), exporterNames);
		
		//	get PDF converter
		PdfExtractor pdfExtractor = goldenGateImagine.getPdfExtractor();
		
		//	load decoder settings
		Settings ggiBatchSettings = goldenGate.getApplicationSettings(ggiConfigPath);
		
		//	initialize storage flags for intermediate results
//		long tempDocStorageFlags = ImDocumentIO.STORAGE_MODE_CSV;
		long tempDocStorageFlags = -1;
		String tempDocStorageFlagStr = ggiBatchSettings.getSetting("tempDocStorageFlags");
		if (tempDocStorageFlagStr != null) try {
			if (tempDocStorageFlagStr.startsWith("0x"))
				tempDocStorageFlagStr = tempDocStorageFlagStr.substring("0x".length());
			tempDocStorageFlags = Long.parseLong(tempDocStorageFlagStr, 16);
		} catch (RuntimeException re) {}
		if (tempDocStorageFlags == -1)
			tempDocStorageFlags = ImDocumentIO.STORAGE_MODE_CSV;
		
		//	load and check char set file if specified
		FontDecoderCharset fontDecoderCharSet = null;
		if ("D".equalsIgnoreCase(dataType) || "BD".equalsIgnoreCase(dataType) || "T".equalsIgnoreCase(dataType)) {
			
			//	default font decoding mode and charset via settings
			if (fontCharSetPath == null)
				fontCharSetPath = ggiBatchSettings.getSetting("fonts.decoding.charsetPath");
			if (fontCharSet == null) {
				if (fontCharSetPath != null)
					fontCharSet = "C"; // use custom charset if we have one
				else fontCharSet = ggiBatchSettings.getSetting("fonts.decoding.charset", "S");
			}
			if (fontMode == null)
				fontMode = ggiBatchSettings.getSetting("fonts.decoding.mode", "U");
			
			//	instantiate font decoder charset
			if ("Q".equals(fontMode))
				fontDecoderCharSet = PdfFontDecoder.NO_DECODING;
			else if ("R".equals(fontMode))
				fontDecoderCharSet = PdfFontDecoder.RENDER_ONLY;
			else if ("U".equals(fontMode) || "V".equals(fontMode) || "D".equals(fontMode)) {
				if ("U".equals(fontCharSet))
					fontDecoderCharSet = PdfFontDecoder.UNICODE;
				else if ("S".equals(fontCharSet))
					fontDecoderCharSet = FontDecoderCharset.union(PdfFontDecoder.LATIN_FULL, PdfFontDecoder.SYMBOLS);
				else if ("M".equals(fontCharSet))
					fontDecoderCharSet = FontDecoderCharset.union(PdfFontDecoder.LATIN_FULL, PdfFontDecoder.MATH);
				else if ("F".equals(fontCharSet))
					fontDecoderCharSet = PdfFontDecoder.LATIN_FULL;
				else if ("L".equals(fontCharSet))
					fontDecoderCharSet = PdfFontDecoder.LATIN;
				else if ("B".equals(fontCharSet))
					fontDecoderCharSet = PdfFontDecoder.LATIN_BASIC;
				else if ("C".equals(fontCharSet)) {
					String charSetName;
					Reader charSetReader;
					if (fontCharSetPath.startsWith("http://") || fontCharSetPath.startsWith("https://")) {
						charSetName = fontCharSetPath.substring(fontCharSetPath.lastIndexOf('/') + "/".length());
						charSetReader = new BufferedReader(new InputStreamReader((new URL(fontCharSetPath)).openStream(), "UTF-8"));
					}
					else if (fontCharSetPath.startsWith("@")) {
						charSetName = fontCharSetPath.substring("@".length()).trim();
						charSetReader = new StringReader(fontCharSetPath); // will be resolved in readCharSet() below
					}
					else {
						File charSetFile = new File(fontCharSetPath);
						if (charSetFile.exists()) {
							charSetName = charSetFile.getName();
							charSetReader = new BufferedReader(new InputStreamReader(new FileInputStream(charSetFile), "UTF-8"));
						}
						else {
							System.out.println("Invalid font decoding charset file '" + fontCharSetPath + "'");
							return;
						}
					}
					fontDecoderCharSet = CustomFontDecoderCharset.readCharSet(charSetName, charSetReader);
					charSetReader.close();
				}
				else fontDecoderCharSet = PdfFontDecoder.UNICODE;
				if ("U".equals(fontMode)) // add "unmapped-only" behavior if requested
					fontDecoderCharSet = FontDecoderCharset.union(fontDecoderCharSet, PdfFontDecoder.DECODE_UNMAPPED); 
				else if ("V".equals(fontMode)) // add "only-verify-mapped" behavior if requested
					fontDecoderCharSet = FontDecoderCharset.union(fontDecoderCharSet, PdfFontDecoder.VERIFY_MAPPED); 
			}
			else fontDecoderCharSet = PdfFontDecoder.LATIN_FULL;
		}
		
		//	switch off multi-threading if requested
		if (useSingleThread)
			ParallelJobRunner.setLinear(true);
		
		//	process files
		PerDocLogger perDocLogger = null;
		for (int d = 0; d < dataInFiles.length; d++) {
			
			//	prepare intermediate caching of document during processing (need to do it up here so we have the folder accessible to cleanup)
			File tempDocFolder = null;
			if (tempDocRootFolder != null) {
				tempDocFolder = new File(tempDocRootFolder, dataInFiles[d].getName());
				if (!tempDocFolder.exists())
					tempDocFolder.mkdirs();
			}
			
			//	trace processing success
			boolean docFullyProcessed = true;
			
			//	convert PDF and batch process document
			try {
				
				//	determine where to store document
				String dataOutName;
				File dataOutFile;
				
				//	request for converting and processing PDF
				if (dataInFiles[d].getName().toLowerCase().endsWith(".pdf")) {
					dataOutName = (dataInFiles[d].getName() + ("D".equals(dataOutType) ? ".imd" : ".imf"));
					if (dataOutPath == null)
						dataOutFile = new File(dataInFiles[d].getAbsoluteFile().getParentFile(), dataOutName);
					else dataOutFile = new File(dataOutPath, dataOutName);
					
					//	we've processed this one before
					if (dataOutFile.exists()) {
						console.sendMessage("Document '" + dataInFiles[d].getAbsolutePath() + "' processed before, skipping");
						continue;
					}
				}
				
				//	request for processing IMF
				else if (dataInFiles[d].getName().toLowerCase().endsWith(".imf")) {
					dataOutName = (dataInFiles[d].getName().substring(0, (dataInFiles[d].getName().length() - ".imf".length())) + ("D".equals(dataOutType) ? ".imd" : ".imf"));
					if (dataOutPath == null)
						dataOutFile = new File(dataInFiles[d].getAbsoluteFile().getParentFile(), dataOutName);
					else dataOutFile = new File(dataOutPath, dataOutName);
				}
				
				//	request for processing IMD
				else if (dataInFiles[d].getName().toLowerCase().endsWith(".imd")) {
					dataOutName = (dataInFiles[d].getName().substring(0, (dataInFiles[d].getName().length() - ".imd".length())) + ("D".equals(dataOutType) ? ".imd" : ".imf"));
					if (dataOutPath == null)
						dataOutFile = new File(dataInFiles[d].getAbsoluteFile().getParentFile(), dataOutName);
					else dataOutFile = new File(dataOutPath, dataOutName);
				}
				
				//	some other file format (that cannot occur with the above logic, but the compiler don't know)
				else {
					console.sendMessage("Unknown input format in document '" + dataInFiles[d].getAbsolutePath() + "', skipping");
					continue;
				}
				
				//	we're processing this one
				console.sendMessage("Processing document '" + dataInFiles[d].getAbsolutePath() + "'");
				
				//	create document specific log files if requested
				if ("DOC".equals(logFileName)) try {
					File logFolder = dataOutFile.getAbsoluteFile().getParentFile();
					perDocLogger = new PerDocLogger(logFolder, dataInFiles[d].getName());
				}
				catch (Exception e) {
					console.sendError("Could not create log files in folder '" + dataOutFile.getAbsoluteFile().getParentFile().getAbsolutePath() + "':" + e.getMessage());
					console.sendError(e);
				}
				
				//	convert input PDF, load IMF
				ImDocument doc = null;
				
				//	check if we have an earlier version cached (batch might have failed at some point)
				if ((tempDocFolder != null) && (new File(tempDocFolder, "entries.txt")).exists()) try {
					doc = ImDocumentIO.loadDocument(tempDocFolder);
					console.sendMessage(" - document restored from previous batch run");
				}
				
				//	don't let a cache lookup get in the way
				catch (Throwable t) {
					console.sendError("Error loading document '" + dataInFiles[d].getAbsolutePath() + "' from cache: " + t.getMessage());
					console.sendError(t);
				}
				
				//	cache miss or error, convert input PDF or load input IMF or IMD
				if (doc == null) {
					
					//	convert PDF
					if (dataInFiles[d].getName().toLowerCase().endsWith(".pdf")) {
						
						//	load PDF bytes
						InputStream in = new BufferedInputStream(new FileInputStream(dataInFiles[d]));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int read;
						while ((read = in.read(buffer, 0, buffer.length)) != -1)
							baos.write(buffer, 0, read);
						in.close();
						console.sendMessage(" - loaded PDF of " + baos.size() + " bytes");
						
						//	convert PDF
						if ("D".equalsIgnoreCase(dataType) || "BD".equalsIgnoreCase(dataType) || "T".equalsIgnoreCase(dataType))
							doc = pdfExtractor.loadTextPdf(baos.toByteArray(), fontDecoderCharSet, console);
						else if ("S".equalsIgnoreCase(dataType))
							doc = pdfExtractor.loadImagePdf(baos.toByteArray(), true, console);
						else doc = pdfExtractor.loadGenericPdf(baos.toByteArray(), console);
						console.sendMessage(" - PDF converted, document ID is '" + doc.docId + "'");
						
						//	add document name
						doc.setAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, dataInFiles[d].getName());
						
						//	cache PDF conversion result
						if (tempDocFolder != null) try {
							console.sendMessage("Storing conversion result to temporary folder");
//							ImDocumentIO.storeDocument(doc, tempDocFolder, console);
							ImDocumentIO.storeDocument(doc, tempDocFolder, tempDocStorageFlags, console);
							console.sendMessage("Document stored to temporary folder");
						}
						
						//	don't let a caching operation get in the way
						catch (Throwable t) {
							console.sendError("Error caching document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
							console.sendError(t);
						}
					}
					
					//	load IMF
					else if (dataInFiles[d].getName().toLowerCase().endsWith(".imf"))
						doc = ImDocumentIO.loadDocument(dataInFiles[d]);
					
					//	load IMD
					else if (dataInFiles[d].getName().toLowerCase().endsWith(".imd"))
						doc = ImDocumentIO.loadDocument(new File(dataInFiles[d].getParentFile(), (dataInFiles[d].getName() + "ir")));
					
					//	some other (yet to implement) format
					else {
						console.sendMessage(" - unknown document format");
						continue;
					}
				}
				
				//	test if document style detected
				if (DocumentStyle.getStyleFor(doc) == null) {
					console.sendMessage(" - unable to assign document style");
					continue;
				}
				else console.sendMessage(" - assigned document style '" + ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE)) + "'");
				
				//	notify listeners
				goldenGateImagine.notifyDocumentOpened(doc, dataInFiles[d], console);
				
				//	keep track of which IMTs have already run TODO use IMFv2 document data property for this
				StringBuffer runImtNames = new StringBuffer((String) doc.getAttribute("_runImtNames", "|"));
				int runImts = 0;
				
				//	process document
				for (int imt = 0; imt < imts.length; imt++) {
					
					//	skip over previously-run IMTs
					if (runImtNames.indexOf("|" + imtNames[imt] + "|") != -1) {
						console.sendMessage("Skipping previously-run Image Markup Tool '" + imts[imt].getLabel() + "'");
						continue;
					}
					
					//	cache batch processing result (unless we have just started over)
					if (runImts != 0) try {
						goldenGateImagine.notifyDocumentSaving(doc, tempDocFolder, console);
//						ImDocumentIO.storeDocument(doc, tempDocFolder, console);
						ImDocumentIO.storeDocument(doc, tempDocFolder, tempDocStorageFlags, console);
						goldenGateImagine.notifyDocumentSaved(doc, tempDocFolder, console);
						console.sendMessage("Document stored to temporary folder");
					}
					
					//	don't let a caching operation get in the way
					catch (Throwable t) {
						console.sendError("Error caching document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
						console.sendError(t);
					}
					
					//	run IMT
					console.sendMessage("Running Image Markup Tool '" + imts[imt].getLabel() + "'");
					imts[imt].process(doc, null, null, console);
					
					//	update tracking data TODO use IMFv2 document data property for this
					runImtNames.append(imtNames[imt] + "|");
					runImts++;
					doc.setAttribute("_runImtNames", runImtNames.toString());
				}
				
				//	remove batch tracking attribute (we do not want this in the final output)
				doc.removeAttribute("_runImtNames");
				
				//	initialize storage flags
//				long storageFlags = ImDocumentIO.STORAGE_MODE_CSV;
				long storageFlags = goldenGateImagine.getImfStorageFlags();
				
				//	store document to directory ...
				if ("D".equals(dataOutType)) {
					dataOutFile.getAbsoluteFile().getParentFile().mkdirs();
					File dataOutFolder = new File(dataOutFile.getAbsolutePath() + "ir");
//					if (!dataOutFolder.exists())
//						dataOutFolder.mkdirs();
					FolderImDocumentData docData;
					if (dataOutFolder.exists()) {
						if (doc instanceof DataBackedImDocument) {
							ImDocumentData exDocData = ((DataBackedImDocument) doc).getDocumentData();
							if ((exDocData instanceof FolderImDocumentData) && exDocData.canStoreDocument() && dataOutFolder.getAbsolutePath().equals(exDocData.getDocumentDataId()))
								docData = ((FolderImDocumentData) exDocData);
							else docData = new FolderImDocumentData(dataOutFolder, storageFlags);
						}
						else docData = new FolderImDocumentData(dataOutFolder, storageFlags);
					}
					else {
						dataOutFolder.mkdirs();
						docData = new FolderImDocumentData(dataOutFolder, storageFlags);
					}
					console.sendMessage("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
					goldenGateImagine.notifyDocumentSaving(doc, dataOutFolder, console);
//					ImDocumentEntry[] entries = ImDocumentIO.storeDocument(doc, dataOutFolder, console);
//					ImDocumentEntry[] entries = ImDocumentIO.storeDocument(doc, dataOutFolder, storageFlags, console);
					ImDocumentIO.storeDocument(doc, docData, console);
					console.sendMessage("Document entries stored");
					if (dataOutFile.exists()) {
						String exDataOutFileName = dataOutFile.getAbsolutePath();
						dataOutFile.renameTo(new File(exDataOutFileName + "." + System.currentTimeMillis() + ".old"));
						dataOutFile = new File(exDataOutFileName);
					}
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataOutFile), "UTF-8"));
//					for (int e = 0; e < entries.length; e++) {
////						out.write(entries[e].toTabString());
//						out.write(entries[e].toTabString(0 < storageFlags));
//						out.newLine();
//					}
					docData.writeEntryList(out, false); // in TSV mode, this add storage flags that were actually used (just like we need)
					out.flush();
					out.close();
					console.sendMessage("Document stored");
					goldenGateImagine.notifyDocumentSaved(doc, dataOutFolder, console);
				}
				
				//	... or file
				else {
					if (dataOutFile.exists()) {
						String exDataOutFileName = dataOutFile.getAbsolutePath();
						dataOutFile.renameTo(new File(exDataOutFileName + "." + System.currentTimeMillis() + ".old"));
						dataOutFile = new File(exDataOutFileName);
					}
					dataOutFile.getAbsoluteFile().getParentFile().mkdirs();
					OutputStream out = new BufferedOutputStream(new FileOutputStream(dataOutFile));
					console.sendMessage("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
					goldenGateImagine.notifyDocumentSaving(doc, dataOutFile, console);
//					ImDocumentIO.storeDocument(doc, out, console);
					ImDocumentIO.storeDocument(doc, out, storageFlags, console);
					out.flush();
					out.close();
					console.sendMessage("Document stored");
					goldenGateImagine.notifyDocumentSaved(doc, dataOutFile, console);
				}
				
				//	export additional data formats
				for (int e = 0; e < idfes.length; e++) try {
					idfes[e].exportDocument(doc, dataOutFile, console);
				}
				
				//	don't let any additional export error disturb main process
				catch (Throwable t) {
					console.sendError("Error exporting document '" + dataInFiles[d].getAbsolutePath() + "' via '" + idfes[e].getExportMenuLabel() + "': " + t.getMessage());
					console.sendError(t);
				}
				
				//	notify listeners that we're done (only after exports, as they might target cached content)
				goldenGateImagine.notifyDocumentClosed(doc.docId);
				doc.dispose();
			}
			
			//	catch and log whatever might go wrong
			catch (Throwable t) {
				console.sendError("Error processing document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
				console.sendError(t);
				docFullyProcessed = false;
			}
			
			//	clean up, error or not
			finally {
				
				//	if cache root set, clean up cache (fast but small RAM discs will run out of space quickly otherwise)
				if (cacheRootPath != null) {
					cleanCacheFolder(new File(cacheRootPath), 0);
					if (docFullyProcessed)
						cleanCacheFolder(tempDocFolder, 2);
				}
				
				//	close log files if logging per document
				if (perDocLogger != null)
					perDocLogger.close();
				perDocLogger = null;
				
				//	garbage collect whatever is left
				System.gc();
			}
		}
		
		//	shut down whatever threads are left
		System.exit(0);
	}
	
	private static class PerDocLogger {
		private File logFolder;
		private String docName;
		
		private File logFileOut;
		private PrintStream logOut;
		private PrintStream sysOut;
		
		private File logFileErr;
		private PrintStream logErr;
		private PrintStream sysErr;
		
		PerDocLogger(File logFolder, String docName) throws Exception {
			this.logFolder = logFolder;
			this.docName = docName;
			
			//	create log files
			this.logFolder.mkdirs();
			this.logFileOut = new File(this.logFolder, (this.docName + ".out.log"));
			this.logFileErr = new File(this.logFolder, (this.docName + ".err.log"));
			
			//	redirect System.out
			this.logFileOut.createNewFile();
			this.sysOut = System.out;
			this.logOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.logFileOut)), true, "UTF-8");
			System.setOut(this.logOut);
			
			//	redirect System.err
			this.logFileErr.createNewFile();
			this.sysErr = System.err;
			this.logErr = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.logFileErr)), true, "UTF-8");
			System.setErr(this.logErr);

		}
		
		void close() {
			
			//	restore System.out
			System.setOut(this.sysOut);
			this.logOut.flush();
			this.logOut.close();
			
			//	restore System.err
			System.setErr(this.sysErr);
			this.logErr.flush();
			this.logErr.close();
			
			//	zip up log files
			try {
				File zipFile = new File(this.logFolder, (this.docName + ".logs.zip"));
				ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
				this.zipUp(this.logFileOut, zipOut);
				this.zipUp(this.logFileErr, zipOut);
				zipOut.flush();
				zipOut.close();
			}
			catch (Exception e) {
				System.out.println("Could not zip up log files in '" + this.logFolder.getAbsolutePath() + "':" + e.getMessage());
				e.printStackTrace(System.out);
			}
		}
		
		void zipUp(File logFile, ZipOutputStream zipOut) throws Exception {
			
			//	zip up log file (unless it's empty)
			if (logFile.length() != 0) {
				InputStream logIn = new BufferedInputStream(new FileInputStream(logFile));
				zipOut.putNextEntry(new ZipEntry(logFile.getName()));
				byte[] buffer = new byte[1024];
				for (int r; (r = logIn.read(buffer, 0, buffer.length)) != -1;)
					zipOut.write(buffer, 0, r);
				zipOut.closeEntry();
				logIn.close();
			}
			
			//	clean up plain log file
			logFile.delete();
		}
	}
	
	private static void cleanCacheFolder(File folder, int depth) {
		File[] folderContent = folder.listFiles();
		for (int c = 0; c < folderContent.length; c++) try {
			if (folderContent[c].isDirectory()) {
				if ((depth == 0) && "TempDocs".equals(folderContent[c].getName()))
					continue; // do not touch intermediate results of batch, they are cleaned up separately
				cleanCacheFolder(folderContent[c], (depth+1));
				if (depth != 0)
					folderContent[c].delete();
			}
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