package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;

public record MotorItem(
        String descricao,
        BigDecimal potenciaCv,
        BigDecimal demandaKvaTabela19,
        boolean isReserva,
        boolean isTrifasico
) {}
