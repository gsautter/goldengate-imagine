function addSelectionHandlerWord(word) {
	word.div.onmousedown = function(event) {
		selectionStartWord = word;
		selectionEndWord = word;
		displayWordSelection();
		event.stopPropagation();
	};
	word.div.onmouseover = function(event) {
		if (selectionStartWord == null)
			return;
		if ((selectionEndWord.textStreamId != selectionStartWord.textStreamId) && (selectionEndWord.textStreamId != word.textStreamId))
			selectionEndWord.div.style.backgroundColor = null;
		selectionEndWord = word;
		displayWordSelection();
		event.stopPropagation();
	};
}
function addDropHandlerWord(word) {
	word.div.ondrop = function(event) {
		var data = event.dataTransfer.getData("text/plain");
		//alert(data);
		var params = '';
		if (paintTextStreams)
			params += '&textStreamsPainted=true';
		var annotTypes = getShowingAnnotTypes();
		if (annotTypes != null)
			params += ('&paintedAnnotTypes=' + annotTypes);
		var regionTypes = getShowingRegionTypes();
		if (regionTypes != null)
			params += ('&paintedRegionTypes=' + regionTypes);
		doDropAction(word.id, data, params);
		return false;
	};
}
function cancelDragDefault(event) {
	event.preventDefault();
}
function addSelectionHandlerPage(pageDiv, page, docDiv) {
	pageDiv.onmousedown = function(event) {
		clearWordSelection();
		selectionStartPoint = new Object();
		selectionStartPoint.x = (event.pageX - pageDiv.offsetLeft + docDiv.scrollLeft);
		selectionStartPoint.y = (event.pageY - pageDiv.offsetTop + docDiv.scrollTop);
		selectionStartPoint.pageId = page.id;
		selectionEndPoint = new Object();
		selectionEndPoint.x = selectionStartPoint.x;
		selectionEndPoint.y = selectionStartPoint.y;
		selectionEndPoint.pageId = page.id;
		displayBoxSelection();
	};
	pageDiv.onmousemove = function(event) {
		if (selectionStartPoint == null)
			return;
		if (selectionEndPoint.pageId == page.id) {
			selectionEndPoint.x = (event.pageX - pageDiv.offsetLeft + docDiv.scrollLeft);
			selectionEndPoint.y = (event.pageY - pageDiv.offsetTop + docDiv.scrollTop);
			displayBoxSelection();
		}
		else clearBoxSelection();
	};
	pageDiv.onmouseup = function(event) {
		
		//	remember point for context menu
		selectionPageX = event.pageX;
		selectionPageY = event.pageY;
		
		//	act on word selection
		if (selectionStartWord != null) {
			var params = ('&selStartWordId=' + selectionStartWord.id + '&selEndWordId=' + selectionEndWord.id);
			if (paintTextStreams)
				params += '&textStreamsPainted=true';
			var annotTypes = getShowingAnnotTypes();
			if (annotTypes != null)
				params += ('&paintedAnnotTypes=' + annotTypes);
			var regionTypes = getShowingRegionTypes();
			if (regionTypes != null)
				params += ('&paintedRegionTypes=' + regionTypes);
			//alert('Taking action on word selection. Params:\n' + params);
			showContextMenu(params);
		}
		
		//	act on box selection
		else if (selectionStartPoint != null) {
			var left = Math.min(selectionStartPoint.x, selectionEndPoint.x);
			left = ((left * page.imageDpi) / page.renderingDpi);
			var right = Math.max(selectionStartPoint.x, selectionEndPoint.x);
			right = ((right * page.imageDpi) / page.renderingDpi);
			var top = Math.min(selectionStartPoint.y, selectionEndPoint.y);
			top = ((top * page.imageDpi) / page.renderingDpi);
			var bottom = Math.max(selectionStartPoint.y, selectionEndPoint.y);
			bottom = ((bottom * page.imageDpi) / page.renderingDpi);
			var params = ('&selPageId=' + page.id + '&selBounds=[' + left + ',' + right + ',' + top + ',' + bottom + ']');
			if (paintTextStreams)
				params += '&textStreamsPainted=true';
			var annotTypes = getShowingAnnotTypes();
			if (annotTypes != null)
				params += ('&paintedAnnotTypes=' + annotTypes);
			var regionTypes = getShowingRegionTypes();
			if (regionTypes != null)
				params += ('&paintedRegionTypes=' + regionTypes);
			//alert('Taking action on box selection. Params:\n' + params);
			showContextMenu(params);
		}
	};
}
function getShowingAnnotTypes() {
	var types = null;
	for (var type in allAnnotColors)
		if (annotColors[type] && (annotColors[type] != null)) {
			if (types == null)
				types = type;
			else types += (';' + type);
		}
	return types;
}
function getShowingRegionTypes() {
	var types = null;
	for (var type in allRegionColors)
		if (regionColors[type] && (regionColors[type] != null)) {
			if (types == null)
				types = type;
			else types += (';' + type);
		}
	return types;
}

