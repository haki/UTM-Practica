package md.utm.universty.controller;

import lombok.AllArgsConstructor;
import md.utm.universty.dto.*;
import md.utm.universty.model.ConfirmationToken;
import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import md.utm.universty.service.ConfirmationTokenService;
import md.utm.universty.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
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
    private final HomeController homeController;

    @ModelAttribute("forgotPasswordForm")
    public PasswordForgotDto forgotPasswordDto() {
        return new PasswordForgotDto();
    }

    @ModelAttribute("passwordResetForm")
    public PasswordResetDto passwordReset() {
        return new PasswordResetDto();
    }

    @ModelAttribute("registerConfirmForm")
    public RegisterConfirmDto registerConfirm() {
        return new RegisterConfirmDto();
    }

    @ModelAttribute("sendRegisterMail")
    public SendRegisterMail registerMail() {
        return new SendRegisterMail();
    }

    @ModelAttribute("updateProfileForm")
    public UpdateProfileDto updateProfile() {
        return new UpdateProfileDto();
    }

    @GetMapping("/accounts/login")
    public ModelAndView signIn() {
        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("user/login");

        return modelAndView;
    }

    @GetMapping("/accounts/register/confirm")
    ModelAndView confirmMail(@RequestParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView();
        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(token);
        ConfirmationToken confirmationToken = optionalConfirmationToken.get();

        if (optionalConfirmationToken.isPresent()) {
            modelAndView.addObject("token", token);
            modelAndView.setViewName("user/register");
        } else {
            modelAndView.setViewName("user/login");
        }

        return modelAndView;
    }

    @PostMapping("/accounts/register/confirm")
    @Transactional
    public String confirmAndSignUpUser(@ModelAttribute("registerConfirmForm") @Valid RegisterConfirmDto form, RedirectAttributes redirectAttributes, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(BindingResult.class.getName() + ".registerConfirmForm", bindingResult);
            redirectAttributes.addFlashAttribute("registerConfirmForm", form);

            return "redirect:/accounts/login";
        }

        Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(form.getToken());
        if (optionalConfirmationToken.isPresent()) {
            ConfirmationToken confirmationToken = optionalConfirmationToken.get();
            User user = confirmationToken.getUser();

            userService.confirmUser(confirmationToken, form);

            return "redirect:/accounts/login?confirm=true";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Token nu este exist");

        return "redirect:/accounts/password/reset/confirm?token=" + form.getToken();
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

    @GetMapping("/accounts/add/user")
    public ModelAndView userCreatingPage() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();

        if (user.getUserRole() == UserRole.Admin || user.getUserRole() == UserRole.Professor) {
            modelAndView.addObject("user", user);
            modelAndView.setViewName("user/addUser");
        } else {
            return homeController.index();
        }

        return modelAndView;
    }

    @PostMapping("/accounts/add/user")
    public ModelAndView sendRegistrationMail(@ModelAttribute("sendRegisterMailForm") SendRegisterMail form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView();

        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();
        Optional<User> optionalnewUser = userService.findUserByEmail(form.getEmail());
        Long reference = user.getId();

        modelAndView.addObject("successMessage", "Success");
        modelAndView.addObject("user", user);

        modelAndView.setViewName("user/addUser");
        if (optionalnewUser.isEmpty()) {
            if (form.getUserRole() == 0 && user.getUserRole() == UserRole.Admin) {
                User newUser = new User(form.getEmail(), UserRole.Admin, reference);
                userService.signUpUser(newUser);
            } else if (form.getUserRole() == 1 && user.getUserRole() == UserRole.Admin) {
                User newUser = new User(form.getEmail(), UserRole.Professor, reference);
                userService.signUpUser(newUser);
            } else if (form.getUserRole() == 2 && (user.getUserRole() == UserRole.Admin || user.getUserRole() == UserRole.Professor)) {
                User newUser = new User(form.getEmail(), UserRole.Student, reference);
                userService.signUpUser(newUser);
            }
        } else {
            modelAndView.setViewName("index");
        }

        return modelAndView;
    }

    @GetMapping("/accounts/list/user")
    public ModelAndView listUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView();
        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();
        modelAndView.addObject("user", user);

        if (user.getUserRole() == UserRole.Admin || user.getUserRole() == UserRole.Professor) {
            modelAndView.addObject("allUsers", userService.listAllUsers());
            modelAndView.setViewName("user/listUser");
        } else {
            modelAndView.setViewName("index");
        }

        return modelAndView;
    }

    @GetMapping("/accounts/delete/user")
    public String deleteUserbyId(@RequestParam("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();
        Optional<User> optionalUser1 = userService.findUserById(id);

        if (user.getUserRole() != UserRole.Student && user.getId() != id && optionalUser1.isPresent()) {
            User deleteUser = optionalUser1.get();
            if (user.getUserRole() == UserRole.Professor && user.getId() == deleteUser.getReference()) {
                userService.deleteUserById(id);
            } else if (user.getUserRole() == UserRole.Admin) {
                userService.deleteUserById(id);
            }

            return "redirect:/accounts/list/user";
        }
        return "redirect:";
    }

    @GetMapping("/accounts/profile")
    public ModelAndView userProfile(@RequestParam("id") Long id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();

        if (user.getId() == id) {
            modelAndView.addObject("user", user);
            modelAndView.setViewName("user/profile");
        } else {
            modelAndView.addObject("user", user);
            modelAndView.setViewName("index");
        }

        return modelAndView;
    }

    @PostMapping("/accounts/profile/update")
    public ModelAndView updateProfile(@ModelAttribute("updateProfile") @Valid UpdateProfileDto form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ModelAndView modelAndView = new ModelAndView();
        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();

        modelAndView.addObject("user", user);
        modelAndView.setViewName("index");

        Optional<User> optionalUser1 = userService.findUserByEmail(form.getEmail());
        if (optionalUser1.isPresent()) {
            User updateUser = optionalUser1.get();
            if (user.getId() == updateUser.getId()) {
                userService.updateUser(user, form);
            }
        }

        return modelAndView;
    }
}
