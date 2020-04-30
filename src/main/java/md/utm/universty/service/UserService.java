package md.utm.universty.service;

import lombok.AllArgsConstructor;
import md.utm.universty.model.ConfirmationToken;
import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import md.utm.universty.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final Optional<User> optionalUser = userRepository.findUserByEmail(email);

        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new UsernameNotFoundException(MessageFormat.format("User cu email {0} nu a fost gasit.", email));
        }
    }

    public void signUpUser(User user) {
        final String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        final User createdUser = userRepository.save(user);
        final ConfirmationToken confirmationToken = new ConfirmationToken(user);

        confirmationTokenService.save(confirmationToken);
        sendConfirmationMail(user.getEmail(), confirmationToken.getConfirmationToken());
    }

    private void sendConfirmationMail(String email, String token) {
        final SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(email);
        mailMessage.setSubject("Inregistrarea la UTM Now");
        mailMessage.setFrom("hakan.meral@isa.utm.md");
        mailMessage.setText("Va rugam sa urmati acest link pentru a confirma procedura de inregistrarea: \n\n" + "http:/localhost:8080/accounts/register/confirm?token=" + token);

        emailSenderService.sendEmail(mailMessage);
    }

    public Optional<User> findUserByUserRole(UserRole userRole) {
        return userRepository.findUserByUserRole(userRole);
    }

    public void confirmUser(ConfirmationToken confirmationToken) {
        final User user = confirmationToken.getUser();

        user.setEnabled(true);
        userRepository.save(user);
        confirmationTokenService.deleteConfirmationToken(confirmationToken.getId());
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public void sendResetToken(User user) {
        final ConfirmationToken confirmationToken = new ConfirmationToken(user);
        final SimpleMailMessage mailMessage = new SimpleMailMessage();

        confirmationTokenService.save(confirmationToken);

        mailMessage.setTo(user.getEmail());
        mailMessage.setFrom("hakan.meral@isa.utm.md");
        mailMessage.setSubject("Recuperare parola");
        mailMessage.setText("Va rugam sa urmati acest link pentru a confirma procedura de recuperare a parolei dvs.:\n\n" + "http://localhost:8080/accounts/password/reset/confirm?token=" + confirmationToken.getConfirmationToken());

        emailSenderService.sendEmail(mailMessage);
    }

    public void updatePassword(User user, ConfirmationToken token, String password) {
        final String encryptedPassword = bCryptPasswordEncoder.encode(password);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
        confirmationTokenService.deleteConfirmationToken(token.getId());
    }
}
