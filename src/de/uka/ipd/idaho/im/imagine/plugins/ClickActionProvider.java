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

import java.awt.Point;

import de.uka.ipd.idaho.im.ImPage;
import de.uka.ipd.idaho.im.ImWord;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel;
import de.uka.ipd.idaho.im.util.ImDocumentMarkupPanel.ClickSelectionAction;

/**
 * Provider of click actions working on Image Markup documents, to dynamically
 * extend the image markup editor.
 * 
 * @author sautter
 */
public interface ClickActionProvider extends GoldenGateImaginePlugin {
	
	/**
	 * Retrieve the available actions for a given number of clicks on a word.
	 * The argument editor panel is to provide the current configuration of the
	 * editing interface.
	 * @param word the word that was clicked
	 * @param clickCount the number of clicks
	 * @param idmp the document editor panel to use the actions in
	 * @return an array holding the actions
	 */
	public abstract ClickSelectionAction[] getActions(ImWord word, int clickCount, ImDocumentMarkupPanel idmp);
	
	/**
	 * Retrieve the available actions for a given number of clicks on a point
	 * in a given page. The argument point is relative to the argument page,
	 * and in its original resolution. The argument editor panel is to provide
	 * the current configuration of the editing interface.
	 * @param page the document page the point lies in
	 * @param point the point that was clicked
	 * @param clickCount the number of clicks
	 * @param idmp the document editor panel to use the actions in
	 * @return an array holding the actions
	 */
	public abstract ClickSelectionAction[] getActions(ImPage page, Point point, int clickCount, ImDocumentMarkupPanel idmp);
}
