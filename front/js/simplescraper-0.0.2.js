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
	window.console.log = function(string) { return false; };
	console = {};
	console.log = function(string) { return false; };
    }
    
    // Give ajax calls a standard error functionality & use of JSON.
    var _ajax = function( options ) {
	return $.ajax($.extend({
	    dataType : 'json',
	    error: function(response, errorType) {
		$.error('Ajax Error ' + errorType + ' from response ' + response.status + ': ' + response.responseText);
	    }
	}, options));
    },
    /* Widget generation to create components of simplescraper. */
    _factory = function ( options ) {
	return {
	    init : function ( ) {
		var factory = this;
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
// Needs to follow redirect. 
				$resource = factory.make('resource', { location : data.location });
				$resource.trigger('get.simplescraper');
				factory.target.prepend($resource);
				return false;
			    },
			    /** Delete this tag. **/
			    'delete.simplescraper' : function ( event ) {
				var $resource = $(this),
				data = $resource.data('simplescraper');
				$_ajax({
				    type : 'delete',
				    url  : data.location,
				    success : function ( ) {
					$resource.remove();
					$('.resource').trigger('get.simplescraper'); // Could affect links in other resources.
				    }
				});
				return false;
			    }
			}
		    },

		    /***
********* Tagger widget. 
* options.target : where on the page to append new tags.
* options.location : where on the server to put these tags.
*/
		    'tagger' : {
			init : function ( options ) {
			    return $('<input>').data('simplescraper', {
				target : options.target,
				location : options.location
			    });
			},
			bindings : {
			    /** Create a new tag. **/
			    'blur' : function ( event ) {
				var $tagger = $(this),
				data = $tagger.data('simplescraper'),
				name = $tagger.val(),
				$target = data.target;
				// Only submit if there's a value for name.
				if($tagger.val() !== '') {
				    _ajax({
					type : 'put',
					url  : data.location,
					data : { name : name },
					success : function( response ) {
					    // Could affect other resources, so refresh all.
					    $('.resource').trigger('get.simplescraper');
					}
				    });
				}
				return false;				    
			    } 
			}
		    },

		    /**
******* Attribute widget.
* options.name : name of the attribute.
* options.value : default value for the attribute.
*/
		    'attribute' : {
			init : function( options ) {
			    return $('<textarea />').attr({name: options.name, value: options.value});
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
			init : function( options ) {
			    $content = $('<div />'),
			    model =  options.location.split('/')[1],
			    id = options.location.split('/')[2];
			    $resource = $('<div />')
				.data({
				    simplescraper : { 
					location : options.location,
					model : model,
					id : id,
					content : $content,
					attributes : { }
				    }
				})
				.draggable()
			    /* Title */
				.append($('<div />').text(model + ' ' + id).addClass('title'))
			    /* Controls */
				.append($('<div />').addClass('upperright')
					.append(factory.make('close'))
					.append(factory.make('delete')))
			    /* Content */
				.append($content);
			    return $resource;
			},
			bindings : {
			    
			    /* Bring the resource up-to-date with the server. */
			    'get.simplescraper' : function( event ) { 
				var $resource = $(this),
				data = $resource.data('simplescraper'),
				$content = data.content.empty();
				_ajax({
				    type: 'get',
				    url: data.location,
				    /* Found the resource! */
				    success: function( response ) {
					var key;
					for( key in response) {
					    // A collection of tags was retrieved.
					    console.log(response);
					    if($.isPlainObject(response[key])) {
						var $tagHolder = $('<div />').text(key + ': ').appendTo($content),
						tags = response[key];
						var name;
						for( name in tags ) {
						    if( tags.hasOwnProperty(name)) {
							console.log(tags[name]);
							$tagHolder.append(factory.make('tag', { name: tags[name], location: name }));
						    }
						}
						$tagHolder.append(factory.make('tagger', { target : $tagHolder, location : data.location + '/' + key }));
						// An individual, put-able value.
					    } else {
						var $attribute = factory.make('attribute', { name : key, value : response[key] });
						$attrHolder = $('<div />')
						    .append($('<label />').text(key + ': '))
						    .append($attribute)
						    .appendTo($content);
						data.attributes[key] = $attribute;
					    }
					}
				    },
				    /* Could not load the resource. */
				    error: function( response ) {
					$resource.trigger('close.simplescraper');
				    }
				});
				return false;
			    },
			    
			    /**  Replace the resource (thus updating its attributes). **/
			    'put.simplescraper' : function( event ) { 
				var $resource = $(this),
				data = $resource.data('simplescraper'),
				// Obtain values from attributes.
				values = {};
				name;
				for ( name in data.attributes ) {
				    if( data.attributes.hasOwnProperty( name ) ) {
					values[name] = data.attributes[name].val();
				    }
				}
				_ajax({
				    type : 'put',
				    url : data.location,
				    data : values,
				    success : function( ) { // TODO: check status
					$resource.trigger('get.simplescraper');
					//$('.resource').trigger('get.simplescraper');
				    }
				});
				return false;
			    },
			    
			    /** Delete the resource from the server. Close resource window if successful. **/
			    'delete.simplescraper' : function( event ) { 
				var $resource = $(this),
				data = $resource.data('simplescraper');
				_ajax({
				    type : 'delete',
				    url : data.location,
				    success : function( ) {
					$resource.trigger('close.simplescraper');
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
		    }
		};
		return factory;
	    },
	    make : function( type, options ) {
		if( ! type in this.widgets ) {
		    $.error(type + ' is not a valid widget.');
		}
		var widget = this.widgets[type],
		$widget = widget.init(options).addClass(type),
		event;
		if( 'bindings' in widget ) {
		    for( event in widget.bindings ) {
			if( widget.bindings.hasOwnProperty( event )) {
			    $widget.bind(event, widget.bindings[event]);
			}
		    }
		}
		return $widget;
	    }
	};
    };
    
    var methods = {
	/* Initialized with a user resource. */
	init: function( options ) {
	    var factory = _factory({target: this}).init(),
	    $target = this;
	    _ajax({
		type : 'post',
		url  : '/signup',
		data : { name : 'test' },
		success : function( response ) {
		    var $initialUser = factory.make('resource', { location: response });
		    $initialUser.trigger('get.simplescraper');
		    $target.append($initialUser);
		}
	    });
	    return this;
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
})( jQuery );

