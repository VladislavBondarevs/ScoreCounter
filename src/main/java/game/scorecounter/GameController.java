package game.scorecounter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/game")
@SessionAttributes({"roundScores", "team1Name", "team2Name", "team1Total", "team2Total"})
public class GameController {


    private final SimpMessagingTemplate messagingTemplate;
    private String viewMode = "gameView";
    private List<String> photos = new ArrayList<>();


    @Autowired
    public GameController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @ModelAttribute("roundScores")
    public List<RoundScore> initRoundScores() {
        List<RoundScore> roundScores = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            roundScores.add(new RoundScore(i));
        }
        return roundScores;
    }

    @ModelAttribute("team1Total")
    public int initTeam1Total() {
        return 0;
    }

    @ModelAttribute("team2Total")
    public int initTeam2Total() {
        return 0;
    }

    @ModelAttribute("team1Name")
    public String initTeam1Name() {
        return "Team 1";
    }

    @ModelAttribute("team2Name")
    public String initTeam2Name() {
        return "Team 2";
    }

    @GetMapping
    public String defaultView(@RequestParam(value = "dev", required = false) Boolean isDev) {
        return isDev != null && isDev ? "game-admin" : "game-view";
    }

    @GetMapping("/admin")
    public String adminView(Model model,
                            @ModelAttribute("roundScores") List<RoundScore> roundScores,
                            @ModelAttribute("team1Name") String team1Name,
                            @ModelAttribute("team2Name") String team2Name) {
        int team1Total = roundScores.stream().mapToInt(RoundScore::getTeam1Score).sum();
        int team2Total = roundScores.stream().mapToInt(RoundScore::getTeam2Score).sum();

        model.addAttribute("team1Total", team1Total);
        model.addAttribute("team2Total", team2Total);
        model.addAttribute("roundScores", roundScores);
        model.addAttribute("team1Name", team1Name);
        model.addAttribute("team2Name", team2Name);

        try {
            List<String> files = Files.walk(Paths.get("upload-dir"), 1)
                    .filter(path -> !path.equals(Paths.get("upload-dir")))
                    .map(path -> path.getFileName().toString())
                    .toList();
            model.addAttribute("files", files);
        } catch (IOException e) {
            model.addAttribute("files", Collections.emptyList());
        }


        return "game-admin";
    }

    @GetMapping("/view")
    public String view(Model model,
                       @ModelAttribute("roundScores") List<RoundScore> roundScores,
                       @ModelAttribute("team1Name") String team1Name,
                       @ModelAttribute("team2Name") String team2Name) {
        int team1Total = roundScores.stream().mapToInt(RoundScore::getTeam1Score).sum();
        int team2Total = roundScores.stream().mapToInt(RoundScore::getTeam2Score).sum();

        model.addAttribute("team1Total", team1Total);
        model.addAttribute("team2Total", team2Total);
        model.addAttribute("roundScores", roundScores);
        model.addAttribute("team1Name", team1Name);
        model.addAttribute("team2Name", team2Name);

        return "game-view";
    }

    @MessageMapping("/updateScores")
    @SendTo("/topic/scores")
    public List<RoundScore> broadcastScores(List<RoundScore> updatedScores) {
        System.out.println("Broadcasting scores: " + updatedScores);
        return updatedScores;
    }

    @MessageMapping("/syncGameData")
    @SendTo("/topic/gameData")
    public Map<String, Object> syncGameData(
            @ModelAttribute("roundScores") List<RoundScore> roundScores,
            @ModelAttribute("team1Name") String team1Name,
            @ModelAttribute("team2Name") String team2Name,
            @ModelAttribute("team1Total") int team1Total,
            @ModelAttribute("team2Total") int team2Total) {
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("roundScores", roundScores);
        gameData.put("team1Name", team1Name);
        gameData.put("team2Name", team2Name);
        gameData.put("team1Total", team1Total);
        gameData.put("team2Total", team2Total);
        gameData.put("photos", photos);
        System.out.println("Broadcasting game data: " + gameData);
        return gameData;
    }

    @MessageMapping("/updateNames")
    @SendTo("/topic/names")
    public Map<String, String> updateNames(Map<String, String> names) {
        System.out.println("Broadcasting names: " + names);
        return names;
    }

    private List<String> fetchPhotos() {
        List<String> photoList = new ArrayList<>();
        try {
            Path photosDir = Paths.get(System.getProperty("user.dir"), "upload-dir").toAbsolutePath().normalize();
            System.out.println("Looking for photos in directory: " + photosDir);

            if (!Files.exists(photosDir)) {
                System.out.println("Directory does not exist: " + photosDir);
                return photoList;
            }

            photoList = Files.walk(photosDir, 1)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            System.out.println("Files found: " + photoList);
        } catch (IOException e) {
            System.err.println("Error while fetching photos: " + e.getMessage());
            e.printStackTrace();
        }
        return photoList;
    }

    @MessageMapping("/updateViewMode")
    public void updateViewMode(@Payload String viewMode) {
        this.viewMode = viewMode;
        if ("photosOnly".equals(viewMode)) {
            System.out.println("Before fetching photos: " + photos);
            photos = fetchPhotos(); // Загружаем фотографии
            System.out.println("After fetching photos: " + photos);
        }
        messagingTemplate.convertAndSend("/topic/viewMode", viewMode);
        messagingTemplate.convertAndSend("/topic/photos", photos);
    }


    @GetMapping("/photos")
        @ResponseBody
        public List<String> getPhotos() {
            return photos;
        }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path path = Paths.get("upload-dir/" + file.getOriginalFilename());
            Files.write(path, file.getBytes());
            return "redirect:/game/admin";
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/game/admin?error=true";
        }
    }

    private List<RoundScore> currentRoundScores = new ArrayList<>();

    @PostMapping("/updateRoundScores")
    @ResponseBody
    public List<RoundScore> updateRoundScores(@RequestBody List<RoundScore> roundScores) {
        this.currentRoundScores = roundScores;
        System.out.println("Received scores: " + currentRoundScores);
        return currentRoundScores;
    }


    @GetMapping("/getRoundScores")
    @ResponseBody
    public List<RoundScore> getRoundScores() {
        return currentRoundScores.isEmpty() ? initRoundScores() : currentRoundScores;
    }

    private String team1Name = "Team 1";
    private String team2Name = "Team 2";

    @GetMapping("/getTeamNames")
    @ResponseBody
    public Map<String, String> getTeamNames() {
        Map<String, String> teamNames = new HashMap<>();
        teamNames.put("team1Name", team1Name);
        teamNames.put("team2Name", team2Name);
        return teamNames;
    }

    @PostMapping("/updateTeamNames")
    @ResponseBody
    public Map<String, String> updateTeamNames(@RequestBody Map<String, String> names) {
        if (names.containsKey("team1Name")) {
            this.team1Name = names.get("team1Name").trim();
        }
        if (names.containsKey("team2Name")) {
            this.team2Name = names.get("team2Name").trim();
        }

        Map<String, String> updatedNames = new HashMap<>();
        updatedNames.put("team1Name", this.team1Name);
        updatedNames.put("team2Name", this.team2Name);
        return updatedNames;
    }

    @PostMapping("/clearRoundScores")
    @ResponseBody
    public List<RoundScore> clearRoundScores() {
        for (RoundScore roundScore : currentRoundScores) {
            roundScore.setTeam1Score(0);
            roundScore.setTeam2Score(0);
        }
        System.out.println("Round scores cleared!");
        messagingTemplate.convertAndSend("/topic/clear", "clear");

        return currentRoundScores;
    }

    @MessageMapping("/clearScores")
    @SendTo("/topic/clear")
    public String broadcastClearScores() {
        for (RoundScore roundScore : currentRoundScores) {
            roundScore.setTeam1Score(0);
            roundScore.setTeam2Score(0);
        }
        System.out.println("Broadcasting clear table command");
        return "clear";
    }


}
