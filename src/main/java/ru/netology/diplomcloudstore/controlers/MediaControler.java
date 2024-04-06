package ru.netology.diplomcloudstore.controlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diplomcloudstore.dto.FileDataDto;
import ru.netology.diplomcloudstore.domain.FileEntityResponse;
import ru.netology.diplomcloudstore.exceptions.FileNotFoundInDatabaseException;
import ru.netology.diplomcloudstore.exceptions.InputParameterException;
import ru.netology.diplomcloudstore.exceptions.MultipartFileException;
import ru.netology.diplomcloudstore.exceptions.ProcessFileException;
import ru.netology.diplomcloudstore.services.Storable;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cloud")
public class MediaControler {

    private final Storable mediaService;

    @GetMapping("/list")
    public ResponseEntity<?> getListFilesByLimit(@RequestParam String limit) throws InputParameterException {
        try {
            Integer limitInt = Integer.parseInt(limit);
            List<FileEntityResponse> list = mediaService.getListFiles(limitInt);
            return new ResponseEntity<>(list, HttpStatus.OK);

        } catch (NumberFormatException ex) {
            String msg = String.format("MediaControler.getListFilesByLimit: Argument limit '%s' is not a Integer.", limit);
            log.error(msg);
            throw new InputParameterException(msg, 1);
        }
    }

    @PostMapping("/file")
    public ResponseEntity<?> addFile(@RequestParam String filename, @RequestParam("file") MultipartFile file) throws IOException, ProcessFileException, MultipartFileException {

        if (file.getSize() == 0) {
            String msg = String.format("MediaControler.getListFilesByLimit: MultipartFile file in request have a 0 byte");
            log.error(msg);
            throw new MultipartFileException(msg);
        }

        mediaService.save(filename, file.getBytes());
        return new ResponseEntity<>("Success upload", HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity<?> getFile(@RequestParam String filename) throws FileNotFoundInDatabaseException, ProcessFileException {
        byte[] file = mediaService.get(filename);
        return new ResponseEntity<>(file, HttpStatus.OK);
    }

    @PutMapping(path = "/file")
    public ResponseEntity<?> putFile(@RequestParam String filename, @RequestBody FileDataDto fileDataRequest) throws FileNotFoundInDatabaseException, ProcessFileException {
        fileDataRequest.setOldName(filename);
        mediaService.put(fileDataRequest);
        return new ResponseEntity<>("File name to upload", HttpStatus.OK);
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> doDelete(@RequestParam String filename) throws FileNotFoundInDatabaseException, ProcessFileException {
        mediaService.delete(filename);
        return new ResponseEntity<>("Success deleted", HttpStatus.OK);
    }
}
