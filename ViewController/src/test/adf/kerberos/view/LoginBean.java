package test.adf.kerberos.view;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import test.adf.kerberos.UserCredentials;

public class LoginBean {
    public LoginBean() {
    }

    public String action() {
        try {
            UserCredentials.getCurrentInstance().checkConnection();
            return "ok";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage (FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
            return null;
        }
    }
}
