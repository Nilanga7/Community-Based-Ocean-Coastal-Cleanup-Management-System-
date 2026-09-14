package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.DiverEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiverEquipmentRepository extends JpaRepository<DiverEquipment, Integer> {

    List<DiverEquipment> findByDiverUserId(Integer userId);
}
