package easyhattrickmanager.service.model.teamtraining;


import static easyhattrickmanager.service.model.teamtraining.LineMatch.ATTACK;
import static easyhattrickmanager.service.model.teamtraining.LineMatch.DEFENSE;
import static easyhattrickmanager.service.model.teamtraining.ZoneSideMatch.CENTRAL;
import static easyhattrickmanager.service.model.teamtraining.ZoneSideMatch.LEFT;
import static easyhattrickmanager.service.model.teamtraining.ZoneSideMatch.RIGHT;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ZoneMatch {
    MIDFIELD(LineMatch.MIDFIELD, CENTRAL),
    RIGHT_DEFENSE(DEFENSE, RIGHT),
    CENTRAL_DEFENSE(DEFENSE, CENTRAL),
    LEFT_DEFENSE(DEFENSE, LEFT),
    RIGHT_ATTACK(ATTACK, RIGHT),
    CENTRAL_ATTACK(ATTACK, CENTRAL),
    LEFT_ATTACK(ATTACK, LEFT);

    private final LineMatch line;
    private final ZoneSideMatch side;
}
