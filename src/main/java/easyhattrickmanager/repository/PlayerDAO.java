package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerDAO {

    void insert(@Param("player") Player player);
}
