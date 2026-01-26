package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerSubSkill;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerSubSkillDAO {

    void insert(@Param("player") PlayerSubSkill player);

    List<PlayerSubSkill> get(@Param("teamId") int teamId);

    List<PlayerSubSkill> getPlayer(@Param("teamId") int teamId, @Param("playerId") int playerId);
}
