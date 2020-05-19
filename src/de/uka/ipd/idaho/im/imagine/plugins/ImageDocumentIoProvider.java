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
package de.uka.ipd.idaho.im.imagine.plugins;

import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.im.ImDocument;

/**
 * An Image Markup IO provider can load and/or save Image Markup documents from
 * and to custom origins and destinations.
 * 
 * @author sautter
 */
public interface ImageDocumentIoProvider extends GoldenGateImaginePlugin {
	
	/**
	 * Retrieve the name of the source documents are loaded from. If an IO
	 * provider cannot load documents, but only save them, this method has to
	 * return null to indicate so.
	 * @return the name of the source documents are loaded from
	 */
	public abstract String getLoadSourceName();
	
	/**
	 * Load an Image Markup document from the backing source. The returned
	 * document is expected to have its name in the 'docName' attribute.
	 * @param a progress monitor to observe the loading process
	 * @return the Image Markup document just loaded
	 */
	public abstract ImDocument loadDocument(ProgressMonitor pm);
	
	/**
	 * Retrieve the name of the destination documents are saved to. If an IO
	 * provider cannot save documents, but only load them, this method has to
	 * return null to indicate so.
	 * @return the name of the destination documents are saved to
	 */
	public abstract String getSaveDestinationName();
	
	/**
	 * Save an Image Markup document to the backing destination. If saving
	 * succeeds, the returned string indicates the name the document was saved
	 * under; if saving fails, implementations must return null to indicate so.
	 * @param doc the Image Markup document to save
	 * @param docName the current name of the document
	 * @param a progress monitor to observe the saving process
	 * @return the name the document was saved under
	 */
	public abstract String saveDocument(ImDocument doc, String docName, ProgressMonitor pm);
}
