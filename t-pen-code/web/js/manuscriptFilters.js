/* 
 * Document     : manuscriptFilters.js
 * Created on   : August 08, 2011
 * Author       : cubap
 * Comment      : compatable with jQuery 1.6.2 and jQueryUI 1.8.9
 * 
 * Javascript used for listing and displaying available manuscripts with the T&#8209;PEN project
 */
var Manuscript = {
    reset: null,
    setReset: function(){
        if(!this.reset) this.reset=$("#repositories").html();
        return this.reset;
    },
    /**
     * Shows filtered results for manuscripts by available repository.
     */
    filteredRepository: function() {
        $("#cities").find("option:first").attr("selected",true);
        $("#listings").html("Loading manuscripts . . .").addClass('loadingBook');
        var repository = document.getElementById("repositories");
        var myOption = repository.value;
        if (repository != null) {
            $.get("manuscriptListings?repository=" + myOption,"",function(data) {
            $("#listings").removeClass('loadingBook').html(data);
            Manuscript.styleResults();
                },"html");
        }
    },
    /**
     * Shows filtered results for manuscripts by city.
     * Applies filter to repository listings.
     */
    filteredCity: function() {
        $("#listings").html("Loading manuscripts . . .").addClass('loadingBook');
        var city = document.getElementById("cities");
        var myOption = city.value;
        if (city != null) {
            $.get("manuscriptListings?city=" + myOption,"",function(data) {
            $("#listings").removeClass('loadingBook').html(data);
            Manuscript.styleResults();
                },"html");
            Manuscript.repoFilter(myOption);
        }
    },
    /**
     * Filters available repository by selected city.
     * 
     * @param city String value from #cities dropdown
     * @see Manuscript.filteredCity()
     */
    repoFilter: function(city) {
        $("#repositories").empty();
        $.get("repoLister?city=" + city,"",function(data) {
           $('#repositories').html(data);
        },"html");
    },
    /**
     * Attaches helping information to manuscript links.
     * Called when either dropdown is finished.
     * 
     * @see Manuscript.filteredCity()
     * @see Manuscript.filteredRepository()
     */
    styleResults: function(){
        $('.resume')                // resume transcribing links on landing page
            .attr("title","A project already exists with this manuscript. Click to resume.")
            .append('<span class="ui-icon ui-icon-pencil right"></span>');
    },
    resetFilters: function(){
        $("#repositories").html(this.setReset());
    }
};