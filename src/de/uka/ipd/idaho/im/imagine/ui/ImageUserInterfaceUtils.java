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
package de.uka.ipd.idaho.im.imagine.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.goldenGate.GoldenGATE;
import de.uka.ipd.idaho.goldenGate.plugins.ResourceManager;
import de.uka.ipd.idaho.goldenGate.ui.DocumentFunction;
import de.uka.ipd.idaho.goldenGate.ui.UserInterfaceUtils;
import de.uka.ipd.idaho.goldenGate.ui.UserInterfaceUtils.DocumentFunctionGroup;
import de.uka.ipd.idaho.goldenGate.util.DialogPanel;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.imagine.plugins.SelectionActionProvider;
import de.uka.ipd.idaho.im.util.ImDocumentIO;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.SelectionAction;
import de.uka.ipd.idaho.im.util.ImUtils;

/**
 * @author sautter
 */
public class ImageUserInterfaceUtils {
	private static final boolean DEBUG_CONTEXT_MENU_STYLING = false;
	
	public static JMenuItem styleContextMenuItem(JMenuItem mi, SelectionAction action) {
		if (DEBUG_CONTEXT_MENU_STYLING) System.out.println("Styling menu item for " + action.name);
		String providerClassName = getProviderClassName(action);
		if (providerClassName != null) {
			if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" ==> applying generic styling with provider class name '" + providerClassName + "'");
			styleContextMenuItem(mi, providerClassName, action, null);
		}
		else if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" ==> styled by provider");
		return mi;
	}
	
	private static final String STYLED_EXTERNALLY = "STYLED_EXTERNALLY";
	private static Map providerClassNamesByActionClassNames = Collections.synchronizedMap(new HashMap());
	private static String getProviderClassName(SelectionAction action) {
		String actionClassName = action.getClass().getName();
		String providerClassName = ((String) providerClassNamesByActionClassNames.get(actionClassName));
		if (providerClassName == null) {} // need to analyze this one below
		else if (providerClassName == STYLED_EXTERNALLY) // provider styled, nothing to do on our end
			return null;
		else return providerClassName; // cache hit
		try {
			Class actionClass = action.getClass();
			if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" - class is " + actionClass.getName());
			Method miCreatorMethod = actionClass.getMethod("getMenuItem", ImDocumentMarkupPanel.class);
			if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" - menu item getter declared by " + miCreatorMethod.getDeclaringClass().getName());
			if (SelectionAction.class.equals(miCreatorMethod.getDeclaringClass())) {
				if (actionClassName.indexOf("$") == -1) {
					if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" ==> top level action class name");
					providerClassNamesByActionClassNames.put(actionClassName, STYLED_EXTERNALLY);
					return null;
				}
				else {
					providerClassName = actionClassName.substring(0, actionClassName.indexOf("$"));
					providerClassNamesByActionClassNames.put(actionClassName, providerClassName);
					if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" ==> got provider class name '" + providerClassName + "'");
					return providerClassName;
				}
			}
			else {
				if (DEBUG_CONTEXT_MENU_STYLING) System.out.println(" ==> styled externally by provider");
				providerClassNamesByActionClassNames.put(actionClassName, STYLED_EXTERNALLY);
				return null;
			}
		}
		catch (Exception e) {
			providerClassNamesByActionClassNames.put(actionClassName, STYLED_EXTERNALLY); // no need to try again, exception would just come back
			e.printStackTrace(System.out);
			return null;
		}
	}
	
	public static JMenuItem styleContextMenuItem(JMenuItem menuItem, SelectionActionProvider actionProvider, SelectionAction action, Color targetColor) {
		if (menuItem == null)
			return menuItem;
		return styleContextMenuItem(menuItem, ((actionProvider == null) ? null : actionProvider.getClass().getName()), action, targetColor);
	}
	
	public static JMenuItem styleContextMenuItem(JMenuItem menuItem, String providerClassName, SelectionAction action, Color targetColor) {
		if (menuItem == null)
			return menuItem;
		return styleContextMenuItem(menuItem, providerClassName, action.name, action.label, action.tooltip, targetColor);
	}
	
	public static JMenuItem styleContextMenuItem(JMenuItem menuItem, String providerClassName, String name, String label, String tooltip, Color targetColor) {
		if (menuItem == null)
			return menuItem;
		return UserInterfaceUtils.styleDesktopContextMenuItem(menuItem, providerClassName, name, label, tooltip, targetColor, null);
	}
	
	public static SelectionAction createAdvancedSelectionAction(GoldenGATE goldenGate, final String functionOwnerClassName, final ImageDocumentDisplay display, final Annotation selection) {
		if (selection == null)
			return null;
		final DocumentFunctionGroup[] dfgs = UserInterfaceUtils.getGenericDocumentFunctionsForSelection(goldenGate, functionOwnerClassName, display, selection);
		if (dfgs.length == 0)
			return null;
		return new SelectionAction("advancedContextManu", "Advanced", ("Apply any advanced generic functions to the selected " + selection.getType() + "")) {
			public boolean performAction(ImDocumentMarkupPanel invoker) {
				return false; // document functions handle atomic actions and refresh
			}
			public JMenuItem getMenuItem(ImDocumentMarkupPanel invoker) {
				JMenu advancedMenu = new JMenu();
				ImageUserInterfaceUtils.styleContextMenuItem(advancedMenu, functionOwnerClassName, this, null);
				for (int g = 0; g < dfgs.length; g++) {
					if (dfgs[g].functions.length == 1) {
						final DocumentFunction df = dfgs[g].functions[0];
						JMenuItem groupMenuItem = new JMenuItem(df.label);
						groupMenuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								df.applyTo(null, display, selection);
							}
						});
						UserInterfaceUtils.styleDesktopContextMenuItem(df, groupMenuItem);
						advancedMenu.add(groupMenuItem);
					}
					else {
						JMenu groupMenu = new JMenu();
						String menuLabel;
						if (dfgs[g].owner instanceof ResourceManager) {
							menuLabel = ((ResourceManager) dfgs[g].owner).getResourceTypeLabel();
							if (menuLabel == null)
								menuLabel = dfgs[g].owner.getPluginName();
							else if (menuLabel.endsWith("es")) {}
							else if (menuLabel.endsWith("s") || menuLabel.endsWith("x"))
								menuLabel = (menuLabel + "es");
							else menuLabel = (menuLabel + "s");
							groupMenu.setText(menuLabel);
						}
						else menuLabel = dfgs[g].owner.getPluginName();
						UserInterfaceUtils.styleDesktopContextMenuItem(groupMenu, dfgs[g].owner.getClass().getName(), "advancedContextActions", menuLabel, ("Functionality from " + dfgs[g].owner.getPluginName()), null, null);
						for (int f = 0; f < dfgs[g].functions.length; f++) {
							final DocumentFunction df = dfgs[g].functions[f];
							JMenuItem functionMenuItem = new JMenuItem(df.label);
							functionMenuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									df.applyTo(null, display, selection);
								}
							});
							UserInterfaceUtils.styleDesktopContextMenuItem(df, functionMenuItem);
							groupMenu.add(functionMenuItem);
						}
						advancedMenu.add(groupMenu);
					}
				}
				return advancedMenu;
			}
		};
	}
	
	public static String getAnnotationShortValue(ImAnnotation annot) {
		return getAnnotationShortValue(annot.getFirstWord(), annot.getLastWord());
	}
	public static String getAnnotationShortValue(ImWord firstWord, ImWord lastWord) {
		if (annotationShortValueLength < 0) {
			Object asvlObj = UserInterfaceUtils.getDisplayProperty("annot.shortValueLength");
			if (asvlObj instanceof Number)
				annotationShortValueLength = ((Number) asvlObj).intValue();
			else {
				UserInterfaceUtils.setDisplayProperty("annot.shortValueLength", Integer.valueOf(defaultAnnotationShortValueLength));
				annotationShortValueLength = defaultAnnotationShortValueLength;
			}
		}
		return ImUtils.getString(firstWord, lastWord, annotationShortValueLength, true);
	}
	private static final int defaultAnnotationShortValueLength = 20;
	private static int annotationShortValueLength = -1;
	
	public static String getAnnotationLabelValue(ImAnnotation annot) {
		return getAnnotationLabelValue(annot.getFirstWord(), annot.getLastWord());
	}
	public static String getAnnotationLabelValue(ImWord firstWord, ImWord lastWord) {
		if (annotationLabelValueLength < 0) {
			Object alvlObj = UserInterfaceUtils.getDisplayProperty("annot.labelValueLength");
			if (alvlObj instanceof Number)
				annotationLabelValueLength = ((Number) alvlObj).intValue();
			else {
				UserInterfaceUtils.setDisplayProperty("annot.labelValueLength", Integer.valueOf(defaultAnnotationLabelValueLength));
				annotationLabelValueLength = defaultAnnotationLabelValueLength;
			}
		}
		return ImUtils.getString(firstWord, lastWord, annotationLabelValueLength, true);
	}
	private static final int defaultAnnotationLabelValueLength = 40;
	private static int annotationLabelValueLength = -1;
	
	public static String getAnnotationTooltipValue(ImAnnotation annot) {
		return getAnnotationTooltipValue(annot.getFirstWord(), annot.getLastWord());
	}
	public static String getAnnotationTooltipValue(ImWord firstWord, ImWord lastWord) {
		if (annotationTooltipValueLength < 0) {
			Object alvlObj = UserInterfaceUtils.getDisplayProperty("annot.tooltipValueLength");
			if (alvlObj instanceof Number)
				annotationTooltipValueLength = ((Number) alvlObj).intValue();
			else {
				UserInterfaceUtils.setDisplayProperty("annot.tooltipValueLength", Integer.valueOf(defaultAnnotationTooltipValueLength));
				annotationTooltipValueLength = defaultAnnotationTooltipValueLength;
			}
		}
		return ImUtils.getString(firstWord, lastWord, annotationTooltipValueLength, true);
	}
	private static final int defaultAnnotationTooltipValueLength = 100;
	private static int annotationTooltipValueLength = -1;
	
	public static void configureDisplay(ImDocumentMarkupPanel idmp, String title) {
		if (title == null)
			title = "Adjust Display Configuration";
		DisplayConfigDialog dcd = new DisplayConfigDialog(title, idmp);
		dcd.setVisible(true);
	}
	
	private static class DisplayConfigDialog extends DialogPanel {
		private static final Integer[] boxSelectionThicknesses;
		private static final Integer[] wordSelectionAlphas;
		static {
			boxSelectionThicknesses = new Integer[16];
			for (int t = 0; t < boxSelectionThicknesses.length; t++)
				boxSelectionThicknesses[t] = Integer.valueOf(t+1);
			wordSelectionAlphas = new Integer[256];
			for (int a = 0; a < wordSelectionAlphas.length; a++)
				wordSelectionAlphas[a] = Integer.valueOf(a);
		}
		private static Dimension initialSize = new Dimension(400, 200);
		private static Point initialPos = null;
		
		ImDocumentMarkupPanel idmp;
		
		Color idmpBoxSelectionColor;
		int idmpBoxSelectionThickness;
		Color boxSelectionColor;
		JButton boxSelectionColorButton = new JButton("Color");
		JComboBox boxSelectionThickness;
		
		Color idmpWordSelectionColor;
		Color wordSelectionColor;
		JButton wordSelectionColorButton = new JButton("Color");
		JComboBox wordSelectionAlpha;
		
		int idmpAnnotHighlightAlpha;
		int annotHighlightAlpha;
		JSlider annotHighlightAlphaSlider;
		
		DisplayConfigDialog(String title, ImDocumentMarkupPanel idmp) {
			super(title, false);
			this.idmp = idmp;
			this.idmpBoxSelectionColor = this.idmp.getSelectionBoxColor();
			this.idmpBoxSelectionThickness = this.idmp.getSelectionBoxThickness();
			this.idmpWordSelectionColor = this.idmp.getSelectionHighlightColor();
			this.idmpAnnotHighlightAlpha = this.idmp.getAnnotationHighlightAlpha();
			
			//	create box selection controls
			this.boxSelectionColor = this.idmp.getSelectionBoxColor();
			this.boxSelectionColorButton.setToolTipText("Select color for selection box");
			this.boxSelectionColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Color color = JColorChooser.showDialog(DisplayConfigDialog.this, "Select Box Selection Color", boxSelectionColor);
					if (color == null)
						return;
					boxSelectionColor = color;
					updateControls(false);
					DisplayConfigDialog.this.idmp.setSelectionBoxColor(boxSelectionColor); // for preview
				}
			});
			this.boxSelectionThickness = new JComboBox(boxSelectionThicknesses);
			this.boxSelectionThickness.setEditable(false);
			this.boxSelectionThickness.setBorder(BorderFactory.createLoweredBevelBorder());
			this.boxSelectionThickness.setPreferredSize(new Dimension(50, 21));
			this.boxSelectionThickness.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					Object value = boxSelectionThickness.getSelectedItem();
					if (value instanceof Integer)
						DisplayConfigDialog.this.idmp.setSelectionBoxThickness(((Integer) value).intValue()); // for preview
				}
			});
			JPanel boxSelectionThicknessPanel = new JPanel(new BorderLayout(), true);
			boxSelectionThicknessPanel.add(new JLabel("Thickness ", JLabel.RIGHT), BorderLayout.CENTER);
			boxSelectionThicknessPanel.add(this.boxSelectionThickness, BorderLayout.EAST);
			JPanel boxSelectionPanel = new JPanel(new GridLayout(0, 1, 0, 4), true);
			boxSelectionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createLineBorder(boxSelectionPanel.getBackground(), 2)));
			boxSelectionPanel.add(new JLabel("Box Selection Settings", JLabel.CENTER));
			boxSelectionPanel.add(this.boxSelectionColorButton);
			boxSelectionPanel.add(boxSelectionThicknessPanel);
			
			//	create word selection controls
			this.wordSelectionColor = this.idmp.getSelectionHighlightColor();
			this.wordSelectionColorButton.setToolTipText("Select color for word selection bighlight");
			this.wordSelectionColorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					Color fullColor = ((wordSelectionColor.getAlpha() == 0xFF) ? wordSelectionColor : new Color(wordSelectionColor.getRed(), wordSelectionColor.getGreen(), wordSelectionColor.getBlue(), 0xFF));
					Color color = JColorChooser.showDialog(DisplayConfigDialog.this, "Select Word Selection Color", fullColor);
					if (color == null)
						return;
					if (fullColor == wordSelectionColor)
						wordSelectionColor = color;
					else wordSelectionColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), wordSelectionColor.getAlpha());
					updateControls(false);
					DisplayConfigDialog.this.idmp.setSelectionHighlightColor(wordSelectionColor); // for preview
				}
			});
			this.wordSelectionAlpha = new JComboBox(wordSelectionAlphas);
			this.wordSelectionAlpha.setEditable(false);
			this.wordSelectionAlpha.setBorder(BorderFactory.createLoweredBevelBorder());
			this.wordSelectionAlpha.setPreferredSize(new Dimension(50, 21));
			this.wordSelectionAlpha.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					Object value = wordSelectionAlpha.getSelectedItem();
					if (value instanceof Integer) {
						int alpha = ((Integer) value).intValue();
						if (alpha == wordSelectionColor.getAlpha())
							return;
						wordSelectionColor = new Color(wordSelectionColor.getRed(), wordSelectionColor.getGreen(), wordSelectionColor.getBlue(), alpha);
						DisplayConfigDialog.this.idmp.setSelectionHighlightColor(wordSelectionColor); // for preview
					}
				}
			});
			JPanel wordSelectionAlphaPanel = new JPanel(new BorderLayout(), true);
			wordSelectionAlphaPanel.add(new JLabel("Alpha ", JLabel.RIGHT), BorderLayout.CENTER);
			wordSelectionAlphaPanel.add(this.wordSelectionAlpha, BorderLayout.EAST);
			JPanel wordSelectionPanel = new JPanel(new GridLayout(0, 1, 0, 4), true);
			wordSelectionPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createLineBorder(wordSelectionPanel.getBackground(), 2)));
			wordSelectionPanel.add(new JLabel("Word Selection Settings", JLabel.CENTER));
			wordSelectionPanel.add(this.wordSelectionColorButton);
			wordSelectionPanel.add(wordSelectionAlphaPanel);
			
			this.annotHighlightAlpha = this.idmp.getAnnotationHighlightAlpha();
			this.annotHighlightAlphaSlider = new JSlider(0, 0xFF, this.annotHighlightAlpha);
			this.annotHighlightAlphaSlider.setMajorTickSpacing(10);
			this.annotHighlightAlphaSlider.setPaintLabels(true);
			this.annotHighlightAlphaSlider.setPaintTicks(true);
			this.annotHighlightAlphaSlider.setPaintTrack(true);
			this.annotHighlightAlphaSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					Object value = annotHighlightAlphaSlider.getValue();
					if (value instanceof Integer) {
						int alpha = ((Integer) value).intValue();
						if (alpha == annotHighlightAlpha)
							return;
						annotHighlightAlpha = alpha;
						DisplayConfigDialog.this.idmp.setAnnotationHighlightAlpha(annotHighlightAlpha);
					}
				}
			});
			JPanel annotHighlightAlphaPanel = new JPanel(new BorderLayout(), true);
			annotHighlightAlphaPanel.add(new JLabel("Alpha Value for Annotation Highlights"), BorderLayout.WEST);
			annotHighlightAlphaPanel.add(this.annotHighlightAlphaSlider, BorderLayout.CENTER);
			
			//	populate and assemble control panel
			this.updateControls(true);
			JPanel selectionPanel = new JPanel(new GridLayout(1, 0, 4, 0), true);
			selectionPanel.add(boxSelectionPanel);
			selectionPanel.add(wordSelectionPanel);
			selectionPanel.setBorder(BorderFactory.createLineBorder(selectionPanel.getBackground(), 3));
			
			//	create and tray up buttons
			JButton ok = new JButton("OK");
			ok.setBorder(BorderFactory.createRaisedBevelBorder());
			ok.setPreferredSize(new Dimension(70, 21));
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					applySettings();
					dispose();
				}
			});
			JButton apply = new JButton("Apply");
			apply.setBorder(BorderFactory.createRaisedBevelBorder());
			apply.setPreferredSize(new Dimension(70, 21));
			apply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					applySettings();
				}
			});
			JButton reset = new JButton("Reset");
			reset.setBorder(BorderFactory.createRaisedBevelBorder());
			reset.setPreferredSize(new Dimension(70, 21));
			reset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					resetSettings();
				}
			});
			JButton defaults = new JButton("Defaults");
			defaults.setBorder(BorderFactory.createRaisedBevelBorder());
			defaults.setPreferredSize(new Dimension(70, 21));
			defaults.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					restoreDefaults();
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.setBorder(BorderFactory.createRaisedBevelBorder());
			cancel.setPreferredSize(new Dimension(70, 21));
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER), true);
			buttonPanel.add(ok);
			buttonPanel.add(apply);
			buttonPanel.add(reset);
			buttonPanel.add(defaults);
			buttonPanel.add(cancel);
			
			//	assemble whole shebang
			this.add(selectionPanel, BorderLayout.NORTH);
			this.add(annotHighlightAlphaPanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			//	configure dialog proper
			this.setSize(initialSize);
			if (initialPos == null)
				this.setLocationRelativeTo(idmp);
			else this.setLocation(initialPos);
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // need to make sure to properly reset target panel on closing
		}
		private void updateControls(boolean updateWorkingValues) {
			if (updateWorkingValues)
				this.boxSelectionColor = this.idmpBoxSelectionColor;
			Color fullBoxSelectionColor = ((this.boxSelectionColor.getAlpha() == 0xFF) ? this.boxSelectionColor : new Color(this.boxSelectionColor.getRed(), this.boxSelectionColor.getGreen(), this.boxSelectionColor.getBlue(), 0xFF));
			this.boxSelectionColorButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLineBorder(fullBoxSelectionColor, 2)));
			if (updateWorkingValues)
				this.boxSelectionThickness.setSelectedItem(Integer.valueOf(this.idmpBoxSelectionThickness));
			
			if (updateWorkingValues)
				this.wordSelectionColor = this.idmpWordSelectionColor;
			Color fullWordSelectionColor = ((this.wordSelectionColor.getAlpha() == 0xFF) ? this.wordSelectionColor : new Color(this.wordSelectionColor.getRed(), this.wordSelectionColor.getGreen(), this.wordSelectionColor.getBlue(), 0xFF));
			this.wordSelectionColorButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLineBorder(fullWordSelectionColor, 2)));
			this.wordSelectionAlpha.setSelectedItem(Integer.valueOf(this.wordSelectionColor.getAlpha()));
			
			if (updateWorkingValues)
				this.annotHighlightAlpha = this.idmpAnnotHighlightAlpha;
			this.annotHighlightAlphaSlider.setValue(this.annotHighlightAlpha);
		}
		void applySettings() {
			this.idmpBoxSelectionColor = this.boxSelectionColor;
			Object boxSelectionThickness = this.boxSelectionThickness.getSelectedItem();
			if (boxSelectionThickness instanceof Integer)
				this.idmpBoxSelectionThickness = ((Integer) boxSelectionThickness).intValue();
			this.idmpWordSelectionColor = this.wordSelectionColor;
			this.idmpAnnotHighlightAlpha = this.annotHighlightAlpha;
		}
		void resetSettings() {
			this.boxSelectionColor = this.idmpBoxSelectionColor;
			this.idmp.setSelectionBoxColor(this.idmpBoxSelectionColor);
			this.boxSelectionThickness.setSelectedItem(Integer.valueOf(this.idmpBoxSelectionThickness));
			this.idmp.setSelectionBoxThickness(this.idmpBoxSelectionThickness);
			this.wordSelectionColor = this.idmpWordSelectionColor;
			this.idmp.setSelectionHighlightColor(this.idmpWordSelectionColor);
			this.annotHighlightAlpha = this.idmpAnnotHighlightAlpha;
			this.idmp.setAnnotationHighlightAlpha(this.idmpAnnotHighlightAlpha);
			this.updateControls(false /* we just restored these values to the ones we preserved in the beginning */);
		}
		void restoreDefaults() {
			for (int n = 0; n < ImDocumentMarkupPanel.displayPropertyNames.length; n++)
				this.idmp.setDisplayProperty(ImDocumentMarkupPanel.displayPropertyNames[n], null);
			this.idmpBoxSelectionColor = this.idmp.getSelectionBoxColor();
			this.idmpBoxSelectionThickness = this.idmp.getSelectionBoxThickness();
			this.idmpWordSelectionColor = this.idmp.getSelectionHighlightColor();
			this.idmpAnnotHighlightAlpha = this.idmp.getAnnotationHighlightAlpha();
			this.updateControls(true);
		}
		public void dispose() {
			this.idmp.setSelectionBoxColor(this.idmpBoxSelectionColor);
			this.idmp.setSelectionBoxThickness(this.idmpBoxSelectionThickness);
			this.idmp.setSelectionHighlightColor(this.idmpWordSelectionColor);
			this.idmp.setAnnotationHighlightAlpha(this.idmpAnnotHighlightAlpha);
			initialSize = this.getSize();
			initialPos = this.getLocation();
			super.dispose();
		}
	}
	
	public static long configureImfStorageFlags(long storageFlags, String title) {
		StorageFlagConfigurationPanel sfcp = new StorageFlagConfigurationPanel(storageFlags);
		int choice = JOptionPane.showConfirmDialog(DialogPanel.getTopWindow(), sfcp, title, JOptionPane.OK_CANCEL_OPTION);
		return ((choice == JOptionPane.OK_OPTION) ? sfcp.getStorageFlags() : -1);
	}
	
	private static class StorageFlagConfigurationPanel extends JPanel implements ItemListener {
		private JCheckBox useTsvMode = new JCheckBox("Use TSV storage mode?");
		private JCheckBox usePageAttributeColumns = new JCheckBox("Use attribute columns for pages?");
		private JCheckBox useSupplementAttributeColumns = new JCheckBox("Use attribute columns for supplements?");
		private JCheckBox bundleSupplementGraphicsData = new JCheckBox("Bundle up graphics supplements?");
		
		private JCheckBox useWordAttributeColumns = new JCheckBox("Use attribute columns for words?");
		private JLabel wordChunkSizeLabel = new JLabel("Target size of word table chunks: ", JLabel.RIGHT);
		private JComboBox wordChunkSize;
		
		private JCheckBox useRegionAttributeColumns = new JCheckBox("Use attribute columns for regions?");
		private JCheckBox externalizeRegionAttributes = new JCheckBox("Externalize long region attribute values?");
		private JLabel externalRegionTypeThresholdLabel = new JLabel("Minimum number of regions for type specific table: ", JLabel.RIGHT);
		private JComboBox externalRegionTypeThreshold;
		
		private JCheckBox useAnnotationAttributeColumns = new JCheckBox("Use attribute columns for annotations?");
		private JCheckBox externalizeAnnotationAttributes = new JCheckBox("Externalize long annotation attribute values?");
		private JLabel externalAnnotationTypeThresholdLabel = new JLabel("Minimum number of annotations for type specific table: ", JLabel.RIGHT);
		private JComboBox externalAnnotationTypeThreshold;
		
		private JTextField storageFlagPreview = new JTextField("");
		
		StorageFlagConfigurationPanel(long storageFlags) {
//			super(new GridLayout(0, 4, 3, 3), true);
			super(new GridLayout(0, 2, 3, 3), true);
			
			//	create and populate drop-downs
			ArrayList wcsList = new ArrayList();
			wcsList.add(new LabeledInt(-1, "<do not chunk word table>"));
			wcsList.add(new LabeledInt(0, ("<use default size (" + ImDocumentIO.STORAGE_MODE_TSV_DEFAULT_WORD_CHUNK_SIZE + ")>")));
			for (int wcs = 0x0100; wcs < 0xFFFF; wcs += 0x0100)
				wcsList.add(new LabeledInt(wcs, (wcs + " words per chunks")));
			LabeledInt[] wcss = ((LabeledInt[]) wcsList.toArray(new LabeledInt[wcsList.size()]));
			this.wordChunkSize = new JComboBox(wcss);
			this.wordChunkSize.setEditable(false);
			this.wordChunkSize.setBorder(BorderFactory.createLoweredBevelBorder());
			
			ArrayList erttList = new ArrayList();
			erttList.add(new LabeledInt(-1, "<do not use type specific region tables>"));
			erttList.add(new LabeledInt(0, ("<use default threshold (" + ImDocumentIO.STORAGE_MODE_TSV_DEFAULT_EXTERNAL_REGION_THRESHOLD + ")>")));
			for (int ertt = 0x0010; ertt < 0x0FFF; ertt += 0x0010)
				erttList.add(new LabeledInt(ertt, (ertt + " regions of same type")));
			LabeledInt[] ertts = ((LabeledInt[]) erttList.toArray(new LabeledInt[erttList.size()]));
			this.externalRegionTypeThreshold = new JComboBox(ertts);
			this.externalRegionTypeThreshold.setEditable(false);
			this.externalRegionTypeThreshold.setBorder(BorderFactory.createLoweredBevelBorder());
			
			ArrayList eattList = new ArrayList();
			eattList.add(new LabeledInt(-1, "<do not use type specific annotation tables>"));
			eattList.add(new LabeledInt(0, ("<use default threshold (" + ImDocumentIO.STORAGE_MODE_TSV_DEFAULT_EXTERNAL_ANNOTATION_THRESHOLD + ")>")));
			for (int eatt = 0x0010; eatt < 0x0FFF; eatt += 0x0010)
				eattList.add(new LabeledInt(eatt, (eatt + " annotations of same type")));
			LabeledInt[] eatts = ((LabeledInt[]) eattList.toArray(new LabeledInt[eattList.size()]));
			this.externalAnnotationTypeThreshold = new JComboBox(eatts);
			this.externalAnnotationTypeThreshold.setEditable(false);
			this.externalAnnotationTypeThreshold.setBorder(BorderFactory.createLoweredBevelBorder());
			
			this.storageFlagPreview.setEditable(false);
			this.storageFlagPreview.setBorder(BorderFactory.createLoweredBevelBorder());
			
			//	initialize input fields
			this.useTsvMode.setSelected((storageFlags & ImDocumentIO.STORAGE_MODE_TSV) != 0);
			
			this.usePageAttributeColumns.setSelected(ImDocumentIO.usePageAttributeColumns(storageFlags));
			this.useSupplementAttributeColumns.setSelected(ImDocumentIO.useSupplementAttributeColumns(storageFlags));
			this.bundleSupplementGraphicsData.setSelected(ImDocumentIO.bundleSupplementGraphicsData(storageFlags));
			
			this.useWordAttributeColumns.setSelected(ImDocumentIO.useWordAttributeColumns(storageFlags));
			if (ImDocumentIO.useWordChunks(storageFlags))
				this.wordChunkSize.setSelectedItem(new LabeledInt(ImDocumentIO.getWordChunkSize(storageFlags)));
			else this.wordChunkSize.setSelectedItem(new LabeledInt(-1));
			
			this.useRegionAttributeColumns.setSelected(ImDocumentIO.useRegionAttributeColumns(storageFlags));
			this.externalizeRegionAttributes.setSelected(ImDocumentIO.externalizeRegionAttributeValues(storageFlags));
			if (ImDocumentIO.useRegionTypeSpecificEntries(storageFlags))
				this.externalRegionTypeThreshold.setSelectedItem(new LabeledInt(ImDocumentIO.getRegionTypeEntrySizeThreshold(storageFlags)));
			else this.externalRegionTypeThreshold.setSelectedItem(new LabeledInt(-1));
			
			this.useAnnotationAttributeColumns.setSelected(ImDocumentIO.useAnnotationAttributeColumns(storageFlags));
			this.externalizeAnnotationAttributes.setSelected(ImDocumentIO.externalizeAnnotationAttributeValues(storageFlags));
			if (ImDocumentIO.useAnnotationTypeSpecificEntries(storageFlags))
				this.externalAnnotationTypeThreshold.setSelectedItem(new LabeledInt(ImDocumentIO.getAnnotationTypeEntrySizeThreshold(storageFlags)));
			else this.externalAnnotationTypeThreshold.setSelectedItem(new LabeledInt(-1));
			
			this.updateStorageFlagPreview();
			
			//	wire up change listener
			this.useTsvMode.addItemListener(this);
			this.useTsvMode.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					checkInputFieldsEnabled();
				}
			});
			
			this.usePageAttributeColumns.addItemListener(this);
			this.useSupplementAttributeColumns.addItemListener(this);
			this.bundleSupplementGraphicsData.addItemListener(this);
			
			this.useWordAttributeColumns.addItemListener(this);
			this.wordChunkSize.addItemListener(this);
			
			this.useRegionAttributeColumns.addItemListener(this);
			this.externalizeRegionAttributes.addItemListener(this);
			this.externalRegionTypeThreshold.addItemListener(this);
			
			this.useAnnotationAttributeColumns.addItemListener(this);
			this.externalizeAnnotationAttributes.addItemListener(this);
			this.externalAnnotationTypeThreshold.addItemListener(this);
			
			this.checkInputFieldsEnabled();
			
			//	assemble whole shebang
			this.add(this.useTsvMode);
			this.add(new JLabel()); // need a spacer there, as we're using grid layout
			
			this.add(this.useSupplementAttributeColumns);
			this.add(this.bundleSupplementGraphicsData);
			
			this.add(this.usePageAttributeColumns);
			this.add(new JLabel()); // need a spacer there, as we're using grid layout
			
			this.add(this.useWordAttributeColumns);
			this.add(new JLabel()); // need a spacer there, as we're using grid layout
			this.add(this.wordChunkSizeLabel);
			this.add(this.wordChunkSize);
			
			this.add(this.useRegionAttributeColumns);
			this.add(this.externalizeRegionAttributes);
			this.add(this.externalRegionTypeThresholdLabel);
			this.add(this.externalRegionTypeThreshold);
			
			this.add(this.useAnnotationAttributeColumns);
			this.add(this.externalizeAnnotationAttributes);
			this.add(this.externalAnnotationTypeThresholdLabel);
			this.add(this.externalAnnotationTypeThreshold);
			
