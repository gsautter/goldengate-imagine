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

import de.uka.ipd.idaho.goldenGate.ui.GoldenGateUI.DocumentDisplay;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.ImDocument;
import de.uka.ipd.idaho.im.gamta.LazyMutableAnnotation;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;

/**
 * @author sautter
 */
public interface ImageDocumentDisplay extends DocumentDisplay {
	
	/**
	 * Retrieve the displaying document.
	 * @return the displaying document
	 */
	public abstract ImDocument getImDocument();
	
	/**
	 * Retrieve the markup panel actually displaying the document.
	 * @return the markup panel
	 */
	public abstract ImDocumentMarkupPanel getImDocumentPanel();
	
	/**
	 * Retrieve the current flags for the generic XML wrapper.
	 * @return the wrapper flags
	 */
	public abstract int getXmlWrapperFlags();
	
	/**
	 * Retrieve the current generic XML wrapper, allowing for reuse. If the
	 * argument flag vector is -1, the current flags are used.
	 * @param flags the required flags
	 * @return the generic XML wrapper
	 */
	public abstract LazyMutableAnnotation getXmlWrapper(int flags);
	
	/**
	 * Apply a generic XML based markup tool to the contained document. If the
	 * argument annotation is null, the image markup tool is applied to the
	 * whole image markup document.
	 * @param imt the image markup tool to apply
	 * @param annot the annotation to apply the XML markup tool to
	 */
	public void applyGenericXmlMarkupTool(ImageMarkupTool imt, ImAnnotation annot);
}
