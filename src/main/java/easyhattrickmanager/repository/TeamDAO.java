package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TeamDAO {

    void insert(@Param("team") Team team);

}
