var lastUpdateId = -1;
function updateUndoMenu(label, id, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	add entry to top
	var umi = new Object();
	umi.label = label;
	umi.id = ('UNDO-' + id);
	undoMenuItems.splice(0, 0, umi);
	undoMenuItem.items.splice(0, 0, umi);
	while (undoMenuItem.items.length > 10)
		undoMenuItem.items.splice(10, 1);
	
	//	make menu item look active
	undoMenuItem.div.style.color = '#000000';
}

function uSetTextStreamColor(type, color, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	textStreamColors[type] = color;
}
function uSetRegionColor(type, color, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	allRegionColors[type] = color;
	var rtColor = getById('control_regionColor_' + type);
	if (rtColor != null)
		rtColor.style.backgroundColor = color;
	else displayControlMissingDirty = true;
}
function uSetAnnotColor(type, color, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	allAnnotColors[type] = color;
	var atColor = getById('control_annotColor_' + type);
	if (atColor != null)
		atColor.style.backgroundColor = color;
	else displayControlMissingDirty = true;
}

function uSetPaintRegions(type, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	if (regionColors[type] != null)
		return;
	regionColors[type] = allRegionColors[type];
	
	var pr = getById('control_paintRegions_' + type);
	if (pr == null)
		displayControlMissingDirty = true;
	else pr.checked = 'checked';
}
function uSetPaintAnnots(type, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	if (annotColors[type] != null)
		return;
	annotColors[type] = allAnnotColors[type];
	
	var pa = getById('control_paintAnnots_' + type);
	if (pa == null)
		displayControlMissingDirty = true;
	else pa.checked = 'checked';
}

function uSetRegionType(oldType, pageId, boundsString, newType, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	get affected page
	var page = pagesById[pageId];
	if (page == null) // page not displaying
		return;
	
	//	find region and change region type
	for (var r = 0; r < page.regions.length; r++) {
		if (page.regions[r].type != oldType)
			continue;
		if (boundsString != ('[' + page.regions[r].bounds.left + ',' + page.regions[r].bounds.right + ',' + page.regions[r].bounds.top + ',' + page.regions[r].bounds.bottom + ']'))
			continue;
		page.regions[r].type = newType;
		break;
	}
	
	//	remember to update UI
	uSetPageDirty(pageId);
	displayControlStaleDirty = true;
}

