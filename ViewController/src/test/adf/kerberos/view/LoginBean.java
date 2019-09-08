package test.adf.kerberos.view;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import oracle.adf.model.BindingContext;

import oracle.adf.model.DataControlFrame;

import oracle.adf.model.binding.DCDataControl;

import oracle.jbo.JboException;

import test.adf.kerberos.UserCredentials;
import test.adf.kerberos.am.MainAppModuleImpl;

public class LoginBean {
    public LoginBean() {
    }
    
    public boolean isLoggedIn () {
        return UserCredentials.getCurrentInstance().isLoggedIn();
    }

    public String action() {
        try {
            UserCredentials userCredentials = UserCredentials.getCurrentInstance();
            userCredentials.checkConnection();
            return "ok";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage (FacesMessage.SEVERITY_FATAL, e.getMessage(), ""));
            return null;
        }
    }
    
    public String logout () {
        MainAppModuleImpl am = MainAppModuleImpl.getCurrentInstance();
        am.setReleaseLevel(MainAppModuleImpl.RELEASE_LEVEL_UNMANAGED);
        BindingContext context = BindingContext.getCurrent();
        String frameName = context.getCurrentDataControlFrame();
        DataControlFrame dcf = context.findDataControlFrame(frameName);
        DCDataControl dc = dcf.findDataControl("MainAppModuleDataControl");
        try {
            dc.endRequest(null);
        } catch (JboException e) {
            if ("JBO".equals(e.getProductCode()) && "30006".equals (e.getErrorCode())) {
                // ignore
            } else {
                throw e;
            }
        }
        return "ok";
    }
}
