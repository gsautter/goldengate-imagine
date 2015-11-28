var docList = new Array();
var docListFields = [
	{"name": "opts", "label": "Options"},
	{"name": "name", "label": "Document Name"},
	{"name": "description", "label": "Description"},
	{"name": "created", "label": "Created"},
	{"name": "updated", "label": "Last Updated"}
];
var docListSortField = 'docName';
var docListSortReversed = false;
var docListHeaderRow = null;
function sortDocuments(sortField) {
	var sortReversed = ((sortField == docListSortField) && !docListSortReversed);
	
	//	sort document list
	docList.sort(function(doc1, doc2) {
		return compareDocuments(doc1, doc2, sortField);
	});
	docListSortField = sortField;
	
	//	update display table
	var docListTable = getById('documentListTable');
	for (var d = 0; d < docList.length; d++)
		removeElement(docList[d].tr);
	for (var d = 0; d < docList.length; d++)
		docListTable.appendChild(docList[d].tr);
}
function ensureDocumentListHeaderRow() {
	if (docListHeaderRow != null)
		return;
	var docListHeaderRow = newElement('tr', null, 'documentListHeaderRow', null);
	for (var f = 0; f < docListFields.length; f++) {
		var docListHeaderCell = newElement('td', null, 'documentListHeaderCell', docListFields[f].label);
		if (docListFields[f].name != 'opts')
			addDocumentListHeaderCellHandler(docListHeaderCell, docListFields[f].name);
		docListHeaderRow.appendChild(docListHeaderCell);
	}
	getById('documentListTable').appendChild(docListHeaderRow);
}
function addDocumentListHeaderCellHandler(docListHeaderCell, docListFieldName) {
	docListHeaderCell.onclick = function(event) {
		sortDocuments(docListFieldName);
	};
}
function addDocument(doc) {
	ensureDocumentListHeaderRow();
	
	//	build document table row
	doc.tr = newElement('tr', null, 'documentListRow', null);
	for (var f = 0; f < docListFields.length; f++) {
		var docListCell = newElement('td', null, 'documentListCell', ((docListFields[f].name == 'opts') ? null : doc[docListFields[f].name]));
		if (docListFields[f].name == 'opts') {
			var downloadButton = newElement('button', null, 'documentListButton', 'Download');
			addDocumentListButtonHandlerDownload(downloadButton, doc['id']);
			docListCell.appendChild(downloadButton);
			var deleteButton = newElement('button', null, 'documentListButton', 'Delete');
			addDocumentListButtonHandlerDelete(deleteButton, doc['id']);
			docListCell.appendChild(deleteButton);
		}
		else if (docListFields[f].name == 'name')
			addDocumentListCellHandlerEdit(docListCell, doc['id']);
		doc.tr.appendChild(docListCell);
	}
	
	//	add document to list via insertion sort by current sort field ...
	//	... and add table row to table right before that of successor in array
	for (var d = 0; d < docList.length; d++)
		if ((compareDocuments(doc, docList[d], docListSortField) * (docListSortReversed ? -1 : 1)) < 0) {
			var docAfter = docList[d];
			docList.splice(d, 0, doc);
			getById('documentListTable').insertBefore(doc.tr, docAfter.tr);
			doc = null;
			break;
		}
	if (doc != null) {
		docList[docList.length] = doc;
		getById('documentListTable').appendChild(doc.tr);
	}
}
function addDocumentListButtonHandlerDownload(downloadButton, docId) {
	downloadButton.title = 'Download the document';
	downloadButton.onclick = function(event) {
		downloadDocument(docId)
	};
}
function addDocumentListButtonHandlerDelete(deleteButton, docId) {
	deleteButton.title = 'Delete the document';
	deleteButton.onclick = function(event) {
		deleteDocument(docId)
	};
}
function addDocumentListCellHandlerEdit(docListCell, docId) {
	docListCell.title = 'Click to open document for editing';
	docListCell.onclick = function(event) {
		editDocument(docId)
	};
}
function removeDocument(docId) {
	for (var d = 0; d < docList.length; d++)
		if (docList[d] == docId) {
			var doc = docList[d];
			docList.splice(d, 1);
			removeElement(doc.tr);
			break;
		}
}
function clearDocuments() {
	for (var d = 0; d < docList.length; d++)
		removeElement(docList[d].tr);
	docList.splice(0, docList.length);
}
function compareDocuments(doc1, doc2, compareField) {
	var val1 = doc1[compareField];
	var val2 = doc2[compareField];
	if (val1 && val2)
		return val1.localeCompare(val2);
	else if (val1)
		return -1;
	else if (val2)
		return 1;
	else return 0;
}