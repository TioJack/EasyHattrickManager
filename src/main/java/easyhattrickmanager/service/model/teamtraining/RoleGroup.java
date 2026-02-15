package easyhattrickmanager.service.model.teamtraining;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RoleGroup {
    NO_GROUP(""),
    KEEPER("GK"),
    BACK("WB"),
    CENTRAL_DEFENDER("CD"),
    WINGER("WI"),
    INNER_MIDFIELD("IM"),
    FORWARD("FW");

    private final String abr;
}
