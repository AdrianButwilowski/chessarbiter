package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "\"User\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String email;
    private String passwordHash;
    private String name;
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.PLAYER;
    private Instant createdAt;
    private Instant deletedAt;
}
