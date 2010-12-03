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
var listClass = 'list';

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

 */
var getObject = function(objectLocation, objectName) {
    var elem = $('<div>').append(objectName);
    elem.addClass(objectClass);
    //console.log(objectLocation + objectName);
    $.ajax({
	    'type': 'get',
	    url: objectLocation + objectName,
	    dataType: 'json',
	    success: function(contents) {
		if(jQuery.type(contents) == 'object') {
		    for(var entry in contents) {
			if(jQuery.type(contents[entry]) == 'array') {
			    var listElem = $('<div>').append(entry).addClass(listClass).appendTo(elem);
			    for(var i = 0; i < contents[entry].length; i++) {
				listElem.append(getObject(objectLocation + objectName + '/' + entry, contents[entry][i]));
			    }
			} else if(jQuery.type(contents[entry]) == 'string') {
			    elem.append($('<input>').attr({'type': 'text', 'value': contents[entry]}));
			    elem.append($('<span>').append('delete').addClass('delete').click(function() {
					$.ajax({    type: 'delete',
						    url: objectLocation + objectName,
						    success: function() {
						    // TODO: check status
						    elem.remove();
						}});
				    }));
			}
		    }
		} else if(jQuery.type(contents) == 'array') {
		    for(var i = 0; i < contents.length; i++) {
			elem.append(getObject(objectLocation + objectName, contents[i]));
		    }
		}
	    }});
    elem.append($('<span>').append('delete').addClass('delete').click(function() {
		$.ajax({    type: 'delete',
			    url: objectLocation + objectName,
			    success: function(response) {
			      console.log(response);
   			      elem.remove();
			}});
	    }));

    return elem;
};

function initialize() {
    $('div#type').append(getObject('/', 'type/'));
    $('div#gatherer').append(getObject('/', 'gatherer/'));
}