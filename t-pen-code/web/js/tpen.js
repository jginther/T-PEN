/* 
 * Document     : tpen.js
 * Created on   : July 11, 2011
 * Author       : cubap
 * Comment      : compatable with jQuery 1.6.2 and jQueryUI 1.8.9
 * 
 * Javascript used for general formatting and interaction with the T&#8209;PEN project
 */
var Page = {
    /** 
     *  Returns converted number to CSS consumable string rounded to n decimals.
     *  
     *  @param num float unprocessed number representing an object dimension
     *  @param n number of decimal places to include in returned string
     *  @returns float in ##.## format (example shows n=2)
     */
    convertPercent: function(num,n){
        return Math.round(num*Math.pow(10,(n+2)))/Math.pow(10,n);
    },
    /**
     * Sloppy hack so .focus functions work in FireFox
     * 
     * @param elem element to focus on
     */
    focusOn: function(elem){
        setTimeout("elem.focus()",0);
    },
    /**
     * Window dimensions.
     * 
     * @return Integer width of visible page
     */
    width: function() {
        return window.innerWidth !== null? window.innerWidth: document.body !== null? document.body.clientWidth:null;
    },
    /**
     * Window dimensions.
     * 
     * @return Integer height of visible page
     */
    height: function() {
        return window.innerHeight !== null? window.innerHeight: document.body !== null? document.body.clientHeight:null;
    }
};
$(function() {
/* jQuery Tabbed Interface */
    var $tabs = $( "#tabs" ).tabs();
    $(".gui-tab-section").addClass("left ui-widget-content ui-corner-tr ui-corner-bl");
    $('#tabs').tabs({fx: {opacity: 'toggle', duration:125}});
    $("#tabs").show("fade",250);
    $("#outer-barG").remove();
/* jQuery Handlers */
    $( ".returnButton" )        // return navigation links
        .addClass("ui-state-default ui-corner-bl ui-corner-br ui-helper-clearfix")          
        .prepend("<span class='ui-icon ui-icon-arrowreturnthick-1-w right'></span>")
        .hover(function(){$(this).addClass  ("ui-state-hover");},
            function(){$(this).removeClass  ("ui-state-hover");}
    );
    $("#options").on({           // shrunk button popovers
        mouseenter: function(event){
            var title = $(this).attr('title');
            $(event.target).before("<div class='btnTitle'>"+title+"</div>");
        },
        mouseleave: function(){
            $(".btnTitle").remove();
        }
    },'.wBtn.shrink');
    $(".tpenButton" )           // styled button-type links and tools
        .addClass("ui-state-default ui-corner-all ui-helper-clearfix")
        .hover(function(){
            $(this).addClass    ("ui-state-hover");
        }, function(){
            $(this).removeClass ("ui-state-hover");
        });
    $('span.delete')            // visual feedback before deleting
        .hover(function(){
            $(this).parent('li').find("a.tpenButton").addClass      ("ui-state-error strikeout");
        }, function(){
            $(this).parent('li').find("a.tpenButton").removeClass   ("ui-state-error strikeout");}
        );     
/* jQuery Interface Interactions */
    $("#forgetFormBtn")         // forgot user password form hide and reveal
        .click(function(){
            $("#forgetForm").slideToggle(500);
            $(this).children("span").toggle();
        });
    $("#main").addClass('ui-widget ui-widget-content ui-corner-all');
	(function( $ ) {
		$.widget( "ui.combobox", {
			_create: function() {
				var input,
					self = this,
					select = this.element.hide(),
					selected = select.children( ":selected" ),
					value = selected.val() ? selected.text() : "",
					wrapper = this.wrapper = $( "<span>" )
						.addClass( "ui-combobox" )
						.insertAfter( select );

				input = $( "<input>" )
					.appendTo( wrapper )
					.val( value )
					.addClass( "ui-state-default ui-combobox-input" )
					.autocomplete({
						delay: 0,
						minLength: 0,
						source: function( request, response ) {
							var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
							response( select.children( "option" ).map(function() {
								var text = $( this ).text();
								if ( this.value && ( !request.term || matcher.test(text) ) )
									return {
										label: text.replace(
											new RegExp(
												"(?![^&;]+;)(?!<[^<>]*)(" +
												$.ui.autocomplete.escapeRegex(request.term) +
												")(?![^<>]*>)(?![^&;]+;)", "gi"
											), "<strong>$1</strong>" ),
										value: text,
										option: this
									};
							}) );
						},
						select: function( event, ui ) {
							ui.item.option.selected = true;
							self._trigger( "selected", event, {
								item: ui.item.option
							});
						},
						change: function( event, ui ) {
							if ( !ui.item ) {
								var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" ),
									valid = false;
								select.children( "option" ).each(function() {
									if ( $( this ).text().match( matcher ) ) {
										this.selected = valid = true;
										return false;
									}
								});
								if ( !valid ) {
									// remove invalid value, as it didn't match anything
									$( this ).val( "" );
									select.val( "" );
									input.data( "autocomplete" ).term = "";
									return false;
								}
							}
						}
					})
					.addClass( "ui-widget ui-widget-content ui-corner-left" );

				input.data( "autocomplete" )._renderItem = function( ul, item ) {
					return $( "<li></li>" )
						.data( "item.autocomplete", item )
						.append( "<a>" + item.label + "</a>" )
						.appendTo( ul );
				};

				$( "<a>" )
					.attr( "tabIndex", -1 )
					.attr( "title", "Show All Items" )
					.appendTo( wrapper )
					.button({
						icons: {
							primary: "ui-icon-triangle-1-s"
						},
						text: false
					})
					.removeClass( "ui-corner-all" )
					.addClass( "ui-corner-right ui-combobox-toggle" )
					.click(function() {
						// close if already visible
						if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
							input.autocomplete( "close" );
							return;
						}

						// work around a bug (likely same cause as #5265)
						$( this ).blur();

						// pass empty string as value to search for, displaying all results
						input.autocomplete( "search", "" );
						input.focus();
					});
			},

			destroy: function() {
				this.wrapper.remove();
				this.element.show();
				$.Widget.prototype.destroy.call( this );
			}
		});
	})( jQuery );
            $(".combobox").combobox();
});
/**
 * Logs out any user without reloading the page.
 * Any user-specific information outside of #userKnown and
 * #userUnknown fields will remain until new page.
 */
