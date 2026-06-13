package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"Game\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Game {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;
    private Integer boardNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "whiteRegistration_id")
    private TournamentRegistration white;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blackRegistration_id")
    private TournamentRegistration black;
    @Enumerated(EnumType.STRING)
    private GameResult result = GameResult.NOT_PLAYED;
}