function uSetAnnotType(oldType, firstWordId, lastWordId, newType, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	change annot type
	var oldAnnotId = (oldType + '@' + firstWordId + '-' + lastWordId);
	var annot = annotsById[oldAnnotId];
	annot.type = newType;
	
	//	update annot ID and index
	annot.id = (annot.type + '@' + annot.firstWord.id + '-' + annot.lastWord.id);
	annotsById[annot.id] = annot;
	delete annotsById[oldAnnotId];
	
	//	remember to update UI
	for (var p = annot.firstWord.pageId; p <= annot.lastWord.pageId; p++)
		uSetPageDirty(p);
	displayControlStaleDirty = true;
}
function uSetAnnotFirstWord(type, oldFirstWordId, lastWordId, newFirstWordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	change annot first word
	var oldAnnotId = (type + '@' + oldFirstWordId + '-' + lastWordId);
	var annot = annotsById[oldAnnotId];
	var oldFirstWord = annot.firstWord;
	annot.firstWordId = newFirstWordId;
	annot.firstWord = wordsById[annot.firstWordId];
	
	//	update page annot lists if anything to remove
	if (oldFirstWord.pageId < annot.firstWord.pageId) {
		for (var p = oldFirstWord.pageId; p < annot.firstWord.pageId; p++) {
			var page = pagesById[p];
			if (!page.annots)
				continue;
			for (var a = 0; a < page.annots.length; a++)
				if (page.annots[a].id == oldAnnotId) {
					page.annots.splice(a, 1);
					uSetPageDirty(p);
					break;
				}
		}
	}
	
	//	update annot ID and index
	annot.id = (annot.type + '@' + annot.firstWord.id + '-' + annot.lastWord.id);
	annotsById[annot.id] = annot;
	delete annotsById[oldAnnotId];
	
	//	update page annot lists if anything to add
	if (oldFirstWord.pageId > annot.firstWord.pageId) {
		for (var p = annot.firstWord.pageId; p < oldFirstWord.pageId; p++) {
			var page = pagesById[p];
			if (!page.annots)
				page.annots = new Array();
			page.annots[page.annots.length] = annot;
		}
		if (page.annots != null) {
			uSortAnnotArray(page.annots);
			uSetPageDirty(p);
		}
	}
	
	uSetPageDirty(annot.firstWord.pageId);
	uSetPageDirty(annot.lastWord.pageId);
	uSetPageDirty(oldFirstWord.pageId);
}
function uSetAnnotLastWord(type, firstWordId, oldLastWordId, newLastWordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	change annot last word
	var oldAnnotId = (type + '@' + firstWordId + '-' + oldLastWordId);
	var annot = annotsById[oldAnnotId];
	var oldLastWord = annot.lastWord;
	annot.lastWordId = newLastWordId;
	annot.lastWord = wordsById[annot.lastWordId];
	
	//	update page annot lists if anything to remove
	if (oldLastWord.pageId > annot.lastWord.pageId) {
		for (var p = (annot.lastWord.pageId + 1); p <= oldLastWord.pageId; p++) {
			var page = pagesById[p];
			if (!page.annots)
				continue;
			for (var a = 0; a < page.annots.length; a++)
				if (page.annots[a].id == oldAnnotId) {
					page.annots.splice(a, 1);
					uSetPageDirty(p);
					break;
				}
		}
	}
	
	//	update annot ID and index
	annot.id = (annot.type + '@' + annot.firstWord.id + '-' + annot.lastWord.id);
	annotsById[annot.id] = annot;
	delete annotsById[oldAnnotId];
	
	//	update page annot lists if anything to add
	if (oldLastWord.pageId < annot.lastWord.pageId) {
		for (var p = (annot.lastWord.pageId + 1); p <= oldLastWord.pageId; p++) {
			var page = pagesById[p];
			if (!page.annots)
				page.annots = new Array();
			page.annots[page.annots.length] = annot;
		}
		if (page.annots != null) {
			uSortAnnotArray(page.annots);
			uSetPageDirty(p);
		}
	}
	
	uSetPageDirty(annot.firstWord.pageId);
	uSetPageDirty(annot.lastWord.pageId);
	uSetPageDirty(oldLastWord.pageId);
}

