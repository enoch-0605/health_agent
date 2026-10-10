package ai_agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Locale;

/** 提供不涉及诊断的基础健康指标计算。 */
public class HealthMetricsTool {

    @Tool(description = "Calculate adult BMI from height and weight. The result is screening information only, not a diagnosis.")
    public String calculateBmi(
            @ToolParam(description = "Height in centimetres, between 100 and 250") double heightCm,
            @ToolParam(description = "Weight in kilograms, between 20 and 350") double weightKg) {
        if (!Double.isFinite(heightCm) || heightCm < 100 || heightCm > 250) {
            return "身高参数无效，应为 100-250 厘米。";
        }
        if (!Double.isFinite(weightKg) || weightKg < 20 || weightKg > 350) {
            return "体重参数无效，应为 20-350 千克。";
        }
        double heightMetres = heightCm / 100.0;
        double bmi = weightKg / (heightMetres * heightMetres);
        return String.format(Locale.ROOT,
                "BMI 为 %.1f。BMI 仅是成人群体层面的粗略筛查指标，不适用于未成年人、孕期人群，且不能单独用于诊断或制定治疗方案。",
                bmi);
    }
}
