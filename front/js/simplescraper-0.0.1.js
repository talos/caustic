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
  availableAreas(): Obtain the available areas, pass them to a callback as an array.
  callback: a function called back with the array when it is obtained.
 */
var availableAreas = function(callback) {
    $.ajax({
	    type: 'GET',
	    url: '/area/',
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
  availableAreasForType(): Obtain the already existing areas within a type,
  pass them to a callback as an array.
  area: the area, as a string.
  callback: a function called back with the array when it is obtained.
*/
var availableTypesForArea = function(callback, type) {
    $.ajax({
	    type: 'GET',
	    url: '/type/' + type,
	    dataType: 'json',
	    success: function(data) { callback(data['areas']); }
	});
};

/*
  informationFromData: create an information element on the screen from stored data.
  area: string area.
  type: string type.
  elem: element to append the information to.
*/
var informationFromData = function(area, type, elem) {
    $.ajax({
	    type: 'GET',
	    url: '/' + area + '/' + type,
	    dataType: 'json',
	    success: function(information) {
		var informationElem = $('<div>information</div>').addClass('information').addClass(deletableClass).append(deleteButton());
		//		areaElem = genericInput('area');
		//		typeElem = genericInput('type');
		
		var areasElem = $('<div>areas</div>').attr('id', 'area');
		var typeElem = $('<div>type</div>').attr('id', 'type');
		
		var defaultFieldsElem = $('<div>default fields</div>').attr('id', 'default_field')
		.append(addButton(hashElem));
		var gatherersElem = $('<div>gatherers</div>').attr('id', 'gatherer')
		.append(addButton(gathererFromData));
		var toFieldsElem = $('<div>interpreters to fields</div>').attr('id', 'to_field')
		.append(addButton(toFieldFromData));
		var toInformationsElem = $('<div>interpreters to informations</div>').attr('id', 'to_information')
		.append(addButton(toInformationFromData));
		
		//		areasElem.val(area);
		//		typeElem.val(type);
		
		if(information.areas) {
		    for(area in information.areas) {
			areasElem.append('<span>' + area + '</span>');
		    }
		}
		if(information.type) {
		    
		}

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
		.append(areasElem)
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
  toFieldFromData: create a toField element on the screen from stored data.
  toField: toField object.
*/
var toFieldFromData = function(toField) {
    var inputField = toField['input_field'];
    var regex = toField['regex'];
    var matchNumber = toField['match_number'];
    var outputField = toField['output_field'];

    var toFieldElem = $('<div>interpreter to field</div>').addClass('toField')
    .addClass(deletableClass).append(deleteButton());
    inputFieldElem = genericInput('input_field');
    regexElem = genericInput('regex');
    matchNumberElem = genericInput('match_number');
    outputFieldElem = genericInput('output_field');
    
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

/*
  toInformationFromData: create a toInformation element on the screen from stored data.
  toInformation: object
*/

var toInformationFromData = function(toInformation) {
    var inputField = toInformation['input_field'];
    var regex = toInformation['regex'];
    var destinationArea = toInformation['destination_area'];
    var destinationType = toInformation['destination_type'];
    var destinationField = toInformation['destination_field'];
    
    var toInformationElem = $('<div>interpreter to information</div>').addClass('toInformation')
    .addClass(deletableClass).append(deleteButton());
    var inputFieldElem = genericInput('input_field');
    var regexElem = genericInput('regex');
    var destinationAreaElem = genericInput('destination_area');
    var destinationTypeElem = genericInput('destination_type');
    var destinationFieldElem = genericInput('destination_field');

    if(inputField)
	inputElem.val(inputField);
    if(regex)
	regexElem.val(regex);
    if(destinationArea)
	destinationAreaElem.val(destinationArea);
    if(destinationType)
	destinationTypeElem.val(destinationType);
    if(destinationField)
	destinationFieldElem.val(destinationField);
    
    toInformationElem
    .append(inputFieldElem)
    .append(regexElem)
    .append(destinationAreaElem)
    .append(destinationTypeElem)
    .append(destinationFieldElem);
    return toInformationElem;
};

/*
  GathererFromData: create a gatherer object on the screen from stored data.
  gatherer: object
*/
var gathererFromData = function(gatherer) {
    var id = gatherer['id'];
    var urls = gatherer['urls'];
    var gets = gatherer['gets'];
    var posts = gatherer['posts'];
    var headers = gatherer['headers'];
    var cookies = gatherer['cookies'];

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
  genericDropdown: create a dropdown with the specified available values.
*/
var genericDropdown = function(options) {
    var elem = $('<select>');
    if(options) {
	for(option in options) {
	    elem.append($('<option>' + option + '</option>'));
	}
    }
    return elem;
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