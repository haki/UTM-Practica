package md.utm.universty.controller;

import lombok.AllArgsConstructor;
import md.utm.universty.model.ConfirmationToken;
import md.utm.universty.service.ConfirmationTokenService;
import md.utm.universty.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserController {
    private final ConfirmationTokenService confirmationTokenService;
    private final UserService userService;

    @GetMapping("/accounts/login")
    public ModelAndView signIn() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("user/login");

        return modelAndView;
    }

    @GetMapping("/accounts/register/confirm")
    String confirmMail(@RequestParam("token") String token) {
        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(token);

        optionalConfirmationToken.ifPresent(userService::confirmUser);

        return "redirect:/accounts/login?confirm=true";
    }
}
