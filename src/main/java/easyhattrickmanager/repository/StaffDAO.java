package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Staff;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StaffDAO {

    void insert(@Param("staff") Staff staff);

    List<Staff> get(@Param("teamId") int teamId);
}