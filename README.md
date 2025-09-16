# Telegram‑Clone

A lightweight **Telegram clone** built with a client‑server architecture for real‑time messaging.

<img width="1536" height="1024" alt="TelegramLogo3" src="https://github.com/user-attachments/assets/c9a6315f-0b02-4be0-a5ed-f66ae3ebf1a6" />


















## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation & Run](#installation--run)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Introduction

This project is a **Telegram desktop clone** with both client and server parts.  
The client is built using **JavaFX**, while the server uses a custom RPC‑based framework and **PostgreSQL** for persistence.  
It provides real‑time messaging, authentication, contact management, and group chat functionalities.

---

## Features

- Modern desktop UI with JavaFX and JFoenix  
- Authentication & session management  
- Real‑time message delivery  
- Group and private chats  
- Data persistence with JPA/Hibernate  
- Modular structure (Client / Server / Shared models)  
- Event‑based real‑time features (typing indicators, new message notifications)  

---

## Architecture

- **Client**: JavaFX UI, controllers for chat, menu, settings, and authentication.  
- **Server**: RPC controllers for business logic (ChatRpcController, ContactRpcController, etc.), DAO layer, and session/event management.  
- **Networking**: Custom RPC protocol over sockets, supporting request‑response and real‑time event broadcasting.  
- **Database**: PostgreSQL with JPA/Hibernate for storing users, messages, and contacts.  

---

## Prerequisites

- Java 23 or higher  
- Gradle  
- PostgreSQL  
- Properly configured ports for networking  

---

## Installation & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/11-devs/Telegram-clone.git
   cd Telegram-clone
   ```

2. Setup the database:
   - Create a PostgreSQL database  
   - Update connection settings (URL, username, password) in the config file  
   - (Optional) Run initial migration scripts (Data Seeder)

3. Build and run the server:

4. Build and run the client:

---

## Usage

- Sign up / log in  
- View chat list  
- Send and receive messages  
- Group chats  
- Real‑time typing indicators  
- Change account settings  

---

## Contributing

Contributions are welcome!  
- Fork the repo and create a new branch for your feature or bugfix.  
- Make sure the project builds and runs correctly.  
- Open a pull request with a clear description of your changes.  

---

## License
**MIT License**:
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

Developed by:

[Aryan Ghasemi](https://github.com/AryanGh-imp)

[Mahdi Hoseinpoor](https://github.com/MahdiHoseinpoor)

[Ali Ghaedrahmat](https://github.com/AliGhaedrahmat)

Special thanks to the open-source community and the creators of Java, JavaFX, PostgreSQL, and other tools that made this project possible.
