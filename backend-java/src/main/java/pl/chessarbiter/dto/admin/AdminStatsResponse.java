package pl.chessarbiter.dto.admin;

public record AdminStatsResponse(
    long usersCount,
    long arbitersCount,
    long playersCount,
    long tournamentsCount,
    long registrationsCount
) {
}
