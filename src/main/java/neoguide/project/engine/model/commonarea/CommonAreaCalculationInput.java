package neoguide.project.engine.model.commonarea;

import java.util.List;

public record CommonAreaCalculationInput(
        LightingTugInput iluminacaoTug,
        List<WaterHeaterItem> aquecedoresPassagem,
        List<BoilerItem> boilers,
        List<ApplianceItem> eletrodomesticos,
        CookingInput coccaoGourmet,
        List<AirConditioningItem> climatizacao,
        List<MotorItem> motores,
        List<ElevatorItem> elevadores,
        List<PoolPumpItem> bombasPiscina,
        List<CustomLoadItem> cargasPersonalizadas
) {}
