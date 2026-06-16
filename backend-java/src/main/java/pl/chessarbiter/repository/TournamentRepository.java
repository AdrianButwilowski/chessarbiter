package pl.chessarbiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, String> {
    Optional<Tournament> findBySlugAndDeletedAtIsNull(String slug);
    Optional<Tournament> findByIdAndDeletedAtIsNull(String id);
    List<Tournament> findByDeletedAtIsNullAndStatusInOrderByStartDateAscCreatedAtDesc(Collection<TournamentStatus> statuses);
    List<Tournament> findByDeletedAtIsNullOrderByStartDateAscCreatedAtDesc();
    List<Tournament> findByCreatedBy_IdAndDeletedAtIsNullOrderByStartDateAscCreatedAtDesc(String createdById);
    boolean existsByCreatedBy_IdAndDeletedAtIsNull(String createdById);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, String id);
}
