package com.example.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class MarkService {

    private final static Logger logger = LoggerFactory.getLogger(MarkService.class);

    private final CsvMapper mapper = new CsvMapper()
            .configure(CsvParser.Feature.ALLOW_COMMENTS, true)
            .configure(CsvParser.Feature.WRAP_AS_ARRAY, true);

    public List<String> parseZipToCsvList(MultipartFile fileZip) throws Exception {
        String extension = FilenameUtils.getExtension(fileZip.getOriginalFilename());
        if (!"zip".equals(extension)) {
            throw new RuntimeException("A " + extension + " file has been passed in the controller. Zip file expected.");
        }
        ZipInputStream zis = new ZipInputStream(fileZip.getInputStream());
        try {
            List<String> csvHolder = new ArrayList<>();
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    logger.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        logger.warn("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
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
            return csvHolder;
        } finally {
            zis.closeEntry();
            zis.close();
        }
    }
    //

    public Map<String, List<Integer>> processCsvList(List<String> csvList) throws JsonProcessingException {
        Map<String, List<Integer>> resultMarkToQuantityList = new HashMap<>();
        for (String csv : csvList) {
            logger.info("Обрабатывается строка: " + csv);
            Map<String, List<Integer>> markToQuantityList = processCsv(csv);
            for (Map.Entry<String, List<Integer>> markToQuantityListEntry : markToQuantityList.entrySet()) {
                String mark = markToQuantityListEntry.getKey();
                List<Integer> quantityList = markToQuantityListEntry.getValue();
                List<Integer> resultQuantityList = resultMarkToQuantityList.computeIfAbsent(mark, (key) -> new ArrayList<>());
                resultQuantityList.addAll(quantityList);
            }
        }
        return resultMarkToQuantityList;
    }

    public Map<String, List<Integer>> processCsv(String csv) throws JsonProcessingException {
        HashMap<String, List<Integer>> markToQuantities = new HashMap<>();
        final String[] csvRows = csv.split("\n");
        int rowNumber = 1;
        for (int j = 0; j < csvRows.length; j++) {
            if (!csvRows[j].contains("#")) {
                rowNumber++;
                String[] row = csvRows[j].split(",");
                String mark = row[0];
                String quantity = row[1];
                if (StringUtils.hasText(quantity)) {
                    try {
                        Integer parsedQuantity = Integer.parseInt(quantity);
                        List<Integer> quantityList = markToQuantities.computeIfAbsent(mark, (key) -> new ArrayList<>());
                        quantityList.add(parsedQuantity);
                    } catch (Exception exc) {
                        logger.error("Got error while parsing csv. Row number: {}. Row: {}", rowNumber, row, exc);
                        throw exc;
                    }
                } else {
                    logger.warn("Got empty quantity. Row number: {}. Row: {}", rowNumber, row);
                }
            }
        }
        return markToQuantities;

    }

    public Map<String, List<Integer>> sortQuantity(Map<String, List<Integer>> mapToQuantityList) {
        System.out.println("Нужно сделать так же:" + mapToQuantityList.toString());
        mapToQuantityList.values().forEach(list -> list.sort(Comparator.reverseOrder()));
        System.out.println("Нужно сделать так же2:" + mapToQuantityList);
        return mapToQuantityList;
    }

    public Map<String, Integer> sumByMarkWithBaseMarks(Map<String, List<Integer>> markToQuantityList) {
        Map<String, Integer> markToSumQuantity = sumByMark(markToQuantityList);
        List<String> baseMarks = Arrays.asList("mark01", "mark17", "mark23", "mark35", "markFV", "markFX", "markFT");
        baseMarks.forEach(baseMark -> {
            if (!markToSumQuantity.containsKey(baseMark)) {
                markToSumQuantity.put(baseMark, null);
            }
        });
        return markToSumQuantity;
    }

    public Map<String, Integer> sumByMark(Map<String, List<Integer>> markToQuantityList) {
        Map<String, Integer> mapAfterSort = new HashMap<>();
        for (String k : markToQuantityList.keySet()) {
            int sum = 0;
            for (int d : markToQuantityList.get(k)) {
                sum += d;
            }
            mapAfterSort.put(k, sum);
        }
        return mapAfterSort;
    }
}
