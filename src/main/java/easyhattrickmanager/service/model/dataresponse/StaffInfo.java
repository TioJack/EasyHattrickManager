package easyhattrickmanager.service.model.dataresponse;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffInfo {

    TrainerInfo trainer;
    List<StaffMemberInfo> staffMembers;
}