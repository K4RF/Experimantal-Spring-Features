package redisCache.project.repository;

import org.springframework.data.repository.CrudRepository;
import redisCache.project.entity.MemberCache;


public interface MemberCacheRepository extends CrudRepository<MemberCache, String> {
    // email로 캐싱
}
