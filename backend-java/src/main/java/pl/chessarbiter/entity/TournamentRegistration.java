package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "\"TournamentRegistration\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TournamentRegistration {
    @Id
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    private String firstName;
    private String lastName;
    private String email;
    private String clubOrCity;
    private String federation;
    private Integer rating;
    private String chessCategory;
    private String phoneNumber;
    private Integer birthYear;
    private String notes;
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;
    private Integer startNumber;
    private Instant createdAt;
}
