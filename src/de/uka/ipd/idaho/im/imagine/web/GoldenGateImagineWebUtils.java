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
package de.uka.ipd.idaho.im.imagine.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.gamta.util.imaging.BoundingBox;
import de.uka.ipd.idaho.htmlXmlUtil.Parser;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost;
import de.uka.ipd.idaho.htmlXmlUtil.grammars.Html;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImRegion;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.util.ImUtils;

/**
 * Utility library for GoldenGATE Imagine online.
 * 
 * @author sautter
 */
public class GoldenGateImagineWebUtils {
	
	/**
	 * An output stream that will not pass through a <code>flush()</code> to
	 * its wrapped stream, to prevent HTTP response output streams from being
	 * flushed prematurely.
	 * 
	 * @author sautter
	 */
	public static class IsolatorOutputStream extends FilterOutputStream {
		public IsolatorOutputStream(OutputStream out) {
			super(out);
		}
		public void flush() throws IOException {}
	}
	
	/**
	 * Escape a string for JavaScript and JSON use.
	 * @param str the string to escape
	 * @return the escaped string
	 */
	public static String escapeForJavaScript(String str) {
		if (str == null)
			return null;
		StringBuffer escaped = new StringBuffer();
		char ch;
		for (int c = 0; c < str.length(); c++) {
			ch = str.charAt(c);
			if ((ch == '\\') || (ch == '\''))
				escaped.append('\\');
			if (ch < 32)
				escaped.append(' ');
			else escaped.append(ch);
		}
		return escaped.toString();
	}
	
	/**
	 * HTML page builder creating an attribute editor for a given attributed
	 * object. The style definitions for the individual classes have to be
	 * provided by the HTML file the form is included in. Thus, client code
	 * should make sure to always use the same HTML file in calls upon this
	 * class that come from the same application. The suggested way is to use
	 * the <code>findFile()</code> method of the argument page builder host to
	 * locate that file.<br>
	 * Whichever way the HTML file is located, this class expects an
	 * 'includeBody' marker tag in the location where the attribute editor form
	 * is to be added.
	 * 
	 * @author sautter
	 */
	public static abstract class AttributeEditorPageBuilder extends HtmlPageBuilder {
		private Attributed target;
		private String targetId;
		private Attributed[] targetContext;
		private String submitUrl;
		
		/** Constructor
		 * @param host the page builder host to work with
		 * @param request the HTTP request to respond to
		 * @param response the HTTP response to write to
		 * @param target the attributed object to edit
		 * @param submitUrl the URL to submit the edit result to
		 * @throws IOException
		 */
		protected AttributeEditorPageBuilder(HtmlPageBuilderHost host, HttpServletRequest request, HttpServletResponse response, Attributed target, String submitUrl) throws IOException {
			this(host, request, response, target, getAttributedId(target), submitUrl);
		}
		
		/** Constructor
		 * @param host the page builder host to work with
		 * @param request the HTTP request to respond to
		 * @param response the HTTP response to write to
		 * @param target the attributed object to edit
		 * @param targetId an identifier for the attributed object to edit
		 * @param submitUrl the URL to submit the edit result to
		 * @throws IOException
		 */
		public AttributeEditorPageBuilder(HtmlPageBuilderHost host, HttpServletRequest request, HttpServletResponse response, Attributed target, String targetId, String submitUrl) throws IOException {
			super(host, request, response);
			this.target = target;
			this.targetId = targetId;
			this.targetContext = this.getContext(target);
			this.submitUrl = submitUrl;
		}
		
