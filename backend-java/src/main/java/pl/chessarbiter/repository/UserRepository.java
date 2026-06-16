package pl.chessarbiter.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByIdAndDeletedAtIsNull(String id);
    List<User> findByDeletedAtIsNullOrderByRoleAscCreatedAtDesc();
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(UserRole role);
    long countByRoleAndDeletedAtIsNull(UserRole role);
}