function uSetWordPredecessor(wordId, prevWordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	var word = wordsById[wordId];
	if (word == null)
		return;
	
	var prevWord = ((prevWordId == null) ? null : wordsById[prevWordId]);
	if (prevWord == word.prevWord)
		return;
	
	var oldPrev = word.prevWord;
	var prevOldNext = ((prevWord == null) ? null : prevWord.nextWord);
	word.prevWordId = prevWordId;
	word.prevWord = prevWord;
	if (word.prevWord != null) {
		word.prevWordId.nextWord = wordId;
		word.prevWord.nextWord = word;
	}
	if (prevOldNext != null) {
		prevOldNext.prevWordId = null;
		prevOldNext.prevWord = null;
	}
	if (oldPrev != null) {
		oldPrev.nextWordId = null;
		oldPrev.nextWord = null;
	}
	
	if (word.prevWord == null) {
		word.textStreamId = wordId;
		word.textStreamPos = 0;
	}
	else {
		word.textStreamId = word.prevWord.textStreamId;
		if (word.pageId == word.prevWord.pageId)
			word.textStreamPos = (word.prevWord.textStreamPos + 1);
		else word.textStreamPos = 0;
		word.textStreamType = word.prevWord.textStreamType;
	}
	
	for (var w = word.nextWord; w != null; w = w.nextWord) {
		if ((w.pageId != word.pageId) && (w.textStreamId == word.textStreamId))
			break;
		if (w == word)
			break;
		if (w.pageId == word.pageId)
			w.textStreamPos = (w.prevWord.textStreamPos + 1);
		w.textStreamId = word.textStreamId;
		w.textStreamType = word.textStreamType;
		uSetPageDirty(w.pageId);
	}
	
	if (prevOldNext != null) {
		prevOldNext.textStreamId = prevOldNext.id;
		prevOldNext.textStreamPos = 0;
		for (var w = prevOldNext.nextWord; w != null; w = w.nextWord) {
			if ((w.pageId != prevOldNext.pageId) && (w.textStreamId == prevOldNext.textStreamId))
				break;
			if (w == prevOldNext)
				break;
			if (w.pageId == prevOldNext.pageId)
				w.textStreamPos = (w.prevWord.textStreamPos + 1);
			w.textStreamId = prevOldNext.textStreamId;
			w.textStreamType = prevOldNext.textStreamType;
			uSetPageDirty(w.pageId);
		}
	}
	
	uSetPageDirty(word.pageId);
}
function uSetWordSuccessor(wordId, nextWordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	var word = wordsById[wordId];
	if (word == null)
		return;
	
	var nextWord = ((nextWordId == null) ? null : wordsById[nextWordId]);
	if (nextWord == word.nextWord)
		return;
	
	var oldNext = word.nextWord;
	var nextOldPrev = ((nextWord == null) ? null : nextWord.prevWord);
	word.nextWordId = nextWordId;
	word.nextWord = nextWord;
	if (word.nextWord != null) {
		word.nextWord.prevWordId = wordId;
		word.nextWord.prevWord = word;
	}
	if (oldNext != null) {
		oldNext.prevWordId = null;
		oldNext.prevWord = null;
	}
	if (nextOldPrev != null) {
		nextOldPrev.nextWordId = null;
		nextOldPrev.nextWord = null;
	}
	
	if (word.nextWord != null) {
		if (word.nextWord.pageId == word.pageId)
			word.nextWord.textStreamPos = (word.textStreamPos + 1);
		else {
			word.nextWord.textStreamPos = 0;
			uSetPageDirty(word.nextWord.pageId);
		}
		word.nextWord.textStreamId = word.textStreamId;
		word.nextWord.textStreamType = word.textStreamType;
		
		for (var w = word.nextWord.nextWord; w != null; w = w.nextWord) {
			if ((w.pageId != word.nextWord.pageId) && (w.textStreamId == word.nextWord.textStreamId))
				break;
			if (w == word)
				break;
			if (w.pageId == word.nextWord.pageId)
				w.textStreamPos = (w.prevWord.textStreamPos + 1);
			w.textStreamId = word.nextWord.textStreamId;
			w.textStreamType = word.textStreamType;
			uSetPageDirty(w.pageId);
		}
	}
	
	if (oldNext != null) {
		oldNext.textStreamId = oldNext.id;
		oldNext.textStreamPos = 0;
		for (var w = oldNext.nextWord; w != null; w = w.nextWord) {
			if ((w.pageId != oldNext.pageId) && (w.textStreamId == oldNext.textStreamId))
				break;
			if (w == oldNext)
				break;
			if (w.pageId == oldNext.pageId)
				w.textStreamPos = (w.prevWord.textStreamPos + 1);
			w.textStreamId = oldNext.textStreamId;
			w.textStreamType = oldNext.textStreamType;
			uSetPageDirty(w.pageId);
		}
	}
	
	uSetPageDirty(word.pageId);
}
function uSetWordRelation(wordId, nextWordRelation, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	set text stream type
	var word = wordsById[wordId];
	if (word == null)
		return;
	if (word.nextRelation != nextWordRelation)
		word.nextRelation = nextWordRelation;
	
	//	remember to update page(s)
	uSetPageDirty(word.pageId);
	if ((word.nextWord != null) && (word.pageId != word.nextWord.pageId))
		uSetPageDirty(word.nextWord.pageId);
}
function uSetWordStreamType(wordId, textStreamType, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	set text stream type
	var word = wordsById[wordId];
	if (word == null)
		return;
	if (word.textStreamType == textStreamType)
		return;
	word.textStreamType = textStreamType;
	
	//	propagate to whole stream, backward and forward
	for (var w = word.prevWord; w != null; w = w.prevWord) {
		w.textStreamType = textStreamType;
		uSetPageDirty(w.pageId);
	}
	for (var w = word.nextWord; w != null; w = w.nextWord) {
		w.textStreamType = textStreamType;
		uSetPageDirty(w.pageId);
	}
}
function uSetWordString(wordId, wordString, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	var word = wordsById[wordId];
	if (word != null)
		word.str = wordString;
}

