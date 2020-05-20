package md.utm.universty.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ErrorHandleController {
    @GetMapping("/error")
    public String redirectToHomeFromErrorPage() {
        return "redirect:";
    }
}
