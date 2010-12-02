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
var modifierClass = 'modifier';
var objectClass = 'object';

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

/*

 */
var getObjectData = function(location, callback) {
    $.ajax({
	    type: 'GET',
	    url: '/' + location,
	    dataType: 'json',
	    success: function(data) { callback(data); }
	});
};

var createObject = function(location) {
    var elem = $('<div>');
    elem.addClass(objectClass);
    getObjectData(location, function(data) {
	    function handle(elem, data) {
		if(jQuery.isArray(data)) {
		    var selectElem = $('<select>');
		    for(var i = 0; i < data.length; i ++) {
			var option = $('<option>').append(data[i]);
			selectElem.append(option);
		    }
		    elem.append(selectElem);
		    var editElem = $('<div>');
		    elem.append(editElem);

		    selectElem.bind('change', function() {
			    console.log(selectElem.val());
			    editElem.empty();
			    editElem.append(createObject(selectElem.val()));
			});
		} else if(jQuery.type(data) == 'string') {
		    elem.append($('<input>').attr('type', 'text').attr('value', data));
		} else if(jQuery.type(data) == 'object') {
		    for(objectType in data) {
			var editElem = $('<div>').append(objectType);
			elem.append(editElem);
			elem.append(handle(editElem, data[objectType]));
			//console.log(data[objectType]);
		    }
		}
	    }
	    handle(elem, data);
	});
    return elem;
}

function initialize() {
    /*    $('.generate').click(function() {
	    $(this).parent().append(informationFromData());
	    });*/
    
    $('div#information').append(createObject('information/'));
}