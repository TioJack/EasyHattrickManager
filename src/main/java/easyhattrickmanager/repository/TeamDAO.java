package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Team;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeamDAO {

    void insert(@Param("team") Team team);

    Team get(@Param("id") int id);

    List<Team> getByUserId(@Param("userId") int userId);

    List<Team> getActiveTeams();

    void deactivateTeam(@Param("userId") int userId, @Param("id") int id);

}
