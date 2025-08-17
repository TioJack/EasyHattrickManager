package easyhattrickmanager.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserConfigDAO {

    void insert(@Param("userId") int userId, @Param("config") String config);

    void update(@Param("userId") int userId, @Param("config") String config);

    String get(@Param("userId") int userId);
}
