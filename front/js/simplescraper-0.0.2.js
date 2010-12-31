/**
   SimpleScraper Front 0.0.1

   Copyright 2010, AUTHORS.txt
   Licensed under the MIT license.

   Requires jQuery, tested with 1.4.4

**/

(function (jQuery) {
    // Prevent console failures.
    if(!window.console) {
	window.console = {};
	window.console.log = function(string) { return false; }
	console = {};
	console.log = function(string) { return false; }
    }

    /** simplescraper global settings. **/
    settings = {
	backDirectory: '/back'
    };

    classes = {
	closer: 'closer',
	selector: 'selector',
	updater: 'updater',
	deleter: 'deleter',
	attributer: 'attributer',
	tagHolder: 'tagHolder',
	tag: 'tag',
	tagger: 'tagger',
	untagger: 'untagger',
	editor: 'editor',
	model: 'model',
	resource: 'resource',
	title: 'title',
	resourceControls: 'upperright'
    };

    /** Keep track of open resources. **/
    open = {};
    
    /** simplescraper editor. **/
    (function ( $ ) {
	var widgets = {
	    /* Select from possibilities. */
	    selector: function(name, url) {
		if(!name || !url)
		    $.error('Must specify name and url to generate a selector.');
		var $selector = $('<select>').addClass(classes.selector).addClass(name);
		$.ajax({
		    type: 'get',
		    url: url,
		    dataType: 'json',
		    success: function(data) {
			for(var i = 0; i < data.length; i++) {
			    $selector.append($('<option>').append(data[i]));
			}
			$selector.trigger('change');
		    }
		});
		return $selector;
	    }
	};
	var methods = {
	    init: function() {
		return this.each(function() {
		    var $editor = $(this).addClass(classes.editor);
		    //$editor.simplescraper_editor('refresh_models');
		    var $selectModel = widgets['selector'](classes.model,settings.backDirectory + '/');
		    $selectModel.bind('change', function() {
			$editor.simplescraper_editor('refresh');
		    });
		    $editor.append($('<p>Model:</p>').append($selectModel));
		    $editor.append($('<p>').append(
			$('<input>').attr('type', 'text'))
			.append($('<span>Add it</span>').click(function() {
			    $editor.simplescraper_editor('createResource')
			})));
		});
	    },
	    /* Update the list of resources. */
	    refresh: function() {
		return this.each(function() {
		    var $editor = $(this);
		    var $selectModel = $editor.find('.' + classes.model);
		    $editor.find('.' + classes.resource).remove();
		    $editor.append($('<p>').append(
			widgets['selector'](
			    classes.resource,
			    settings.backDirectory + '/' + $selectModel.val() + '/'))
				   .append('<span>View it</span>').addClass(classes.resource).click(function() {
				       var selections = $editor.simplescraper_editor('selected');
				       $editor.simplescraper_editor('viewResource', selections.model, selections.resource)
				   }));
		});
	    },
	    /* Determine what current selections are. */
	    selected: function() {
		var array = [];
		this.each(function() {
		    var $editor = $(this);
		    array.push({
			model: $editor.find('.' + classes.model).val(),
			resource: $editor.find('select.' + classes.resource).val(),
			input: $editor.find('input').val()
		    });
		});
		if(array.length == 1)
		    return array[0];
		return array;
	    },
	    createResource: function() {
		return this.each(function() {
		    var $editor = $(this);
		    var selections = $editor.simplescraper_editor('selected');
		    $.ajax({
			type: 'put',
			url: settings.backDirectory + '/' + selections.model + '/' + selections.input,
			success: function() {
			    $editor.simplescraper_editor('refresh', selections.input);
			    $editor.simplescraper_editor('viewResource', selections.model, selections.input);
			}
		    });
		});
	    },
	    viewResource: function(model, resource) {
		return this.each(function() {
		    var $editor = $(this);
		    //var selections = $editor.simplescraper_editor('selected');
		    $('body').append($('<div>').simplescraper_resource('init', model, resource));
		});
	    }
	};
	$.fn.simplescraper_editor = function(method) {
	    if ( methods[method] ) {
		return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
	    } else if ( typeof method === 'object' || ! method ) {
		return methods.init.apply( this, arguments );
	    } else {
		$.error( 'Method ' +  method + ' does not exist in simplescraper_editor.' );
	    }
	};
    }) (jQuery);

    /** simplescraper resource. **/
    (function ( $ ) {
	/* Widget generation. */
	var widgets = {
	    /* Triggers resource updates. */
	    updater: function() {
		return $('<span>').append('update').addClass(classes.updater);
	    },
	    /* Triggers resource deletion. */
	    deleter: function() {
		return $('<span>').append('delete').addClass(classes.deleter);
	    },
	    /* Generate an attribute input. */
	    attributer: function(name, value) {
		return $('<span>').append(name + ': ').addClass(classes.attributer).append($('<input>').attr({type: 'text', name: name, value: value}));
	    },
	    /* A tag holder. */
	    tagHolder: function(name, values) {
		var $tagHolder = $('<div>').append(name + ': ').addClass(classes.tagHolder);
		for(var i = 0; i < values.length; i++) {
		    $tagHolder.append(widgets['tag'](name, values[i]));
		}
		$tagHolder.append(widgets['tagger'](name));
		return $tagHolder;
	    },
	    /* A tagger. Allows the user to add tags. */
	    tagger: function(name) {
		return $('<input>').addClass(classes.tagger).data('name',name);
	    },
	    /* A tag. */
	    tag: function(name, value) {
		return $('<span>').append(value).addClass(classes.tag).data({name: name, value: value})
		    .append(widgets['untagger']);
	    },
	    /* Remove the tag this is attached to. */
	    untagger: function() {
		return $('<span>').append('X').addClass(classes.untagger);
	    },
	    /* Close this resource. */
	    closer: function() {
		return $('<span>').append('close').addClass(classes.closer).click(function() {
		    $(this).closest('.' + classes.resource).simplescraper_resource('close')
		});
	    }
	};
	/* Resource methods. */
	var methods = {
	    init: function(model, name) {
		return this.each(function() {
		    if(!name || !model)
			$.error('Must specify model and name to create a resource.');
		    var $resource = $(this).addClass(classes.resource).data({model: model, name: name});
		    // Don't allow the same resource to appear in multiple windows.
		    if(open[$(this).simplescraper_resource('identify')]) {
			$resource.remove();
			return;
		    }
		    open[$(this).simplescraper_resource('identify')] = true;
		    // Set up event handlers.
		    $resource.delegate('.' + classes.updater, 'click', function() {
			$resource.simplescraper_resource('put');
			return false;
		    });
		    $resource.delegate('.' + classes.deleter, 'click', function() {
			$resource.simplescraper_resource('delete');
			return false;
		    });
		    $resource.delegate('.' + classes.tagger, 'blur', function(){
			var $tagger = $(this);
			$resource.simplescraper_resource('tag', $tagger.data('name'), $tagger.val());
			return false;
		    });
		    $resource.delegate('.' + classes.untagger, 'click', function() {
			var $tag = $(this).closest('.' + classes.tag);
			$resource.simplescraper_resource('untag', $tag.data('name'), $tag.data('value'));
			return false;
		    });
		    $resource.simplescraper_resource('get');
		});
	    },
	    /* Identify a resource. */
	    identify: function() {
		var array = [];
		this.each(function() {
		    array.push([$(this).data('model'), $(this).data('name')].join('/'));
		});
		if(array.length == 1)
		    return array[0];
		else return array;
	    },
	    /* Obtain the location of a resource. */
	    location: function() {
		var locations = [];
		this.each(function() {
		    locations.push([settings.backDirectory, $(this).data('model'), $(this).data('name')].join('/'));
		});
		if(locations.length == 1)
		    return locations[0];
		return locations;
	    },
	    /* Obtain a resource's attributes from the page. */
	    attributes: function() {
		var attributesAry = [];
		this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(classes.resource)) // No attributes unless resource.
			return;
		    var attributes = {};
		    $.each($resource.find('.' + classes.attributer), function() {
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
	    /* Bring the page up-to-date with the server. */
	    get: function() {
		return this.each(function() {
		    var $resource = $(this).empty().append($('<span>').addClass(classes.title).append($(this).data('model')));
		    $resource.
			append($('<div>').addClass(classes.resourceControls)
			       .append(widgets['deleter']).append(widgets['updater']).append(widgets['closer']));
		    $.ajax({
			type: 'get',
			url: $resource.simplescraper_resource('location'),
			dataType: 'json',
			success: function(data)
			{
			    if('name' in data) { // If there's a name, place it first.
				$resource.append(widgets['attributer']('name', data['name']));
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
		});
	    },
	    /* Attempt to bring the server-side resource up-to-date with the page. */
	    put: function() {
		return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(classes.resource)) // Only resources can be updated.
			return;
		    var data = $resource.simplescraper_resource('attributes');
		    $.ajax({    type: 'put',
				url: $resource.simplescraper_resource('location'),
				data: data,
				success: function(response) // TODO: check status
				{
				    if(data['name']) // keep ID up to date
					$resource.data('name', data['name']);
				    //$resource.simplescraper_resource('get');
				    $('.' + classes.editor).simplescraper_editor('refresh');
				    $('.' + classes.resource).simplescraper_resource('get'); // Could modify taggings in other displayed items.
				}
			   });
		});
	    },
	    /* Close a resource. */
	    close: function() {
		return this.each(function() {
		    delete open[$(this).simplescraper_resource('identify')];
		    $(this).remove();
		    console.log(open);
		});
	    },
	    /* Delete a resource. */
	    delete: function() {
		return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(classes.resource)) // Only resources can be deleted.
			return;
		    $.ajax({
			type: 'delete',
			url: $resource.simplescraper_resource('location'),
			success: function(contents)
			{// TODO: check status
			    $('.' + classes.editor).simplescraper_editor('refresh');
			    $resource.simplescraper_resource('close');
			}
		    });
		})
	    },
	    /* Add a tag a resource. Must supply a type of tag and tag name. */
	    tag: function(tagType, tagName) {
		return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(classes.resource)) // Only resources can be tagged.
			return;
		    $.ajax({
			type: 'put',
			url: $resource.simplescraper_resource('location') + '/' + tagType + '/' + tagName, // Pluralizes.
			success: function(contents)
			{
			    $resource.simplescraper_resource('put'); // This will PUT possibly unsaved changes, which will also GET.
			},
			error: function(response, code) {
			    console.log(response);
			}
		    });
		});
	    },
	    /* Untag a tag from a resource.  Must supply a type of tag and tag name. */
	    untag: function(tagType, tagName) {
		return this.each(function() {
		    var $resource = $(this);
		    if(!$resource.hasClass(classes.resource)) // Only resources can be untagged.
			return;
		    $.ajax({
			type: 'delete',
			url: $resource.simplescraper_resource('location') + '/' + tagType + '/' + tagName, // Pluralizes.
			success: function(contents)
			{
			    $resource.simplescraper_resource('get');
			}
		    });
		});
	    }
	};

	$.fn.simplescraper_resource = function(method) {
	    if ( methods[method] ) {
		return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
	    } else if ( typeof method === 'object' || ! method ) {
		return methods.init.apply( this, arguments );
	    } else {
		$.error( 'Method ' +  method + ' does not exist in simplescraper_resource.' );
	    }
	};
    }) (jQuery);
}) (jQuery);