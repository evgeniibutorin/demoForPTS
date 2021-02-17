package com.example.demo.service;

import com.example.demo.ApplicationSettings;
import com.example.demo.dto.Mark;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class MarkService {

    private final ApplicationSettings applicationSettings;
    private final List<Mark> baseMarksList;

    public MarkService(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
        this.baseMarksList = applicationSettings.getBaseMarks().stream().map(Mark::new).collect(Collectors.toList());
    }

    private final CsvMapper mapper = new CsvMapper()
            .enable(CsvParser.Feature.ALLOW_COMMENTS)
            .enable(CsvParser.Feature.WRAP_AS_ARRAY);


    public Map<Mark, List<Long>> sortQuantity(Map<Mark, List<Long>> mapToQuantityList) {
        mapToQuantityList.values().forEach(list -> list.sort(Comparator.reverseOrder()));
        return mapToQuantityList;
    };

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
                    log.debug("There is directory in the archive.");
                } else {
                    String entryName = FilenameUtils.getExtension(zipEntry.getName());
                    if (!entryName.equals("csv")) {
                        log.warn("A " + entryName + " file has been passed in the archive. Zip file has to include csv files.");
                        //todo: выдать исключение не инфор а варн если лог
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

    public Map<Mark, List<Long>> processCsv(String csv) throws IOException {
        HashMap<Mark, List<Long>> markToQuantities = new HashMap<>();
        MappingIterator<String[]> csvRows = mapper.readerFor(String[].class).readValues(csv);

        int rowNumber = 1;
        while (csvRows.hasNext()) {
            rowNumber++;
            String[] row = csvRows.next();
            Mark mark = new Mark(row[0]);
            String quantity = row[1];
            // if quantity = "" (empty), we should not try to parse it to number
            if (StringUtils.hasText(quantity)) {
                try {
                    Long parsedQuantity = Long.parseLong(quantity);
                    List<Long> quantityList = markToQuantities.computeIfAbsent(mark, (key) -> new ArrayList<>());
                    quantityList.add(parsedQuantity);
                } catch (Exception exc) {
                    log.error("Got error while parsing csv. Row number: {}. Row: {}", rowNumber, row, exc);
                    // todo: map to normal exception (with response status 4xx)
                    throw exc;
                }
            } else {
                log.warn("Got empty quantity. Row number: {}. Row: {}", rowNumber, row);
            }
        }
        log.debug("Successfully processed csv. Row numbers: {}", rowNumber);
        return markToQuantities;
    }

    public Map<Mark, List<Long>> processCsvList(List<String> csvList) throws IOException {
        Map<Mark, List<Long>> resultMarkToQuantityList = new HashMap<>();
        for (String csv : csvList) {
            Map<Mark, List<Long>> markToQuantityList = processCsv(csv);
            for (Map.Entry<Mark, List<Long>> markToQuantityListEntry : markToQuantityList.entrySet()) {
                Mark mark = markToQuantityListEntry.getKey();
                List<Long> quantityList = markToQuantityListEntry.getValue();
                List<Long> resultQuantityList = resultMarkToQuantityList.computeIfAbsent(mark, (key) -> new ArrayList<>());
                resultQuantityList.addAll(quantityList);
            }
        }
        return resultMarkToQuantityList;
    }

    public Map<Mark, Long> sumByMark(Map<Mark, List<Long>> markToQuantityList) {
        Map<Mark, Long> mapAfterSort = new HashMap<>();
        for (Mark k : markToQuantityList.keySet()) {
            long sum = 0;
            for (long d : markToQuantityList.get(k)) {
                sum += d;
            }
            mapAfterSort.put(k, sum);
        }
        return mapAfterSort;
    }

    public Map<Mark, Long> sumByMarkWithBaseMarks(Map<Mark, List<Long>> markToQuantityList) {
        Map<Mark, Long> markToSumQuantity = sumByMark(markToQuantityList);
        baseMarksList.forEach(baseMark -> {
            if (!markToSumQuantity.containsKey(baseMark)) {
                markToSumQuantity.put(baseMark, null);
            }
        });

        return markToSumQuantity;
    }
}





