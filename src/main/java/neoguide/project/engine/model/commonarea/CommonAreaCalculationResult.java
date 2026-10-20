package neoguide.project.engine.model.commonarea;

import java.math.BigDecimal;
import java.util.List;

public record CommonAreaCalculationResult(
        BigDecimal demandaTotalDsKva,
        CommonAreaSubtotal parcelaA_IluminacaoTug,
        CommonAreaSubtotal parcelaB_AquecedoresPassagem,
        CommonAreaSubtotal parcelaC_Boilers,
        CommonAreaSubtotal parcelaD_Eletrodomesticos,
        CommonAreaSubtotal parcelaE_CoccaoGourmet,
        CommonAreaSubtotal parcelaF_Climatizacao,
        CommonAreaSubtotal parcelaG_Motores,
        CommonAreaSubtotal parcelaH_Elevadores,
        CommonAreaSubtotal parcelaI_BombasPiscina,
        CommonAreaSubtotal parcelasCustomizadas,
        List<BigDecimal> demandasPorItem
) {
    public CommonAreaCalculationResult(
            BigDecimal demandaTotalDsKva,
            CommonAreaSubtotal parcelaA_IluminacaoTug,
            CommonAreaSubtotal parcelaB_AquecedoresPassagem,
            CommonAreaSubtotal parcelaC_Boilers,
            CommonAreaSubtotal parcelaD_Eletrodomesticos,
            CommonAreaSubtotal parcelaE_CoccaoGourmet,
            CommonAreaSubtotal parcelaF_Climatizacao,
            CommonAreaSubtotal parcelaG_Motores,
            CommonAreaSubtotal parcelaH_Elevadores,
            CommonAreaSubtotal parcelaI_BombasPiscina,
            CommonAreaSubtotal parcelasCustomizadas
    ) {
        this(
                demandaTotalDsKva,
                parcelaA_IluminacaoTug,
                parcelaB_AquecedoresPassagem,
                parcelaC_Boilers,
                parcelaD_Eletrodomesticos,
                parcelaE_CoccaoGourmet,
                parcelaF_Climatizacao,
                parcelaG_Motores,
                parcelaH_Elevadores,
                parcelaI_BombasPiscina,
                parcelasCustomizadas,
                List.of()
        );
    }

    public BigDecimal getDemandaServicosDsKva() {
        return demandaTotalDsKva;
    }

    public List<BigDecimal> getDemandasPorItem() {
        return demandasPorItem;
    }
}
