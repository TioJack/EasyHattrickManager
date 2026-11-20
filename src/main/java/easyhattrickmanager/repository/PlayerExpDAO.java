package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerExp;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerExpDAO {

    void insert(@Param("player") PlayerExp player);

    List<PlayerExp> get(@Param("teamId") int teamId);

    List<PlayerExp> getPlayer(@Param("teamId") int teamId, @Param("playerId") int playerId);
}
