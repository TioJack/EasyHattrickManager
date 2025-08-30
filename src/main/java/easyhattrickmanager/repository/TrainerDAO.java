package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Trainer;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainerDAO {

    void insert(@Param("trainer") Trainer trainer);

    List<Trainer> get(@Param("teamId") int teamId);
}