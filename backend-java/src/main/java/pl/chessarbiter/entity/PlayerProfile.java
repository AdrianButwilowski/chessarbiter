package pl.chessarbiter.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "\"PlayerProfile\"")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerProfile {
    @Id
    private String id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String firstName;
    private String lastName;
    private String email;
    private String clubOrCity;
    private String federation;
    private Integer classicalRating;
    private Integer rapidRating;
    private Integer blitzRating;
    private String chessCategory;
    private String phoneNumber;
    private Integer birthYear;
    private String licenseNumber;
}
