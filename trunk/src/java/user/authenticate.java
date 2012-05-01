package user;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 *
 * Servlet to handle logins via openid
 */
public class authenticate extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException, ConsumerException, DiscoveryException, MessageException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

           try {

            HttpSession session=request.getSession();
            ConsumerManager manager;
            if(session.getAttribute("manager")==null)
            {
            manager = new ConsumerManager();
            session.setAttribute("comsumerManager", manager);
            }
            else
            {
                manager=(ConsumerManager)session.getAttribute("manager");
            }

            
            //Try to authenticate them
             // extract the parameters from the authentication response
    // (which comes in as a HTTP request from the OpenID provider)
    ParameterList openidResp = new ParameterList(request.getParameterMap());

    // retrieve the previously stored discovery information
    DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("discovered");

    // extract the receiving URL from the HTTP request
    StringBuffer receivingURL = request.getRequestURL();
    String queryString = request.getQueryString();
    if (queryString != null && queryString.length() > 0)
        receivingURL.append("?").append(request.getQueryString());

    // verify the response
    VerificationResult verification = null;
     Identifier verified;
            try {

                verification = manager.verify(receivingURL.toString(), openidResp, discovered);



    // examine the verification result and extract the verified identifier

    verified= verification.getVerifiedId();
    } catch (Exception ex) {
                verified=null;
                out.print("Open ID authentication failed. The provider sent bad information back.");
            }

    if (verified != null && verification!=null)
    {
        AuthSuccess authSuccess =(AuthSuccess) verification.getAuthResponse();
       if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    List emails = fetchResp.getAttributeValues("email");
                    List names = fetchResp.getAttributeValues("namePerson");
                    String email = (String) emails.get(0);
                    String name=(String)names.get(0);
                    if(name==null)
                        name="new new";
                    session.setAttribute("OpenID", email);
                try {
                    user.User thisuser = new user.User(email, true);
                    //if this is a person who has never visited the site before, create a new account for them
                    if(thisuser==null || thisuser.getUID()==-1 || thisuser.getUID()==0)
                    {
                        thisuser=new user.User("new", "member", name, "nopass",email);
                    }
                    session.setAttribute("UID", thisuser.getUID());
                    if(session.getAttribute("ref")!=null)
                    {
                        String tmp=(String)session.getAttribute("ref");
                        session.setAttribute("ref", null);
                        if(tmp.contains("landing") || !tmp.contains(".jsp"))
                            response.sendRedirect("ToC.jsp");
                        else
                        response.sendRedirect(tmp);
                    }
                    else
                    response.sendRedirect("ToC.jsp");
                } catch (SQLException ex) {
                    Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
        

        //out.print("Authenticated as "+ verified.getIdentifier());
        
    }
    else
    {

            //If that failed, send them to get authenticated.

            String userSuppliedString="";
            if(request.getParameter("use")!=null)
            {
                if (request.getParameter("use").compareTo("yahoo")==0)
                {
                    userSuppliedString="http://yahoo.com/";
                }
                if (request.getParameter("use").compareTo("google")==0)
                {
                    userSuppliedString="https://www.google.com/accounts/o8/id";
                }
            }
            
        String returnURL = "http://normananonymous.org/ENAP/authenticate";
        // perform discovery on the user-supplied identifier
        if(userSuppliedString.length()>1)
        {
    List discoveries = manager.discover(userSuppliedString);

    // attempt to associate with the OpenID provider
    // and retrieve one service endpoint for authentication
    discovered = manager.associate(discoveries);

    // store the discovery information in the user's session for later use
    // leave out for stateless operation / if there is no session
    session.setAttribute("discovered", discovered);

    // obtain a AuthRequest message to be sent to the OpenID provider
    AuthRequest authReq = manager.authenticate(discovered, returnURL);
    FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email",
                    // attribute alias
                    "http://schema.openid.net/contact/email",   // type URI
                    true);
            fetch.addAttribute("namePerson",
                    // attribute alias
                    "http://schema.openid.net/contact/namePerson",   // type URI
                    true);  // required

            // attach the extension to the authentication request
            authReq.addExtension(fetch);
    response.sendRedirect(authReq.getDestinationUrl(true));
    }
    }
  }
       finally
       {
            out.close();
       }

    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (MessageException ex) {
                Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ConsumerException ex) {
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DiscoveryException ex) {
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {
            try {
                processRequest(request, response);
            } catch (MessageException ex) {
                Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ConsumerException ex) {
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DiscoveryException ex) {
            Logger.getLogger(authenticate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
