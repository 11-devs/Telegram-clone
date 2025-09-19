<div align="center">
  <h1>Telegram Clone Desktop</h1>
  <p>
    An advanced, feature-rich desktop chat application built from the ground up with a custom networking framework, demonstrating deep knowledge of software architecture, real-time communication, and modern UI development.
  </p>

  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  [![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
  [![JavaFX Version](https://img.shields.io/badge/JavaFX-21-orange.svg)](https://openjfx.io/)
  [![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)](https://www.postgresql.org/)
  [![Framework](https://img.shields.io/badge/Framework-JSocket2-brightgreen.svg)](https://github.com/MahdiHoseinpoor/JSocket2)

</div>

---

This project is a high-fidelity clone of the Telegram desktop application, developed as a final project for an Advanced Programming course. It features a robust client-server architecture, a custom-built, secure networking framework (**[JSocket2](https://github.com/MahdiHoseinpoor/JSocket2)**), and a polished, responsive user interface using **JavaFX**.

The application supports real-time one-on-one and group messaging, complete with modern features like typing indicators, message editing/deleting, file transfers, and end-to-end session encryption.

## ✨ Key Features

The application is packed with features that mirror a professional messaging client.

#### Core Messaging & Chats
- **Real-Time Private & Group Chats:** Instantaneous message delivery in both one-on-one and multi-user group chats.
- **Advanced Message Interactions:**
  - **Edit & Delete:** Modify or retract your messages after sending.
  - **Reply & Forward:** Quote-reply to specific messages or forward them to other chats.
  - **Text Formatting:** Use Markdown-style formatting for **\*\*bold\*\***, **\_\_italic\_\_**, **++underline++**, and **||spoiler||** text.
- **Media & File Sharing:**
  - **File Transfers:** Send and receive any file type with progress indicators for uploads and downloads.
  - **Voice Messages:** Record, send, and play back voice notes with an integrated audio player.
  - **Video Player:** In-app video player for viewing shared video files.
- **Real-Time Feedback:**
  - **Typing Indicators:** See when other users are typing in real-time.
  - **Message Status:** Track message status with icons for *Sending*, *Delivered*, and *Read*.

#### Account & User Management
- **Secure Authentication:** Multi-step authentication flow with phone number, OTP (via SMS or in-app), and a "Cloud Password".
- **Account Management:**
  - **Profile Customization:** Users can edit their name, username, bio, and profile picture.
  - **Password Recovery:** Securely reset a forgotten Cloud Password via a registered email address.
  - **Account Reset:** Option to completely reset an account if all recovery methods are lost.
- **Contact Management:** A dedicated contacts section to view, add, and manage contacts.

#### UI & UX
- **Modern & Responsive UI:** Built with JavaFX, featuring a clean, intuitive, and animated user interface.
- **Sidebar & Settings Menus:** Fully-featured sidebar menu and a multi-layered settings dialog for comprehensive customization.
- **Theme Management:** Seamlessly switch between **Dark** and **Light** themes.
- **System Notifications:** Receive native desktop notifications for new messages.
- **Dialogs & Alerts:** Custom-styled, non-blocking dialogs for a smooth user experience.

---

## 🏗️ Technical Architecture & Deep Dive

This project's architecture is its cornerstone, showcasing a deep understanding of software engineering principles.

### Custom Networking Framework: [JSocket2](https://github.com/MahdiHoseinpoor/JSocket2)

All client-server communication is powered by **JSocket2**, a custom, open-source networking framework built specifically for high-performance, real-time applications. **It is available as a standalone project [here](https://github.com/MahdiHoseinpoor/JSocket2)**. The framework handles all the complexities of low-level networking, allowing the application to focus on business logic.

1.  **Custom Binary Protocol:**
    - A robust TCP-based protocol using **magic bytes** for stream synchronization.
    - A fixed-size `MessageHeader` containing a request `UUID`, `MessageType`, encryption flags, and body lengths.
    - A flexible `MessageBody` split into `Metadata` (for routing/context) and `Payload` (the actual data).

2.  **End-to-End Session Encryption:**
    - **Asymmetric Handshake:** The server sends its **RSA public key** upon connection. The client generates a symmetric **AES key**, encrypts it with the server's public key, and sends it back.
    - **Symmetric Session:** All subsequent communication for the session is encrypted using the shared AES key, ensuring privacy and security.

3.  **RPC (Remote Procedure Call) System:**
    - An elegant RPC layer allows the client to call server-side methods as if they were local.
    - **Client:** The `RpcCaller` class provides a simple API (e.g., `chatService.sendMessage(...)`).
    - **Server:** The `RpcDispatcher` routes incoming calls to the appropriate `RpcController` (e.g., `MessageRpcController`) and action.

4.  **Real-Time Event Hub (Pub/Sub):**
    - A powerful event-driven system for pushing real-time updates from the server to clients.
    - **Server:** The `EventBase` system (e.g., `NewMessageEvent`) publishes events to specific users or topics via the `ServerSessionManager`.
    - **Client:** Event subscribers (`NewMessageSubscriber`, `UserTypingSubscriber`) listen for events and update the UI through the `ChatUIService`, decoupling the network layer from the UI controllers.

5.  **Managed File Transfer Protocol:**
    - A resilient file transfer manager that handles both uploads and downloads.
    - **Chunking:** Large files are split into manageable chunks (64KB) for efficient and reliable transfer.
    - **Progress Tracking:** An `IProgressListener` interface provides real-time feedback on transfer progress.
    - **Resumable Transfers:** A `.info` file system tracks transfer state, allowing for future implementation of resumable downloads.

6.  **Dependency Injection (DI) Container:**
    - A custom DI framework (`ServiceCollection`, `ServiceProvider`, `@Inject`) inspired by modern DI patterns.
    - Manages object lifecycles (**Singleton**, **Scoped**, **Transient**) and automatically resolves dependencies, promoting clean, decoupled, and testable code.

### Server-Side Architecture

The server is built using a layered, service-oriented architecture.

- **Controller Layer:** `RpcController` classes (`AccountRpcController`, `ChatRpcController`, etc.) define the public API endpoints.
- **Service Layer:** Services (`AuthService`, `ClientLifecycleManager`) encapsulate complex business logic.
- **Data Access Layer (DAO):** A generic `GenericDAO<T>` class implements the Repository Pattern, abstracting database operations for all entities.
- **Persistence:** **PostgreSQL** is used for robust data storage, managed by **JPA/Hibernate** for object-relational mapping (ORM). The schema includes soft deletes (`@SQLDelete`) for data integrity.
- **Session Management:** The `ServerSessionManager` tracks all active client sessions, manages user online status, and is used by the Event Hub to broadcast messages to connected clients.

### Client-Side Architecture

The client application is a well-structured JavaFX project following modern UI design patterns.

- **MVC-like Pattern:**
  - **Models:** `ChatViewModel` and other `ViewModel` classes use JavaFX properties to enable seamless data binding with the UI.
  - **Views:** FXML files define the UI layout, separated from application logic.
  - **Controllers:** `MainChatController` and other controller classes handle user input, manage UI state, and orchestrate calls to the service layer.
- **Concurrency:** JavaFX `Task`s are used for all long-running operations (network calls, file I/O) to ensure the UI remains responsive at all times.
- **Service Layer:** Client-side services (`ChatService`, `FileDownloadService`) abstract all communication with the server's RPC endpoints, providing a clean API for the controllers.
- **Event-Driven UI Updates:** The client subscribes to server events. The `ChatUIService` acts as a mediator, receiving events from subscribers and updating the `MainChatController` on the JavaFX Application Thread, ensuring thread safety.

---

## 🚀 Technologies & Skills Demonstrated

- **Languages & Frameworks:**
  - **Java 21** & **JavaFX 21** (with FXML)
  - **JPA / Hibernate** (for ORM)
  - **JFoenix** (for Material Design components)
  - **JUnit 5** (for unit testing)

- **Architecture & Design Patterns:**
  - **Client-Server Architecture**
  - **Layered Architecture** (Controller, Service, DAO)
  - **Repository Pattern** (`GenericDAO`)
  - **Singleton Pattern** (`AppConnectionManager`, `ThemeManager`)
  - **Builder Pattern** (`ChatViewModelBuilder`)
  - **Observer Pattern** (Event Hub / Pub-Sub System)
  - **Dependency Injection** & **Inversion of Control**

- **Networking & Concurrency:**
  - **Custom Framework Development ([JSocket2](https://github.com/MahdiHoseinpoor/JSocket2))**
  - **Low-Level TCP/IP Socket Programming**
  - **Custom Binary Protocol Design**
  - **Remote Procedure Call (RPC)** implementation
  - **Multithreading** (JavaFX `Task`, `ExecutorService`)
  - **Asynchronous Programming** (`CompletableFuture`)

- **Database:**
  - **PostgreSQL**
  - **Database Schema Design** & **Entity Relationships** (`@ManyToOne`, `@OneToMany`, etc.)
  - **Soft Deletes** (`@SQLDelete`, `@Where`) for data integrity.

- **Security:**
  - **Hybrid Encryption:** RSA for key exchange, AES for session data.
  - **Secure Password Handling:** Hashing with SHA-256.

---

## 🛠️ Installation & Setup

Follow these steps to get the project running locally.

### Prerequisites
- **JDK 21** or newer.
- **Gradle 8.5** or newer.
- **PostgreSQL** server (version 14 or higher recommended).
- An IDE like IntelliJ IDEA or Eclipse.

### 1. Database Setup
1.  Ensure your PostgreSQL server is running.
2.  The application will automatically attempt to create the database specified in `persistence.xml`.
3.  By default, it looks for a database named `telegramclone` and connects using user `postgres` with password `12345678`.
4.  To change these settings, modify the properties in `src/main/resources/META-INF/persistence.xml`:
    ```xml
    <property name="jakarta.persistence.jdbc.url" value="jdbc:postgresql://localhost:5432/your_db_name"/>
    <property name="jakarta.persistence.jdbc.user" value="your_username"/>
    <property name="jakarta.persistence.jdbc.password" value="your_password"/>
    ```

### 2. Run the Data Seeder (Optional but Recommended)
To populate the database with realistic sample data (users, chats, messages), run the `DataSeeder` class:
```
▶ Run Server.DataSeeder.main()
```

### 3. Run the Server
Execute the `main` method in the `Server` class to start the server:
```
▶ Run Server.Server.main()
```
The server will start listening on port `8587`.

### 4. Run the Client
Execute the `main` method in the `Main` class to launch the client application:
```
▶ Run Client.Main.main()
```
You can now register a new account using a phone number (e.g., `+989123456789` for the pre-seeded "My Account") or log in to an existing one.

---

## 📜 License

This project is licensed under the MIT License. See the `LICENSE` file for more details.

---

## 👥 Authors & Credits

This project was developed by:

- **Aryan Ghasemi** - ([@AryanGh-imp](https://github.com/AryanGh-imp))
- **Mahdi Hoseinpoor** - ([@MahdiHoseinpoor](https://github.com/MahdiHoseinpoor))
- **Ali Ghaedrahmat** - ([@AliGhaedrahmat](https://github.com/AliGhaedrahmat))
