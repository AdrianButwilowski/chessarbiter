package pl.chessarbiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "\"Game\"")
public class Game extends AbstractTimestampedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"tournamentId\"", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"roundId\"", nullable = false)
    private Round round;

    @Column(name = "\"boardNumber\"", nullable = false)
    private Integer boardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"whiteRegistrationId\"")
    private TournamentRegistration whiteRegistration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"blackRegistrationId\"")
    private TournamentRegistration blackRegistration;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "\"result\"", nullable = false, columnDefinition = "\"GameResult\"")
    private GameResult result = GameResult.NOT_PLAYED;

    @Column(name = "\"whitePoints\"", nullable = false)
    private Double whitePoints = 0.0;

    @Column(name = "\"blackPoints\"", nullable = false)
    private Double blackPoints = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"resultEnteredById\"")
    private User resultEnteredBy;

    @Column(name = "\"resultEnteredAt\"")
    private Instant resultEnteredAt;
}
