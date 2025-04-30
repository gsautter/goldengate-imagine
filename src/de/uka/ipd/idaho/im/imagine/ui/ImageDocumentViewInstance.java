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

import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentViewProvider;
import de.uka.ipd.idaho.goldenGate.ui.DocumentView;
import de.uka.ipd.idaho.goldenGate.ui.DocumentViewInstance;
import de.uka.ipd.idaho.im.ImAnnotation;
import de.uka.ipd.idaho.im.gamta.LazyAnnotation;

/**
 * XML markup specific instance of a document view, with dedicated support for
 * XML markup data model.
 * 
 * @author sautter
 */
public class ImageDocumentViewInstance extends DocumentViewInstance {
	
	/** the flags to use for the generic XML wrapper */
	public final int xmlWrapperFlags;
	
	/** the annotation to show in the document view */
	public final ImAnnotation imScope;
	
	/** Constructor
	 * @param provider the provider of the view
	 * @param name the name of the view, unique within the views offered by same provider
	 * @param label the label of the view, for use in menus, etc.
	 * @param description a description of the view, for use in tooltips, etc.
	 * @param modeFlags the view mode flags, encoding the mode of operation
	 * @param scope the scope of the document view, i.e., the portion of a document it displays (null for full-document views)
	 */
	public ImageDocumentViewInstance(DocumentViewProvider provider, String name, String label, String description, DocumentView docView, int modeFlags, ImAnnotation scope) {
		this(provider, name, label, description, docView, modeFlags, -1, scope);
	}
	
	/** Constructor
	 * @param provider the provider of the view
	 * @param name the name of the view, unique within the views offered by same provider
	 * @param label the label of the view, for use in menus, etc.
	 * @param description a description of the view, for use in tooltips, etc.
	 * @param modeFlags the view mode flags, encoding the mode of operation
	 * @param xmlWrapperFlags the flags to use for the generic XML wrapper (set to -1 to use flags from parent display)
	 * @param scope the scope of the document view, i.e., the portion of a document it displays (null for full-document views)
	 */
	public ImageDocumentViewInstance(DocumentViewProvider provider, String name, String label, String description, DocumentView docView, int modeFlags, int xmlWrapperFlags, ImAnnotation scope) {
		super(provider, name, label, description, docView, modeFlags, wrapScopeAnnotation(scope, xmlWrapperFlags));
		this.xmlWrapperFlags = xmlWrapperFlags;
		this.imScope = scope;
	}
	
	/** Constructor
	 * @param provider the provider of the view
	 * @param name the name of the view, unique within the views offered by same provider
	 * @param label the label of the view, for use in menus, etc.
	 * @param description a description of the view, for use in tooltips, etc.
	 * @param modeFlags the view mode flags, encoding the mode of operation
	 */
	public ImageDocumentViewInstance(DocumentViewProvider provider, String name, String label, String description, DocumentView docView, int modeFlags) {
		this(provider, name, label, description, docView, modeFlags, -1);
	}
	
	/** Constructor
	 * @param provider the provider of the view
	 * @param name the name of the view, unique within the views offered by same provider
	 * @param label the label of the view, for use in menus, etc.
	 * @param description a description of the view, for use in tooltips, etc.
	 * @param modeFlags the view mode flags, encoding the mode of operation
	 * @param xmlWrapperFlags the flags to use for the generic XML wrapper (set to -1 to use flags from parent display)
	 */
	public ImageDocumentViewInstance(DocumentViewProvider provider, String name, String label, String description, DocumentView docView, int modeFlags, int xmlWrapperFlags) {
		super(provider, name, label, description, docView, modeFlags);
		this.xmlWrapperFlags = xmlWrapperFlags;
		this.imScope = null;
	}
	
	private static Annotation wrapScopeAnnotation(ImAnnotation imData, int dataFlags) {
		if (imData == null)
			return null;
		else return new LazyAnnotation(imData, dataFlags);
	}
}
