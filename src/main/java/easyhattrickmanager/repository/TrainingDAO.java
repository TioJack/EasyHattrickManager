package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Training;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainingDAO {

    void insert(@Param("training") Training training);
}
