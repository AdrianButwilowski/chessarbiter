package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.entity.*;
import pl.chessarbiter.repository.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoundService {
    private final RoundRepository roundRepo;
    private final GameRepository gameRepo;
    private final TournamentRegistrationRepository registrationRepo;

    public List<Round> generateRounds(Tournament tournament, List<TournamentRegistration> participants) {
        List<Round> rounds = new ArrayList<>();
        int totalRounds = tournament.getRoundsCount() != null ? tournament.getRoundsCount() : 5;
        for (int i = 1; i <= totalRounds; i++) {
            Round round = Round.builder()
                    .id(UUID.randomUUID().toString())
                    .tournament(tournament)
                    .roundNumber(i)
                    .status(RoundStatus.NOT_STARTED)
                    .build();
            rounds.add(round);
        }
        return roundRepo.saveAll(rounds);
    }
}
