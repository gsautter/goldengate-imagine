var helpChaptersById = new Object();
var helpTreeLevelsOpen = new Array();
var helpChapterOpen = null;
function hBuildChapterTree() {
	hAddChapter(helpRootChapter, 0);
	hTreeExpand(helpRootChapter);
}
//var hChildrenNone = '&#x25CF;'; // small black circle
var hChildrenNone = '&#x25C6;'; // black diamond (fits triangle better proportionally)
var hChildrenCollapsed = '&#x25B6;'; // black right-pointing triangle
var hChildrenExpanded = '&#x25BC;'; // black down-pointing triangle
function hAddChapter(chapter, level) {
	chapter.level = level;
	helpChaptersById[chapter.id] = chapter;
	
	var cEntry = newElement('span', null, 'helpTreeEntry', null);
	chapter.treeEntry = cEntry;
	
	var cToggle = newElement('span', null, 'helpTreeToggle', null);
	cToggle.innerHTML = (chapter.subChapters ? hChildrenCollapsed : hChildrenNone);
	chapter.treeToggle = cToggle;
	cEntry.appendChild(cToggle);
	
	var cTitle = newElement('span', null, 'helpTreeTitle', chapter.title);
	chapter.treeTitle = cTitle;
	cEntry.appendChild(cTitle);
	
	hAddChapterHandlers(chapter)
	
	var cLine = newElement('div', null, ('helpTreeLine helpTreeLineLevel' + level), null);
	cLine.style.display = ((level == 0) ? '' : 'none');
	cLine.style.paddingLeft = ((15 * level) + 'px');
	cLine.appendChild(cEntry);
	chapter.treeLine = cLine;
	
	var treeDiv = getById('helpTree');
	treeDiv.appendChild(cLine);
	
	if (chapter.subChapters)
		for (var c = 0; c < chapter.subChapters.length; c++) {
			chapter.subChapters[c].parentChapter = chapter;
			hAddChapter(chapter.subChapters[c], (level + 1));
		}
}
function hAddChapterHandlers(chapter) {
	if (chapter.subChapters)
		chapter.treeToggle.onclick = function(event) {
			if (helpTreeLevelsOpen[chapter.level] == chapter)
				hTreeCollapse(chapter);
			else hTreeExpand(chapter);
		};
	chapter.treeTitle.onclick = function(event) {
		hShowChapter(chapter.id);
		event.stopPropagation();
	};
}
function hTreeExpand(chapter) {
	if (helpTreeLevelsOpen[chapter.level] == chapter)
		return;
	
	//	collapse to own level
	if (helpTreeLevelsOpen[chapter.level] != null)
		hTreeCollapse(helpTreeLevelsOpen[chapter.level]);
	
	//	expand parent chapters
	if (chapter.parentChapter)
		hTreeExpand(chapter.parentChapter);
	
	//	show sub chapters
	for (var c = 0; c < chapter.subChapters.length; c++)
		chapter.subChapters[c].treeLine.style.display = '';
	
	//	add to open chapters index
	helpTreeLevelsOpen[chapter.level] = chapter;
	
	//	adjust toggle
	chapter.treeToggle.innerHTML = hChildrenExpanded;
}
function hTreeCollapse(chapter) {
	if (helpTreeLevelsOpen[chapter.level] != chapter)
		return;
	
	//	collapse children first, recursively
	if (helpTreeLevelsOpen[chapter.level + 1])
		hTreeCollapse(helpTreeLevelsOpen[chapter.level + 1]);
	
	//	hide sub chapters
	for (var c = 0; c < chapter.subChapters.length; c++)
		chapter.subChapters[c].treeLine.style.display = 'none';
	
	//	remove from open chapters index
	helpTreeLevelsOpen.splice(chapter.level, 1);
	
	//	adjust toggle
	chapter.treeToggle.innerHTML = hChildrenCollapsed;
}
function hShowChapter(chapterId) {
	var chapter = helpChaptersById[chapterId];
	if (chapter == null)
		return;
	
	//	expand parent chapter in tree, as well as own chapter if sub chapters exist
	if (chapter.parentChapter)
		hTreeExpand(chapter.parentChapter);
	if (chapter.subChapters)
		hTreeExpand(chapter);
	
	
	//	show help in content IFRAME
	getById('helpContent').src = chapter.path;
	
	//	change tree chapter highlight
	if (helpChapterOpen != null)
		helpChapterOpen.treeEntry.style.backgroundColor = null;
	helpChapterOpen = chapter;
	helpChapterOpen.treeEntry.style.backgroundColor = '#FFFF99';
}