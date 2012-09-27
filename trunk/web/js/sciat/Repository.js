/* 
 * Container for all SharedCanvas manifests distributed by a single repository
 * 
 */
function Repos(url, functionToRun)
{
    this.manifests=new Array();
    this.names=new Array();
    this.names_hash={};
    toret=new Array();
    names=new Array
    names_hash={};
    completeCount=0;
    complete=false;
    
    $.ajax({
        url: url,
        success: $.proxy(function(data, status, xhr) {
            var repository = $.rdf.databank();
            repository.load(data);
            setPrefixesForDatabank(data, repository);

            var uris = [];
            var localCollections = [];
            $.rdf({
                databank: repository
            })
            .where('?collection ore:isDescribedBy ?uri')
            .each(function(index, match) {
                uris.push(match.uri.value.toString());
                toret.push(match.uri.value.toString());
                var manifestURI=match.collection.value.toString();
                var manifestURL=match.uri.value.toString();
                $.ajax({
                    // temporary T-PEN hosted annotation repo                
                    url: 'http://165.134.241.141:8080/Annotation/proxy?url='+match.uri.value.toString(),
                    success: $.proxy(function(data, status, xhr) {
                        var repository2 = $.rdf.databank();
                        repository2.load(data);
                        setPrefixesForDatabank(data, repository2);
                        $.rdf({
                            databank: repository2
                        })
                        .where('<'+manifestURI+'> dc:title ?uri')
                        .each(function(index, match) {
                            names.push(match.uri.value.toString());
                            names_hash[match.uri.value.toString()]=manifestURL;
                            completeCount++;
                            if(complete && completeCount==toret.length)
                            {
                                functionToRun();
                            }    
                        })
                        complete=true;
                    }
                    ),
                    error: $.proxy(function(xhr, status, error) {
                        alert('Error loading collection: '+error);
                    }, this)
                });
            })	
        }, this)
    });
    this.manifests=toret;
    this.names=names;
    this.names_hash=names_hash;
    this.getManifests=function()
    {
        return this.manifests;
    }
}
var setPrefixesForDatabank = function(data, databank) {
    var root = $(data).children()[0];
    for (var i = 0; i < root.attributes.length; i++) {
        var att = root.attributes[i];
        databank.prefix(att.nodeName.split(':')[1], att.nodeValue);
    }
};


