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
var updateClass = 'update';
var tagClass = 'tag';
var untagClass = 'untag';

var collectionClass = 'collection';
var resourceClass = 'resource';

/*
When clicked, makes a delete request for the nearest resource (which could be a tag.)
 */
var deleteButton = function() {
    return $('<span>').append('delete').addClass(deleteClass).click(function() {
	    var $resource = $(this).closest('.' + resourceClass);
	    $.ajax({    type: 'delete',
			url: $resource.attr('id'),
			dataType: 'json',
			success: function(contents) {// TODO: check status
			$resource.remove();
		    }
		});
	    return false;
	});
};

/*
When clicked, makes a put request to the collection it falls within.
 */
var addButton = function() {
    return $('<span>').append('add').addClass(addClass).click(function() {
	    var $elem = $(this).closest('.' + collectionClass);
	    $.ajax({    type: 'post',
			url: $elem.attr('id'),
			dataType: 'json',
			success: function(contents) {// TODO: check status
			$elem.trigger('refresh');
		    }
		});
	    console.log($elem);
	    return false;
	});
};

/*
When clicked, makes a post request for the resource it falls within.
*/
var updateButton = function() {
    return $('<span>').append('update').addClass(updateClass).click(function() {
	    var $elem = $(this).closest('.' + resourceClass);
	    var resourceData = {};
	    $.each($elem.find('input'), function() {
		    var $input = $(this);
		    if(!$input.attr('name')) // Ignore inputs without names.  They are not for attributes.
			return;
		    resourceData[$input.attr('name')] = $input.val();
		});
	    console.log(resourceData);
	    $.ajax({    type: 'put',
			url: $elem.attr('id'),
			data: resourceData,
			success: function(contents) {// TODO: check status
			$elem.trigger('refresh');
		    }
		});
	    return false;
	});
};

/*
When clicked, makes a put request for a specific tag ID.
 */
var tagButton = function() {
    return $('<span>').append('tag').addClass(tagClass).click(function() {
	    var $elem = $(this).prev('input');
	    $.ajax({ type: 'put',
			url: $elem.attr('id') + '/' + $elem.val(),
			success: function(contents) {
			$elem.closest('.' + resourceClass).trigger('refresh');
		    }});
	    return false;
	});
};

/*
When clicked, makes a delete request for the tag selected in an option element.
*/
var untagButton = function() {
    return $('<span>').append('untag').addClass(untagClass).click(function() {
	    var $elem = $(this).prev('select');
	    
	    $.ajax({ type: 'delete',
			url: $elem.attr('id') + '/' + $elem.val(),
			success: function(contents) {
			$elem.closest('.' + resourceClass).trigger('refresh');
		    }});
	    return false;
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
var getCollection = function(collectionLocation, collectionName) {
    var $elem = $('<div>').attr('id', collectionLocation + collectionName);
    $elem.addClass(collectionClass);
    $elem.bind('refresh', function(event) {
	    var $elem = $(this);
	    $elem.empty().append(collectionName);
	    $.ajax({type: 'get',
		    url: collectionLocation + collectionName,
		    dataType: 'json',
		    success: function(collectionData) {
			$elem.append(addButton());
			for(var i = 0; i < collectionData.length; i++) {
			    var memberLocation = String(collectionData[i]);
			    if(memberLocation.charAt(memberLocation.length - 1) == '/') {
				$elem.append(getCollection(collectionLocation + collectionName, memberLocation));
			    } else {
				$elem.append(getResource(collectionLocation + collectionName, memberLocation));
			    }
			}
		    }
		});
	    return false; // Prevent bubbling.
	});
    $elem.trigger('refresh');
    return $elem;
};

/*

 */
var getResource = function(resourceLocation, resourceName) {
    var $elem = $('<div>').attr('id', resourceLocation + resourceName);
    $elem.addClass(resourceClass);
    $elem.bind('refresh', function(event) {
	    var $elem = $(this);
	    $elem.empty().append(resourceName).append(updateButton).append(deleteButton);
	    $.ajax({type: 'get',
		    url: $elem.attr('id'),
			dataType: 'json',
			success: function(resourceData) {
			for(var key in resourceData) {
			    var attribute = resourceData[key];
			    if(jQuery.isArray(attribute)) { // A collection of tags.
				var $tagContainer = $('<div>').append(key);
				var $tagSelector = $('<select>').attr('id',resourceLocation + resourceName + '/' + key);
				for(tagId in attribute) {
				    $tagSelector.append($('<option>').append(String(attribute)))
				}
				var $tagInput = $('<input>').attr({id: resourceLocation + resourceName + '/' + key, type: 'text'});
				$elem.append($tagContainer.append($tagSelector).append(untagButton()).append($tagInput).append(tagButton()));
				//var listElem = $('<div>').addClass(attribute).attr('id', $elem.attr('id') + '/' + key).append(key).append(addButton()).appendTo($elem);
			    } else { // An individual, post-able value.
				$elem.append($('<br><span>').append(key + ': ').append($('<input>').attr({'type': 'text', 'name': key, 'value': attribute})));
			    }
			}
		    }
		});
	    return false;
	});
    $elem.trigger('refresh');
    return $elem;
}

function initialize() {
    $('div#root').append(getCollection('/back', '/'));
}