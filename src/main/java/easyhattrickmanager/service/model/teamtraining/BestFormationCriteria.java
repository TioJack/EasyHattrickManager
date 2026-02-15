package easyhattrickmanager.service.model.teamtraining;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BestFormationCriteria {
    HATSTATS(
        comparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(), comparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    RIGHT_DEFENSE(
        comparingDouble((PlayerRating obj) -> obj.getRating().getRightDefense()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getRightDefense()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    CENTRAL_DEFENSE(
        comparingDouble((PlayerRating obj) -> obj.getRating().getCentralDefense()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getCentralDefense()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    LEFT_DEFENSE(
        comparingDouble((PlayerRating obj) -> obj.getRating().getLeftDefense()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getLeftDefense()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    DEFENSE(
        comparingDouble((PlayerRating obj) -> obj.getRating().getDefense()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getDefense()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    MIDFIELD(
        comparingDouble((PlayerRating obj) -> obj.getRating().getMidfield()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getMidfield()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    MIDFIELD3(
        comparingDouble((PlayerRating obj) -> obj.getRating().getMidfield3()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getMidfield3()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    RIGHT_ATTACK(
        comparingDouble((PlayerRating obj) -> obj.getRating().getRightAttack()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getRightAttack()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    CENTRAL_ATTACK(
        comparingDouble((PlayerRating obj) -> obj.getRating().getCentralAttack()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getCentralAttack()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    LEFT_ATTACK(
        comparingDouble((PlayerRating obj) -> obj.getRating().getLeftAttack()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getLeftAttack()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    ATTACK(
        comparingDouble((PlayerRating obj) -> obj.getRating().getAttack()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getAttack()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed()),
    PEASO_STATS(
        comparingDouble((PlayerRating obj) -> obj.getRating().getPeasoStats()).thenComparingInt((PlayerRating obj) -> obj.getRating().getHatStats()).reversed(),
        comparingDouble((FormationRating obj) -> obj.getRating().getPeasoStats()).thenComparingInt((FormationRating obj) -> obj.getRating().getHatStats()).reversed());
    
    private final Comparator<PlayerRating> playerRatingComparator;
    private final Comparator<FormationRating> formationRatingComparator;
}