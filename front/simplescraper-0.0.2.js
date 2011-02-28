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
* options.tag_directory : the tag's relative directory (i.e. "gatherers/" )
* options.model : the model of the resource that the tag refers to (i.e. "gatherer" )
* options.name : the name of the tag. (optional) 
* options.id : the ID of the tag and resource (i.e. "1") (optional)
*/
		    'tag' : {
			init : function ( options ) {
			    return $('<span />').text(options.name ? options.name : '').data('simplescraper', {
				name : options.name,
				model : options.model,
				tag_directory : options.tag_directory,
				id : options.id
			    }).append(factory.make('delete'));
			},
			bindings : {
			    /** Load up the resource this tag refers to. **/ 
			    'click' : function ( event ) {
				var $tag = $(this),
				resource_data = $tag.closest('.resource').data('simplescraper'),
				data = $tag.data('simplescraper');
				if(!data.id) {
				    return false;
				}
				$resource = factory.make('resource', { location : data.model + '/' + data.id });
				factory.target.prepend($resource);
				$resource.trigger('get.simplescraper');
				return false;
			    },
			    /** Update the data for this tag (creating it if it does not yet exist.) **/
			    'put.simplescraper' : function ( event ) {
				var $tag = $(this),
				resource_data = $tag.closest('.resource').data('simplescraper'),
				data = $tag.data('simplescraper');
				if(!resource_data) {
				    return false;
				}
				_ajax({
				    type : 'put',
				    url  : resource_data.location + '/' + data.tag_directory + (data.id ? data.id : ''),
				    data : data.name ? { name : data.name } : {},
				    success : function ( ) {
					$('.resource').trigger('get.simplescraper'); // Could affect links in other resources.
				    }
				});
				return false;
			    },
			    /** Delete this tag. **/
			    'delete.simplescraper' : function ( event ) {
				var $tag = $(this),
				resource_data = $tag.closest('.resource').data('simplescraper'),
				data = $tag.data('simplescraper');
				if(!data.id) {
				    return false;
				}
				_ajax({
				    type : 'delete',
				    url  : resource_data.location + '/' + data.tag_directory + data.id,
				    success : function ( ) {
					$('.resource').trigger('get.simplescraper'); // Could affect links in other resources.
				    }
				});
				return false;
			    }
			}
		    },

		    /***
********* Tagger widget. 
* options.tag_directory : the relative location of the tags. (i.e. "gatherers/")
*/
		    'tagger' : {
			init : function ( options ) {
			    var $tagger = $('<input />');
			    return $tagger.data('simplescraper', {
				tag_directory : options.tag_directory,
				selected : false
			    }).autocomplete({
				minLength : 1,
				source    : function( request, response ) {
				    var data = $tagger.data('simplescraper'),
				    resource_data = $tagger.closest('.resource').data('simplescraper');
				    _ajax({
					url : resource_data.model + '/' + data.tag_directory,
					data : {
					    name : request.term + '%'
					},
					success : function ( resources ) {
					    labels = [];
					    for ( var i = 0; i < resources.length; i++ ) {
						labels.push({
						    label : resources[i].name,
						    value : resources[i].id
						});
					    }
					    response( labels );
					}
				    });
				},
				select    : function( event, ui ) {
				    var resource_data = $tagger.closest('.resource').data('simplescraper'),
				    data = $tagger.data('simplescraper');
				    data.autofill_selected = true;
				    if( ui.item ) {
					$tagger.val('');
					$tag = factory.make('tag', {
					    'tag_directory': data.tag_directory,
					    model : data.model,
					    id : ui.item.value
					});
					$tagger.append($tag);
					$tag.trigger('put.simplescraper');
				    }
				},
				open : function (event, ui) {
				    var resource_data = $tagger.closest('.resource').data('simplescraper'),
				    data = $tagger.data('simplescraper');
				    data.autofill_open = true;
				},
				close: function (event, ui) {
				    console.log('close');
				    var resource_data = $tagger.closest('.resource').data('simplescraper'),
				    data = $tagger.data('simplescraper');
				    console.log(data.autofill_selected);
				    data.autofill_open = false;
				    if(data.autofill_selected !== true) {
					console.log('triggered');
					$(this).trigger('blur');
				    }
				    data.autofill_selected = false;
				}
			    });
			},
			bindings : {
			    'blur' : function ( event ) {
				var $tagger = $(this),
				data = $tagger.data('simplescraper'),
				resource_data = $tagger.closest('.resource').data('simplescraper'),
				name = $tagger.val();
				if(data.autofill_open) {
				    return;
				}
				if(name != '') {
				    $tag = factory.make('tag', {
					'tag_directory' : data.tag_directory,
					model : resource_data.model,
					name : name
				    });
				    $tagger.append($tag);
				    $tag.trigger('put.simplescraper');
				}
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
			    if(options.value === null) {
				options.value = '';
			    }
			    return $('<textarea />').attr({name: options.name, value: options.value})
				.data('simplescraper', { lastValue: options.value });
			},
			bindings : {
			    'blur' : function( event ) { 
				var data = $(this).data('simplescraper');
				if(data.lastValue != $(this).val()) { // Only update if value changed.
				    data.lastValue = $(this).val();
				    $(this).trigger('put.simplescraper');
				}
			    }
			}
		    },

		    /**
******* Resource widget.
* options.location : where this resource is absolutely located (i.e. "/gatherer/1")
*/
		    'resource' : {
			init : function( options ) {
			    var $content = $('<div />'),
			    $draggable = $('<div />').draggable(),
			    model =  options.location.split('/')[0],
			    id = options.location.split('/')[1],
			    elem_class = model + id;
			    var $resource = $('<div />').css('position', 'absolute').addClass(elem_class)
				.data({
				    simplescraper : { 
					location : options.location,
					model : model,
					id : id,
					content : $content,
					attributes : { },
					elem_class : elem_class
				    }
				})
				.append($draggable
					/* Title */
					//.append($('<div />').text(model + ' ' + id).addClass('title'))
					/* Controls */
					.append($('<div />').addClass('upperright')
						.append(factory.make('close'))
						.append(factory.make('delete')))
					/* Content */
					.append($content)
				       );
			    return $resource;
			},
			bindings : {
			    /* Bring the resource up-to-date with the server. */
			    'get.simplescraper' : function( event ) { 
				var $resource = $(this),
				data = $resource.data('simplescraper'),
				$content = data.content.empty();
				
				if($('.' + data.elem_class).length > 1) { // Close if it's a dupe.
				    $resource.trigger('close.simplescraper');
				    return false;
				}
				_ajax({
				    type: 'get',
				    url: data.location,
				    /* Found the resource! */
				    success: function( response, obj, req ) {
					var keys = [];
					// Alphabetize.
					for (var key in response) { 
					    keys.push(key);
					}
					keys.sort();
					for( var i = 0; i < keys.length; i++) {
					    var key = keys[i];
					    if($.isArray(response[key])) {
						var tag_directory = key,
						$tagHolder = $('<div />').text(tag_directory + ': ').appendTo($content),
						tags = response[tag_directory];
						for( var j = 0 ; j < tags.length ; j++ ) {
						    $tagHolder.append(factory.make('tag', $.extend({
							'tag_directory' : tag_directory
						    }, tags[j])));
						}
						$tagHolder.append(factory.make('tagger', { 'tag_directory'  : tag_directory } ));
					    } else {
						var attribute_name = key,
						$attribute = factory.make('attribute', { name : attribute_name, value : response[attribute_name] });
						$attrHolder = $('<div />')
						    .append($('<label />').text(key + ': '))
						    .append($attribute)
						    .appendTo($content);
						data.attributes[attribute_name] = $attribute;
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
				    complete : function( ) {
					$('.resource').trigger('get.simplescraper'); // Could affect names of other resources (thus links)
				    },
				    error : function( ) { return; } // TODO: let user know their changes were not committed noisily.
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
					$('.resource').trigger('get.simplescraper'); // Could alter tags.
				    }
				});
				return false;
			    },	 

			    /** Close the resource window. **/
			    'close.simplescraper' : function( event ) {
				var $resource = $(this),
				waitForParent = setInterval(function() {
				    if($resource.parent().length > 0) {
					$resource.remove();
					clearInterval(waitForParent);
				    }
				}, 50);
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
			    'click' : function( event ) {
				$(this).trigger('close.simplescraper');
				return false;
			    }
			}
		    },

		    /**
***** Delete widget.
*/
		    'delete' : {
			init : function( ) { return $('<span />').text('delete'); },
			bindings : {
			    'click' : function( event ) {
				$(this).trigger('delete.simplescraper');
				return false;
			    }
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
	// The 'open' option contains an array of resources, or an individual resource, to open by default.
	init: function( options ) {
	    var factory = _factory({target: this}).init(),
	    $target = this;
	    if( options.open ) {
		var openAry = $.isArray( options.open ) ? options.open : [ options.open ],
		i;
		for ( i = 0; i < openAry.length; i++ ) {
		    factory.make('resource', { location: openAry[i] }).appendTo($target).trigger('get.simplescraper');
		}
	    }
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