function uAddWord(pageId, word, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	get affected page
	var page = pagesById[pageId];
	if (page == null)
		return;
	
	//	add word to document
	word.pageId = pageId;
	word.id = ('' + word.pageId + '.[' + word.bounds.left + ',' + word.bounds.right + ',' + word.bounds.top + ',' + word.bounds.bottom + ']');
	wordsById[word.id] = word;
	
	//	update text streams
	uSetWordPredecessor(word.id, word.prevWordId);
	uSetWordSuccessor(word.id, word.nextWordId);
	if (!word.nextRelation)
		word.nextRelation = 'S';
	
	//	add word to page (only now that text stream integration is complete)
	page.words[page.words.length] = word;
	uSortWordArray(page.words);
	
	//	create word DIVs
	var wordDiv = newElement('div', word.id, 'word');
	wordDiv.style.position = 'absolute';
	page.div.appendChild(wordDiv);
	word.div = wordDiv;
	
	//	add mouse listeners for word selection
	addSelectionHandlerWord(wordDiv, words);
	
	//	add mouse listeners for word selection
	addDropHandlerWord(wordDiv, word);
	
	//	no predecessor
	if (!dDoc.pages[p].words[w].prevWord || (dDoc.pages[p].words[w].prevWord == null)) {}
	
	//	predecessor on other page
	else if (dDoc.pages[p].words[w].prevWord.pageId != dDoc.pages[p].words[w].pageId) {}
	
	//	predecessor above
	else if (dDoc.pages[p].words[w].prevWord.bounds.bottom < dDoc.pages[p].words[w].bounds.top) {}
	
	//	predecessor below
	else if (dDoc.pages[p].words[w].prevWord.bounds.top > dDoc.pages[p].words[w].bounds.bottom) {}
	
	//	no space after predecessor
	else if (dDoc.pages[p].words[w].prevWord.bounds.right >= dDoc.pages[p].words[w].bounds.left) {}
	
	//	create word space DIV
	else {
		var preWordDiv = newElement('div', ('pre.' + dDoc.pages[p].words[w].id), 'word');
		preWordDiv.style.position = 'absolute';
		pageDiv.appendChild(preWordDiv);
		word.preDiv = preWordDiv;
	}
	
	//	remember to update page
	uSetPageDirty(pageId);
}
function uRemoveWord(wordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	get affected word
	var word = wordsById[wordId];
	if (word == null)
		return;
	
	//	remove word from page
	var page = pagesById[word.pageId];
	if (page != null) {
		for (var w = 0; w < page.words.length; w++)
			if (page.words[w].id == word.id) {
				page.words.splice(w, 1);
				break;
			}
		uSetPageDirty(word.pageId);
	}
	
	//	remove word from index
	delete wordsById[wordId];
	
	//	cut word out of logical text stream
	var pWord = word.prevWord;
	var nWord = word.nextWord;
	if ((pWord != null) && (nWord != null))
		uSetWordSuccessor(pWord.id, nWord.id);
	else if (pWord != null)
		uSetWordSuccessor(pWord.id, null);
	else if (nWord != null)
		uSetWordPredecessor(nWord.id, null);
	
	//	remove DIVs from document
	if (word.div != null)
		removeElement(word.div);
	if (word.preDiv != null)
		removeElement(word.preDiv);
}

function uAddRegion(pageId, region, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	get affected page
	var page = pagesById[pageId];
	if (page == null) // page not displaying
		return;
	
	//	add region to page
	page.regions[page.regions.length] = region;
	uSortRegionArray(page.regions);
	uSetPageDirty(pageId);
}
function uRemoveRegion(type, pageId, boundsString, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	get affected page
	var page = pagesById[pageId];
	if (page == null) // page not displaying
		return;
	
	//	find region and remove it
	for (var r = 0; r < page.regions.length; r++) {
		if (page.regions[r].type != type)
			continue;
		if (boundsString != ('[' + page.regions[r].bounds.left + ',' + page.regions[r].bounds.right + ',' + page.regions[r].bounds.top + ',' + page.regions[r].bounds.bottom + ']'))
			continue;
		page.regions.splice(r, 1);
		uSetPageDirty(pageId);
		displayControlStaleDirty = true;
		break;
	}
}
function uAddAnnot(annot, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	set words
	annot.firstWord = wordsById[annot.firstWordId];
	annot.lastWord = wordsById[annot.lastWordId];
	
	//	set annot ID and update index
	annot.id = (annot.type + '@' + annot.firstWord.id + '-' + annot.lastWord.id);
	annotsById[annot.id] = annot;
	
	//	add annotation to document
	dDoc.annotations[dDoc.annotations.length] = annot;
	uSortAnnotArray(dDoc.annotations);
	
	//	add annot to pages
	for (var p = annot.firstWord.pageId; p <= annot.lastWord.pageId; p++) {
		var page = pagesById[p];
		if (!page.annots)
			page.annots = new Array();
		page.annots[page.annots.length] = annot;
		uSortAnnotArray(page.annots);
		uSetPageDirty(p);
	}
}
function uRemoveAnnot(type, firstWordId, lastWordId, updateId) {
	if (updateId <= lastUpdateId)
		return;
	else lastUpdateId = updateId;
	
	//	build annot ID and update index
	annotId = (type + '@' + firstWordId + '-' + lastWordId);
	delete annotsById[annotId];
	
	//	remove annotation from document
	for (var a = 0; a < dDoc.annotations.length; a++)
		if (dDoc.annotations[a].id == annotId) {
			dDoc.annotations.splice(a, 1);
			break;
		}
	
	//	remove annot from pages
	var firstWord = wordsById[firstWordId];
	var lastWord = wordsById[lastWordId];
	for (var p = firstWord.pageId; p <= lastWord.pageId; p++) {
		var page = pagesById[p];
		if (!page.annots)
			continue;
		for (var a = 0; a < page.annots.length; a++)
			if (page.annots[a].id == annotId) {
				page.annots.splice(a, 1);
				break;
			}
		uSetPageDirty(p);
	}
	displayControlStaleDirty = true;
}

