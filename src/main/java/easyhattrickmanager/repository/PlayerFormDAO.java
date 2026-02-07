package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerForm;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerFormDAO {

    void insert(@Param("player") PlayerForm player);

    List<PlayerForm> get(@Param("teamId") int teamId);

    List<PlayerForm> getPlayer(@Param("teamId") int teamId, @Param("playerId") int playerId);
}