		/**
		 * This implementation catches the <code>includeBody</code> marker tag.
		 * Sub classes wanting to write their own extensions to the page body
		 * may overwrite this method and catch the same marker tag, but have to
		 * make the super call for the resulting page to work.
		 * @see de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder#include(java.lang.String, java.lang.String)
		 */
		protected void include(String type, String tag) throws IOException {
			if ("includeBody".equals(type)) {
				this.writeLine("<div class=\"editAttributesMain\">");
				
				this.writeLine("<div class=\"editAttributesTitle\">" + html.escape(this.getTitle(this.target)) + "</div>");
				
				this.writeLine("<div id=\"attributeTable\" class=\"editAttributesTable\" style=\"overflow: auto;\"></div>");
				
				this.writeLine("<div class=\"editAttributesFields\"><div class=\"editAttributesFieldRow\">");
				this.writeLine("<input type=\"text\" id=\"attributeNameField\" placeholder=\"&lt;Enter Attribute Name&gt;\" list=\"attributeNames\" onkeyup=\"return catchReturnKeyInAttributeName(event);\" />");
				this.writeLine("<datalist id=\"attributeNames\">");
				if (this.targetContext != null) {
					TreeSet ans = new TreeSet();
					for (int c = 0; c < this.targetContext.length; c++)
						ans.addAll(Arrays.asList(this.targetContext[c].getAttributeNames()));
					for (Iterator anit = ans.iterator(); anit.hasNext();)
						this.writeLine("<option value=\"" + html.escape((String) anit.next()) + "\" />");
				}
				this.writeLine("</datalist>");
				this.writeLine("<button class=\"attributeAddButton\" onclick=\"return addDataAttribute();\">Add / Set Attribute</button>");
				this.writeLine("</div><div class=\"editAttributesFieldRow\">");
				this.writeLine("<input type=\"text\" id=\"attributeValueField\" placeholder=\"&lt;Enter Attribute Value&gt;\" list=\"attributeValues\" onfocus=\"updateAttributeValueSuggestions();\" onkeyup=\"return catchReturnKeyInAttributeValue(event);\" />");
				this.writeLine("<datalist id=\"attributeValues\"></datalist>");
				this.writeLine("</div></div>");
				
				SubmitButton[] buttons = this.getButtons();
				this.writeLine("<div class=\"editAttributesButtons\">");
				for (int b = 0; b < buttons.length; b++)
					this.writeLine("<button class=\"attributeMainButton\"" + ((buttons[b].tooltip == null) ? "" : (" title=\"" + html.escape(buttons[b].tooltip) + "\"")) + " onclick=\"" + buttons[b].jsCall + "\">" + html.escape(buttons[b].label) + "</button>");
				this.writeLine("</div>");
				
				this.writeLine("</div>");
				
				
				this.writeLine("<form id=\"attributeForm\" method=\"POST\" action=\"" + this.submitUrl +  "\" style=\"display: none;\">");
				this.writeLine("<input type=\"hidden\" name=\"id\" value=\"" + this.targetId + "\"/>");
				this.writeLine("</form>");
				
				
				this.writeLine("<script type=\"text/javascript\">");
				String[] ans = target.getAttributeNames();
				for (int n = 0; n < ans.length; n++) {
					Object av = target.getAttribute(ans[n]);
					if (av != null)
						this.writeLine("setDataAttribute('" + ans[n] + "', '" + GoldenGateImagineWebUtils.escapeForJavaScript(av.toString()) + "');");
				}
				this.writeLine("</script>");
			}
			else super.include(type, tag);
		}
		
		protected final boolean includeJavaScriptDomHelpers() {
			return true;
		}
		
