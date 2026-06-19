package pl.chessarbiter.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "\"User\"")
public class User extends AbstractTimestampedEntity {

    @Column(name = "\"email\"", nullable = false, unique = true)
    private String email;

    @Column(name = "\"passwordHash\"", nullable = false)
    private String passwordHash;

    @Column(name = "\"name\"")
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"role\"", nullable = false, columnDefinition = "\"UserRole\"")
    private UserRole role = UserRole.PLAYER;

    @Column(name = "\"deletedAt\"")
    private Instant deletedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlayerProfile playerProfile;

    @OneToMany(mappedBy = "createdBy")
    private List<Tournament> tournaments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<TournamentRegistration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "resultEnteredBy")
    private List<Game> enteredResults = new ArrayList<>();
}
