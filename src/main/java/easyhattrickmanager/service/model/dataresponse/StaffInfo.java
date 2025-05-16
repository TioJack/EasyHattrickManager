package easyhattrickmanager.service.model.dataresponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffInfo {

    int trainerId;
    String trainerName;
    int trainerType;
    int trainerLeadership;
    int trainerSkillLevel;
    int trainerStatus;

    Integer staff1Id;
    String staff1Name;
    Integer staff1Type;
    Integer staff1Level;
    Integer staff1HofPlayerId;

    Integer staff2Id;
    String staff2Name;
    Integer staff2Type;
    Integer staff2Level;
    Integer staff2HofPlayerId;

    Integer staff3Id;
    String staff3Name;
    Integer staff3Type;
    Integer staff3Level;
    Integer staff3HofPlayerId;

    Integer staff4Id;
    String staff4Name;
    Integer staff4Type;
    Integer staff4Level;
    Integer staff4HofPlayerId;
}