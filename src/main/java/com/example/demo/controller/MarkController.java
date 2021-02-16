package com.example.demo.controller;

import com.example.demo.MarkService.MarkQuantityService;
import com.example.demo.MarkService.MarkService;
import com.example.demo.repository.MarkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class MarkController {

    private ObjectMapper objectMapper;
    private MarkRepository markRepository;
    private MarkService markService;
    private MarkQuantityService markQuantityService;
    Logger logger = LoggerFactory.getLogger(MarkController.class);

    public MarkController(ObjectMapper objectMapper, MarkRepository markRepository, MarkService markService, MarkQuantityService markQuantityService) {
        this.objectMapper = objectMapper;
        this.markRepository = markRepository;
        this.markService = markService;
        this.markQuantityService = markQuantityService;
    }

    @PostMapping(value = "/getMarksAndSumQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody byte[] getMarksAndSumQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.sumCointer(markService.parseArrayStringToMap(markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getAllMarksAndQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody byte[] getAllMarksAndQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getMarksAndQuantityWitoutNull", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody byte[] getMarksAndQuantityWithoutNull(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.selectMapsWithoutNull(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip))))).getBytes();
    }


    @PostMapping(value = "/unzipToDarabase", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity saveMarksFromZip2(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
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
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //выдать исключение не инфор а варн если лог
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                }
                zipEntry = zis.getNextEntry();
            }
            markService.putIntoDB(csvHolder);//передается список строковых выражений csv файла
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } finally {
            zis.closeEntry();
            zis.close();
        }
    }








/*    @PostMapping(value = "/marks", consumes = {"application/zip"}, produces = "application/json")
    public ResponseEntity saveMarks(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
        for (MultipartFile file : files) {
            markService.parseCSVFile(file);
            markQuantityService.amountCounter(file);
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }*/



    @PostMapping(value = "/unzip3", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public HashMap<String, ArrayList<Integer>> saveMarksFromZip(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
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
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //выдать исключение не инфор а варн если лог
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                }
                zipEntry = zis.getNextEntry();
            }
            return markService.parseArrayStringToMap(csvHolder);//передается список строковых выражений csv файла

        } finally {
            zis.closeEntry();
            zis.close();
        }
    }


    @PostMapping(value = "/unzip1", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public HashMap<String, Integer> saveMarksFromZipAndShowWithQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
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
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //выдать исключение не инфор а варн если лог
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                }
                zipEntry = zis.getNextEntry();
            }
            return markService.sumCointer(markService.parseArrayStringToMap(csvHolder));//передается список строковых выражений csv файла

        } finally {
            zis.closeEntry();
            zis.close();
        }
    }



    /*@PostMapping(value = "/getFileWithMarksAndQuantityFile1", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody byte[] saveMarksFromZip1(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
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
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.info("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //выдать исключение не инфор а варн если лог
                    }
                    StringBuilder s = new StringBuilder();
                    int read = 0;
                    byte[] buffer = new byte[1024];
                    while ((read = zis.read(buffer, 0, 1024)) >= 0) {
                        s.append(new String(buffer, 0, read));
                    }
                    csvHolder.add(s.toString());
                }
                zipEntry = zis.getNextEntry();
            }
            return objectMapper.writeValueAsString(markService.orderedMapSum(markService.putIntoDB2(csvHolder))).getBytes();

        } finally {
            zis.closeEntry();
            zis.close();
        }
    }*/



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

    @GetMapping(value = "/findMarks",produces = "application/json")
    public List<Object> findMarks() {
        return markRepository.findAllMarks();
    }

    @GetMapping(value = "/getMarksAndQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getMarksAndQuantityFileFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksAndQuantity()).getBytes();
    }

    @GetMapping(value = "/getFileWithMarksAndQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getFileWithMarksAndQuantityFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksWithoutNullQuantity()).getBytes();
    }

    @GetMapping(value = "/getMarksWithArrayQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getMarksWithArrayQuantityFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksWithArrayQuantity()).getBytes();
    }



}
