package game.scorecounter.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class FileUploadController {

    @Value("${upload-dir}")
    private String uploadDir;

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService){
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) {
        try {
            Path uploadDir = Paths.get("upload-dir").toAbsolutePath().normalize();
            List<String> files = Files.walk(uploadDir, 1)
                    .filter(path -> !path.equals(uploadDir))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            model.addAttribute("files", files);
        } catch (IOException e) {
            model.addAttribute("files", Collections.emptyList());
            e.printStackTrace();
        }
        return "game-admin";
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("upload-dir").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files, RedirectAttributes redirectAttributes) {
        try {
            Path uploadDir = Paths.get("upload-dir").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            for (MultipartFile file : files) {
                Path destinationFile = uploadDir.resolve(file.getOriginalFilename()).normalize();

                Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            redirectAttributes.addFlashAttribute("message", "Files uploaded successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload files!");
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/files")
    @ResponseBody
    public List<String> listFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get("upload-dir"))) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @PostMapping("/delete-file")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, String> request) {
        String filename = request.get("filename");
        try {
            Path filePath = Paths.get("upload-dir").resolve(filename).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found: " + filename);
            }
            Files.delete(filePath);
            return ResponseEntity.ok("File deleted: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + filename);
        }
    }

    @GetMapping("/game-view")
    @ResponseBody
    public Map<String, Object> gameView() {
        Map<String, Object> response = new HashMap<>();
        response.put("photos", selectedPhotos);
        return response;
    }

    private List<String> selectedPhotos = new ArrayList<>();

    @MessageMapping("/selectPhotos")
    @SendTo("/topic/photos")
    public List<String> selectPhotos(@Payload List<String> selected) {
        if (selected == null || selected.isEmpty()) {
            throw new IllegalArgumentException("Selected photos cannot be null or empty");
        }
        selectedPhotos = new ArrayList<>(selected);
        System.out.println("Selected photos: " + selectedPhotos);
        return selectedPhotos;
    }
}
