package cn.org.hentai.simulator.service.task;

public enum TaskGroupSource
{
    SINGLE("single", "单车创建"),
    BATCH("batch", "批量创建");

    private final String value;
    private final String displayText;

    TaskGroupSource(String value, String displayText)
    {
        this.value = value;
        this.displayText = displayText;
    }

    public String getValue()
    {
        return value;
    }

    public String getDisplayText()
    {
        return displayText;
    }
}
