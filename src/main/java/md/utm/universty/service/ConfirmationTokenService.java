package md.utm.universty.service;

import lombok.AllArgsConstructor;
import md.utm.universty.model.ConfirmationToken;
import md.utm.universty.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationTokenService {
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public void save(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    public Optional<ConfirmationToken> findConfirmationTokenByToken(String token) {
        return confirmationTokenRepository.findConfirmationTokenByConfirmationToken(token);
    }

    public void deleteConfirmationToken(Long id) {
        confirmationTokenRepository.deleteById(id);
    }

    public Optional<ConfirmationToken> findConfirmationTokenByUser(Long id) {
        return confirmationTokenRepository.findConfirmationTokenByUser(id);
    }
}
