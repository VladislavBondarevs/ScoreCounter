# 🏆 ScoreCounter
ScoreCounter is a Java Spring Boot application for real-time game scoring between two teams.
It supports dynamic score updates, live synchronization using WebSockets, and photo uploads used as advertisements or banners.
The application also works fully offline, ensuring functionality without an internet connection.

## 📌 Features

- 🎯 Track scores for 16 rounds between two teams
- 🧠 Realtime score updates via WebSocket (STOMP over SockJS)
- 📸 Upload and display photos during the game
- ✏️ Set and update team names dynamically
- 🔃 View switching and admin panel
- 🧼 Clear/reset scoreboard functionality

## ⚙️ Technologies

- Java 21
- Spring Boot
- Spring WebSocket (SockJS + STOMP)
- Thymeleaf
- JavaScript (Vanilla)
- HTML5 / CSS3

## 📁 Project Structure

src/
├── main/
│ ├── java/game/scorecounter/
│ │ ├── ScorecounterApplication.java
│ │ ├── GameController.java
│ │ ├── RoundScore.java
│ │ ├── WebSocketConfig.java
│ │ └── storage/
│ │ ├── FileUploadController.java
│ │ ├── FileSystemStorageService.java
│ │ ├── StorageService.java
│ │ ├── StorageException.java
│ │ ├── StorageFileNotFoundException.java
│ │ └── StorageProperties.java
│ └── resources/
│ ├── static/
│ │ ├── images/
│ │ └── js/
│ ├── templates/
│ │ ├── game-admin.html
│ │ └── game-view.html
│ └── application.properties

## 🚀 How to Run
mvn clean spring-boot:run

Open in browser:

http://localhost:8080
## 🔌 WebSocket Functionality
WebSocket initialized in WebSocketConfig.java

Live communication via /ws endpoint

Subscriptions:

/topic/scores — receives updated scores

/topic/names — receives team name changes

## 📂 File Uploads
Users can upload multiple photos (via FileUploadController)

Uploaded files stored in /upload-dir/

Displayed in the admin view with delete options

## 🛠 Config
All app settings and upload path in application.properties

## 📝 License
This project is for demonstration and educational purposes only.

## 👤 Author
Vladislav Bondarevs