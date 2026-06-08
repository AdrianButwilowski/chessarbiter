package pl.chessarbiter.model;

public class TournamentOption {
    private final String id;
    private final String title;
    private final String city;
    private final String location;
    private final String dateLabel;
    private final int activeRegistrationCount;
    private final Integer maxPlayers;

    public TournamentOption(
            String id,
            String title,
            String city,
            String location,
            String dateLabel,
            int activeRegistrationCount,
            Integer maxPlayers
    ) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.location = location;
        this.dateLabel = dateLabel;
        this.activeRegistrationCount = activeRegistrationCount;
        this.maxPlayers = maxPlayers;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public String getLocation() {
        return location;
    }

    public String getDateLabel() {
        return dateLabel;
    }

    public int getActiveRegistrationCount() {
        return activeRegistrationCount;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }
}
