package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.PlayerTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayerTransferDAO {

    void insert(@Param("player") PlayerTransfer player);
}
