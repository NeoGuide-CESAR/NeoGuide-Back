package neoguide.project.engine.model.commonarea;

import java.util.List;

public record CookingInput(
        List<OvenItem> fornos,
        List<CooktopItem> cooktops
) {}
