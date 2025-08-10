package easyhattrickmanager.repository;

import easyhattrickmanager.repository.model.Language;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LanguageDAO {

    void insert(@Param("language") Language language);

    Optional<Language> get(@Param("id") int id);

    List<Language> getAllLanguages();
}