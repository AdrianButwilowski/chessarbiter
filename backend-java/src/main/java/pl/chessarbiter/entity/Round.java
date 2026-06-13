package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"Round\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Round {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    private Integer roundNumber;
    @Enumerated(EnumType.STRING)
    private RoundStatus status = RoundStatus.NOT_STARTED;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Game> games = new ArrayList<>();
}
