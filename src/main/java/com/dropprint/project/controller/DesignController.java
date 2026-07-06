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
            @RequestParam("file") MultipartFile file,
            @RequestParam("printArea") String printArea
    ) {
        try {
            String fileUrl = storageService.uploadFile(file, "designs");

            Design design = new Design();
            design.setId(idGeneratorService.generate("dsn", "design_id_seq"));
            design.setFileUrl(fileUrl);
            design.setPrintArea(printArea);

            return designRepository.save(design);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}