
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
    
    /**
     * The default mail server used for sending e-mails.  This value is,
     * by default, read from the file {@see version.properties}.
     * 
     * @var String
     */
    String mailServer = "";
    
    /**
     * The default "from" address used when sending e-mails.  This value is,
     * by default, read from the file {@see version.properties}.
     * 
     * @var String
     */
    String mailFrom = "";
    
    
    public mailer()
    {
        this.mailServer = Folio.getRbTok("MAIL_SERVER");
        this.mailFrom = Folio.getRbTok("MAIL_FROM");
    }
    
    
    /**
     * Returns the current date using an arbitrary, hard-coded format.
     * The format is close to ISO 8601, but it uses slashes, not hyphens
     * ("YYYY/MM/DD HH:mm:ss").
     * 
     * @return the current date
     */
    public String getDate()
    {
        // @TODO:  Move this format to config file.
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    
    /**
     * Sends an e-mail.  For use, e.g., when an error has occurred.
     * 
     * @param to
     * @param subject
     * @param messageBody
     * 
     * @throws MessagingException
     * @throws AddressException 
     */
    public void sendMail(String to, String subject, String messageBody
    ) throws MessagingException, AddressException
    {
        this.sendMail(this.mailServer, this.mailFrom, to, subject, messageBody);
     }
    
    
    /**
     * Sends an e-mail.  For use, e.g., when an error has occurred.
     * 
     * @param mailServer
     * @param from
     * @param to
     * @param subject
     * @param messageBody
     * 
     * @throws MessagingException
     * @throws AddressException 
     */
    public void sendMail(String mailServer, String from, String to,
                         String subject, String messageBody
    ) throws MessagingException, AddressException
    {
        
        // Setup mail server
        if (mailServer == null) {
            // BOZO:  This will never be used, right?  (Since Java prevents nulls here.)
            mailServer = this.mailServer;
        }
        Properties props = System.getProperties();
        props.put("mail.smtp.host", mailServer);
        
        // Get a mail session
        Session session = Session.getDefaultInstance(props, null);
        
        // Define a new mail message
        Message message = new MimeMessage(session);
        
        // Set message sender
        if (from == null) {
            // BOZO:  This will never be used, right?  (Since Java prevents nulls here.)
            from = this.mailFrom;
        }
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
    
    
    /**
     * (TODO:  Complete.)
     * 
     * @param addresses
     * @return
     * @throws AddressException 
     */
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
