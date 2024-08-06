# Social Network Mobile Application

## Project Overview

This repository contains the Android mobile client for the Social Network application. It complements the web client by providing a native mobile experience. The Android application communicates with the server backend to provide functionalities like user registration, posting, commenting, and more.

### Applications
- **Server**: Java Spring Boot backend. [Server Repository](https://github.com/anna02272/Social-Network)
- **Web Client**: Angular frontend.
- **Mobile Client**: Android application. [Mobile App Repository](https://github.com/anna02272/Social-Network_mobile)

## Prerequisites

1. **Java Development Kit (JDK) 11 or higher**
2. **Android Studio**
3. **A device or emulator with Android API level 21 (Lollipop) or higher**
4. **Internet connection**

## Steps to Launch

### Step 1: Clone the Repository

Clone the Android mobile app repository to your local machine:
```bash
git clone https://github.com/anna02272/Social-Network_mobile
```

### Step 2: Open the Project in Android Studio

1. Open Android Studio.
2. Click on `File -> Open...` and select the cloned repository folder.
3. Android Studio will import the project and sync the Gradle files.

### Step 3: Load Gradle Dependencies

Ensure all Gradle dependencies are downloaded. Android Studio should automatically start syncing the project. If it doesn't, you can manually sync it by clicking on `File -> Sync Project with Gradle Files`.

### Step 4: Ensure the Server is Running

Ensure that the server backend is running by following the instructions in the [server repository](https://github.com/anna02272/Social-Network).

### Step 5: Configure Network

To enable communication between the mobile application and the server, both must be connected to the same network. Find the IP address of the device where the server is running. Access the server through the IP address and port (e.g., `http://192.168.0.26:8080`).

Since you don’t want to hardcode the IP address in the source code, save it in the `local.properties` file in the root of your project. Add the following line:
```properties
ip_addr=192.168.0.26
```

### Step 6: Run the Application

1. Connect your Android device to your computer or start an emulator.
2. In Android Studio, click on the `Run` button or press `Shift + F10`.
3. Select the device or emulator to run the application.

## Functionalities

- **User Registration:** Register new users.
- **Login and Logout:** Log in and out of the system.
- **Posting:** Create posts with or without images.
- **Commenting:** Reply to posts and comments.
- **User Reactions:** Like, dislike, and heart posts and comments.
- **Profile Management:** Change password, update profile data including display name, description, and profile picture.
- **Search:** Search for users and manage friend requests.
- **Group Management:** Create and administer groups, handle join requests, and manage group content.

## Technologies Used

- **Kotlin**
- **Retrofit for Networking**
- **Firebase for Cloud Storage**
- **Android Jetpack Components**

## Non-functional Requirements

- User authentication using username and password.
- Authorization using the token mechanism.
- Log messages about important events during application execution.
