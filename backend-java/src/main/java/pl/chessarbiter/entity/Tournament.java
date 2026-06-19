package pl.chessarbiter.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "\"Tournament\"")
public class Tournament extends AbstractTimestampedEntity {

    @Column(name = "\"title\"", nullable = false)
    private String title;

    @Column(name = "\"slug\"", nullable = false, unique = true)
    private String slug;

    @Column(name = "\"description\"", nullable = false)
    private String description;

    @Column(name = "\"location\"", nullable = false)
    private String location;

    @Column(name = "\"city\"", nullable = false)
    private String city;

    @Column(name = "\"startDate\"", nullable = false)
    private Instant startDate;

    @Column(name = "\"endDate\"")
    private Instant endDate;

    @Column(name = "\"registrationDeadline\"")
    private Instant registrationDeadline;

    @Column(name = "\"organizer\"", nullable = false)
    private String organizer;

    @Column(name = "\"contactEmail\"", nullable = false)
    private String contactEmail;

    @Column(name = "\"contactPhone\"")
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"tournamentType\"", nullable = false, columnDefinition = "\"TournamentType\"")
    private TournamentType tournamentType;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"timeControlType\"", nullable = false, columnDefinition = "\"TimeControlType\"")
    private TimeControlType timeControlType;

    @Column(name = "\"timeControlDescription\"", nullable = false)
    private String timeControlDescription;

    @Column(name = "\"rounds\"", nullable = false)
    private Integer rounds;

    @Column(name = "\"maxPlayers\"")
    private Integer maxPlayers;

    @Column(name = "\"entryFee\"")
    private String entryFee;

    @Column(name = "\"regulationsUrl\"")
    private String regulationsUrl;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"status\"", nullable = false, columnDefinition = "\"TournamentStatus\"")
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Column(name = "\"registrationOpen\"", nullable = false)
    private boolean registrationOpen;

    @Column(name = "\"allowPlayerCancellation\"", nullable = false)
    private boolean allowPlayerCancellation = true;

    @Column(name = "\"showRegisteredPlayers\"", nullable = false)
    private boolean showRegisteredPlayers = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"createdById\"", nullable = false)
    private User createdBy;

    @Column(name = "\"deletedAt\"")
    private Instant deletedAt;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentRegistration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> tournamentRounds = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentStanding> standings = new ArrayList<>();
}