var pendingTwoClickAction = null;
var pendingTwoClickActionHighlight = null;
function setPendingTwoClickAction(action) {
	//	clean up any previous two-click action
	if (pendingTwoClickAction != null) {
		//	remove highlight div
		if (pendingTwoClickActionHighlight != null)
			removeElement(pendingTwoClickActionHighlight);
		pendingTwoClickActionHighlight = null;
		//	clear label
		setTwoClickActionLabel(null);
	}
	
	//	set and show new two-click action
	pendingTwoClickAction = action;
	if (pendingTwoClickAction != null) {
		//	add highlight div
		pendingTwoClickActionHighlight = newElement('div', 'pendingTwoClickActionHighlight', null, null);
		pendingTwoClickActionHighlight.style.position = 'absolute';
		pendingTwoClickActionHighlight.style.pointerEvents = 'none';
		pendingTwoClickActionHighlight.style.backgroundColor = 'rgba(0, 255, 0, 0.33)';
		var tcaHighlightPage = pagesById[pendingTwoClickAction.twoClickHighlight.pageId];
		pendingTwoClickActionHighlight.style.left = ((((pendingTwoClickAction.twoClickHighlight.left - tcaHighlightPage.bounds.left) * tcaHighlightPage.renderingDpi) / tcaHighlightPage.imageDpi) + 'px');
		pendingTwoClickActionHighlight.style.top = ((((pendingTwoClickAction.twoClickHighlight.top - tcaHighlightPage.bounds.top) * tcaHighlightPage.renderingDpi) / tcaHighlightPage.imageDpi) + 'px');
		pendingTwoClickActionHighlight.style.width = ((((pendingTwoClickAction.twoClickHighlight.right - pendingTwoClickAction.twoClickHighlight.left) * tcaHighlightPage.renderingDpi) / tcaHighlightPage.imageDpi) + 'px');
		pendingTwoClickActionHighlight.style.height = ((((pendingTwoClickAction.twoClickHighlight.bottom - pendingTwoClickAction.twoClickHighlight.top) * tcaHighlightPage.renderingDpi) / tcaHighlightPage.imageDpi) + 'px');
		tcaHighlightPage.div.appendChild(pendingTwoClickActionHighlight);
		//	show label
		setTwoClickActionLabel(pendingTwoClickAction.twoClickLabel);
	}
}
function setTwoClickActionLabel(label) {
	var tcaLabel = getById('twoClickActionLabel');
	while (tcaLabel.firstChild)
		removeElement(tcaLabel.firstChild);
	if (label == null)
		tcaLabel.style.display = 'none';
	else {
		tcaLabel.appendChild(document.createTextNode(label));
		tcaLabel.style.display = '';
	}
}
/*
"doing something" with selections:
- open overlay to block screen
- send selection to backend, along with display control settings (context/display sensitive actions !!!)
- receive context menu options
- build context menu
- close overlay
- show context menu
- open overlay to block screen
- send user selection to backend
- receive resulting data model updates (only ones relevant for browser UI)
- adjust local data model
- make changes show
- close overlay
*/
function clearSelection() {
	if (selectionStartWord != null)
		clearWordSelection();
	if (selectionStartPoint != null)
		clearBoxSelection();
}
var selectionPageX = 0;
var selectionPageY = 0;
var selectionStartWord = null;
var selectionEndWord = null;
function displayWordSelection() {
	if (selectionStartWord.textStreamId != selectionEndWord.textStreamId) {
		selectionStartWord.div.style.backgroundColor = 'rgba(0, 255, 0, 0.33)';
		selectionEndWord.div.style.backgroundColor = 'rgba(0, 255, 0, 0.33)';
		clearWordSelectionAround(selectionStartWord);
		clearWordSelectionAround(selectionEndWord);
	}
	else {
		var fsw;
		var lsw;
		if (selectionStartWord.pageId < selectionEndWord.pageId) {
			fsw = selectionStartWord;
			lsw = selectionEndWord;
		}
		else if (selectionStartWord.pageId > selectionEndWord.pageId) {
			fsw = selectionEndWord;
			lsw = selectionStartWord;
		}
		else if (selectionStartWord.textStreamPos < selectionEndWord.textStreamPos) {
			fsw = selectionStartWord;
			lsw = selectionEndWord;
		}
		else {
			fsw = selectionEndWord;
			lsw = selectionStartWord;
		}
		clearWordSelectionBefore(fsw);
		for (var w = fsw; w != null; w = w.nextWord) {
			getById(w.id).style.backgroundColor = 'rgba(0, 255, 0, 0.33)';
			if ((w != fsw) && w.preDiv)
				w.preDiv.style.backgroundColor = 'rgba(0, 255, 0, 0.33)';
			if (w == lsw)
				break;
		}
		clearWordSelectionAfter(lsw);
	}
}
function clearWordSelection() {
	if (selectionStartWord == null)
		return;
	if (selectionStartWord.textStreamId != selectionEndWord.textStreamId) {
		selectionStartWord.div.style.backgroundColor = null;
		selectionEndWord.div.style.backgroundColor = null;
		clearWordSelectionAround(selectionStartWord);
		clearWordSelectionAround(selectionEndWord);
	}
	else {
		var fsw;
		var lsw;
		if ((selectionStartWord.pageId < selectionEndWord.pageId) || ((selectionStartWord.pageId == selectionEndWord.pageId) && (selectionStartWord.textStreamPos < selectionEndWord.textStreamPos))) {
			fsw = selectionStartWord;
			lsw = selectionEndWord;
		}
		else if (selectionStartWord.pageId > selectionEndWord.pageId) {
			fsw = selectionEndWord;
			lsw = selectionStartWord;
		}
		else if (selectionStartWord.textStreamPos < selectionEndWord.textStreamPos) {
			fsw = selectionStartWord;
			lsw = selectionEndWord;
		}
		else {
			fsw = selectionEndWord;
			lsw = selectionStartWord;
		}
		clearWordSelectionBefore(fsw);
		for (var w = fsw; w != null; w = w.nextWord) {
			w.div.style.backgroundColor = null;
			if ((w != fsw) && w.preDiv)
				w.preDiv.style.backgroundColor = null;
			if (w == lsw)
				break;
		}
		clearWordSelectionAfter(lsw);
	}
	selectionStartWord = null;
	selectionEndWord = null;
	setPendingTwoClickAction(null);
}
function clearWordSelectionAround(word) {
	clearWordSelectionBefore(word);
	clearWordSelectionAfter(word);
}
function clearWordSelectionBefore(word) {
	if (word.preDiv)
		word.preDiv.style.backgroundColor = null;
	for (var w = word.prevWord; w != null; w = w.prevWord) {
		if (w.div.style.backgroundColor == null)
			break;
		w.div.style.backgroundColor = null;
		if (w.preDiv)
			w.preDiv.style.backgroundColor = null;
	}
}
function clearWordSelectionAfter(word) {
	for (var w = word.nextWord; w != null; w = w.nextWord) {
		if (w.div.style.backgroundColor == null)
			break;
		w.div.style.backgroundColor = null;
		if (w.preDiv)
			w.preDiv.style.backgroundColor = null;
	}
}
var selectionStartPoint = null;
var selectionEndPoint = null;
var selectionDiv = null;
function displayBoxSelection() {
	if (selectionDiv == null) {
		selectionDiv = newElement('div', 'selectionDiv', 'selection');
		selectionDiv.style.border = '3px solid red';
		selectionDiv.style.position = 'absolute';
		selectionDiv.style.width = '1px';
		selectionDiv.style.height = '1px';
		getById('page' + selectionStartPoint.pageId).appendChild(selectionDiv);
	}
	selectionDiv.style.left = Math.min(selectionStartPoint.x, selectionEndPoint.x);
	selectionDiv.style.width = (Math.max(selectionStartPoint.x, selectionEndPoint.x) - Math.min(selectionStartPoint.x, selectionEndPoint.x));
	selectionDiv.style.top = Math.min(selectionStartPoint.y, selectionEndPoint.y);
	selectionDiv.style.height = (Math.max(selectionStartPoint.y, selectionEndPoint.y) - Math.min(selectionStartPoint.y, selectionEndPoint.y));
}
function clearBoxSelection() {
	if (selectionDiv != null) {
		removeElement(selectionDiv);
		selectionDiv = null;
	}
	selectionStartPoint = null;
	selectionEndPoint = null;
	setPendingTwoClickAction(null);
}

