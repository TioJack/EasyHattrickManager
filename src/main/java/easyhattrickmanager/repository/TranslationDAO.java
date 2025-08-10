package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Translation;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TranslationDAO {

    void insert(@Param("translation") Translation translation);

    void upsert(@Param("translation") Translation translation);

    List<Translation> getAllTranslations();

    void deleteEHMTranslations();
}