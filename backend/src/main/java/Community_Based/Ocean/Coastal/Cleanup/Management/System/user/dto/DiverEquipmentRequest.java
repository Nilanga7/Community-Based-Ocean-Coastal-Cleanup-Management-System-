package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiverEquipmentRequest(
        @NotBlank @Size(max = 255) String equipmentName
) {
}
