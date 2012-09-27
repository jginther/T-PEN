/* Fetch a single manifest
 */
function Manifest(manifestURL,functionToRun)
{
    this.getManifestContent=function(manifestURL,functionToRun)
    {
        cache = [];
        liString='';
        var sequenceURL='';
        var uris = [];
        var _this=this;
        $.ajax({
            url: manifestURL,
            success: $.proxy(function(data, status, xhr,functionToRun) {
                var manifest = $.rdf.databank();
                manifest.load(data);
                setPrefixesForDatabank(data, manifest);
                var info = {};
                if (manifest.prefix().sc != null) {
                    var entry = $.rdf({
                        databank: manifest
                    })
                    .where('?ns rdf:type ?type')
                    .filter('type', /ns\/Sequence/)
                    .where('?ns sc:hasOptimizedSerialization ?opt')
                    .where('?ns ore:isDescribedBy ?uri').get(0);
                    if (entry.opt != null) {
                        info.optUri = entry.opt.value.toString();
                    } else {
                        info.nsUri = entry.ns.value.toString();
                    }
                } else {
                    var entry = $.rdf({
                        databank: manifest
                    })
                    .where('?ns rdf:type ?type')
                    .filter('type', /ns\/Sequence/)
                    .where('?ns ore:isDescribedBy ?uri').get(0).uri.value.toString();
                    info.nsUri = entry;
                }
           
                info.iaUri = $.rdf({
                    databank: manifest
                })
                .where('?ns rdf:type ?type')
                .filter('type', /ns\/ImageAnnotationList/)
                .where('?ns ore:isDescribedBy ?uri').get(0).uri.value.toString();
                sequenceURL=info.iaUri;
                sequenceURL='proxy?url='+sequenceURL;
           
                cache.push(info);

                liString += '<li id="man_'+this.idCount+'">'+info.title+'</li>';
                this.idCount++;
           
                this.fetchSeq('proxy?url='+info.nsUri,this,'proxy?url='+info.iaUri);
            
            }, this),
            error: $.proxy(function(xhr, status, error) {
                alert('Error loading collection: '+error);
            }, this)
        });
    }
                        
    this.fetchImageAnnotations=function (iaURL){
        var _this=this;
        var store = rdfstore.create()

        store.execute('LOAD <'+iaURL+'> INTO GRAPH <lisp>', function(success){
            if(success) {
                var query = 'PREFIX oac:<http://www.openannotation.org/ns/> select *{?sub oac:hasTarget ?obj}';// <'+ _this.sequence[i]+'>' PREFIX foaf:<http://xmlns.com/foaf/0.1/> SELECT ?o FROM NAMED <lisp> { GRAPH <lisp> { ?s foaf:page ?o} }';
                store.execute(query, function(success, results) {
                    alert(query[0].sub.value);
                // process results
                });
            }
        })
        $.ajax({
            url: iaURL,
            success: function(data, status, xhr, url) {
                var db=$.rdf.databank(data);
                db.load(data);
                setPrefixesForDatabank(data,db);

                var info = {};             
                for (var i=0;i<  _this.sequence.length;i++)
                {
                    var annos = $.rdf({
                        databank: db
                    }).where('?title oac:hasTarget <'+ _this.sequence[i]+'>');
                    annos = $.rdf({
                        databank: db
                    }).where('<'+annos.get(0).title.value+'> oac:hasBody ?title');
                                              
                    var dataBankBindings = annos.select();
                    var allBits='';
                                          
                    for(var j=0;j<dataBankBindings.length;j++)
                    {
                        allBits+=dataBankBindings[j].title.value+"";
                    }
                    _this.sequence[i]=new Canvas(_this.sequence[i],_this.title[i],'','',allBits,null);
                }
                displayManifest(_this);
            },
            error: $.proxy(function(xhr, status, error) {
                alert('Error loading manifest: '+error);
            },this)
        }
        ,null);
    }
                        
    this.fetchSeq=function (seqURL,manifestInstance,iaURI){
        
        $.ajax({
            url: seqURL,
            success: function(data, status, xhr, url) {
                var db=$.rdf.databank();
                db.load(data);
                setPrefixesForDatabank(data,db);

                var info = {};
                var annos3 = $.rdf({
                    databank: db
                }).where('?manifest ore:aggregates ?title'); 
                for (var i=0;i<  annos3.length;i++)
                {
                    manifestInstance.sequence[i]='';
                    manifestInstance.title[i]='';
                    try{
                        manifestInstance.sequence[i]=annos3.get(i).title.value;
                        $.rdf({
                            databank: db
                        }).where('<'+manifestInstance.sequence[i]+'> dc:title ?title')
                        .each(function(index, targ) {
                            manifestInstance.title[i]=targ.title.value.toString();
                        })   
                    }
                    catch( e)
                    {
                        alert('error:'+value);
                    }
                }
                function buildAnno(anno) {
                    var targetId = anno.children('oac\\:hasTarget').attr('rdf:resource');
                    var bodyId = anno.children('oac\\:hasBody').attr('rdf:resource');
                    var target = $('rdf\\:Description[rdf\\:about="'+targetId+'"]', data);
                    var body = $('rdf\\:Description[rdf\\:about="'+bodyId+'"]', data);
                    return new Canvas(bodyId,targetId,'w','h','annotationURI',targetID);
                }
                
                manifestInstance.fetchImageAnnotations(iaURI);
            },
            error: $.proxy(function(xhr, status, error) {
                alert('Error loading manifest: '+error);
            },null)
        }
        ,null);
    }
    this.setSequence=function(newSeq)
    {
        this.sequence=newSeq;
    }
    this.getSequence = function()
    {
        return this.sequence;
    }
    this.setAnnos=function()
    {
        for (anno in annos)
        {
            images[anno]=annos[anno].getBody();
            canvases[anno]=annos[anno].getTarget();
                    
        }
    }

    //code here for reading something like http://dms-data.stanford.edu/Oxford/Bodley342/Manifest.xml
    this.annos=null;
    this.sequence=new Array();
    this.title=new Array();
    images=new Array();
    
    // This bit is code from the dms kickstart that is being transformed to fit our purposes.
    manifestURL=url = 'proxy?url='+manifestURL;
    this.getManifestContent(manifestURL,functionToRun);
    
}


