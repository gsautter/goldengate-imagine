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
package de.uka.ipd.idaho.im.imagine.plugins;

import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.im.ImDocument;

/**
 * A GoldenGATE Imagine document listener receives notifications of Image
 * Markup documents being opened, saved, and closed throughout a JVM from a
 * central point.
 * 
 * @author sautter
 */
public interface GoldenGateImagineDocumentListener extends GoldenGateImaginePlugin {
	
	/**
	 * Receive notification that an Image Markup document has been opened. This
	 * method is called right after the loading process is completed. The source
	 * argument will usually be a file or an Image Markup IO provider, but might
	 * also be null.
	 * Notifications received through this method allow for receivers to load
	 * additional data from supplements, for instance, e.g. to use in reactions
	 * to user edits.
	 * @param doc the document that was opened
	 * @param source the source the document was loaded from
	 * @param pm a progress monitor observing post-load processing
	 */
	public abstract void documentOpened(ImDocument doc, Object source, ProgressMonitor pm);
	
	/**
	 * Receive notification that an Image Markup document has been selected for
	 * displaying and editing (most likely in some sort of UI). This method is
	 * called whenever the selected document changes.
	 * Notifications received through this method allow for receivers to adjust
	 * custom UI components, for instance.
	 * @param doc the document that was selected
	 */
	public abstract void documentSelected(ImDocument doc);
	
	/**
	 * Receive notification that an Image Markup document is about to be saved
	 * to persistent storage. This method is called right before the actual
	 * saving process begins. The destination argument will usually be a file
	 * or an Image Markup IO provider, but might also be null.
	 * Notifications received through this method allow for receivers to update
	 * supplements to be stored along with the document, for instance.
	 * @param doc the document that is about to be saved
	 * @param dest the destination the document will be saved to
	 * @param pm a progress monitor observing saving preparations
	 */
	public abstract void documentSaving(ImDocument doc, Object dest, ProgressMonitor pm);
	
	/**
	 * Receive notification that an Image Markup document has been saved to
	 * persistent storage. This method is called right after the actual saving
	 * process has completed successfully. The destination argument will usually
	 * be a file or an Image Markup IO provider, but might also be null.
	 * Notifications received through this method allow for receivers to clean
	 * up history data associated with the document, for instance.
	 * @param doc the document that has been saved
	 * @param dest the destination the document was saved to
	 * @param pm a progress monitor observing post-save processing
	 */
	public abstract void documentSaved(ImDocument doc, Object dest, ProgressMonitor pm);
	
	/**
	 * Receive notification that an Image Markup document has been closed. This
	 * method is called right after the document is disposed.
	 * Notifications received through this method allow for receivers to clean
	 * up data structures associated with the document, for instance.
	 * @param docId the ID of the document that was closed
	 */
	public abstract void documentClosed(String docId);
}