function Canvas(uri,name,width,height, images, functionToRun)
{
    if(images)
    this.images=images;
else
    this.images='none';
    this.uri=uri;
    this.name=name;
    this.width=width;
    this.height=height;

this.getImages=function()
{
    return this.images; 
}
this.getUri=function()
{
    return this.uri
}
this.getWidth=function()
{
    return this.width;
}
this.getHeight=function()
{
    return this.height();
}
this.getName=function()
{
    return this.name;
}
}