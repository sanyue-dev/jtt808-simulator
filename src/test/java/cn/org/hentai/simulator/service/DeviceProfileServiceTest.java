package cn.org.hentai.simulator.service;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import cn.org.hentai.simulator.infrastructure.persistence.mapper.DeviceProfileMapper;
import cn.org.hentai.simulator.web.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceProfileServiceTest
{
    @Test
    void directAuthenticationRequiresToken()
    {
        DeviceProfileService service = new DeviceProfileService();
        ReflectionTestUtils.setField(service, "deviceProfileMapper", new RecordingDeviceProfileMapper());

        DeviceProfile profile = validProfile();
        profile.setAuthMode("direct");
        profile.setAuthToken("");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.save(profile));

        assertEquals("直接鉴权设备必须填写鉴权码", ex.getMessage());
    }

    private DeviceProfile validProfile()
    {
        DeviceProfile profile = new DeviceProfile();
        profile.setName("线上设备复现");
        profile.setVehicleNumber("京A12345");
        profile.setDeviceSn("DEVICE001");
        profile.setSimNumber("013800000001");
        profile.setAuthToken("AUTH-CODE");
        profile.setAuthMode("direct");
        profile.setDefaultRouteId(10L);
        profile.setServerAddress("127.0.0.1");
        profile.setServerPort(20021);
        profile.setInitialMileage(0);
        profile.setEnabled(1);
        return profile;
    }

    private static class RecordingDeviceProfileMapper implements DeviceProfileMapper
    {
        @Override
        public int insert(DeviceProfile record)
        {
            record.setId(1L);
            return 1;
        }

        @Override
        public int updateByPrimaryKey(DeviceProfile record)
        {
            return 1;
        }

        @Override
        public DeviceProfile selectByPrimaryKey(Long id)
        {
            return null;
        }

        @Override
        public int deleteByPrimaryKey(Long id)
        {
            return 1;
        }

        @Override
        public List<DeviceProfile> find(String keyword, int offset, int limit)
        {
            return Collections.emptyList();
        }

        @Override
        public long count(String keyword)
        {
            return 0;
        }

        @Override
        public int updateEnabled(Long id, int enabled)
        {
            return 1;
        }
    }
}
