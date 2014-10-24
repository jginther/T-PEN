/* 
 * Document     : tpen.js
 * Created on   : August 08, 2011
 * Author       : cubap,jdeerin1
 * Comment      : compatable with jQuery 1.6.2 and jQueryUI 1.8.9
 * 
 * Javascript used for interaction with the T&#8209;PEN transcription GUI and tools 
 */

/* Variables */
// layout
var ie = (document.all) ? true : false;
var bookmarkPadding = 0;        //adjustable padding for the bookmark bounding box
var topImgPosition, bottomImgPosition, imgBottomHeight, bookmarkTop, bookmarkHeight, bookmarkLeft, bookmarkWidth, imgTopHeight, captionHeight;
var screenMultiplier = 0;
var jumpMultiplier = 0;
var $prevLine, $prevNote;
var maxImgTop = 0.7;
var workspaceHeight = function () {
    return $("#workspace").height() + 24; // include padding
};
var lineBtnPrev = '<a class="previousLine wLeft ui-corner-all ui-state-default ui-button" title="previous"><span class="ui-icon ui-icon-seek-prev left"></span>Previous Line</a>';
var lineBtnNext = '<a class="nextLine wRight ui-corner-all ui-state-default ui-button" title="next"><span class="ui-icon ui-icon-seek-next right"></span>Next Line</a>';
var lineBtnLast = '<a class="nextLine wRight ui-corner-all ui-button navigation" onclick="$(\'#nextPage\').click();" title="End of Page"><span class="ui-icon ui-icon-seek-next right"></span>Next Page</a>';
// user interface
var isUnsaved = function () {
    return ($(".isUnsaved").size() > 0);
};
var isUnadjusted = true;
var isFullscreen = true;
var isZoomed = false;
var isBlank = true;
var zoomMemory = new Array(2);
var zoomMultiplier = 2;
var isMagnifying = false;
var dragHelper = "<div id='dragHelper'></div>";
var isDestroyingLine = false;
var focusItem = [$("#t1"), $("#t1")];
var currentFocus;
var clickPassed = false;
var ParsingInterval, ResizeTopImg, WaitToFocus;
// tools
var liveTool = "none";
var compareIndex = "none";
var abbrevLabelsAll;
var loadValues = [];
var month = ["January", "February", "March", "April", "May", "June", "July",
    "August", "September", "October", "November", "December"];
var selectRulerColor = 'black';
var isAddingLines = true;
var linebreakString = "<br>";
var brokenText = [""];
var imgRatio = 1;
var leftovers;
var columnCount;
var isMember, permitOACr, permitOACw, permitExport, permitCopy, permitModify,
    permitAnnotation, permitButtons, permitParsing, permitMetadata,
    permitNotes, permitRead;
isMember = permitOACr = permitOACw = permitExport = permitCopy = permitModify
    = permitAnnotation = permitButtons = permitParsing = permitMetadata
    = permitNotes = permitRead = false;
var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
/* Screen Interactions */
var Screen = {
    /**
     *  Sets the maximum width for tools in splitscreen.
     *  
     *  @param width in pixels of unadjusted width for #wrapper
     */
    limit: function (width) {
        var limit = Page.width() / 3 * 2;
        if (width > limit) {
            width = limit;
        }
        return width;
    },
    restore: function (btn,event) {
      if($(btn).hasClass('ui-state-active')) {
        event.stopPropagation();
        Screen.fullsize(event);
        return false;
      }
      return true;
    },
    /**
     *  Restores full screen transcription interface. Removes overlays; resets 
     *  liveTool, isZoomed, isFullscreen, isUnadjusted. 
     *  
     */
    fullsize: function (event) {
        window.clearInterval(ParsingInterval);
        window.clearInterval(WaitToFocus);
        window.clearInterval(ResizeTopImg);
        if ($(event.target).hasClass('ui-resizable-handle')) {
            return false;
        }
        if ($("#overlay").is(":visible")) {
            $("#overlay").click();
            return false;
        }
        if ($(".transcriptlet").size() == 0) {
            // no lines to transcribe
            var cfrm = confirm("There are no longer any lines on this page. Click 'OK' to reload the page or 'Cancel' to parse manually.");
            if (cfrm) {
                window.location.reload();
            } else {
                $('#parsingBtn').click();
            }
        }
//        if (isMember || permitAnnotation) {
//            Annotation.autoSave();
//        }
        liveTool = "none";
        $("#wrapper").resizable('disable');
        Interaction.activateToolBtn(null);
        $("[id$='Split']").hide();
        $(".line, .parsing, .adjustable,.parsingColumn").remove();
        $("#help").css({"left":"100%"}).fadeOut(1000);
        $("#helpPanels").css({"margin-left":"0px"});
        $("#imgTopImg").css({
            height  :   "auto",
            width   :   "100%"
        });
//        $("#imgTop").css("overflow","hidden");
        $("#tools")
//            .width("0%")
            .find("img").height("").width("");
        $("#wrapper,#bookmark").show().width("100%").css("padding", "0");
        $(".btnTitle").remove();
        $("#workspace, #imgBottom, #location").show();
        isUnadjusted = isFullscreen = true;
        currentFocus = "transcription" + focusItem[1].attr('id').substring(1);
        WaitToFocus = window.setTimeout("focusItem[1].focusin()", 10);
        $("#fullscreenBtn").fadeOut(250);
        isZoomed = false;
        $(".magnifyBtn").each(function(){
          if ($(this).hasClass('ui-state-active')) $(this).click();
        });
        event.preventDefault();
//        return false;
    },
    /**
     * Adjusts font-size in transcription and notes fields based on size of screen.
     * Minimum is 13px and maximum is 18px.
     * 
     */
    textSize: function () {
        var textSize = Math.floor(focusItem[1].find(".theText").width() / 60),
            resize = (textSize > 18) ? 18 : textSize,
            wrapperWidth = $('#wrapper').width();
        resize = (resize < 13) ? 13 : resize;
        $(".theText,.notes,#previous span,#helpPanels ul").css("font-size",resize+"px");
        if (wrapperWidth < 550) {
            Interaction.shrinkButtons();
        } else {
            Interaction.expandButtons();
        }
    },
    /**
     * Reveals or hides note textarea.notes beneath current textarea.
     * Original id value is indexed. Inserted values match lineid.
     * 
     * @param notes jQuery object .transcriplet
     */
    notesToggle: function (notes) {
        notes.find('.notes').slideToggle('normal', function () {
            if ($(this).is(':hidden')) {
                notes.find('.theText').focus();
            } else {
                notes.find('.notes').focus();
            }
            Screen.maintainWorkspace();
        });
        $('#note' + notes).toggleClass('ui-state-active');
    },
    /**
     * Sets values for positioning variables used to set the screen.
     * 
     * @see updatePresentation()
     */
    setPositions: function(){
        // Scale stored dimensions based on 1000 pixel image height
        screenMultiplier = $("#imgBottom img").height()/1000;
        //Determine size of section above workspace
        if(focusItem[1].find(".lineHeight").val() != null){
            var currentLineHeight = parseInt(focusItem[1].find(".lineHeight").val());
            var currentLineTop    = parseInt(focusItem[1].find(".lineTop").val());
            // top of column
            var previousLine = (focusItem[1].prev().is('.transcriptlet') && (currentLineTop > parseInt(focusItem[1].prev().find(".lineTop").val())) ) ? parseInt(focusItem[1].prev().find(".lineHeight").val()) : focusItem[1].find(".lineTop").val();
            // oversized for screen
            if (Page.height()-20 < (previousLine+currentLineHeight)*screenMultiplier+workspaceHeight()) previousLine = (previousLine==0) ? 0 : 20; 
            imgTopHeight = (previousLine+currentLineHeight)*screenMultiplier+20;//DEBUG+workspaceHeight(); // obscure behind workspace
            // topImgPosition, bookmarkTop - other bookmark dimensions stored in hidden inputs
            topImgPosition = (previousLine - currentLineTop)*screenMultiplier;
            bookmarkTop = (previousLine - bookmarkPadding)*screenMultiplier;
            bookmarkHeight = (currentLineHeight + 2*bookmarkPadding)*screenMultiplier;
            // bottomImgPosition
//            bottomImgPosition = (focusItem[1].next().is(".transcriptlet")) ? -(parseInt(focusItem[1].next().find(".lineTop").val()))*screenMultiplier: -((currentLineTop)+currentLineHeight)*screenMultiplier;
            bottomImgPosition = -(currentLineTop+currentLineHeight)*screenMultiplier;
            //imgBottomHeight = Page.height()-imgTopHeight;
            //control the empty space, if needed
            if (imgTopHeight > ($("#imgTopImg").height()+topImgPosition)) imgTopHeight = ($("#imgTopImg").height()+topImgPosition);//DEBUG+workspaceHeight());
//    //DEBUG this should leave the bookmark the right size and just tuck it behind the workspace
//            //if (bookmarkHeight > (imgTopHeight-bookmarkTop)) bookmarkHeight =(imgTopHeight-bookmarkTop);
//            if ((bookmarkTop + bookmarkHeight) > imgTopHeight){
////                bookmarkTop = 62 - imgTopHeight;
////                topImgPosition = 62 - parseInt(focusItem[1].find(".lineTop").val())*screenMultiplier;
//            }
//            if (bookmarkHeight < 0){
////                bookmarkTop = 0;
////                topImgPosition = -parseInt(focusItem[1].find(".lineTop").val())*screenMultiplier;
//                bookmarkHeight = parseInt(focusItem[1].find(".lineHeight").val())*screenMultiplier;
//            } 
        }
    },
    /**
     * Shows previous line of transcription above current textarea.
     * 
     * @see updatePresentation()
     */
    updateCaptions: function() {
        if (focusItem[1].attr("id") != "t1"){
            $prevLine = focusItem[1].prev().find('.theText').val();
            $prevNote = focusItem[1].prev().find('.notes').val();
        } else {
            $prevLine = "You are at the top of your document.";
            $prevNote = "Your transcription will appear here.";
            //check for previous page
            if ($(".previewPage").eq(0).attr("data-pagenumber") != folio){
                var lastPage = $(".previewPage").eq(0).find(".previewText").eq(-1).text();
                if (lastPage.length > 0){
                    $prevLine = lastPage;
                    $prevNote = "(from previous page)";
                }
            }
            //lowlight tags in preview
            $prevLine = Preview.scrub($prevLine);
            $('#texts').html($prevLine);
            $('#notes').text($prevNote);
            $('#captions').css("height","4em").find("span").css("opacity",1);
            return true;
        }                    
        $('#texts').text($prevLine);
        $('#notes').text($prevNote);
    },
    /**
     * Removes previous textarea and slides in the new focus.
     * 
     * @see updatePresentation()
     */
    swapTranscriptlet: function() {
        // hide help if open
        $("#help").css({"left":Page.width()+"px"});
        $("#helpPanels").css({"margin-left":"0px"});
        // slide up or down
//        var transcriptletSlide;
//        transcriptletSlide = (parseInt(focusItem[1].find(".lineTop").val()) < parseInt(focusItem[0].find(".lineTop").val())) ? -60: 100;
        // include any closing tags
        focusItem[0].addClass("transcriptletBefore").removeClass('noTransition').find(".xmlClosingTags").children().appendTo(focusItem[1].find(".xmlClosingTags"));
        Interaction.updateClosingTags();
        // slide in the new transcriptlet
        focusItem[1].css({"width":"auto","z-index":"5"});
        focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
        //place the cursor and scroll at the top of the textarea
        Interaction.setCursorPosition(focusItem[1].find(".theText")[0],0);
        //slide out old transcriptlet and hide offscreen
//        focusItem[0].one("webkitTransitionEnd transitionend oTransitionEnd",function(){
//            $(this).css({
//                "left":"-9000px",          
//                "position":"absolute",
//                "z-index":"3"
//            });  
//        });
//        focusItem[0].css({
//            "width":focusItem[0].width()
//        });
        focusItem[1].prevAll(".transcriptlet").addClass("transcriptletBefore").removeClass("transcriptletAfter");
        focusItem[1].nextAll(".transcriptlet").addClass("transcriptletAfter").removeClass("transcriptletBefore");       
//        focusItem[1].find(".theText").focus();
        isUnadjusted = true;
//        if (focusItem[0].attr("id")==focusItem[1].attr("id")) {
//            focusItem[1].css({
//                "position":"relative",
//                "z-index":"5",
//                "opacity":1,
//                "top":"0px"
//            });
//        }
    },
    /**
     * Aligns images and workspace using defined dimensions.
     * 
     * @see maintainWorkspace()
     */
    adjustImgs: function(){
        //move background images above and below the workspace
        $("#imgTop").css({
            "height":imgTopHeight+"px"
            })
        .find("img").css({
            top:    topImgPosition + "px",
            left:   "0px"
        });
        //realign indicator of location
        $('#bookmarkText').html($(focusItem[1]).find(".counter").text().replace(" ", "&nbsp;"));   //line number is experiemental TODO
        bookmarkLeft = parseInt($(focusItem[1]).find('.lineLeft').val())*screenMultiplier;
        bookmarkWidth = parseInt($(focusItem[1]).find('.lineWidth').val())*screenMultiplier;
        $('#bookmark').css({ 
            left:   bookmarkLeft+"px",
            top:    bookmarkTop+"px",
            height: bookmarkHeight+"px",
            width:  bookmarkWidth+"px"
        })
        .attr("lineid",focusItem[1].attr("data-lineid"));
        $("#imgBottom")
        //.height(imgBottomHeight)
        .find("img").css({
            top:    bottomImgPosition+"px",
            left:   "0px"
        });
//DEBUG        $("#workspace").css("margin-top",-workspaceHeight()+"px");
    },
    /**
     * Keep workspace on the screen when displaying large lines.
     * Tests for need and then adjusts. Runs on change to 
     * workspace size or line change.
     */
    maintainWorkspace: function(){
        // keep top img within the set proportion of the screen
        if (imgTopHeight > Page.height()) {
            imgTopHeight = Page.height();
        }
        this.adjustImgs();
    },
    /**
     * Organizes the screen when a new line is focused on.
     * Tests if adjustment is needed before running.
     * 
     * @see setPositions()
     * @see updateCaptions()
     * @see swapTranscriptlet()
     */
    updatePresentation: function(object){
        if (liveTool == "parsing") return false;
        focusItem[0]=focusItem[1];
        focusItem[1]=object;
        if ((focusItem[0] == null) || (focusItem[0].attr("id")!=focusItem[1].attr("id"))){
            this.setPositions();
            this.updateCaptions();
            this.swapTranscriptlet();
            History.contribution();
            //show previous line transcription
            $('#previous span').animate({
                opacity:1
            },100);                           
            Screen.maintainWorkspace();
            if (liveTool === "history"){
                History.showLine(focusItem[1].attr("data-lineid"));
            }
            Data.saveTranscription();       
        } else {
            //will only adjust if page load or refocusing after changing the browser size
            if (isUnadjusted){
                this.setPositions();
                this.maintainWorkspace();
            }
        }
        Screen.textSize();
        //prevent textareas from going invisible and not moving out of the workspace
        focusItem[1].removeClass("transcriptletBefore transcriptletAfter");
        if(document.activeElement.id == "transcriptionPage"){
            // nothing is focused on somehow
            focusItem[1].find('.theText')[0].focus();
        }
    },
    /**
     * Determines action based on transcription line clicked and tool in use.
     * Alerts 'unknown click' if all fails. Calls lineChange(e,event) for 
     * parsing tool. Jumps to transcriptlet for full page tool.
     */
    clickedLine: function(e,event) {
        if ($(e).hasClass("parsing")){
            if ($("#addLines").hasClass('ui-state-active')||$("#removeLines").hasClass('ui-state-active')){
                Parsing.lineChange(e,event);
            }
        }
        else if (clickPassed)
            $(e).addClass("jumpLine");
        if ($(e).hasClass("jumpLine"))
            $("#transcription"+($(e).index(".line")+1)).focus();
    },
    /**
     * Hard reset for the screen based around focus. If the focus is not
     * available, the first line becomes the focus.
     * 
     * @param focus ID of transcriptlet to build around
     * @see updatePresentation()
     */
    resetWorkspace: function(focus){
        isUnadjusted = true;
        focusItem[1] = (focus != null) ? focus : $("#t1");
        var focusText = focusItem[1].find(".theText");
        $("#popin > div").hide();
        Screen.updatePresentation(focusItem[1]);
        $.get("tagTracker",{
            listTags    : true,
            folio    : folio,
            projectID   : projectID
        }, function(tags){
            if(tags != null){     
                Interaction.buildClosingTags(tags.split("\n"));
            }
        });
        focusText.focus();
    },
    /**
     *  Cleans up the display when the window has been resized.
     */
    doneResizing: function(){
        // fires inappropriately when resizing #wrapper FIXME
//        if (liveTool == 'none'){
//            $("#fullscreenBtn").click();
//        } else {
//            var activate = (liveTool.indexOf("frameBtn") == 0) ? liveTool : liveTool+"Btn";
//            $("#"+activate).click();
//        }
    },
    /**
     *  Creates highlights on tags in the preview tool.
     *  
     *  @param newText String to be placed on tags
     *  @see Screen.spannedText(textToSpan)
     */
    loadText: function(newText){
        var oldText = $("#loadText").text();
        var spannedText = this.spannedText(oldText);
        var newSpan = this.spannedText(newText).replace("<span>", "<span style='opacity:0;'>");
        $("#loadText").fadeOut(250,function(){
            $(this).html(newText).fadeIn(250);
        });
    },
    /**
     *  Wraps transcription text is spans for preview tool
     *  
     *  @param textToSpan array of strings to wrap with spans
     */
    spannedText: function(textToSpan){
        var toret = new Array();
        for (var i=0;i<textToSpan.length;i++){
            toret.push(textToSpan.substr(i,1));
        }
        return "<span>"+toret.join("</span><span>")+"</span>";
    }
};

