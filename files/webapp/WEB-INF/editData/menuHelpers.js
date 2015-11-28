var mainMenuWidth = 5;
var mainMenuHeight = 5;
var mainMenuActiveWidth = 0;
var mainMenuActiveHeight = 0;
function addMainMenuItem(item, id) {
	var mainMenuDiv = getById('mainMenu');
	if (mainMenuDiv == null)
		return;
	
	//	measure main menu before first item is added
	if ((mainMenuActiveWidth == 0) && (mainMenuActiveHeight == 0)) {
		mainMenuWidth = Math.max(mainMenuWidth, mainMenuDiv.clientWidth);
		mainMenuHeight = Math.max(mainMenuHeight, mainMenuDiv.clientHeight);
	}
	
	//	create menu item
	var itemDiv = newElement('div', id, 'mainMenuItem', item.label);
	if (item.tooltip)
		itemDiv.title = item.tooltip;
	itemDiv.style.border = 'solid 1px #888888';
	itemDiv.style.padding = '3px';
	itemDiv.style.backgroundColor = '#BBBBBB';
	itemDiv.style.whiteSpace = 'nowrap';
	item.div = itemDiv;
	
	//	measure menu item without restraint of (possibly fixed width) main menu
	itemDiv.style.position = 'absolute';
	itemDiv.style.left = (window.innerWidth + 2);
	itemDiv.style.top = (window.innerHeight + 2);
	document.body.appendChild(itemDiv);
	var itemDivWidth = itemDiv.clientWidth;
	var itemDivHeight = itemDiv.clientHeight;
	removeElement(itemDiv);
	itemDiv.style.position = null;
	
	//	add item to main menu
	addMainMenuHandlers(item, mainMenuDiv, itemDiv);
	mainMenuDiv.appendChild(itemDiv);
	
	//	adjust expanded width of main menu
	mainMenuActiveWidth = Math.max(mainMenuActiveWidth, itemDivWidth);
	mainMenuActiveHeight = Math.max(mainMenuActiveHeight, itemDivHeight);
	
	//	add listeners to main menu, and adjust main body to no slip underneath collapsed main menu
	var mainDiv = getById('main');
	
	//	vertical main menu
	if (mainMenuDiv.offsetWidth < (window.innerWidth / 2)) {
		itemDiv.style.float = '';
		mainDiv.style.marginLeft = mainMenuDiv.offsetWidth;
		if (!window.resetMainMenuSize) {
			mainMenuDiv.onmouseover = function() {
				getById('mainMenu').style.width = mainMenuActiveWidth;
			};
			window.resetMainMenuSize = function() {
				getById('mainMenu').style.width = mainMenuWidth;
			}
			mainMenuDiv.onmouseout = function() {
				if (menuOverlay == null)
					resetMainMenuSize();
			};
		}
	}
	
	//	horizontal main menu
	else if (mainMenuDiv.offsetHeight < (window.innerHeight / 2)) {
		itemDiv.style.float = 'left';
		mainDiv.style.marginTop = mainMenuDiv.offsetHeight;
		if (!window.resetMainMenuSize) {
			mainMenuDiv.onmouseover = function() {
				getById('mainMenu').style.height = mainMenuActiveHeight;
			};
			window.resetMainMenuSize = function() {
				getById('mainMenu').style.height = mainMenuHeight;
			}
			mainMenuDiv.onmouseout = function() {
				if (menuOverlay == null)
					resetMainMenuSize();
			};
		}
	}
}
function addMainMenuHandlers(item, mainMenuDiv, itemDiv) {
	
	//	sub menu
	if (item.items) {
		itemDiv.onmouseover = function(event) {
			iCloseMenu(0, false); // close sub menu(s) below our own level before opening new one
			mainMenuDiv.onmouseover(); // make sure main menu is expanded before we position a sub menu
			if (mainMenuDiv.offsetWidth < (window.innerWidth / 2))
				iShowMenu(item.items, (mainMenuDiv.offsetLeft + mainMenuDiv.offsetWidth), (mainMenuDiv.offsetLeft + mainMenuDiv.offsetWidth), (mainMenuDiv.offsetTop + itemDiv.offsetTop), (mainMenuDiv.offsetTop + itemDiv.offsetTop + itemDiv.offsetHeight), 0);
			else if (mainMenuDiv.offsetHeight < (window.innerHeight / 2))
				iShowMenu(item.items, (mainMenuDiv.offsetLeft + itemDiv.offsetLeft), (mainMenuDiv.offsetLeft + itemDiv.offsetLeft), (mainMenuDiv.offsetTop + mainMenuDiv.offsetHeight), (mainMenuDiv.offsetTop + mainMenuDiv.offsetHeight), 0);
			event.stopPropagation();
		};
		itemDiv.onclick = function() {
			event.stopPropagation(); // stop event before it gets to glass pane
		};
	}
	
	//	plain menu item
	else {
		itemDiv.onmouseover = function(event) {
			mainMenuDiv.onmouseover(); // make sure main menu is expanded before we position a sub menu
			iCloseMenu(0, false); // close sub menu(s) below our own level
			event.stopPropagation();
		};
		itemDiv.onclick = function() {
			iUseMenuItem(item);
			event.stopPropagation(); // stop event before it gets to glass pane
		};
	}
}
var helpWindow = null;
function showHelpChapter(item) {
	
	//	open help window if not open
	if ((helpWindow == null) || helpWindow.closed)
		helpWindow = window.open((item.contentPath + '?getHelpWindow=true'), 'helpWindow', 'width=500,height=400,top=100,left=100,resizable=yes,scrollbar=yes,scrollbars=yes', true);
	
	//	show help page
	else {
		helpWindow.focus();
		helpWindow.hShowChapter(item.id);
	}
}
function closeEditor() {
	//	TODO figure out a page to forward to, as browsers refuse the window.close() command if window wasn't opened by script ...
	//	... which it will have been in any deployment scenario, however
	if (window.opener) {
		//	TODO refresh window editor was opened from ('window.opener'), appending '&cacheControl=force' to URL if not already present
		//	==> shows newly marked materials citations right away
	}
	window.close();
}
