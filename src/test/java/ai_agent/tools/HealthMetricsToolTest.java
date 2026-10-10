package ai_agent.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthMetricsToolTest {

    private final HealthMetricsTool tool = new HealthMetricsTool();

    @Test
    void calculatesBmiAndKeepsSafetyDisclaimer() {
        String result = tool.calculateBmi(170, 65);

        assertThat(result).contains("22.5").contains("不能单独用于诊断");
    }

    @Test
    void rejectsInvalidMeasurements() {
        assertThat(tool.calculateBmi(0, 65)).contains("身高参数无效");
        assertThat(tool.calculateBmi(170, -1)).contains("体重参数无效");
    }
}
