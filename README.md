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

### Step 4: Set Up Firebase

1. **Create a Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Click on `Add project` and follow the steps to create a new project.

2. **Add Firebase to Your Android Project**:
   - In the Firebase console, click on `Add app` and select `Android`.
   - Register your app with the package name (e.g., `com.example.socialnetwork`).
   - Download the `google-services.json` file.
   - Place the `google-services.json` file in the `app/` directory of your Android project.

### Step 5: Ensure the Server is Running

Ensure that the server backend is running by following the instructions in the [server repository](https://github.com/anna02272/Social-Network).

### Step 6: Configure Network

To enable communication between the mobile application and the server, both must be connected to the same network. Find the IP address of the device where the server is running. Access the server through the IP address and port (e.g., `http://192.168.0.26:8080`).

Since you don’t want to hardcode the IP address in the source code, save it in the `local.properties` file in the root of your project. Add the following line:
```properties
ip_addr=192.168.0.26
```

### Step 7: Run the Application

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

## Images of project

### Login and Register
<img src="https://github.com/user-attachments/assets/5ca88b4b-2d77-4a65-804b-824c3f4857f5" width="20%">
<img src="https://github.com/user-attachments/assets/dc279adf-a038-44b7-9bad-5a1b9acfcfb2" width="20%">

### Posts and reactions
<img src="https://github.com/user-attachments/assets/c45c6d8e-1c6b-42e3-a6d9-cd7caa410c13" width="20%">

### Create and edit post
<img src="https://github.com/user-attachments/assets/f4fa9511-7214-4cff-a66a-483fcac9c1ef" width="20%">
<img src="https://github.com/user-attachments/assets/42d99a26-60ac-4e76-80ae-1368d3794a92" width="20%">

### Comments
<img src="https://github.com/user-attachments/assets/40e48882-5e3c-4a0f-ab15-97fc1d694c50" width="20%">

### Groups and create group
<img src="https://github.com/user-attachments/assets/38f09a2d-c9a9-4fdf-bd53-a60f1725900b" width="20%">
<img src="https://github.com/user-attachments/assets/aa24f352-8519-4651-bd7c-793da91bc674" width="20%">
<img src="https://github.com/user-attachments/assets/7a0aa703-6e9f-4c2e-9084-f85332e38369" width="20%">

### Group requests and members
<img src="https://github.com/user-attachments/assets/d46f2c28-291b-4f8b-b786-ca4fe3961953" width="20%">
<img src="https://github.com/user-attachments/assets/e4ce684e-3f6a-46be-9351-bba509f5695e" width="20%">

### Profile
<img src="https://github.com/user-attachments/assets/c97c6458-231c-4a18-8d91-2b1be3c78f75" width="20%">
<img src="https://github.com/user-attachments/assets/e8db8530-5a1a-4949-b937-a8dce0d43f0c" width="20%">

### Friend requests
<img src="https://github.com/user-attachments/assets/43f13a24-0d52-4c6d-b70b-b41b85fd4013" width="20%">

### Reports and blocked users
<img src="https://github.com/user-attachments/assets/0f9a899e-79ae-48d5-9c62-dcae70a68057" width="20%">
<img src="https://github.com/user-attachments/assets/7b1fd1e7-a48c-4398-af4a-356447777fea" width="20%">
<img src="https://github.com/user-attachments/assets/d53fe4b2-3e12-45c4-9f55-bf3589a7fe00" width="20%">

### Search for friends
<img src="https://github.com/user-attachments/assets/8115f257-3f2a-4c35-8923-3245852414c5" width="20%">


