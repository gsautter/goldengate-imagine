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

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDropEvent;

import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;

/**
 * Convenience abstract super class for drop handler plugins, inheriting the
 * life cycle methods from abstract GoldenGATE plugin. Sub classes that require
 * all the information about a <code>DropTargetDropEvent</code> to work should
 * overwrite the method that takes the latter as an argument. Sub classes that
 * can make do with the actual transfer data should instead overwrite the
 * method that only takes a <code>Transferable</code>, which also works for
 * emulated calls.
 * 
 * @author sautter
 */
public class AbstractImageDocumentDropHandler extends AbstractGoldenGateImaginePlugin implements ImageDocumentDropHandler {
	/** zero-argument constructor for class loading */
	protected AbstractImageDocumentDropHandler() {}
	
	/**
	 * Handle a drop on a page of an Image Markup document. This default
	 * implementation gets the transfer data from the argument event and then
	 * loops through to the version of this method that takes a
	 * <code>Transferable</code> as an argument instead of the
	 * <code>DropTargetDropEvent</code> required by this method. Only sub
	 * classes that require all the information coming with a
	 * <code>DropTargetDropEvent</code> to work should overwrite this method,
	 * all others should rather overwrite the method that takes a simple
	 * <code>Transferable</code> as an argument.
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler#handleDrop(de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel, de.uka.ipd.idaho.im.ImPage, int, int, java.awt.dnd.DropTargetDropEvent)
	 */
	public boolean handleDrop(ImDocumentMarkupPanel idmp, ImPage page, int pageX, int pageY, DropTargetDropEvent dtde) {
		return this.handleDrop(idmp, page, pageX, pageY, dtde.getTransferable());
	}
	
	/**
	 * Handle a drop on a page of an Image Markup document. This default
	 * implementation simply returns <code>false</code>. Sub classes that
	 * can make do with the actual transfer data should overwrite this
	 * method, which also works for emulated calls. Only sub classes that
	 * require all the information coming with a
	 * <code>DropTargetDropEvent</code> to work should overwrite the method
	 * that takes an instance of the latter as an argument, and leave this
	 * method alone to simply refuse any incoming drop.
	 * @see de.uka.ipd.idaho.im.imagine.plugins.ImageDocumentDropHandler#handleDrop(de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel, de.uka.ipd.idaho.im.ImPage, int, int, java.awt.datatransfer.Transferable)
	 */
	public boolean handleDrop(ImDocumentMarkupPanel idmp, ImPage page, int pageX, int pageY, Transferable transfer) {
		return false;
	}
}