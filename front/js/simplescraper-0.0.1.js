/**
   SimpleScraper Front 0.0.1

   Copyright 2010, AUTHORS.txt
   Licensed under the MIT license.

**/
$(document).ready(function() {
	initialize();
    });

var addClass = 'add';
var deleteClass = 'delete';
var deletableClass = 'deletable';
var dataClass = 'data';

/*
  availableNamespaces(): Obtain the available namespaces, pass them to a callback as an array.
  callback: a function called back with the array when it is obtained.
 */
var availableNamespaces = function(callback) {
    $.ajax({
	    type: 'GET',
	    url: '/namespace/',
	    dataType: 'json',
	    success: function(data) { callback(data); }
	});
};

/*
  availableTypes(): Obtain the available types, pass them to a callback as an array.
  callback:  a function called back with the array when it is obtained.
*/
var availableTypes = function(callback) {
    $.ajax({
	    type: 'GET',
	    url: '/type/',
	    dataType: 'json',
	    success: function(data) { callback(data); }
	});
};

/*
  availableTypesForNamespace(): Obtain the already existing types within a namespace,
  pass them to a callback as an array.
  namespace: the namespace, as a string.
  callback: a function called back with the array when it is obtained.
*/
var availableTypesForNamespace = function(callback) {
    $.ajax({
	    type: 'GET',
	    url: '/' + namespace + '/type/',
	    dataType: 'json',
	    success: function(data) { callback(data); }
	});
};

/*
  informationFromData: create an information element on the screen from stored data.
  namespace: string namespace.
  type: string type.
  elem: element to append the information to.
*/
var informationFromData = function(namespace, type, elem) {
    $.ajax({
	    type: 'GET',
	    url: '/' + namespace + '/' + type,
	    dataType: 'json',
	    success: function(information) {
		var informationElem = $('<div>information</div>').addClass('information').addClass(deletableClass).append(deleteButton());
		namespaceElem = genericInput('namespace');
		typeElem = genericInput('type');
		defaultFieldsElem = $('<div>default fields</div>').attr('id', 'defaultField')
		.append(addButton(hashElem));
		gatherersElem = $('<div>gatherers</div>').attr('id', 'gatherer')
		.append(addButton(gathererFromData));
		toFieldsElem = $('<div>interpreters to fields</div>').attr('id', 'toField')
		.append(addButton(toFieldFromData));
		toInformationsElem = $('<div>interpreters to informations</div>').attr('id', 'toInformation')
		.append(addButton(toInformationFromData));

		namespaceElem.val(namespace);
		typeElem.val(type);
		
		if(information.defaultFields)
		    hashToElem(information.defaultFields, defaultFieldsElem);
    
		if(information.gatherers) {
		    information.gatherers.each(function(index) {
			    gatherersElem.append (gathererFromData(information.gatherers[index]));
			});
		}
		if(information.toFields) {
		    information.toFields.each(function(index) {
			    information.toFields.append(toFieldFromData(information.toFields[index]));
			});
		}
		if(information.toInformations) {
		    information.toInformations.each(function(index) {
			    information.toInformations.append(toInformationFromData(information.toInformations[index]))
			});
		}
		
		informationElem
		.append(namespaceElem)
		.append(typeElem)
		.append(defaultFieldsElem)
		.append(gatherersElem)
		.append(toFieldsElem)
		.append(toInformationsElem);
		.appendTo(elem);
	    }
	});
};

/*
  informationToData(informationElem): update the data on the server to be current with the Information currently
displayed on the screen.
  informationElem: the information element on the screen.
 */
