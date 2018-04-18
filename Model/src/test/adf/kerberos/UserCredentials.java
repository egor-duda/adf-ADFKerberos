package test.adf.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;

import java.security.PrivilegedAction;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.HashMap;

import java.util.Properties;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import oracle.jbo.JboException;

import oracle.jdbc.OracleDriver;

import oracle.net.ano.AnoServices;

public class UserCredentials implements CallbackHandler{
    
    private static final String url ="jdbc:oracle:thin:@orcl.or.pvt:1521:orcl";

    
    private String userName;
    private String password;
    
    public UserCredentials() {
        super();
    }
    
    private static Object resolveExpression(String expression) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) return null;
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        return valueExp.getValue(elContext);
    }
        
    public static UserCredentials getCurrentInstance() {
        return (UserCredentials)resolveExpression("#{data.UserCredentials.dataProvider}");
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return null;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)callbacks[i];
                pc.setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
    
    public void checkConnection () throws LoginException, SQLException {
        Subject specificSubject = new Subject();
        
        // This first part isn't really meaningful to the sake of this demo. In
        // a real world scenario, you have a valid "specificSubject" Subject that
        // represents a web user that has valid Kerberos credentials.
        Krb5LoginModule krb5Module = new Krb5LoginModule();
        HashMap<String,String> sharedState = new HashMap<>();
        HashMap<String,String> options = new HashMap<>();
        options.put("doNotPrompt","false");
        options.put("useTicketCache","false");
        options.put("principal", userName + "@TLS.PVT");
        
        krb5Module.initialize(specificSubject,this,sharedState,options);
        boolean retLogin = krb5Module.login();
        krb5Module.commit();
        if(!retLogin)
          throw new JboException("Kerberos5 adaptor couldn't retrieve credentials (TGT) from the cache"); 
          
        Connection conn = Subject.doAs(specificSubject, new PrivilegedAction<Connection>() {
            @Override public Connection run() {
                Properties prop = new Properties();
                prop.setProperty(AnoServices.AUTHENTICATION_PROPERTY_SERVICES, 
                                 "(" + AnoServices.AUTHENTICATION_KERBEROS5 + ")");
                try {
                    OracleDriver driver = new OracleDriver();
                    return driver.connect(url, prop);
                } catch (Exception e) {
                    throw new JboException (e);
                }
              }
            });
        conn.close();
    }
}