function logout(){
    $.get("logout","",function(data){
        if (data === "Logout complete"){
            window.location = "index.jsp";
            return false;
        }
    });
}
var Export = {
  /** 
   * Returns valid state for folio range order.
   * 
   * @returns {Boolean}
   */
    validForm: function(){
        var firstFolio = $("#beginFolio").children("option:selected").index();
        var lastFolio = $("#endFolio").children("option:selected").index();
        if (firstFolio <= lastFolio) {
            return true;
        } else {
            alert("The " + Export.ordinal(firstFolio+1) +
                    " page comes after the " + Export.ordinal(lastFolio+1) +
                    " page in this project.\nPlease check your page range.");
            return false;
        }
    },
  /**
   * Converts any integer to an ordinal string.
   * 
   * @param {integer} n
   * @returns {String} converted to ordinal
   */
    ordinal: function(n) {
        if (10 < n && n < 14) return n + 'th';
        switch (n % 10) {
            case 1:
                return n + 'st';
            case 2:
                return n + 'nd';
            case 3:
                return n + 'rd';
            default:
                return n + 'th';
        }
    }
};
String.prototype.indices = function(string){
	var returns = [];
	var position = 0;
	while(this.indexOf(string, position) > -1){
		var index = this.indexOf(string, position);
		returns.push(index);
		position = index + string.length;
	}
	return returns;
}
/* Image Filters 
 * code borrowed from http://www.html5rocks.com/en/tutorials/canvas/imagefilters/
 * uses HTML 5 canvas element 
 * *\/
Filters = {};
Filters.getPixels = function(img) {
  var c = this.getCanvas(img.width, img.height);
  var ctx = c.getContext('2d');
  ctx.drawImage(img);
  return ctx.getImageData(0,0,c.width,c.height);
};
Filters.getCanvas = function(w,h) {
  var c = document.createElement('canvas');
  c.width = w;
  c.height = h;
  return c;
};
Filters.filterImage = function(filter, image, var_args) {
  var args = [this.getPixels(image)];
  for (var i=2; i<arguments.length; i++) {
    args.push(arguments[i]);
  }
  return filter.apply(null, args);
};
Filters.grayscale = function(pixels, args) {
  var d = pixels.data;
  for (var i=0; i<d.length; i+=4) {
    var r = d[i];
    var g = d[i+1];
    var b = d[i+2];
    // CIE luminance for the RGB
    // The human eye is bad at seeing red and blue, so we de-emphasize them.
    var v = 0.2126*r + 0.7152*g + 0.0722*b;
    d[i] = d[i+1] = d[i+2] = v
  }
  return pixels;
};
Filters.brightness = function(pixels, adjustment) {
  var d = pixels.data;
  for (var i=0; i<d.length; i+=4) {
    d[i] += adjustment;
    d[i+1] += adjustment;
    d[i+2] += adjustment;
  }
  return pixels;
};
Filters.threshold = function(pixels, threshold) {
  var d = pixels.data;
  for (var i=0; i<d.length; i+=4) {
    var r = d[i];
    var g = d[i+1];
    var b = d[i+2];
    var v = (0.2126*r + 0.7152*g + 0.0722*b >= threshold) ? 255 : 0;
    d[i] = d[i+1] = d[i+2] = v
  }
  return pixels;
};
Filters.tmpCanvas = document.createElement('canvas');
Filters.tmpCtx = Filters.tmpCanvas.getContext('2d');

Filters.createImageData = function(w,h) {
  return this.tmpCtx.createImageData(w,h);
};

Filters.convolute = function(pixels, weights, opaque) {
  var side = Math.round(Math.sqrt(weights.length));
  var halfSide = Math.floor(side/2);
  var src = pixels.data;
  var sw = pixels.width;
  var sh = pixels.height;
  // pad output by the convolution matrix
  var w = sw;
  var h = sh;
  var output = Filters.createImageData(w, h);
  var dst = output.data;
  // go through the destination image pixels
  var alphaFac = opaque ? 1 : 0;
  for (var y=0; y<h; y++) {
    for (var x=0; x<w; x++) {
      var sy = y;
      var sx = x;
      var dstOff = (y*w+x)*4;
      // calculate the weighed sum of the source image pixels that
      // fall under the convolution matrix
      var r=0, g=0, b=0, a=0;
      for (var cy=0; cy<side; cy++) {
        for (var cx=0; cx<side; cx++) {
          var scy = sy + cy - halfSide;
          var scx = sx + cx - halfSide;
          if (scy >= 0 && scy < sh && scx >= 0 && scx < sw) {
            var srcOff = (scy*sw+scx)*4;
            var wt = weights[cy*side+cx];
            r += src[srcOff] * wt;
            g += src[srcOff+1] * wt;
            b += src[srcOff+2] * wt;
            a += src[srcOff+3] * wt;
          }
        }
      }
      dst[dstOff] = r;
      dst[dstOff+1] = g;
      dst[dstOff+2] = b;
      dst[dstOff+3] = a + alphaFac*(255-a);
    }
  }
  return output;
};
if ($("body").is("#transcriptionPage")){
    var img = document.getElementById('imgTopImg');
    img.addEventListener('load', function() {

      var canvases = document.getElementsByTagName('canvas');
      for (var i=0; i<canvases.length; i++) {
        var c = canvases[i];
        c.parentNode.insertBefore(img.cloneNode(true), c);
        c.style.display = 'none';
      }

      function runFilter(id, filter, arg1, arg2, arg3) {
        var c = document.getElementById(id);
        var s = c.previousSibling.style;
        var b = c.parentNode.getElementsByTagName('button')[0];
        if (b.originalText == null) {
          b.originalText = b.textContent;
        }
        if (s.display == 'none') {
          s.display = 'inline';
          c.style.display = 'none';
          b.textContent = b.originalText;
        } else {
          var idata = Filters.filterImage(filter, img, arg1, arg2, arg3);
          c.width = idata.width;
          c.height = idata.height;
          var ctx = c.getContext('2d');
          ctx.putImageData(idata, 0, 0);
          s.display = 'none';
          c.style.display = 'inline';
          b.textContent = 'Restore original image';
        }
      }
      grayscale = function() {
        runFilter('grayscale', Filters.grayscale);
      }

      brightness = function() {
        runFilter('brightness', Filters.brightness, 40);
      }

      threshold = function() {
        runFilter('threshold', Filters.threshold, 128);
      }

      sharpen = function() {
        runFilter('sharpen', Filters.convolute,
          [ 0, -1,  0,
           -1,  5, -1,
            0, -1,  0]);
      }
      blurC = function() {
        runFilter('blurC', Filters.convolute,
          [ 1/9, 1/9, 1/9,
            1/9, 1/9, 1/9,
            1/9, 1/9, 1/9 ]);
      }
      sobel = function() {
        runFilter('sobel', function(px) {
          px = Filters.grayscale(px);
          var vertical = Filters.convoluteFloat32(px,
            [-1,-2,-1,
              0, 0, 0,
              1, 2, 1]);
          var horizontal = Filters.convoluteFloat32(px,
            [-1,0,1,
             -2,0,2,
             -1,0,1]);
          var id = Filters.createImageData(vertical.width, vertical.height);
          for (var i=0; i<id.data.length; i+=4) {
            var v = Math.abs(vertical.data[i]);
            id.data[i] = v;
            var h = Math.abs(horizontal.data[i]);
            id.data[i+1] = h
            id.data[i+2] = (v+h)/4;
            id.data[i+3] = 255;
          }
          return id;
        });
      }
      custom = function() {
        var inputs = document.getElementById('customMatrix').getElementsByTagName('input');
        var arr = [];
        for (var i=0; i<inputs.length; i++) {
          arr.push(parseFloat(inputs[i].value));
        }
        runFilter('custom', Filters.convolute, arr, true);
      }
    }, false);
}*/