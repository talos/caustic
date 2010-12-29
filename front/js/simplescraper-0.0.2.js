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

(function ( $ ) {
    var settings = {
	generatorClass: 'generator',
	updaterClass: 'updater',
	deleterClass: 'deleter',
	attributerClass: 'attributer',
	tagHolder: 'tagHolder',
	tagClass: 'tag',
	taggerClass: 'tagger',
	untaggerClass: 'untagger',
	collectionClass: 'collection',
	resourceClass: 'resource',
	backDirectory: '/back'
    };
    /* Widget generation. */
    var widgets = {
	/* Creates new resources. */
	generator: function() {
	    return $('<span>').append('new').addClass(settings.generatorClass);
	},
	/* A collection. */
	collection: function(type) {
	    return $('<div>').simplescraper('collection', type);
	},
	/* A resource. */
	resource: function(type, id) {
	    return $('<div>').simplescraper('resource', type, id);
	},
	/* Triggers resource updates. */
	updater: function() {
	    return $('<span>').append('update').addClass(settings.updaterClass);
	},
	/* Triggers resource deletion. */
	deleter: function() {
	    return $('<span>').append('delete').addClass(settings.deleterClass);
	},
	/* Generate an attribute input. */
	attributer: function(name, value) {
	    return $('<span>').append(name + ': ').addClass(settings.attributerClass).append($('<input>').attr({type: 'text', name: name, value: value}));
	},
	/* A tag holder. */
	tagHolder: function(name, values) {
	    var $tagHolder = $('<div>').append(name + ': ').addClass(settings.tagHolder);
	    for(var i = 0; i < values.length; i++) {
		$tagHolder.append(widgets['tag'](name, values[i]));
	    }
	    $tagHolder.append(widgets['tagger'](name));
	    return $tagHolder;
	},
	/* A tagger. Allows the user to add tags. */
	tagger: function(name) {
	    return $('<input>').addClass(settings.taggerClass).data('name',name);
	},
	/* A tag. */
	tag: function(name, value) {
	    return $('<span>').append(value).addClass(settings.tagClass).data({name: name, value: value})
	    .append(widgets['untagger']);
	},
	/* Remove the tag this is attached to. */
	untagger: function() {
	    return $('<span>').append('X').addClass(settings.untaggerClass);
	}
    };
    /* Methods for resources. */
    var methods = {
	/* Create all collections. */
	init: function() {
	    return this.each(function() {
		    var $simpleScraperElem = $(this);
		    $.ajax({   type: 'get',
			       url: settings.backDirectory + '/',
			       dataType: 'json',
			       success: function(collections) { // Returns all the collections.
				for(var i = 0; i < collections.length; i++) {
				    $simpleScraperElem.append(widgets['collection'](collections[i]));
				}
			    }
			});
		});
	},
	/* Create a new collection. Must specify type. */
	collection: function(type) {
	    return this.each(function() {
		    var $collection = $(this).addClass(settings.collectionClass).data('type', type);
		    // Set up event handlers.
		    $collection.bind('refresh.simplescraper', function() {
			    $collection.simplescraper('refresh');
			});
		    $collection.delegate('.' + settings.generatorClass, 'click', function() {
			    $collection.append(widgets['resource'](type));
			});

		    $collection.append(widgets['generator']);
		    $collection.trigger('refresh.simplescraper');
		});
	},
	/* Create a new resource. Must specify type. Will POST for a new resource ID if no ID specified. */
	resource: function(type, id) {
	    return this.each(function() {
		    var $resource = $(this).addClass(settings.resourceClass).data('type', type);
		    // Set up event handlers.
		    $resource.bind('refresh.simplescraper', function() {
			    $resource.simplescraper('refresh');
			});
		    $resource.delegate('.' + settings.updaterClass, 'click', function() {
			    $resource.simplescraper('update');
			});
		    $resource.delegate('.' + settings.deleterClass, 'click', function() {
			    $resource.simplescraper('delete');
			});
		    $resource.delegate('.' + settings.taggerClass, 'blur', function(){
			    var $tagger = $(this);
			    $resource.simplescraper('tag', $tagger.data('name'), $tagger.val());
			});
		    $resource.delegate('.' + settings.untaggerClass, 'click', function() {
			    var $tag = $(this).closest('.' + settings.tagClass);
			    $resource.simplescraper('untag', $tag.data('name'), $tag.data('value'));
			});
		    if(id) {
			$resource.data('id', id);
			$resource.trigger('refresh.simplescraper');
		    } else {
			$.ajax({    type: 'post',
				    url: [settings.backDirectory, type].join('/') + '/',
				    dataType: 'json',
				    success: function(id)
				    {// TODO: check status
					$resource.data('id', id);
					$resource.trigger('refresh.simplescraper');
				    }
			    });
		    }
		});
	},
	/* Obtain the location of a resource or collection. */
	location: function() {
	    var locations = [];
	    this.each(function() {
		    if($(this).hasClass(settings.resourceClass)) { // Location for resource.
			locations.push([settings.backDirectory, $(this).data('type'), $(this).data('id')].join('/'));
		    } else if($(this).hasClass(settings.collectionClass)) { // Location for collection..
			locations.push([settings.backDirectory, $(this).data('type')].join('/') + '/');
		    } else {
			locations.push(null);
		    }
		});
	    if(locations.length == 1)
		return locations[0];
	    return locations;
	},
	/* Obtain the a resource's attributes. */
	attributes: function() {
	    var attributesAry = [];
	    this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(settings.resourceClass)) // No attributes unless resource.
			return;
		    var attributes = {};
		    $.each($resource.find('.' + settings.attributerClass), function() {
			    var $attributer = $(this);
			    var $input =$attributer.find('input');
			    attributes[$input.attr('name')] = $input.val();
			});
		    attributesAry.push(attributes);
		});
	    if(attributesAry.length == 1)
		return attributesAry[0];
	    return attributesAry;
	},
	/* Bring the front-side view up-to-date with the server. */
	refresh: function() {
	    return this.each(function() {
		    if($(this).hasClass(settings.resourceClass)) { // Refresh a Resource.
			var $resource = $(this).empty();
			var $name = $('<div>').append([$resource.data('type'), $resource.data('id')].join(' ')).appendTo($resource);
			$resource.append(widgets['deleter']).append(widgets['updater']);
			$.ajax({    type: 'get',
				    url: $resource.simplescraper('location'),
				    dataType: 'json',
				    success: function(data)
				    {
					if('name' in data) { // If there's a name, place it first.
					    $name.append(widgets['attributer']('name', data['name']));
					    delete data['name'];
					}
					for(var key in data) {
					    var attribute = data[key];
					    if(jQuery.isArray(data[key])) { // A collection of tags was retrieved.
						$resource.append($('<p>').append(widgets['tagHolder'](key, data[key])));
					    } else { // An individual, post-able value.
						$resource.append($('<p>').append(widgets['attributer'](key, data[key])));
					    }
					}
				    }
			    });
		    } else if($(this).hasClass(settings.collectionClass)) { // Refresh a collection.
			var $collection = $(this).empty();
			var $name = $('<div>').append($collection.data('type')).appendTo($collection);
			$collection.append(widgets['generator']);
			$.ajax({    type: 'get',
				    url: $collection.simplescraper('location'),
				    dataType: 'json',
				    success: function(data)
				    {  // Should return array of IDs.
					for(var i = 0; i < data.length; i++) {
					    $collection.append(widgets['resource']($collection.data('type'), data[i]));
					}
				    }
			    });
		    }
		});
	},

	/* Attempt to bring the server-side resource up-to-date. */
	update: function() {
	    return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(settings.resourceClass)) // Only resources can be updated.
			return;
		    $.ajax({    type: 'put',
				url: $resource.simplescraper('location'),
				data: $resource.simplescraper('attributes'),
				success: function(response) // TODO: check status
				{
				    $resource.trigger('refresh.simplescraper');
				}
			});
		});
	},
	/* Delete a resource. */
	delete: function() {
	    return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(settings.resourceClass)) // Only resources can be deleted.
			return;
		    $.ajax({    type: 'delete',
				url: $resource.simplescraper('location'),
				success: function(contents)
				{// TODO: check status
				    $resource.remove();
				}
			});
		})
	},
	/* Add a tag a resource. Must supply a type of tag and tag name. */
	tag: function(tagType, tagName) {
	    return this.each(function() {
		var $resource = $(this);
		if(!$resource.hasClass(settings.resourceClass)) // Only resources can be tagged.
		    return;
		$.ajax({    type: 'put',
			    url: $resource.simplescraper('location') + '/' + tagType + '/' + tagName, // Pluralizes.
			    success: function(contents)
			    {
				$resource.trigger('refresh.simplescraper');
			    }
		    });
		});
	},
	/* Untag a tag from a resource.  Must supply a type of tag and tag name. */
	untag: function(tagType, tagName) {
	    return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(settings.resourceClass)) // Only resources can be untagged.
			return;
		    $.ajax({    type: 'delete',
				url: $resource.simplescraper('location') + '/' + tagType + '/' + tagName, // Pluralizes.
				success: function(contents)
				{
				    $resource.trigger('refresh.simplescraper');
				}
			});
		});
	}
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
}) (jQuery);
