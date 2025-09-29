package easyhattrickmanager.service.model.dataresponse.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.PlayerData;
import easyhattrickmanager.repository.model.PlayerTraining;
import easyhattrickmanager.service.model.dataresponse.PlayerInfo;
import easyhattrickmanager.service.model.dataresponse.PlayerTrainingInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = SPRING)
public interface PlayerInfoMapper {

    @Mapping(target = "id", source = "player.id")
    @Mapping(target = "piecesSkill", source = "playerData.setPiecesSkill")
    PlayerInfo toInfo(Player player, PlayerData playerData, PlayerTraining playerTraining);

    @Mapping(target = "pieces", source = "setPieces")
    PlayerTrainingInfo toInfo(PlayerTraining playerTraining);
}
