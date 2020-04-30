package md.utm.universty.repository;

import md.utm.universty.model.User;
import md.utm.universty.model.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUserRole(UserRole userRole);
}
