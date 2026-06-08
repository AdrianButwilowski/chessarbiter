package pl.chessarbiter.model;

import java.time.LocalDateTime;

public class RegistrationEntry {
    private final String tournamentId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String clubOrCity;
    private final String federation;
    private final String licenseNumber;
    private final Integer rating;
    private final String chessCategory;
    private final String phoneNumber;
    private final Integer birthYear;
    private final String notes;
    private final LocalDateTime createdAt;

    public RegistrationEntry(
            String tournamentId,
            String firstName,
            String lastName,
            String email,
            String clubOrCity,
            String federation,
            String licenseNumber,
            Integer rating,
            String chessCategory,
            String phoneNumber,
            Integer birthYear,
            String notes
    ) {
        this.tournamentId = tournamentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.clubOrCity = clubOrCity;
        this.federation = federation;
        this.licenseNumber = licenseNumber;
        this.rating = rating;
        this.chessCategory = chessCategory;
        this.phoneNumber = phoneNumber;
        this.birthYear = birthYear;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getClubOrCity() {
        return clubOrCity;
    }

    public String getFederation() {
        return federation;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public Integer getRating() {
        return rating;
    }

    public String getChessCategory() {
        return chessCategory;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
