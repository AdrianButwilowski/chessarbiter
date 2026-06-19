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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "\"TournamentRegistration\"")
public class TournamentRegistration extends AbstractTimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"tournamentId\"", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"")
    private User user;

    @Column(name = "\"firstName\"", nullable = false)
    private String firstName;

    @Column(name = "\"lastName\"", nullable = false)
    private String lastName;

    @Column(name = "\"email\"", nullable = false)
    private String email;

    @Column(name = "\"clubOrCity\"", nullable = false)
    private String clubOrCity;

    @Column(name = "\"federation\"")
    private String federation;

    @Column(name = "\"rating\"", nullable = false)
    private Integer rating;

    @Column(name = "\"chessCategory\"", nullable = false)
    private String chessCategory = "NONE";

    @Column(name = "\"phoneNumber\"")
    private String phoneNumber;

    @Column(name = "\"birthYear\"")
    private Integer birthYear;

    @Column(name = "\"notes\"")
    private String notes;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"status\"", nullable = false, columnDefinition = "\"RegistrationStatus\"")
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "\"seedNumber\"")
    private Integer seedNumber;

    @Column(name = "\"startNumber\"")
    private Integer startNumber;

    @Column(name = "\"finalRank\"")
    private Integer finalRank;

    @OneToMany(mappedBy = "whiteRegistration")
    private List<Game> whiteGames = new ArrayList<>();

    @OneToMany(mappedBy = "blackRegistration")
    private List<Game> blackGames = new ArrayList<>();

    @OneToOne(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private TournamentStanding standing;
}
