function xpand (i) {
    var totalHeight;
    var topHeight;
    var originalHeight = $('#id'+i).height();
    var originalTop = $('#i'+i).position().top;
    //You are already looking at the whole page... why did you click that?
    if (i==0 && $('#'+(i+1)) == null) {
        totalHeight = originalHeight;
        topHeight = originalTop;
    //First line only the first and next line
    } else if (i==0) {
        totalHeight = $('#id'+i).height() + $('#id'+(i+1)).height();
        topHeight = $('#i'+i).css('top');
    //Last line only the last and previous line
    } else if ($('#'+ (i+1)) == null) {
        totalHeight = $('#id'+i).height() + $('#id'+(i-1)).height();
        topHeight = $('#i'+(i-1)).position().top;
    //Full triple line display
    } else {
        totalHeight = $('#id'+i).height() + $('#id'+(i+1)).height() + $('#id'+(i-1)).height();
        topHeight = $('#i'+(i-1)).position().top;
    }

    $('#id'+i).toggle(
        function(event){
            if (totalHeight > 350){
                if(confirm('The resulting image is ' + totalHeight + 'px tall and may push other items off your screen. Limit to 350px? (Cancel to proceed anyway.)')){
                    totalHeight = 350;
                }
            };
            $(this).animate({
                height:totalHeight+'px'
            },1000);
            $('#i'+i).animate({
                top:topHeight+'px'
            },1200, 'easeOutBounce');
            $('#x'+i).html('<span class="left ui-icon ui-icon-arrowstop-1-n"></span>Collapse View');
            if(event2)$(this).unbind(event2);
            var event2 = true;
        },
        function(event2){
            $(this).animate({
                height:originalHeight+'px'
            },1000);
            $('#i'+i).animate({
                top:originalTop+'px'
            },1200, 'easeOutBounce');
            $('#x'+i).html('<span class="left ui-icon ui-icon-arrow-2-n-s"></span>Expand View');
            $(this).unbind(event);
        }
        );
}
function changeHeight (i) {
    xpand(i);
}

