package org.example.web3.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import org.example.web3.Messages;

@FacesValidator("yRangeValidator")
public class YRangeValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext ctx, UIComponent comp, Object value) throws ValidatorException {
        if (value == null) throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("error.y.required"), null));;

        final double y;
        try {
            y = Double.parseDouble(value.toString().trim());
        } catch (NumberFormatException ex) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("error.y.number"), null));
        }

        if (y < -5 || y > 5) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("error.y.range"), null));
        }
    }
}