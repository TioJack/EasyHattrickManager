package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.Language;
import easyhattrickmanager.service.model.dataresponse.LanguageInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface LanguageInfoMapper {

    List<LanguageInfo> toInfo(List<Language> language);

    @Mapping(target = "name", source = "friendlyName")
    LanguageInfo toInfo(Language language);
}
