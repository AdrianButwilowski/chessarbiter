package pl.chessarbiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.repository.TournamentRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepo;

    public List<Tournament> findAll() {
        return tournamentRepo.findAll();
    }

    public Tournament getBySlug(String slug) {
        return tournamentRepo.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new pl.chessarbiter.exception.NotFoundException("Tournament not found"));
    }
}
