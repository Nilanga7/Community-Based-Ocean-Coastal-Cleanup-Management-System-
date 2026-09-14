package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.DiverEquipment;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.DiverEquipmentRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerDiverRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.DiverEquipmentRequest;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.DiverEquipmentResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiverEquipmentService {

    private final VolunteerDiverRepository volunteerDiverRepository;
    private final DiverEquipmentRepository diverEquipmentRepository;

    public DiverEquipmentService(
            VolunteerDiverRepository volunteerDiverRepository,
            DiverEquipmentRepository diverEquipmentRepository
    ) {
        this.volunteerDiverRepository = volunteerDiverRepository;
        this.diverEquipmentRepository = diverEquipmentRepository;
    }

    @Transactional
    public DiverEquipmentResponse addEquipment(Integer userId, DiverEquipmentRequest request) {
        VolunteerDiver diver = findDiver(userId);

        DiverEquipment equipment = DiverEquipment.builder()
                .diver(diver)
                .equipmentName(request.equipmentName())
                .build();
        equipment = diverEquipmentRepository.save(equipment);

        return toResponse(equipment);
    }

    public List<DiverEquipmentResponse> listEquipment(Integer userId) {
        // Confirms the diver exists (and gives a clean 404 if not) before returning what would
        // otherwise be an indistinguishable empty list for "no equipment yet" vs "no such diver".
        findDiver(userId);

        return diverEquipmentRepository.findByDiverUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private VolunteerDiver findDiver(Integer userId) {
        return volunteerDiverRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such volunteer diver: " + userId));
    }

    private DiverEquipmentResponse toResponse(DiverEquipment equipment) {
        return new DiverEquipmentResponse(equipment.getEquipmentId(), equipment.getEquipmentName());
    }
}