		/**
		 * This implementation writes the JavaScript functions backing the
		 * attribute editor. Sub classes overwriting this method to add their
		 * own functions thus have to make the super call.
		 */
		protected void writePageHeadExtensions() throws IOException {
			this.writeLine("<script type=\"text/javascript\">");
			this.writeLine("var attributesById = new Object();");
			this.writeLine("var attributes = new Array();");
			
			//	add/set an attribute, assuming the name to be valid
			this.writeLine("function setDataAttribute(name, value) {");
			this.writeLine("  var attr = attributesById[name];");
			this.writeLine("  if (!attr || (attr == null)) {");
			this.writeLine("    attr = new Object();");
			this.writeLine("    attr.name = name;");
			this.writeLine("    attr.value = value;");
			this.writeLine("    attributesById[name] = attr;");
			this.writeLine("    var atRow = newElement('div', null, 'attributeTableRow', null);");
			this.writeLine("    atRow.onclick = function() {");
			this.writeLine("      activateDataAttribute(name);");
			this.writeLine("    }");
			this.writeLine("    var atRemove = newElement('button', null, 'attributeRemoveButton', 'X');");
			this.writeLine("    atRemove.title = ('Remove attribute \\'' + name + '\\'');");
			this.writeLine("    atRemove.onclick = function() {");
			this.writeLine("      removeDataAttribute(name);");
			this.writeLine("    };");
			this.writeLine("    atRow.appendChild(atRemove);");
			this.writeLine("    var atName = newElement('span', null, 'attributeName', name);");
			this.writeLine("    atRow.appendChild(atName);");
			this.writeLine("    var atValue = newElement('span', null, 'attributeValue', value);");
			this.writeLine("    atRow.appendChild(atValue);");
			this.writeLine("    attr.atRow = atRow;");
			this.writeLine("    attr.atValue = atValue;");
			this.writeLine("    for (var a = 0; a < attributes.length; a++)");
			this.writeLine("      if (attributes[a].name.localeCompare(name) > 0) {");
			this.writeLine("        var aAttr = attributes[a];");
			this.writeLine("        attributes.splice(a, 0, attr);");
			this.writeLine("        aAttr.atRow.parentNode.insertBefore(attr.atRow, aAttr.atRow);");
			this.writeLine("        attr = null;");
			this.writeLine("        break;");
			this.writeLine("      }");
			this.writeLine("    if (attr != null) {");
			this.writeLine("      attributes[attributes.length] = attr;");
			this.writeLine("      getById('attributeTable').appendChild(attr.atRow);");
			this.writeLine("    }");
			this.writeLine("  }");
			this.writeLine("  else {");
			this.writeLine("    attr.value = value;");
			this.writeLine("    while (attr.atValue.firstChild)");
			this.writeLine("      attr.atValue.removeChild(attr.atValue.firstChild);");
			this.writeLine("    attr.atValue.appendChild(document.createTextNode(value));");
			this.writeLine("  }");
			this.writeLine("}");
			
			//	remove an attribute
			this.writeLine("function removeDataAttribute(name) {");
			this.writeLine("  var attr = attributesById[name];");
			this.writeLine("  if (attr == null)");
			this.writeLine("    return;");
			this.writeLine("  delete attributesById[name];");
			this.writeLine("  for (var a = 0; a < attributes.length; a++)");
			this.writeLine("    if (attributes[a].name == name) {");
			this.writeLine("      attributes.splice(a, 1);");
			this.writeLine("      break;");
			this.writeLine("    }");
			this.writeLine("  removeElement(attr.atRow);");
			this.writeLine("}");
			
			//	select an attribute for editing
			this.writeLine("function activateDataAttribute(name) {");
			this.writeLine("  var attr = attributesById[name];");
			this.writeLine("  if (attr) {");
			this.writeLine("    getById('attributeNameField').value = attr.name;");
			this.writeLine("    getById('attributeValueField').value = attr.value;");
			this.writeLine("  }");
			this.writeLine("  getById('attributeValueField').focus();");
			this.writeLine("}");
			
			//	newly add an attribute from the input fields
			this.writeLine("function addDataAttribute() {");
			this.writeLine("  if (!checkAttributeName(true))");
			this.writeLine("    return false;");
			this.writeLine("  var nameInput = getById('attributeNameField');");
			this.writeLine("  var valueInput = getById('attributeValueField');");
			this.writeLine("  var name = nameInput.value;");
			this.writeLine("  nameInput.value = '';");
			this.writeLine("  var value = valueInput.value;");
			this.writeLine("  valueInput.value = '';");
			this.writeLine("  if ((value == null) || (value.length == 0))");
			this.writeLine("    removeDataAttribute(name);");
			this.writeLine("  else setDataAttribute(name, value);");
			this.writeLine("  getById('attributeNameField').focus();");
			this.writeLine("  return false;");
			this.writeLine("}");
			
			//	catch return key in attribute name to move to attribute value input
			this.writeLine("function catchReturnKeyInAttributeName(event) {");
			this.writeLine("  if (event.keyCode == 13) {");
			this.writeLine("    event.stopPropagation();");
			this.writeLine("    if (!checkAttributeName(true))");
			this.writeLine("      return false;");
			this.writeLine("    var name = getById('attributeNameField').value;");
			this.writeLine("    if (attributesById[name])");
			this.writeLine("      getById('attributeValueField').value = attributesById[name].value;");
			this.writeLine("    else getById('attributeValueField').value = '';");
			this.writeLine("    getById('attributeValueField').focus();");
			this.writeLine("    return false;");
			this.writeLine("  }");
			this.writeLine("  else return true;");
			this.writeLine("}");
			
			//	check if an attribute name is valid
			this.writeLine("function checkAttributeName(focusNameField) {");
			this.writeLine("  var nameInput = getById('attributeNameField');");
			this.writeLine("  var name = nameInput.value;");
			this.writeLine("  if (name == null) {");
			this.writeLine("    if (focusNameField)");
			this.writeLine("      getById('attributeNameField').focus();");
			this.writeLine("    return false;");
			this.writeLine("  }");
			this.writeLine("  name = name.trim();");
			this.writeLine("  if (name.length == 0) {");
			this.writeLine("    if (focusNameField)");
			this.writeLine("      getById('attributeNameField').focus();");
			this.writeLine("    return false;");
			this.writeLine("  }");
			this.writeLine("  if (!/^[a-zA-Z\\-\\_\\.][a-zA-Z0-9\\-\\_\\.\\:]*$/i.test(name)) {");
			this.writeLine("    showAlertDialog(('\\'' + name + '\\' is not a valid attribute name'), 'Invalid Attribute Name', 0);");
			this.writeLine("    if (focusNameField)");
			this.writeLine("      getById('attributeNameField').focus();");
			this.writeLine("    return false;");
			this.writeLine("  }");
			this.writeLine("  return true;");
			this.writeLine("}");
			
			//	update selectable attribute values
			this.writeLine("function updateAttributeValueSuggestions() {");
			this.writeLine("  var attrValueList = getById('attributeValues');");
			this.writeLine("  while (attrValueList.firstElementChild)");
			this.writeLine("    attrValueList.removeChild(attrValueList.firstElementChild);");
			this.writeLine("  var attrValues = attributeValuesById[getById('attributeNameField').value];");
			this.writeLine("  if (attrValues == null)");
			this.writeLine("    return;");
			this.writeLine("  for (var v = 0; v < attrValues.length; v++) {");
			this.writeLine("    var attrValueOption = newElement('option', null, null, null);");
			this.writeLine("    attrValueOption.value = attrValues[v];");
			this.writeLine("    attrValueList.appendChild(attrValueOption);");
			this.writeLine("  }");
			this.writeLine("}");
			
			//	catch return key in attribute value to set attribute
			this.writeLine("function catchReturnKeyInAttributeValue(event) {");
			this.writeLine("  if (event.keyCode == 13) {");
			this.writeLine("    event.stopPropagation();");
			this.writeLine("    addDataAttribute();");
			this.writeLine("    return false;");
			this.writeLine("  }");
			this.writeLine("  else return true;");
			this.writeLine("}");
			
			//	submit attribute form
			this.writeLine("function submitDataAttributes() {");
			this.writeLine("  var attrForm = getById('attributeForm');");
			//this.writeLine("  var params = '';");
			this.writeLine("  for (var a = 0; a < attributes.length; a++) {");
			this.writeLine("    var attrField = newElement('input', null, null, null);");
			this.writeLine("    attrField.type = 'hidden';");
			this.writeLine("    attrField.name = ('ATTR_' + attributes[a].name);");
			this.writeLine("    attrField.value = attributes[a].value;");
			this.writeLine("    attrForm.appendChild(attrField);");
			//this.writeLine("    params += ('&ATTR_' + attributes[a].name + '=' + attributes[a].value);");
			this.writeLine("  }");
			//this.writeLine("  alert(params);");
			this.writeLine("  attrForm.submit();");
			this.writeLine("}");
			
			//	open script and create attribute value index
			this.writeLine("var attributeValuesById = new Object();");
			
			//	collect attribute values and index arrays by names
			if (this.targetContext != null) {
				
				//	index attribute values by name
				TreeMap anvs = new TreeMap();
				for (int c = 0; c < this.targetContext.length; c++) {
					String[] ans = this.targetContext[c].getAttributeNames();
					for (int n = 0; n < ans.length; n++) {
						Object av = this.targetContext[c].getAttribute(ans[n]);
						if (av == null)
							continue;
						TreeSet avs = ((TreeSet) anvs.get(ans[n]));
						if (avs == null) {
							avs = new TreeSet();
							anvs.put(ans[n], avs);
						}
						avs.add(av.toString());
					}
				}
				
				//	map attribute names to value arrays
				for (Iterator anit = anvs.keySet().iterator(); anit.hasNext();) {
					String an = ((String) anit.next());
					this.write("attributeValuesById['" + an + "'] = [");
					TreeSet avs = ((TreeSet) anvs.get(an));
					for (Iterator avit = avs.iterator(); avit.hasNext();) {
						this.write("'" + GoldenGateImagineWebUtils.escapeForJavaScript((String) avit.next()) + "'");
						if (avit.hasNext())
							this.write(", ");
					}
					this.writeLine("];");
				}
			}
			
			//	close script
			this.writeLine("</script>");
		}
		
