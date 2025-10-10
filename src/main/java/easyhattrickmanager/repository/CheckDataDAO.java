package easyhattrickmanager.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CheckDataDAO {

    List<Integer> checkTraining(@Param("seasonWeek") String seasonWeek);

    List<Integer> checkPlayerData(@Param("seasonWeek") String seasonWeek);

    List<Integer> checkTrainer(@Param("seasonWeek") String seasonWeek);

    boolean checkStartProjects();
}
