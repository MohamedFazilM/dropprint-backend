package com.dropprint.project.controller;

import com.dropprint.project.model.Design;
import com.dropprint.project.repository.DesignRepository;
import com.dropprint.project.service.IdGeneratorService;
import com.dropprint.project.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/designs")
@CrossOrigin(origins = "*")
public class DesignController {

    @Autowired
    private SupabaseStorageService storageService;

    @Autowired
    private DesignRepository designRepository;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @PostMapping("/upload")
    public Design uploadDesign(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "frontFile", required = false) MultipartFile frontFile,
            @RequestParam(value = "backFile", required = false) MultipartFile backFile,
            @RequestParam("printArea") String printArea,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "positionX", required = false) Double positionX,
            @RequestParam(value = "positionY", required = false) Double positionY,
            @RequestParam(value = "scale", required = false) Double scale,
            @RequestParam(value = "rotation", required = false) Double rotation
    ) {
        try {
            String frontUrl = null;
            String backUrl = null;

            if (frontFile != null && !frontFile.isEmpty()) {
                frontUrl = storageService.uploadFile(frontFile, "designs");
            }
            if (backFile != null && !backFile.isEmpty()) {
                backUrl = storageService.uploadFile(backFile, "designs");
            }

            if (file != null && !file.isEmpty()) {
                String uploadedUrl = storageService.uploadFile(file, "designs");
                if ("Back".equalsIgnoreCase(printArea)) {
                    backUrl = uploadedUrl;
                } else {
                    frontUrl = uploadedUrl;
                }
            }

            Design design = new Design();
            design.setId(idGeneratorService.generate("dsn", "design_id_seq"));
            design.setFileUrl(frontUrl != null ? frontUrl : backUrl);
            design.setFileUrlBack(backUrl);
            design.setPrintArea(printArea);
            design.setPosition(position);
            design.setPositionX(positionX);
            design.setPositionY(positionY);
            design.setScale(scale);
            design.setRotation(rotation);

            return designRepository.save(design);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}