function uSortAnnotArray(annots) {
	annots.sort(uCompareAnnots);
}
function uCompareAnnots(annot1, annot2) {
	//	different start or end pages
	if (annot1.firstWord.pageId != annot2.firstWord.pageId)
		return (annot1.firstWord.pageId - annot2.firstWord.pageId);
	if (annot1.lastWord.pageId != annot2.lastWord.pageId)
		return (annot2.lastWord.pageId - annot1.lastWord.pageId);
	
	//	compare first and last words
	var c = uCompareWords(annot1.firstWord, annot2.firstWord);
	if (c != 0)
		return c;
	c = uCompareWords(annot2.lastWord, annot1.lastWord);
	if (c != 0)
		return c;
	
	//	use type as last resort
	return annot1.type.localeCompare(annot2.type);
}
function uSortWordArray(words) {
	words.sort(uCompareWords);
}
function uCompareWords(word1, word2) {
	//	quick check
	if (word1 == word2)
		return 0;
	
	//	same text stream, compare page ID and position
	if (word1.textStreamId == word2.textStreamId)
		return ((word1.pageId == word2.pageId) ? (word1.textStreamPos - word2.textStreamPos) : (word1.pageId - word2.pageId));
	
	//	parse page IDs off text stream IDs and compare them
	var tshPid1 = parseInt(word1.textStreamId.substring(0, word1.textStreamId.indexOf('.')));
	var tshPid2 = parseInt(word2.textStreamId.substring(0, word2.textStreamId.indexOf('.')));
	if (tshPid1 != tshPid2)
		return (tshPid1 - tshPid2);
	
	//	simply compare bounding box strings, good enough for here
	var tshBb1 = word1.textStreamId.substr(word1.textStreamId.indexOf('.') + 1);
	var tshBb2 = word2.textStreamId.substr(word2.textStreamId.indexOf('.') + 1);
	return tshBb1.localeCompare(tshBb2);
}
function uSortRegionArray(regions) {
	regions.sort(uCompareRegions);
}
function uCompareRegions(reg1, reg2) {
	//	quick check
	if (reg1 == reg2)
		return 0;
	
	//	compare size
	var rs1 = ((reg1.bounds.right - reg1.bounds.left) * (reg1.bounds.bottom - reg1.bounds.top));
	var rs2 = ((reg2.bounds.right - reg2.bounds.left) * (reg2.bounds.bottom - reg2.bounds.top));
	if (rs1 != rs2)
		return (rs2 - rs1);
	
	//	resort to region types
	return reg1.type.localeCompare(reg2.type);
}

var displayControlMissingDirty = false;
var displayControlStaleDirty = false;
var dirtyPageIDs = null;
function uSetPageDirty(pageId) {
	if (dirtyPageIDs == null)
		dirtyPageIDs = new Object();
	dirtyPageIDs[pageId] = true;
}
function uFinishUpdate() {
	if (dirtyPageIDs != null) {
		for (var p in dirtyPageIDs)
			updatePageBackground(p, null, false);
		dirtyPageIDs = null;
	}
	
	if (displayControlMissingDirty || displayControlStaleDirty)
		updateDisplayControl(displayControlStaleDirty);
	displayControlMissingDirty = false;
	displayControlStaleDirty = false;
}