/* T-PEN Tools */
var Parsing = {
    /** 
     * Sets screen for parsing tool use.
     * Slides the workspace down and scales the top img
     * to full height.
     */
     hideWorkspaceForParsing: function(){
        if(!isMember && !permitParsing)return false;
        $("#wrapper").resizable('disable');
        $("#fullscreenBtn,#location").hide()
//            .show().find("span")
//            .css({"margin-bottom":arrowSpacing+"px"});
        $("#tools").children("[id$='Split']").hide();
//        var topImg = $("#imgTopImg");
        imgRatio = $("#imgTopImg")[0].width/$("#imgTopImg")[0].height;
        var wrapWidth = imgRatio*Page.height();
        var PAGEWIDTH = Page.width();
        if (wrapWidth > PAGEWIDTH-350)wrapWidth = PAGEWIDTH-350;
        $("#tools").children("[id$='Split']").hide();
        $("#parsingSplit").css({
            "width"   :   "auto"
        }).fadeIn();
        $("#imgTopImg").css({
                "top":"0px",
                "left":"0px",
                "height":"auto",
//                "width":"100%",
                "overflow":"auto"
        });
        $("#wrapper").width(wrapWidth/PAGEWIDTH*100+"%").css("padding","0");
//        $("#workspace,#imgBottom").fadeOut();
        $("#workspace,#imgBottom").hide();
        // Safari DEBUG to include min-height
        ResizeTopImg = window.setTimeout('$("#imgTop").height($("#imgTopImg").height())', 850);
        ParsingInterval = window.setTimeout(function(){
            Interaction.writeLines($("#imgTopImg"));
            $("#imgTop").children(".line").addClass("parsing").removeClass("line");
            $("#bookmark").css("left","-9999px");
            var firstLine = $(".parsing").filter(":first");
            if ($.browser.opera) firstLine += 2;
            // Find the correct height for a well-displayed tool with a full-height image.
            var correctHeight = ($("#imgTopImg").height() > Page.height()) ? -999 : firstLine.attr("lineheight") * $("#imgTopImg").height()/1000;
            if ((Math.abs(firstLine.height()-correctHeight) > 2.5) || ($("#imgTopImg").height() > Page.height())) {
                // assures that the resizing of the img completely took place
                // FIXME may cause slow loop
                console.log("parsing adjustment ("+firstLine.height()+", "+correctHeight+")");
                Parsing.hideWorkspaceForParsing(); 
            } else {
                //hack to make the image draw correctly in some cases
                $("#imgTopImg").css('top','auto');
                $("#imgTopImg").css('top','0');
                console.log("image position confirmed");
                window.clearInterval(ParsingInterval);
            }
        },1200);
    },
    /**
     * Resets the buttons to move between lines attached to each transcriptlet.
     * Called on $.load and in cleanupTranscriptlets().
     * 'Next Page', 'Previous Page', and 'End of Page'
     *  
     *  @see cleanupTranscriptlets()
     */
    setLineNavBtns: function(){
        var theseLines = $(".addNotes").length;
        $(".previousLine,.nextLine").remove();
        $(".addNotes").each(function(index){
            var thisLine = $(this);
            if (index > 0) thisLine.after(lineBtnPrev);
            if (index < (theseLines-1)) thisLine.after(lineBtnNext);
            else thisLine.after(lineBtnLast);
        });
    },
    /**
     * Changes ruler color in parsing tool to custom value.
     */
    customRulerColor: function(){
        $('#customColor').val($('#customRuler').val()).attr('checked',true);
        selectRulerColor = $("#customRuler").val();
        $('#sampleRuler').stop(true,true).animate({backgroundColor:selectRulerColor}, 1000,function(){$(this).css('background-color',selectRulerColor);});
    },
    /**
     * Removes selected column and destroys the lines.
     * Used by parsing tool when Destroy Column(s) or Clear Page is clicked.
     * 
     * @column jQuery object, column to be removed
     */
    removeColumn: function(column){
        if(!isMember && !permitParsing)return false;
        if(column.attr("hastranscription")==="true"){
            var cfrm = confirm("This column contains transcription data that will be lost.\n\nContinue?");
            if (!cfrm) return false;
        }
        var colX = column.attr("lineleft");
        // collect lines from column
        var lines = $(".line[lineleft='"+colX+"']");
        lines.addClass("deletable");
        var linesSize = lines.size();
        // delete from the end, alerting for any deleted data
        for (var i=linesSize; i>0;i--){
            Parsing.updateLine(null, Parsing.removeLine(lines[i-1]));
        }
     },
    /**
     * Removes clicked line, merges if possible with the following line.
     * updateLine(e,additionalParameters) handles the original, resized line.
     * 
     * @param e clicked line element from lineChange(e) via saveNewLine(e)
     * @see lineChange(e)
     * @see saveNewLine(e)
     */
     removeLine: function(e){
         if(!isMember && !permitParsing)return false;
        $("#imageTip").hide();
        var removedLine = $(e);
        if ($(e).attr("lineleft") == $(e).next(".parsing").attr("lineleft")) {
            removedLine = $(e).next();
            var newLineHeight = parseInt(removedLine.attr("linetop"))
                + parseInt(removedLine.attr("lineheight"))
                - parseInt($(e).attr("linetop"));
            $(e).css({
                "height" :  Page.convertPercent(newLineHeight/1000,2)+"%",
                "top" :     $(e).css("top")+"%"
            }).addClass("newDiv").attr({
                "lineheight":   newLineHeight
            });
        } else if ($(e).hasClass("deletable") && $(".transcriptlet[data-lineid='"+$(e).attr("data-lineid")+"']").find(".theText").val().length > 0){
            var toDelete = $(".transcriptlet[data-lineid='"
                + $(e).attr("data-lineid")+"']").find(".theText").val().substr(0,15)
                + "\u2026";
            var cfrm = confirm("Removing this line will remove any data contained as well.\n'"+toDelete+"'\n\nContinue?");
            if(!cfrm)return false;
            isDestroyingLine = true;
        } 
        var params = new Array({name:"remove",value:removedLine.attr("data-lineid")});
        removedLine.remove(); 
        this.removeTranscriptlet(removedLine.attr("data-lineid"),$(e).attr("data-lineid"));
        return params;
    },
    /**
     * Tests isAddingLines to add or remove the line.
     * updateLine(e,additionalParameters) handles the original, resized line.
     * 
     * @param e clicked line element from lineChange(e)
     * @return jQuery serialized parameters to POST
     * @see updateLine(e)
     */
     saveNewLine: function(e){
         if(!isMember && !permitParsing)return false;
        var params = new Array();
        if(!isAddingLines){
            params = this.removeLine(e);
        } else {
            var newLine = $(".parsing[newline]");
            params.push({name:"newy",value:newLine.attr("linetop")},{name:"newx",value:newLine.attr("lineleft")},{name:"newwidth",value:newLine.attr("linewidth")},{name:"newheight",value:newLine.attr("lineheight")},{name:"new",value:true});
        }
        return params;
    },
    /**
     * Collects parameters and adds them to the parameters of the updated line.
     * POSTs to updateLinePositions servlet.
     * 
     * @param e line element to be updated
     * @param additionalParameters array to add or remove lines
     * @return new lineID (int data from POST) if new line is created
     */
     updateLine: function(e,additionalParameters){
         if(!isMember && !permitParsing)return false;
        $('#savedChanges').html('Saving . . .').stop(true,true).css({
            "opacity" : 0,
            "top"     : "35%"
        }).show().animate({
            "opacity" : 1,
            "top"     : "0%"
        },1000,"easeOutCirc");
        var lineID = (e != null) ? $(e).attr("data-lineid") : -1;
        var params = new Array({name:'submitted',value:true},{name:'folio',value:folio},{name:'projectID',value:projectID});
        if ((lineID>0 && (!isDestroyingLine)) || $(e).attr("id")=="dummy")params.push({name:"updatey",value:$(e).attr("linetop")},{name:"updatex",value:$(e).attr("lineleft")},{name:"updatewidth",value:$(e).attr("linewidth")},{name:"updateheight",value:$(e).attr("lineheight")},{name:"update",value:lineID});
        isDestroyingLine = false;
        if(additionalParameters != undefined){
            params = params.concat(additionalParameters);
        }
        $.post("updateLinePositions",$.param(params),function(data) {
                    $('#savedChanges')
                        .html('<span class="left ui-icon ui-icon-check"></span>Changes saved.')
                        .delay(3000)
                        .fadeOut(1500);
//                    isUnsaved = false;
                    if(data>0){
                        $("[newline]").attr("data-lineid",data).removeAttr("newline");
                        Parsing.buildTranscriptlet($(".parsing[data-lineid='"+data+"']"), lineID, data);
                    };
                    return data;
        },"html")
        .error(function(){
            $('#savedChanges')
                        .html('No changes made')
                        .addClass('ui-state-error');
                $('#savedChanges')
                    .stop(true,true)
                    .animate({color:'white',borderColor:'white',left: '+=15'},250,function(){
                        $(this).animate({
                            color:'#226683',
                            borderColor:'#69ACC9',
                            left:'0'},750)
                    });
        });
    },
    /**
     * Adds a line by splitting the current line where it was clicked.
     * 
     * @param e clicked line element
     * @see organizePage(e)
     */
     splitLine: function(e,event){
         if(!isMember && !permitParsing)return false;
        var oldDimensions = new Array();                        //top, height
        oldDimensions[0] = $(e).position().top;
        oldDimensions[1] = $(e).height();
        oldHeight        = parseInt($(e).attr("lineheight"));
        var newClick = event.pageY - e.offsetTop - $(event.target.parentElement).offset().top;               //accomodates browser inconsistencies
        //Insert new line and clip old one
        var newLine = $(e).clone(true);                         //clone(true) is a deep copy
        var scaledClick = newClick / ($("#imgTopImg").height() / 1000); 
        //reduce fraction math at the cost of some precision
        newLine.css({
            "height"    :   oldDimensions[1]-newClick,
            "top"       :   oldDimensions[0]+newClick
        }).attr({
            "newline"   :   true,
            "linetop"   :   Math.round(parseInt($(e).attr("linetop"))+scaledClick)
        });
        $(e).attr("lineheight",parseInt(newLine.attr("linetop"))-parseInt($(e).attr("linetop")));
        newLine.attr("lineheight",oldHeight-parseInt($(e).attr("lineheight")));
        $(e)
            .css("height",newClick)
            .after(newLine);
//        this.organizePage(e);
    },
    /**
     * Inserts new transcriptlet when line is added.
     * Cleans up inter-transcriptlet relationships afterwards.
     * 
     * @param e line element to build transcriptlet from
     * @param afterThisID lineid of line before new transcriptlet
     * @param newLineID lineid of new line
     */
     buildTranscriptlet: function(e, afterThisID, newLineID){
        var isNotColumn = true;
        var newW = e.attr("linewidth");
        var newX = e.attr("lineleft");
        var newY = e.attr("linetop");
        var newH = e.attr("lineheight");
        if (afterThisID == -1){
            afterThisID = $(".transcriptlet").eq(-1).attr("data-lineid");
            isNotColumn = false;
        } 
        var $afterThis = $(".transcriptlet[data-lineid='"+afterThisID+"']");
        var newTranscriptlet = [
            "<div class='transcriptlet transcriptletBefore' id='t",newLineID,
            "' data-lineid='",newLineID, // took out style DEBUG
            "'>\n",
            "<span class='counter wLeft ui-corner-all ui-state-active ui-button'>Inserted Line</span>\n",
            "<input class='lineWidth' type='hidden' value='",newW,"'/>\n",
            "<input class='lineHeight' type='hidden' value='",newH,"'/>\n",
            "<input class='lineLeft' type='hidden' value='",newX,"'/>\n",
            "<input class='lineTop' type='hidden' value='",newY,"'/>\n",
            "<span class='addNotes wRight ui-corner-all ui-state-default ui-button'><span class='ui-icon ui-icon-note right'></span>Add Notes</span>\n",
            "<textarea id='transcription",newLineID,"' class='ui-corner-all theText' onkeydown='return Interaction.keyhandler(event);' style='height:50px;'></textarea>\n",
            "<div class='xmlClosingTags' id='closeTags2'></div>\n",
            "<textarea id='notes",newLineID,"' class='ui-corner-all notes'' style='display:none;'></textarea>\n",
            "</div>"];
        if (isNotColumn){
            //update transcriptlet that was split
            $afterThis.after(newTranscriptlet.join("")).find(".lineHeight").val($(".parsing[data-lineid='"+afterThisID+"']").attr("lineheight"));                    
        } else {
            if (afterThisID === undefined) {
                $("#entry").append(newTranscriptlet.join(""));
            } else {
                $afterThis.after(newTranscriptlet.join(""));
            }
//            $("#t"+newLineID)
//                .find(".lineWidth").val($(".parsing[data-lineid='"+newLineID+"']").attr("linewidth")).end()
//                .find(".lineHeight").val($(".parsing[data-lineid='"+newLineID+"']").attr("lineheight"));
        }
        this.cleanupTranscriptlets();
    },
    /**
     * Removes transcriptlet when line is removed. Updates transcriplet
     * if line has been merged with previous.
     * 
     * @param lineid lineid to remove
     * @param updatedLineID lineid to be updated
     */
     removeTranscriptlet: function(lineid, updatedLineID){
         if(!isMember && !permitParsing)return false;
        //update remaining line, if needed
        if (lineid != updatedLineID){
            var updatedLine =   $(".parsing[data-lineid='"+updatedLineID+"']");
            var toUpdate =      $(".transcriptlet[data-lineid='"+updatedLineID+"']");
            var removedText =   $(".transcriptlet[data-lineid='"+lineid+"']").find(".theText").val();
            toUpdate.find(".theText").val(function(){
                var thisValue = $(this).val();
                if (removedText != undefined) thisValue += removedText;
                return thisValue;
            });
            toUpdate.find(".lineHeight").val(updatedLine.attr("lineheight"));
        }
        //remove transcriptlet
        $(".transcriptlet[data-lineid='"+lineid+"']").remove();
        Parsing.cleanupTranscriptlets();
    },
    /**
     * @deprecated Nobody cares about tabIndex anymore
     * 
     * Coordinates tabindex and link references for newly added transcriptlets.
     * 
     * @param e recently changed element
     * @see splitLine(e,event)
     * @see cleanupTranscriptlets()
     */
     organizePage: function(e){
        //reset tabindex if needed
        var correlatingTextbox = $(".transcriptlet[data-lineid='"+$(e).attr("data-lineid")+"']").find(".theText");
        if (correlatingTextbox.attr("tabindex")%10 > 8){
            $(".theText").each(function(index){
                this.tabIndex = 1000+index*10;
            });
        }
        currentFocus = e.id;
        this.cleanupTranscriptlets();
    },
    /**
     * Allows for column adjustment in the parsing interface.
     */
     adjustColumn: function(event){
         if(!isMember && !permitParsing)return false;
        //prep for column adjustment
        Parsing.linesToColumns();
        var thisColumnID = new Array(2);
        var thisColumn;
        var originalX = 1;
        var originalY = 1;
        var originalW = 1;
        var originalH = 1;
        var adjustment = "";
        if($(".parsingColumn").hasClass("ui-resizable")){
            $(".parsingColumn").has(".ui-resizable").resizable("enable");
        } else {
            $(".parsingColumn").resizable({
                handles     : "n,s,w,e",
                containment : 'parent',
                start       : function(event,ui){
//                    originalX = ui.originalPosition.left;
//                    originalY = ui.originalPosition.top;
//                    originalW = ui.originalSize.width;
//                    originalH = ui.originalSize.height;
//                    var newX = ui.position.left;
//                    var newY = ui.position.top;
//                    var newW = ui.size.width;
//                    var newH = ui.size.height;
                    $("#progress").html("Adjusting Columns - unsaved").fadeIn();
                    $("#columnResizing").show();
                    $("#sidebar").fadeIn();
                    thisColumn = $(".ui-resizable-resizing");
                    thisColumnID = [thisColumn.attr("startid"),thisColumn.attr("endid")];
                    adjustment = "new";
                },
                resize      : function(event,ui){
                    if(adjustment=="new"){
                        var originalX = ui.originalPosition.left;
                        var originalY = ui.originalPosition.top;
                        var originalW = ui.originalSize.width;
                        var originalH = ui.originalSize.height;
                        var newX = ui.position.left;
                        var newY = ui.position.top;
                        var newW = ui.size.width;
                        var newH = ui.size.height;
                        if (Math.abs(originalW-newW)>5) adjustment = "right";
                        if (Math.abs(originalH-newH)>5) adjustment = "bottom";
                        if (Math.abs(originalX-newX)>5) adjustment = "left";    // a left change would affect w and x, order matters
                        if (Math.abs(originalY-newY)>5) adjustment = "top";     // a top change would affect h and y, order matters
                        $("#progress").html("Adjusting "+adjustment+" - unsaved");
                    }
                },
                stop        : function(event,ui){
                    $("#progress").html("Column Resized - Saving...");
                    var parseRatio = $("#imgTopImg").height()/1000;
                    var originalX = ui.originalPosition.left;
                    var originalY = ui.originalPosition.top;
                    var originalW = ui.originalSize.width;
                    var originalH = ui.originalSize.height;
                    var newX = ui.position.left;
                    var newY = ui.position.top;
                    var newW = ui.size.width;
                    var newH = ui.size.height;
                    var oldHeight, oldTop, oldLeft, newWidth, newLeft;
                    if(adjustment=="top"){
                        //save a new height for the top line;
                        var startLine = $(".line[data-lineid='"+thisColumnID[0]+"']");
                        oldHeight = parseInt(startLine.attr("lineheight"));
                        oldTop = parseInt(startLine.attr("linetop"));
                        
                        startLine.attr({
                            "linetop"    : Math.round(newY/parseRatio),
                            "lineheight" : Math.round(oldTop+oldHeight-newY/parseRatio)
                        });
                        if (parseInt(startLine.attr("lineheight"))<0){
                            // top of the column is below the bottom of its top line
                            var newTopLine = startLine;
                            do {
                                newTopLine = startLine.next('.line');
                                Parsing.updateLine(null, Parsing.removeLine(startLine));
                                startLine = newTopLine;
                                oldHeight = parseInt(startLine.attr("lineheight"));
                                oldTop = parseInt(startLine.attr("linetop"));
                                startLine.attr({
                                    "linetop"    : Math.round(newY/parseRatio),
                                    "lineheight" : Math.round(oldTop+oldHeight-newY/parseRatio)
                                });
                            } while (startLine.attr("lineheight")<0);
                            Linebreak.saveWholePage();
                        };
                        $(".transcriptlet[data-lineid='"+thisColumnID[0]+"']")
//                            .addClass("isUnsaved")
                            .find(".lineTop").val(startLine.attr("linetop")).end()
                            .find(".lineHeight").val(startLine.attr("lineheight"));
//FIXME this does not actualy detect success
//                        if(Parsing.updateLine(startLine)=="success"){
//                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
//                        };
                        Parsing.updateLine(startLine);
                        $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                    } else if(adjustment=="bottom"){
                        
                        //save a new height for the bottom line
                        var endLine = $(".line[data-lineid='"+thisColumnID[1]+"']");
                        oldHeight = parseInt(endLine.attr("lineheight"));
                        oldTop = parseInt(endLine.attr("linetop"));
                        endLine.attr({
                            "lineheight" : Math.round((newH+originalY)/parseRatio-oldTop)
                        });
                        if (parseInt(endLine.attr("lineheight"))<0){
                            //the bottom line isnt large enough to account for the change, delete lines until we get to a  line that, wehn combined with the deleted lines
                            //can account for the requested change.
                            do {
                                oldHeight = parseInt(endLine.attr("lineheight"));
                                oldTop = parseInt(endLine.attr("linetop"));
                                var nextline = endLine.prev(".line");
                                Parsing.updateLine(null, Parsing.removeLine(endLine));
                                //adjustedOldTop=oldTop-parseInt(endLine.attr("lineheight"));
                                
                                nextline.attr({
                                    "lineheight" : Math.round((newH+originalY)/parseRatio-oldTop)
                                });
                                endLine=nextline;
                                //oldTop=adjustedOldTop;
                            } while (endLine.attr("lineheight")<1);
                            Linebreak.saveWholePage();
                        };
                        $(".transcriptlet[data-lineid='"+thisColumnID[1]+"']")
                            .find(".lineTop").val(endLine.attr("linetop")).end()
                            .find(".lineHeight").val(endLine.attr("lineheight"));
//FIXME this does not actualy detect success
//                        if(Parsing.updateLine(endLine)=="success"){
//                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
//                        };
                            Parsing.updateLine(endLine);
                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                    } else if(adjustment=="left"){
                        //save a new left,width for all these lines
                        var leftGuide = $(".line[data-lineid='"+thisColumnID[0]+"']");
                        oldLeft = parseInt(leftGuide.attr("lineleft"));
                        newWidth = Math.round(newW/parseRatio);
                        newLeft = Math.round(newX/parseRatio);
                        $(".line[lineleft='"+oldLeft+"']").each(function(){
                            $(this).attr({
                                "lineleft" : newLeft,
                                "linewidth": newWidth
                            });
                            $(".transcriptlet[data-lineid='"+$(this).attr("data-lineid")+"']")
                                .find(".lineLeft").val($(this).attr("lineleft")).end()
                                .find(".lineWidth").val($(this).attr("linewidth"));
                        });
                        $.post('batchUpdateLinePositions', {
                            "lineID"  : thisColumnID[0],
                            "newwidth": newWidth,
                            "newleft" : newLeft
                        }, function(){
                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                        },'html');
                    } else if (adjustment=="right"){
                        //save a new width for all these lines
                        var rightGuide = $(".line[data-lineid='"+thisColumnID[0]+"']");
                        oldLeft = parseInt(rightGuide.attr("lineleft"));
                        newWidth = Math.round(newW/parseRatio);
                        $(".line[lineleft='"+oldLeft+"']").each(function(){
                            $(this).attr({
                                "linewidth": newWidth
                            });
                            $(".transcriptlet[data-lineid='"+$(this).attr("data-lineid")+"']")
                                .find(".lineWidth").val($(this).attr("linewidth"));
                        });
                        $.post('batchUpdateLinePositions', {
                            "lineID"  : thisColumnID[0],
                            "newwidth": newWidth
                        }, function(){
                            $("#progress").html("Column Saved").delay(3000).fadeOut(1000);
                        },'html');
                    } else {
                        $("#progress").html("No changes made.").delay(3000).fadeOut(1000);
                    }
                    $("#lineResizing").delay(3000).fadeOut(1000);
                    adjustment = "";
                }
            });
        }
     },
    /**
     * Handles clicks in parsing tool.
     * 
     * @param e clicked parsing element
     * @see splitLine(e,event)
     * @see updateLine(e,additionalParameters)
     */
     lineChange: function(e,event){
        if(isAddingLines)Parsing.splitLine(e,event);
        this.updateLine(e,this.saveNewLine(e)); //auto-saving  
    },
    /**
     * Shows ruler within parsing tool. Called on mouseenter .parsing.
     */
     applyRuler: function(){
        if ($("#addLines").hasClass('ui-state-active')||$("#removeLines").hasClass('ui-state-active')){
            var sRCbkp = selectRulerColor; //select Ruler Color backup
            $("#imageTip").html("Add a Line");
            if(!isAddingLines){
                if ($(this).attr("lineleft") == $(this).next("div").attr("lineleft")) {
                    $(this).next("div").addClass('deletable');
                }
                $(this).addClass('deletable');
                if($(".deletable").size()>1){
                    $(".deletable").addClass("mergeable");
                    $("#imageTip").html("Merge Line");
                } else {
                    $("#imageTip").html("Delete Line");
                }
                sRCbkp = 'transparent';
            };
            $(this).css('cursor','crosshair').bind('mousemove', function(e){
                var myLeft = $(this).position().left;
                var myWidth = $(this).width();
                $('#imageTip').show().css({
                    left:e.pageX,
                    top:e.pageY+20
                });
                $('#ruler1').show().css({
                    left: myLeft,
                    top: e.pageY,
                    height:'1px',
                    width:e.pageX-myLeft-7,
                    background:sRCbkp
                });
                $('#ruler2').show().css({
                    left: e.pageX+7,
                    top: e.pageY,
                    width:myWidth+myLeft-e.pageX-7,
                    height:'1px',
                    background:sRCbkp
                });
            });
        }
    },
    /**
     * Hides ruler within parsing tool. Called on mouseleave .parsing.
     */
     removeRuler: function(){
        if(!isAddingLines){$(".deletable").removeClass('deletable mergeable');}
        $(document).unbind('mousemove');
        $('#imageTip, #ruler1, #ruler2').hide();
    },
    /**
     * Reassigns the interface data attached to each transcriptlet.
     * 
     * @see setLineNavBtns()
     */
     cleanupTranscriptlets: function(){
        var columnCtr = 0;
        var columnLineShift = 0;
        var lineCtr = 0;
        var oldLeft = -9999;
        var columnLeft;
        $(".transcriptlet").each(function(index){
            lineCtr++;
            columnLeft = parseInt($(this).find(".lineLeft").val(),10);
            if (columnLeft > oldLeft){
                columnCtr++;
                columnLineShift = lineCtr-1;
                oldLeft = columnLeft;
            }
            $(this).attr("id","t"+(index+1))
                .find(".theText").attr("id","transcription"+(index+1)).end()
                .find(".counter").text("Column:"+String.fromCharCode(64+columnCtr).toUpperCase()+" Line:"+(lineCtr-columnLineShift));  
        });
        this.setLineNavBtns();
        Preview.rebuild();
        // realign the focus with something in the DOM, if missing
        if (!focusItem[1].closest('body').length) focusItem[1] = $('#t1');
    },
    /** 
     * Restructures lines overlay as columns.
     * Used in parsing tool.
     */
     linesToColumns: function(){
        //update lines in case of changes
        $(".parsingColumn").remove();
        if ($(".transcriptlet").size() == 0) return false;
        Interaction.writeLines($("#imgTopImg"));
        //loop through lines to find column dimensions
        var columnParameters = new Array(); // x,y,w,h,startID,endID
        var i = 0;
        var colX,colY,colW,colH;
        var nextColumn = -1;
        do{
            //build column parameters
            var hasTranscription = false;
            var $first = $(".line").eq(nextColumn+1);
            colX = parseInt($first.attr("lineleft"));
            colY = parseInt($first.attr("linetop"));
            colW = parseInt($first.attr("linewidth"));
            var $last = $(".line[lineleft='"+colX+"']").eq(-1);
            nextColumn = $(".line").index($last);
            colH = parseInt($last.attr("linetop"))-colY+parseInt($last.attr("lineheight"));
            $(".line[lineleft='"+colX+"']").each(function(){
                if ($(".transcriptlet[data-lineid='"+$(this).attr('data-lineid')+"']").find(".theText").val().length > 0) {
                    hasTranscription = true;
                    return false; //break out of each() loop
                }
            });
            //add column to array
            columnParameters.push([colX,colY,colW,colH,$first.attr("data-lineid"),$last.attr("data-lineid"),hasTranscription]);
            i++;
        }while ($last.next().is(".line"));
        //build columns
        var columns = [];
        for (j = 0;j<i;j++){
            var parseImg = document.getElementById("imgTopImg");
            var originalX = parseImg.width/parseImg.height*1000;
            var scaledX = Page.convertPercent(columnParameters[j][0]/originalX,2);
            var scaledY = Page.convertPercent(columnParameters[j][1]/1000,2);
            var scaledW = Page.convertPercent(columnParameters[j][2]/originalX,2);
            var scaledH = Page.convertPercent(columnParameters[j][3]/1000,2);
            // recognize, alert, and adjust to out of bounds columns
            if (scaledX+scaledW > 100){
                // exceeded the right boundary of the image
                if (scaledX > 98){
                    scaledX = 98;
                    scaledW = 2;
                } else {
                    scaledW = 100-scaledX-1;
                };
            }
            if (scaledX < 0){
                // exceeded the left boundary of the image
                scaledW += scaledX;
                scaledX = 0;
            }
            if (scaledY+scaledH > 100){
                // exceeded the bottom boundary of the image
                if (scaledY > 98){
                    scaledY = 98;
                    scaledH = 2;
                } else {
                    scaledH = 100-scaledY-1;
                };
            }
            if (scaledY < 0){
                // exceeded the top boundary of the image
                scaledH += scaledY;
                scaledY = 0;
            }
            columns.push("<div class='parsingColumn' lineleft='",columnParameters[j][0],"'",
            " linetop='",columnParameters[j][1],"'",
            " linewidth='",columnParameters[j][2],"'",
            " lineheight='",columnParameters[j][3],"'",
            " startID='",columnParameters[j][4],"'",
            " endID='",columnParameters[j][5],"'",
            " hastranscription=",columnParameters[j][6]==true,
            " style='top:",scaledY,"%;left:",scaledX,"%;width:",scaledW,"%;height:",scaledH,"%;'>",
            "</div>");
        }
        //attach columns
        $(parseImg).before(columns.join(""));
    }
};
var Preview = {
    /**
     *  Syncs changes between the preview tool and the transcription area,
     *  if it is on the page. If it is the previous or following page, a button
     *  to save remotely is created and added.
     *  
     *  @param line jQuery object, line edited in the preview tool
     */
    edit: function(line){
        var focusLineID = $(line).siblings(".previewLineNumber").attr("data-lineid");
        var focusFolio = $(line).parent(".previewPage").attr("data-pagenumber");
        var transcriptionText = ($(line).hasClass("previewText")) ? ".theText" : ".notes";
        var pair = $(".transcriptlet[data-lineid='"+focusLineID+"']").find(transcriptionText);
        if ($(line).hasClass("currentPage")){
          if (pair.parent().attr('id') !== focusItem[1].attr('id')) Screen.updatePresentation(pair.parent());
                line.focus();
            $(line).keyup(function(){
                Data.makeUnsaved();
                pair.val($(this).text());
            });
        } else {
            var saveLine = $(line).parent(".previewLine");
            if (saveLine.find(".previewSave").size() == 0)
                saveLine.prepend("<div class='right ui-state-default ui-corner-all previewSave' onclick='Preview.remoteSave(this,"+focusLineID+","+focusFolio+");'>Save Changes</div>");
        }
    },
    /**
     *  Saves the changes made in the preview tool on the previous or following
     *  page. Overwrites anything currently saved.
     *  
     *  @param button element clicked (for removal after success)
     *  @param saveLineID int lineID of changed transcription object
     *  @param focusFolio int id of folio in which the line has been changed
     */
    remoteSave: function(button,saveLineID,focusFolio){
        if(!isMember && !permitModify)return false;
        var saveLine = $(".previewLineNumber[data-lineid='"+saveLineID+"']").parent(".previewLine");
        var saveText = saveLine.find(".previewText").text();
        var saveComment = saveLine.find(".previewNotes").text();
        Data.saveLine(saveText, saveComment, saveLineID, focusFolio);
        $(button).fadeOut();
    },
    /**
     *  Syncs the current line of transcription in the preview tool when changes
     *  are made in the main interface. Called on keyup in .theText and .notes.
     *  
     *  @param current element textarea in which change is made.
     */
     updateLine: function(current) {
            var lineid = $(current).parent(".transcriptlet").attr("data-lineid");
            var previewText = ($(current).hasClass("theText")) ? ".previewText" : ".previewNotes";
            $(".previewLineNumber[data-lineid='"+lineid+"']").siblings(previewText).html(Preview.scrub($(current).val()));
            Data.makeUnsaved();
     },
    /**
     *  Rebuilds every line of the preview when changed by parsing.
     *  
     */
    rebuild: function(){
        var allTrans = $(".transcriptlet");
        var columnValue = 65;
        var columnLineShift = 0;
        var oldLeftPreview = allTrans.eq(0).find(".lineLeft").val();
        // empty the current page
        var currentPreview = $("[data-pagenumber='"+folio+"']");
        currentPreview.find(".previewLine").remove();
        var newPage = new Array();
        allTrans.each(function(index){
            var columnLeft = $(this).find(".lineLeft").val();
            if (columnLeft > oldLeftPreview){
                columnValue++;
                columnLineShift = (index+1);
                oldLeftPreview = columnLeft;
            }
            newPage.push("<div class='previewLine' data-linenumber='",
                    (index+1),"'>",
                "<span class='previewLineNumber' data-lineid='",
                    $(this).attr("data-lineid"),"' data-linenumber='",
                    (index+1),"' data-linenumber='",
                    String.fromCharCode(columnValue),"' data-lineofcolumn='",
                    (index+1-columnLineShift),"'>",
                    String.fromCharCode(columnValue),(index+1-columnLineShift),
                    "</span>",
                "<span class='previewText currentPage' contenteditable=true>",
                    Preview.scrub($(this).find(".theText").val()),
                    "</span><span class='previewLinebreak'></span>",
                "<span class='previewNotes currentPage' contenteditable=true>",
                    Preview.scrub($(this).find(".notes").val()),"</span></div>");
        });
        currentPreview.find('.previewFolioNumber').after(newPage.join(""));
     },
    /**
     *  Cleans up the preview tool display.
     */
    format: function(){
        $(".previewText").each(function(){
            $(this).html(Preview.scrub($(this).text()));
        });       
    },
    /**
     *  Analyzes the text in the preview tool to highlight the tags detected.
     *  Returns html of this text to Preview.format()
     *  
     *  @param thisText String loaded in the current line of the preview tool
     */
    scrub: function(thisText){
        var workingText = $("<div/>").text(thisText).html();
        var encodedText = [workingText];
        if (workingText.indexOf("&gt;")>-1){
            var open = workingText.indexOf("&lt;");
            var beginTags = new Array();
            var endTags = new Array();
            var i = 0;
            while (open > -1){
                beginTags[i] = open;
                var close = workingText.indexOf("&gt;",beginTags[i]);
                if (close > -1){
                    endTags[i] = (close+4);
                } else {
                    beginTags[0] = null;
                    break;}
                open = workingText.indexOf("&lt;",endTags[i]);
                i++;
            }
            //use endTags because it might be 1 shorter than beginTags
            var oeLen = endTags.length; 
            encodedText = [workingText.substring(0, beginTags[0])];
            for (i=0;i<oeLen;i++){
                encodedText.push("<span class='previewTag'>",
                    workingText.substring(beginTags[i], endTags[i]),
                    "</span>");
                if (i!=oeLen-1){
                    encodedText.push(workingText.substring(endTags[i], beginTags[i+1]));
            }
            }
        if(oeLen>0)encodedText.push(workingText.substring(endTags[oeLen-1]));
        }
        return encodedText.join("");
    },
    /**
     *  Animates scrolling to the current page (middle of 3 shown)
     *  in the Preview Tool. Also restores the intention of the selected options.
     */
    scrollToCurrentPage: function(){
        var pageOffset = $(".previewPage").filter(":first").height() + 20;
        $("#previewDiv").animate({
            scrollTop:pageOffset
        },500);
        $("#previewNotes").filter(".ui-state-active").each( // pulled out #previewAnnotations
            function(){
                if ($("."+this.id).is(":hidden")){
                    $("."+this.id).show();
                    Preview.scrollToCurrentPage();
                }
            });
    }
}
// old news. replaced with SCIAT
//var Annotation = {
//    /** 
//     *  Save annotations made within the tool.
//     *  
//     *  @param x int left position of annotation
//     *  @param y int top position of annotation
//     *  @param h int height of annotation
//     *  @param w int width of annotation
//     *  @param text String attached plaintext data
//     *  @param folio int unique id of folio
//     *  @param projectID int unique id of project
//     */
//    save: function(x,y,h,w,text,folio,projectID){
//        if(!isMember && !permitAnnotation)return false;
//        var params = new Array({name:"create",value:true});
//        if (folio != undefined) params.push({name:"folio",value:folio});
//        if (projectID != undefined) params.push({name:"projectID",value:projectID});
//        if (text != undefined) params.push({name:"text",value:text});
//        try {
//            params.push({name:"x",value:x},{name:"y",value:y},{name:"h",value:h},{name:"w",value:w});
//        } catch (err){
//            alert ("Failed to save: missing location value");
//            return false;
//        }
//        $.post('annotation', $.param(params), function(data){
//            $('[data-id]="new"').eq(0).removeClass('pendingSave').attr('data-id',data);
//        }, "html");
//    },
//    /**
//     *  Auto-saves annotations when the tool is closed or the page is exited.
//     */
//    autoSave: function(){
//        if(!isMember && !permitAnnotation)return false;
//        var $unsaved = $(".pendingSave");
//        $unsaved.each(function(){
//            var $this = $(this);
//            var a = {
//                x           : $this.attr("data-x"),
//                y           : $this.attr("data-y"),
//                h           : $this.attr("data-height"),
//                w           : $this.attr("data-width"),
//                text        : $this.attr("title"),
//                annotationID: $this.attr("data-id")
//            };
//            if (a.annotationID == "new"){
//                // new annotation
//                Annotation.save(a.x, a.y, a.h, a.w, a.text, folio, projectID);
//            } else {
//                // update annotation
//                Annotation.update(a.x, a.y, a.h, a.w, a.text, folio, projectID, a.annotationID);
//            }
//        });
//    },
//    /**
//     *  Deletes annotation from interface and database.
//     *  
//     *  @param annotationID int unique id of annotation to be removed
//     */
//    remove: function(annotationID){
//        if(!isMember && !permitAnnotation)return false;
//        var params = new Array({name:"id",value:annotationID},{name:"delete",value:true});
//        $.post('annotation', $.param(params), function(data){
//            //deleted
//        }, "html");      
//    },
//    /**
//     *  Updates an existing annotation.
//     *  
//     *  @param x int left position of annotation
//     *  @param y int top position of annotation
//     *  @param h int height of annotation
//     *  @param w int width of annotation
//     *  @param text String attached plaintext data
//     *  @param folio int unique id of folio
//     *  @param projectID int unique id of project
//     *  @param annotationID int uniqueid of updated annotation
//     */
//    update: function(x,y,h,w,text,folio,projectID,annotationID){
//        if(!isMember && !permitAnnotation)return false;
//        var params = new Array({name:"update",value:true});
//        if (folio != undefined) params.push({name:"folio",value:folio});
//        if (projectID != undefined) params.push({name:"projectID",value:projectID});
//        if (text != undefined) params.push({name:"text",value:text});
//        try {
//            params.push({name:"id",value:annotationID},{name:"x",value:x},{name:"y",value:y},{name:"h",value:h},{name:"w",value:w});
//        } catch (err){
//            alert ("Failed to save: missing location value");
//            return false;
//        }
//        $.post('annotation', $.param(params), function(data){
//            $('[data-id]="'+annotationID+'"').removeClass('pendingSave');
//        }, "html");
//    },
//    /**
//     *  Handles changes to the options in the annotation tool.
//     *  
//     *  @param elem element clicked from .toolLinks
//     */
//    options: function(elem){
//        if(!isMember && !permitAnnotation)return false;
//        if ($(elem).hasClass("ui-state-active") && $(elem).is("#highlightAnnotation")){
//            $("#defaultInst").show().siblings().hide();
//            $(elem).removeClass("ui-state-active");
//            $(".annotation").removeClass("newAnno adjustAnno deleteAnno showAnno");
//            $("#defaultInst").show().siblings().hide();
//        } else {
//            $(elem).addClass("ui-state-active").siblings().removeClass("ui-state-active");
//            $(".annotation").removeClass("newAnno adjustAnno deleteAnno showAnno").draggable('destroy').resizable('destroy');
//            $("#"+$(elem).attr('id')+"Inst").show().siblings().hide();
//            switch ($(elem).index()){
//                case 0:     //#highlightAnnotation
//                    $(".annotation").addClass('showAnno');
//                    break;
//                case 1:     //#addAnnotation
//                    var newAnnotation = $("<div/>");
//                    newAnnotation.addClass("annotation newAnno").css({
//                        'width'     : "150px",
//                        'height'    : "150px",
//                        'top'       : $("#annotations").height()/2-75+"px",
//                        'left'      : $("#annotations").width()/2-75+"px"
//                    }).attr('title','').appendTo("#annotations");
//                    $('.newAnno').draggable({
//                        stop: function(event,ui){Annotation.adjust($(this),ui)},
//                        containment: $("#annotations")
//                    }).resizable({
//                        stop: function(event,ui){Annotation.adjust($(this),ui)},
//                        containment: $("#annotations"),
//                        minHeight: 5,
//                        minWidth: 5,
//                        handles: 'all'
//                    });
//                    break;
//            }
//        }
//    },
//    /**
//     *  Create and display annotations within the annotation tool.
//     */
//    display: function() {
//        var allAnnos = $(".annotation");
//        var ratio = $("#annotationDiv").height()/1000;
//        allAnnos.each(function(){
//            var a = $(this);
//            a.css({
//                'width'  :    a.attr('data-width')*ratio+"px",
//                'height' :    a.attr('data-height')*ratio+"px",
//                'left'   :    a.attr('data-x')*ratio+"px",
//                'top'    :    a.attr('data-y')*ratio+"px",
//                'display':'block'
//            });
//        });
//        $("#defaultInst").show().siblings().hide();
//    },
//    /**
//     *  Updates the annotation attributes in preparation for saving.
//     *  
//     *  @param $anno jQuery object, annotation updated
//     *  @param ui object from jQuery resizable
//     */
//    adjust: function($anno,ui){
//        if(!isMember && !permitAnnotation)return false;
//        if ($anno.attr('data-id')==null) $anno.attr('data-id','new');
//        var ratio = $("#annotations").height()/1000;
//        if (ui.size != null){
//             $anno.attr({
//                'data-width'    : parseInt(ui.size.width/ratio),
//                'data-height'   : parseInt(ui.size.height/ratio)
//            });
//        }
//        $anno.attr({
//            'data-x'        : parseInt(ui.position.left/ratio),
//            'data-y'        : parseInt(ui.position.top/ratio)
//        })
//        .addClass('pendingSave');
//    },
//    /**
//     *  Reveals the text and link of an annotation when hovering over the region.
//     *  
//     *  @param $a jQuery object, annotation
//     */
//    showText: function($a){
//        $a.addClass('activeAnnotation');
//        var annoText = $a.attr('title');
//        var annoLink = $a.attr('linked');
//        $("#annotationText").val(annoText);
//        $("#annotationLink").val(annoLink);
//    },
//    /**
//     *  Show link results
//     *  
//     */
//    showLink: function(){
//        if ($("#aLinkFrame").is(":visible")){
//            $("#aLinkFrame").hide("fade",250, function(){$(this).remove();});
//            $("#aShowLink").text("Preview Link");
//        } else {
//            var urlLink = $("#annotationLink").val();
//            var iframe = $("<iframe id='aLinkFrame' src='"+urlLink+"'></iframe>");
//            $("#annotations").append(iframe);
//            $("#aShowLink").text("Hide Preview");
//        }
//    },
//    /**
//     *
//     */
//    validateLink: function(){
//        var thisLink = $("#annotationLink");
//        var urlLink = thisLink.val();
//        var valid = /^(http|https|ftp)\:\/\/([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(\/($|[a-zA-Z0-9\.\,\?\'\\\+&amp;%\$#\=~_\-]+))*$/;
//        if (!valid.test(urlLink)){
//            thisLink.addClass("restricted").attr("title","This may not be a valid link");
//        } else {
//            thisLink.removeClass("restricted").attr("title","");
//        }
//    },
//    /**
//     *  Update links in annotation
//     *  
//     *  @param a element, annotation
//     */
//    updateLink: function(){
//        if(!isMember && !permitAnnotation)return false;
//        Annotation.validateLink();
//        $('.activeAnnotation').attr('linked',$("#annotationLink").val()).addClass('pendingSave');
//    },
//    /**
//     *  Update text of annotation
//     *  
//     *  @param a element, annotation
//     */
//    updateText: function(){
//        if(!isMember && !permitAnnotation)return false;
//        $('.activeAnnotation').attr('title',$("#annotationText").val()).addClass('pendingSave');
//    }
//};
var Help = {
    /**
     *  Shows the help interface.
     */
    revealHelp: function(){
        imgTopHeight = Page.height()-workspaceHeight();
        var wrapWidth = $("#wrapper").width();
        Screen.maintainWorkspace();
        $(".helpPanel").height(imgTopHeight)
            .width('20%');
        $("#helpPanels").width('500%');
        $("#help").show().css({
            "left":"0%",
            "width":'100%'
        });
        $(".helpContents").eq(0).click();
        $("#bookmark").hide();
        $("#closeHelp").show();
    },
    /**
     *  Adjusts the position of the help panels to reveal the selected section.
     */
    select  : function(contentSelect){
          $(contentSelect).addClass("helpActive").siblings().removeClass("helpActive");
          $("#helpPanels").css("margin-left",-$("#wrapper").width()*contentSelect.index()+"px");
    },
    /**
     *  Shows specific page element through overlay and zooms in. If the element
     *  is not displayed on screen, an alternative message is shown.
     *  
     *  @param refIndex int index of help button clicked
     */
    lightUp: function(refIndex){
        switch (refIndex){
            case 0  :   //Previous Line
                this.highlight(focusItem[1].find(".previousLine"));
                break;
            case 1  :   //Next Line
                this.highlight(focusItem[1].find(".nextLine"));
                break;
            case 2  :   //Line Indicator
                this.highlight(focusItem[1].find(".counter"));
                break;
            case 3  :   //View Full Page
            case 7  :
                this.highlight($("#imageBtn"));
                break;
            case 4  :   //Preview Tool
            case 15 :
                this.highlight($("#previewBtn"));
                break;
            case 5  :   //Special Characters
                this.highlight($("#charactersPopin"));
                break;
            case 6  :   //XML Tags
                this.highlight($("#xmlTagPopin"));
                break;
            case 8  :   //Magnify Tool
                this.highlight($("#magnify1"));
                break;
            case 9  :   //History
                this.highlight($("#historyBtn"));
                break;
            case 10 :   //Abbreviations
                this.highlight($("#abbrevBtn"));
                break;
            case 11 :   //Compare Pages
                this.highlight($("#compareBtn"));
                break;
            case 12 :   //Magnify Tool
                this.highlight($("#magnify2"));
                break;
            case 13 :   //Linebreaking
                this.highlight($("#linebreakBtn"));
                break;
            case 14 :   //Correct Parsing
                this.highlight($("#parsingBtn"));
                break;
            case 16 :   //Location Flag
            case 17 :
                this.highlight($("#location"));
                break;
            case 18 :   //Previous Page
                this.highlight($("#prevPage"));
                break;
            case 19 :   //Next Page
                this.highlight($("#nextPage"));
                break;
            default :
                console.log("No element located for "+refIndex);
        }
    },
    /**
     *  Redraws the element on top of the overlay.
     *  
     *  @param $element jQuery object to redraw
     */
    highlight: function($element){
        if ($element.length == 0) $element = $("<div/>");
        var look = $element.clone().attr('id','highlight');
        var position = $element.offset();
        $("#overlay").show().after(look);
        if ((position == null) || (position.top < 1)){
            position = {left:(Page.width()-260)/2,top:(Page.height()-46)/2};
            look.prepend("<div id='offscreen' class='ui-corner-all ui-state-error'>This element is not currently displayed.</div>")
            .css({
                "left"  : position.left,
                "top"   : position.top
            }).delay(2000).show("fade",0,function(){
                $(this).remove();
                $("#overlay").hide("fade",2000);
            });
        } else {
            $("#highlight").css({
                "box-shadow":"0 0 5px 3px whitesmoke",
                "left"  : position.left,
                "top"   : position.top
            }).show("scale",{
                percent:150,
                direction:'both',
                easing:"easeOutExpo"},
            2000,function(){
                $(this).remove();
                $("#overlay").hide("fade",1000);
            });
        }
    },
    /**
     *  Help function to call up video, if available.
     *  
     *  @param refIndex int index of help button clicked
     */
    video: function(refIndex){
        var vidLink ='';
        switch (refIndex){
            case 0  :   //Previous Line
            case 1  :   //Next Line
                vidLink = 'http://www.youtube.com/embed/gcDOP5XfiwM';
                break;
            case 2  :   //Line Indicator
                vidLink = 'http://www.youtube.com/embed/rIfF9ksffnU';
                break;
            case 3  :   //View Full Page
            case 7  :
                vidLink = 'http://www.youtube.com/embed/6X-KlLpF6RQ';
                break;
            case 4  :   //Preview Tool
            case 15 :
                vidLink = 'http://www.youtube.com/embed/dxS-BF3PJ_0';
                break;
            case 5  :   //Special Characters
                vidLink = 'http://www.youtube.com/embed/EJL_GRA-grA';
                break;
            case 6  :   //XML Tags
                vidLink = '';
                break;
            case 8  :   //Magnify Tool
                vidLink = '';
                break;
            case 9  :   //History
                vidLink = '';
                break;
            case 10 :   //Abbreviations
                vidLink = '';
                break;
            case 11 :   //Compare Pages
                vidLink = '';
                break;
            case 12 :   //Magnify Tool
                vidLink = '';
                break;
            case 13 :   //Linebreaking
                vidLink = '';
                break;
            case 14 :   //Correct Parsing
                vidLink = '';
                break;
            case 16 :   //Location Flag
                vidLink = 'http://www.youtube.com/embed/8D3drB9MTA8';
                break;
            case 17 :   //Jump to Page
                vidLink = 'http://www.youtube.com/embed/mv_W_3N_Sbo';
                break;
            case 18 :   //Previous Page
                vidLink = '';
                break;
            case 19 :   //Next Page
                vidLink = '';
                break;
            default :
                console.log("No element located for "+refIndex);
        }
        var videoview = $("<iframe id=videoView class=popover allowfullscreen src="+vidLink+" />");
        if (vidLink.length>0){
            $('#overlay').show().after(videoview);
        } else {
            $(".video[ref='"+refIndex+"']").addClass('ui-state-disabled').text('unavailable');
        }
    }
}
var History = {
    /**
     *  Displays the image of the line in the history tool.
     *  
     *  @param x int left position of history entry
     *  @param y int top position of the history entry
     *  @param h height of the history entry
     *  @param w width of the history entry
     */
    showImage: function(x,y,h,w){
        var buffer = 30; //even is better
        var hView = $("#historyViewer");
        var hImg = hView.find("img");
        var hBookmark = $("#historyBookmark");
        var historyRatio = hImg.height()/1000;
        hImg.css({
            "top" :-y *historyRatio +buffer/2 +"px"
        });
        hView.css({
            "height" :h *historyRatio +buffer +"px"
        });
        hBookmark.css({
            "top"   : buffer/2 +"px",
            "left"  :x *historyRatio +"px",
            "width" :w *historyRatio +"px",
            "height":h *historyRatio +"px"
        });
        // size listings for balance of page for scrolling
        $("#historyListing").height(Page.height()-hView.height()-22); 
    },
    /**
     *  Updates the display when hovering over an entry in the history tool.
     *  
     *  @param lineid int id of the targeted line
     */
    showLine: function(lineid){
        $("#historyBookmark").empty();
        $("#history"+lineid).show(0,function(){
            // persist history options
            $("#historySplit .ui-state-active").each(function(){
                $(this).removeClass("ui-state-active").click();
            });            
        }).siblings(".historyLine").hide();
        var refLine = $(".transcriptlet[data-lineid='"+lineid+"']");
        History.showImage(parseInt(refLine.find(".lineLeft").val()),
            parseInt(refLine.find(".lineTop").val()),
            parseInt(refLine.find(".lineHeight").val()),
            parseInt(refLine.find(".lineWidth").val()));
    },
    /**
     *  Replaces empty entries with an indicator.
     */
    scrubHistory: function(){
        $(".historyText,.historyNote").html(function(){
            if($(this).html().length === 0){
                return "<span style='color:silver;'> - empty - </span>";
            } else {
                return $(this).html();
            }
        })
    },
    /**
     *  Shows the changes to parsing when hovering over a history entry.
     */
    adjustBookmark: function(archive){
        var buffer = 30; //even is better
        var hView = $("#historyViewer");
        var hImg = hView.find("img");
        var historyRatio = hImg.height()/1000;
        var historyDims = [];
        var archiveDims = {
            x:  parseInt(archive.attr("lineleft")),
            y:  parseInt(archive.attr("linetop")),
            w:  parseInt(archive.attr("linewidth")),
            h:  parseInt(archive.attr("lineheight"))
        }
        
        var dims = {
            x:  parseInt(focusItem[1].find(".lineLeft").val()),
            y:  parseInt(focusItem[1].find(".lineTop").val()),
            w:  parseInt(focusItem[1].find(".lineWidth").val()),
            h:  parseInt(focusItem[1].find(".lineHeight").val())
        }
        var delta = {
            x:  archiveDims.x - dims.x,
            y:  archiveDims.y - dims.y,
            w:  archiveDims.w - dims.w,
            h:  archiveDims.h - dims.h
        };
//compare and build new dimensions for box
        if (Math.abs(delta.x < 1)) delta.x = 0;
        if (Math.abs(delta.y < 1)) delta.y = 0;
        $("#historyBookmark").css("border","solid thin transparent").clone().appendTo("#historyBookmark");
        $("#historyBookmark").children().css({
            "top"   : delta.y *historyRatio +"px",
            "left"  : delta.x *historyRatio +"px",
            "width" : archiveDims.w *historyRatio +"px",
            "height": archiveDims.h *historyRatio +"px",
            "box-shadow": "0 0 0 transparent",
            "border": "solid thin red"
        });
        if (Math.abs(delta.x) > 1) historyDims.push("left: ",  Math.round(archiveDims.x/dims.x*100),"%");
        if (Math.abs(delta.y) > 1) historyDims.push("top: ",   Math.round(archiveDims.y/dims.y*100),"%");
        if (Math.abs(delta.w) > 1) historyDims.push("width: ", Math.round(archiveDims.w/dims.w*100),"%");
        if (Math.abs(delta.h) > 1) historyDims.push("height: ",Math.round(archiveDims.h/dims.h*100),"%");
        archive.find(".historyDims").html(historyDims.join(" "));
    },
    /**
     *  Show only the text changes in the history tool.
     *  
     *  @param button jQuery object, clicked button
     */
    textOnly: function(button){
        //toggle
        if (button.hasClass("ui-state-active")){
            button.removeClass("ui-state-active");
            $(".historyEntry").slideDown();
        } else {
            button.addClass("ui-state-active");
            $(".historyText").each(function(index){
                var collection = $(".historyText");
                var thisOne = $(this).html();
                var previousOne = (index>0) ? collection.eq(index-1).html() : "";
                if((thisOne === previousOne) || (thisOne.length === 0)){
                    // hide duplicate or empty fields
                    $(this).parent(".historyEntry").slideUp();
                }
            });
        }
        return false;
    },
    /**
     *  Show only parsing changes in the history tool.
     *  
     *  @param button jQuery object, clicked button
     */
    parsingOnly: function(button) {
        //toggle
        if (button.hasClass("ui-state-active")){
            button.removeClass("ui-state-active");
            $(".historyEntry").slideDown();
        } else {
            button.addClass("ui-state-active");
            $(".historyEntry").each(function(index){
                var collection = $(".historyEntry");
                var thisOne = [$(this).attr("linewidth"),$(this).attr("lineheight"),$(this).attr("lineleft"),$(this).attr("linetop")];
                var previousOne = (index>0) ? [collection.eq(index-1).attr("linewidth"),collection.eq(index-1).attr("lineheight"),collection.eq(index-1).attr("lineleft"),collection.eq(index-1).attr("linetop")] : ["0","0","0","0"];
                if(thisOne.join("|")==previousOne.join("|")){
                    // hide duplicate or empty fields
                    $(this).slideUp();
                }
            });
        }
        return false;
    },
    /**
     *  Show notes as well in the history tool.
     *  
     *  @param button jQuery object, clicked button
     */
    showNotes: function(button) {
        //toggle
        if (button.hasClass("ui-state-active")){
            button.removeClass("ui-state-active").html("Show Notes");
            $(".historyNote").slideUp();
        } else {
            button.addClass("ui-state-active").html("Hide Notes");
            $(".historyNote").slideDown();
        }
        return false;
    },
    /**
     *  Revert only the text value from the history entry.
     *  
     *  @param entry jQuery object, clicked history entry
     */
    revertText: function(entry){
        if(!isMember && !permitModify)return false;
        var historyText = entry.find(".historyText").text();
        var historyNotes = entry.find(".historyNote").text();
        if (historyText.indexOf("- empty -") !== -1) historyText = "";
        if (historyNotes.indexOf("- empty -") !== -1) historyNotes = "";
        var lineid = entry.parent(".historyLine").attr("id").substr(7);
        var pair = $(".transcriptlet[data-lineid='"+lineid+"']");
        pair.addClass("isUnsaved").find(".theText").val(historyText);
        pair.find(".notes").val(historyNotes);
        Preview.updateLine(pair.find(".theText")[0]);
        Preview.updateLine(pair.find(".notes")[0]);
    },
    /**
     *  Revert only the parsing value from the history entry.
     *  
     *  @param entry jQuery object, clicked history entry
     */
    revertParsing: function(entry){
        if(!isMember && !permitParsing)return false;
        var lineid = entry.parent(".historyLine").attr("id").substr(7);
        var dims = {
            width   : parseInt(entry.attr("linewidth")),
            height   : parseInt(entry.attr("lineheight")),
            left   : parseInt(entry.attr("lineleft")),
            top   : parseInt(entry.attr("linetop"))
        }
        var thisTranscriptlet = $(".transcriptlet[data-lineid='"+lineid+"']");
        var dummyBuild = ["<div id='dummy' style='display:none'",
        " data-lineid='",lineid,     "'",
        " lineleft='",   dims.left,  "'",
        " linetop='",    dims.top,   "'",
        " linewidth='",  dims.width, "'",
        " lineheight='", dims.height,"'",
        "></div>"];
        var dummy = dummyBuild.join("");
        if (thisTranscriptlet.find(".lineLeft").val() !== $(dummy).attr("lineleft")){
            //moved out of column
            var cfrm = confirm("This revision will remove this line from the column it is in.\n\nConfirm or click 'Cancel' to leave the column intact and adjust other dimensions only.");
            if (cfrm) thisTranscriptlet.find(".lineLeft").val($(dummy).attr("lineleft")).end();
        thisTranscriptlet
            .find(".lineTop").val($(dummy).attr("linetop")).end()
            .find(".lineWidth").val($(dummy).attr("linewidth")).end()
            .find(".lineHeight").val($(dummy).attr("lineheight"));
        } else {
        thisTranscriptlet
            .find(".lineTop").val($(dummy).attr("linetop")).end()
            .find(".lineWidth").val($(dummy).attr("linewidth")).end()
            .find(".lineHeight").val($(dummy).attr("lineheight"));
        }
        Parsing.updateLine(dummy);
    },
    /**
     *  Reverts to values from the history entry.
     *  
     *  @param entry jQuery object, clicked history entry
     */
    revertAll: function(entry){
        this.revertText(entry);
        this.revertParsing(entry);
    },
    /**
     *  Revert only the text value from the history entry.
     *  
     *  @param h element, clicked history tool
     */
    revert: function(h){
        if(!isMember && !permitModify)return false;
        var entry = $(h).parents(".historyEntry");
        // decide which was clicked
        if($(h).hasClass("ui-icon-arrowreturnthick-1-n")){
            // revert all
            History.revertAll(entry);
        } else if($(h).hasClass("ui-icon-pencil")){
            // revert text
            History.revertText(entry);
        } else if($(h).hasClass("ui-icon-image")){
            // revert parsing
            History.revertParsing(entry);
        } else {
            // bad click capture
            console.log(h);
        }
    },
    /**
     *  Adds a history entry to the top of the tool when a line is changed.
     *  
     *  @param lineid int unique id to attach to aded entry
     */
    prependEntry: function(lineid){
        var updated = $(".transcriptlet[data-lineid='"+lineid+"']");
        var firstEntry = $("#history"+lineid).find(".historyEntry").eq(0);
        var newEntry = null;
        if (firstEntry.length < 1){
            // No previous versions, add a new entry entirely
            var firstEntry = ["<div id='newEntry' class='historyEntry ui-corner-all' linewidth='' lineheight='' lineleft='' linetop=''>",
                "<div class='historyDate'></div><div class='historyCreator'></div>",
                "<div class='right historyRevert'></div><div class='right loud historyDims'></div>",
                "<div class='historyText'></div><div class='historyNote'></div>",
                "<div class='historyOptions' style='display: none;'>",
                    "<span title='Revert image parsing only' class='ui-icon ui-icon-image right'></span>",
                    "<span title='Revert text only' class='ui-icon ui-icon-pencil right'></span>",
                    "<span title='Revert to this version' class='ui-icon ui-icon-arrowreturnthick-1-n right'></span>",
                "</div></div>"].join("");
            $("#history"+lineid).html(firstEntry);
            newEntry = $("#newEntry");
            newEntry.attr("id", "");
        } else {
            newEntry = firstEntry.clone();
        }
        newEntry.attr({
            "linewidth" : updated.find(".lineWidth").val(),
            "lineheight" : updated.find(".lineHeight").val(),
            "lineleft" : updated.find(".lineLeft").val(),
            "linetop" : updated.find(".lineTop").val()
        }).find(".historyDate").html("<span class='quiet' title='History will update when the page reloads'>new entry</span>")
        .siblings(".historyCreator").html("<span class='quiet' title='History will update when the page reloads'>Local User</span>")
        .siblings(".historyText").text(updated.find(".theText").val())
        .siblings(".historyNote").text(updated.find(".notes").val())
        .siblings(".historyOptions").find("span").click(function(){History.revert(this)});
        newEntry.insertBefore(firstEntry);
    },
    /**
     *  Attaches the credit for the most recent edit to the main interface.
     */
    contribution: function(){
        $("#contribution").html($("#history"+focusItem[1].attr('data-lineid')).find('.historyCreator').eq(0).text());
        if ($("#contribution").html().length == 0){
            $("#contribution").hide();
        } else {
            $("#contribution").show();
        }
    }
}
var Linebreak = {
    /**
     * Inserts uploaded linebreaking text into the active textarea.
     * Clears all textareas following location of inserted text.
     * Used within T&#8209;PEN linebreaking tool.
     */
    useText: function(){
        if(!isMember && !permitModify)return false;
        //Load all text into the focused on line and clear it from all others
        var cfrm = confirm("This will insert the text at the current location and clear all the following lines for linebreaking.\n\nOkay to continue?");
        if (cfrm){
// FIXME           $("#"+currentFocus).val($("<div/>").html(leftovers).text()).focus()
            focusItem[1].find(".theText").val($("<div/>").html(leftovers).text()).focus()
            .parent().addClass("isUnsaved")
            .nextAll(".transcriptlet").addClass("isUnsaved")
                .find(".theText").html("");
            Data.saveTranscription();
            Preview.updateLine(focusItem[1].find(".theText")[0]);
        }
    },
    /**
     * Inserts uploaded linebreaking text beginning in the active textarea.
     * Automatically breaks at each occurance of linebreakString.
     * Used within T&#8209;PEN linebreaking tool.
    */
    useLinebreakText: function(){
        if(!isMember && !permitModify)return false;
        var cfrm = confirm("This will insert the text at the current location and replace all the following lines automatically.\n\nOkay to continue?");
        if (cfrm){
            $("#linebreakStringBtn").click();
            var bTlength = brokenText.length;
            var thoseFollowing = focusItem[1].nextAll(".transcriptlet").find(".theText");
            focusItem[1].find('.theText').add(thoseFollowing).each(function(index){
                if(index < bTlength){
                    if (index < bTlength-1 ) brokenText[index] += linebreakString;
                    $(this).val(unescape(brokenText[index])).parent(".transcriptlet").addClass("isUnsaved");
                    Preview.updateLine(this);
                    if (index == thoseFollowing.length) {
                        leftovers = brokenText.slice(index+1).join(linebreakString);
                        $("#lbText").text(unescape(leftovers));
                    }
                }
            });
            Data.saveTranscription();
        }
    },
    /**
     * Saves all textarea values on the entire page.
     *  
     * @see Data.saveTranscription()
     */
    saveWholePage: function(){
        if(!isMember && !permitModify && !permitNotes)return false;
        $(".transcriptlet").addClass(".isUnsaved");
        Data.saveTranscription();
    },
    /** 
     * Records remaining linebreaking text for later use.
     * POSTs to updateRemainingText servlet.
     *  
     * @param leftovers text to record
     */
    saveLeftovers: function(leftovers){
        if(!isMember && !permitModify)return false;
        $('#savedChanges').html('Saving . . .').stop(true,true).css({
            "opacity" : 0,
            "top"     : "35%"
        }).show().animate({
            "opacity" : 1,
            "top"     : "0%"
        },1000,"easeOutCirc");
        $.post("updateRemainingText", {
            transcriptionleftovers  : unescape(leftovers),
            projectID               : projectID
        }, function(data){
            if(data=="success!"){
                $('#savedChanges')
                .html('<span class="left ui-icon ui-icon-check"></span>Linebreak text updated.')
                .delay(3000)
                .fadeOut(1500);
            } else {
                //successful POST, but not an appropriate response
                $('#savedChanges').html('<span class="left ui-icon ui-icon-alert"></span>Failed to save linebreak text.');
                alert("There was a problem saving your linebreaking progress, please check your work before continuing.");
            }
        }, 'html');
    },
    /**
     * Moves all text after the cursor to the following transcription textarea.
     * Asks to save value as linebreak remaining text if on the last line.
     * 
     * @return false to prevent Interaction.keyhandler() from propogating 
     */
    moveTextToNextBox: function() {
        if(!isMember && !permitModify)return false;
        var myfield = focusItem[1].find(".theText")[0];
        focusItem[1].addClass("isUnsaved");       
        //IE support
        if (document.selection) {
            //FIXME this is not actual IE support
            myfield.focus();
            sel = document.selection.createRange();
        }
        //MOZILLA/NETSCAPE support
        else if (myfield.selectionStart || myfield.selectionStart == '0') {
            var startPos = myfield.selectionStart;
            if(focusItem[1].find(".nextLine").hasClass("ui-state-error") && myfield.value.substring(startPos).length > 0) {
            // if this is the last line, ask before proceeding
                var cfrm = confirm("You are on the last line of the page. T-PEN can save the remaining text in the linebreaking tool for later insertion. \n\nConfirm?");
                if (cfrm) {
                    leftovers = myfield.value.substring(startPos);
                    $("#lbText").text(leftovers);
                    myfield.value=myfield.value.substring(0, startPos);
                    Linebreak.saveLeftovers(escape(leftovers));
                } else {
                    return false;
                }
            } else {
                //prevent saving from changing focus until after values are changed
                var nextfield = focusItem[1].next(".transcriptlet").find(".theText")[0];
                nextfield.value = myfield.value.substring(startPos)+nextfield.value;
                Preview.updateLine(nextfield);
                myfield.value = myfield.value.substring(0, startPos);
                Preview.updateLine(myfield);
                $(nextfield).parent(".transcriptlet").addClass("isUnsaved");
                focusItem[1].find(".nextLine").click();
            }
        }
        Data.saveTranscription();
        return false;
    }
};

