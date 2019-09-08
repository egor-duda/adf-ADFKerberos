package test.adf.kerberos.view;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Logout extends HttpServlet {
    
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
    @SuppressWarnings("compatibility:-2195218796485537686")
    private static final long serialVersionUID = 5769325750725584796L;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    private void writeResponse (HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>Session ended</title></head>");
        out.println("<body>");
        out.println("<p style=\"text-align: center\">You have logged out. <a href=\"faces/login\">Log in again</a></p>");
        out.println("</body></html>");
        out.close();
    }
    
    private void logoutSession (HttpSession session) {
        if (session != null) {
            session.setAttribute("INVALIDATED", Boolean.TRUE);
            session.setMaxInactiveInterval(1);
            session.invalidate();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        writeResponse (response);
        logoutSession (request.getSession(false));
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        writeResponse (response);
        logoutSession (request.getSession(false));
    }
    
    public Logout() {
        super();
    }
}
