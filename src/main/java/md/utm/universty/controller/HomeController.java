package md.utm.universty.controller;

import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import md.utm.universty.service.UserService;
import org.dom4j.rule.Mode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class HomeController {
    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> optionalUser = userService.findUserByEmail(authentication.getName());
        User user = optionalUser.get();

        modelAndView.addObject("user", user);

        modelAndView.setViewName("index");

        return modelAndView;
    }
}