/* Interaction */
var Interaction = {
    /**
     *  Posts to check for an active connection to the server. Alerts user if
     *  disruption will result in loss of ability to save edits.
     */
    heartbeat: function(){
       $.ajax({
            url:"heartbeat",
            type:"POST",
            success:Interaction.ping(),
            error:function(){}
        });
    },
    /**
     *  Pings server.
     */
    ping: function(){
        window.setTimeout(this.heartbeat, 300000);
    },
    /**
     *  Alerts user if disruption will result in loss of ability to save edits.
     */
    forbiddenError: function(){
        if ($("#urgentError").length == 0){
            var ErrorNotice = ["<div id='urgentError'><p id='errorMessage'>",
                "This sessions has timed out due to inactivity or a server reset. ",
                "Please log in again to resume working.<br/>",
                "<span>Some work on the current, unsaved line may have been lost. ",
                "Check your work carefully after logging in.</span><br/>",
                "<form id='login' action='login.jsp' method='POST'>",
                "<label for='uname'>Email</label><input class='text' type='text' name='uname'>",
                "<label for='password'>Password</label><input class='text' type='password' name='password'>",
                "<input type='hidden' name='ref' value='http://t-pen.org/TPEN/login.jsp'>",
                "<input class='ui-state-default ui-button ui-corner-all' type='submit' title='Log In' value='Log In'>",
                "</form></p>",
                "<a href='index.jsp'>T&#8209;PEN Home</a></div>",
                "<div id='trexHead'></div>"].join('');
            $("body").append(ErrorNotice);
        }
    },
    /**
     * Passes a click through the designated object.
     * 
     * @param ghost jQuery element to pass the click through
     * @param contain jQuery element within which to pass clicks
     */
    clickThrough: function(event,ghost,contain){
        var boundary = {
            x: contain.offset().left,
            y: contain.offset().top,
            w: contain.width(),
            h: contain.height()
        };
        var clicks = {
            x: event.pageX,
            y: event.pageY
        };
        if ((clicks.x > boundary.x) && (clicks.x < boundary.x+boundary.w) && ((clicks.y > boundary.y) && (clicks.y < boundary.y+boundary.h))) {
            ghost.css("z-index",-10).hide();
            clickPassed = true;
            $(document.elementFromPoint(event.pageX,event.pageY)).click();
            clickPassed = false;
        } else {
            return true;
        }
    },
    /**
     * Places cursor at the beginning of textarea.
     * 
     * @deprecated Use setCursorPosition(e,0)
     * @param e element textarea to place cursor within
     */
     selectFirst: function(e) {
        this.setCursorPosition(e, 0);
    },
    /**
     * Handles key events in textareas.
     * 
     * @param e key event
     * @return boolean to accept keystroke
     */
     keyhandler: function(e) {
        var pressedkey=e.which;
        if(e.keyCode) pressedkey=e.keyCode;
        if(pressedkey==13) {  //pressed return, move any text in front of the cursor down to the next line
            if (e.shiftKey || e.altKey) return true;
            e.preventDefault();
            return Linebreak.moveTextToNextBox();
        }
        if(pressedkey>48 && pressedkey<59) {
            if(e.ctrlKey) {
                var theChar = window["char" + pressedkey];
                this.addchar(String.fromCharCode(theChar));
                return false;
            }
        }
        if(e.altKey){   //alt options for navigation
            if(pressedkey==40){ //down arrow
                focusItem[1].find('.nextLine').click();
                return false;
            }
            if(pressedkey==38){ //up arrow
                focusItem[1].find('.previousLine').click();
                return false;
            }           
            if (pressedkey == 36) { //home key, does not work in some browsers, which reserve it
                $("#transcription1").focus();
                return false;
            }
            if (pressedkey == 35) { //end key, does not work in some browsers, which reserve it
                $(".transcriptlet:last").find(".theText").focus()
                return false;
            }
        }
    },
    /**
     * @deprecated As of 2.0, prefer focusItem[0]
     * Updates currentFocus with current textarea.
     * 
     * @param element active transcription textarea
     */
     newFocus: function(element) {
//        currentFocus=$(element).attr("id");
    },
    /**
     * Adds closing tag button to textarea.
     * 
     * @param tagName text of tag for display in button
     * @param fullTag title of tag for display in button
     */
     closeTag: function(tagName,fullTag){
            // Do not create for self-closing tags
            if (tagName.lastIndexOf("/") == (tagName.length-1)) return false;
            var tagLineID = focusItem[1].attr("data-lineid");
            var closeTag = document.createElement("div");
            var tagID;
            $.get("tagTracker",{
                addTag      : true,
                tag         : tagName,
                projectID   : projectID,
                folio       : folio,
                line        : tagLineID
            }, function(data){
                tagID = data;
                $(closeTag).attr({
                    "class"     :   "tags ui-corner-all right ui-state-error",
                    "title"     :   unescape(fullTag),
                    "data-line" :   tagLineID,
                    "data-folio":   folio,
                    "data-tagID":   tagID
                }).text("/"+tagName);
                focusItem[1].children(".xmlClosingTags").append(closeTag);
            });
        //orderTags()
        //FIXME: tags not in the right order, just the order they are added 
    },
    /**
     * Removes tag from screen and database without inserting.
     * 
     * @param thisTag tag element
     */
     destroyClosingTag: function(thisTag){
        if(!isMember && !permitModify)return false;
                $(thisTag).fadeOut("normal",function(){
                    $(thisTag).remove();
                });
                this.removeClosingTag(thisTag);
                return false;
    },
    /**
     * Removes tag from screen and database without closing.
     * 
     * @param thisTag tag element
     */
     removeClosingTag: function(thisTag){
        var tagID = thisTag.getAttribute("data-tagID");
        $.get("tagTracker",{
            removeTag   : true,
            id          : tagID
        }, function(data){
            if(!data){
                alert("Database communication error.\n(openTagTracker.removeTag:removal "+tagID+")");
            }
        });
    },
    //make tags visible or invisible depending on location
    /**
     * Hides or shows closing tags based on origination.
     */
     updateClosingTags: function(){
        var tagIndex = 0;
        var tagFolioLocation = 0;
        var currentLineLocation = focusItem[1].index();
        var currentFolioLocation = folio;
        focusItem[1].find("div.tags").each(function(){
            tagFolioLocation = parseInt($(this).attr("data-folio"),10);
            tagIndex = $(".transcriptlet[data-lineid='"+$(this).attr("data-line")+"']").index();
            if (tagFolioLocation == currentFolioLocation && tagIndex > currentLineLocation) {
            // tag is from this page, but a later line
                $(this).hide();
            } else {
            //tag is from a previous page or line    
                $(this).show();
            }
        });
    },
    //use String[] from TagTracker.getTagsAfterFolio() to build the live tags list
    /**
     * Builds live tags list from string and insert closing buttons.
     * Uses String[] from utils.openTagTracker.getTagsAfterFolio().
     * 
     * @param tags comma separated collection of live tags and location properties
     */
     buildClosingTags: function(tags){
        var thisTag;
        var closingTags = [];
        for (i=0;i<tags.length;i++){
            thisTag = tags[i].split(",");
            var tagID               =   thisTag[0];     
            var tagName             =   thisTag[1];
            var tagFolioLocation    =   thisTag[2];
            var tagLineLocation     =   thisTag[3];
            if (tagID>0){        //prevent the proliferation of bogus tags that did not input correctly
                closingTags.push("<div class='tags ui-corner-all right ui-state-error");
                if (folio !== tagFolioLocation) {
                    closingTags.push(" ui-state-disabled' title='(previous page) ");
                } else {
                    closingTags.push("' title='");
                }
                closingTags.push(tagName,"' data-line='",tagLineLocation,"' data-folio='",tagFolioLocation,"' data-tagID='",tagID,"'>","/",tagName,"</div>");
            }    
        }
        focusItem[1].find(".xmlClosingTags").html(closingTags.join(""));
    },
    /**
     * Inserts a value into active textarea.
     * 
     * @param theChar value to insert
     */
     addchar: function(theChar, closingTag)
    {
        if(!isMember && !permitModify)return false;
        var closeTag = (closingTag == undefined) ? "" : closingTag;
        var e = focusItem[1].find('.theText')[0];
        if(e!=null) {
            Data.makeUnsaved();
            return this.setCursorPosition(e,this.insertAtCursor(e,theChar,closeTag));
        }
        return false;
    },
    /**
     *  Insert tag string from XML Button into the transcription textarea.
     *
     *  @param tagName string xml tag name
     *  @param fullTag string entire tag including all parameters
     */
    insertTag: function(tagName,fullTag){
        if (tagName.lastIndexOf("/") == (tagName.length-1)) {
            //transform self-closing tags
            var slashIndex = tagName.length;
            fullTag = fullTag.slice(0,slashIndex)+fullTag.slice(slashIndex+1,-1)+" />";
        }
        // Check for wrapped tag
        if (!this.addchar(escape(fullTag),escape(tagName))) {
            this.closeTag(escape(tagName), escape(fullTag));
        }
        
    },
    /**
     * Inserts value at cursor location.
     * 
     * @param myField element to insert into
     * @param myValue value to insert
     * @return int end of inserted value position
     */
     insertAtCursor: function(myField, myValue, closingTag) {
        var closeTag = (closingTag == undefined) ? "" : unescape(closingTag);
        //IE support
        if (document.selection) {
            myField.focus();
            sel = document.selection.createRange();
            sel.text = unescape(myValue);
            Preview.updateLine(myField);
            return sel+unescape(myValue).length;
        }
        //MOZILLA/NETSCAPE support
        else if (myField.selectionStart || myField.selectionStart == '0') {
            var startPos = myField.selectionStart;
            var endPos = myField.selectionEnd;
            if (startPos != endPos) {
                // something is selected, wrap it instead
                var toWrap = myField.value.substring(startPos,endPos);
                myField.value = myField.value.substring(0, startPos)
                    + unescape(myValue)
                    + toWrap
                    + "</" + closeTag +">"
                    + myField.value.substring(endPos, myField.value.length);
                myField.focus();
                Preview.updateLine(myField);
                var insertLength = startPos + unescape(myValue).length +
                    toWrap.length + 3 + closeTag.length;
                return "wrapped" + insertLength;              
            } else {
                myField.value = myField.value.substring(0, startPos)
                    + unescape(myValue)
                    + myField.value.substring(startPos, myField.value.length);
                myField.focus();
                Preview.updateLine(myField);
                return startPos+unescape(myValue).length;
            }
        } else {
            myField.value += unescape(myValue);
            myField.focus();
            Preview.updateLine(myField);
            return myField.length;
        }
    },
    /**
     * Sets cursor position in form element.
     * 
     * @param e element
     * @param position position for cursor
     */
     setCursorPosition: function(e, position)
    {
        var pos = position;
        var wrapped = false;
        if (pos.toString().indexOf("wrapped") == 0) {
            pos = parseInt(pos.substr(7));
            wrapped = true;
        }
        e.focus();
        if(e.setSelectionRange) {
            e.setSelectionRange(pos,pos);
        }
        else if (e.createTextRange) {
            e = e.createTextRange();
            e.collapse(true);
            e.moveEnd('character', pos);
            e.moveStart('character', pos);
            e.select();
        }
        return wrapped;
    },
    /**
     * Navigates to selected page in project.
     * 
     * @param dropdown select element listing pages
     */
     navigateTo: function(dropdown) {
        if (isUnsaved()){
            $("body").ajaxStart(function(){
                $(this).addClass("ui-state-disabled");
            }).ajaxStop(function(){
                document.location='?p='+dropdown.value+'&tool='+liveTool;
            });
//            Annotation.autoSave();
            Data.saveTranscription();
//            var exit = confirm("Some changes to this page have not been saved. If you continue, you will lose any unsaved data.");
//            if (!exit) {
//                $(".isUnsaved").removeClass("isUnsaved");
//                return false;
//            }
        } else {
            document.location='?p='+dropdown.value;
        }
    },
    /** 
     * Divides the screen and displays a tool on the right.
     *  
     * @param tool  id of the tool to display
     * @param width integer of the img or form within the component
     */
     splitScreen: function(tool,width,event){
       var btn = ($(event.target).hasClass('wBtn')) ? event.target : event.target.parentNode;
       if (!Screen.restore(btn,event)) return false;
        Interaction.activateToolBtn(event.target);
        $("#wrapper").resizable('enable');
        var PAGEWIDTH = Page.width();
        $("#tools").css({
            "height":"100%",
            "width":width/PAGEWIDTH*100+"%",
            "overflow":"hidden"
        }).children("[id*='plit']").hide();
        $("#"+tool).show();
        var arrowSpacing = (Page.height()-80)/4; // 80 = 16px * 5 arrows
        $("#fullscreenBtn")
            .show().find("span")
            .css({"margin-bottom":arrowSpacing+"px"});
        $("#wrapper").css({
            "width" :   (PAGEWIDTH-width)/PAGEWIDTH*100+"%",
            "padding-right" : "16px"
//FIXME this is disabled as the transitions have been pulled
//                }).one("webkitTransitionEnd transitionend oTransitionEnd",function(){
            // Assure a clean transition
//            Screen.updatePresentation(focusItem[1]);
        });
        Screen.updatePresentation(focusItem[1]);
    },
    /** 
     * Restores interface after shift key is released.
     */
     unShiftInterface: function(){
        $("#entry, #captions,#imgTop,#imgBottom").unbind("mousedown")
        .css("cursor","");
        $("#bookmark").resizable("option","disabled",true)
        .find("ui-resizable-handle").hide();
        $(document).mouseup();
    },
    /** 
     * Allows workspace to be moved up and down on the screen.
     * Requires shift key to be held down.
     */
     moveWorkspace: function(event){
        $("#imgTop,#imgBottom,#imgBottom img").addClass('noTransition');
        var startImgTop = $("#imgTop").height();
        var startImgBottom = $("#imgBottom img").position().top;
        var startImgBottomH = $("#imgBottom").height();
        var mousedownPosition = event.pageY;
        event.preventDefault();
        $(dragHelper).appendTo("body");
        $(document)
        .disableSelection()
        .mousemove(function(event){
            $("#imgTop").height(startImgTop + event.pageY - mousedownPosition);
            $("#imgBottom").css({
                "height": startImgBottomH - (event.pageY - mousedownPosition)
            }).find("img").css({
                "top"   : startImgBottom - (event.pageY - mousedownPosition)
            });
            $("#dragHelper").css({
                top :   event.pageY - 90,
                left:   event.pageX - 90
            })
            if(!event.altKey) Interaction.unShiftInterface();
        })
        .mouseup(function(){
            $("#dragHelper").remove();
            $("#imgTop,#imgBottom,#imgBottom img").removeClass('noTransition');
            $(document)
            .enableSelection()
            .unbind("mousemove")
            isUnadjusted = false;
        });
    },
    /** 
     * Allows manuscript image to be moved around.
     * Requires shift key to be held down.
     * Synchronizes movement of top and bottom images.
     * Bookmark bounding box moves with top image.
     */
     moveImg: function(event){
        if($(event.target).hasClass("ui-resizable-handle")) return true; //user is trying to resize the bookmark
        if($(event.target).attr('id')=="workspace" || $(event.target).parents("#workspace").length > 0) return true; //user is trying to move the workspace or resize the bookmark
        var startImgPositionX = parseInt($("#imgTopImg").css("left"));
        var startImgPositionY = parseInt($("#imgTopImg").css("top"));
        var startBottomImgPositionX = parseInt($("#imgBottom img").css("left"));
        var startBottomImgPositionY = parseInt($("#imgBottom img").css("top"));
        var startBookmarkX = parseInt($("#bookmark").css("left"));
        var startBookmarkY = parseInt($("#bookmark").css("top"));
        var mousedownPositionX = event.pageX;
        var mousedownPositionY = event.pageY;
        event.preventDefault();
        $(dragHelper).appendTo("body");
        $("#imgTopImg,#imgBottom img,#bookmark").addClass('noTransition');
        $(document)
        .disableSelection()
        .mousemove(function(event){
            $("#imgTopImg").css({
                top :   startImgPositionY + event.pageY - mousedownPositionY,
                left:   startImgPositionX + event.pageX - mousedownPositionX
            });
            $("#imgBottom img").css({
                top :   startBottomImgPositionY + event.pageY - mousedownPositionY,
                left:   startBottomImgPositionX + event.pageX - mousedownPositionX
            });
            $("#dragHelper").css({
                top :   event.pageY - 90,
                left:   event.pageX - 90
            });
            //$("#previewBtn").html((startBookmarkX + event.pageX - mousedownPositionX)+","+(startBookmarkY + event.pageY - mousedownPositionY));//debug
            $("#bookmark").css({
                top :   startBookmarkY + event.pageY - mousedownPositionY,
                left:   startBookmarkX + event.pageX - mousedownPositionX
            });
            if(!event.altKey) Interaction.unShiftInterface();
        })
        .mouseup(function(){
            $("#dragHelper").remove();
            $("#imgTopImg,#imgBottom img,#bookmark").removeClass('noTransition');
            $(document)
            .enableSelection()
            .unbind("mousemove")
            isUnadjusted = false;
        });
    },
    /**
     * Zooms in on the bounded area for a closer look.
     * 
     * @param zoomOut: boolean to zoom in or out, prefer to use isZoomed
     */
    zoomBookmark: function(zoomOut){
        var topImg = $("#imgTopImg");
        var btmImg = $("#imgBottom img");
        var imgSrc = topImg.attr("src");
        if (imgSrc.indexOf("quality") === -1) {
            imgSrc += "&quality=100";
            topImg.add(btmImg).attr("src",imgSrc);
        }
        var WRAPWIDTH = $("#wrapper").width();
        var availableRoom = new Array (Page.height()-workspaceHeight(),WRAPWIDTH);
        var limitIndex = (bookmarkWidth/bookmarkHeight > availableRoom[1]/availableRoom[0]) ? 1 : 0;
        var zoomRatio = (limitIndex === 1) ? availableRoom[1]/bookmarkWidth : availableRoom[0]/bookmarkHeight;
        var imgDims = new Array (topImg.height(),topImg.width(),parseInt(topImg.css("left")),parseInt(topImg.css("top"))-bookmarkTop);
//        var imgBottomDims = new Array (btmImg.height(),btmImg.width(),parseInt(btmImg.css("left")),parseInt(btmImg.css("top"))-bookmarkTop);
        if (!zoomOut){
            //zoom in
            $("#bookmark").hide();
            zoomMemory = [parseInt(topImg.css("top")),parseInt(btmImg.css("top"))];
            $("#imgTop").css({
                "height"    : bookmarkHeight * zoomRatio + 32
            });
            topImg.css({
                "width"     : imgDims[1] * zoomRatio / WRAPWIDTH * 100 + "%",
                "left"      : -bookmarkLeft * zoomRatio,
                "top"       : imgDims[3] * zoomRatio
            });
            btmImg.css({
                "left"      : -bookmarkLeft * zoomRatio,
                "top"       : (imgDims[3]-bookmarkHeight) * zoomRatio,
                "width"     : imgDims[1] * zoomRatio / WRAPWIDTH * 100 + "%"
            });
            isZoomed = true;
        } else {
            //zoom out
            topImg.css({
                "width"     : "100%",
                "left"      : 0,
                "top"       : zoomMemory[0]
            });
            btmImg.css({
                "width"     : "100%",
                "left"      : 0,
                "top"       : zoomMemory[1]
            });
            $("#imgTop").css({
                "height"    : imgTopHeight
            });
            isZoomed = false;
        }        
    },
    /**
     * Implements mouseZoom on the designated element.
     * 
     * @param img string ID of img element to magnify
     */
    magnify: function(img){
//        $("#"+img).on("mousemove",function(event){
            Interaction.mouseZoom($("#"+img));
//        });
    },
    /** 
     * Creates a zoom on the image beneath the mouse.
     *  
     * @param img jQuery img element to zoom on
     */
    mouseZoom: function($img){
        isMagnifying = true;
        if (!event) event=window.event;
        //collect information about the img
        var imgDims = new Array($img.offset().left,$img.offset().top,$img.width(),$img.height());
        //build the zoomed div
        var zoomSize = (Page.height()/3 < 120) ? 120 : Page.height()/3;
        var zoomPos = new Array(event.pageX - zoomSize/2,event.pageY - zoomSize/2);
        $("#zoomDiv").css({
            "box-shadow"    : "2px 2px 5px black,15px 15px "+zoomSize/3+"px rgba(230,255,255,.8) inset,-15px -15px "+zoomSize/3+"px rgba(0,0,15,.4) inset",
            "width"         : zoomSize,
            "height"        : zoomSize,
            "left"          : zoomPos[0] + 3,
            "top"           : zoomPos[1] + 3,
            "background-position" : "0px 0px",
            "background-size"     : imgDims[2] * zoomMultiplier+"px",
            "background-image"    : "url('"+$img.attr("src")+"')"
        })
        //TODO add to current tool so clickthrough is not needed - change positioning to accomodate
        //.appendTo(toolDiv)
        .show().on("click",function(event){
            Interaction.clickThrough(event, $(this),$("#tools"));
        });
        $(document).on({
                mousemove: function(event){
                  if (liveTool !== "image" && liveTool !== "compare") {
                    $(document).off("mousemove");
                    $("#zoomDiv").hide();
                  }
                var mouseAt = new Array(event.pageX,event.pageY);
                var zoomPos = new Array(mouseAt[0]-zoomSize/2,mouseAt[1]-zoomSize/2);
                var imgPos = new Array((imgDims[0]-mouseAt[0])*zoomMultiplier+zoomSize/2-3,(imgDims[1]-mouseAt[1])*zoomMultiplier+zoomSize/2-3); //3px border adjustment
                $("#zoomDiv").css({
                    "left"  : zoomPos[0],
                    "top"   : zoomPos[1],
                    "background-size"     : imgDims[2] * zoomMultiplier+"px",
                    "background-position" : imgPos[0]+"px " + imgPos[1]+"px",
                    "z-index"             : 2
                });
//                if ((mouseAt[0] < imgDims[0]) || (mouseAt[0] > imgDims[0] + imgDims[2]) || (mouseAt[1] < imgDims[1]) || (mouseAt[1] > imgDims[1] + imgDims[3])){
//                    $(document).unbind("mousemove");
//                    isMagnifying = false;
//                    $("#zoomDiv").fadeOut();
//                }
            }
          }, $img
        );
    },
    /** 
     * Builds div for use by Interaction.writeLines(imgToParse).
     *  
     * @param thisLine transcriptlet element to represent
     * @param multiplier ratio to 1000px standard
     * @return complete div for addition to DOM
     */
     makeOverlayDiv: function(thisLine,originalX){
        var Y = parseInt(thisLine.find(".lineTop").val());
        var X = parseInt(thisLine.find(".lineLeft").val());
        var H = parseInt(thisLine.find(".lineHeight").val());
        var W = parseInt(thisLine.find(".lineWidth").val());
        var oH = ($.browser.opera) ?
            [-1/$("#imgTopImg")[0].width,2/$("#imgTopImg")[0].height] : 
            [0,0]; //opera hack
        var newY = Page.convertPercent(Y/1000+oH[0],2);
        var newX = Page.convertPercent(X/originalX+oH[0],2);
        var newH = Page.convertPercent(H/1000+oH[1],2);
        var newW = Page.convertPercent(W/originalX+oH[1],2);
        var lineOverlay = ["\n<div class='line' style='",
        "top:",   newY,
        "%; left:",  newX,
        "%; height:",newH,
        "%; width:", newW,
        "%;' data-lineid='",thisLine.attr("data-lineid"),"'",
        " linetop='",   Y,"'",
        " lineleft='",  X,"'",
        " lineheight='",H,"'",
        " linewidth='", W,"'>",
        "</div>"];
        return lineOverlay.join("");
    },
    /** 
     * Overlays divs for each parsed line onto img indicated.
     * Divs receive different classes in different 
     *  
     * @param imgToParse img element lines will be represented over
     */
    writeLines: function(imgToParse){
        $(".line,.parsing,.adjustable,.parsingColumn").remove(); //clear and old lines to put in updated ones
        var originalX = imgToParse[0].width/imgToParse[0].height*1000;
        var setOfLines = [];
        $(".transcriptlet").each(function(index){
            setOfLines[index] = Interaction.makeOverlayDiv($(this),originalX);
        });
        imgToParse.before($(setOfLines.join("")));
    },
    /**
     *  Attaches highlight to active tool.
     *  
     *  @param button element that was clicked
     */
    activateToolBtn: function(button){
        $("#buttonList").find(".ui-state-active").not($("#charactersPopin, #xmlTagPopin")).removeClass("ui-state-active");
//        $(button).addClass("ui-state-active");
        if ($(button).hasClass('wBtn')) {
          $(button).addClass("ui-state-active");
        } else if ($(button).hasClass('ui-icon')) {
          $(button).parent().addClass("ui-state-active");
        }
    },
    /**
     *  Shrinks the interface buttons to icons when the width
     *  of the workspace narrows.
     */
    shrinkButtons: function(){
        if($('.shrink').length < 5){
            $('.counter').addClass('shrink')
                .each(function(){
                    $(this).text($(this).text().replace('Column:','').replace(' Line',''))
                });
            $('#buttonList').find('a').add('.nextLine,.addNotes,.previousLine')
                .addClass('shrink').filter(':not(:has(.ui-icon))')
                .append('<span class="ui-icon ui-icon-script"></span>');
            $("#helpBtns").hide();
        }
    },
    /**
     *  Restores the interface buttons from icons when the width
     *  of the workspace expands.
     */
    expandButtons: function(){
        if($('.shrink').not('.icon').length > 0){
            $('.shrink').not('.icon').removeClass('shrink');
            $('.counter').each(function(){
                var text = $(this).text();
                $(this).text('Column:'+text.substr(0,1)+' Line'+text.substr(1,3));
            });
            $('#buttonList').find('.ui-icon-script').remove();
            $("#helpBtns").show();
        }
    }
};

