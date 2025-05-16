package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerDataDAO {

    void insert(@Param("player") PlayerData player);

    List<PlayerData> get(@Param("teamId") int teamId);
}
