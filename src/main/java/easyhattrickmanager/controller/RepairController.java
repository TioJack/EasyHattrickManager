package easyhattrickmanager.controller;

import easyhattrickmanager.service.RepairService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("repair")
public class RepairController {

    private final RepairService repairService;

    @GetMapping
    public ResponseEntity<Void> getData() {
        repairService.fillInGaps();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/extendStaff")
    public ResponseEntity<Void> extendStaff(@RequestParam("teamId") int teamId) {
        repairService.extendStaff(teamId);
        return ResponseEntity.ok().build();
    }

}