var Data = {
    /**
     * Adds status class of isUnsaved.
     * isUnsaved is tested in several other functions.
     * 
     * @see navigateTo(dropdown)
     * @see Data.saveTranscription()
     */
    makeUnsaved: function(){
        if(isMember || permitModify || permitNotes){
            focusItem[1].addClass("isUnsaved");
        }
    }, 
    /**
     * Caches manuscript image.
     */
    preloadPage: function(){
        msImage = new Image();
        // insert images once loaded
        $(msImage).load(this.loadPage);
        // preload the image file
        if(typeof imgURL !== "undefined")
            msImage.src=imgURL;
    },
    /**
     * Reveals screen after loading image.
     */
    loadPage: function() {
        if(typeof imgURL !== "undefined")
            $(".preloadImage").attr("src",imgURL);
        $("#imgTop").find("p").remove();
        //wait to reveal page
        $("#transcriptionPage").css("visibility","visible");
        $("#parsingLoader").fadeOut(250);
        imgRatio = $("#imgTopImg")[0].width / $("#imgTopImg")[0].height;
        //build out page relationships
        Parsing.setLineNavBtns();
        focusOnLastEntry();
        //if there is no transcription data on the page, confirm the automatic parsing first
        if(isBlank){
            $("#parsingBtn").click();
            $("#backToTranscribing").stop(true,true).show("scale",250,function(){$(this).siblings().removeClass("ui-state-disabled");}).siblings().addClass("ui-state-disabled");        
        }
        $.get("tagTracker",{
            listTags    : true,
            folio    : folio,
            projectID   : projectID
        }, function(tags){
            if(tags != null){     
                Interaction.buildClosingTags(tags.split("\n"));
            }
        });
        Data.postloader();
        Interaction.heartbeat();
    },
    /**
     * Sets up page after loading.
     * Caches and parses neighboring manuscript images.
     * Loads active tool.
     */
    postloader: function() {
        // assure page loaded completely
        if($("#transcriptionPage").length==1){
            var screenCheck = setInterval (function(){
                if ($("#imgTopImg").height()/1000 === screenMultiplier || liveTool === "parsing"){
                    clearInterval(screenCheck);
                    Screen.updateCaptions();
                } else {
                    focusItem[1].find(".theText").blur().focus();
                    console.log("adjust ");
                }
            },500);
        }
        msPrevImage = new Image();
        msNextImage = new Image();
        if(typeof prevURL !== "undefined")
            msPrevImage.src=prevURL;       // load previous and next page images into the browser cache
        if(typeof nextURL !== "undefined")
            msNextImage.src=nextURL;
        $("#abbrevImg").attr({
            "src" : "//t-pen.org/images/cappelli/Scan0064.jpg",
            "width": "auto"
        });
        abbrevLabelsAll = $("#abbrevLabels").clone();
        if (liveTool != "none") {
            var activate = (liveTool.indexOf("frameBtn") == 0) ? liveTool : liveTool+"Btn";
            $("#"+activate).click();
        }
        History.scrubHistory();
        if (nextFolio != null) {
            $.post("parseImage",{
                folio   : nextFolio
            });
        }
    },
    /** 
     * Formats the date display with leading zero when needed.
     *  
     * @param number number of day or month
     * @return int number with leading zero if needed
     */
    dateFormat: function(number){
        return (number<10 ? '0' : '') + number;
    },
    /**
     *  Saves transcription object textual data.
     *  
     *  @param transcription String from main textarea value
     *  @param notes String from notes section
     *  @param lineid int unique line id
     *  @param folioNum int unique folio id
     */
    saveLine: function(transcription,notes,lineid,folioNum){
        if(!isMember && !permitModify && !permitNotes)return false;
        $.ajax({
            url:"updateLine",
            type:"POST",
            data: {
                text:transcription,
                comment:notes,
                folio:folioNum,
                line: lineid,
                projectID: projectID
            },
            success:function(data) {
                if(Data.saveSuccess(data, transcription)) {
                    var date=new Date();
                    var columnMark = $(".transcriptlet[data-lineid='"+lineid+"']").find(".counter").text().replace(" ", "&nbsp;");
                    $("#saveReport")
                        .stop(true,true).animate({"color":"green"}, 400)
                        .prepend("<div class='saveLog'>"+columnMark + '&nbsp;saved&nbsp;at&nbsp;'+date.getHours()+':'+Data.dateFormat(date.getMinutes())+':'+Data.dateFormat(date.getSeconds())+"</div>")//+", "+Data.dateFormat(date.getDate())+" "+month[date.getMonth()]+" "+date.getFullYear())
                        .animate({"color":"#618797"}, 600);
                        $(".transcriptlet[data-lineid='"+lineid+"']").removeClass("isUnsaved");
                    History.prependEntry(lineid);
                } else {
                    alert("The server is responding, but saving has failed. Please submit a bug report including the content of the line you were trying to save.\n\nThe following was sent to the server:\n"+transcription);
                }
            },
            timeout: 5000,
            dataType: "html"
        });  
    },
    /** 
     * Records the values of the current and immediately previous transcriptions.
     * Checks for changes with isUnsaved before executing.
     */
    saveTranscription: function(){
        if(isUnsaved()) {
            var saveLine;
            var saveText;
            var saveNotes;
            $(".isUnsaved").each(function(){
                saveLine = $(this).attr("data-lineid");
                saveText = $(this).find(".theText").val();
                saveNotes = $(this).find(".notes").val();
                Data.saveLine(saveText, saveNotes, saveLine, folio);
            });
        } else {
            $("#saveReport")
                .stop(true,true).animate({"color":"red"}, 400)
                .prepend("<div class='noChange'>No changes made</div>")//+", "+Data.dateFormat(date.getDate())+" "+month[date.getMonth()]+" "+date.getFullYear())
                .animate({"color":"#618797"}, 1600,function(){$("#saveReport").find(".noChange").remove();});
        }
//        Annotation.autoSave();
    },
    /**
     * Checks for successful saving of transcription data.
     * 
     * @param data string returned from saveTranscription POST
     * @param saveText string sent to saveTranscription POST
     */
    saveSuccess: function(data, saveText){
       /*FIXME better comparison - linebreaking is tough without.
        if ( saveText === undefined ) return true;
        var simpleComp = (escape(data)==escape(saveText));
        if (!simpleComp)alert("The saving process encountered an unexpected problem. Please submit a bug report including the content of the line you were trying to save.\n\nThe following was sent to the server:\n"+saveText);
        return simpleComp;*/
        return true;
    }
};
/**
 * Updates manuscript image within compare tool.
 * 
 * @param dropdown select element within compare tool
 */
