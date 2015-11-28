var contextMenusByLevel = new Object();
var contextMenuOverlay = null;
function iShowContextMenu(items) {
	iDoShowContextMenu(items, selectionPageX, selectionPageX, selectionPageY, selectionPageY, 0);
}
function iDoShowContextMenu(items, lx, rx, ty, by, level) {
	
	//	clean up selection if level is 0 and items.length is 0
	if ((level == 0) && (items.length == 0))
		clearSelection();
	
	//	add glass pane
	if (level == 0) {
		contextMenuOverlay = getOverlay('contextMenuOverlay', 'contextMenuOverlay', true);
		contextMenuOverlay.onclick = function(event) {
			iCloseContextMenu();
			event.stopPropagation();
		};
		contextMenuOverlay.ondrop = function(event) {
			iCloseContextMenu();
		};
	}
	
	//	build menu
	var menuDiv = newElement('div', ('contextMenu_' + level), 'contextMenu');
	menuDiv.style.position = 'absolute';
	menuDiv.style.border = 'solid 1px #BBBBBB';
	for (var i = 0; i < items.length; i++) {
		var itemDiv = newElement('div', null, 'contextMenuItem', ((items[i].id == 'SEPARATOR') ? null : items[i].label));
		if (items[i].tooltip)
			itemDiv.title = items[i].tooltip;
		itemDiv.style.border = 'solid 1px #888888';
		itemDiv.style.backgroundColor = '#BBBBBB';
		itemDiv.style.whiteSpace = 'nowrap';
		if (items[i].id != 'SEPARATOR')
			iAddContextMenuHandlers(items[i], level, menuDiv, itemDiv);
		menuDiv.appendChild(itemDiv);
	}
	
	//	store menu by level for dynamic effects
	contextMenusByLevel[level] = menuDiv;
	
	//	show menu (off screen first, to measure it)
	menuDiv.style.left = (window.innerWidth + 2);
	menuDiv.style.top = (window.innerHeight + 2);
	contextMenuOverlay.appendChild(menuDiv);
	
	//	measure display to keep context menu inside it
	var display = getById('display');
	var minMenuTop = display.offsetTop;
	var maxMenuBottom = (display.offsetTop + display.offsetHeight);
	var minMenuLeft = display.offsetLeft;
	var maxMenuRight = (display.offsetLeft + display.offsetWidth);
	
	//	make sure context menu is in visible part of page vertically, and not higher than page proper
	if ((ty + menuDiv.offsetHeight) < maxMenuBottom)
		menuDiv.style.top = ty; // open straight downward
	else if ((by - menuDiv.offsetHeight) >= minMenuTop)
		menuDiv.style.top = (by - menuDiv.offsetHeight); // open straight upward
	else if (menuDiv.offsetHeight <= (maxMenuBottom - minMenuTop))
		menuDiv.style.top = (minMenuTop + (((maxMenuBottom - minMenuTop) - menuDiv.offsetHeight) / 2)); // open centered
	else {
		menuDiv.style.height = ((maxMenuBottom - minMenuTop - 10) + 'px');
		menuDiv.style.overflow = 'scroll';
		menuDiv.style.top = (minMenuTop + 5); // open centered
	}
	
	//	make sure context menu is in display horizontally, only now that we might have added a scroll bar
	if ((rx + menuDiv.offsetWidth) < maxMenuRight)
		menuDiv.style.left = rx; // open straight to the right
	else if ((lx - menuDiv.offsetWidth) >= minMenuLeft)
		menuDiv.style.left = (lx - menuDiv.offsetWidth); // open straight to the left
	else menuDiv.style.left = (minMenuLeft + (((maxMenuRight - minMenuLeft) - menuDiv.offsetWidth) / 2)); // open centered
}
function iAddContextMenuHandlers(item, level, menuDiv, itemDiv) {
	
	//	sub menu
	if (item.items) {
		itemDiv.onmouseover = function(event) {
			iCloseContextMenu(level + 1); // close sub menu(s) below our own level before opening new one
			iDoShowContextMenu(item.items, menuDiv.offsetLeft, (menuDiv.offsetLeft + menuDiv.offsetWidth), (menuDiv.offsetTop + itemDiv.offsetTop), (menuDiv.offsetTop + itemDiv.offsetTop + itemDiv.offsetHeight), (level + 1));
			event.stopPropagation();
		};
		itemDiv.onclick = function(event) {
			event.stopPropagation(); // stop event before it gets to glass pane
		};
	}
	
	//	plain menu item
	else {
		itemDiv.onmouseover = function(event) {
			iCloseContextMenu(level + 1); // close sub menu(s) below our own level
			event.stopPropagation();
		};
		itemDiv.onclick = function(event) {
			iUseContextMenuItem(item);
			event.stopPropagation();
		};
	}
}
function iUseContextMenuItem(item) {
	//alert('Doing ' + item.label + ', id is ' + item.id);
	if (item.twoClickLabel && item.twoClickHighlight)
		setPendingTwoClickAction(item);
	doContextMenuAction(item.id);
	iCloseContextMenu();
}
function iCloseContextMenu(level) {
	if (!level)
		level = 0;
	
	//	if level is 0, clean up selection
	if (level == 0)
		clearSelection();
	
	//	nothing to close
	if (!contextMenusByLevel[level])
		return;
	
	//	close sub menus first
	if (contextMenusByLevel[level + 1] != null)
		iCloseContextMenu(level + 1);
	
	//	close current level
	var menuDiv = contextMenusByLevel[level];
	removeElement(menuDiv);
	delete contextMenusByLevel[level];
	
	//	remove glass pane
	if (level == 0) {
		removeElement(contextMenuOverlay);
		contextMenuOverlay = null;
	}
}

