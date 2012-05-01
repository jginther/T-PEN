
package textdisplay;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
    import javax.mail.*;
 import javax.mail.internet.*;
 import javax.activation.*;
/**Sends mail when an error occurs

 */

public class mailer {
    public mailer()
    {
        
    }
    public String getDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
public void sendMail(String mailServer, String from, String to,
                             String subject, String messageBody
                             ) throws
MessagingException, AddressException
     {
         // Setup mail server
         Properties props = System.getProperties();
         props.put("mail.smtp.host", mailServer);

         // Get a mail session
         Session session = Session.getDefaultInstance(props, null);

         // Define a new mail message
         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
         
         //message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         message.addRecipients(Message.RecipientType.TO, unicodifyAddresses(to));
         message.setSubject(subject);

         // Create a message part to represent the body text
         BodyPart messageBodyPart = new MimeBodyPart();
         messageBodyPart.setText(messageBody);

         //use a MimeMultipart as we need to handle the file attachments
         Multipart multipart = new MimeMultipart();

         //add the message body to the mime message
         multipart.addBodyPart(messageBodyPart);



         // Put all message parts in the message
         message.setContent(multipart);

         // Send the message
         Transport.send(message);


     }
InternetAddress[] unicodifyAddresses(String addresses) throws AddressException {
    InternetAddress[] recips = InternetAddress.parse(java.net.IDN.toASCII(addresses), false);
    for(int i=0; i<recips.length; i++) {
                try {
                    recips[i] = new InternetAddress(recips[i].getAddress(), recips[i].getPersonal(), "utf-8");
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(mailer.class.getName()).log(Level.SEVERE, null, ex);
                }
      
    }
    return recips;
}
}
