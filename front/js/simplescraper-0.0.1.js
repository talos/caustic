/**
   SimpleScraper Front 0.0.1

   Copyright 2010, AUTHORS.txt
   Licensed under the MIT license.

   Requires jQuery, tested with 1.4.4

**/

// Prevent console failures.
if(!window.console) {
    //alert('no console!');
    //console = {};
    window.console = {};
    window.console.log = function(string) { return false; }
    console = {};
    console.log = function(string) { return false; }
}
/*
$(document).ready(function() {
	initialize();
    });
*/

(function ( $ ) {
    var methods = {};
    var settings = {
	addClass: 'add button'
	deleteClass: 'delete button'
	updateClass: 'update button'
	tagClass: 'tag button'
	untagClass: 'untag button',
	collectionClass: 'collection',
	resourceClass: 'resource',
	backDirectory: '/back'
    };

    /*
      When clicked, makes a delete request for the nearest resource.
    */
    /*var deleteButton = function() {
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
	    };*/
    methods['location'] = function() {
	return [settings.backDirectory, $resource.data('type'), $resource.data('id')].join('/');
    };
    methods['delete'] = function() {
	this.each(function() {
		var $resource = $(this);
		$.ajax({    type: 'delete',
			    url: $resource.simplescraper('location');
			    success: function(contents)
			    {// TODO: check status
				$resource.remove();
			    }
		    });
	    })
    };

    /*
      When clicked, makes a put request to the collection it falls within.
    */
    /*var addButton = function() {
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
	    };*/
    methods['create'] = function(type) {
	this.each(function() {
		var $resource = $(this);
		$.ajax({    type: 'post',
			    url: [settings.backDirectory + type].join('/'),
			    dataType: 'json',
			    success: function(contents) {// TODO: check status
			    $elem.trigger('refresh');
			}
		    });
	    });	
    };

    /*
      When clicked, makes a post request for the resource it falls within.
    */
    /* var updateButton = function() {
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
    };*/
    methods['update'] = function() {
	return this.each(function() {
		var $resource = $(this);
		var url = $resource.data('url');
		var resourceData = {};
		$.each($resource.find('input'), function() {
			var $input = $(this);
			if(!$input.attr('name')) // Ignore inputs without names.  They are not for attributes.
			    return;
			resourceData[$input.attr('name')] = $input.val();
		    });
		$.ajax({    type: 'put',
			    url: url,
			    data: resourceData,
			    success: function(contents) // TODO: check status
			    {
				$resource.trigger('refresh');
			    }
		    });
	    });
    }

    /*
      When clicked, makes a put request for a specific tag ID.
    */
    /*var tagButton = function() {
	return $('<span>').append('tag').addClass(tagClass).click(function() {
		var $elem = $(this).prev('input');
		$.ajax({ type: 'put',
			    url: $elem.attr('id') + '/' + $elem.val(),
			    success: function(contents) {
			    $elem.closest('.' + resourceClass).trigger('refresh');
			}});
		return false;
	    });
    };*/
    methods['tag'] = function(tagName) {
	return this.each(function() {
		var $resource = $(this);
		var url = $resource.data('url')  + '/' + tagName;
		$.ajax({    type: 'put',
			    url: url,
			    success: function(contents)
			    {
				$resource.trigger('refresh');
			    }
		    });
	    });
    };

    /*
      When clicked, makes a delete request for the tag selected in an option element.
    */
    /*var untagButton = function() {
	return $('<span>').append('untag').addClass(untagClass).click(function() {
		var $elem = $(this).prev('select');
	    
		$.ajax({ type: 'delete',
			    url: $elem.attr('id') + '/' + $elem.val(),
			    success: function(contents) {
			    $elem.closest('.' + resourceClass).trigger('refresh');
			}});
		return false;
	    });
	    };*/
    methods['untag'] = function(tagName) {
	return this.each(function() {
		var $resource = $(this);
		var url = $resource.data('url') + '/' + tagName;
		$.ajax({    type: 'delete',
			    url: url,
			    success: function(contents)
			    {
				$resource.trigger('refresh');
			    }
		    });
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
				    for(var i = 0; i < attribute.length; i++) {
					$tagSelector.append($('<option>').append(String(attribute[i])));
				    }
				    var $tagInput = $('<input>').attr({id: resourceLocation + resourceName + '/' + key, type: 'text'});
				    $elem.append($tagContainer.append($tagSelector).append(untagButton()).append($tagInput).append(tagButton()));
				    //var listElem = $('<div>').addClass(attribute).attr('id', $elem.attr('id') + '/' + key).append(key).append(addButton()).appendTo($elem);
				} else { // An individual, post-able value.
				    $elem.append($('<p><span>').append(key + ': ').append($('<input>').attr({'type': 'text', 'name': key, 'value': attribute})));
				}
			    }
			}
		    });
		return false;
	    });
	$elem.trigger('refresh');
	return $elem;
    };

    $.fn.simplescraper = function(method) {
	if ( methods[method] ) {
	    return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
	} else if ( typeof method === 'object' || ! method ) {
	    return methods.init.apply( this, arguments );
	} else {
	    $.error( 'Method ' +  method + ' does not exist in simplescraper.' );
	}
    };
});
// function initialize() {
// 	$('div#root').append(getCollection('/back', '/'));
// }