package pl.chessarbiter.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.chessarbiter.dto.admin.AdminStatsResponse;
import pl.chessarbiter.entity.User;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.exception.BadRequestException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentRepository;
import pl.chessarbiter.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
            userRepository.count(),
            userRepository.countByRoleAndDeletedAtIsNull(UserRole.ARBITER),
            userRepository.countByRoleAndDeletedAtIsNull(UserRole.PLAYER),
            tournamentRepository.count(),
            registrationRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public List<User> users() {
        return userRepository.findByDeletedAtIsNullOrderByRoleAscCreatedAtDesc();
    }

    @Transactional
    public User changeRole(String userId, UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new BadRequestException("Admin role cannot be assigned from API.");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("User not found."));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin role cannot be changed.");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new NotFoundException("User not found."));
        if (user.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Admin account cannot be deleted.");
        }
        if (tournamentRepository.existsByCreatedBy_IdAndDeletedAtIsNull(user.getId())) {
            throw new BadRequestException("User manages tournaments and cannot be deleted.");
        }

        user.setDeletedAt(Instant.now());
        user.setEmail("deleted-" + user.getId() + "@deleted.local");
        user.setName("Deleted user");
        user.setPasswordHash("");
        userRepository.save(user);
    }
}
