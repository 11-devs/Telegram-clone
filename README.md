<div align="center">

<h1>Telegram Clone Desktop</h1>

<p>
  An advanced, feature-rich desktop chat application built from the ground up with a custom networking framework — demonstrating deep knowledge of software architecture, real-time communication, and modern UI development.
</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-2d3436.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-23-red.svg)](https://www.oracle.com/java/technologies/downloads/)
[![JavaFX Version](https://img.shields.io/badge/JavaFX-21-0984e3.svg)](https://openjfx.io/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-1e3799.svg)](https://www.postgresql.org/)
[![Framework](https://img.shields.io/badge/Framework-JSocket2-6c5ce7.svg)](https://github.com/MahdiHoseinpoor/JSocket2)
[![rating](https://img.shields.io/badge/%231-Advanced_Programming_Final_Project-f7dc6f.svg)]()


<img width="1536" height="1024" alt="TelegramLogo4" src="https://github.com/user-attachments/assets/1037a0e7-451a-4653-a397-cba433e8edd4" />

> **Disclaimer**: This project is not affiliated with, endorsed by, or associated with Telegram Messenger LLP or any of its official products.
All names, logos, and references to "Telegram" are used for educational and demonstration purposes only.
The goal of this project is to study and recreate real-time messaging architectures and modern UI design patterns in Java.

</div>

---

## 📖 Introduction

**Telegram Clone Desktop** is a modern, full-featured chat application that replicates core functionalities of Telegram Desktop.  
It is composed of both **client** and **server** sides:

- 🖥 **Client:** Built with **JavaFX**, offering a clean, responsive, and interactive UI.  
- ⚙️ **Server:** Powered by a custom **RPC-based framework** using **PostgreSQL** for persistence.  
- 🌐 **Networking:** Real-time communication over sockets with event broadcasting and RPC calls.  

---

## ✨ Features

- 🪄 Modern desktop UI with JavaFX & JFoenix  
- 🔐 Authentication & session management  
- ⚡ Real-time private and group chats  
- 💬 Typing indicators & live message delivery  
- 🧱 Persistent data with JPA/Hibernate  
- 🧩 Modular architecture (Client / Server / Shared)  
- 🔔 Event-driven updates for smooth interactivity  

---

## 🏗️ Architecture Overview

- **Client:**  
  JavaFX UI, controllers for chat, menus, settings, and authentication.  

- **Server:**  
  RPC controllers for business logic (`ChatRpcController`, `ContactRpcController`, etc.), DAO layer, and event/session management.  

- **Networking:**  
  Custom RPC protocol built on sockets — enabling efficient request/response and event streaming.  

- **Database:**  
  PostgreSQL + JPA/Hibernate for user, message, and contact storage.  

```
Client (JavaFX)
     ↓
RPC Networking (JSocket2)
     ↓
Server (Business Logic + DAO)
     ↓
Database (PostgreSQL)
```

---

## ⚙️ Prerequisites

Before building and running the project, ensure you have:

- ☕ **Java 23 or higher**  
- 🧰 **Gradle**  
- 🐘 **PostgreSQL**  
- 🌐 Properly configured ports for networking  

---

## 🚀 Installation & Run

1️⃣ **Clone the repository**
```bash
git clone https://github.com/11-devs/Telegram-clone.git
cd Telegram-clone
```

2️⃣ **Setup the database**
- Create a new PostgreSQL database  
- Update connection settings (URL, username, password) in your configuration file  
- *(Optional)* Run initial migration scripts or data seeders  

3️⃣ **Build and run the server**

4️⃣ **Build and run the client**

---

## 🧑‍💻 Usage

- 🪪 Sign up or log in  
- 💬 Send and receive messages  
- 👥 Create group chats  
- ⚡ Enjoy real-time typing indicators  
- ⚙️ Manage account settings and preferences  

---

## 🎥 Demo Video

Check out the demo video:  
👉 [**▶️ Telegram Clone Demo**](telegramclone-video.mkv)


---

## 📜 License

This project is distributed under the **MIT License**.  
See the [LICENSE](LICENSE) file for more details.

---

## 👨‍💻 Credits

Developed by:

- [**Aryan Ghasemi**](https://github.com/AryanGh-imp)  
- [**Mahdi Hoseinpoor**](https://github.com/MahdiHoseinpoor)  
- [**Ali Ghaedrahmat**](https://github.com/AliGhaedrahmat)

<div align="center">

</div>