var renderingDpi = 96;

function changeZoom() {
	var zc = getById('control_zoom');
	if (renderingDpi == zc.value)
		return;
	var display = getById('display');
	var zoomedScrollLeft = ((display.scrollLeft * zc.value) / renderingDpi);
	var zoomedScrollTop = ((display.scrollTop * zc.value) / renderingDpi);
	renderingDpi = zc.value;
	zoomPages();
	display.scrollLeft = zoomedScrollLeft;
	display.scrollTop = zoomedScrollTop;
}

function changePageDirection() {
	var pdc = getById('control_pageDirection');
	var display = getById('display');
	if (pdc.value == 'lr') {
		var relScrollPos = (display.scrollTop / display.scrollHeight);
		display.style.whiteSpace = 'nowrap';
		display.scrollLeft = (display.scrollWidth * relScrollPos);
	}
	else {
		var relScrollPos = (display.scrollLeft / display.scrollWidth);
		display.style.whiteSpace = null;
		display.scrollTop = (display.scrollHeight * relScrollPos);
	}
}

function zoomPages() {
	for (var pageId in pagesById)
		zoomPage(pageId);
}
function zoomPage(pageId) {
	var page = pagesById[pageId];
	if (page.renderingDpi == renderingDpi)
		return;
	page.renderingDpi = renderingDpi;
	
	//	zoom page DIV
	page.div.style.width = ((((page.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
	page.div.style.height = ((((page.bounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
	
	//	zoom page image
	page.img.style.width = ((((page.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
	page.img.style.height = ((((page.bounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
	
	//	zoom background image canvas
	page.bg.style.width = ((((page.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
	page.bg.style.height = ((((page.bounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
	page.bg.width = (((page.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	page.bg.height = (((page.bounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	
	//	zoom page words
	for (var w = 0; w < page.words.length; w++) {
		
		//	zoom word DIV
		page.words[w].div.style.left = ((((page.words[w].bounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
		page.words[w].div.style.top = ((((page.words[w].bounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
		page.words[w].div.style.width = ((((page.words[w].bounds.right - page.words[w].bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
		page.words[w].div.style.height = ((((page.words[w].bounds.bottom - page.words[w].bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
		
		//	zoom word space DIV
		if (page.words[w].preDiv) {
			page.words[w].preDiv.style.left = ((((page.words[w].prevWord.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 'px');
			page.words[w].preDiv.style.top = ((((page.words[w].bounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
			page.words[w].preDiv.style.width = ((((page.words[w].bounds.left - page.words[w].prevWord.bounds.right) * page.renderingDpi) / page.imageDpi) + 'px');
			page.words[w].preDiv.style.height = ((((page.words[w].bounds.bottom - page.words[w].bounds.top) * page.renderingDpi) / page.imageDpi) + 'px');
		}
	}
	
	//	draw words and regions
	updatePageBackground(pageId, null, false);
}

var textStreamColors = new Object();
var paintTextStreams = false;

function togglePaintTextStreams() {
	var pts = getById('control_paintTextStreams');
	if (pts.checked) {
		if (paintTextStreams)
			return;
		paintTextStreams = true;
		updatePageBackgrounds(null, false);
	}
	else {
		if (!paintTextStreams)
			return;
		paintTextStreams = false;
		updatePageBackgrounds(null, false);
	}
}

var allAnnotColors = new Object();
var annotColors = new Object();

function togglePaintAnnots(type) {
	var pa = getById('control_paintAnnots_' + type);
	if (pa.checked) {
		if (annotColors[type] != null)
			return;
		annotColors[type] = allAnnotColors[type];
		updatePageBackgrounds(type, true);
	}
	else {
		if (annotColors[type] == null)
			return;
		annotColors[type] = null;
		updatePageBackgrounds(type, true);
	}
}
function togglePaintAnnotsAll(paint) {
	for (var type in allAnnotColors) {
		var pa = getById('control_paintAnnots_' + type);
		if (pa == null)
			continue;
		annotColors[type] = (paint ? allAnnotColors[type] : null);
		pa.checked = paint;
	}
	updatePageBackgrounds(null, true);
}

var allRegionColors = new Object();
var regionColors = new Object();

function togglePaintRegions(type) {
	var pr = getById('control_paintRegions_' + type);
	if (pr.checked) {
		if (regionColors[type] != null)
			return;
		regionColors[type] = allRegionColors[type];
		updatePageBackgrounds(type, false);
	}
	else {
		if (regionColors[type] == null)
			return;
		regionColors[type] = null;
		updatePageBackgrounds(type, false);
	}
}
function togglePaintRegionsAll(paint) {
	for (var type in allRegionColors) {
		var pr = getById('control_paintRegions_' + type);
		if (pr == null)
			continue;
		regionColors[type] = (paint ? allRegionColors[type] : null);
		pr.checked = paint;
	}
	updatePageBackgrounds(null, false);
}

function updatePageBackgrounds(forType, isAnnotType) {
	for (var pageId in pagesById)
		updatePageBackground(pageId, forType, isAnnotType);
}
function updatePageBackground(pageId, forType, isAnnotType) {
	var page = pagesById[pageId];
	
	//	check if we have to update anything at all
	if (forType != null) {
		var noUpdate = true;
		
		//	check annotations
		if (isAnnotType && (page.annots != null)) {
			for (var a = 0; a < page.annots.length; a++)
				if (page.annots[a].type == forType) {
					noUpdate = false;
					break;
				}
		}
		
		//	check regions
		else if (!isAnnotType && (page.regions != null)) {
			for (var r = 0; r < page.regions.length; r++)
				if (page.regions[r].type == forType) {
					noUpdate = false;
					break;
				}
		}
		
		//	page not affected, save rendering effort
		if (noUpdate)
			return;
	}
	
	//	clear page background canvas
	var pageBgCtx = page.bg.getContext('2d');
	pageBgCtx.clearRect(0, 0, page.bg.width, page.bg.height);
	
	//	draw annotation highlights, counting starts and ends per word along the way
	var annotStartEndCounts = new Object();
	if (page.annots != null)
		for (var a = 0; a < page.annots.length; a++) {
			
			//	get highlight color (also indicates if annotation painted)
			var annotColor = annotColors[page.annots[a].type];
			if (annotColor == null)
				continue;
			
			//	count annotation starts and ends
			if (page.annots[a].firstWord.pageId == page.id) {
				if (!annotStartEndCounts['S' + page.annots[a].firstWord.id])
					annotStartEndCounts['S' + page.annots[a].firstWord.id] = 0;
				annotStartEndCounts['S' + page.annots[a].firstWord.id]++;
			}
			if (page.annots[a].lastWord.pageId == page.id) {
				if (!annotStartEndCounts['E' + page.annots[a].lastWord.id])
					annotStartEndCounts['E' + page.annots[a].lastWord.id] = 0;
				annotStartEndCounts['E' + page.annots[a].lastWord.id]++;
			}
			
			//	paint annotation highlight
			pageBgCtx.fillStyle = getTranslucentColor(annotColor, 0.25);
			for (var w = page.annots[a].firstWord; w != null; w = ((w == page.annots[a].lastWord) ? null : w.nextWord)) {
				if (w.pageId < page.id)
					continue;
				if (w.pageId > page.id)
					break;
				var wBoundsLeft;
				if ((w == page.annots[a].firstWord) || !w.prevWord || (w.prevWord == null) || (w.prevWord.pageId != w.pageId) || (w.prevWord.bounds.right > w.bounds.left) || (w.prevWord.bounds.top > w.bounds.bottom) || (w.prevWord.bounds.bottom < w.bounds.top))
					wBoundsLeft = w.bounds.left;
				else wBoundsLeft = w.prevWord.bounds.right;
				pageBgCtx.fillRect((((wBoundsLeft - page.bounds.left) * page.renderingDpi) / page.imageDpi),
					(((w.bounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi),
					(((w.bounds.right - wBoundsLeft) * page.renderingDpi) / page.imageDpi),
					(((w.bounds.bottom - w.bounds.top) * page.renderingDpi) / page.imageDpi));
			}
		}
	
	//	measure page margins
	var pageWordLeft = page.bounds.right;
	var pageWordRight = page.bounds.left;
	for (var w = 0; w < page.words.length; w++) {
		pageWordLeft = Math.min(pageWordLeft, page.words[w].bounds.left);
		pageWordRight = Math.max(pageWordRight, page.words[w].bounds.right);
	}
	
	//	create dummy bounds as anchors in top left and bottom right corners
	var topLeftDummy = new Object();
	topLeftDummy.left = (pageWordLeft / 2);
	topLeftDummy.right = (topLeftDummy.left + 1);
	topLeftDummy.top = 0;
	topLeftDummy.bottom = 1;
	var bottomRightDummy = new Object();
	bottomRightDummy.left = ((pageWordRight + page.bounds.right) / 2);
	bottomRightDummy.right = (bottomRightDummy.left + 1);
	bottomRightDummy.top = (page.bounds.bottom - 1);
	bottomRightDummy.bottom = (bottomRightDummy.top + 1);
	
	//	draw text streams
	for (var w = 0; w < page.words.length; w++) {
		
		//	draw word bounds
		pageBgCtx.strokeStyle = textStreamColors[page.words[w].textStreamType];
		var closeLeft = (!page.words[w].prevWord || (page.words[w].prevWord == null) || ((page.words[w].prevWord.nextRelation != 'H') && (page.words[w].prevWord.nextRelation != 'C')));
		var closeRight = (!page.words[w].nextWord || (page.words[w].nextWord == null) || ((page.words[w].nextRelation != 'H') && (page.words[w].nextRelation != 'C')));
		var paraStart = (((page.words[w].textStreamType != 'deleted') && (page.words[w].textStreamType != 'artifact')) && (!page.words[w].prevWord || (page.words[w].prevWord == null) || (page.words[w].prevWord.nextRelation == 'P')));
		var paraEnd = (((page.words[w].textStreamType != 'deleted') && (page.words[w].textStreamType != 'artifact')) && (!page.words[w].nextWord || (page.words[w].nextWord == null) || (page.words[w].nextRelation == 'P')));
		drawWordBox(page.words[w].bounds, page, closeLeft, closeRight, paraStart, paraEnd, pageBgCtx);
		
		//	no text stream connectors to paint
		if (!paintTextStreams)
			continue;
		
		//	no predecessor to connect to
		if (!page.words[w].prevWord || (page.words[w].prevWord == null)) {}
		
		//	predecessor on previous page, connect word to top left corner
		else if (page.words[w].prevWord.pageId != page.words[w].pageId)
			drawConnectorLineSequenceIntoPage(topLeftDummy, page.words[w].bounds, page, pageBgCtx);
		
		//	draw word connectors if text streams are painted
		else drawConnectorLineSequence(page.words[w].prevWord.bounds, page.words[w].bounds, page, pageBgCtx);
		
		//	no successor to connect to
		if (!page.words[w].nextWord || (page.words[w].nextWord == null)) {}
		
		//	successor on next page, connect word to bottom right corner
		else if (page.words[w].nextWord.pageId != page.words[w].pageId)
			drawConnectorLineSequenceOutOfPage(page.words[w].bounds, bottomRightDummy, page, pageBgCtx);
	}
	
	//	TODO check connector IDs instead of connectors proper, we might be showing an excerpt
	
	//	draw regions
	var pRegions = new Array();
	if (page.regions != null)
		for (var r = 0; r < page.regions.length; r++) {
			if (regionColors[page.regions[r].type] != null)
				pRegions[pRegions.length] = page.regions[r];
		}
	var pRegionOutdents = new Array();
	for (var r = 0; r < pRegions.length; r++)
		pRegionOutdents[r] = -1;
	for (var remaining = true; remaining;) {
		remaining = false;
		for (var r = 0; r < pRegions.length; r++) {
			if (pRegionOutdents[r] != -1)
				continue;
			var regionOutdent = 0;
			for (var cr = (r+1); cr < pRegions.length; cr++) {
				if (!boundsIncludeBounds(pRegions[r].bounds, pRegions[cr].bounds))
					continue;
				if (pRegionOutdents[cr] == -1) {
					regionOutdent = -1;
					break;
				}
				else regionOutdent = Math.max(regionOutdent, (pRegionOutdents[cr] + 1));
			}
			if (regionOutdent == -1)
				remaining = true;
			else pRegionOutdents[r] = regionOutdent;
		}
	}
	for (var r = 0; r < pRegionOutdents.length; r++)
		pRegionOutdents[r] *= 2;
	pageBgCtx.lineWidth = 1;
	for (var r = 0; r < pRegions.length; r++) {
		pageBgCtx.strokeStyle = regionColors[pRegions[r].type];
		pageBgCtx.strokeRect((((pRegions[r].bounds.left - page.bounds.left - pRegionOutdents[r]) * page.renderingDpi) / page.imageDpi),
			(((pRegions[r].bounds.top - page.bounds.top - pRegionOutdents[r]) * page.renderingDpi) / page.imageDpi),
			(((pRegions[r].bounds.right - pRegions[r].bounds.left + (pRegionOutdents[r] * 2)) * page.renderingDpi) / page.imageDpi),
			(((pRegions[r].bounds.bottom - pRegions[r].bounds.top + (pRegionOutdents[r] * 2)) * page.renderingDpi) / page.imageDpi));
	}
	
	//	draw annotation starts and ends
	if (page.annots != null)
		for (var a = 0; a < page.annots.length; a++) {
			
			//	get highlight color (also indicates if annotation painted)
			var annotColor = annotColors[page.annots[a].type];
			if (annotColor == null)
				continue;
			
			//	paint annotation start and end
			pageBgCtx.strokeStyle = annotColor;
			if (page.annots[a].firstWord.pageId == page.id) {
				drawAnnotationStart(page.annots[a].firstWord.bounds, page, annotStartEndCounts['S' + page.annots[a].firstWord.id], pageBgCtx);
				annotStartEndCounts['S' + page.annots[a].firstWord.id]--;
			}
			if (page.annots[a].lastWord.pageId == page.id) {
				drawAnnotationEnd(page.annots[a].lastWord.bounds, page, annotStartEndCounts['E' + page.annots[a].lastWord.id], pageBgCtx);
				annotStartEndCounts['E' + page.annots[a].lastWord.id]--;
			}
		}
}
function boundsIncludeBounds(outerBox, innerBox) {
	return ((outerBox.left <= innerBox.left) && (innerBox.right <= outerBox.right) && (outerBox.top <= innerBox.top) && (innerBox.bottom <= outerBox.bottom));
}
function boundsGetArea(bounds) {
	return ((bounds.right - bounds.left) * (bounds.bottom - bounds.top));
}
function drawWordBox(wordBounds, page, closeLeft, closeRight, paraStart, paraEnd, pageBgCtx) {
	if (closeLeft) {
		pageBgCtx.lineWidth = (paraStart ? 3 : 1);
		pageBgCtx.beginPath();
		pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (paraStart ? 1 : 0)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - (paraStart ? 2 : 0)));
		pageBgCtx.lineTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (paraStart ? 1 : 0)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 2 : 0)));
		pageBgCtx.stroke();
	}
	
	if (closeRight) {
		pageBgCtx.lineWidth = (paraEnd ? 3 : 1);
		pageBgCtx.beginPath();
		pageBgCtx.moveTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 1 : 0)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - (paraStart ? 2 : 0)));
		pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 1 : 0)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 2 : 0)));
		pageBgCtx.stroke();
	}
	
	pageBgCtx.lineWidth = (paraStart ? 3 : 1);
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (paraStart ? 2 : 0)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - (paraStart ? 1 : 0)));
	pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 2 : 0)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - (paraStart ? 1 : 0)));
	pageBgCtx.stroke();
	
	pageBgCtx.lineWidth = (paraEnd ? 3 : 1);
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (paraStart ? 2 : 0)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi)  + (paraEnd ? 1 : 0)));
	pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 2 : 0)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + (paraEnd ? 1 : 0)));
	pageBgCtx.stroke();
}
function drawConnectorLineSequence(from, to, page, pageBgCtx) {
	pageBgCtx.lineWidth = 1;
	pageBgCtx.beginPath();
	
	//	convert coordinates
	var fromLeft = (((from.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var fromRight = (((from.right - page.bounds.left) * page.renderingDpi) / page.imageDpi)
	var fromTop = (((from.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var fromBottom = (((from.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var toLeft = (((to.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var toRight = (((to.right - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var toTop = (((to.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var toBottom = (((to.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	
	//	rightward connection
	if (from.right <= to.left) {
		
		//	boxes adjacent in reading order, use simple horizontal line
		if ((from.top < to.bottom) && (from.bottom > to.top)) {
			pageBgCtx.moveTo(fromRight, ((fromTop + fromBottom + toTop + toBottom) / 4));
			pageBgCtx.lineTo(toLeft, ((fromTop + fromBottom + toTop + toBottom) / 4));
		}
		
		//	successor to upper right of predecessor (column break)
		else if (to.bottom <= from.top) {
			var fromRegion = null;
			for (var r = 0; r < page.regions.length; r++) {
				if (!boundsIncludeBounds(page.regions[r].bounds, from))
					continue;
				if (boundsIncludeBounds(page.regions[r].bounds, to))
					continue;
				if ((fromRegion == null) || (boundsGetArea(page.regions[r].bounds) > boundsGetArea(fromRegion.bounds)))
					fromRegion = page.regions[r];
			}
			var midFromRight = ((fromRegion == null) ? fromRight : (((fromRegion.bounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi));
			var toRegion = null;
			for (var r = 0; r < page.regions.length; r++) {
				if (!boundsIncludeBounds(page.regions[r].bounds, to))
					continue;
				if (boundsIncludeBounds(page.regions[r].bounds, from))
					continue;
				if ((toRegion == null) || (boundsGetArea(page.regions[r].bounds) > boundsGetArea(toRegion.bounds)))
					toRegion = page.regions[r];
			}
			var midToLeft = ((toRegion == null) ? toLeft : (((toRegion.bounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi));
			pageBgCtx.moveTo(fromRight, ((fromTop + fromBottom) / 2));
			pageBgCtx.lineTo(((midFromRight + midToLeft) / 2), ((fromTop + fromBottom) / 2));
			pageBgCtx.lineTo(((midFromRight + midToLeft) / 2), ((toTop + toBottom) / 2));
			pageBgCtx.lineTo(toLeft, ((toTop + toBottom) / 2));
		}
		
		//	successor to lower right of predecessor (block break)
		else {
			pageBgCtx.moveTo(fromRight, ((fromTop + fromBottom) / 2));
			pageBgCtx.lineTo(((fromRight + toLeft) / 2), ((fromTop + fromBottom) / 2));
			pageBgCtx.lineTo(((fromRight + toLeft) / 2), ((toTop + toBottom) / 2));
			pageBgCtx.lineTo(toLeft, ((toTop + toBottom) / 2));
		}
	}
	
	//	successor to left of predecessor, can only be lower (line break)
	else if ((from.right > to.left) && (from.bottom <= to.top)) {
		var outswing = Math.min(((toTop - fromBottom) / 2), (renderingDpi / 6));
		pageBgCtx.moveTo(fromRight, ((fromTop + fromBottom) / 2));
		pageBgCtx.lineTo((fromRight + outswing), ((fromTop + fromBottom) / 2));
		pageBgCtx.lineTo((fromRight + outswing), ((fromBottom + toTop) / 2));
		pageBgCtx.lineTo((toLeft - outswing), ((fromBottom + toTop) / 2));
		pageBgCtx.lineTo((toLeft - outswing), ((toTop + toBottom) / 2));
		pageBgCtx.lineTo(toLeft, ((toTop + toBottom) / 2));
	}
	
	//	finally ...
	pageBgCtx.stroke();
}
function drawConnectorLineSequenceIntoPage(from, to, page, pageBgCtx) {
	pageBgCtx.lineWidth = 1;
	pageBgCtx.beginPath();
	
	//	convert coordinates
	//var fromLeft = (((from.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var fromRight = (((from.right - page.bounds.left) * page.renderingDpi) / page.imageDpi)
	var fromTop = (((from.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	//var fromBottom = (((from.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var toLeft = (((to.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	//var toRight = (((to.right - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var toTop = (((to.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var toBottom = (((to.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	
	//	draw angled line down and right
	pageBgCtx.moveTo(fromRight, fromTop);
	pageBgCtx.lineTo(fromRight, ((toTop + toBottom) / 2));
	pageBgCtx.lineTo(toLeft, ((toTop + toBottom) / 2));
	
	//	finally ...
	pageBgCtx.stroke();
}
function drawConnectorLineSequenceOutOfPage(from, to, page, pageBgCtx) {
	pageBgCtx.lineWidth = 1;
	pageBgCtx.beginPath();
	
	//	convert coordinates
	//var fromLeft = (((from.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var fromRight = (((from.right - page.bounds.left) * page.renderingDpi) / page.imageDpi)
	var fromTop = (((from.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var fromBottom = (((from.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	var toLeft = (((to.left - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	//var toRight = (((to.right - page.bounds.left) * page.renderingDpi) / page.imageDpi);
	var toTop = (((to.top - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	//var toBottom = (((to.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi);
	
	//	draw angled line right and down
	pageBgCtx.moveTo(fromRight, ((fromTop + fromBottom) / 2));
	pageBgCtx.lineTo(toLeft, ((fromTop + fromBottom) / 2));
	pageBgCtx.lineTo(toLeft, toTop);
	
	//	finally ...
	pageBgCtx.stroke();
}
function drawAnnotationStart(wordBounds, page, out, pageBgCtx) {
	pageBgCtx.lineWidth = 2;
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (out * 2)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.lineTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (out * 2)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.stroke();
	
	pageBgCtx.lineWidth = 1;
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (out * 2)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.lineTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 2), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.stroke();
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) - (out * 2)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.lineTo(((((wordBounds.left - page.bounds.left) * page.renderingDpi) / page.imageDpi) + 2), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.stroke();
}
function drawAnnotationEnd(wordBounds, page, out, pageBgCtx) {
	pageBgCtx.lineWidth = 2;
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (out * 2)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (out * 2)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.stroke();
	
	pageBgCtx.lineWidth = 1;
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (out * 2)), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) - 2), ((((wordBounds.top - page.bounds.top) * page.renderingDpi) / page.imageDpi) - out));
	pageBgCtx.stroke();
	pageBgCtx.beginPath();
	pageBgCtx.moveTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) + (out * 2)), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.lineTo(((((wordBounds.right - page.bounds.left) * page.renderingDpi) / page.imageDpi) - 2), ((((wordBounds.bottom - page.bounds.top) * page.renderingDpi) / page.imageDpi) + out));
	pageBgCtx.stroke();
}
function getTranslucentColor(color, opacity){
    color = color.replace('#','');
    var r = parseInt(color.substring(0,2), 16);
    var g = parseInt(color.substring(2,4), 16);
    var b = parseInt(color.substring(4,6), 16);
    return ('rgba(' + r + ',' + g + ',' + b + ',' + opacity + ')');
}

function updateDisplayControl(clear) {
	var distinctRegionTypes = new Object();
	var regionTypes = new Array();
	for (var p = 0; p < dDoc.pages.length; p++)
		for (var r = 0; r < dDoc.pages[p].regions.length; r++) {
			if (distinctRegionTypes[dDoc.pages[p].regions[r].type])
				continue;
			distinctRegionTypes[dDoc.pages[p].regions[r].type] = 'true';
			regionTypes[regionTypes.length] = dDoc.pages[p].regions[r].type;
		}
	regionTypes.sort();
	
	var distinctAnnotTypes = new Object();
	var annotTypes = new Array();
	for (var a = 0; a < dDoc.annotations.length; a++) {
		if (distinctAnnotTypes[dDoc.annotations[a].type])
			continue;
		distinctAnnotTypes[dDoc.annotations[a].type] = 'true';
		annotTypes[annotTypes.length] = dDoc.annotations[a].type;
	}
	annotTypes.sort();
	
	var dcr = getById('control_regions');
	var dcrt = getById('control_regions_paintAllNone');
	if (clear) {
		while (dcrt.nextElementSibling)
			dcr.removeChild(dcrt.nextElementSibling);
	}
	var lastRtDiv = dcrt;
	for (var t = 0; t < regionTypes.length; t++) {
		var rtDiv = getById('control_regions_' + regionTypes[t]);
		if (rtDiv == null) {
			rtDiv = newElement('div', ('control_regions_' + regionTypes[t]), 'control_type');
			var rtToggle = newElement('input', ('control_paintRegions_' + regionTypes[t]), 'control_paintType');
			rtToggle.type = 'checkbox';
			rtToggle.value = regionTypes[t];
			if (regionColors[regionTypes[t]] != null)
				rtToggle.checked = 'checked';
			addToggleHandlerRegions(rtToggle, regionTypes[t]);
			rtDiv.appendChild(rtToggle);
			var rtColor = newElement('span', ('control_regionColor_' + regionTypes[t]), 'control_typeColor', regionTypes[t]);
			rtColor.style.backgroundColor = allRegionColors[regionTypes[t]];
			addColorHandlerRegions(rtColor, rtDiv, regionTypes[t]);
			rtDiv.appendChild(rtColor);
			if (clear)
				dcr.appendChild(rtDiv);
			else dcr.insertBefore(rtDiv, lastRtDiv.nextElementSibling);
		}
		lastRtDiv = rtDiv;
	}
	
	var dca = getById('control_annots');
	var dcat = getById('control_annots_paintAllNone');
	if (clear) {
		while (dcat.nextElementSibling)
			dca.removeChild(dcat.nextElementSibling);
	}
	var lastAtDiv = dcat;
	for (var t = 0; t < annotTypes.length; t++) {
		var atDiv = getById('control_annots_' + annotTypes[t]);
		if (atDiv == null) {
			atDiv = newElement('div', ('control_annots_' + annotTypes[t]), 'control_type');
			var atToggle = newElement('input', ('control_paintAnnots_' + annotTypes[t]), 'control_paintType');
			atToggle.type = 'checkbox';
			atToggle.value = annotTypes[t];
			if (annotColors[annotTypes[t]] != null)
				atToggle.checked = 'checked';
			addToggleHandlerAnnots(atToggle, annotTypes[t]);
			atDiv.appendChild(atToggle);
			var atColor = newElement('span', ('control_annotColor_' + annotTypes[t]), 'control_typeColor', annotTypes[t]);
			atColor.style.backgroundColor = allAnnotColors[annotTypes[t]];
			addColorHandlerAnnots(atColor, atDiv, annotTypes[t]);
			atDiv.appendChild(atColor);
			if (clear)
				dca.appendChild(atDiv);
			else dca.insertBefore(atDiv, lastAtDiv.nextElementSibling);
		}
		lastAtDiv = atDiv;
	}
}
function addToggleHandlerRegions(rtToggle, type) {
	rtToggle.onchange = function() {
		togglePaintRegions(type);
	};
}
function addToggleHandlerAnnots(atToggle, type) {
	atToggle.onchange = function() {
		togglePaintAnnots(type);
	};
}
function addColorHandlerRegions(rtColor, rtDiv, type) {
	var ci = newElement('input', null, null, null);
	ci.type = 'color';
	ci.value = allRegionColors[type];
	ci.style.border = '0px';
	ci.style.padding = '0px';
	ci.style.width = '0px';
	ci.style.height = '0px';
	ci.onchange = function() {
		rtColor.style.backgroundColor = ci.value;
		allRegionColors[type] = ci.value;
		if (regionColors[type] != null)
			regionColors[type] = ci.value;
		updatePageBackgrounds(type, false);
	};
	rtColor.onclick = function() {
		ci.click();
	};
	rtDiv.appendChild(ci);
}
function addColorHandlerAnnots(atColor, atDiv, type) {
	var ci = newElement('input', null, null, null);
	ci.type = 'color';
	ci.value = allAnnotColors[type];
	ci.style.border = '0px';
	ci.style.padding = '0px';
	ci.style.width = '0px';
	ci.style.height = '0px';
	ci.onchange = function() {
		atColor.style.backgroundColor = ci.value;
		allAnnotColors[type] = ci.value;
		if (annotColors[type] != null)
			annotColors[type] = ci.value;
		updatePageBackgrounds(type, true);
	};
	atColor.onclick = function() {
		ci.click();
	};
	atDiv.appendChild(ci);
}