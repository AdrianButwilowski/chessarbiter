package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"Tournament\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tournament {
    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String slug;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private TournamentType type;
    @Enumerated(EnumType.STRING)
    private TimeControlType timeControlType;
    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.DRAFT;
    private Integer roundsCount;
    private Instant startDate;
    private Instant endDate;
    private String location;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy_id")
    private User createdBy;
    private Instant createdAt;
    private Instant deletedAt;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Round> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TournamentRegistration> registrations = new ArrayList<>();
}
