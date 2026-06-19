package pl.chessarbiter.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.chessarbiter.dto.admin.AdminStatsResponse;
import pl.chessarbiter.dto.admin.AdminUserResponse;
import pl.chessarbiter.dto.admin.ChangeUserRoleRequest;
import pl.chessarbiter.dto.common.MessageResponse;
import pl.chessarbiter.dto.tournament.TournamentSummaryResponse;
import pl.chessarbiter.service.AdminService;
import pl.chessarbiter.service.TournamentService;
import pl.chessarbiter.security.CurrentUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final TournamentService tournamentService;

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() {
        return adminService.users()
            .stream()
            .map(AdminUserResponse::from)
            .toList();
    }

    @PatchMapping("/users/{userId}/role")
    @Transactional
    public AdminUserResponse changeRole(@PathVariable String userId, @Valid @RequestBody ChangeUserRoleRequest request) {
        return AdminUserResponse.from(adminService.changeRole(userId, request.role()));
    }

    @DeleteMapping("/users/{userId}")
    public MessageResponse deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return new MessageResponse("User deleted.");
    }

    @GetMapping("/tournaments")
    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> tournaments() {
        return tournamentService.listManagedTournaments(CurrentUser.require())
            .stream()
            .map(TournamentSummaryResponse::from)
            .toList();
    }
}
