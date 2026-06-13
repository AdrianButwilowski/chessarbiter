package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"TournamentStanding\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TournamentStanding {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private TournamentRegistration registration;
    private Double points;
    private Integer rank;
}
