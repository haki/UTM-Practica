package md.utm.universty.controller;

import lombok.AllArgsConstructor;
import md.utm.universty.dto.PasswordForgotDto;
import md.utm.universty.dto.PasswordResetDto;
import md.utm.universty.model.ConfirmationToken;
import md.utm.universty.model.User;
import md.utm.universty.service.ConfirmationTokenService;
import md.utm.universty.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserController {
    private final ConfirmationTokenService confirmationTokenService;
    private final UserService userService;

    @ModelAttribute("forgotPasswordForm")
    public PasswordForgotDto forgotPasswordDto() {
        return new PasswordForgotDto();
    }

    @ModelAttribute("passwordResetForm")
    public PasswordResetDto passwordReset() {
        return new PasswordResetDto();
    }

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

    @GetMapping("/accounts/password/reset")
    public ModelAndView forgotPasswordPage() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("user/forgotPassword");

        return modelAndView;
    }

    @PostMapping("/accounts/password/reset")
    public ModelAndView sendResetToken(@ModelAttribute("forgotPasswordForm") @Valid PasswordForgotDto form) {
        ModelAndView modelAndView = new ModelAndView();
        Optional<User> optionalUser = userService.findUserByEmail(form.getEmail());

        if (optionalUser.isEmpty()) {
            modelAndView.addObject("errorMessage", "Nu am putut gasi acest e-mail.");
            modelAndView.setViewName("user/forgotPassword");
        } else {
            userService.sendResetToken(optionalUser.get());
            modelAndView.addObject("successMessage", "Va rugam sa controlati posta.");
            modelAndView.setViewName("user/forgotPassword");
        }

        return modelAndView;
    }

    @GetMapping("/accounts/password/reset/confirm")
    public ModelAndView newPasswordPage(@RequestParam("token") String token, ModelAndView modelAndView) {
        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(token);

        if (optionalConfirmationToken.isPresent()) {
            ConfirmationToken confirmationToken = optionalConfirmationToken.get();
            modelAndView.addObject("token", confirmationToken.getConfirmationToken());
            modelAndView.setViewName("user/resetPassword");
        } else {
            modelAndView.addObject("errorMessage", "Token nu exista.");
            modelAndView.setViewName("user/forgotPassword");
        }

        return modelAndView;
    }

    @PostMapping("/accounts/password/reset/confirm")
    @Transactional
    public String handlePasswordReset(@ModelAttribute("passwordResetForm") @Valid PasswordResetDto form, RedirectAttributes redirectAttributes, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.class.getName() + ".passwordResetForm", bindingResult);
            redirectAttributes.addFlashAttribute("passwordResetForm", form);

            return "redirect:/accounts/password/reset/confirm?token=" + form.getToken();
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Parola si confirmare nu sunt egale");

            return "redirect:/accounts/password/reset/confirm?token=" + form.getToken();
        }

        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(form.getToken());
        if (optionalConfirmationToken.isPresent()) {
            ConfirmationToken confirmationToken = optionalConfirmationToken.get();
            User user = confirmationToken.getUser();

            userService.updatePassword(user, confirmationToken, form.getPassword());

            return "redirect:/accounts/login?reset=true";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Token nu este exist");

        return "redirect:/accounts/password/reset/confirm?token=" + form.getToken();
    }
}
