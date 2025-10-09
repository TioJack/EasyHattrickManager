package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerTraining;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerTrainingDAO {

    void insert(@Param("player") PlayerTraining player);

    List<PlayerTraining> get(@Param("teamId") int teamId);

    List<PlayerTraining> getPlayer(@Param("teamId") int teamId, @Param("playerId") int playerId);
}