var informationToData = function(informationElem) {
    var informationUrl = '/information/' + 	    
    informationElem.find('namespace').val() + '/' +
    informationElem.find('type').val();

    // Delete the existing information.
    $.ajax({
	    type: 'DELETE',
	    url: informationUrl
	});
    
    // Re-post it from scratch.
    $.ajax({
	    type: 'POST',
		url: informationUrl
	});

    // Add 'toField' data.
    $.ajax({
	    type: 'POST',
		
}

/*
  toFieldFromData: create a toField element on the screen from stored data.
  inputField: string inputField
  regex: string regular expression
  matchNumber: integer match number
  outputField: output field
*/
var toFieldFromData = function(inputField, regex, matchNumber, outputField) {
    var toFieldElem = $('<div>interpreter to field</div>').addClass('toField')
    .addClass(deletableClass).append(deleteButton());
    inputFieldElem = genericInput('inputField');
    regexElem = genericInput('regex');
    matchNumberElem = genericInput('matchNumber');
    outputFieldElem = genericInput('outputField');
    
    if(inputField)
	inputFieldElem.val(inputField);
    if(regex)
	regexElem.val(regex);
    if(matchNumber)
	matchNumberElem.val(matchNumber);
    if(outputField)
	outputFieldElem.val(outputField);
	    
    toFieldElem.append(inputFieldElem)
    .append(regexElem)
    .append(matchNumberElem)
    .append(outputFieldElem);
    return toFieldElem;
};

var toFieldToData = function(toFieldElem) {
};

/*
  toInformationFromData: create a toInformation element on the screen from stored data.
  inputField: string inputField
  regex: string regular expression
  outputNamespace: string namespace
  outputType: string type
  outputField: string field
*/

var toInformationFromData = function(inputField, regex, outputNamespace,
					outputType, outputField) {
    var toInformationElem = $('<div>interpreter to information</div>').addClass('toInformation')
    .addClass(deletableClass).append(deleteButton());
    inputFieldElem = genericInput('inputField');
    regexElem = genericInput('regex');
    outputNamespaceElem = genericInput('outputNamespace');
    outputTypeElem = genericInput('outputType');
    outputFieldElem = genericInput('outputField');

    if(inputField)
	inputElem.val(inputField);
    if(regex)
	regexElem.val(regex);
    if(outputNamespace)
	outputNamespaceElem.val(outputNamespace);
    if(outputType)
	outputTypeElem.val(outputType);
    if(outputField)
	outputFieldElem.val(outputField);
    
    toInformationElem
    .append(inputFieldElem)
    .append(regexElem)
    .append(outputNamespaceElem)
    .append(outputTypeElem)
    .append(outputFieldElem);
    return toInformationElem;
};

/* Convert a DOM toInformation object to data. */
var toInformationToData = function(toInformationElem) {
    //    toInformation
}

/*
  GathererFromData: create a gatherer object on the screen from stored data.
  urls: an array of urls
  gets: a hash of get variables
  posts: a hash of post variables
  headers: a hash of headers
  cookies: a hash of cookies
*/
    var gathererFromData = function(id, parents, urls, gets, posts, headers, cookies, parents) {
    var gathererElem = $('<div>gatherer</div>').addClass('gatherer')
    .addClass(deletableClass).append(deleteButton());
    var idElem = genericInput('id');
    var parentsElem = $('<div>parents</div>').attr('id', 'parent').append(addButton(arrayElem));
    var urlsElem = $('<div>urls</div>').attr('id', 'url').append(addButton(arrayElem));
    var getsElem = $('<div>gets</div>').attr('id', 'get').append(addButton(hashElem));
    var postsElem = $('<div>posts</div>').attr('id', 'post').append(addButton(hashElem));
    var headersElem = $('<div>headers</div>').attr('id', 'header').append(addButton(hashElem));
    var cookiesElem = $('<div>cookies</div>').attr('id', 'cookie').append(addButton(hashElem));
    
    if(id)
	idElem.val(id);

    if(parents)
	arrayToElem(parents, parentsElem);
    if(urls)
	arrayToElem(urls, urlsElem);
    
    if(gets)
	hashToElem(gets, getsElem);
    if(posts)
	hashToElem(posts, postsElem);
    if(headers)
	hashToElem(headers, headersElem);
    if(cookies)
	hashToElem(cookies, cookiesElem);

    gathererElem
    .append(idElem)
    .append(parentsElem)
    .append(urlsElem)
    .append(getsElem)
    .append(postsElem)
    .append(headersElem)
    .append(cookiesElem);
    return gathererElem;
};

/*
  Convert a gatherer DOM element back into an object.
  gatherer: a gatherer DOM element.
 */
var gathererToData = function(gathererElem) {
    var gatherer = {};
    gatherer.parents = [];
    gatherer.urls = [];
    gatherer.gets = {};
    gatherer.posts = {};
    gatherer.headers = {};
    gatherer.cookies = {};
    
    elemToArray(gathererElem.children('parents').first(), gatherer.parents);
    elemToArray(gathererElem.children('urls').first(), gatherer.urls);

    elemToHash(gathererElem.children('gets').first(), gatherer.gets);
    elemToHash(gathererElem.children('posts').first(), gatherer.posts);
    elemToHash(gathererElem.children('headers').first(), gatherer.headers);
    elemToHash(gathererElem.children('cookies').first(), gatherer.cookies);
    
    return gatherer;
}

/*
  genericInput: create a text input with the name and value specified.  If value is null,
  (name) is used.
 */
var genericInput = function(name, value) {
    if(value == null)
	value = '(' + name + ')';
    var elem =  $('<span>').append(name + ': ');
    var input = $('<input>').attr({
	    'type': 'text',
	    'name': name,
	    'id': name,
	    'value': value});
    elem.append(input);
    return elem;
};

/*
  deleteButton: deletes the parent element marked as deletable.
*/
var deleteButton = function() {
    return $('<span>').append('delete').addClass(deleteClass).click(function() {
	    var elemToDelete = $(this).closest('.' + deletableClass);
	    elemToDelete.remove();
	});
};

/*
  addButton: creates a piece of text that says 'add', and appends the callback to the parent when clicked.
*/
var addButton = function(callback) {
    return $('<span>add</span>').addClass(addClass)
    .click(function() {
	    $(this).parent().append(callback());
	});
};


/*
  Convert a hash to a DOM element with name:value input pairs.
 */
var hashToElem = function(hash, element) {
    $.each(hash, function(name) {
	    element.append(dataLineElem({'name': name, 'value': hash[name]}));
	});
};

/*
  Convert an array to a DOM element with its values in inputs.
 */
var arrayToElem = function(array, element) {
    $.each(array, function(index) {
	    element.append(dataLineElem({'value': array[index]}));
	});
};

/*
  Single line of inputs.
 */
var dataLineElem = function(inputs) {
    var elem = $('<div>').addClass(deletableClass).addClass(dataClass).append(deleteButton());
    $.each(inputs, function(inputName) {
	elem.append(genericInput(inputName, inputs[inputName]));
	});
    return elem;
};
var hashElem = function() {
    return dataLineElem({'name': null, 'value': null})};
var arrayElem = function() {
    return dataLineElem({'value': null})};

function initialize() {
    $('.generate').click(function() {
	    
	    $(this).parent().append(informationFromData());
	});

}