/*
 * @author Jon Deering
 Copyright 2011 Saint Louis University. Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use
 this file except in compliance with the License.

 You may obtain a copy of the License at http://www.osedu.org/licenses/ECL-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 and limitations under the License.
 */
package textdisplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.tutego.jrtf.Rtf;
import com.tutego.jrtf.RtfPara;
import com.tutego.jrtf.RtfText;
import static com.tutego.jrtf.RtfText.*;

/**
 * A class to provide output formatting based on xml tags in a document.
 */
public class TagFilter {

   private String text;

   public TagFilter(String text) {
      this.text = text;
   }

   /**
    * Build an array of tag names that are present in the document.
    *
    * @return an array of tags with no brackets or parameters
    */
   public String[] getTags() {
      //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
      //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
      String[] parts = text.split("<");
      Hashtable h = new Hashtable();
      Stack<String> inOrder = new Stack();
      for (int i = 0; i < parts.length; i++) {
         String[] tmp = parts[i].split(">");
         //if there was a > tag, then this is an actual tag, not a random angle bracket, so add it to the list
         if (tmp.length > 1 || parts[i].endsWith(">")) {
            String thisTag = tmp[0];
            if (thisTag.endsWith("/")) {
               thisTag = thisTag.split(" ")[0];

            }
            thisTag = thisTag.split(" ")[0];
            if (h.contains(thisTag)) {
            } else {
               //find the matching end tag before adding this
               Boolean b = false;


               inOrder.add(thisTag);
               h.put(thisTag, thisTag);
            }
         }
      }
      String[] toret = new String[0];
      while (!inOrder.isEmpty()) {
         //only add the tag to the tag list if it is self closing or has a closing tag
         Boolean addThis = false;
         String theTag = inOrder.pop();
         if (theTag.endsWith("/")) {
            addThis = true;
         }
         if (h.contains("/" + theTag)) {
            addThis = true;
         }

         if (addThis) {
            String[] tmp = new String[toret.length + 1];
            for (int i = 0; i < toret.length; i++) {
               tmp[i] = toret[i];
            }
            tmp[tmp.length - 1] = theTag;
            toret = tmp;
         }
      }
      return toret;
   }

   /**
    * Remove tags in the along with any text or other tags inside these tags
    */
   public String removeTagsAndContents(String[] tagsToExclude) {
      //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
      //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
      if (tagsToExclude.length == 0) {
         return text;
      }
      String content = text;
      for (int i = 0; i < tagsToExclude.length; i++) {
         LOG.log(Level.INFO, "Removing {0}", tagsToExclude[i]);
         if (tagsToExclude[i] != null && tagsToExclude[i].compareTo("") != 0) {
            String[] parts = text.split("<" + tagsToExclude[i] + " .*?>");
            content = "";
            content += parts[0];
            for (int j = 1; j < parts.length; j++) {
               String[] tmp = parts[j].split("</" + tagsToExclude[i] + ">");
               if (tmp.length == 2) {
                  content += tmp[1];
               }

            }
         }
      }
      return content;

   }

   /**
    * Remove these tags along with any text or other tags inside these tags
    */
   public String stripTags(String[] tagsToExclude) {
      //this method does not use existing xml libraries because there is no presumption that the document is a well formed xml document
      //it can be a single page from a document, or just have some tags wrapping certain items that the user intends to style.
      if (tagsToExclude.length == 0) {
         return text;
      }
      String content = text;
      for (int i = 0; i < tagsToExclude.length; i++) {
         LOG.log(Level.INFO, "Stripping out {0}", tagsToExclude[i]);
         if (tagsToExclude[i] != null && tagsToExclude[i].compareTo("") != 0) {
            String[] parts = content.split("<" + tagsToExclude[i] + ">|<" + tagsToExclude[i] + " +.*?>");

            //System.out.print("tag is "+ tagsToExclude[i]+"\n");
            //for(int j=0;j<parts.length;j++)
            // System.out.print("part "+j+" is: "+parts[j]+"\n");
            content = "";
            content += parts[0];
            for (int j = 1; j < parts.length; j++) {
               String[] tmp = parts[j].split("</" + tagsToExclude[i] + ">");
               if (tmp.length == 2) {
                  content += tmp[0] + tmp[1];
               } else {
                  if (tmp.length < 2) {
                     System.out.print("Missed closing for " + tagsToExclude[i] + "\n" + tmp[0] + "\n");
                     content += tmp[0];
                  }

               }

            }
         }
      }
      return content;

   }

