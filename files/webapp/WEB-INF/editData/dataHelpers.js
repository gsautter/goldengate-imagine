var dDoc = null;

var pagesById = new Object();
function addPage(page) {
	pagesById[page.id] = page;
	page.renderingDpi = 0;
}

var wordsById = new Object();
var textStreamHeads = new Array();
function addWord(pageId, word) {
	word.pageId = pageId;
	word.id = ('' + word.pageId + '.[' + word.bounds.left + ',' + word.bounds.right + ',' + word.bounds.top + ',' + word.bounds.bottom + ']');
	wordsById[word.id] = word;
	if (word.prevWordId) {
		word.prevWord = wordsById[word.prevWordId];
		word.prevWord.nextWord = word;
		word.textStreamId = word.prevWord.textStreamId;
		word.textStreamType = word.prevWord.textStreamType;
		word.textStreamPos = ((word.pageId == word.prevWord.pageId) ? (word.prevWord.textStreamPos + 1) : 0);
	}
	else {
		word.textStreamId = word.id;
		word.textStreamPos = 0;
		textStreamHeads[textStreamHeads.length] = word;
	}
	if (!word.nextRelation)
		word.nextRelation = 'S';
}

var annotsById = new Object();
function addAnnotation(annot) {
	annot.firstWord = wordsById[annot.firstWordId];
	annot.lastWord = wordsById[annot.lastWordId];
	annot.id = (annot.type + '@' + annot.firstWord.id + '-' + annot.lastWord.id);
	annotsById[annot.id] = annot;
	for (var p = annot.firstWord.pageId; p <= annot.lastWord.pageId; p++) {
		var page = pagesById[p];
		if (!page.annots)
			page.annots = new Array();
		page.annots[page.annots.length] = annot;
	}
}

function setDocument(doc) {
	
	//	clean up
	for (var aid in annotsById)
		delete annotsById[aid];
	for (var wid in wordsById) {
		removeElement(wordsById[wid].div);
		if (wordsById[wid].preDiv)
			removeElement(wordsById[wid].preDiv);
		delete wordsById[wid];
	}
	textStreamHeads.splice(0, textStreamHeads.length);
	for (var pid in pagesById) {
		removeElement(pagesById[pid].div);
		delete pagesById[pid];
	}
	
	//	store new document
	dDoc = doc;
	
	//	add pages
	for (var p = 0; p < dDoc.pages.length; p++) {
		addPage(dDoc.pages[p]);
		
		//	add page words
		for (var w = 0; w < dDoc.pages[p].words.length; w++)
			addWord(dDoc.pages[p].id, dDoc.pages[p].words[w]);
	}
	
	//	add annotations
	for (var a = 0; a < dDoc.annotations.length; a++)
		addAnnotation(dDoc.annotations[a]);
	
	//	display document
	var displayDiv = getById('display');
	for (var p = 0; p < dDoc.pages.length; p++) {
		
		//	create page DIV
		var pageDiv = newElement('div', ('page' + dDoc.pages[p].id), 'page');
		pageDiv.style.position = 'relative';
		pageDiv.style.border = '1px solid gray';
		pageDiv.style.margin = '16px';
		pageDiv.style.display = 'inline-block';
		display.appendChild(pageDiv);
		dDoc.pages[p].div = pageDiv;
		
		//	add page image (fetch from backend via image src URL)
		var pageImg = newElement('img', ('pageImage' + dDoc.pages[p].id), 'pageImage');
		setAttribute(pageImg, 'style', 'pointer-events: none;');
		pageImg.src = (dDoc.pages[p].imagePath);
		pageImg.style.position = 'absolute';
		pageDiv.appendChild(pageImg);
		dDoc.pages[p].img = pageImg;
		
		//	add background image canvas
		var pageBg = newElement('canvas', ('pageBg' + dDoc.pages[p].id), 'pageBg');
		pageBg.style.position = 'absolute';
		pageDiv.appendChild(pageBg);
		dDoc.pages[p].bg = pageBg;
		
		//	add page words
		for (var w = 0; w < dDoc.pages[p].words.length; w++) {
			
			//	create word DIV
			var wordDiv = newElement('div', dDoc.pages[p].words[w].id, 'word');
			wordDiv.style.position = 'absolute';
			pageDiv.appendChild(wordDiv);
			dDoc.pages[p].words[w].div = wordDiv;
			
			//	add mouse listeners for word selection
			addSelectionHandlerWord(dDoc.pages[p].words[w]);
			
			//	add mouse listeners for word selection
			addDropHandlerWord(dDoc.pages[p].words[w]);
			
			//	no predecessor
			if (!dDoc.pages[p].words[w].prevWord || (dDoc.pages[p].words[w].prevWord == null))
				continue;
			//	predecessor on other page
			if (dDoc.pages[p].words[w].prevWord.pageId != dDoc.pages[p].words[w].pageId)
				continue;
			//	predecessor above
			if (dDoc.pages[p].words[w].prevWord.bounds.bottom < dDoc.pages[p].words[w].bounds.top)
				continue;
			//	predecessor below
			if (dDoc.pages[p].words[w].prevWord.bounds.top > dDoc.pages[p].words[w].bounds.bottom)
				continue;
			//	no space after predecessor
			if (dDoc.pages[p].words[w].prevWord.bounds.right >= dDoc.pages[p].words[w].bounds.left)
				continue;
			
			//	create word space DIV
			var preWordDiv = newElement('div', ('pre.' + dDoc.pages[p].words[w].id), 'word');
			preWordDiv.style.position = 'absolute';
			pageDiv.appendChild(preWordDiv);
			dDoc.pages[p].words[w].preDiv = preWordDiv;
		}
		
		//	add mouse listeners for box selection
		addSelectionHandlerPage(pageDiv, dDoc.pages[p], displayDiv);
		
		//	set zoom, and draw words and regions
		zoomPage(dDoc.pages[p].id);
	}
	
	updateDisplayControl(true);
}