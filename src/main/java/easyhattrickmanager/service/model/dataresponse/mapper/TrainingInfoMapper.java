package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.Training;
import easyhattrickmanager.service.model.dataresponse.TrainingInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface TrainingInfoMapper {

    TrainingInfo toInfo(Training training);
}
