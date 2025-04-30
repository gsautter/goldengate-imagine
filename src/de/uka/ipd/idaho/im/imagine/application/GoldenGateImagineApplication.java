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

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.GoldenGateConfiguration;
import de.uka.ipd.idaho.goldenGate.applications.ApplicationRuntimeUtils;
import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationRuntimeUtils;
import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationUtils.AppConfigGroupDescriptor;
import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationUtils.AppConfigVersionDescriptor;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagineConstants;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Application wrapper for GoldenGATE Imagine
 * 
 * @author sautter
 */
public class GoldenGateImagineApplication implements GoldenGateImagineConstants {
	private static final String JAVA_LOOK_AND_FEEL_NAME = "JAVA";
	
	private static final String LOG_TIMESTAMP_DATE_FORMAT = "yyyyMMdd-HHmm";
	private static final DateFormat LOG_TIMESTAMP_FORMATTER = new SimpleDateFormat(LOG_TIMESTAMP_DATE_FORMAT);
	
	public static void main(String[] args) throws Exception {
		if ("GgImagine".equals(System.getProperty("gg." + APPLICATION_FAMILY_NAME_APPLICATION_PROPERTY)))
			mainApplication(args); // got application family name from starter or IDE, good to go
		else mainStarter(args); // loop through to starter routine otherwise
	}
	
	private static void mainStarter(String[] args) throws Exception {
		ApplicationRuntimeUtils.startApplication(new File("."), "GgImagine", "GgImagine.ggApp.cnfg", args, true, true);
	}
	