		/**
		 * Retrieve the context objects for the attributed object to edit. The
		 * attributes of the context objects provide suggestions and auto-fill
		 * functionality. This default implementation loops through to the
		 * <code>getAttributedContext()</code> method, sub classes are welcome
		 * to overwrite it as needed. 
		 * @param target the attributed object being edited
		 * @return an array holding the context objects
		 */
		protected Attributed[] getContext(Attributed target) {
			return getAttributedContext(target);
		}
		
		/**
		 * Retrieve the title for an attribute editor form, giving some hint to
		 * the nature of the attributed object under editing. This default
		 * implementation loops through to the
		 * <code>getAttributeEditorTitle()</code> method, sub classes are
		 * welcome to overwrite it as needed. 
		 * @param target the attributed object being edited
		 */
		protected String getTitle(Attributed target) {
			return getAttributeEditorTitle(target);
		}
		
		/**
		 * Retrieve the buttons submitting the attribute editor form, or
		 * closing its surrounding HTML page in some other way. This default
		 * implementation returns three buttons: 'OK', 'Cancel', and 'Reset',
		 * with the latter simply reloading the attribute editor. Sub classes
		 * may provide a different selection of buttons.
		 * @return an array holding the buttons
		 */
		protected SubmitButton[] getButtons() {
			SubmitButton[] fsbs = {
				new SubmitButton("OK", "submitDataAttributes();"),
				new SubmitButton("Cancel", "window.close();"),
				new SubmitButton("Reset", "window.location.reload();")
			};
			return fsbs;
		}
		
