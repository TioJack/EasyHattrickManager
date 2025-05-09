package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerDataDAO {

    void insert(@Param("player") PlayerData player);
}