//			this.add(new JLabel()); // need a spacer there, as we're using grid layout
			this.add(new JLabel("Current storage flags: ", JLabel.RIGHT)); // need a spacer there, as we're using grid layout
			this.add(this.storageFlagPreview);
//			this.add(new JLabel()); // need a spacer there, as we're using grid layout
		}
		
		void checkInputFieldsEnabled() {
			boolean enabled = this.useTsvMode.isSelected();
			this.usePageAttributeColumns.setEnabled(enabled);
			this.useSupplementAttributeColumns.setEnabled(enabled);
			this.bundleSupplementGraphicsData.setEnabled(enabled);
			
			this.useWordAttributeColumns.setEnabled(enabled);
			this.wordChunkSizeLabel.setEnabled(enabled);
			this.wordChunkSize.setEnabled(enabled);
			
			this.useRegionAttributeColumns.setEnabled(enabled);
			this.externalizeRegionAttributes.setEnabled(enabled);
			this.externalRegionTypeThresholdLabel.setEnabled(enabled);
			this.externalRegionTypeThreshold.setEnabled(enabled);
			
			this.useAnnotationAttributeColumns.setEnabled(enabled);
			this.externalizeAnnotationAttributes.setEnabled(enabled);
			this.externalAnnotationTypeThresholdLabel.setEnabled(enabled);
			this.externalAnnotationTypeThreshold.setEnabled(enabled);
		}
		
		/**
		 * Retrieve the currently configured storage flag vector.
		 * @return the storage flag vector
		 */
		public long getStorageFlags() {
			if (!this.useTsvMode.isSelected())
				return ImDocumentIO.STORAGE_MODE_CSV;
			long storageFlags = ImDocumentIO.STORAGE_MODE_TSV;
			
			storageFlags = ImDocumentIO.setUsePageAttributeColumns(storageFlags, this.usePageAttributeColumns.isSelected());
			storageFlags = ImDocumentIO.setUseSupplementAttributeColumns(storageFlags, this.useSupplementAttributeColumns.isSelected());
			storageFlags = ImDocumentIO.setBundleSupplementGraphicsData(storageFlags, this.bundleSupplementGraphicsData.isSelected());
			
			storageFlags = ImDocumentIO.setUseWordAttributeColumns(storageFlags, this.useWordAttributeColumns.isSelected());
			LabeledInt wordChunkSize = ((LabeledInt) this.wordChunkSize.getSelectedItem());
			storageFlags = ImDocumentIO.setUseWordChunks(storageFlags, (wordChunkSize.value != -1));
			storageFlags = ImDocumentIO.setWordChunkSize(storageFlags, Math.max(wordChunkSize.value, 0));
			
			storageFlags = ImDocumentIO.setUseRegionAttributeColumns(storageFlags, this.useRegionAttributeColumns.isSelected());
			storageFlags = ImDocumentIO.setExternalizeRegionAttributeValues(storageFlags, this.externalizeRegionAttributes.isSelected());
			LabeledInt externalRegionTypeThreshold = ((LabeledInt) this.externalRegionTypeThreshold.getSelectedItem());
			storageFlags = ImDocumentIO.setUseRegionTypeSpecificEntries(storageFlags, (externalRegionTypeThreshold.value != -1));
			storageFlags = ImDocumentIO.setRegionTypeEntrySizeThreshold(storageFlags, Math.max(externalRegionTypeThreshold.value, 0));
			
			storageFlags = ImDocumentIO.setUseAnnotationAttributeColumns(storageFlags, this.useAnnotationAttributeColumns.isSelected());
			storageFlags = ImDocumentIO.setExternalizeAnnotationAttributeValues(storageFlags, this.externalizeAnnotationAttributes.isSelected());
			LabeledInt externalAnnotationTypeThreshold = ((LabeledInt) this.externalAnnotationTypeThreshold.getSelectedItem());
			storageFlags = ImDocumentIO.setUseAnnotationTypeSpecificEntries(storageFlags, (externalAnnotationTypeThreshold.value != -1));
			storageFlags = ImDocumentIO.setAnnotationTypeEntrySizeThreshold(storageFlags, Math.max(externalAnnotationTypeThreshold.value, 0));
			
			return storageFlags;
		}
		
		public void itemStateChanged(ItemEvent ie) {
			this.updateStorageFlagPreview();
		}
		private void updateStorageFlagPreview() {
			String storageFlags = Long.toString(this.getStorageFlags(), 16).toUpperCase();
			while (storageFlags.length() < 16)
				storageFlags = ("0" + storageFlags);
			this.storageFlagPreview.setText("0x" + storageFlags);
		}
		
		private static class LabeledInt {
			final int value;
			final String label;
			LabeledInt(int value) {
				this(value, ("" + value));
			}
			LabeledInt(int value, String label) {
				this.value = value;
				this.label = label;
			}
			public int hashCode() {
				return this.value;
			}
			public boolean equals(Object obj) {
				return ((obj instanceof LabeledInt) && (((LabeledInt) obj).value == this.value));
			}
			public String toString() {
				return this.label;
			}
			
		}
	}
	
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		configureImfStorageFlags(ImDocumentIO.STORAGE_MODE_CSV, "Configure IMF Storage Flags");
	}
}