function compareTo(dropdown){
    if (isNaN(parseInt(dropdown.value))) return false; // Selected "Compare to page"
    $("#gotoThis").addClass("ui-state-disabled");
    var compareImgURL = "imageResize?folioNum="+dropdown.value+"&height=2000";
    if (!loadingCompare) var loadingCompare = "<div id='loadingCompare'><p class='loadingStatus'>requesting image&nbsp;.&nbsp;.&nbsp;.</p><img src='css/custom-theme/images/loadingImg.gif' height='"+Page.height()+"' /></div>";
    $("#compareSplit").append(loadingCompare);
    $("#compareDiv").hide().attr("src", compareImgURL).load(function(){
        $("#compareBtn").click();
        $("#loadingCompare").remove();
        $(this).fadeIn("normal");
        $("#gotoThis").removeClass("ui-state-disabled");
    });
}
/* jQuery Handling */
$(function() {
    $("#loadText").css("left","100%");
    setTimeout('$("#loadText").html("Building&nbsp;Page...")', 5200);
    setTimeout('$("#loadText").html("Initializing&nbsp;Interface...")', 14000);
    setTimeout('$("#loadText").html("<div class=\'ui-state-error ui-corner-all\' style=\'text-shadow:none;font-size:18px;padding:5px;\'><h3>This&nbsp;is&nbsp;taking&nbsp;a&nbsp;long&nbsp;time!</h3>There&nbsp;may&nbsp;have&nbsp;been&nbsp;a problem&nbsp;loading&nbsp;your&nbsp;image. You may continue to wait, or attempt to reload the page.</div>").css({"left":"40%","width":"300px"})', 25000);
    // broken img
    $("img").error(function(){
        $(this).attr({
            "src":"images/imageerror.jpg",
            "width":$("#abbrevSplit").width()
            });
    });
    $('.counter').addClass('ui-corner-all ui-state-active ui-button');
    $('.nextLine, .addNotes, .previousLine').addClass('ui-corner-all ui-state-default ui-button');
    $(".showMe").append("<span class='ui-icon ui-icon-carat-1-s'>more</span>");
    // toolbar formatting
    $('#msOptions,#navOptions,#projectOptions,#siteNavigation,.toolLinks,#helpBtns,#popinDiv')
        .children('a,input,button').addClass('ui-state-default ui-button')
        .filter(':first-child').addClass('listBegin').end()
        .filter(':last-child').addClass('listEnd');
        $("#gotoThis").click(function(){
            if ($(this).hasClass("ui-state-disabled")) return false;
            //TODO get a swap index and record the page leaving to place it here.
            //also attach IDs to selects to make them easier to grab and use a flexible variable to know if it is the same project or not
            document.location = "transcription.jsp?tool=none&compareIndex="+folio+"&p="+$("#pageCompare").val(); // tool=none because of undecipherable issue with #wrapper width when loading compare tool FIXME
        });
    $("a[id$='Btn']:not(:has('span.ui-icon'))").append("<span class='left ui-icon ui-icon-gear'></span>");
    $('body')
    .on({
        mouseenter: function(){$(this).addClass('ui-state-hover')},
        mouseleave: function(){$(this).removeClass('ui-state-hover')}
    }, '.ui-state-default')
    .on({
        mouseenter  : function(){$(this).siblings(".parsingColumn").stop(true,true).fadeTo(150, .1).css("z-index",2);},
        mouseleave  : function(){$(".parsingColumn").stop(true,true).fadeTo(150, .3).css("z-index",3);},
        keydown     : function(event){
            //FIXME This does not seem to work in Chrome. Perhaps another option will work.
            if (!event) event=window.event;
            var pressedkey=e.which;
            if(e.keyCode)pressedkey=e.keyCode;
            if(e.keyCode > 36 && e.keyCode < 41){
                //pressed arrow keys, cycle through divs
                var parsingColumns = $(".parsingColumn");
                var numberOfColumns = parsingColumn.length;
                var thisIndex = parsingColumns.index($(this));
                var moveTo = (e.keyCode == 37 || e.keyCode == 38) ? -1 : 1;
                var newIndex = thisIndex + moveTo;
                if (newIndex > numberOfColumns)newIndex = 0;
                if (newIndex < 0)newIndex = numberOfColumns;
                parsingColumns.eq(newIndex).trigger("mouseleave").delay(150).trigger("mouseenter");
            }
        }
    },".parsingColumn")
    .on({
        mouseenter: function(){
                        var lineInfo;
                        lineInfo = $("#transcription"+($(this).index(".line")+1)).val();
                        $("#lineInfo").empty().text(lineInfo).append("<div>" + $("#t"+($(this).index(".line")+1)).find(".counter").text() +"</div>").show();
                        if (!isMagnifying){
                        $(this).addClass("jumpLine");
                        }
                    },
        mouseleave: function(){
                        $(".line").removeClass("jumpLine");
                        $("#lineInfo").hide();
                    },
        click:      function(event){
                        Screen.clickedLine(this,event);
                    }
    },".line")
    .on({
        mouseenter: Parsing.applyRuler,
        mouseleave: Parsing.removeRuler,
        click:      function(event){
                        if ($("#addLines").hasClass('ui-state-active')||$("#removeLines").hasClass('ui-state-active')){
                            Parsing.lineChange(this,event);
                        }
                    }
    },".parsing")
    .on({
        mouseenter: function(){
                        $(this).fadeOut()
                    }
    },"#savedChanges,#progress,#lineResizing,.btnTitle");
    $('#annotationSplit').on({
        click:  function(){
            var annoID = $(".activeAnnotation").attr('data-id')
            Annotation.remove(annoID);
            $(".activeAnnotation").remove();
            $("#annotationInfo").fadeOut();
        },
        mouseenter: function(){
            $(".activeAnnotation").addClass("deleteAnno");
        },
        mouseleave: function(){
            $(".activeAnnotation").removeClass("deleteAnno");
        }
    }, '#deleteAnnotation')
    .on({
        mouseenter: function () {
            $("#aLinkFrame").hide("fade",250, function(){
                $(this).remove();
                $("#aShowLink").text("Preview Link");
            });
            $(".annotation").addClass('adjustAnno');
            $('.annotation').draggable({
                stop: function(event,ui){Annotation.adjust($(this),ui)},
                containment: $("#annotations")
            }).resizable({
                stop: function(event,ui){Annotation.adjust($(this),ui)},
                containment: $("#annotations"),
                minHeight: 5,
                minWidth: 5,
                handles: 'all'
            });
        },
        mouseleave: function() {
            $(".annotation").removeClass('adjustAnno').draggable('destroy').resizable('destroy');        
        },
        click: function(){
            var panel = $("#annotationInfo");
            var anno = $(this);
            if (anno.hasClass("activeAnnotation")){
                Annotation.updateText();
                Annotation.updateLink();
                $(".activeAnnotation").removeClass("activeAnnotation");
                panel.fadeOut()
            } else {
                $(".activeAnnotation").removeClass("activeAnnotation");
                anno.addClass("activeAnnotation");
                panel.show();
                Annotation.showText(anno);
                panel.removeClass("topAdjust");
                if (anno.position().top + anno.height() > $("#annotations").height() - panel.height()) {
                    panel.addClass("topAdjust");
                }
            }
        }
    }, '.annotation');
    $('#entry')
    .on('focusin','.transcriptlet',function(){
        if (!isZoomed) {
            Screen.updatePresentation($(this));
        } else {
            // looking at zoomed line
        }
    })
    //Next and Previous line buttons: names match i-value of target
    .on('click','.previousLine',function(){
        var navIndex = $(this).parents(".transcriptlet").index()-1
        $(".transcriptlet").eq(navIndex).find(".theText").focus();
    })
    .on('click','.nextLine',function(){
        var navIndex = $(this).parents(".transcriptlet").index()+1
        $(".transcriptlet").eq(navIndex).find(".theText").focus();
    })
    .on('keyup',".theText,.notes",function(event){
        if (!event) event = window.event;
        if (event.which > 45 || event.which == 8 || event.which == 32) {
            Preview.updateLine(this);
        }
    })
    .on('focus',".theText,.notes",function(event){
        var textVar = this;
        textVar.style.height = 0;
        this.style.height = textVar.scrollHeight + "px";
    })
    .on('click','.addNotes',function(){
        Screen.notesToggle($(this).parent('.transcriptlet'));
    })
    .on({
        click: function(event){
            if(event.target != this){return true;}
            Data.makeUnsaved();
            Interaction.addchar("<" + $(this).text() + ">");
            Interaction.destroyClosingTag(this);
            },
        mouseenter: function(){
            $(this).css({
                "padding": "4px",
                "margin": "-3px -4px -2px -3px",
                "z-index": 21
            })
            .append("<span onclick='Interaction.destroyClosingTag(this.parentNode);' class='destroyTag ui-icon ui-icon-closethick right'></span>");
            },
        mouseleave: function(){
            $(this).css({
                "padding": "1px",
                "margin": "0px -1px 1px 0px",
                "z-index": 20
            })
            .find(".destroyTag").remove();
        }
    },".tags");   
    $(".exitPage").click(function(event) {
      var thisLink = $(this).attr("href");
      if (isUnsaved()) {
        event.preventDefault();
        $("body").ajaxStart(function() {
          $(this).addClass("ui-state-disabled");
        }).ajaxStop(function() {
          if (thisLink.indexOf("?") != -1)
            thisLink += "&tool=" + liveTool;
          document.location = thisLink;
        });
        Data.saveTranscription();
        if (leftovers != undefined)
          Linebreak.saveLeftovers(escape(leftovers));
      } else {
        document.location = thisLink;
      }
    });
    //IPR links
    $("#iprAgreement").find("a").attr("target","_blank");
    //Parsing Options
    $("#parseOptions").children("span").click(function(event){
        $("#imgTop").unbind();
        $("#confirmParsingInst").slideUp();
        var thisID = $(this).attr("id");
        if (!$("#"+thisID+"Inst").is(":visible")) {
            $(".actions").not($("#"+thisID+"Inst")).slideUp();
            $("#"+thisID+"Inst").slideDown();
        }
        $(".accordion").children("div").slideUp().end()
            .children("span").removeClass("ui-state-active").children(".ui-icon-triangle-1-s").switchClass("ui-icon-triangle-1-s","ui-icon-triangle-1-e");
        $(this).addClass("ui-state-active")
            .children(".ui-icon-carat-1-e").switchClass("ui-icon-carat-1-e","ui-icon-carat-1-s").end()
        .siblings("span").removeClass("ui-state-active")
            .children(".ui-icon-carat-1-s").switchClass("ui-icon-carat-1-s","ui-icon-carat-1-e");
        if (thisID == "backToTranscribing"){
            return true;
        }
        else if (thisID == "ctrlLines") {
            Interaction.writeLines($("#imgTopImg"));
            $("#imgTop").children(".line").addClass("parsing").removeClass("line");
        }
        else if (thisID == "ctrlColumns") {
            Parsing.linesToColumns();
        };
    });
    $(".accordion").children("span")
        .append("<span class='ui-icon ui-icon-triangle-1-e left'></span>")
        .click(function(event){
        if(!isMember && !permitParsing)return false;
        $("#imgTop").unbind();
        var thisID = $(this).attr("id");
        if (!$(this).next("div").is(":visible")) {
            $(this).siblings("div").not($(this).next("div")).slideUp();
            $(this).addClass("ui-state-active")
                .children(".ui-icon-triangle-1-e").switchClass("ui-icon-triangle-1-e","ui-icon-triangle-1-s").end()
                .siblings("span").removeClass("ui-state-active")
                .children(".ui-icon-triangle-1-s").switchClass("ui-icon-triangle-1-s","ui-icon-triangle-1-e");          
            $(this).next("div").slideDown();
        }
        if (thisID == "adjustLines"){
            //prep for adjustment
            var thisLineID = -1;
            var originalW = 1;
            var thisLine;
            Interaction.writeLines($("#imgTopImg"));
            $("#imgTop").children(".line").addClass("adjustable").removeClass("line");
            if($(".adjustable").hasClass("ui-resizable")){
                $(".adjustable").has(".ui-resizable").resizable("enable");
            } else {
                $(".adjustable").resizable({
                    handles     : "e",
                    containment : 'parent',
                    start       : function(event,ui){
                        originalW = ui.originalSize.width;
                        var newW = ui.size.width;
                        $("#progress").html("Resizing Line").fadeIn();
                        $("#lineResizing").show();
                        $("#originalLine").html(originalW);
                        $("#newLine").html(Math.round(100*(newW/originalW)));
                        $("#sidebar").fadeIn();
                        thisLine = $(".ui-resizable-resizing");
                    },
                    resize      : function(event,ui){
                        newW = ui.size.width;
                        $("#newLine").html(Math.round(100*(newW/originalW)));
                    },
                    stop        : function(event,ui){
                        $("#progress").html("Line Resized - Saving...");
                        var thisLineID = thisLine.attr("data-lineid");
                        var thisLineW = Math.round(parseInt(thisLine.attr("linewidth"))*(newW/originalW));
                        thisLine.attr("linewidth",thisLineW);
                        $(".transcriptlet[data-lineid='"+thisLineID+"']").find(".lineWidth").val(thisLineW);
                        if(Parsing.updateLine(thisLine)=="success"){
                            $("#progress").html("Line Saved");
                            $("#lineResizing").delay(3000).fadeOut(1000);                        
                        };
//FIXME this does not actualy detect success
                            $("#progress").html("Line Saved").delay(3000).fadeOut(1000);
                            $("#lineResizing").delay(3000).fadeOut(1000);                        
                    }
                });
            }
        }
        if (thisID == "addLines"){
            Interaction.writeLines($("#imgTopImg"));
            $("#imgTop").children(".line").addClass("parsing").removeClass("line");
            isAddingLines = true;
        }
        if (thisID == "removeLines"){
            //prep for column adjustment
            Interaction.writeLines($("#imgTopImg"));
            $("#imgTop").children(".line").addClass("parsing").removeClass("line");
            isAddingLines = false;
        }
        if (thisID == "createColumn"){
            var isCreating = true;
            Parsing.adjustColumn(event);
            Parsing.removeRuler();
            $("#imgTop").bind({
                mousedown: function(event){
                    if ((event.target.id != "imgTopImg") || !$("#createColumn").hasClass("ui-state-active")){
                        return true;
                        isCreating = false;
                    }
                    //console.log("mousedown");
                    //capture mouseclick and insert a div
                    var newCol = "<div id='newCol' class='parsing'></div>";
                    var point = {x:event.pageX,y:event.pageY};
                    var deltaPoint = {x:0,y:0};
                    $(newCol).insertBefore($("#imgTopImg")).css({
                        'position'  : 'absolute',
                        'top'       : point.y,
                        'left'      : point.x,
                        'min-width' : "5px",
                        'min-height': "5px"
                    });
                    $("#imgTop").bind({
                        mousemove:  function(event){
                            //console.log("mousemove");
                            event.preventDefault();
                            deltaPoint = {
                                x: event.pageX-point.x,
                                y: event.pageY-point.y
                            }
                            $("#newCol").css({
                                "width":    deltaPoint.x,
                                "height":   deltaPoint.y
                            });
                        },
                        mouseup:    function(event){
                    if (!isCreating || !$("#createColumn").hasClass("ui-state-active")){
                        return true;
                    }
                    //console.log("mouseup");
                    var $newCol = $("#newCol");
                    var colRatio = $("#imgTopImg").height() / 1000;
                    $newCol.attr({
                        "data-lineid"   : "-1",
                        "newline"       : true,
                        "linetop"       : parseInt($newCol.css("top")) / colRatio,
                        "lineleft"      : parseInt($newCol.css("left")) / colRatio,
                        "linewidth"     : parseInt($newCol.css("width")) / colRatio,
                        "lineheight"    : parseInt($newCol.css("height")) / colRatio,
                        "id"            : "",
                        "hastranscription": false
                    });
                    // Prevent tiny columns on accident
                    if (parseInt($newCol.attr('linewidth'))+parseInt($newCol.attr('lineheight')) < 12) {
                        $newCol.remove();
                        return true;
                    }
                    isAddingLines = true;
                    $("#createColumn").ajaxStop(function(){
                        $(this).click()
                        .unbind("ajaxStop");
                        isCreating = false;
                    });
                    Parsing.updateLine(null,Parsing.saveNewLine(null));
                    $('#imgTop').unbind('mousemove');
                }
                    });
                }
            });
        }
        if (thisID == "destroyColumn") {
            //prep for column adjustment
            Parsing.linesToColumns();
            $(".parsingColumn").bind({
                mouseenter: function(){
                    $(this).addClass("parsingColumnSelected");
                },
                mouseleave: function(){
                    $(this).removeClass("parsingColumnSelected");
                },
                click:      function(){
                    Parsing.removeColumn($(this));
                    $(this).hide("blind",250, function(){
                        $("#destroyColumn").click();
                    });
                }
            });
        }
        if (thisID == "clearColumns") {
            //prep for column adjustment
            Parsing.linesToColumns();
            var columnSet = $(".parsingColumn");
            columnSet.addClass("parsingColumnSelected");
            var columnText = (columnSet.size() === 1) ? "Destroy This Column" : "Destroy "+columnSet.size()+" Columns";
            $("#destroyPage").html('<span class="left ui-icon ui-icon-circle-close"></span>'+columnText+'');
        }
        if (thisID == "reparseColumns") {
            //load the adjustment tool and then show the reparsing instructions
            Parsing.adjustColumn(event);
            var columnSet = $(".parsingColumn");
            $("#reparseColumn").html('<span class="left ui-icon ui-icon-refresh"></span>Submit '+columnSet.size()+' for Reparsing');
        }
    }); 
    $("#destroyPage").click(function(){
        if(!isMember && !permitParsing)return false;
        $(".parsingColumn").each(function(){
            Parsing.removeColumn($(this));
        });
        $("#ctrlColumns").click();
    });
    $("#reparsePage").css("cursor","pointer").click(function(){
        if(!isMember && !permitParsing)return false;
        $("body").addClass("ui-state-disabled");
        $("#destroyPage").click();
        window.location.reload();
    })
    $("#reparseColumn").click(function(){
        if(!isMember && !permitParsing)return false;
        Parsing.linesToColumns();
        $("body").addClass("ui-state-disabled");
        var columnParams = new Array({
            name:   "projectID",
            value:  projectID
        },{
            name:   "folioNum",
            value:  folio
        });
        $(".parsingColumn").each(function(index, selectColumn){
            //check for data
            var columnPeers = $(".transcriptlet").filter(function(){
                return ($(this).find(".lineLeft") == $(selectColumn).attr("lineleft"))
            });
            columnPeers.each(function(){
                if ($(".transcriptlet[data-lineid='"+$(selectColumn).attr("startid")+"']").find(".theText").val().length > 0){
                    var toDelete = $(".transcriptlet[data-lineid='"+$(e).attr("data-lineid")+"']").find(".theText").val().substr(0,15)+"\u2026";
                    var cfrm = confirm("Removing this line will remove any data contained as well.\n'"+toDelete+"'\n\nContinue?");
                    if(!cfrm)return false;
                }
            })
            columnParams.push({
                name:   "t"+index,
                value:  $(selectColumn).attr("linetop")
            },{
                name:   "l"+index,
                value:  $(selectColumn).attr("lineleft")
            },{
                name:   "w"+index,
                value:  $(selectColumn).attr("linewidth")
            },{
                name:   "b"+index,
                value:  parseInt($(selectColumn).attr("linetop"))+parseInt($(selectColumn).attr("lineheight"))
            });
            //Parsing.removeColumn($(this));
        });
        $.get("fixColumns", $.param(columnParams), function(data){
            if (data == "success"){
                window.location.reload();
            } else {
                alert("Unexpected error. Please reload this page.");
                $("body").removeClass("ui-state-disabled");
            }
        }, 'html')
    });
    $('.popin').click(function(){
        var thisId = $(this).attr('id');
        $('#'+thisId+'List').toggle(250,'swing',function(){
            $('#'+thisId).toggleClass('ui-state-active');
            Screen.maintainWorkspace();
        });
    });
    $("#historySplit").on("click","a",function(){
        var button = $(this);
        switch(button.index()){
            case 0:
                History.parsingOnly(button);
                break;
            case 1:
                History.textOnly(button);
                break;
            case 2:
                History.showNotes(button);
        }
    });
    $("#previewSplit")
        .on("click",".previewText,.previewNotes",function(){Preview.edit(this);})
        .on("click","#previewNotes",function(){
            if($(this).hasClass("ui-state-active")){
                $(".previewNotes").hide();
                $("#previewNotes")
                    .text("Show Notes")
                    .removeClass("ui-state-active");
            } else {
                $(".previewNotes").show();
                $("#previewNotes")
                    .text("Hide Notes")
                    .addClass("ui-state-active");
            }
            Preview.scrollToCurrentPage();
//        })
//        .on("click","#previewAnnotations",function(){
//            if($(this).hasClass("ui-state-active")){
//                $(".previewAnnotations").empty();
//                $("#previewAnnotations")
//                    .text("Show Annotations")
//                    .removeClass("ui-state-active");
//            } else {
//                sciatCanvas.listAnnotationsInPreview();
//                $("#previewAnnotations")
//                    .text("Hide Annotations")
//                    .addClass("ui-state-active");
//            }
//            Preview.scrollToCurrentPage();
        });
    $(".magnifyBtn").click(function(event){
        if ($(this).hasClass("ui-state-active")){
            $('#zoomDiv').hide();
            isMagnifying = false;
            $(document).off("mousemove");
            $(this).removeClass("ui-state-active");
        } else {
            $(this).addClass("ui-state-active");
            $('#zoomDiv').show();
            Interaction.magnify($(this).attr("magnifyImg"));
        }
    });
    $(document)
        .keydown(function(event){
            if(!event)event=window.event;
            if(event.ctrlKey || event.metaKey){
                $("#bookmark,#location,#savedChanges,#zoomDiv").delay(750).hide("fade",250);
                $(".line").delay(750).animate({"opacity":.3},250);
//                $(".annotation").addClass("showAnno");
                if (event.which == 36||event.which == 103) {
                        $("#transcription1").focus();
                }
                if (event.shiftKey && !isZoomed) {
                    Interaction.zoomBookmark(isZoomed);
                }
            }
            //TAB hijack to keep behavior as expected
            var inForm = $(document.activeElement).is('input,textarea') && !$(document.activeElement).is('.theText,.notes');
            if(event.which == 9 && !inForm){
                event.preventDefault();
                if(event.shiftKey) focusItem[1].find(".previousLine").click();
                else  focusItem[1].find(".nextLine").not('.navigation').click();
            }
            //F1 for help
            if (event.which == 112) {
                event.preventDefault();
                $("#helpMe").click();
            }
//            if(event.which == F11) { //If ever desired to force it
//                var docElm = document.documentElement;
//                if (docElm.requestFullscreen) {
//                    docElm.requestFullscreen();
//                }
//                else if (docElm.mozRequestFullScreen) {
//                    docElm.mozRequestFullScreen();
//                }
//                else if (docElm.webkitRequestFullScreen) {
//                    docElm.webkitRequestFullScreen();
//                }
//            }
            //Change magnification
            if(isMagnifying){
                //back out
                if ((event.which === 109) || (event.which === 189)){
                    zoomMultiplier -= .4;
                }
                //ease in
                else if ((event.which === 107) || (event.which === 187)){
                    zoomMultiplier += .4;
                }
            } else {
                //stop magnifying
            }
            //ESC pressed
            if(event.which === 27) $("#fullscreenBtn").click();
        })
        .keyup(function(event){
            if(!event)event=window.event;
            if(!event.ctrlKey || !event.metaKey){
                $("#bookmark,#location").stop(true,true).show("fade",500);
                $(".line").stop(true,true).css("opacity","");
//                $(".annotation").removeClass("showAnno");
                if (isZoomed){
                    Interaction.zoomBookmark(isZoomed)
                }
                if (isMagnifying){
                    $("#zoomDiv").stop(true,true).show("fade",500);
                }
            }
        })
        .ajaxError(function(event,jqXHR,settings,exception){
            //TODO add more clear error handling
            switch (jqXHR.status){
                case 500:       //server error
                case 404:       //file not found
                case 403:       //forbidden
                    Interaction.forbiddenError();
                case 200:       //success
                    break;
            }
        });
    $("#historyListing").on({
        mouseenter: function(){
            History.adjustBookmark($(this));
            if(!isMember && !permitModify)return false;
            $(this).children(".historyOptions").fadeIn("fast");
        },
        mouseleave: function(){
        $("#historyBookmark").css({"box-shadow":"box-shadow:0 0 30px black","border":"solid thin blue"}).add(".historyDims").empty();
        $(this).children(".historyOptions").fadeOut("fast");
        }
    },".historyEntry");
    if(isMember || permitModify){
        $("#historyListing").on({
            mouseenter: function(){
                var dims = $(this).parents(".historyEntry").children(".historyRevert");
                dims.html($(this).attr("title")).show();
            }, 
            mouseleave: function(){
                $(this).parents(".historyEntry").children(".historyRevert").html("").hide();
            },
            click: function(){
                History.revert(this);
            }
        },".historyOptions span");
    }
    $("#fullscreenBtn, #closeHelp").on({
        click: Screen.fullsize
    });
    $("#wrapper").resizable({
        helper:'resizeHelper',
//        animate:true,
//        animateDuration:500,
//        animateEasing:'linear',
        handles:'e',
        start:function(){
            $("#tools").css("pointer-events","none");
        },
        stop: function(event,ui){
            $("#tools").css("pointer-events","auto");
            if (liveTool != "none") {
                var activate = (liveTool.indexOf("frameBtn") == 0) ? "split"+liveTool.substring(8) : liveTool+"Split";
                Interaction.splitScreen(activate,(Page.width()-ui.size.width),event);
                $("#tools")
//                    .css("overflow","auto")
                    .find("img").height("auto").width("100%");
                $("#wrapper").height("100%");
            }
        }
    });
    $("#lineInfo,#annotationInstructions").mouseover(function(){
        $(this).hide();
        if ($(this).css("top") != "18px"){
            $(this).css({
                "top":"18px",
                "bottom":"auto"
            });
        } else {
            $(this).css({
                "bottom":"18px",
                "top":"auto"
            });
        };
        $(this).show();
    });
    $("#abbrevGroups").change(function(){   
        $("#abbrevSplit").addClass("ui-state-disabled");
        $("#abbrevLabels option").remove();
        abbrevLabelsAll.children("."+$("#abbrevGroups option:selected").val()).clone(true).appendTo("#abbrevLabels");
        $("#abbrevLabels").removeAttr("disabled");
        $("#abbrevSplit").removeClass("ui-state-disabled");
        $("#abbrevLabels option:first").attr("selected",true);
        $("#abbrevLabels").change();
    });
    $("#abbrevLabels").change(function(){
        $("#abbrevImg").attr("src","//t-pen.org/images/cappelli/"+$(this).val());
    });
    $("#abbrevBtn").click(function(event){
        //confirm image is loaded
        liveTool = "abbrev";
//        Interaction.activateToolBtn(this);
        $("#abbrevImg").height("100%").width("auto");
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        var toolWidth = 700*Page.height()/1086; // Cover image
        toolWidth = Screen.limit(toolWidth);
        Interaction.splitScreen("abbrevSplit",toolWidth,event);
//        if(!loopCatch)var loopCatch = 0;
//        if(toolWidth<15){
//            loopCatch++
//            if (loopCatch>100){
//                loopCatch = 0;
//                return false;
//            }
//            $("#abbrevBtn").click();
//        }
        return false;
    });       
    $("#imageBtn").click(function(event){
        liveTool = "image";
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        jumpMultiplier = (Page.height()-22)/1000;                    // 1000 px standard image height, 22px adjustment for toolLinks header
        $("#fullImg").height(Page.height()-22).width("auto");
        var toolWidth = $("#fullImg")[0].width / $("#fullImg")[0].height * (Page.height() - 22);
        var limit = Screen.limit(toolWidth);
        if (limit != toolWidth){
            $("#fullImg").height('auto').width('100%');
            toolWidth = limit;
        }
        //$("#imageSplit").width(toolWidth);
        Interaction.splitScreen ("imageSplit",toolWidth,event);
        Interaction.writeLines($("#fullImg"));
        return false;
    });
//    $("#annotationBtn").click(function(event){
//        liveTool = "annotation";
//        Interaction.activateToolBtn(this);
//        if(isFullscreen)isUnadjusted = true;
//        isFullscreen = false;
//        $(".annotation").hide();
//        $("#annotationDiv").height(Page.height()-22).width("auto");
//        var toolWidth = $("#annotationDiv")[0].width / $("#annotationDiv")[0].height * (Page.height() - 22);
//        var limit = Screen.limit(toolWidth);
//        if (limit != toolWidth){
//            $("#annotationDiv").height('auto').width('100%');
//            toolWidth = limit;
//        }
//        //$("#annotationSplit").width(toolWidth);
//        Interaction.splitScreen("annotationSplit",toolWidth,event);
//        Annotation.display();
//        return false;
//    });
    $("#paleographyBtn").click(function(){
        if ($(this).attr('isready') == "true"){
            window.open('paleo.jsp?'+location.href.match(/\jsp\?(.*)/)[1]);          
        } else {
            alert('This page has not been analyzed yet. Please try later.');
        }
        return false;
    });
    $("#compareBtn").click(function(event){
        liveTool = "compare";
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        $("#compareDiv").height(Page.height()-22).width("auto");
        var toolWidth = $("#compareDiv")[0].width / $("#compareDiv")[0].height * (Page.height() - 22);
        var limit = Screen.limit(toolWidth);
        if (limit != toolWidth){
            $("#compareDiv").height('auto').width('100%');
            toolWidth = limit;
        }
        //$("#compareSplit").width(toolWidth);
        Interaction.splitScreen("compareSplit",toolWidth,event);
        if(compareIndex !== "none"){
            $("#pageCompare").children("option[value^='"+compareIndex+"']").attr("selected", true).change();
            compareIndex = "none";
        }
        return false;
    });
    $("#historyBtn").click(function(event){
        liveTool = "history";
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        var toolWidth = $("#historyViewer").width();
        Interaction.splitScreen("historySplit",toolWidth,event);
        $("#historyBookmark").empty();
        History.showLine(focusItem[1].attr("data-lineid"));
    });
//    $("#sciatBtn").click(function(event){
//        liveTool = "sciat";
//        Interaction.activateToolBtn(this);
//        if(isFullscreen)isUnadjusted = true;
//        isFullscreen = false;
//        var toolWidth = Page.width()*.5;
//        Interaction.splitScreen("sciatSplit",toolWidth,event);
//        $('#newCanvas').val(folio);
//        if ($("#annotationTool")[0].src.indexOf("svg-editor") == -1) {
//            $("#goCanvas").click();
//        }
//        return false;
//    });
    $("#parsingBtn").click(function(){
        liveTool = "parsing";
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        Parsing.hideWorkspaceForParsing();
        $(".actions").hide();
        $("#confirmParsingInst").show();
    });    
    $(".iframeTools").click(function(event){
        liveTool = $(this).attr("id");
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        var toolIndex = liveTool.substr(8);
        var toolWidth = Page.width()*.4;
        $("#frameSplitDiv"+toolIndex).height(function(){
            return Page.height()-22; // toolLinks margin
        });
        Interaction.splitScreen("split"+toolIndex,toolWidth,event);
    });
    $("#linebreakBtn").click(function(event){
        liveTool = "linebreak";
//        Interaction.activateToolBtn(this);
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
//        $("#linebreakDiv").width($("#linebreakSplit").width());
        Interaction.splitScreen("linebreakSplit",350,event);
        return false;
    });
    $("#previewBtn").click(function(event){
        event.preventDefault();
        liveTool = "preview";
//        Interaction.activateToolBtn(this);
        Preview.format();
        if(isFullscreen)isUnadjusted = true;
        isFullscreen = false;
        $("#previewDiv").height(Page.height()-22);        
        Interaction.splitScreen("previewSplit", 600,event);
        // Scroll to current page after transition
        window.setTimeout(Preview.scrollToCurrentPage, 800);
    });
//    $("#annotationSplit").find(".toolLinks").find("a").click(function(){Annotation.options(this)});
    $("#saveBtn").addClass("ui-state-disabled").click(function(){alert("Due to debugging, only autosaving is available at this time.");});
    $("#bookmark").resizable({
        disabled    :true,
        handles     :'e,s,n',
        minHeight   :16,
        minWidth    :16,
        aspectRatio :false,
        start: function(event,ui){
            var r = $("#bookmark");
            r.addClass("noTransition");
//            console.log(event.target);
//            if($(event.target).hasClass('ui-resizable-e')){
//                r.resizable("option",'minHeight',ui.originalSize.height)
//                    .resizable("option",'maxHeight',ui.originalSize.height);
//            } else {
//                r.resizable("option",'minWidth',ui.originalSize.width)
//                    .resizable("option",'maxWidth',ui.originalSize.width);
//            };
        },
        stop        :function(event,ui){
            var bookmark = $("#bookmark");
            bookmark.removeClass("noTransition");
            var thisID = bookmark.attr("lineid");
            var thisTranscriptlet = $(".transcriptlet[data-lineid='"+thisID+"']");
            var parseRatio = $("#imgTopImg").height()/1000;
            var newP = [ui.position.top, ui.size.width, ui.size.height];
            var oldP = [ui.originalPosition.top, ui.originalSize.width, ui.originalSize.height];
            var dummyBuild = ["<div id='dummy' style='display:none'",
            " data-lineid='", thisID, "'",
            " lineleft='",    thisTranscriptlet.find(".lineLeft").val(), "'",
            " linetop='",     Math.round(parseInt(thisTranscriptlet.find(".lineTop").val())     +(newP[0]-oldP[0])/parseRatio),"'",
            " linewidth='",   Math.round(parseInt(thisTranscriptlet.find(".lineWidth").val())   +(newP[1]-oldP[1])/parseRatio),"'",
            " lineheight='",  Math.round(parseInt(thisTranscriptlet.find(".lineHeight").val())  +(newP[2]-oldP[2])/parseRatio),"'",
            "></div>"];
            var dummy = dummyBuild.join("");
            thisTranscriptlet
                .find(".lineTop").val($(dummy).attr("linetop")).end()
                .find(".lineWidth").val($(dummy).attr("linewidth")).end()
                .find(".lineHeight").val($(dummy).attr("lineheight"));
            Parsing.updateLine(dummy);
        }
    });
    $(document).keydown(function(event){
        if (event.altKey){
            $("#entry, #captions").mousedown(function(event){Interaction.moveWorkspace(event)})
            .css("cursor","n-resize");
            $("#imgTop,#imgBottom").mousedown(function(event){Interaction.moveImg(event)})
            .css("cursor","url(css/custom-theme/images/grab.gif),move");
            $("#bookmark").resizable("option","disabled",false)
            .find("ui-resizable-handle").show("highlight",250);
            if ((event.ctrlKey || event.metaKey) && !isZoomed) {
                    Interaction.zoomBookmark(isZoomed)
                }
        }
    })
    .keyup(function(event){
        if (!event.altKey) {
            Interaction.unShiftInterface();
            if (isZoomed) Interaction.zoomBookmark(isZoomed);
        }
        });
    Data.preloadPage();
    $("[name='rulerColor']").change(function(){
        selectRulerColor = $(this).val();
        $('#sampleRuler').stop(true,true).animate({backgroundColor:selectRulerColor}, 500,function(){$(this).css('background-color',selectRulerColor);});
    });
    $("#helpMe").click(Help.revealHelp);
    $("#useText").click(Linebreak.useText);
    $("#useLinebreakText").click(Linebreak.useLinebreakText);
    $("#linebreakStringBtn").click(function(event){
        if(event.target != this){return true;}
        if ($("#linebreakString").val().length > 0) {
            $("#useLinebreakText").fadeIn();
            linebreakString = $("#linebreakString").val();
            brokenText = $("<div/>").html(leftovers).text().split(linebreakString);
            var btLength = brokenText.length;
            $("#lbText").html(function(index,html){
                return html.split(unescape(linebreakString)).join(decodeURI(linebreakString)+"<br/>");
            });        
        } else {
            alert("Please enter a string for linebreaking first.");
        }
        if (btLength > 1){
            $("#linesDetected").html("("+(btLength)+" lines detected)");
        } else {
            alert("Linebreak string was not found.");
        }
    });
});
/**
 * Starts page on last entry. Will not supercede currentFocus.
 * If page is complete, defaults to first line.
 * Runs after jQuery pageload.
 */
function focusOnLastEntry(){
    var lastEntry = ($("#"+currentFocus).is("textarea")) ? currentFocus : "transcription1";
    if (lastEntry != currentFocus){
        $(".theText").each(function(index){
            if ($(this).val().length > 0) {
                lastEntry = "transcription"+(index+1);
                isBlank = false;
            }
        });
        // Do not go to the last entry if it is the end of the page
        if (lastEntry == $(".theText").eq(-1).attr("id")) lastEntry = "transcription1";
    }
    focusItem[1] = $("#"+lastEntry).parent(".transcriptlet");
    Screen.updatePresentation(focusItem[1]);
}
$(window)
.resize(function(){
    clearTimeout(this.id);
    this.id = setTimeout(Screen.doneResizing, 500);
    });
