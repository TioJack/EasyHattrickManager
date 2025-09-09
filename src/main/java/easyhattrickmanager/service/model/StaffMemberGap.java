package easyhattrickmanager.service.model;

import easyhattrickmanager.repository.model.StaffMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffMemberGap {

    int teamId;
    int playerId;
    String seasonWeekStart;
    String seasonWeekEnd;
    int missingWeeks;
    StaffMember staffMemberStart;
    StaffMember staffMemberEnd;

}
