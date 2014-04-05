package ch.test.entityCollections;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;  
import javax.faces.context.FacesContext;  
import javax.faces.event.ActionEvent;  
import javax.inject.Named;

/**
 * Session Bean implementation class GrowlBean
 */
@Named
@Stateless
@LocalBean
public class GrowlBean {  
  
    private String text;  
      
    public String getText() {  
        return text;  
    }  
    public void setText(String text) {  
        this.text = text;  
    }  
  
    public void save(ActionEvent actionEvent) {  
        FacesContext context = FacesContext.getCurrentInstance();  
          
        context.addMessage(null, new FacesMessage("Successful", "Hello " + text));  
        context.addMessage(null, new FacesMessage("Second Message", "Additional Info Here..."));  
    }  
}  