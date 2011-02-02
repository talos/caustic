/**
   SimpleScraper Front 0.0.2

   Copyright 2010, AUTHORS.txt
   Licensed under the MIT license.

   Requires jQuery, tested with 1.4.4

**/

( function ( $ ) {
    // Prevent console failures.
    if( ! window.console ) {
	window.console = {};
	window.console.log = function(string) { return false; }
	console = {};
	console.log = function(string) { return false; }
    }
    
    // Give ajax calls a standard error functionality.
    var _ajax = function( options ) {
	$.extend(options, {
	    error: function(response, code) {
		$.error('AJAX error ' + response.status + ': ' + response.responseText);
	    }
	});
	$.ajax(options);
    },
    /* Widget generation to create components of simplescraper. */
    _factory = function ( options ) {
	return {
	    init : function ( ) {
		var factory = this,
		factory.target = options.target;

		/**** Widget definitions. *****/

		factory.widgets = {

		    /***
********* Tag widget.
* options.name : the name of the tag.
* options.location : the location to load the tag from.
*/
		    'tag' : {
			init : function ( options ) {
			    return $('<span>').text(options.name).data('simplescraper', {
				name : options.name,
				location : options.location
			    });
			},
			bindings : {
			    /** Load up the resource this tag refers to. **/ 
			    'click' : function ( event ) {
				var data = $(this).data('simplescraper');

//////////////// TODO ////////////////
				factory.target.append(factory.make('resource', { location : data.location }));
				return false;
			    },
			    /** Delete this tag. **/
			    'delete.simplescraper' : function ( event ) {
				var data = $(this).data('simplescraper');
				$_ajax({
				    type : 'delete',
				    url  : data.location,
				    success : function ( ) {
					$(this).remove();
				    }
				});
				return false;
			    }
			}
		    },

		    /***
********* Tagger widget. 
* options.target : where on the page to append new tags.
*/
		    'tagger' : {
			init : function ( options ) {
			    return $('<input>').data('simplescraper', {
				target : options.target,
				prevVal : ''
			    });
			},
			bindings : {
			    /** Create a new tag. **/
			    'blur' : function ( event ) {
				var data = $(this).data('simplescraper'),
				$target = data.target;
				// Only submit if value changed.
				if(data.prevVal === $(this).val())
				    return false;
				_ajax({
				    type : 'put',
				    url  : $target.closest('.resource').data('simplescraper').location + '/',
				    success : function( response ) {
					/* Once we have retrieved an ID, we can create the tag widget. */
					$target.append(factory.make('tag', {
					    name : $(this).val(),
					    location : response
					}));
					data.prevVal = $(this).val();
					$(this).empty();
				    }
				});
				return false;				    
			    } 
			}
		    },

		    /**
******* Attribute widget.
*/
		    'attribute' : {
			init : function( options ) {
			    return $('<textarea />').attr({name: options.name, value: options.value}).before($('<label />').text(options.name + ': '));
			},
			bindings : {
			    'blur' : function( event ) { 
				$(this).trigger('put.simplescraper');
			    }
			}
		    },

		    /**
******* Resource widget.
* options.location : where to load this resource from.
*/
		    'resource' : {
			/* options.id, options.model */
			init : function( options ) {
			    $content = $('<div />');
			    return $('<div />')
				.data({
				    simplescraper : { 
					location : options.location
					model : options.location.split('/')[0],
					id : options.location.split('/')[1],
					content : $content,
					attributes : { }
				    }
				})
				.draggable()
			    /* Title */
				.append($('<div />').text(options.model + ' ' + options.id).addClass('title'))
			    /* Controls */
				.append($('<div />').addClass('upperright')
					.append(factory.make('close'))
					.append(factory.make('delete')))
			    /* Content */
				.append($content);
			},
			bindings : {
			    
			    /* Bring the resource up-to-date with the server. */
			    'get.simplescraper' : function( event ) { 
				var data = $(this).data('simplescraper'),
				$content = data.content.empty();
				functions.ajax({
				    type: 'get',
				    url: data.location,
				    dataType: 'json',
				    success: function(response)
				    {
					if('name' in response) { // If there's a name, place it first.
					    $content.append(factory.make('attribute', {'name': response['name']}));
					    delete response['name'];
					}
					for(var key in response) {
					    // A collection of tags was retrieved.
					    if($.isPlainObject(response[key])) {
						var $tagHolder = $('<div />').text(key + ': '),
						tags = data[key];
						for( var name in tags) {
						    $tagHolder.append(factory.make('tag', { name: name, data: tags[name] }));
						}
						$tagHolder.append(factory.make('tagger', { resource : $(this) }));
						// An individual, put-able value.
					    } else {
						var $attribute = factory.make('attribute', { name: key, value: data[key] } )
						    .appendTo($content);
						data.attributes[key] = $attribute;
					    }
					}
				    }});
				return false;
			    },
			    
			    /**  Replace the resource (thus updating its attributes). **/
			    'put.simplescraper' : function( event ) { 
				var data = $(this).data('simplescraper'),
				// Obtain values from attributes.
				values = {};
				for ( var name in data.attributes ) {
				    values[name] = data.attributes.val();
				}
				_ajax({
				    type : 'put',
				    url : data.location,
				    data : values,
				    success : function( ) { // TODO: check status
					$(this).trigger('get.simplescraper'); // See whether our changes took.
				    }
				});
				return false;
			    },
			    
			    /** Delete the resource from the server. Close resource window if successful. **/
			    'delete.simplescraper' : function( event ) { 
				var data = $(this).data('simplescraper'),
				_ajax({
				    type : 'delete',
				    url : data.location,
				    success : function( ) {
					$(this).trigger('close.simplescraper');
				    }
				});
				return false;
			    },	 

			    /** Close the resource window. **/
			    'close.simplescraper' : function( event ) {
				$(this).remove();
				return false;
			    }
			},

			/**
***** Close widget.
*/
			'close' : {
			    init : function ( ) { return $('<span />').text('X'); },
			    bindings : {
				'click' : function( event ) { $(this).trigger('close.simplescraper'); }
			    }
			},

			/**
***** Delete widget.
*/
			'delete' : {
			    init : function( ) { return $('<span />').text('delete'); },
			    bindings : {
				'click' : function( event ) { $(this).trigger('delete.simplescraper'); }
			    }
			},
		    }
		};
	    };
	    return factory;
	},
	make : function( type, options ) {
	    if( ! type in widgets )
		$.error(widget + ' is not a valid widget.');
	    var $widget = widgets.type.init(options).addClass(type);
	    if( bindings in widgets.type ) {
		for( var event in widget.bindings ) {
		    $widget.bind(widget.bindings[event]);
		}
	    }
	    return $widget;
	}
    };

    var methods = {
	init: function( options ){
	    return this.each(function( ) {
		var $this = $(this),
		data = $this.data('simplescraper');
		
		if( ! data ) {
		    $(this).data('simplescraper', {
			resources : {},
			target : $this
		    });
		    
		}
	    }).trigger(events.get);
	},
	destroy : function( ) {
	    return this.each(function(){
		var $this = $(this),
		data = $this.data('simplescraper');
		
		data.simplescraper.remove();
		$this.removeData('simplescraper');
	    })
	}

	/* Add a tag a resource. Must supply a type of tag and tag id. */
	},
	/* Untag a tag from a resource.  Must supply a type of tag and tag id. */
	untag: function(tagType, tagId) {
	    return this.each(function() {
		var $resource = $(this);
		if(!$resource.hasClass(classes.resource)) // Only resources can be untagged.
		    return;
		var url = $resource.simplescraper('location');
		if(!url)
		    return;
		url = url + '/' + tagType + '/' + tagId, // Pluralizes.

		functions.ajax({
		    type: 'delete',
		    url: url,
		    success: function(contents)
		    {
			$resource.simplescraper('get');
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
}) ( jQuery );
