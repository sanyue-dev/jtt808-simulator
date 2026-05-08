package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import cn.org.hentai.simulator.service.DeviceProfileService;
import cn.org.hentai.simulator.web.exception.ValidationException;
import cn.org.hentai.simulator.web.vo.Page;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceControllerTest
{
    @Test
    void saveCreatesReusableDeviceProfile()
    {
        DeviceController controller = new DeviceController();
        RecordingDeviceProfileService service = new RecordingDeviceProfileService();
        ReflectionTestUtils.setField(controller, "deviceProfileService", service);

        DeviceProfile saved = controller.save(null,
                "线上设备复现",
                "京A12345",
                "DEVICE001",
                "013800000001",
                "AUTH-CODE",
                "direct",
                10L,
                "127.0.0.1",
                "20021",
                "100",
                "复现报警联调");

        assertEquals(1L, saved.getId());
        assertEquals("线上设备复现", service.recorded.getName());
        assertEquals("京A12345", service.recorded.getVehicleNumber());
        assertEquals("DEVICE001", service.recorded.getDeviceSn());
        assertEquals("013800000001", service.recorded.getSimNumber());
        assertEquals("AUTH-CODE", service.recorded.getAuthToken());
        assertEquals("direct", service.recorded.getAuthMode());
        assertEquals(10L, service.recorded.getDefaultRouteId());
        assertEquals("127.0.0.1", service.recorded.getServerAddress());
        assertEquals(20021, service.recorded.getServerPort());
        assertEquals(100, service.recorded.getInitialMileage());
        assertEquals("复现报警联调", service.recorded.getRemark());
        assertEquals(1, service.recorded.getEnabled());
    }

    @Test
    void listReturnsPagedDeviceProfiles()
    {
        DeviceController controller = new DeviceController();
        RecordingDeviceProfileService service = new RecordingDeviceProfileService();
        ReflectionTestUtils.setField(controller, "deviceProfileService", service);

        Page<DeviceProfile> page = controller.list(2, 10, "线上");

        assertEquals(2, page.getPageIndex());
        assertEquals(10, page.getPageSize());
        assertEquals(1, page.getList().size());
        assertEquals("线上设备复现", page.getList().get(0).getName());
        assertEquals("线上", service.keyword);
    }

    @Test
    void saveRejectsInvalidServerPortAsValidationFailure()
    {
        DeviceController controller = new DeviceController();
        ReflectionTestUtils.setField(controller, "deviceProfileService", new RecordingDeviceProfileService());

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.save(null,
                "线上设备复现",
                "京A12345",
                "DEVICE001",
                "013800000001",
                "AUTH-CODE",
                "direct",
                10L,
                "127.0.0.1",
                "abc",
                "100",
                "复现报警联调"));

        assertEquals("服务端端口必须是整数", ex.getMessage());
    }

    @Test
    void removeDeletesDeviceProfile()
    {
        DeviceController controller = new DeviceController();
        RecordingDeviceProfileService service = new RecordingDeviceProfileService();
        ReflectionTestUtils.setField(controller, "deviceProfileService", service);

        controller.remove(12L);

        assertEquals(12L, service.removedId);
    }

    @Test
    void statusTogglesDeviceProfileEnabledState()
    {
        DeviceController controller = new DeviceController();
        RecordingDeviceProfileService service = new RecordingDeviceProfileService();
        ReflectionTestUtils.setField(controller, "deviceProfileService", service);

        controller.status(12L, 0);

        assertEquals(12L, service.statusId);
        assertEquals(0, service.enabled);
    }

    private static class RecordingDeviceProfileService extends DeviceProfileService
    {
        private DeviceProfile recorded;
        private String keyword;
        private Long removedId;
        private Long statusId;
        private Integer enabled;

        @Override
        public DeviceProfile save(DeviceProfile profile)
        {
            this.recorded = profile;
            profile.setId(1L);
            return profile;
        }

        @Override
        public Page<DeviceProfile> find(String keyword, int pageIndex, int pageSize)
        {
            this.keyword = keyword;
            DeviceProfile profile = new DeviceProfile();
            profile.setId(1L);
            profile.setName("线上设备复现");
            Page<DeviceProfile> page = new Page<>(pageIndex, pageSize);
            page.setList(List.of(profile));
            page.setRecordCount(1);
            return page;
        }

        @Override
        public int removeById(Long id)
        {
            this.removedId = id;
            return 1;
        }

        @Override
        public int updateEnabled(Long id, int enabled)
        {
            this.statusId = id;
            this.enabled = enabled;
            return 1;
        }
    }
}
