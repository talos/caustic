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

 */
var deleteButton = function() {
    return $('<span>').append('delete').addClass('delete').click(function() {
			var object = $(this).closest('.' + objectClass);
			$.ajax({    type: 'delete',
				    url: object.attr('id'),
				    dataType: 'json',
				    success: function(contents) {
				    // TODO: check status
				    if(contents == true) {
					object.remove();
				    } else {
					console.log(contents);
				    }
				}});
			return false;
	});
};

/*

 */
var addButton = function() {
    return $('<span>').append('add').addClass('add').click(function() {
				    var nameToAdd = $(this).children('input').val();
				    var elem = $(this).closest('.' + objectClass);
				    $.ajax({    type: 'put',
						url: elem.attr('id') + nameToAdd,
						dataType: 'json',
						success: function(contents) {
						// TODO: check status
						if(contents == true) {
						    elem.trigger('refresh');
						} else {
						    console.log(contents);
						}
					    }});
				    return false;
	}).append($('<input>').attr('type', 'text'));
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
    var elem = $('<div>').attr('id', objectLocation + objectName);
    elem.addClass(objectClass);
    var isCollection = objectName.charAt(objectName.length - 1) == '/' ? true : false;
    elem.data({ name: objectName,
		isCollection: isCollection });

    // GET a collection, you get an array.  each element of the array is an existing resource.  new resources can be added with arbitrary names.
    //                           => each element must be gotten.
    // GET a resource, you get a hash.
    //                           => IF element is an array, it is a collection within the resource.
    //                           => IF element is a string, allow it to be modified.
    elem.bind('refresh', function(event) {
	    var elem = $(this);
	    var objectName = elem.data('name');
	    var isCollection = elem.data('isCollection');
	    elem.empty();
	    elem.append(objectName);
	    $.ajax({
		    'type': 'get',
		    url: elem.attr('id'),
		    dataType: 'json',
		    success: function(contents) {
			if(isCollection) {
			    if(jQuery.isArray(contents)) {
				elem.append(addButton());
				for(var i = 0; i < contents.length; i ++) {
				    elem.append(getObject(elem.attr('id'), contents[i]));
				}
			    }
			} else if(!isCollection) {
			    elem.append(deleteButton());
			    if(jQuery.isPlainObject(contents)) {
				for(var key in contents) {
				    if(jQuery.isArray(contents[key])) {
					var listElem = $('<div>').addClass(objectClass).attr('id', elem.attr('id') + '/' + key).append(key).append(addButton()).appendTo(elem);
					for(var i = 0; i < contents[key].length; i++) {
					    listElem.append(getObject(listElem.attr('id'), contents[key][i]));
					}
				    } else if(jQuery.type(contents[key]) == 'string') {
					elem.append($('<span>').append(key + ': ').append($('<input>').attr({'type': 'text', 'value': contents[key]})));
				    }
				}
			    }
			}
		    }
		});
	    return false; // Prevent bubbling.
	});

    elem.trigger('refresh');

    return elem;
};



function initialize() {
    $('div#root').append(getObject('', '/'));
}