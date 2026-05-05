package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TerminalIdentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentityBatchGenerator
{
    public List<TerminalIdentity> generate(int count, long startIndex, String vehicleNumberPattern, String deviceSnPattern, String simNumberPattern)
    {
        if (count < 1) throw new IllegalArgumentException("终端数量必须大于 0");

        List<TerminalIdentity> identities = new ArrayList<>(count);
        Set<String> vehicleNumbers = new HashSet<>(count);
        Set<String> deviceSns = new HashSet<>(count);
        Set<String> simNumbers = new HashSet<>(count);

        for (int i = 0; i < count; i++)
        {
            long index = startIndex + i;
            String vehicleNumber = format("车牌号", vehicleNumberPattern, index);
            String deviceSn = normalizeDeviceSn(format("终端ID", deviceSnPattern, index));
            String simNumber = normalizeSimNumber(format("SIM卡号", simNumberPattern, index));

            requireUnique("车牌号", vehicleNumber, vehicleNumbers);
            requireUnique("终端ID", deviceSn, deviceSns);
            requireUnique("SIM卡号", simNumber, simNumbers);

            identities.add(new TerminalIdentity(vehicleNumber, deviceSn, simNumber));
        }

        return identities;
    }

    private String format(String name, String pattern, long index)
    {
        if (pattern == null || pattern.isBlank()) throw new IllegalArgumentException(name + "规则不能为空");
        try
        {
            return String.format(pattern, index);
        }
        catch(Exception ex)
        {
            throw new IllegalArgumentException(name + "规则无法格式化 index=" + index + ": " + ex.getMessage(), ex);
        }
    }

    private String normalizeDeviceSn(String deviceSn)
    {
        if (deviceSn.length() > 7) throw new IllegalArgumentException("终端ID长度不能超过 7 位: " + deviceSn);
        return leftPad(deviceSn, 7);
    }

    private String normalizeSimNumber(String simNumber)
    {
        if (simNumber.matches("\\d+") == false) throw new IllegalArgumentException("SIM卡号必须为数字: " + simNumber);
        if (simNumber.length() > 12) throw new IllegalArgumentException("SIM卡号长度不能超过 12 位: " + simNumber);
        return leftPad(simNumber, 12);
    }

    private String leftPad(String value, int length)
    {
        if (value.length() >= length) return value;
        return "0".repeat(length - value.length()) + value;
    }

    private void requireUnique(String name, String value, Set<String> values)
    {
        if (values.add(value) == false)
        {
            throw new IllegalArgumentException(name + "生成重复: " + value);
        }
    }
}
