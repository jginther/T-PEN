/* 
 * author: cubap
 * date: July 5, 2012
 * 
 * Customized manipulation for the T-PEN instance of SCIAT
 */
function getURLParameter(name) {
    var toret = decodeURI(
    (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
    if (toret == "null") toret = null;
    return toret;
}
// URI of selected Canvas
if (parent.folio) var folio = parent.folio;
var thisCanvas = "http://t-pen.org/canvases/"+folio;
var repoList = [];
var sciatCanvas = {
    drawAnnos:  function (annos) {
        var drawn = [];
        if (annos.length == 0) {
            alert ('There are no annotations associated with this image.');
            return false;
        }
        for(a in annos) {
            console.log(annos[a]);
            var anno = annos[a];
            // build SVG annotation to put in
            var attributes = {
                "text_annotation": anno.body,
                "uri_ref": anno.annotationURI,
                "creator": anno.annotator_name,
                "generator": anno.generator,
                "creation_date": sciatCanvas.dayFromDayOfYear(anno.creation)
            };
            // These dimensions should already be formatted for svg consumption
            // e.g., 'circle' would be "{'svg':'circle','cx':45,'cy':60,'r':4}"
            var dims = anno.getDimensions();
            var annoAttributes = JSON.parse(dims);
            if (annoAttributes.svg) {
                anno.shape = annoAttributes.svg;
                delete annoAttributes.svg;
            }
            var JSONsvg = {
                        "element"   : anno.shape,
                        "attr"      : $.extend(annoAttributes,attributes),
                        "curStyles" : true
                    };
            // adds element and returns it
            var addedAnno = document.getElementById("annotationTool").contentWindow.svgCanvas.addSvgElementFromJson(JSONsvg);
            drawn.push(addedAnno);
            // Disable the button to load
            $('#loadAllAnnotations').attr('disabled','disabled').addClass('ui-state-disabled');
        }
        sciatCanvas.buildFilters();
        return drawn;
    },
    displayAnnotations: function () {
        var annoStore = new AnnotationStore("http://165.134.241.141/Annotation/Annotation",'T-PEN Annotation Store','http://t-pen.org/login.jsp');
        annoStore.fetch(thisCanvas, 'ContentAnnotation', sciatCanvas.drawAnnos);
    },
    listAnnotationsInPreview: function () {
        $('.previewPage').each(function(){
            var thisPage = "http://t-pen.org/canvases/"+$(this).attr("data-pagenumber");
            var annoStore = new AnnotationStore("http://165.134.241.141/Annotation/Annotation",'T-PEN Annotation Store','http://t-pen.org/login.jsp');
            annoStore.fetch(thisPage, 'ContentAnnotation', sciatCanvas.toPreview);
        })
    },
    toPreview: function(annos) {
        if (!annos) return;
        var toInsert = [];
        for (a in annos) {
            var anno = annos[a];
            var tmp = '<span class="previewAnnoText clear-left" title="' + 
                sciatCanvas.dimsToString(anno) + '">' +
                anno.body + 
                '<span class="ui-icon ui-icon-pin-w left"></span></span>';
            toInsert.push(tmp);
            // find relevant folio
            var target = anno.target
            var folioIndex = target.lastIndexOf('/');
            var targetFolio = target.substr(folioIndex+1);
            $('.previewPage[data-pagenumber="'+targetFolio+'"]').find('.previewAnnotations').append(toInsert.join(''));
        }
    },
    dimsToString: function (anno) {
        // These dimensions should already be formatted for svg consumption
        // e.g., 'circle' would be "{'svg':'circle','cx':45,'cy':60,'r':4}"
        var annoAttributes = JSON.parse(anno.getDimensions());
        var theDims = [];
        for(dim in annoAttributes) {
            if (dim == 'svg') continue;
            theDims.push(dim+": "+annoAttributes[dim]);
        }
        return theDims.join(', ')
    },
    buildFilters: function() {
        // Build filters for annotation tool: Name, Date
        var allAnnotations = $('#annotationTool').contents().find('[creator]');
        var annoNames = [], annoDates = [];
        allAnnotations.each(function(){
            var thisCreator = $(this).attr('creator');
            var thisDate = $(this).attr('creation_date');
            if ($.inArray(thisCreator,annoNames) < 0) {
                annoNames.push(thisCreator);
            }
            if ($.inArray(thisDate,annoDates) < 0) {
                thisDate = thisDate.substring(0, thisDate.length-3);
                annoDates.push(thisDate);
            }
        });
        if (annoDates.length > 5) {
            // shorten list by consolidating days
            var dayDates = [];
            for (day in annoDates) {
                var thisDay = annoDates[day].substring(0,10); // Just the date, no time
                if ($.inArray(thisDay, dayDates) < 0) {
                    // add to range
                    dayDates.push(thisDay);
                }
            }
            annoDates = dayDates;
        }
        if (annoDates.length > 8) {
            // shorten list by consolidating timeframes
            var rangeDates = [];
            var today = new Date();
            var lastWeek = new Date(today.getDate()-7);
            var lastMonth = new Date(today.getMonth()-1);
            var lastYear = new Date(today.getYear()-1);
            for (day in annoDates) {
                var theseDates = new Date(annoDates[day]);
                if (theseDates < lastYear) {
                    if ($.inArray("Long ago", rangeDates) < 0) {
                        // add to range
                        rangeDates.push("Long ago");
                    }
                } else if (theseDates < lastMonth) {
                    if ($.inArray("This year", rangeDates) < 0) {
                        // add to range
                        rangeDates.push("This year");
                    }
                } else if (theseDates < lastWeek) {
                    if ($.inArray("This month", rangeDates) < 0) {
                        // add to range
                        rangeDates.push("This month");
                    }
                } else if (theseDates < today) {
                    if ($.inArray("This week", rangeDates) < 0) {
                        // add to range
                        rangeDates.push("This week");
                    }
                } else {
                    // is today
                    rangeDates.push("Today");
                }
            }
            // If this is a reasonable change, make it
            if (rangeDates.length > 2) annoDates = dayDates;
        }
        var dateSelect = $('<select id="dateSelect"><option value="none" selected="selected">Filter by Date</option></select>');
        var nameSelect = $('<select id="nameSelect"><option value="none" selected="selected">Filter by Creator</option></select>');
        for (date in annoDates) {
            var aDate = '<option value="'+annoDates[date]+'">'+annoDates[date]+'</option>';
            dateSelect.append(aDate);
        }
        for (name in annoNames) {
            var aName = '<option value="'+annoNames[name]+'">'+annoNames[name]+'</option>';
            nameSelect.append(aName);
        }
        // Attach filters to toolbar
        $("#annotation_toolbar").append(dateSelect,nameSelect);
        $('#dateSelect').change(function(){sciatCanvas.filterByDate(this)});
        $('#nameSelect').change(function(){sciatCanvas.filterByName(this)});
    },
    filterByDate : function(dropdown) {
        var allAnnotations = $('#annotationTool').contents().find('[creation_date]');
        allAnnotations.show();
        var filter = dropdown.value;
        var isDate = !isNaN(parseFloat(new Date(filter) - 1));
        if (isDate) {
            // Find and remove all annotations previous to this date 
            var dateType = (filter.length > 10) ? "full" : "day";
            var recent = new Date(filter);
            allAnnotations.each(function(){
                var generated = new Date(this.getAttribute('creation_date'));
                if (dateType == "full") {
                    // compare full
                } else {
                    // compare day
                    recent.setHours(0,0,0,0);               
                }
                if (recent > generated) {
                    $(this).hide();
                }
            });
        } else {
            // String Date : [Long ago, This year, This month, This week, Today]
            var today = new Date();
            var compareRange;
            switch (filter) {
                case "This year" :
                    compareRange = new Date(today.getYear());
                    break;
                case "This month" :
                    compareRange = new Date(today.getMonth());
                    break;
                case "This week" :
                    compareRange = new Date(today.getDate()-7);
                    break;
                case "Today" :
                    compareRange = new Date(today);
                    break;
                default :
                    return;
            }
            compareRange.setHours(0,0,0,0);
            allAnnotations.each(function(){
                var generated = new Date(this.getAttribute('creation_date'));
                    if (compareRange > generated) {
                        $(this).hide();
                    }
            });
        }
    },
    filterByName : function(dropdown) {
        var allAnnotations = $('#annotationTool').contents().find('[creator]');
        allAnnotations.show();
        var filter = dropdown.value;
        allAnnotations.each(function(){
            var creator = this.getAttribute('creator');
            if (creator != filter) {
                $(this).hide();
            }
        });
        if (!allAnnotations.filter('[creator="'+filter+'"]')) {
            // None were found, hide none
            allAnnotations.show();
        }
    },
    dayFromDayOfYear : function (n, y) {
        // Hack for bad dates
        // --This is an adjustment for bad test data and can be safely removed
        if (n.indexOf(" ") == 9) {
            num = n.substring(6,9);
            if(!y) y= new Date().getFullYear();
            var d= new Date(y, 0);
            var tmp = new Date(d.setMonth(0, num));
            return "20"+n.substring(0,6)+tmp.getDate()+n.substring(9);
        } else {
            return n;
        }
    }
}
var AnnotationData = {
    getURI: function (elem) {
        return elem.getAttribute("uri_ref");
    },
    getText: function (elem) {
        return elem.getAttribute("text_annotation");
    },
    getShape: function (elem) {
        return elem.tagName;
    },
    getTarget: function (elem) {
        return thisCanvas;
    },
    buildDimensions: function (elem) {
        var thisSvg = $(elem).clone();
        var JSONtoString = AnnotationData.elementToJSON(elem);
        if (thisSvg[0].tagName == "g") {
            thisSvg.children().each(function(index){
                // need a good way to identify a group of things
                JSONtoString["grouped_shapes"+index] = AnnotationData.elementToJSON(this);
            });
        }
        return JSON.stringify(JSONtoString).replace(/"/g,"'");
    },
    elementToJSON: function (elem) {
        var toret = {'svg':elem.tagName};
        for (var attr, i=0, attrs=elem.attributes, l=attrs.length; i<l; i++){
            attr = attrs.item(i);
            // Eliminate standard and redundant attributes
            if (new RegExp(/\bfill|\bstroke|style|\bid\b|uri_ref|text_annotation|target_canvas/).test(attr.nodeName)) {
                continue;
            }
            toret[attr.nodeName] = attr.nodeValue;
        }
        return toret;
    },
    saveAll: function () {
        //find all changed annotations
        // attribute 'text_annotation' is added to any svg element once
        // the text has been altered.
        var updatedAnnotations = $('#annotationTool').contents().find('[text_annotation]');
        //save them
        updatedAnnotations.each(function(index){
            if (AnnotationData.getURI(this)) {
                new Annotation(AnnotationData.getText(this), AnnotationData.getTarget(this), AnnotationData.getShape(this), AnnotationData.buildDimensions(this), AnnotationData.getURI(this)).update();
                console.log("updated "+AnnotationData.getShape(this));
            } else {
                //save a new annotation
                AnnotationData.createNew(this);
            }
        });
    },
    createNew: function (elem) {
        //submit a new annotation
        var newAnnotation = new Annotation(this.getText(elem), this.getTarget(elem), this.getShape(elem), this.buildDimensions(elem));
        // attach the returned URI to the element
        console.log(newAnnotation);
        $(elem).one('ajaxComplete',function(){
            this.setAttribute("uri_ref", newAnnotation.annotationURI);
            // SCIAT message for new annotations
            $('#SCIATloadMsg').remove();
            var SCIATloadMsg = $("<div class='ui-state-active' id='SCIATloadMsg' style='position:absolute;left:40%;top:60%;padding:.5em;'>Annotation Saved.</div>");
            $('#annotationTool').after(SCIATloadMsg);
            $('#SCIATloadMsg').animate({
                top: '0%'
            }, 2000, function(){
                $('#SCIATloadMsg').fadeOut(4000);
            });
        });
    }
}