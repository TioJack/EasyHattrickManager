package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Player;
import easyhattrickmanager.repository.model.UpdateExecution;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UpdateExecutionDAO {

    void insert(@Param("updateExecution") UpdateExecution updateExecution);

    void update(@Param("updateExecution") UpdateExecution updateExecution);

    List<UpdateExecution> getPending();

}
