package com.example.demo.MarkService;

import com.example.demo.entity.MarkQuantity;
import com.example.demo.repository.MarkQuantityRepository;
import com.example.demo.repository.MarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarkQuantityService {

private final MarkQuantityRepository markQuantityRepository;
private final MarkRepository markRepository;

    public MarkQuantityService(MarkQuantityRepository markQuantityRepository, MarkRepository markRepository) {
        this.markQuantityRepository = markQuantityRepository;
        this.markRepository = markRepository;
    }


    public List<MarkQuantity> amountCounter(final MultipartFile file) throws Exception {
        final List<MarkQuantity> quantity = new ArrayList<>();
        try {
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) { // BufferedReader класс считывает текст из символьного потока ввода, буферизируя прочитанные символы. InputStreamReader получает данные из потока, считывает байты и декодирует их в символы
                String line;
                while ((line = br.readLine()) != null) {
                    final String[] data = line.split(",");

                    final MarkQuantity markQuantity = new MarkQuantity();
                    markQuantity.setQuantity(Integer.parseInt(data[1]));
                    markQuantity.setMark(markRepository.findFirstByName(data[0]));
                    //mark.setQuantity(Integer.parseInt(data[1]));
                    quantity.add(markQuantity);
                    //logger.info("Object " + data[0] + " has been added to the database");

                }
                return markQuantityRepository.saveAll(quantity);
            }
        } catch (final IOException e) {
            //logger.error("Failed to parse CSV file {}", e); // объявление логера ошибок
            throw new Exception("Failed to parse CSV file {}", e);
        }
    }






}
