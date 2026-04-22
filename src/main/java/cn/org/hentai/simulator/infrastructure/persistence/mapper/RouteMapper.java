package cn.org.hentai.simulator.infrastructure.persistence.mapper;

import cn.org.hentai.simulator.infrastructure.persistence.mapper.Mapper;
import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.infrastructure.persistence.example.RouteExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RouteMapper {
    /**
     */
    long countByExample(RouteExample example);

    /**
     */
    int deleteByExample(RouteExample example);

    /**
     */
    int deleteByPrimaryKey(Long id);

    /**
     */
    int insert(Route record);

    /**
     */
    int insertSelective(Route record);

    /**
     */
    List<Route> selectByExample(RouteExample example);

    /**
     */
    Route selectByPrimaryKey(Long id);

    /**
     */
    int updateByExampleSelective(@Param("record") Route record, @Param("example") RouteExample example);

    /**
     */
    int updateByExample(@Param("record") Route record, @Param("example") RouteExample example);

    /**
     */
    int updateByPrimaryKeySelective(Route record);

    /**
     */
    int updateByPrimaryKey(Route record);
}