var menusByLevel = new Object();
var menuOverlay = null;
function iShowMenu(items, lx, rx, ty, by) {
	iDoShowMenu(items, lx, rx, ty, by, 0);
}
function iDoShowMenu(items, lx, rx, ty, by, level) {
	
	//	add glass pane
	if ((level == 0) && (menuOverlay == null)) {
		menuOverlay = getOverlay('menuOverlay', 'menuOverlay', true);
		menuOverlay.onclick = function(event) {
			iCloseMenu(0, true);
			event.stopPropagation();
		};
		menuOverlay.onmouseover = function(event) {
			event.stopPropagation();
		};
		menuOverlay.ondrop = function(event) {
			iCloseMenu(0, true);
		};
	}
	
	//	build menu
	var menuDiv = newElement('div', ('menu_' + level), 'menu');
	menuDiv.style.position = 'absolute';
	menuDiv.style.border = 'solid 1px #BBBBBB';
	for (var i = 0; i < items.length; i++) {
		var itemDiv = newElement('div', null, 'menuItem', items[i].label);
		if (items[i].tooltip)
			itemDiv.title = items[i].tooltip;
		itemDiv.style.border = 'solid 1px #888888';
		itemDiv.style.padding = '3px';
		itemDiv.style.backgroundColor = '#BBBBBB';
		itemDiv.style.whiteSpace = 'nowrap';
		iAddMenuHandlers(items[i], level, menuDiv, itemDiv);
		menuDiv.appendChild(itemDiv);
	}
	
	//	store menu by level for dynamic effects
	menusByLevel[level] = menuDiv;
	
	//	show menu (off screen first, to measure it)
	menuDiv.style.left = (window.innerWidth + 2);
	menuDiv.style.top = (window.innerHeight + 2);
	menuOverlay.appendChild(menuDiv);
	
	//	measure display to keep context menu inside it
	var minMenuTop = 0;
	var maxMenuBottom = window.innerHeight;
	var minMenuLeft = 0;
	var maxMenuRight = window.innerWidth;
	
	//	make sure menu is in visible part of page vertically, and not higher than page proper
	if ((ty + menuDiv.offsetHeight) < maxMenuBottom)
		menuDiv.style.top = ty; // open straight downward
	else if ((by - menuDiv.offsetHeight) >= minMenuTop)
		menuDiv.style.top = (by - menuDiv.offsetHeight); // open straight upward
	else if (menuDiv.offsetHeight <= (maxMenuBottom - minMenuTop))
		menuDiv.style.top = (minMenuTop + (((maxMenuBottom - minMenuTop) - menuDiv.offsetHeight) / 2)); // open centered
	else {
		menuDiv.style.height = ((maxMenuBottom - minMenuTop - 10) + 'px');
		menuDiv.style.overflow = 'scroll';
		menuDiv.style.top = (minMenuTop + 5); // open centered
	}
	
	//	make sure menu is in display horizontally, only now that we might have added a scroll bar
	if ((rx + menuDiv.offsetWidth) < maxMenuRight)
		menuDiv.style.left = rx; // open straight to the right
	else if ((lx - menuDiv.offsetWidth) >= minMenuLeft)
		menuDiv.style.left = (lx - menuDiv.offsetWidth); // open straight to the left
	else menuDiv.style.left = (minMenuLeft + (((maxMenuRight - minMenuLeft) - menuDiv.offsetWidth) / 2)); // open centered
}
function iAddMenuHandlers(item, level, menuDiv, itemDiv) {
	
	//	sub menu
	if (item.items) {
		itemDiv.onmouseover = function(event) {
			iCloseMenu(level + 1); // close sub menu(s) below our own level before opening new one
			iDoShowMenu(item.items, menuDiv.offsetLeft, (menuDiv.offsetLeft + menuDiv.offsetWidth), (menuDiv.offsetTop + itemDiv.offsetTop), (menuDiv.offsetTop + itemDiv.offsetTop + itemDiv.offsetHeight), (level + 1));
			event.stopPropagation();
		};
		itemDiv.onclick = function(event) {
			event.stopPropagation(); // stop event before it gets to glass pane
		};
	}
	
	//	plain menu item
	else {
		itemDiv.onmouseover = function(event) {
			iCloseMenu(level + 1); // close sub menu(s) below our own level
			event.stopPropagation();
		};
		itemDiv.onclick = function(event) {
			iUseMenuItem(item);
			event.stopPropagation();
		};
	}
}
function iUseMenuItem(item) {
	//alert('Doing ' + item.label + ', id is ' + item.id);
	iCloseMenu(0, true);
	
	//	UNDO action, update menu
	if (('' + item.id).indexOf('UNDO-') == 0) {
		while (undoMenuItems.length != 0) {
			var umi = undoMenuItems[0];
			undoMenuItems.splice(0, 1);
			if (umi.id == item.id)
				break;
		}
		while (undoMenuItem.items.length != 0) {
			var umi = undoMenuItem.items[0];
			undoMenuItem.items.splice(0, 1);
			if (umi.id == item.id)
				break;
		}
		while ((undoMenuItem.items.length < 10) && (undoMenuItem.items.length < undoMenuItems.length))
			undoMenuItem.items[undoMenuItem.items.length] = undoMenuItems[undoMenuItem.items.length];
		
		if (undoMenuItem.items.length == 0)
			undoMenuItem.div.style.color = '#888888';
		else undoMenuItem.div.style.color = '#000000';
		
		//	trigger UNDO action via script
		doUndoAction(item.id);
	}
	
	//	export action, open download
	else if (('' + item.id).indexOf('EXP-') == 0) {
		iTriggerDownload(item.exportUrl);
	}
	
	//	edit action, process via script
	else if (('' + item.id).indexOf('EDT-') == 0) {
		doMainMenuAction(item.id, null);
	}
	
	//	tool action, process via script
	else if (('' + item.id).indexOf('TLS-') == 0) {
		doMainMenuAction(item.id, null);
	}
	
	//	help action, open help
	else if (('' + item.id).indexOf('HLP-') == 0) {
		showHelpChapter(item);
	}
	
	//	TODO view action, process via script
	else if (('' + item.id).indexOf('DVW-') == 0) {
		
	}
	
	//	file action, process via script, add send color settings as params
	else if (('' + item.id).indexOf('FL-') == 0) {
		var params = '';
		for (var at in annotColors)
			params = (params + '&ac.' + at + '=' + allAnnotColors[at]);
		for (var rt in regionColors)
			params = (params + '&rc.' + rt + '=' + allRegionColors[rt]);
		for (var tst in textStreamColors)
			params = (params + '&tsc.' + tst + '=' + textStreamColors[tst]);
		doMainMenuAction(item.id, params);
	}
}
function iCloseMenu(level, closeOverlay) {
	if (!level)
		level = 0;
	
	//	nothing to close
	if (!menusByLevel[level])
		return;
	
	//	close sub menus first
	if (menusByLevel[level + 1] != null)
		iCloseMenu(level + 1);
	
	//	close current level
	var menuDiv = menusByLevel[level];
	removeElement(menuDiv);
	delete menusByLevel[level];
	
	//	remove glass pane
	if ((level == 0) && closeOverlay) {
		removeElement(menuOverlay);
		menuOverlay = null;
		if (window.resetMainMenuSize)
			resetMainMenuSize();
	}
}
function iTriggerDownload(fileUrl) {
	window.open(fileUrl, 'exportWindow', 'width=300,height=200,top=0,left=0,resizable=yes,scrollbar=yes,scrollbars=yes', true);
	//window.location.assign(fileUrl);
}
