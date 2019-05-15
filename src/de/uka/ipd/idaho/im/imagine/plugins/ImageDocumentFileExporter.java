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

import java.io.File;
import java.io.IOException;

import de.uka.ipd.idaho.gamta.util.ProgressMonitor;
import de.uka.ipd.idaho.im.ImDocument;

/**
 * Image Document Exporter that can write directly to a file, without a user
 * choosing the export destination file in any sort of way, in particular
 * without opening any <code>JFileChooser</code> prompts. This is especially
 * useful in deployment scenarios that do not have a UI in the same JVM the
 * export runs in. On top of that, it provides a three-argument version of the
 * generic export function, adding a likely export destination, e.g. for
 * priming a file chooser.
 * 
 * @author sautter
 */
public interface ImageDocumentFileExporter extends ImageDocumentExporter {
	
	/**
	 * Export a document. The argument file indicates a likely destination of
	 * an export, e.g. for priming a file chooser. If the application cannot
	 * make such a prediction, the argument is null. It is recommended that
	 * implementations of the two-argument version delegate here.
	 * @param likelyDest the likely destination file
	 * @param doc the document to export
	 * @param pm a progress monitor observing export progress
	 */
	public abstract void exportDocument(File likelyDest, ImDocument doc, ProgressMonitor pm);
	
	/**
	 * Export a document to a file. Implementations may extend the name of the
	 * argument file to avoid collisions or adjust file extension, but should
	 * not modify the parent file, i.e., the export destination folder.
	 * @param doc the document to export
	 * @param destFile the destination file
	 * @param pm a progress monitor observing export progress
	 * @return the file the document was actually exported to, to notify the
	 *            invoker of this method about file name adjustments
	 */
	public abstract File exportDocument(ImDocument doc, File destFile, ProgressMonitor pm) throws IOException;
}
