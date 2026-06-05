package org.example.web3.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import org.example.web3.Messages;

@FacesConverter("doubleConverter")
public class DoubleConverter implements Converter<Double> {

    @Override
    public Double getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.replace(',', '.');

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    Messages.get("error.number.invalid"),
                    Messages.get("error.number.details"));
            throw new ConverterException(msg);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Double value) {
        if (value == null) {
            return "";
        }
        return value.toString().replace('.', ',');
    }
}