		/**
		 * Abstract description of a button submitting the attribute editor
		 * form, consisting of a button text (label), an optional button title
		 * (tooltip), and a JavaScript call to make when the button is clicked.
		 * The latter has to close the attribute editor in some way, either by
		 * directly closing the window (<code>window.close()</code>), or by
		 * submitting the form. The default way for the latter is calling the
		 * built-in <code>submitDataAttributes()</code> function, but sub
		 * classes may also provide and call their own functions to implement
		 * additional functionality.
		 * 
		 * @author sautter
		 */
		public static class SubmitButton {
			final String label;
			final String tooltip;
			final String jsCall;
			
			/**
			 * @param label
			 * @param jsCall
			 */
			public SubmitButton(String label, String jsCall) {
				this(label, null, jsCall);
			}
			
			/**
			 * @param label
			 * @param tooltip
			 * @param jsCall
			 */
			public SubmitButton(String label, String tooltip, String jsCall) {
				this.label = label;
				this.tooltip = tooltip;
				this.jsCall = jsCall;
			}
		}
	};
	
//	public static HtmlPageBuilder getAttributeEditorPageBuilder(HtmlPageBuilderHost host, HttpServletRequest request, HttpServletResponse response, final String submitUrl, final Attributed target, final Attributed[] context, final String title) throws IOException {
//		return new HtmlPageBuilder(host, request, response) {
////			private Attributed[] targetContext = getAttributedContext(target);
//			protected void include(String type, String tag) throws IOException {
//				
//				//	TODO_ make this a simple includeBody, including the body of editAttributes.html
//				
//				//	TODO_ figure out where to (_configurably_) put all the styles, preferably in a central location
//				
//				//	TODO_ add submit value variable to form
//				
//				if ("includeTitle".equals(type))
////					this.write(html.escape(getEditAttributesTitle(target)));
//					this.write(html.escape(title));
//				else if ("includeForm".equals(type)) {
////					this.writeLine("<form id=\"attributeForm\" method=\"POST\" action=\"" + this.request.getContextPath() + this.request.getServletPath() + "/" + id + "/editAttributes\" style=\"display: none;\">");
//					this.writeLine("<form id=\"attributeForm\" method=\"POST\" action=\"" + submitUrl +  "\" style=\"display: none;\">");
//					this.writeLine("<input type=\"hidden\" name=\"id\" value=\"" + this.request.getParameter("id") + "\"/>");
//					this.writeLine("</form>");
//				}
//				else if ("includeAttributeNames".equals(type)) {
////					if (this.targetContext == null)
//					if (context == null)
//						return;
//					TreeSet ans = new TreeSet();
////					for (int c = 0; c < this.targetContext.length; c++)
////						ans.addAll(Arrays.asList(this.targetContext[c].getAttributeNames()));
//					for (int c = 0; c < context.length; c++)
//						ans.addAll(Arrays.asList(context[c].getAttributeNames()));
//					for (Iterator anit = ans.iterator(); anit.hasNext();)
//						this.writeLine("<option value=\"" + html.escape((String) anit.next()) + "\" />");
//				}
//				else if ("includeInitCalls".equals(type)) {
//					this.writeLine("<script type=\"text/javascript\">");
//					String[] ans = target.getAttributeNames();
//					for (int n = 0; n < ans.length; n++) {
//						Object av = target.getAttribute(ans[n]);
//						if (av != null)
//							this.writeLine("setDataAttribute('" + ans[n] + "', '" + GoldenGateImagineEditorServlet.escapeForJavaScript(av.toString()) + "');");
//					}
//					this.writeLine("</script>");
//				}
//				else super.include(type, tag);
//			}
//			protected boolean includeJavaScriptDomHelpers() {
//				return true;
//			}
//			protected void writePageHeadExtensions() throws IOException {
//				
//				//	TODO_ move all the functions from editAttributes.html over here
//				
//				//	TODO_ add submitAttributeEditorForm() function, filling and submitting the form
//				
//				//	open script and create attribute value index
//				this.writeLine("<script type=\"text/javascript\">");
//				this.writeLine("var attributeValuesById = new Object();");
//				
//				//	collect attribute values and index arrays by names
////				if (this.targetContext != null) {
//				if (context != null) {
//					
//					//	index attribute values by name
//					TreeMap anvs = new TreeMap();
////					for (int c = 0; c < this.targetContext.length; c++) {
//					for (int c = 0; c < context.length; c++) {
////						String[] ans = this.targetContext[c].getAttributeNames();
//						String[] ans = context[c].getAttributeNames();
//						for (int n = 0; n < ans.length; n++) {
////							Object av = this.targetContext[c].getAttribute(ans[n]);
//							Object av = context[c].getAttribute(ans[n]);
//							if (av == null)
//								continue;
//							TreeSet avs = ((TreeSet) anvs.get(ans[n]));
//							if (avs == null) {
//								avs = new TreeSet();
//								anvs.put(ans[n], avs);
//							}
//							avs.add(av.toString());
//						}
//					}
//					
//					//	map attribute names to value arrays
//					for (Iterator anit = anvs.keySet().iterator(); anit.hasNext();) {
//						String an = ((String) anit.next());
//						this.write("attributeValuesById['" + an + "'] = [");
//						TreeSet avs = ((TreeSet) anvs.get(an));
//						for (Iterator avit = avs.iterator(); avit.hasNext();) {
//							this.write("'" + GoldenGateImagineEditorServlet.escapeForJavaScript((String) avit.next()) + "'");
//							if (avit.hasNext())
//								this.write(", ");
//						}
//						this.writeLine("];");
//					}
//				}
//				
//				//	close script
//				this.writeLine("</script>");
//			}
//		};
//	}
	
