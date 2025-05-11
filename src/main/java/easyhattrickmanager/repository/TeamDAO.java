package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Team;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeamDAO {

    void insert(@Param("team") Team team);

    Team get(@Param("id") int id);

    List<Team> getActiveTeams();

}
