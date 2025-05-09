package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.UserEhm;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEhmDAO {

    void insert(@Param("user") UserEhm user);

    Optional<UserEhm> get(@Param("username") String username);
}
