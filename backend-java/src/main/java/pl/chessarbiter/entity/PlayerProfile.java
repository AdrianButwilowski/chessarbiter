package pl.chessarbiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "\"PlayerProfile\"")
public class PlayerProfile extends AbstractTimestampedEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"userId\"", nullable = false, unique = true)
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

    @Column(name = "\"classicalRating\"")
    private Integer classicalRating;

    @Column(name = "\"rapidRating\"")
    private Integer rapidRating;

    @Column(name = "\"blitzRating\"")
    private Integer blitzRating;

    @Column(name = "\"chessCategory\"", nullable = false)
    private String chessCategory = "NONE";

    @Column(name = "\"phoneNumber\"")
    private String phoneNumber;

    @Column(name = "\"birthYear\"")
    private Integer birthYear;
}