   public enum styles {

      italic, bold, underlined, superscript, none, remove, paragraph
   };

   public enum noteStyles {

      footnote, endnote, sidebyside, inline, remove
   };

   public void replaceTagsWithPDFEncoding(String[] tags, styles[] tagStyles, OutputStream os) throws DocumentException {
      //   FileWriter w = null;

      try {
         BaseFont bf = BaseFont.createFont("/usr/Junicode.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

         Document doc = new Document();
         PdfWriter p = PdfWriter.getInstance(doc, os);
         doc.open();
         Paragraph para = new Paragraph();
         para.setFont(new Font(bf, 12, Font.NORMAL));
         //doc.add(para);
         Font italic = new Font(bf, 12, Font.ITALIC);
         Font bold = new Font(bf, 12, Font.BOLD);
         Font underlined = new Font(bf, 12, Font.UNDERLINE);

         StringBuilder chunkBuffer = new StringBuilder(""); //holds the next bit of content that will be added to the pdf as a chunk
         styles chunkStyle = null; //the style to be applied to chunkBuffer when it gets added to the document
         String chunkTag = "";
         Stack<String> wrappingTags = new Stack();
         Stack<styles> wrappingStyles = new Stack();
         String content = text;
         Boolean inTag = false; //is this inside a tag, meaning between the < and >
         String tagTextBuffer = ""; //the text of the tag, including name and any parameters
         Boolean beingTagged = false; //Is the parser currently reading character data that is surrounded by a tag that demands styling
         for (int charCounter = 0; charCounter < this.text.length(); charCounter++) {

            if (text.charAt(charCounter) == '>') {
               inTag = false;
               //if this was a self closing tag, dont do anything
               if (tagTextBuffer.contains("/>")) {
                  tagTextBuffer = "";
               } else {
                  //this is a closing tag, save the chunk and pop the tag and style off of the stack
                  if (tagTextBuffer.startsWith("/")) {
                     if (chunkStyle != null) {
                        LOG.log(Level.INFO, " Closing tag {0} with style {1}", new Object[] { tagTextBuffer, chunkStyle.name() });
                     } else {
                        LOG.log(Level.INFO, " Closing tag {0} with style null", tagTextBuffer);
                     }
                     if (chunkStyle == styles.paragraph) {
                        chunkBuffer = new StringBuilder("\n" + chunkBuffer);
                     }
                     Chunk c = new Chunk(chunkBuffer.toString());
                     styleChunk(c, chunkStyle);

                     if (chunkStyle != styles.remove) {
                        para.add(c);
                     }
                     chunkBuffer = new StringBuilder("");
                     chunkStyle = null;
                     chunkTag = "";
                     if (!wrappingStyles.empty()) {
                        chunkStyle = wrappingStyles.pop();
                        chunkTag = wrappingTags.pop();
                     }
                     tagTextBuffer = "";

                  } else {
                     //this is the closing bracket of an opening tag
                     String tagName = tagTextBuffer.split(" ")[0];
                     LOG.log(Level.INFO, "Closing <{0}>", tagName);
                     for (int i = 0; i < tags.length; i++) {

                        if (tags[i].compareTo(tagName) == 0) {
                           // This is a tag that is supposed to be styled in the pdf.
                           if (chunkStyle != null) {
                              //this tag is nested in a tag that was already applying styling. Add this chunk to the pdf and put the tag/style
                              //for the previous tag on the stack, so when this new tag ends, the previous styling will resume.
                              if (chunkStyle == styles.paragraph) {
                                 chunkBuffer = new StringBuilder("\n" + chunkBuffer);
                              }
                              Chunk c = new Chunk(chunkBuffer.toString());
                              styleChunk(c, chunkStyle);
                              if (chunkStyle != styles.remove) {
                                 para.add(c);
                              }
                              wrappingStyles.add(chunkStyle);
                              wrappingTags.add(chunkTag);
                              chunkTag = tagName;
                              chunkStyle = tagStyles[i];
                              chunkBuffer = new StringBuilder("");
                           } else {
                              Chunk c = new Chunk(chunkBuffer.toString());
                              para.add(c);
                              chunkTag = tagName;
                              chunkStyle = tagStyles[i];
                              chunkBuffer = new StringBuilder("");
                           }
                        }
                     }
                     tagTextBuffer = "";
                  }
               }
            }
            if (inTag) {
               tagTextBuffer += text.charAt(charCounter);
            }
            if (text.charAt(charCounter) == '<') {
               if (inTag) {
                  //if we hit another < before hitting a > this was not a tag, so add the tagTextBuffer to the chunk. It was simply conent.
                  chunkBuffer.append(tagTextBuffer);
                  tagTextBuffer = "";
               }
               inTag = true;
            }
            if (!inTag && text.charAt(charCounter) != '>') {
               chunkBuffer.append(text.charAt(charCounter));
            }
         }
         Chunk c = new Chunk(chunkBuffer.toString());
         para.add(c);
         doc.newPage();
         doc.add(para);
         doc.newPage();
         doc.close();
      } catch (IOException ex) {
         Logger.getLogger(TagFilter.class.getName()).log(Level.SEVERE, null, ex);
      } finally {
      }

   }

   /**
    * Apply a style (font) to a chunk of pdf text
    */
   private void styleChunk(Chunk c, styles s) {
      try {

         BaseFont bf = BaseFont.createFont("/usr/Junicode.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
         BaseFont ita = BaseFont.createFont("/usr/Junicode-Italic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
         BaseFont bol = BaseFont.createFont("/usr/Junicode-Italic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
         if (bf.charExists(540)) {
            System.out.print("font cant do 540\n");
         }
         Font italic = new Font(ita, 12, Font.ITALIC);
         Font bold = new Font(bol, 12, Font.BOLD);
         Font underlined = new Font(bf, 12, Font.UNDERLINE);
         Font superscript = new Font(bf, 9, Font.NORMAL);



         if (s == styles.bold) {
            c.setFont(bold);
         }
         if (s == styles.italic) {
            c.setFont(italic);
         }
         if (s == styles.underlined) {
            c.setFont(underlined);
         }
         if (s == styles.superscript) {
            c.setTextRise(7.0f);
            c.setFont(superscript);

         }

         //wipe out that content


      } catch (DocumentException ex) {
         Logger.getLogger(TagFilter.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
         Logger.getLogger(TagFilter.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   /**
    * Style the tags that have been passed in as requested and write the rtf document to the writer w
    */
   public void replaceTagsWithRTFEncoding(String[] tags, styles[] tagStyles, Writer w) {






      for (int i = 0; i < tagStyles.length; i++) {
         if (tagStyles[i] == null) {
            tagStyles[i] = styles.none;
         }
      }
      Stack<RtfText> paragraphs = new Stack();
      String content = text;

      StringBuilder chunkBuffer = new StringBuilder(""); //holds the next bit of content that will be added to the pdf as a chunk
      styles chunkStyle = null; //the style to be applied to chunkBuffer when it gets added to the document
      String chunkTag = "";
      Stack<String> wrappingTags = new Stack();
      Stack<styles> wrappingStyles = new Stack();
      Boolean inTag = false; //is this inside a tag, meaning between the < and >
      String tagTextBuffer = ""; //the text of the tag, including name and any parameters
      Boolean beingTagged = false; //Is the parser currently reading character data that is surrounded by a tag that demands styling
      for (int charCounter = 0; charCounter < this.text.length(); charCounter++) {

         if (text.charAt(charCounter) == '>') {
            inTag = false;
            //if this was a self closing tag, dont do anything
            if (tagTextBuffer.contains("/>") || tagTextBuffer.contains("-->")) {
               System.out.print("Skipping auto closing or comment tag " + tagTextBuffer + "\n");
               tagTextBuffer = "";
            } else {
               //this is a closing tag, save the chunk and pop the tag and style off of the stack
               if (tagTextBuffer.startsWith("/")) {
                  if (chunkStyle != null) {
                     System.out.print(" closing tag " + tagTextBuffer.replace("/", "") + " with style " + chunkStyle.name() + "\n");
                  } else {
                     System.out.print(" closing tag " + tagTextBuffer + " with style null and content " + chunkBuffer + "\n");
                  }
                  if (chunkStyle != styles.remove) {
                     paragraphs.add(applyRTFStyle(chunkStyle, chunkBuffer.toString()));
                  }
                  chunkBuffer = new StringBuilder("");

                  chunkTag = "";
                  if (!wrappingStyles.empty()) {
                     chunkStyle = wrappingStyles.pop();
                     chunkTag = wrappingTags.pop();
                  } else {
                     System.out.print("Forcing style italic because style is unknown\n");
                     chunkStyle = styles.none;
                  }
                  tagTextBuffer = "";

               } else {
                  //this is the closing bracket of an opening tag
                  String tagName = tagTextBuffer.split(" ")[0];
                  System.out.print("tag is " + tagName + "\n");
                  for (int i = 0; i < tags.length; i++) {

                     if (tags[i].compareTo(tagName) == 0) {
                        // this is a tag that is suposed to be styled in the pdf
                        if (chunkStyle != null) {
                           //this tag is nested in a tag that was already applying styling. Add this chunk to the pdf and put the tag/style
                           //for the previous tag on the stack, so when this new tag ends, the previous styling will resume.
                           //if(chunkStyle != styles.remove)
                           paragraphs.add(applyRTFStyle(chunkStyle, chunkBuffer.toString()));
                           wrappingStyles.add(chunkStyle);
                           wrappingTags.add(chunkTag);
                           System.out.print("Stack add " + chunkTag + " with style " + chunkStyle + "\n");
                           chunkTag = tagName;
                           chunkStyle = tagStyles[i];
                           chunkBuffer = new StringBuilder("");
                        } else {
                           paragraphs.add(text(chunkBuffer));
                           chunkTag = tagName;
                           chunkStyle = tagStyles[i];
                           chunkBuffer = new StringBuilder("");
                        }
                     }
                  }
                  tagTextBuffer = "";
               }
            }
         }
         if (inTag) {
            tagTextBuffer += text.charAt(charCounter);
         }
         if (text.charAt(charCounter) == '<') {
            if (inTag) {
               //if we hit another < before hitting a > this was not a tag, so add the tagTextBuffer to the chunk. It was simply conent.
               chunkBuffer.append(tagTextBuffer);
               tagTextBuffer = "";
            }
            inTag = true;
         }
         if (!inTag && text.charAt(charCounter) != '>') {
            chunkBuffer.append(text.charAt(charCounter));

         }
      }
      if (chunkBuffer.length() > 0) {
         paragraphs.add(applyRTFStyle(styles.none, chunkBuffer.toString()));
      }
      Stack<RtfPara> textParas = new Stack();
      RtfText[] textarray = new RtfText[paragraphs.size()];
      for (int i = 0; i < paragraphs.size(); i++) {
         textarray[i] = paragraphs.get(i);
      }
      RtfPara p = RtfPara.p(textarray, true);

      textParas.add(p);
      Rtf.rtf().section(textParas).out(w);
      return;

   }

   public RtfText applyRTFStyle(styles tagStyle, String text) {
      RtfText styledPortion = RtfText.text();
      Boolean styled = false;
      if (tagStyle == null) {
         return styledPortion;
      }
      if (tagStyle == styles.italic) {
         styledPortion = RtfText.italic(text);
         styled = true;
      }
      if (tagStyle == styles.bold) {
         styledPortion = RtfText.bold(text);
         styled = true;
      }
      if (tagStyle == styles.underlined) {
         styledPortion = RtfText.underline(text);

         styled = true;
      }
      if (tagStyle == styles.remove) {
         styledPortion = RtfText.text("");
         styled = true;
      }
      if (tagStyle == styles.none) {
         styledPortion = RtfText.text(text);
         styled = true;
      }
      if (tagStyle == styles.paragraph) {
         styledPortion = RtfText.text("\n" + text);
         styled = true;
      }
      if (tagStyle == styles.superscript) {
         styledPortion = RtfText.superscript(text);
         styled = true;
      }

      if (!styled) {

         System.out.print("Unknown style, using default non styled text!\n");
         styledPortion = RtfText.text(text);
      }
      return styledPortion;
   }

   public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException {
      BufferedReader f = new BufferedReader(new FileReader(new File("/usr/web/test.xml")));
      FileWriter w = new FileWriter(new File("/usr/web/filtered2.txt"));
      String txt = "";
      while (f.ready()) {
         txt += f.readLine();

      }
      txt = txt.replaceAll(" +", " ");
      // System.out.print(txt);
      TagFilter filter = new TagFilter(txt);
      String[] tags = filter.getTags();
      for (int i = 0; i < tags.length; i++) {
         System.out.print(tags[i] + "\n");
         if (tags[i].compareTo("p") == 0) {
            tags[i] = "";
         }
      }

      //String tmp=filter.stripTags(tags);
      //w.append(tmp);
      //filter=new TagFilter(tmp);
      String[] o = new String[]{"p"};
      filter.replaceTagsWithPDFEncoding(new String[]{"p", "note"}, new styles[]{styles.italic, styles.bold}, new FileOutputStream(new File("/usr/test.pdf")));

   }
   private static final Logger LOG = Logger.getLogger(TagFilter.class.getName());
}
