package ma.ensaj.orderservice.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class CheckoutValidationException extends RuntimeException {
    private final List<String> validationErrors;
    
    public CheckoutValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    public CheckoutValidationException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }
}