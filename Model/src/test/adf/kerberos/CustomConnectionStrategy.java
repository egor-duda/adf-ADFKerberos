package test.adf.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;

import java.security.PrivilegedAction;

import java.util.HashMap;

import java.util.Properties;

import javax.security.auth.Subject;

import oracle.jbo.ApplicationModule;
import oracle.jbo.JboException;
import oracle.jbo.common.ampool.DefaultConnectionStrategy;
import oracle.jbo.common.ampool.EnvInfoProvider;
import oracle.jbo.common.ampool.SessionCookie;

import oracle.net.ano.AnoServices;

public class CustomConnectionStrategy extends DefaultConnectionStrategy {
    public CustomConnectionStrategy() {
        super();
    }

    @Override
    public void connect(ApplicationModule applicationModule, SessionCookie sessionCookie,
                        EnvInfoProvider envInfoProvider) {
        
        UserCredentials userCredentials = UserCredentials.getCurrentInstance();
        Subject specificSubject = new Subject();
          
        Krb5LoginModule krb5Module = new Krb5LoginModule();
        HashMap<String,String> sharedState = new HashMap<>();
        HashMap<String,String> options = new HashMap<>();
        options.put("doNotPrompt","false");
        options.put("useTicketCache","false");
        options.put("principal",userCredentials.getPrincipal());
        krb5Module.initialize(specificSubject,userCredentials,sharedState,options);
        try {
            boolean retLogin = krb5Module.login();
            if(!retLogin)
                throw new JboException("Kerberos5 adaptor couldn't retrieve credentials (TGT) from the cache"); 
            krb5Module.commit();
        } catch (Exception e) {
            throw new JboException (e);
        }
        final ApplicationModule localApplicationModule = applicationModule;
        final SessionCookie localSessionCookie = sessionCookie;
        final EnvInfoProvider localEnvInfoProvider = envInfoProvider;
        Subject.doAs(specificSubject, new PrivilegedAction<String>() {
            public String run() {
                superConnect(localApplicationModule, localSessionCookie, localEnvInfoProvider);
                return null;
            }
        });
        userCredentials.setLoggedIn(true);
    }
    
    private void superConnect (ApplicationModule applicationModule, SessionCookie sessionCookie,
                        EnvInfoProvider envInfoProvider) {
        Properties prop = sessionCookie.getMessageContext().getJDBCProperties();
        prop.setProperty(AnoServices.AUTHENTICATION_PROPERTY_SERVICES, 
                             "(" + AnoServices.AUTHENTICATION_KERBEROS5 + ")");
        prop.remove ("user");
        super.connect(applicationModule, sessionCookie, envInfoProvider);
    }

    public void disconnect(ApplicationModule applicationModule, boolean force, SessionCookie sessionCookie) {
        super.disconnect(applicationModule, force, sessionCookie);
        // UserCredentials.getCurrentInstance().setLoggedIn(false);
    }

    public void reconnect(ApplicationModule applicationModule, SessionCookie sessionCookie,
                          EnvInfoProvider envInfoProvider) {
        super.reconnect(applicationModule, sessionCookie, envInfoProvider);
    }
}
