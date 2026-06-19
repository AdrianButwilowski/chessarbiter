package pl.chessarbiter.web;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.chessarbiter.dto.engine.RoundResponse;
import pl.chessarbiter.dto.engine.StandingResponse;
import pl.chessarbiter.dto.player.PlayerDetailResponse;
import pl.chessarbiter.dto.player.PlayerGameResponse;
import pl.chessarbiter.dto.registration.RegistrationResponse;
import pl.chessarbiter.entity.RegistrationStatus;
import pl.chessarbiter.dto.tournament.TournamentDetailResponse;
import pl.chessarbiter.entity.Tournament;
import pl.chessarbiter.entity.TournamentStatus;
import pl.chessarbiter.exception.ApiException;
import pl.chessarbiter.exception.NotFoundException;
import pl.chessarbiter.repository.RoundRepository;
import pl.chessarbiter.repository.TournamentRegistrationRepository;
import pl.chessarbiter.repository.TournamentStandingRepository;
import pl.chessarbiter.security.CurrentUser;
import pl.chessarbiter.security.SecurityUser;
import pl.chessarbiter.service.ProfileService;
import pl.chessarbiter.service.RegistrationService;
import pl.chessarbiter.service.StandingService;
import pl.chessarbiter.service.TournamentService;
import pl.chessarbiter.web.form.RegistrationForm;

@Controller
@RequiredArgsConstructor
public class TournamentViewController {

    private final TournamentService tournamentService;
    private final RegistrationService registrationService;
    private final ProfileService profileService;
    private final StandingService standingService;
    private final RoundRepository roundRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentStandingRepository standingRepository;

    @GetMapping("/turnieje/{slug}")
    @Transactional(readOnly = true)
    String details(@PathVariable String slug, Model model) {
        return renderDetails(slug, new RegistrationForm(), model);
    }

    @PostMapping("/turnieje/{slug}/zgloszenie")
    @Transactional
    String register(
        @PathVariable String slug,
        @Valid @ModelAttribute("registrationForm") RegistrationForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        if (bindingResult.hasErrors()) {
            return renderDetails(slug, form, model);
        }
        try {
            registrationService.register(tournament.getId(), form.toRequest(), CurrentUser.optional().orElse(null));
            redirectAttributes.addFlashAttribute("success", "Zgłoszenie zostało zapisane.");
            return "redirect:/turnieje/" + slug;
        } catch (ApiException exception) {
            bindingResult.reject("registration", exception.getMessage());
            return renderDetails(slug, form, model);
        }
    }

    @GetMapping("/turnieje/{slug}/lista-startowa")
    @Transactional(readOnly = true)
    String startingList(@PathVariable String slug, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("registrations", registrationRepository
            .findByTournament_IdAndStatusNotOrderByStartNumberAscRatingDescLastNameAscFirstNameAsc(
                tournament.getId(),
                RegistrationStatus.CANCELLED
            )
            .stream()
            .map(RegistrationResponse::from)
            .toList());
        return "tournaments/starting-list";
    }

