package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.League;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LeagueDAO {

    void insert(@Param("league") League league);
}