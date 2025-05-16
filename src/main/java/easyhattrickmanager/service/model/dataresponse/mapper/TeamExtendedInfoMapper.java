package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.Team;
import easyhattrickmanager.service.model.dataresponse.TeamExtendedInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface TeamExtendedInfoMapper {

    @Mapping(target = "league.id", source = "leagueId")
    @Mapping(target = "weeklyData", ignore = true)
    TeamExtendedInfo toInfo(Team team);

    List<TeamExtendedInfo> toInfos(List<Team> teams);
}
