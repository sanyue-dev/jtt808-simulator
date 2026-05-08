package cn.org.hentai.simulator.service;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import cn.org.hentai.simulator.infrastructure.persistence.mapper.DeviceProfileMapper;
import cn.org.hentai.simulator.web.exception.ValidationException;
import cn.org.hentai.simulator.web.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DeviceProfileService
{
    @Autowired
    DeviceProfileMapper deviceProfileMapper;

    public DeviceProfile save(DeviceProfile profile)
    {
        validate(profile);
        if (profile.getEnabled() == null) profile.setEnabled(1);
        if (profile.getInitialMileage() == null) profile.setInitialMileage(0);
        if (profile.getId() == null) deviceProfileMapper.insert(profile);
        else deviceProfileMapper.updateByPrimaryKey(profile);
        return profile;
    }

    public Page<DeviceProfile> find(String keyword, int pageIndex, int pageSize)
    {
        Page<DeviceProfile> page = new Page<>(pageIndex, pageSize);
        int offset = Math.max(pageIndex - 1, 0) * pageSize;
        List<DeviceProfile> profiles = deviceProfileMapper.find(keyword, offset, pageSize);
        page.setList(profiles);
        page.setRecordCount(deviceProfileMapper.count(keyword));
        return page;
    }

    public int removeById(Long id)
    {
        return deviceProfileMapper.deleteByPrimaryKey(id);
    }

    public int updateEnabled(Long id, int enabled)
    {
        return deviceProfileMapper.updateEnabled(id, enabled == 0 ? 0 : 1);
    }

    private void validate(DeviceProfile profile)
    {
        if ("direct".equals(profile.getAuthMode()) && !StringUtils.hasText(profile.getAuthToken()))
        {
            throw new ValidationException("直接鉴权设备必须填写鉴权码");
        }
    }
}
