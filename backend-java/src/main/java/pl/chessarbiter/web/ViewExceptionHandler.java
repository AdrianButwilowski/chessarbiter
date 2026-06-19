package pl.chessarbiter.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.chessarbiter.exception.ApiException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "pl.chessarbiter.web")
public class ViewExceptionHandler {

    @ExceptionHandler(ApiException.class)
    String handleApiException(ApiException exception, HttpServletResponse response, Model model) {
        response.setStatus(exception.getStatus().value());
        model.addAttribute("title", "Nie udało się otworzyć strony");
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
