
/**Used for annotations that have been saved to the annotation store and thus have a uri rather than urn*/
function Annotation(body,target,shape,dimensions,annotationURI,functionToRun, annotator_email, annotator_name,creation, generator)
{
    this.annotationURI=annotationURI;
    this.shape=shape;
    this.body=body;
    this.dimensions=dimensions;
    this.target=target;
    this.annotator_email=annotator_email;
    this.annotator_name=annotator_name;
    this.creation=creation;
    this.generator=generator;
    //if there is no annotationURI, this is a new annotation that needs to be saved.
    var xml_special_to_escaped_one_map = {
    '&': '&amp;',
    '"': '&quot;',
    '<': '&lt;',
    '>': '&gt;'
};
function encodeXml(string) {
    return string.replace(/([\&"<>])/g, function(str, item) {
        return xml_special_to_escaped_one_map[item];
    });
};
    if(annotationURI==null)
    {
        //save this annotation
        var annotationURN=createUUID();
         this.annotationURI=createUUID();
        var selectorURN=createUUID();
        var content='<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:cnt="http://www.w3.org/2008/content#" xmlns:sc="http://www.shared-canvas.org/ns/" xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:oac="http://www.openannotation.org/ns/" ><rdf:Description rdf:about="urn:uuid:'+annotationURN+'"><rdf:type rdf:resource="http://www.w3.org/2008/content#ContentAsText"/><cnt:characterEncoding>utf-8</cnt:characterEncoding><cnt:rest>'+encodeXml(this.body)+'</cnt:rest><oac:hasSelector rdf:resource="urn:uuid:'+selectorURN+'"/></rdf:Description><rdf:Description rdf:about="urn:uuid:'+selectorURN+'"><rdf:type rdf:resource="oac:SvgSelector"/><cnt:chars>'+this.dimensions+'</cnt:chars></rdf:Description><rdf:Description rdf:about="urn:uuid:'+this.annotationURI+'"><rdf:type rdf:resource="http://www.shared-canvas.org/ns/ContentAnnotation"/><oac:hasTarget rdf:resource="'+this.target+'"/><oac:hasBody rdf:resource="urn:uuid:'+annotationURN+'"/></rdf:Description></rdf:RDF>';
        var params = new Array({
            name:'save',
            value:true
        },{
            name:'name',
            value:sciatUsername
        },{
            name:'email',
            value:sciatEmail
        },{
            name:'target',
            value:this.target
        },{
            name:'content',
            value:content
        });
        var newURI=this.annotationURI;
        var _this=this;
        $.ajax({
            type: 'POST',
            url: "proxy?url=http://165.134.241.71:80/Annotation/Annotation"+encodeURIComponent("?"+$.param(params)),
            data: $.param(params),
            success: function(data) {
            _this.annotationURI=data;        
             if(functionToRun!=null)
            functionToRun(_this); 
            return data;
                },
            dataType: "html"
        })
        .error(function(){
            alert('error saving annotation!');
        });
        
    }
    else
    {
    //dont need to do anything at this point
    }
  
        

    this.getBody=function()
    {
        return body;
    }
    this.getShape=function()
    {
        return this.shape;
    }
    this.getDimensions=function()
    {
        //restore double quotes in dimensions, removed for URL simplicity
        return this.dimensions.replace(/'/g,"\"");
    }
    this.getTarget=function()
    {
        return target();
    }

    this.update=function()
    {
        //save an existing annotation
        var annotationURN=createUUID();
        var selectorURN=createUUID();
        
        var content='<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:cnt="http://www.w3.org/2008/content#" xmlns:sc="http://www.shared-canvas.org/ns/" xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:oac="http://www.openannotation.org/ns/" ><rdf:Description rdf:about="urn:uuid:'+annotationURN+'"><rdf:type rdf:resource="http://www.w3.org/2008/content#ContentAsText"/><cnt:characterEncoding>utf-8</cnt:characterEncoding><cnt:rest>'+encodeXml(this.body)+'</cnt:rest><oac:hasSelector rdf:resource="urn:uuid:'+selectorURN+'"/></rdf:Description><rdf:Description rdf:about="urn:uuid:'+selectorURN+'"><rdf:type rdf:resource="oac:SvgSelector"/><cnt:chars>'+this.dimensions+'</cnt:chars></rdf:Description><rdf:Description rdf:about="urn:uuid:'+this.annotationURI+'"><rdf:type rdf:resource="http://www.shared-canvas.org/ns/ContentAnnotation"/><oac:hasTarget rdf:resource="'+this.target+'"/><oac:hasBody rdf:resource="urn:uuid:'+annotationURN+'"/></rdf:Description></rdf:RDF>';
        var params = new Array({
            name:'update',
            value:true
        },{
            name:'target',
            value:this.target
        },{
            name:'content',
            value:content
        },{
            name:'uri',
            value:this.annotationURI
            
        });
        $.ajax({
            type: 'POST',
            url: "proxy?url=http://165.134.241.71:80/Annotation/Annotation",
            data: $.param(params),
            success: function(data) {
            return data;
                },
            dataType: "html"
        })
        .error(function(){
            alert('error saving annotation!');
        });
    }
}
/**UUID generation code provided by a stackoverflow answer http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript*/
function createUUID() {
    // http://www.ietf.org/rfc/rfc4122.txt
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}


