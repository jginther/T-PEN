T-PEN CSS Style and Structure
04 April 2012

Directories

Pages in T-PEN are almost exclusively .jsp format and in the main /web directory.
Subdirectories contain images, javascript (/js), and css as expected. The css
directory also contains /custom-theme which holds images and icons used to theme
interactions. An /includes directory can be found WEB-INF containing .jspf files
for some reusable elements.

General Structure

Most pages have the following structure and can be adjusted in dimension 
or layout with a few small changes:
body > div#wrapper > div#header || div#content || div#footer

The transcription interface eschews this structure to fill the screen,
containing all components for transcription within the div#wrapper and 
following it with div#tools, which contains all the additional tools.

Due to a bug in Google Chrome browsers, most modal div elements for manuscript
selection, etc. are placed outside the main content. In the case of includes,
javascript is used to move these elements after the page is built.

Interactivity

Animations and transitions help indicate loading status and movement across
large images. CSS3 transitions are preferred in most places and have a discrete
area of tpen.css. Some simple animations are managed by jQueryUI, and class-
swapping is used to change element states.

Javascript is relied upon for many click, key, and mouse handling behaviors.
In many places, span elements are used in place of anchors to reflect the necessity
of having javascript enabled for the complete use of tools on the site. The
jQueryUI (http://jqueryui.com) library is used extensively for managing general
display, such as the tabbed interface.

Theming

T-PEN makes thorough use the jQueryUI themes throughout the site. Changing the
custom jQuery.css reference to a jQuery theme stylesheet will alter the color
theme of most of the site with little alteration. For the puposes of co-branding,
the body tag under the typography section of tpen.css can be updated to point to
a new logo and the position adjusted. Use of colors throughout the site is
consistent and global find-replace should not cause unexpected results.