package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Country;
import easyhattrickmanager.repository.model.LeagueCountry;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CountryDAO {

    void insert(@Param("country") Country country);

    Optional<Country> get(@Param("id") int id);

    void insertLeagueCountry(@Param("leagueId") int leagueId, @Param("countryId") int countryId);

    List<LeagueCountry> getAllLeagueCountry();
}