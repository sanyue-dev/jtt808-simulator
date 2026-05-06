package cn.org.hentai.simulator.service.task;

public class PreflightCheckException extends IllegalArgumentException
{
    private final PreflightCheckResult preflight;

    public PreflightCheckException(PreflightCheckResult preflight)
    {
        super(String.join("; ", preflight.getFailures()));
        this.preflight = preflight;
    }

    public PreflightCheckResult getPreflight()
    {
        return preflight;
    }
}
