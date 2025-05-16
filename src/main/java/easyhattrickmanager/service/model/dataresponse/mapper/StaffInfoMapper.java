package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.Staff;
import easyhattrickmanager.service.model.dataresponse.StaffInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface StaffInfoMapper {

    StaffInfo toInfo(Staff staff);
}
