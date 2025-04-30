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
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ImageMarkupTool;

/**
 * A GoldenGATE Imagine atomic action listener receives notifications of atomic
 * actions on an Image Markup documents starting and finishing throughout a JVM
 * from a central point.
 * 
 * @author sautter
 */
public interface GoldenGateImagineAtomicActionListener extends GoldenGateImaginePlugin {
	
	/**
	 * Receive notification that an atomic action is starting on an Image
	 * Document markup panel. Both the Image Markup Tool and the target
	 * annotation can be null, and will be for Selection Actions. In fact, the
	 * target annotation only ever is not null when an Image Markup Tool is run
	 * on an annotation.
	 * @param id the unique ID of the started action
	 * @param label the label of the action
	 * @param imt the Image Markup Tool performing the action
	 * @param annot the annotation being processed
	 * @param idmp the document editor panel the atomic action is starting on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public abstract void atomicActionStarted(long id, String label, ImageMarkupTool imt, ImAnnotation annot, ImDocumentMarkupPanel idmp, ProgressMonitor pm);
	
	/**
	 * Receive notification that the running atomic action is finishing on an
	 * Image Document markup panel. This method is intended for implementors to
	 * trigger any follow-on activities, still within the running atomic
	 * action, e.g. updating derived data stored on the document proper.
	 * @param id the unique ID of the finishing action
	 * @param idmp the document editor panel the atomic action is finishing on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public abstract void atomicActionFinishing(long id, ImDocumentMarkupPanel idmp, ProgressMonitor pm);
	
	/**
	 * Receive notification that the running atomic action has finished on an
	 * Image Document markup panel. Any activities triggered by client code on
	 * this notification does not fall under the running atomic action anymore.
	 * @param id the unique ID of the finished action
	 * @param idmp the document editor panel the atomic action was finished on
	 * @param pm the progress monitor observing on the action (if any)
	 */
	public abstract void atomicActionFinished(long id, ImDocumentMarkupPanel idmp, ProgressMonitor pm);
}