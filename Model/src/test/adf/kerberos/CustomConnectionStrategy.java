package test.adf.kerberos;

import com.sun.security.auth.module.Krb5LoginModule;

import java.io.IOException;

import java.security.PrivilegedAction;

import java.util.HashMap;

import java.util.Properties;

import javax.security.auth.Subject;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import oracle.jbo.ApplicationModule;
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
        Subject specificSubject = new Subject();
          
        Krb5LoginModule krb5Module = new Krb5LoginModule();
        HashMap<String,String> sharedState = new HashMap<>();
        HashMap<String,String> options = new HashMap<>();
        options.put("doNotPrompt","false");
        options.put("useTicketCache","false");
        options.put("principal","deo-guest@TLS.PVT");
        krb5Module.initialize(specificSubject,new KrbCallbackHandler(),sharedState,options);
        try {
            boolean retLogin = krb5Module.login();
            if(!retLogin)
                throw new RuntimeException("Kerberos5 adaptor couldn't retrieve credentials (TGT) from the cache"); 
            krb5Module.commit();
        } catch (Exception e) {
            e.printStackTrace();
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
    }
    
    private void superConnect (ApplicationModule applicationModule, SessionCookie sessionCookie,
                        EnvInfoProvider envInfoProvider) {
        Properties prop = sessionCookie.getMessageContext().getJDBCProperties();
        prop.setProperty(AnoServices.AUTHENTICATION_PROPERTY_SERVICES, 
                             "(" + AnoServices.AUTHENTICATION_KERBEROS5 + ")");
        prop.remove ("user");
        prop.remove ("password");
        super.connect(applicationModule, sessionCookie, envInfoProvider);
    }
    
    
    private class KrbCallbackHandler implements CallbackHandler {
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            String password = "a1a2a3a#";
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback)callbacks[i];
                    System.out.println("set password");
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                }
            }
        }
    }
    
}