    @GetMapping("/turnieje/{slug}/rundy")
    @Transactional(readOnly = true)
    String rounds(@PathVariable String slug, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("rounds", roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournament.getId())
            .stream().map(RoundResponse::from).toList());
        return "tournaments/rounds";
    }

    @GetMapping("/turnieje/{slug}/rundy/{roundNumber}")
    @Transactional(readOnly = true)
    String round(@PathVariable String slug, @PathVariable Integer roundNumber, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        RoundResponse round = roundRepository.findByTournament_IdAndRoundNumber(tournament.getId(), roundNumber)
            .map(RoundResponse::from)
            .orElseThrow(() -> new NotFoundException("Nie znaleziono rundy."));
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("round", round);
        model.addAttribute("editable", false);
        model.addAttribute("managementView", false);
        return "tournaments/round";
    }

    @GetMapping({"/turnieje/{slug}/wyniki", "/turnieje/{slug}/tabela"})
    @Transactional
    String results(@PathVariable String slug, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        List<RoundResponse> rounds = roundRepository.findByTournament_IdOrderByRoundNumberAsc(tournament.getId())
            .stream().map(RoundResponse::from).toList();
        List<StandingResponse> standings = standingService.recalculate(tournament).stream()
            .map(StandingResponse::from)
            .toList();
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("rounds", rounds);
        model.addAttribute("standings", standings);
        return "tournaments/results";
    }

    @GetMapping("/turnieje/{slug}/zawodnicy/{registrationId}")
    @Transactional(readOnly = true)
    String player(@PathVariable String slug, @PathVariable String registrationId, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        var registration = registrationRepository.findByIdAndTournament_Id(registrationId, tournament.getId())
            .orElseThrow(() -> new NotFoundException("Nie znaleziono zawodnika."));
        var standing = standingRepository.findByTournament_IdAndRegistration_Id(tournament.getId(), registrationId).orElse(null);
        SecurityUser currentUser = CurrentUser.optional().orElse(null);
        boolean privateData = currentUser != null
            && ("ADMIN".equals(currentUser.getRole()) || tournament.getCreatedBy().getId().equals(currentUser.getId()));
        List<PlayerGameResponse> games = new ArrayList<>();
        registration.getWhiteGames().forEach(game -> games.add(PlayerGameResponse.asWhite(game)));
        registration.getBlackGames().forEach(game -> games.add(PlayerGameResponse.asBlack(game)));
        games.sort(Comparator.comparing(PlayerGameResponse::roundNumber).thenComparing(PlayerGameResponse::boardNumber));

        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("player", new PlayerDetailResponse(
            registration.getId(),
            registration.getFirstName(),
            registration.getLastName(),
            privateData ? registration.getEmail() : null,
            registration.getClubOrCity(),
            registration.getFederation(),
            registration.getRating(),
            registration.getChessCategory(),
            registration.getBirthYear(),
            registration.getStatus(),
            registration.getStartNumber(),
            standing == null ? 0.0 : standing.getPoints(),
            standing == null ? null : standing.getRank(),
            privateData ? registration.getPhoneNumber() : null,
            privateData ? registration.getNotes() : null,
            games
        ));
        return "tournaments/player";
    }

    private String renderDetails(String slug, RegistrationForm form, Model model) {
        Tournament tournament = tournamentService.getPublicTournament(slug);
        SecurityUser currentUser = CurrentUser.optional().orElse(null);
        if (isEmpty(form) && currentUser != null) {
            var profile = profileService.findProfile(currentUser).orElse(null);
            if (profile != null) {
                form.setFirstName(profile.getFirstName());
                form.setLastName(profile.getLastName());
                form.setEmail(profile.getEmail());
                form.setClubOrCity(profile.getClubOrCity());
                form.setFederation(profile.getFederation());
                form.setChessCategory(profile.getChessCategory());
                form.setPhoneNumber(profile.getPhoneNumber());
                form.setBirthYear(profile.getBirthYear());
                form.setRating(switch (tournament.getTimeControlType()) {
                    case CLASSICAL -> profile.getClassicalRating();
                    case RAPID -> profile.getRapidRating();
                    case BLITZ -> profile.getBlitzRating();
                });
            } else {
                form.setEmail(currentUser.getEmail());
            }
        }
        boolean ownRegistration = currentUser != null && registrationService.myRegistrations(currentUser).stream()
            .anyMatch(registration -> registration.getTournament().getId().equals(tournament.getId()));
        model.addAttribute("tournament", TournamentDetailResponse.from(tournament));
        model.addAttribute("registrationForm", form);
        model.addAttribute("registrationOpen", isRegistrationOpen(tournament));
        model.addAttribute("ownRegistration", ownRegistration);
        model.addAttribute("isManager", currentUser != null
            && ("ADMIN".equals(currentUser.getRole()) || tournament.getCreatedBy().getId().equals(currentUser.getId())));
        return "tournaments/details";
    }

    private boolean isRegistrationOpen(Tournament tournament) {
        return tournament.getStatus() == TournamentStatus.PUBLISHED
            && tournament.isRegistrationOpen()
            && (tournament.getRegistrationDeadline() == null || !tournament.getRegistrationDeadline().isBefore(Instant.now()));
    }

    private boolean isEmpty(RegistrationForm form) {
        return form.getFirstName() == null && form.getEmail() == null;
    }
}
