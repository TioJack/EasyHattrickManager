package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.StaffMember;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StaffMemberDAO {

    void insert(@Param("staffMember") StaffMember staffMember);

    List<StaffMember> get(@Param("teamId") int teamId);
}