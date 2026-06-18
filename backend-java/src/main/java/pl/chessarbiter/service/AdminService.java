package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.dto.admin.*;
import pl.chessarbiter.entity.UserRole;
import pl.chessarbiter.repository.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepo;
    private final TournamentRepository tournamentRepo;
    private final TournamentRegistrationRepository registrationRepo;

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
            userRepo.count(),
            userRepo.countByRole(UserRole.ARBITER),
            userRepo.countByRole(UserRole.PLAYER),
            tournamentRepo.count(),
            registrationRepo.count()
        );
    }

    public List<AdminUserResponse> listUsers() {
        return userRepo.findAll().stream().map(AdminUserResponse::from).toList();
    }
}
