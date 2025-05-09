package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.UserEhmHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEhmHistoryDAO {

    void insert(@Param("history") UserEhmHistory history);

}
