package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Training;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainingDAO {

    void insert(@Param("training") Training training);

    List<Training> get(@Param("teamId") int teamId);
}
