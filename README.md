# LinkedIn-clone Application with Java and JavaFX

## Description

PolyLinked is a LinkedIn-clone application built using Java and JavaFX. The client-side application follows MVC (Model-View-Controller) architecture and The server-side programm handles incoming requests, which follows DAO (Data Access Object) design pattern.
<br/>This was the project of advanced programming course at AmirKabir University (@AUT-CE).

## Table of Contents

- [Installation](#installation)
- [Features](#features)
- [Endpoints](#endpoints)
- [Screenshots](#screenshots)
- [Technologies](#technologies)
- [License](#license)
- [Contribution](#contribution)

## Installation

To run the application, follow these steps:

1. Clone the repository to your local machine.

2. Build and run the server programm using maven.

3. Build and run the client application using maven.

Notice that The server is running localhost.

## Features

1. User Management
   - Create, retrieve, update, and delete users.
   - Add bios and profile images.
   - Follow, unfollow and connect other users.

2. Post Management
   - Create posts and repost.
   - Like and comment.
   - View customized homePage.
   
3. User Profile
   - View user profiles.
   - Edit profile details.
   - Add Education, Skills and more.

4. Messaging, Notifications and Searching
   - View and send messages.
   - View notifications.
   - Search users and hashtags.

## Endpoints

Below are the available API endpoints in the server:

```java
api/users
api/users/login
api/users/profiles"
api/users/educations
api/users/skills
api/users/followers 
api/users/followings
api/users/callInfo

api/follows
api/connections

api/posts
api/likes
api/comments

api/messages
api/newsfeed

api/search
api/resources
```

## Screenshots

<img width="1500" alt="login" src="https://github.com/Javad-Ak/PolyLinked/blob/main/document/screenshots/login.png"> <img width="1500" alt="signup" src="https://github.com/Javad-Ak/PolyLinked/blob/main/document/screenshots/signup.png">
<img width="1500" alt="home" src="https://github.com/Javad-Ak/PolyLinked/blob/main/document/screenshots/home.png"> <img width="1500" alt="profile" src="https://github.com/Javad-Ak/PolyLinked/blob/main/document/screenshots/profile.png">

## Technologies

- Java
- JavaFX
- SQLite
- HTTP Server
- JWT authentication
- MVC Architecture
- JSON serialization
- DAO design pattern

## License

PolyLinked is licensed under the MIT License. You are free to modify and distribute the project according to the terms of the license.

## Contribution

Contributions to PolyLinked are welcome! If you want to contribute, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and ensure that the codebase passes all tests.
4. Submit a pull request describing your changes.
Notice that in order to open the fxml files in scenebuilder, you need to download their dependencies jar files to scenebuilder.
