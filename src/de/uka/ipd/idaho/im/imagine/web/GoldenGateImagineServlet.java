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
package de.uka.ipd.idaho.im.imagine.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.uka.ipd.idaho.easyIO.web.HtmlServlet;
import de.uka.ipd.idaho.gamta.util.constants.LiteratureConstants;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.HtmlPageBuilder;

/**
 * This abstract class centrally provides the basic storage settings for all of
 * the browser based version of GoldenGATE Imagine.
 * 
 * @author sautter
 */
public abstract class GoldenGateImagineServlet extends HtmlServlet implements LiteratureConstants {
	
	/** the root folder for transient caching */
	protected File cacheRootPath = null;
	
	/** the root folder of the GoldenGATE Imagine installation */
	protected File ggImagineRootPath = null;
	
	/** the usual zero-argument constructor */
	protected GoldenGateImagineServlet() {}
	
	/** This implementation reads the document and cache root paths. Sub
	 * classes overwriting this method thus have to make the super call.
	 * @see de.uka.ipd.idaho.easyIO.web.HtmlServlet#reInit()
	 */
	protected void reInit() throws ServletException {
		super.reInit();
		
		//	get cache root
		String cacheRootPath = this.getSetting("cacheRootPath");
		if (cacheRootPath == null)
			this.cacheRootPath = new File(this.webInfFolder, "cache");
		else if (cacheRootPath.startsWith("./"))
			this.cacheRootPath = new File(this.webInfFolder, cacheRootPath.substring("./".length()));
		else this.cacheRootPath = new File(cacheRootPath);
		if (this.cacheRootPath != null)
			this.cacheRootPath.mkdirs();
		
		//	get root of GoldenGATE Imagine installation
		String ggImagineRootPath = this.getSetting("ggImagineRootPath");
		if (ggImagineRootPath == null)
			this.ggImagineRootPath = new File(this.webInfFolder, "ggImagine");
		else if (ggImagineRootPath.startsWith("./"))
			this.ggImagineRootPath = new File(this.webInfFolder, ggImagineRootPath.substring("./".length()));
		else this.ggImagineRootPath = new File(ggImagineRootPath);
		if (this.ggImagineRootPath != null)
			this.ggImagineRootPath.mkdirs();
	}
	
	/**
	 * Create an HTML page builder that creates a web page with only a series
	 * JavaScript calls in its body, the last one of which closes the popin
	 * window via 'window.close()'. If the argument JavaScript calls are to be
	 * executed by the main page behind the popin, they have to be prefixed
	 * with 'window.parent.'.
	 * @param request the HTTP request to respond to
	 * @param response the HTTP response to write to
	 * @param javaScriptCalls an array of JavaScript calls to execute before
	 *            actually closing the popin window
	 * @return an HTMP page builder to create the described page
	 * @throws IOException
	 */
	protected HtmlPageBuilder getClosePopinWindowPageBuilder(HttpServletRequest request, HttpServletResponse response, final String[] javaScriptCalls) throws IOException {
		return GoldenGateImagineWebUtils.getClosePopinWindowPageBuilder(this, request, response, javaScriptCalls);
	}
	
	/**
	 * Get the path for static resources, i.e., ones physically lying in the
	 * file system.
	 * @return the path for static resources
	 */
	public String getStaticResourcePath() {
		return ("/" + RESOURCE_PATH + this.dataPath);
	}
	
	/**
	 * Write an HTML tag to include a JavaScript. Virtual JavaScripts are ones
	 * generated on the fly, e.g. under consideration of session or request
	 * specific information. Physical JavaScript files are static ones, loaded
	 * from the file system.
	 * @param hpb the HTML page builder to write the tag to
	 * @param jsName the name of the JavaScript file
	 * @param virtual is the JavaScript file virtual or physical?
	 * @throws IOException
	 */
	protected void writeJavaScriptTag(HtmlPageBuilder hpb, String jsName, boolean virtual) throws IOException {
		hpb.writeLine("<script type=\"text/javascript\" src=\"" + hpb.request.getContextPath() + (virtual ? hpb.request.getServletPath() : ("/" + RESOURCE_PATH + dataPath)) + "/" + jsName + "\"></script>");
	}
}