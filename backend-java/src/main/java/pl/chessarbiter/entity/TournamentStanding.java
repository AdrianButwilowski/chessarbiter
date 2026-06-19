package pl.chessarbiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "\"TournamentStanding\"")
public class TournamentStanding {

    @Id
    @Column(name = "\"id\"", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"tournamentId\"", nullable = false)
    private Tournament tournament;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"registrationId\"", nullable = false, unique = true)
    private TournamentRegistration registration;

    @Column(name = "\"points\"", nullable = false)
    private Double points = 0.0;

    @Column(name = "\"gamesPlayed\"", nullable = false)
    private Integer gamesPlayed = 0;

    @Column(name = "\"wins\"", nullable = false)
    private Integer wins = 0;

    @Column(name = "\"draws\"", nullable = false)
    private Integer draws = 0;

    @Column(name = "\"losses\"", nullable = false)
    private Integer losses = 0;

    @Column(name = "\"forfeits\"", nullable = false)
    private Integer forfeits = 0;

    @Column(name = "\"buchholz\"")
    private Double buchholz;

    @Column(name = "\"medianBuchholz\"")
    private Double medianBuchholz;

    @Column(name = "\"sonnebornBerger\"")
    private Double sonnebornBerger;

    @Column(name = "\"progressiveScore\"")
    private Double progressiveScore;

    @Column(name = "\"directEncounter\"")
    private Double directEncounter;

    @Column(name = "\"rank\"", nullable = false)
    private Integer rank;

    @Column(name = "\"updatedAt\"", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
