/**
*
* JavaScript for SimpleScraper.
*
**/

$(document).ready(function() {
    /* Allows for PUT and DELETE from forms, reloads upon success. */
    $('form').each(function() {
	var method = $(this).attr('method');
	$(this).ajaxForm({
	    type : method,
	    success : function() {
		location.reload();
	    },
	    error : function(response, responseText) {
		$.error( responseText );
	    }
	});
    });
    /* Autofill 'add' inputs, which are used for tagging & resource creation. */
    $('input.add').each(function() {
	var $input = $(this);
	$input.autocomplete({
	    minLength : 1,
	    /* Replace 'term' with the input's name, plus wildcard. */
	    source : function ( request, response ) {
		var term = $input.attr('name'),
		data = {};
		data[term] = '%' + request.term + '%';
		$.ajax({
		    url : $input.attr('title'),
		    dataType : 'json',
		    data : data,
		    success : function ( labels ) {
			console.log(labels);
			response ( labels );
		    },
		    error : function ( response, responseText ) {
			$.error( responseText );
		    }
		});
	    }
	});
    });
    /* Accordion. */
    $('.accordion').accordion({autoHeight: false});
    /* Button. */
    $('button').button();
    /* Tabs. */
    $('.tabs').tabs({
	cookie : {}
    });
});

