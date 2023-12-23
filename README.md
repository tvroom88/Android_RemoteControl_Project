# Android Remote Control Open Source

![Language](https://img.shields.io/badge/language-Kotlin-orange.svg) 
![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=102)

This project is Android Remote control service by using webrtc and accessibility service.

First, I would like to express my gratitude to 'CodeWithKael' for granting permission to use the WebRTC code. 

The link below connects to his WebRTC lesson and github repository. 

If you interested webrtc, then you can learn diverse webrtc skills from his youtube channel.

Reference Resources : (WebRTC) [Link](https://www.youtube.com/watch?v=_-GPHNSug2Q&list=PLFelST8t9nqgKO5XUNHS01jmD-fMf6EPX&index=1)

## Skills

1. WebSocket : To enroll user data, and to exchange session descriptions between caller and callee
2. Webrtc : (1) Screen Sharing (2) Caller send x, y coordinate to Callee through DataChannel 
3. Accessibiltiy Service : Remote Control (Caller's phone)


## Getting Started

1. Open Node.js Server folder (ScreenShareSignalServer-main) through Visual Studio Code ![image](https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/1820a1cb-648e-4e8a-afaa-79668daed454)

     You can DownLoad this [Link}(https://code.visualstudio.com/)

2. Setting Server and Start Server : Type the following command in the terminal.
   1. npm init
   2. npm install websocket
   3. npm install nodemon --include=dev
   4. npm start
  
    If you meet result like below screen shot, then npm server is opened succssfully.

   <img width="331" alt="스크린샷 2023-12-23 오후 12 00 07" src="https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/8c9a9166-405b-4db4-b8c7-fa0e06b2637d">
   </br>
  
3. Open Android Application (Android_RemoteControl_Service) with Android Studio or Intellij
4. Follow like below video with two emulator or phone.



https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/54a0e271-eb0e-4aca-9900-fc7052cbbdd4 

https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/d493cb27-e079-408d-9eef-20caec61c813




## FlowChart

#### 1) Webrtc screen Sharing FlowChart

<img width="865" alt="스크린샷 2023-12-23 오후 2 30 01" src="https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/b4ee2abe-48d2-45cf-9069-6bd9d0f4b7e1">


#### 2) Enable Accessibility Service FlowChart

<img width="858" alt="스크린샷 2023-12-23 오후 2 29 55" src="https://github.com/tvroom88/Android_RemoteControl_Project/assets/4710854/d1deb9ac-d66f-4e40-9764-5e091b017064">


