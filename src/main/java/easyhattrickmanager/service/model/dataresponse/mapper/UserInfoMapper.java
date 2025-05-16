package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.User;
import easyhattrickmanager.service.model.dataresponse.UserInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface UserInfoMapper {

    UserInfo toInfo(User user);
}