	/**
	 * Create an HTML page builder that creates a web page with only a series
	 * JavaScript calls in its body, the last one of which closes the popin
	 * window via 'window.close()'. If the argument JavaScript calls are to be
	 * executed by the main page behind the popin, they have to be prefixed
	 * with 'window.opener.'.
	 * @param host the page builder host to use
	 * @param request the HTTP request to respond to
	 * @param response the HTTP response to write to
	 * @param javaScriptCalls an array of JavaScript calls to execute before
	 *            actually closing the popin window
	 * @return an HTMP page builder to create the described page
	 * @throws IOException
	 */
	public static HtmlPageBuilder getClosePopinWindowPageBuilder(HtmlPageBuilderHost host, HttpServletRequest request, HttpServletResponse response, final String[] javaScriptCalls) throws IOException {
		return new HtmlPageBuilder(host, request, response) {
			protected void include(String type, String tag) throws IOException {
				if ("includeBody".equals(type)) {
					this.writeLine("<script type=\"text/javascript\">");
					if (javaScriptCalls != null) {
						for (int c = 0; c < javaScriptCalls.length; c++)
							this.writeLine(javaScriptCalls[c]);
					}
					//	close status window (we need to wait until pop-in parent has replaced close() function)
					this.writeLine("window.setTimeout('window.close()', 100);");
					this.writeLine("</script>");
				}
				else super.include(type, tag);
			}
		};
	}
	
	/** HTML grammar for extracting type information from tokens, etc */
	public static Html html = new Html();
	
	/** HTML-configured parser for handling web page templates */
	public static Parser htmlParser = new Parser(html);
	
	/**
	 * Send an HTML page. This method locates the base page file with the
	 * specified name using the findFile() method and streams it through the
	 * argument page builder. The argument page builder is closed afterward.
	 * The base page is assumed to be encoded in the platform default encoding.
	 * @param basePageName the name of the base page file to use
	 * @param pageBuilderHost the page builder host to use for localizing the
	 *            base page
	 * @param pageBuilder the page builder to use
	 * @throws IOException
	 */
	public static void sendHtmlPage(String basePageName, HtmlPageBuilderHost pageBuilderHost, HtmlPageBuilder pageBuilder) throws IOException {
		File pageFile = pageBuilderHost.findFile(basePageName);
		if (pageFile == null)
			throw new IOException("Page base file not found: '" + basePageName + "'.");
		Reader basePageReader = new BufferedReader(new InputStreamReader(new FileInputStream(pageFile)));
		sendHtmlPage(basePageReader, pageBuilder);
		basePageReader.close();
	}
	
	/**
	 * Send an HTML page. This method streams the data from the argument reader
	 * through the argument page builder. The argument page builder is closed
	 * afterward.
	 * @param basePageReader the reader to read from
	 * @param pageBuilder the page builder to use
	 * @throws IOException
	 */
	public static void sendHtmlPage(Reader basePageReader, HtmlPageBuilder pageBuilder) throws IOException {
		htmlParser.stream(basePageReader, pageBuilder);
		pageBuilder.close();
	}
	
