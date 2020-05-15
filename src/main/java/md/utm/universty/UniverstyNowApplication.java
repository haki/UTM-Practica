package md.utm.universty;

import lombok.AllArgsConstructor;
import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import md.utm.universty.repository.UserRepository;
import md.utm.universty.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

@AllArgsConstructor
@SpringBootApplication
public class UniverstyNowApplication implements CommandLineRunner {
    private final UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(UniverstyNowApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Optional<User> optionalUser = userService.findUserByUserRole(UserRole.Admin);

        if (optionalUser.isEmpty()) {
            User user = new User("hakanmeral99@gmail.com", UserRole.Admin);
            userService.signUpUser(user);
        }
    }
}
