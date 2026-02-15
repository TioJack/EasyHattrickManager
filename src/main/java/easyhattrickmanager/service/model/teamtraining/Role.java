package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
    KEEPER(100, RoleGroup.KEEPER, true, true, true),
    RIGHT_BACK(101, RoleGroup.BACK, false, false, true),
    RIGHT_CENTRAL_DEFENDER(102, RoleGroup.CENTRAL_DEFENDER, false, false, true),
    MIDDLE_CENTRAL_DEFENDER(103, RoleGroup.CENTRAL_DEFENDER, false, true, false),
    LEFT_CENTRAL_DEFENDER(104, RoleGroup.CENTRAL_DEFENDER, true, false, false),
    LEFT_BACK(105, RoleGroup.BACK, true, false, false),
    RIGHT_WINGER(106, RoleGroup.WINGER, false, false, true),
    RIGHT_INNER_MIDFIELD(107, RoleGroup.INNER_MIDFIELD, false, false, true),
    MIDDLE_INNER_MIDFIELD(108, RoleGroup.INNER_MIDFIELD, false, true, false),
    LEFT_INNER_MIDFIELD(109, RoleGroup.INNER_MIDFIELD, true, false, false),
    LEFT_WINGER(110, RoleGroup.WINGER, true, false, false),
    RIGHT_FORWARD(111, RoleGroup.FORWARD, false, false, true),
    MIDDLE_FORWARD(112, RoleGroup.FORWARD, false, true, false),
    LEFT_FORWARD(113, RoleGroup.FORWARD, true, false, false),
    SUBSTITUTION_KEEPER(114, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION_DEFENDER(115, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION_INNER_MIDFIELD(116, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION_WINGER(117, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION_FORWARD(118, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__KEEPER(200, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__CENTRAL_DEFENDER(201, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__WING_BACK(202, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__INNER_MIDFIELDER(203, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__FORWARD(204, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__WINGER(205, RoleGroup.NO_GROUP, false, false, false),
    SUBSTITUTION__EXTRA(206, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_KEEPER(207, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_CENTRAL_DEFENDER(208, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_WING_BACK(209, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_INNER_MIDFIELDER(210, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_FORWARD(211, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_WINGER(212, RoleGroup.NO_GROUP, false, false, false),
    BACKUP_EXTRA(213, RoleGroup.NO_GROUP, false, false, false),
    SET_PIECES(17, RoleGroup.NO_GROUP, false, false, false),
    CAPTAIN(18, RoleGroup.NO_GROUP, false, false, false),
    REPLACED_PLAYER_1(19, RoleGroup.NO_GROUP, false, false, false),
    REPLACED_PLAYER_2(20, RoleGroup.NO_GROUP, false, false, false),
    REPLACED_PLAYER_3(21, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_1(22, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_2(23, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_3(24, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_4(25, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_5(26, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_6(27, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_7(28, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_8(29, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_9(30, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_10(31, RoleGroup.NO_GROUP, false, false, false),
    PENALTY_TAKER_11(32, RoleGroup.NO_GROUP, false, false, false);

    private final int value;
    private final RoleGroup roleGroup;
    private final boolean left;
    private final boolean middle;
    private final boolean right;
}
