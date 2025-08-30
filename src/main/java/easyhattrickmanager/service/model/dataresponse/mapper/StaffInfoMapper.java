package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.StaffMember;
import easyhattrickmanager.repository.model.Trainer;
import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface StaffInfoMapper {

    StaffInfo toInfo(Trainer trainer, List<StaffMember> staffMembers);
}
