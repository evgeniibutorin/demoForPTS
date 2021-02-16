package com.example.demo.controller;

import com.example.demo.MarkService.MarkQuantityService;
import com.example.demo.MarkService.MarkService;
import com.example.demo.entity.Mark;
import com.example.demo.repository.MarkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    public @ResponseBody
    byte[] getMarksAndSumQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.sumCointer(markService.parseArrayStringToMap(markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getAllMarksAndQuantity", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody
    byte[] getAllMarksAndQuantity(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip)))).getBytes();
    }

    @PostMapping(value = "/getMarksAndQuantityWithoutNull", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public @ResponseBody
    byte[] getMarksAndQuantityWithoutNull(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        return objectMapper.writeValueAsString(markService.selectMapsWithoutNull(markService.parseArrayStringToMap((markService.parseZipToArrayString(fileZip))))).getBytes();
    }

    @PostMapping(value = "/saveMarksToDB", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public ResponseEntity createMarks(@RequestParam(value = "files") MultipartFile fileZip) throws Exception {
        markService.createMarkAndQuantity(markService.parseZipToArrayString(fileZip));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(value = "/findMarksAndQuantity", produces = "application/json")
    public List<Object> findMarksAndQuantity() {
        return markRepository.findAllMarksAndQuantity();
    }

    @GetMapping(value = "/findMarksWithoutNullQuantity", produces = "application/json")
    public List<Object> findMarksWithoutNullQuantity() {
        return markRepository.findAllMarksWithoutNullQuantity();
    }

    @GetMapping(value = "/findMarksWithArrayQuantity", produces = "application/json")
    public List<Mark> findMarksWithArrayQuantity() {
        return markRepository.findAllMarksWithArrayQuantity();
    }

    @GetMapping(value = "/getMarksAndQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getMarksAndQuantityFileFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksAndQuantity()).getBytes();
    }

    @GetMapping(value = "/getFileWithMarksAndQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getFileWithMarksAndQuantityFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksWithoutNullQuantity()).getBytes();
    }

    @GetMapping(value = "/getMarksWithArrayQuantityFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] getMarksWithArrayQuantityFile() throws IOException {
        return objectMapper.writeValueAsString(markRepository.findAllMarksWithArrayQuantity()).getBytes();
    }

    @GetMapping(value = "/findMarks", produces = "application/json")
    public List<Object> findMarks() {
        return markRepository.findAllMarks();
    }

}
