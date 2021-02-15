package com.example.demo.controller;

import com.example.demo.MarkService.MarkQuantityService;
import com.example.demo.MarkService.MarkService;
import com.example.demo.repository.MarkRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class MarkController {

    MarkRepository markRepository;
    MarkService markService;
    MarkQuantityService markQuantityService;
    Logger logger = LoggerFactory.getLogger(MarkController.class);

    @PostMapping(value = "/marks", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity saveMarks(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
        for (MultipartFile file : files) {
            markService.parseCSVFile(file);
            markQuantityService.amountCounter(file);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(value = "/unzip", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity saveMarksFromZip(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        String extension = FilenameUtils.getExtension(fileZip.getOriginalFilename());
        if (!extension.equals("zip")) {
            throw new RuntimeException("A " + extension + " file has been passed in the controller. Zip file expected.");
        }
        ZipInputStream zis = new ZipInputStream(fileZip.getInputStream());
        try {
            ArrayList<String> csvHolder = new ArrayList<>();
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    logger.info("There is directory in the archive.");
                } else {
                    //todo:check zip entry name extenshen csv
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                    zipEntry = zis.getNextEntry();
                }
                zipEntry = zis.getNextEntry();
            }
            markService.putIntoDB(csvHolder);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } finally {
            zis.closeEntry();
            zis.close();
        }
    }

    @GetMapping(value = "/findMarksAndQuantity",produces = "application/json")
    public List<Object> findMarksAndQuantity() {
        return markRepository.findAllMarksAndQuantity();
    }

    @GetMapping(value = "/findMarksWithoutNullQuantity",produces = "application/json")
    public List<Object> findMarksWithoutNullQuantity() {
        return markRepository.findAllMarksWithoutNullQuantity();
    }

    @GetMapping(value = "/findMarksWithArrayQuantity",produces = "application/json")
    public List<Object> findMarksWithArrayQuantity() {
        return markRepository.findAllMarksWithArrayQuantity();
    }


}
