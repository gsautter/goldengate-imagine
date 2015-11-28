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
package de.uka.ipd.idaho.im.imagine.web.plugins;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder.HtmlPageBuilderHost;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;

/**
 * An image markup tool that opens a web based document view. Image Markup
 * Tools that open a dialog with specific functionality in desktop use, rather
 * than doing automated processing, should implement this interface to work in
 * a web based setting as well. Note that this is only required if actions in
 * the view modify the document directly, or if the dialog is highly
 * sophisticated. Prompts that simply ask for parameters or the like can be
 * handled via the various <code>DialogFactory.confirm()</code> methods.
 * 
 * @author sautter
 */
public interface WebDocumentViewer extends ImageMarkupTool {
	
	/**
	 * Create a web based view of the document residing in a markup panel. All
	 * dynamic content HTTP requests sent by the view page have to start with
	 * the argument base URL to make sure requests are properly routed by to
	 * the corresponding server side document view object.
	 * @param idmp the markup panel holding the document
	 * @param baseUrl the URL to use
	 * @return a web based view of the document
	 */
	public abstract WebDocumentView getWebDocumentView(String baseUrl);
	
	/**
	 * A web based document view displaying (data from) a single document.
	 * Classes implementing the surrounding interface should keep all status
	 * information inside instances of this class, as multiple views from the
	 * same viewer might be active simultaneously.
	 * 
	 * @author sautter
	 */
	public static abstract class WebDocumentView implements ImageMarkupTool {
		private ImageMarkupTool parentImt;
		
		private Object lock = new Object();
		
		/** the URL that HTTP requests from the web based view to an instance of this class have to start with */
		protected final String baseUrl;
		
		/** Constructor
		 * @param parentImt the parent Image Markup Tool
		 * @param baseUrl the base URL for HTTP requests
		 */
		protected WebDocumentView(ImageMarkupTool parentImt, String baseUrl) {
			this.parentImt = parentImt;
			this.baseUrl = baseUrl;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#getLabel()
		 */
		public String getLabel() {
			return this.parentImt.getLabel();
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#getTooltip()
		 */
		public String getTooltip() {
			return this.parentImt.getTooltip();
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#getHelpText()
		 */
		public String getHelpText() {
			return this.parentImt.getHelpText();
		}
		
		/**
		 * This implementation first calls the <code>preProcess()</code> method,
		 * then blocks until the <code>close()</code> method is called, and
		 * finally calls the <code>postProcess()</code> method.
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#process(de.uka.ipd.idaho.im.ImDocument, de.uka.ipd.idaho.im.ImAnnotation, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
		 */
		public final void process(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm) {
			
			//	do processing before opening dialog
			this.preProcess(doc, annot, idmp);
			
			//	block until dialog closed
			synchronized (this.lock) {
				try {
					this.lock.wait();
				}
				catch (InterruptedException ie) {
					return; // if we get here, it's most likely due to a server shutdown
				}
			}
			
			//	do processing after dialog closed
			this.postProcess(doc, annot, idmp);
		}
		
		/**
		 * Do any processing required before the document view can be opened.
		 * This default implementation does nothing, sub classes are welcome to
		 * overwrite it as needed.
		 * @param doc the document to process
		 * @param annot the annotation to process (null for whole-document
		 *        processing)
		 * @param idmp the markup panel displaying the argument document
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#process(de.uka.ipd.idaho.im.ImDocument, de.uka.ipd.idaho.im.ImAnnotation, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
		 */
		protected void preProcess(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp) {}
		
		/**
		 * Do any processing required after the document view has been closed.
		 * This default implementation does nothing, sub classes are welcome to
		 * overwrite it as needed.
		 * @param doc the document to process
		 * @param annot the annotation to process (null for whole-document
		 *        processing)
		 * @param idmp the markup panel displaying the argument document
		 * @see de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool#process(de.uka.ipd.idaho.im.ImDocument, de.uka.ipd.idaho.im.ImAnnotation, de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel, de.uka.ipd.idaho.gamta.util.ProgressMonitor)
		 */
		protected void postProcess(ImDocument doc, ImAnnotation annot, ImDocumentMarkupPanel idmp) {}
		
		/**
		 * Close the view. This method is called by the surrounding code soon
		 * as <code>isCloseRequest()</code> returns true. 
		 */
		public void close() {
			synchronized (this.lock) {
				this.lock.notify();
			}
		}
		
		/**
		 * Get a builder for an HTML page representing the view. Sub classes
		 * whose <code>preProcess()</code> method takes some time to return
		 * should take measures to ensure that this method does not return a
		 * page builder before the latter method has finished. This method can
		 * be called multiple times, e.g. if a user hits the 'Reload' button,
		 * and thus the internal state should not change.
		 * @param host the host object granting access to files, etc.
		 * @param request the HttpServletRequest to answer
		 * @param response the HttpServletResponse to write the answer to
		 * @return an HTML page builder creating the document view page
		 * @throws IOException
		 */
		public abstract HtmlPageBuilder getViewPageBuilder(HtmlPageBuilderHost host, HttpServletRequest request, HttpServletResponse response) throws IOException;
		
		/**
		 * Get a <code>Reader</code> that provides the static basic view page.
		 * If this method returns null, the surrounding code should use the
		 * default popup page template, with 'includeBody' being the only
		 * marker tag. This default implementation does return null, sub
		 * classes are welcome to overwrite it as needed.
		 * @return a reader for the static basic HTML page.
		 */
		public Reader getViewBasePage() {
			return null;
		}
		
		/**
		 * Test if an HTTP request if directed at this view, and process it if
		 * that is the case. If this method returns true, the surrounding code
		 * should do nothing further about the argument request. If the argument
		 * request closes the view (i.e., <code>isCloseRequest()</code> will
		 * return true), this method must return false.
		 * @param request the HTTP request to handle
		 * @param response the HTTP response to write to
		 * @param pathInfo the part of the URL after the base URL
		 * @return true if the request has been handled, false otherwise
		 * @throws IOException
		 */
		public abstract boolean handleRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo) throws IOException;
		
		/**
		 * Test if an HTTP request is intended to close the view. The HTML page
		 * representing the view in a browser <b>must</b> send a request for
		 * which this method returns true for any action that is intended to
		 * close the view. Implementations may read the request body and act
		 * upon it, e.g. if the request is the result of a form in the browser
		 * side view being committed.
		 * @param request the HTTP request to check
		 * @param pathInfo the part of the URL after the base URL
		 * @return true if the view should be closed in response to the
		 *        argument HTTP request
		 */
		public abstract boolean isCloseRequest(HttpServletRequest request, String pathInfo);
	}
}