package batch.project.repository;

import org.springframework.data.repository.CrudRepository;
import batch.project.entity.MemberCache;


public interface MemberCacheRepository extends CrudRepository<MemberCache, String> {
    // email로 캐싱
}
