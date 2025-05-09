package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StaffDAO {

    void insert(@Param("staff") Staff staff);
}