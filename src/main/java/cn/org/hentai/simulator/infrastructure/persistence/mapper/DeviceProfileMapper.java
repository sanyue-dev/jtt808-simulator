package cn.org.hentai.simulator.infrastructure.persistence.mapper;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceProfileMapper
{
    int insert(DeviceProfile record);

    int updateByPrimaryKey(DeviceProfile record);

    DeviceProfile selectByPrimaryKey(Long id);

    int deleteByPrimaryKey(Long id);

    List<DeviceProfile> find(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long count(@Param("keyword") String keyword);

    int updateEnabled(@Param("id") Long id, @Param("enabled") int enabled);
}
