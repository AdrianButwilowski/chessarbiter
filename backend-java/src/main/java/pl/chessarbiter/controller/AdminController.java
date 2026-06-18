package pl.chessarbiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.chessarbiter.dto.admin.*;
import pl.chessarbiter.service.AdminService;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/stats")
    public AdminStatsResponse stats() { return adminService.getStats(); }

    @GetMapping("/users")
    public List<AdminUserResponse> users() { return adminService.listUsers(); }
}
