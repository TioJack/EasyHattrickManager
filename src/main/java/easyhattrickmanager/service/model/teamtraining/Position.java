package easyhattrickmanager.service.model.teamtraining;

import static easyhattrickmanager.service.model.teamtraining.Behaviour.DEFENSIVE;
import static easyhattrickmanager.service.model.teamtraining.Behaviour.NORMAL;
import static easyhattrickmanager.service.model.teamtraining.Behaviour.OFFENSIVE;
import static easyhattrickmanager.service.model.teamtraining.Behaviour.TOWARDS_MIDDLE;
import static easyhattrickmanager.service.model.teamtraining.Behaviour.TOWARDS_WING;
import static easyhattrickmanager.service.model.teamtraining.Role.KEEPER;
import static easyhattrickmanager.service.model.teamtraining.Role.LEFT_BACK;
import static easyhattrickmanager.service.model.teamtraining.Role.LEFT_CENTRAL_DEFENDER;
import static easyhattrickmanager.service.model.teamtraining.Role.LEFT_FORWARD;
import static easyhattrickmanager.service.model.teamtraining.Role.LEFT_INNER_MIDFIELD;
import static easyhattrickmanager.service.model.teamtraining.Role.LEFT_WINGER;
import static easyhattrickmanager.service.model.teamtraining.Role.MIDDLE_CENTRAL_DEFENDER;
import static easyhattrickmanager.service.model.teamtraining.Role.MIDDLE_FORWARD;
import static easyhattrickmanager.service.model.teamtraining.Role.MIDDLE_INNER_MIDFIELD;
import static easyhattrickmanager.service.model.teamtraining.Role.RIGHT_BACK;
import static easyhattrickmanager.service.model.teamtraining.Role.RIGHT_CENTRAL_DEFENDER;
import static easyhattrickmanager.service.model.teamtraining.Role.RIGHT_FORWARD;
import static easyhattrickmanager.service.model.teamtraining.Role.RIGHT_INNER_MIDFIELD;
import static easyhattrickmanager.service.model.teamtraining.Role.RIGHT_WINGER;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Position {
    KP(KEEPER, NORMAL),
    MCD(MIDDLE_CENTRAL_DEFENDER, NORMAL),
    MCDO(MIDDLE_CENTRAL_DEFENDER, OFFENSIVE),
    RCD(RIGHT_CENTRAL_DEFENDER, NORMAL),
    RCDO(RIGHT_CENTRAL_DEFENDER, OFFENSIVE),
    RCDTW(RIGHT_CENTRAL_DEFENDER, TOWARDS_WING),
    LCD(LEFT_CENTRAL_DEFENDER, NORMAL),
    LCDO(LEFT_CENTRAL_DEFENDER, OFFENSIVE),
    LCDTW(LEFT_CENTRAL_DEFENDER, TOWARDS_WING),
    RB(RIGHT_BACK, NORMAL),
    RBD(RIGHT_BACK, DEFENSIVE),
    RBO(RIGHT_BACK, OFFENSIVE),
    RBTM(RIGHT_BACK, TOWARDS_MIDDLE),
    LB(LEFT_BACK, NORMAL),
    LBD(LEFT_BACK, DEFENSIVE),
    LBO(LEFT_BACK, OFFENSIVE),
    LBTM(LEFT_BACK, TOWARDS_MIDDLE),
    RW(RIGHT_WINGER, NORMAL),
    RWD(RIGHT_WINGER, DEFENSIVE),
    RWO(RIGHT_WINGER, OFFENSIVE),
    RWTM(RIGHT_WINGER, TOWARDS_MIDDLE),
    LW(LEFT_WINGER, NORMAL),
    LWD(LEFT_WINGER, DEFENSIVE),
    LWO(LEFT_WINGER, OFFENSIVE),
    LWTM(LEFT_WINGER, TOWARDS_MIDDLE),
    MIM(MIDDLE_INNER_MIDFIELD, NORMAL),
    MIMD(MIDDLE_INNER_MIDFIELD, DEFENSIVE),
    MIMO(MIDDLE_INNER_MIDFIELD, OFFENSIVE),
    RIM(RIGHT_INNER_MIDFIELD, NORMAL),
    RIMD(RIGHT_INNER_MIDFIELD, DEFENSIVE),
    RIMO(RIGHT_INNER_MIDFIELD, OFFENSIVE),
    RIMTW(RIGHT_INNER_MIDFIELD, TOWARDS_WING),
    LIM(LEFT_INNER_MIDFIELD, NORMAL),
    LIMD(LEFT_INNER_MIDFIELD, DEFENSIVE),
    LIMO(LEFT_INNER_MIDFIELD, OFFENSIVE),
    LIMTW(LEFT_INNER_MIDFIELD, TOWARDS_WING),
    MFW(MIDDLE_FORWARD, NORMAL),
    MFWD(MIDDLE_FORWARD, DEFENSIVE),
    RFW(RIGHT_FORWARD, NORMAL),
    RFWD(RIGHT_FORWARD, DEFENSIVE),
    RFWTW(RIGHT_FORWARD, TOWARDS_WING),
    LFW(LEFT_FORWARD, NORMAL),
    LFWD(LEFT_FORWARD, DEFENSIVE),
    LFWTW(LEFT_FORWARD, TOWARDS_WING);

    private final Role role;
    private final Behaviour behaviour;
}
