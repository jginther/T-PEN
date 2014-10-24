/* 
Handles retrieving from an annotation store
 */

function AnnotationStore(url,name, authenticationURL)
{
    this.url=url;
    this.name=name;
    this.authenticationURL=authenticationURL;

    this.fetch=function(canvas, type,functionToRun)
    {
        //fetch actual data from the annotation store here
        res=new Array();
    var url='proxy?url='+encodeURIComponent(this.url+'?canvas='+encodeURIComponent(canvas)+'&name='+encodeURIComponent(sciatUsername)+'&email='+encodeURIComponent(sciatEmail));
        $.ajax({
            url: url,
            success: $.proxy(function(data, status, xhr) {
                var repository = $.rdf.databank();
                repository.load(data);
                setPrefixesForDatabank(data, repository);
                var uris = [];
                var localCollections = [];
                // check for oac annotations
                if (data.getElementsByTagNameNS('http://www.openannotation.org/ns/','hasTarget').length < 1) {
                    // SCIAT message for no annotations
//                    alert ('No OAC annotations exist yet');
                    $('#SCIATloadMsg').remove();
                    var SCIATloadMsg = $("<div class='ui-state-active' id='SCIATloadMsg' style='position:absolute;left:40%;top:60%;padding:.5em;'>No OAC annotations exist yet</div>");
                    $('#annotationTool').after(SCIATloadMsg);
                    $('#SCIATloadMsg').animate({
                        top: '0%'
                    }, 2000, function(){
                        $('#SCIATloadMsg').fadeOut(4000);
                    });
                    return;
                }
                //find the annotation
                $.rdf({
                    databank: repository
                })
                .where('?collection oac:hasBody ?uri')
                .each(function(index, body) {
                            
                    var temp_body='';
                    var temp_target='';
                    var temp_svg='';
                    var annotator_name='';
                    var annotator_email='';
                    var generator='';
                    var creation='';
                    var bodyURI=body.uri.value.toString()
                    //find the target's text'
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+body.uri.value.toString()+'> cnt:rest ?txt')
                    .each(function(index, content) {
                        temp_body=content.txt.value.toString();
                    })
                    //now find the target
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+body.collection.value.toString()+'> oac:hasTarget ?uri')
                    .each(function(index, targ) {
                        temp_target=targ.uri.value.toString();
                    })
                    //fetch the urn:uuid for the annotator, so the name and email can be fetched
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+body.collection.value.toString()+'> oac:annotator ?uri')
                    .each(function(index, targ) {
                        var annotatorURN=targ.uri.value.toString();
                        $.rdf({
                            databank: repository
                        })
                        .where('<'+annotatorURN+'> foaf:name ?textval')
                        .each(function(index, targ) {
                            annotator_name=targ.textval.value.toString();
                               
                        })
                        $.rdf({
                            databank: repository
                        })
                        .where('<'+annotatorURN+'> foaf:mbox ?textval')
                        .each(function(index, targ) {
                            annotator_email=targ.textval.value.toString();
                        })
                    })
                    //fetch the generator uuid, and use that to fetch the generator name
                        
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+body.collection.value.toString()+'> oac:generator ?uri')
                    .each(function(index, targ) {
                        var generatorURI=targ.uri.value.toString();
                        $.rdf({
                            databank:repository
                        })
                        .where('<'+generatorURI+'> foaf:name ?name')
                        .each(function(index, targ) {
                            generator=targ.name.value.toString();
                        })   
                    })
                    //get the creation timestamp
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+body.collection.value.toString()+'> oac:generated ?timestamp')
                    .each(function(index, targ) {
                        creation=targ.timestamp.value.toString();
                    })    
                        
                        
                    $.rdf({
                        databank: repository
                    })
                    .where('<'+bodyURI+'> oac:hasSelector ?uri')
                    .each(function(index, targ) {
                        var selectorURN=targ.uri.value.toString();
                        $.rdf({
                            databank: repository
                        })
                        .where('<'+selectorURN+'> cnt:chars ?txt')
                        .each(function(index, targ) {
                            
                            temp_svg=targ.txt.value.toString();
                        })
                    })
                    res.push(new Annotation(temp_body,temp_target,'square',temp_svg,body.collection.value.toString(),null,annotator_email, annotator_name,creation,generator));
                })
                if (functionToRun) {
                    functionToRun(res);
                } else {
                    console.log("No callback for fetch.");
                }
            }),
            error: $.proxy(function(xhr, status, error) {
                alert('Error loading annotations from store: '+error);
            }, this)
        });
    }
    /**See if the user has read access to the annotation store. If false, authentication is needed.*/
    this.checkReadAccess=function()
    {
        //perform a test read
        return true;
    }
    /**See if the user has write access to the annotation store. If false, authentication is needed.*/
    this.checkWriteAccess=function()
    {
        //perform a test write
        return true;
    }
    this.getAutheticationUrl=function()
    {
        return this.authenticationURL;
    }
}