	/**
	 * Write the submission of an attribute editor form created by this class
	 * through to its target attributed object. The identifier of the target
	 * object is expected to be in the 'id' attribute of the argument HTTP
	 * request.
	 * @param target the parent document of the object whose attributes to edit
	 * @param request the attribute editor form submission
	 * @return true if the attributes changed, false otherwise
	 */
	public static boolean processAttributeEditorSubmission(ImDocument doc, HttpServletRequest request) {
		Attributed target = getAttributed(doc, request.getParameter("id"));
		if (target == null)
			return false;
		else return processAttributeEditorSubmission(target, request);
	}
	
	/**
	 * Write the submission of an attribute editor form created by this class
	 * through to its target attributed object.
	 * @param target the object whose attributes to edit
	 * @param request the attribute editor form submission
	 * @return true if the attributes changed, false otherwise
	 */
	public static boolean processAttributeEditorSubmission(Attributed target, HttpServletRequest request) {
		boolean changed = false;
		
		//	read request attributes
		String[] targetAttributeNames = target.getAttributeNames();
		HashMap requestAttributes = new HashMap();
		for (Enumeration pne = request.getParameterNames(); pne.hasMoreElements();) {
			String pn = ((String) pne.nextElement());
			if (!pn.startsWith("ATTR_"))
				continue;
			String pv = request.getParameter(pn);
			if (pv.length() == 0)
				continue;
			requestAttributes.put(pn.substring("ATTR_".length()), pv);
		}
		
		//	update pre-existing attributes, and remove spurious ones
		for (int n = 0; n < targetAttributeNames.length; n++) {
			String requestAttributeValue = ((String) requestAttributes.remove(targetAttributeNames[n]));
			if (requestAttributeValue == null) {
				target.removeAttribute(targetAttributeNames[n]);
				changed = true;
			}
			else {
				Object targetAttributeValue = target.getAttribute(targetAttributeNames[n]);
				if ((targetAttributeValue == null) || !requestAttributeValue.equals(targetAttributeValue.toString())) {
					target.setAttribute(targetAttributeNames[n], requestAttributeValue);
					changed = true;
				}
			}
		}
		
		//	add new attributes
		for (Iterator ranit = requestAttributes.keySet().iterator(); ranit.hasNext();) {
			String requestAttributeName = ((String) ranit.next());
			Object requestAttributeValue = requestAttributes.get(requestAttributeName);
			target.setAttribute(requestAttributeName, requestAttributeValue);
			changed = true;
		}
		
		//	indicate changes
		return changed;
	}
	
	/**
	 * Obtain the title for an attribute editor, depending on the concrete type
	 * of the attributed object whose attributes to edit.
	 * @param attributed the attributed object
	 * @return a title string for the attribute editor dialog
	 */
	public static String getAttributeEditorTitle(Attributed attributed) {
		if (attributed instanceof ImWord)
			return ("Edit Attributes of Word '" + ((ImWord) attributed).getString() + "'");
		else if (attributed instanceof ImPage)
			return ("Edit Attributes of Page " + (attributed.hasAttribute(LiteratureConstants.PAGE_NUMBER_ATTRIBUTE) ? attributed.getAttribute(LiteratureConstants.PAGE_NUMBER_ATTRIBUTE) : (((ImPage) attributed).pageId + 1)));
		else if (attributed instanceof ImRegion)
			return ("Edit Attributes of '" + ((ImRegion) attributed).getType() + "' Region at " + ((ImRegion) attributed).bounds.toString());
		else if (attributed instanceof ImAnnotation) {
			ImWord firstWord = ((ImAnnotation) attributed).getFirstWord();
			ImWord lastWord = ((ImAnnotation) attributed).getLastWord();
			String annotValue;
			if (firstWord == lastWord)
				annotValue = firstWord.getString();
			else if (firstWord.getNextWord() == lastWord)
				annotValue = ImUtils.getString(firstWord, lastWord, true);
			else annotValue = (firstWord.getString() + " ... " + lastWord.getString());
			return ("Edit Attributes of '" + ((ImAnnotation) attributed).getType() + "' Annotation '" + annotValue + "'");
		}
		else if (attributed instanceof ImDocument)
			return "Edit Document Attributes";
		else return "Edit Attributes";
	}
	
	/**
	 * Obtain a label for an attribute edit action, depending on the concrete
	 * type of the attributed object whose attributes to edit.
	 * @param attributed the attributed object
	 * @return a label string for the attribute edit action
	 */
	public static String getAttributeEditActionLabel(Attributed attributed) {
		if (attributed instanceof ImWord)
			return ("Edit Word Attributes");
		else if (attributed instanceof ImPage)
			return ("Edit Page Attributes");
		else if (attributed instanceof ImRegion)
			return ("Edit " + ((ImRegion) attributed).getType() + " Attributes");
		else if (attributed instanceof ImAnnotation)
			return ("Edit " + ((ImAnnotation) attributed).getType() + " Attributes");
		else if (attributed instanceof ImDocument)
			return "Edit Document Attributes";
		else return "Edit Attributes";
	}
	
