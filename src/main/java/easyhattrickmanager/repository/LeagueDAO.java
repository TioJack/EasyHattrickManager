package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.League;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LeagueDAO {

    void insert(@Param("league") League league);

    Optional<League> get(@Param("id") int id);
}