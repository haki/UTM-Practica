package md.utm.universty;

import lombok.AllArgsConstructor;
import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import md.utm.universty.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@AllArgsConstructor
@SpringBootApplication
public class UniverstyNowApplication {
    private final UserRepository userRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void main(String[] args) {
        SpringApplication.run(UniverstyNowApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                String hashedPassword = bCryptPasswordEncoder.encode("123");
                User user = new User("Hakan", "Meral", "hakanmeral99@gmail.com", hashedPassword, UserRole.Admin);
                userRepository.save(user);
            }
        };
    }
}