	/**
	 * Retrieve context objects for a given attributed object, to be used as
	 * sources of suggested attributes and attribute values.
	 * @param attributed the attributed object whose context objects to retrieve
	 * @return an array of context objects
	 */
	public static Attributed[] getAttributedContext(Attributed attributed) {
		if (attributed instanceof ImWord)
			return ((ImWord) attributed).getDocument().getPage(((ImWord) attributed).pageId).getWords();
		else if (attributed instanceof ImAnnotation)
			return ((ImAnnotation) attributed).getDocument().getAnnotations(((ImAnnotation) attributed).getType());
		else if (attributed instanceof ImRegion)
			return ((ImRegion) attributed).getDocument().getPage(((ImRegion) attributed).pageId).getRegions(((ImRegion) attributed).getType());
		else return null;
	}
	
	/**
	 * Create an identifier string for an attributed object of the Image Markup
	 * data model. Namely, this method uses a prefix indicating the concrete
	 * class of the argument, followed by a colon, and whichever identifier is
	 * applicable for the argument object. Given the returned identifier, the
	 * argument object can be retrieved from its parent Image Markup document
	 * via the <code>getAttributed()</code> method.
	 * @param attributed the attributed object to create an identifier for
	 * @return the identifier
	 */
	public static String getAttributedId(Attributed attributed) {
		String attributedType = null;
		String attributedId = null;
		if (attributed instanceof ImWord) {
			attributedType = "W";
			attributedId = ((ImWord) attributed).getLocalID();
		}
		else if (attributed instanceof ImPage) {
			attributedType = "P";
			attributedId = ("" + ((ImPage) attributed).pageId);
		}
		else if (attributed instanceof ImRegion) {
			attributedType = "R";
			attributedId = (((ImRegion) attributed).getType() + "@" + ((ImRegion) attributed).pageId + "." + ((ImRegion) attributed).bounds.toString());
		}
		else if (attributed instanceof ImAnnotation) {
			attributedType = "A";
			attributedId = (((ImAnnotation) attributed).getType() + "@" + ((ImAnnotation) attributed).getFirstWord().getLocalID() + "-" + ((ImAnnotation) attributed).getLastWord().getLocalID());
		}
		else if (attributed instanceof ImDocument) {
			attributedType = "D";
			attributedId = (((ImDocument) attributed).docId);
		}
		if ((attributedType != null) && (attributedId != null))
			return (attributedType + ":" + attributedId);
		else return null;
	}
	
	/**
	 * Retrieve an attributed object from its parent Image Markup document via
	 * the identifier created for it by the <code>getAttributedId()</code>
	 * method.
	 * @param doc the parent document of the attributed object
	 * @param attributedId the identifier of the attributed object
	 * @return the attributed object with the argument identifier
	 */
	public static Attributed getAttributed(ImDocument doc, String attributedId) {
		if (attributedId.indexOf(':') == -1)
			return null;
		String targetType = attributedId.substring(0, attributedId.indexOf(':'));
		String targetId = attributedId.substring(attributedId.indexOf(':') + ":".length());
		
		if ("W".equals(targetType))
			return doc.getWord(targetId);
		else if ("P".equals(targetType))
			return doc.getPage(Integer.parseInt(targetId));
		else if ("R".equals(targetType)) {
			String[] targetIdParts = targetId.split("[\\@\\.]");
			if (targetIdParts.length != 3)
				return null;
			ImPage page = doc.getPage(Integer.parseInt(targetIdParts[1]));
			if (page == null)
				return null;
			BoundingBox bounds = BoundingBox.parse(targetIdParts[2]);
			if (bounds == null)
				return null;
			ImRegion[] pageRegions = page.getRegions(targetIdParts[0]);
			for (int r = 0; r < pageRegions.length; r++) {
				if (pageRegions[r].bounds.equals(bounds))
					return pageRegions[r];
			}
		}
		else if ("A".equals(targetType)) {
			String[] targetIdParts = targetId.split("[\\@\\-]");
			if (targetIdParts.length != 3)
				return null;
			ImWord firstWord = doc.getWord(targetIdParts[1]);
			if (firstWord == null)
				return null;
			ImWord lastWord = doc.getWord(targetIdParts[2]);
			if (lastWord == null)
				return null;
			ImAnnotation[] annots = doc.getAnnotations(firstWord, null);
			for (int a = 0; a < annots.length; a++) {
				if ((annots[a].getLastWord() == lastWord) && targetIdParts[0].equals(annots[a].getType()))
					return annots[a];
			}
		}
		else if ("D".equals(targetType))
			return doc;
		
		return null;
	}
}