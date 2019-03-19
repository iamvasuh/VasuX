# VasuX WarmUp Project Report

This is a WarmUp Project from NDN Mentor: Ju Pan


AIM: Image Synchronization over UDP


USE CASE: User A has two images on her Android phone. User B wants to fetch those images from User A’s device to her own Android phone. User B click the “fetch request” button on her phone, User A click click “agree” button to start synchronizing images. As a result, User B gets those images on her phone screen.


IMPLEMENTATION: The "VasuX" app is an app for transfer or sharing files from one device to another device. This app enables point to point file transfer/sharing between devices
 using WIFI-Hotspot.The VasuX library supports the VasuX demo for the app to function and share files. The library consistes of 4 main componenents namely-
 
1)"Hotspot Controller" , which uses a process of examining or modifying the run time behavior of a class at run time known as JAVA Reflection as there are no such APIs available for turning on/off the Hotpost in Android. This HC creates an open Wifi hotspot configuration with an SSID which can taken by receivers to recognize VasuX senders.

2)"VasuXServer" , which is a small HTTP server extended from NanoHttpd(an open-source, small-footprint web server that is suitable for embedding in applications, written in the Java programming language) and serves the sender data to receivers using IP address as hostname and works on port assigned by user or system by default.

3)"VasuXService" , which controls the lifecycle of the VasuXServer and also manages notification with a stop action.

4)"SENDER & RECEIVER" , where the SENDER displays port,connected clients info along with file transfer status for each connected client and the RECEIVER provides UX/UI to list the files for download from Android Download Manager.

HOW IT WORKS: There are two modes in the app known as "SENDER MODE" & "RECEIVER MODE":
 
1)SENDER MODE: Intents Extras like "ShareService.EXTRA_FILE_PATHS"(which holds location references to files on device) are passed in order to start "VasuXActivity" which invokes "VasuXService" which in turn starts an instance of "VasuXServer" with device IP as address and port if not specified assigned by system. On successful server setup, Hotspot Controller creates an OPEN WIfi Configuration with an SSID using combination of Android ID, Port number and Sender Name("Vasu", name in this case).The "VasuXActivity" then scans for clients/receivers using HC methods and adds them to the list.

2)RECEIVER MODE: This mode doesn't uses any Intent Extras and is easy to initiate.The "ReceiverActivity" scans for senders automatically and gets files info. from "VasuXServer" after having a succesful connection. Then the "ReceiverActivity" places the "FilesListingFragment" and Sender files are listed and download request to Android Download Manager is enqueued.

RESULT: The 2 images in device A is transferred to device B using the VasuX app.


VIDEO LINK: https://www.youtube.com/watch?v=gMobRBCaHlk
