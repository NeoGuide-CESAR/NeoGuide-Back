package neoguide.project.engine.enums;

import java.math.BigDecimal;

public enum VoltageSystem {
    TRIFASICO_380_220V(new BigDecimal("380.00"), "380/220 V"),
    TRIFASICO_220_127V(new BigDecimal("220.00"), "220/127 V");

    private final BigDecimal lineVoltage;
    private final String description;

    VoltageSystem(BigDecimal lineVoltage, String description) {
        this.lineVoltage = lineVoltage;
        this.description = description;
    }

    public BigDecimal getLineVoltage() {
        return lineVoltage;
    }

    public String getDescription() {
        return description;
    }
}
