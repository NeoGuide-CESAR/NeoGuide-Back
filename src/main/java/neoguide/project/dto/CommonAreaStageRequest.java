package neoguide.project.dto;

import java.util.ArrayList;
import java.util.List;

public class CommonAreaStageRequest {

    private List<CommonAreaLoadItemRequest> cargas = new ArrayList<>();

    public CommonAreaStageRequest() {
    }

    public CommonAreaStageRequest(List<CommonAreaLoadItemRequest> cargas) {
        this.cargas = cargas;
    }

    public List<CommonAreaLoadItemRequest> getCargas() {
        return cargas;
    }

    public void setCargas(List<CommonAreaLoadItemRequest> cargas) {
        this.cargas = cargas;
    }
}
