package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.User;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDAO {

    void insert(@Param("user") User user);

    void link(@Param("userEhmId") int userEhmId, @Param("userId") int userId);

    User get(@Param("username") String username);

    User getByTeamId(@Param("teamId") int teamId);

    List<User> getAllUsers();
}
