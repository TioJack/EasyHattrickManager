package easyhattrickmanager.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckDataDAO {

    boolean checkTraining(@Param("seasonWeek") String seasonWeek);

    boolean checkPlayerData(@Param("seasonWeek") String seasonWeek);

    boolean checkTrainer(@Param("seasonWeek") String seasonWeek);

    boolean checkStartProjects();
}
