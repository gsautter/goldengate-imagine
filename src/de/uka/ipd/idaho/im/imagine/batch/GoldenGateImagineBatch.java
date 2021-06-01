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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.util.DocumentStyle;
import de.uka.ipd.idaho.gamta.util.ParallelJobRunner;
import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.FileConfiguration;
import de.uka.ipd.idaho.goldenGate.configuration.UrlConfiguration;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagineConstants;
import de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentFileExporter;
import de.uka.ipd.idaho.im.pdf.PdfExtractor;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.CustomFontDecoderCharset;
import de.uka.ipd.idaho.im.pdf.PdfFontDecoder.FontDecoderCharset;
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
		
		//	print help and exit if asked to
		if (printHelpExplicit || printHelpImplicit) {
			System.out.println("GoldenGATE Imagine Batch can take the following parameters:");
			System.out.println("");
			System.out.println("PATH:\tthe folder to run GoldenGATE Imagine Batch in (defaults to the\r\n\tinstallation folder)");
			System.out.println("CONF:\tthe (path and) name of the configuration file to run GoldenGATE\r\n\tImagine Batch with (defaults to 'GgImagineBatch.cnfg' in the folder\r\n\tGoldenGATE Imagine Batch is running in)");
			System.out.println("CACHE:\tthe root folder for all data caching folders (defaults to the path\r\n\tfolder, useful for directing caching to a RAM disc, etc.)");
			System.out.println("DATA:\tthe PDF files to process:");
			System.out.println("\t- set to PDF file path and name to process that file");
			System.out.println("\t- set to folder path and name to process all PDF files in that folder");
			System.out.println("\t- set to TXT file to process all PDF files listed in that file");
			System.out.println("DT:\tthe type of the PDF files to process (defaults to 'G' for 'generic'):");
			System.out.println("\t- set to 'D' or 'BD' to indicate born-digital PDF files");
			System.out.println("\t- set to 'S' to indicate scanned PDF files");
			System.out.println("\t- set to 'G' or omit to indicate generic PDF files (expects both\r\n\t  born-digital and scanned, determining type on a per-file basis)");
			System.out.println("FM:\tthe way of handling embedded fonts (relevant only for 'DT=D' and \r\n\t'DT=G'):");
			System.out.println("\t- set to D to completely decode embedded fonts");
			System.out.println("\t- set to V to decode un-mapped characters from embedded fonts, i.e.,\r\n\t  ones without a Unicode mapping, and verify existing Unicode mappings");
			System.out.println("\t- set to U to decode un-mapped characters from embedded fonts, i.e.,\r\n\t  ones without a Unicode mapping (the default)");
			System.out.println("\t- set to R to only render embedded fonts, but do not decode glyphs");
			System.out.println("\t- set to Q for quick mode, using Unicode mapping only");
			System.out.println("CS:\tthe char set for decoding embedded fonts (relevant only for 'FM=D' and\r\n\t'FM=U'):");
			System.out.println("\t- set to U to use all of Unicode");
			System.out.println("\t- set to S to use Latin characters and scientific symbols only (the\r\n\t  default)");
			System.out.println("\t- set to M to use Latin characters and mathematical symbols only");
			System.out.println("\t- set to F to use Full Latin and derived characters only");
			System.out.println("\t- set to L to use Extended Latin characters only");
			System.out.println("\t- set to B to use Basic Latin characters only");
			System.out.println("\t- set to C for custom, using 'CP' parameter to specify path (file or\r\n\t  URL) to load from, or name of a named charset to load from a provider");
			System.out.println("CP:\tthe file or URL to load the charset for embedded font decoding from\r\n\t(relevant only for 'CS=C', and required then; implies 'CS=C' if 'CS'\r\n\tparameter omitted); can also be the name of a named charset to resolve\r\n\tvia some provider (prefix with '@' to indicate such a name)");
			System.out.println("OUT:\tthe folder to store the produced IMF files in (defaults to the folder\r\n\teach individual source PDF file was loaded from)");
			System.out.println("OT:\tthe way of storing the produced IMF files (defaults to 'F' for 'file'):");
			System.out.println("\t- set to 'F' or omit to indicate (zipped) single file storage");
			System.out.println("\t- set to 'D' to indicate indicate (non-zipped) folder storage");
			System.out.println("LOG:\tthe name for the log files to write respective information to (file\r\n\tnames are suffixed with '.out.log' and '.err.log', set to 'IDE' or 'NO'\r\n\tto log directly to the console, or to DOC to create one log file per\r\n\tdocument, located next to the IMF)");
			System.out.println("ST:\tno value, just add this token to the command to make the batch run on a\r\n\tsingle core (e.g. if resources required for other simultaneous tasks)");
			System.out.println("VC:\tno value, just add this token to the command to make the batch produce\r\n\tverbose console output");
			System.out.println("HELP:\tprint this help text");
			System.out.println("");
			System.out.println("The file configuration file ('GgImagineBatch.cnfg' by default) specifies how\r\nto process PDF documents after decoding, and can also provide environmental and\r\nPDF decoding parameters:");
			System.out.println("- imageMarkupTools: a space separated list of the Image Markup Tools to run");
			System.out.println("- documentExporters: a space separated list of the Document Exporters to run\r\n  after processing is finished (defaults to all available)");
			System.out.println("- configName: the name of the GoldenGATE Imagine configuration to load the\r\n  Image Markup Tools and Document Exporters from");
			System.out.println("- cacheRootFolder: configurable default for 'CACHE' parameter");
			System.out.println("- fonts.decoding.mode: configurable default for 'FM' parameter");
			System.out.println("- fonts.decoding.charset: configurable default for 'CS' parameter");
			System.out.println("- fonts.decoding.charsetPath: configurable default for 'FP' parameter");
			
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
			System.out.println("No data specified to work with, use 'DATA' parameter:");
			System.out.println("- set to PDF file name: process that file");
			System.out.println("- set to IMF file name: process that file");
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
		final PrintStream systemOut = new PrintStream(System.out, true) {
			public void println(String str) {
				super.println(str);
				if (System.out != this.out)
					System.out.println(str);
			}
			public void println() {
				super.println();
				if (System.out != this.out)
					System.out.println();
			}
			public void println(boolean x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(char x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(int x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(long x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(float x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(double x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(char[] x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
			public void println(Object x) {
				super.println(x);
				if (System.out != this.out)
					System.out.println(x);
			}
		};
		
		//	create log files if required
		File logFolder = null;
		if ((logFileName != null) && !"DOC".equals(logFileName)) try {
			File logFileOut = null;
			File logFileErr = null;
			
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
		File ggiSettingsFile;
		if (ggiConfigPath.startsWith("/") || (ggiConfigPath.indexOf(":\\") == 1) || (ggiConfigPath.indexOf(":/") == -1))
			ggiSettingsFile = new File(ggiConfigPath);
		else ggiSettingsFile = new File(BASE_PATH, ggiConfigPath);
		Settings ggiSettings = Settings.loadSettings(ggiSettingsFile);
		
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
		
		//	create GoldenGATE Imagine core
		GoldenGateConfiguration ggiConfig = null;
		
		//	local master configuration selected
		if (ggiConfigName == null)
			ggiConfig = new FileConfiguration("Local Master Configuration", BASE_PATH, true, true, null);
		
		//	other local configuration selected
		else if (ggiConfigName.startsWith("http://") || ggiConfigName.startsWith("https://"))
			ggiConfig = new UrlConfiguration(ggiConfigName);
		
		//	remote configuration selected
		else ggiConfig = new FileConfiguration(ggiConfigName, new File(new File(BASE_PATH, CONFIG_FOLDER_NAME), ggiConfigName), false, true, null);
		
		//	check for config file specified cache root
		if (cacheRootPath == null)
			cacheRootPath = ggiSettings.getSetting("cacheRootFolder");
		
		//	folder for temporarily storing documents during batch processing
		File tempDocRootFolder = null;
		
		//	if cache path set, add settings for page image and supplement cache
		if (cacheRootPath != null) {
			
			//	make sure cache path denotes folder
			if (!cacheRootPath.endsWith("/"))
				cacheRootPath += "/";
			
			//	add PDF decoder cache settings
			Settings set = ggiConfig.getSettings();
			set.setSetting("cacheRootFolder", cacheRootPath);
			set.setSetting("pageImageFolder", (cacheRootPath + "PageImages"));
			set.setSetting("supplementFolder", (cacheRootPath + "Supplements"));
			
			//	set up temporary document storage for batch crash recovery
			String tempDocRootFolderName = (cacheRootPath + "TempDocs");
			if (tempDocRootFolderName.startsWith("/") || (tempDocRootFolderName.indexOf(':') != -1))
				tempDocRootFolder = new File(tempDocRootFolderName);
			else if (tempDocRootFolderName.startsWith("./"))
				tempDocRootFolder = new File(BASE_PATH, tempDocRootFolderName.substring("./".length()));
			else tempDocRootFolder = new File(BASE_PATH, tempDocRootFolderName);
			if (!tempDocRootFolder.exists())
				tempDocRootFolder.mkdirs();
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
		final PrintStream pmInfoSystemOut = (verboseConsoleOutput ? systemOut : System.out);
		ProgressMonitor pm = new ProgressMonitor() {
			public void setStep(String step) {
				systemOut.println(step);
			}
			public void setInfo(String info) {
				pmInfoSystemOut.println(info);
			}
			public void setBaseProgress(int baseProgress) {}
			public void setMaxProgress(int maxProgress) {}
			public void setProgress(int progress) {}
		};
		
		//	get PDF converter
		PdfExtractor pdfExtractor = goldenGateImagine.getPdfExtractor();
		
		//	load and check char set file if specified
		FontDecoderCharset fontDecoderCharSet = null;
		if ("D".equalsIgnoreCase(dataType) || "BD".equalsIgnoreCase(dataType) || "T".equalsIgnoreCase(dataType)) {
			
			//	default font decoding mode and charset via settings
			if (fontCharSetPath == null)
				fontCharSetPath = ggiSettings.getSetting("fonts.decoding.charsetPath");
			if (fontCharSet == null) {
				if (fontCharSetPath != null)
					fontCharSet = "C"; // use custom charset if we have one
				else fontCharSet = ggiSettings.getSetting("fonts.decoding.charset", "S");
			}
			if (fontMode == null)
				fontMode = ggiSettings.getSetting("fonts.decoding.mode", "U");
			
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
						systemOut.println("Document '" + dataInFiles[d].getAbsolutePath() + "' processed before, skipping");
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
					systemOut.println("Unknown input format in document '" + dataInFiles[d].getAbsolutePath() + "', skipping");
					continue;
				}
				
				//	we're processing this one
				systemOut.println("Processing document '" + dataInFiles[d].getAbsolutePath() + "'");
				
				//	create document specific log files if requested
				if ("DOC".equals(logFileName)) try {
					logFolder = dataOutFile.getAbsoluteFile().getParentFile();
					perDocLogger = new PerDocLogger(logFolder, dataInFiles[d].getName());
				}
				catch (Exception e) {
					systemOut.println("Could not create log files in folder '" + logFolder.getAbsolutePath() + "':" + e.getMessage());
					e.printStackTrace(systemOut);
				}
				
				//	convert input PDF, load IMF
				ImDocument doc = null;
				
				//	check if we have an earlier version cached (batch might have failed at some point)
				if ((tempDocFolder != null) && (new File(tempDocFolder, "entries.txt")).exists()) try {
					doc = ImDocumentIO.loadDocument(tempDocFolder);
					systemOut.println(" - document restored from previous batch run");
				}
				
				//	don't let a cache lookup get in the way
				catch (Throwable t) {
					systemOut.println("Error loading document '" + dataInFiles[d].getAbsolutePath() + "' from cache: " + t.getMessage());
					t.printStackTrace(systemOut);
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
						systemOut.println(" - loaded PDF of " + baos.size() + " bytes");
						
						//	convert PDF
						if ("D".equalsIgnoreCase(dataType) || "BD".equalsIgnoreCase(dataType) || "T".equalsIgnoreCase(dataType))
							doc = pdfExtractor.loadTextPdf(baos.toByteArray(), fontDecoderCharSet, pm);
						else if ("S".equalsIgnoreCase(dataType))
							doc = pdfExtractor.loadImagePdf(baos.toByteArray(), true, pm);
						else doc = pdfExtractor.loadGenericPdf(baos.toByteArray(), pm);
						systemOut.println(" - PDF converted, document ID is '" + doc.docId + "'");
						
						//	add document name
						doc.setAttribute(ImDocument.DOCUMENT_NAME_ATTRIBUTE, dataInFiles[d].getName());
						
						//	cache PDF conversion result
						if (tempDocFolder != null) try {
							systemOut.println("Storing conversion result to temporary folder");
							ImDocumentIO.storeDocument(doc, tempDocFolder, pm);
							systemOut.println("Document stored to temporary folder");
						}
						
						//	don't let a caching operation get in the way
						catch (Throwable t) {
							systemOut.println("Error caching document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
							t.printStackTrace(systemOut);
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
						systemOut.println(" - unknown document format");
						continue;
					}
				}
				
				//	test if document style detected
				if (DocumentStyle.getStyleFor(doc) == null) {
					systemOut.println(" - unable to assign document style");
					continue;
				}
				else systemOut.println(" - assigned document style '" + ((String) doc.getAttribute(DocumentStyle.DOCUMENT_STYLE_NAME_ATTRIBUTE)) + "'");
				
				//	notify listeners
				goldenGateImagine.notifyDocumentOpened(doc, dataInFiles[d], pm);
				
				//	keep track of which IMTs have already run
				StringBuffer runImtNames = new StringBuffer((String) doc.getAttribute("_runImtNames", "|"));
				int runImts = 0;
				
				//	process document
				for (int imt = 0; imt < imts.length; imt++) {
					
					//	skip over previously-run IMTs
					if (runImtNames.indexOf("|" + imtNames[imt] + "|") != -1) {
						systemOut.println("Skipping previously-run Image Markup Tool '" + imts[imt].getLabel() + "'");
						continue;
					}
					
					//	cache batch processing result (unless we have just started over)
					if (runImts != 0) try {
						goldenGateImagine.notifyDocumentSaving(doc, tempDocFolder, pm);
						ImDocumentIO.storeDocument(doc, tempDocFolder, pm);
						goldenGateImagine.notifyDocumentSaved(doc, tempDocFolder, pm);
						systemOut.println("Document stored to temporary folder");
					}
					
					//	don't let a caching operation get in the way
					catch (Throwable t) {
						systemOut.println("Error caching document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
						t.printStackTrace(systemOut);
					}
					
					//	run IMT
					systemOut.println("Running Image Markup Tool '" + imts[imt].getLabel() + "'");
					imts[imt].process(doc, null, null, pm);
					
					//	update tracking data
					runImtNames.append(imtNames[imt] + "|");
					runImts++;
					doc.setAttribute("_runImtNames", runImtNames.toString());
				}
				
				//	remove batch tracking attribute (we do not want this in the final output)
				doc.removeAttribute("_runImtNames");
				
				//	store document to directory ...
				if ("D".equals(dataOutType)) {
					dataOutFile.getAbsoluteFile().getParentFile().mkdirs();
					File dataOutFolder = new File(dataOutFile.getAbsolutePath() + "ir");
					if (!dataOutFolder.exists())
						dataOutFolder.mkdirs();
					systemOut.println("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
					goldenGateImagine.notifyDocumentSaving(doc, dataOutFolder, pm);
					ImDocumentEntry[] entries = ImDocumentIO.storeDocument(doc, dataOutFolder, pm);
					systemOut.println("Document entries stored");
					if (dataOutFile.exists()) {
						String exDataOutFileName = dataOutFile.getAbsolutePath();
						dataOutFile.renameTo(new File(exDataOutFileName + "." + System.currentTimeMillis() + ".old"));
						dataOutFile = new File(exDataOutFileName);
					}
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataOutFile), "UTF-8"));
					for (int e = 0; e < entries.length; e++) {
						out.write(entries[e].toTabString());
						out.newLine();
					}
					out.flush();
					out.close();
					systemOut.println("Document stored");
					goldenGateImagine.notifyDocumentSaved(doc, dataOutFolder, pm);
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
					systemOut.println("Storing document to '" + dataOutFile.getAbsolutePath() + "'");
					goldenGateImagine.notifyDocumentSaving(doc, dataOutFile, pm);
					ImDocumentIO.storeDocument(doc, out, pm);
					out.flush();
					out.close();
					systemOut.println("Document stored");
					goldenGateImagine.notifyDocumentSaved(doc, dataOutFile, pm);
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
				
				//	notify listeners that we're done (only after exports, as they might target cached content)
				goldenGateImagine.notifyDocumentClosed(doc.docId);
				doc.dispose();
			}
			
			//	catch and log whatever might go wrong
			catch (Throwable t) {
				systemOut.println("Error processing document '" + dataInFiles[d].getAbsolutePath() + "': " + t.getMessage());
				t.printStackTrace(systemOut);
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