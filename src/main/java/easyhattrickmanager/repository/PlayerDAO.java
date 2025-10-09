package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Player;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerDAO {

    void insert(@Param("player") Player player);

    List<Player> get(@Param("teamId") int teamId);

    Player getPlayer(@Param("playerId") int playerId);
}
