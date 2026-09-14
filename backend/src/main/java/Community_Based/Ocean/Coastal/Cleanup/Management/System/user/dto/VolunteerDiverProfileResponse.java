package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import java.util.List;

public record VolunteerDiverProfileResponse(
        String experience,
        String certification,
        String preferredRegion,
        List<EquipmentItem> equipment
) {

    public record EquipmentItem(Integer equipmentId, String equipmentName) {
    }
}