	/**	the main method to run GoldenGATE Imagine as a standalone application
	 * @param args the arguments, which have the following meaning:<ul>
	 * <li>args[0]: the RUN parameter (if not specified, the GoldenGATE.bat startup script will be created)</li>
	 * <li>args[1]: the ONLINE parameter (if not specified, GoldenGATE will run purely offline and will not allow its plugin components to access the network or WWW)</li>
	 * <li>args[2]: the root path of the GoldenGATE installation (if not specified, GoldenGATE will use the current path instead, i.e. './')</li>
	 * <li>args[3]: the file to open (this parameter is used to handle a file drag&dropped on the GoldenGATE.bat startup script)</li>
	 * </ul>
	 */
	private static void mainApplication(String[] args) throws Exception {
		
		//	set platform L&F
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
//		
//		//	if no args, jar has been started directly
//		if ((args.length == 0) || !RUN_PARAMETER.equals(args[0])) {
//			File starterBatch = new File("./GoldenGateImagine.bat");
//			
//			//	create batch file if not exists
//			if (!starterBatch.exists()) {
//				try {
//					EasyIO.writeFile(starterBatch, ("java -jar -Xms" + DEFAULT_START_MEMORY + "m -Xmx" + DEFAULT_MAX_MEMORY + "m GgImagine.jar " + RUN_PARAMETER + " %1"));
//				} catch (IOException ioe) {}
//			}
//			
//			//	batch file exists, show error
//			else JOptionPane.showMessageDialog(null, "Please use GoldenGateImagineStarter.jar to start GoldenGATE Imagine.", "Please Use GoldenGateImagineStarter.jar", JOptionPane.INFORMATION_MESSAGE);
//			
//			//	we're done here
//			System.exit(0);
//			return;
//		}
		
		//	adjust basic parameters
		String basePath = ".";
		boolean online = false;
//		String cacheRootPath = null;
		StringVector argFilePaths = new StringVector();
		String logFileName = ("GgImagine." + LOG_TIMESTAMP_FORMATTER.format(new Date()) + ".log");
		
		//	parse remaining args
		for (int a = 1; a < args.length; a++) {
			String arg = args[a];
			if (arg != null) {
				if (arg.startsWith(BASE_PATH_PARAMETER + "="))
					basePath = arg.substring((BASE_PATH_PARAMETER + "=").length());
//				else if (args[a].startsWith(CACHE_PATH_PARAMETER + "="))
//					cacheRootPath = args[a].substring((CACHE_PATH_PARAMETER + "=").length());
				else if (ONLINE_PARAMETER.equals(arg))
					online = true;
				else if (arg.equals(LOG_PARAMETER + "=IDE") || arg.equals(LOG_PARAMETER + "=NO"))
					logFileName = null;
				else if (arg.startsWith(LOG_PARAMETER + "="))
					logFileName = arg.substring((LOG_PARAMETER + "=").length());
				else argFilePaths.addElementIgnoreDuplicates(arg);
			}
		}
		
		//	remember program base path
		final File rootFolder = new File(basePath);
		
		//	load application properties
		Properties appProperties = ApplicationRuntimeUtils.loadApplicationProperties(rootFolder);
		String appName = appProperties.getProperty(APPLICATION_NAME_APPLICATION_PROPERTY, "GoldenGATE Imagine");
		
		//	keep user posted
		StatusDialog sd = new StatusDialog(Toolkit.getDefaultToolkit().getImage(new File(new File(rootFolder, DATA_FOLDER_NAME), ICON_FILE_NAME).toString()), (appName + " Initializing"));
		sd.popUp();
		
		//	configure web access and set up HTPS (regardless of online or offline mode, better to have HTTPS set up)
//		if (online)
		ApplicationRuntimeUtils.setUpWebAccess(rootFolder, appProperties, true);
		
		//	set configured look & feel
		try {
			String lfName = appProperties.getProperty("lookAndFeel");
			if (JAVA_LOOK_AND_FEEL_NAME.equals(lfName))
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {}
		
		//	create log files if required (which should hardly ever be the case, as starter handles logging)
		if (logFileName != null)
			ApplicationRuntimeUtils.setUpLogFiles(rootFolder, logFileName);
//		if (logFileName != null) try {
//			ApplicationRuntimeUtils.setUpLogFiles(rootFolder, logFileName);
//		}
//		catch (Exception e) {
//			logFileName = logFileName.replace('\\', '/');
//			File logFolder;
//			if (logFileName.startsWith("/") || (logFileName.indexOf(":/") != -1))
//				logFolder = (new File(logFileName)).getAbsoluteFile().getParentFile();
//			else if (logFileName.indexOf("/") == -1)
//				logFolder = new File(rootFolder, LOG_FOLDER_NAME);
//			else {
//				String logFolderName = logFileName.substring(0, logFileName.lastIndexOf("/"));
//				logFolder = new File(rootFolder, logFolderName);
//			}
//			JOptionPane.showMessageDialog(null, "Could not create log files in folder '" + logFolder.getAbsolutePath() + "'." +
//					"\nCommon reasons are a full hard drive, lack of write permissions to the folder, or system protection software." +
//					"\nUse the 'Configure' button in the configuration selector dialog to select a different log location." +
//					"\nThen exit and re-start GoldenGATE Imagine to apply the change." +
//					"\n\nNote that you can work normally without the log files, it's just that in case of an error, there are" +
//					"\nno log files to to help investigate what exactly went wrong and help developers fix the problem.", "Error Creating Log Files", JOptionPane.ERROR_MESSAGE);
//		}
		
		//	get available (and applicable) configurations
		String[] configHosts = (online ? ConfigurationRuntimeUtils.getConfigHosts(rootFolder) : new String[0]);
		String configName = appProperties.getProperty(CONFIGURATION_NAME_APPLICATION_PROPERTY);
		AppConfigVersionDescriptor appConfig;
		
		//	no fixed configuration, open selector
		if (configName == null) {
			String configNameSuffix = appProperties.getProperty(CONFIGURATION_NAME_MARKER_APPLICATION_PROPERTY);
			sd.setStepLabel("Listing Configurations");
			AppConfigGroupDescriptor[] appConfigs = ConfigurationRuntimeUtils.listAvailableConfigurationGroups(configHosts, rootFolder, configNameSuffix);
			
			//	select configuration
			appConfig = ConfigurationRuntimeUtils.selectConfiguration(appConfigs, rootFolder, sd);
		}
		
		//	start with configured configuration
		else {
			AppConfigVersionDescriptor[] appConfigVersions = ConfigurationRuntimeUtils.listAvailableConfigurations(configHosts, rootFolder, configName);
			String updateStrategy = (online ? ConfigurationRuntimeUtils.SELECTION_STRATEGY_UPDATE_STEPPED : ConfigurationRuntimeUtils.SELECTION_STRATEGY_UPDATE_LOCAL);
			appConfig = ConfigurationRuntimeUtils.getConfiguration(appConfigVersions, configName, updateStrategy, rootFolder, sd);
		}
		
		//	check if cancelled
		if (appConfig == null) {
			System.out.println("Cannot " + ((configName == null) ? "work without configuration" : ("find configuration '" + configName + "', please check config files")));
			if (configName != null)
				JOptionPane.showMessageDialog(sd, ("Cannot find configuration '" + configName + "', please check config files"), ("Configuration '" + configName + "' Not Found"), JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		//	get GoldenGATE Imagine configuration
		GoldenGateConfiguration ggiConfig = ConfigurationRuntimeUtils.loadConfiguration(appConfig, rootFolder, sd);
		
		//	create GoldenGATE core
		GoldenGATE goldenGate = GoldenGATE.openGoldenGATE(rootFolder, ggiConfig, sd);
		
		//	get cache root folder
		String cacheRootPath = appProperties.getProperty("cacheRootFolder");
		
		//	get GGI specific settings and adjust in-memory object before handing over to GGI core
		Settings ggiSet = goldenGate.getApplicationSettings("GgImagine.cnfg");
		if (cacheRootPath != null)
			ggiSet.setSetting("cacheRootFolder", cacheRootPath); // GGI reads this internally
		String pageImagePath = appProperties.getProperty("pageImageFolder");
		if (pageImagePath != null)
			ggiSet.setSetting("pageImageFolder", pageImagePath); // GGI reads this internally
		
		//	create GoldenGTAE Imagine core
		GoldenGateImagine goldenGateImagine = GoldenGateImagine.openGoldenGATE(rootFolder, goldenGate);
		System.out.println("GoldenGATE Imagine core created, configuration is " + ggiConfig.getName());
		
		//	create document cache folder (settings should be loaded by now)
		String docCacheFolderName;
		if ((cacheRootPath == null) || (cacheRootPath.length() == 0))
			docCacheFolderName = "./DocumentData";
		else if (cacheRootPath.endsWith("/"))
			docCacheFolderName = (cacheRootPath + "DocumentData");
		else docCacheFolderName = (cacheRootPath + "/" + "DocumentData");
		File docCacheFolder;
		if (docCacheFolderName.startsWith("/") || (docCacheFolderName.indexOf(':') != -1))
			docCacheFolder = new File(docCacheFolderName);
		else if (docCacheFolderName.startsWith("./"))
			docCacheFolder = new File(rootFolder, docCacheFolderName.substring("./".length()));
		else docCacheFolder = new File(rootFolder, docCacheFolderName);
		if (!docCacheFolder.exists())
			docCacheFolder.mkdirs();
		
		//	hide status dialog
		sd.setVisible(false);
		
		//	open GoldenGATE Imagine window
		GoldenGateImagineUI ggiUi = new GoldenGateImagineUI(goldenGateImagine, appProperties.getProperty(SPECIFIED_VERSION_DATE_APPLICATION_PROPERTY), docCacheFolder);
		ggiUi.setIconImage(ggiConfig.getIconImage());
		ggiUi.setVisible(true);
		System.out.println(" - window opened");
		
		//	make sure to close down any remaining threads when window closed
		ggiUi.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent we) {
				System.exit(0);
			}
		});
		
		//	open any dropped files
		for (int f = 0; f < argFilePaths.size(); f++)
			ggiUi.loadDroppedFile(argFilePaths.get(f));
	}
}