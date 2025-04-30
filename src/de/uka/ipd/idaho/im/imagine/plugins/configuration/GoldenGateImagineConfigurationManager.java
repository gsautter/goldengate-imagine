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
package de.uka.ipd.idaho.im.imagine.plugins.configuration;

import java.util.ArrayList;

import de.uka.ipd.idaho.goldenGate.configuration.ConfigurationExportUtils.ExportPluginGroup;
import de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager;
import de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin;
import de.uka.ipd.idaho.im.imagine.GoldenGateImagine;
import de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Configuration manager for GoldenGATE Imagine. This configuration exporter
 * allows explicitly selecting only those plug-ins that can be used in
 * GoldenGATE Imagine. Transitive resolution of dependencies is not affected
 * by this restriction, however.
 * 
 * @author sautter
 */
public class GoldenGateImagineConfigurationManager extends AbstractConfigurationManager implements GoldenGateImaginePlugin {
//	private GoldenGateImagine imagineParent;
	
	/** public zero-argument constructor to facilitate class loading */
	public GoldenGateImagineConfigurationManager() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#setImagineParent(de.uka.ipd.idaho.im.imagine.GoldenGateImagine)
	 */
	public void setImagineParent(GoldenGateImagine ggImagine) {
//		this.imagineParent = ggImagine;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.im.imagine.plugins.GoldenGateImaginePlugin#initImagine()
	 */
	public void initImagine() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getPluginName()
	 */
	public String getPluginName() {
		return "IM Configuration Manager";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#getMainMenuTitle()
	 */
	public String getMainMenuTitle() {
		return "Imagine Configurations";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager#getSelectablePluginClassNames()
	 */
	protected StringVector getSelectablePluginClassNames() {
		StringVector selectablePluginClassNames = new StringVector();
		GoldenGatePlugin[] plugins = this.parent.getPlugins();
		for (int p = 0; p < plugins.length; p++) {
			System.out.println("Checking " + plugins[p].getClass().getName() + " for export");
			if (plugins[p] instanceof GoldenGateImaginePlugin) {
				selectablePluginClassNames.addElementIgnoreDuplicates(plugins[p].getClass().getName());
				System.out.println(" ==> permitted as Imagine plug-in");
				continue;
			}
			Class[] allPluginClasses = getClasses(plugins[p]);
			System.out.println(" - checking " + allPluginClasses.length + " classes and interfaces");
			for (int c = 0; c < allPluginClasses.length; c++) {
				String clsName = allPluginClasses[c].getName();
				System.out.println("   - checking " + clsName);
				if (clsName.indexOf("$") != -1) // truncate to name of top level class
					clsName = clsName.substring(0, clsName.indexOf("$"));
				if (clsName.indexOf(".") != -1) // truncate package prefix
					clsName = clsName.substring(clsName.lastIndexOf(".") + ".".length());
				if (clsName.matches("GoldenGate[A-Za-z]+Plugin")) /* specific to some other application than our own (latter is caught above) */ {
					allPluginClasses = null;
					System.out.println(" ==> filtered out");
					break;
				}
			}
			if (allPluginClasses == null) // filtered as being specific to some other application
				continue;
			selectablePluginClassNames.addElementIgnoreDuplicates(plugins[p].getClass().getName());
			System.out.println(" ==> permitted as generic plug-in");
		}
		return selectablePluginClassNames;
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager#getPluginGroups()
	 */
	protected ExportPluginGroup[] getPluginGroups() {
		ArrayList epgs = new ArrayList();
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider.class, "IM Context Menu Action Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ClickActionProvider.class, "IM Click Action Providers"));
//		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.KeystrokeActionProvider.class, "IM Keyboard Shortcut Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentViewProvider.class, "IM Document View Providers"));
		
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ImageMarkupToolProvider.class, "Image Markup Tool Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler.class, "Image Markup Drag&Drop Action Handlers"));
		
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentIoProvider.class, "Image Document IO Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentExporter.class, "Image Document Exporters"));
		
		//	TODO add further interfaces as we define them (document view providers, etc.)
		
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.goldenGate.gamta.plugins.SelectionActionProvider.class, "GAMTA Context Menu Action Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.goldenGate.gamta.plugins.ClickActionProvider.class, "GAMTA Click Action Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.goldenGate.gamta.plugins.KeystrokeActionProvider.class, "GAMTA Keyboard Shortcut Providers"));
		epgs.add(new ExportPluginGroup(de.uka.ipd.idaho.goldenGate.gamta.plugins.GamtaDocumentViewProvider.class, "GAMTA Document View Providers"));
		
		//	TODO any more groups to add for XML view ???
		
		return ((ExportPluginGroup[]) epgs.toArray(new ExportPluginGroup[epgs.size()]));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager#getApplicationSettingsFileName(java.lang.String)
	 */
	protected String getApplicationSettingsFileName(String configName) {
		return "GgImagine.cnfg";
	}
//	
//	/* (non-Javadoc)
//	 * @see de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager#adjustConfiguration(java.lang.String, de.uka.ipd.idaho.goldenGate.configuration.ConfigurationExportUtils.ExportConfiguration)
//	 */
//	protected void adjustConfiguration(String configName, ExportConfiguration config) {
//		config.addDataItem(new ExportDataItem("GgImagine.cnfg", config.configTimestamp));
//		//	menu and context menu layout are part of configuration of local installation, not user config
//	}
//	
//	/* (non-Javadoc)
//	 * @see de.uka.ipd.idaho.goldenGate.configuration.AbstractConfigurationManager#getSpecialDataHandler(java.lang.String, de.uka.ipd.idaho.goldenGate.configuration.ConfigurationUtils.SpecialDataHandler)
//	 */
//	protected SpecialDataHandler getSpecialDataHandler(final String configName, final SpecialDataHandler sdh) {
//		return new SpecialDataHandler() {
//			public InputStream getInputStream(String dataName) throws IOException {
//				if ("GgImagine.cnfg".equals(dataName))
//					return getGoldenGateImagineConfig(configName);
//				else return sdh.getInputStream(dataName);
//			}
//		};
//	}
//	
//	ByteArrayInputStream getGoldenGateImagineConfig(String configName) throws IOException {
////		
////		//	get current local configuration of GGX
////		Settings ggiSet;
////		if (this.imagineParent == null) // running outside GGI, load settings from disk on demand
////			ggiSet = this.parent.getApplicationSettings("GgImagine.cnfg");
////		else ggiSet = this.imagineParent.getLocalApplicationSettings();
////		if ((ggiSet == null) || (ggiSet.isEmpty()))
////			return null;
//		
//		//	load any general amendments for configuration exports
//		Settings ggiExportSet;
//		if (this.dataProvider.isDataAvailable("GgImagine.cnfg")) {
//			BufferedReader ggiExportBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("GgImagine.cnfg"), "UTF-8"));
//			ggiExportSet = Settings.loadSettings(ggiExportBr);
//			ggiExportBr.close();
//		}
//		else ggiExportSet = null;
//		
//		//	load any configuration specific amendments
//		Settings ggiConfigSet;
//		if (this.dataProvider.isDataAvailable("GgImagine." + configName + ".cnfg")) {
//			BufferedReader ggiConfigBr = new BufferedReader(new InputStreamReader(this.dataProvider.getInputStream("GgImagine." + configName + ".cnfg"), "UTF-8"));
//			ggiConfigSet = Settings.loadSettings(ggiConfigBr);
//			ggiConfigBr.close();
//		}
//		else ggiConfigSet = null;
//		
//		//	amalgamate the two
//		Settings ggiSet;
//		if ((ggiExportSet == null) && (ggiConfigSet == null))
//			return null;
//		else if (ggiExportSet == null)
//			ggiSet = ggiConfigSet;
//		else if (ggiConfigSet == null)
//			ggiSet = ggiExportSet;
//		else {
//			ggiSet = ggiExportSet;
//			String[] ggiConfigSetKeys = ggiConfigSet.getFullKeys();
//			for (int k = 0; k < ggiConfigSetKeys.length; k++) {
//				String confSetValue = ggiConfigSet.getSetting(ggiConfigSetKeys[k]);
//				if ("".equals(confSetValue)) // use empty string to encode removal in configuration based version
//					ggiSet.removeSetting(ggiConfigSetKeys[k]);
//				else ggiSet.setSetting(ggiConfigSetKeys[k], confSetValue);
//			}
//		}
//		
//		//	anything left?
//		if (ggiSet.isEmpty())
//			return null;
//		
//		//	serialize and return what we got
//		ByteArrayOutputStream ggiSetBuffer = new ByteArrayOutputStream();
//		BufferedWriter ggiSetBw = new BufferedWriter(new OutputStreamWriter(ggiSetBuffer, "UTF-8"));
//		ggiSet.storeAsText(ggiSetBw);
//		ggiSetBw.flush();
//		ggiSetBw.close();
//		return new ByteArrayInputStream(ggiSetBuffer.toByteArray());
//	}
	/*
Using local settings makes preciously little sense (display preferences of exporting user ... might be color blind or like their dark mode or something)
- instead, add support for 'GgImagine.<configName>.cnfg' (to be held in data folder of configuration manager) ...
- ... as well as for export dedicated 'GgImagine.cnfg' (to be held in data folder of configuration manager)
==> 'GgImagine.cnfg' and 'GgImagine.local.cnfg' still belong to installation (use might be color blind or like their dark mode or something) ...
==> ... so those local settings need to prevail (and do, with new defaulting approach)
==> 'GgImagine.cnfg' from configuration provides means to inject configuration specific additions over installed defaults ...
==> ... e.g. special annotation types or attribute suggestions
	 */
//	
//	public static void main(String[] args) throws Exception {
//		LinkedHashSet allPluginClasses = new LinkedHashSet();
//		for (Class cls = GoldenGateImagineConfigurationManager.class; cls != null; cls = cls.getSuperclass()) {
//			allPluginClasses.add(cls.getName());
//			System.out.println("Adding class " + cls.getName());
//			Class[] clsIfcs = cls.getInterfaces();
//			for (int i = 0; i < clsIfcs.length; i++) {
//				allPluginClasses.add(clsIfcs[i].getName());
//				System.out.println(" . adding interface " + clsIfcs[i].getName());
//			}
//		}
//		for (Iterator cnit = allPluginClasses.iterator(); cnit.hasNext();) {
//			String clsName = ((String) cnit.next());
//			System.out.println("Checking class name " + clsName);
//			if (clsName.indexOf("$") != -1) // truncate to name of top level class
//				clsName = clsName.substring(0, clsName.indexOf("$"));
//			if (clsName.indexOf(".") != -1) // truncate package prefix
//				clsName = clsName.substring(clsName.lastIndexOf(".") + ".".length());
//			System.out.println(" - truncated to " + clsName);
//			if (clsName.matches("GoldenGate[A-Za-z]+Plugin")) /* specific to some other application than out own (latter is caught above) */ {
//				System.out.println(" ==> filtered as application specific plug-in");
//				allPluginClasses = null;
//			}
//			else System.out.println(" - not an application specific plug-in");
//		}
//	}
}