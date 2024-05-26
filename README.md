# farmers_collective
Publication : https://dl.acm.org/doi/10.1145/3609262

The project is a sub-part of a larger project under the Google AI for Social Good programme and is in
collaboration with ACT4D - IIT Delhi, Gram Vaani and CCD. 
</br>
- Developed a tool using the ODK Collect Framework to introduce digitalization of data records which helped in elimnating existing redundant data collection practices. <br>
- Currently exploring options to develop a digital market for the price surveillance of the crops keeping in mind the CoRE Stack : Commoning for Resilience and Equality.
- Recieved an honorable mention for the "Best Presentation" at the mid program AI4SG workshop hosted by Google. Commended based on the progress done so far in field deployments and test pilots. : https://sites.google.com/view/aiforsocialgoodworkshop/2021-projects/ai4sg-workshop-7-10-feb-22
- System Architecture : https://www.figma.com/file/FxrJba3VazzLoQwDRo8qkZ/CCD-App
- Application FlowChart : https://www.figma.com/file/XqE6NrQ4jf9Hglowd6YjBA/FlowChart
- Mid Program AI4SG Workshop : https://www.youtube.com/watch?v=CBejp1uK55c&t=1835s
- Current Status of the Application (Visuals) :  https://drive.google.com/file/d/1Lhp8tbWW9vtFGcLZf8ANXB4qR5ED-4Kz/view?usp=sharing
- Application Description (SlideShow) : https://docs.google.com/presentation/d/1aJQr4w4535DM4W9SDmgYJvEZIvhNGMgpx35XLd3NYpc/edit?usp=sharing
- APK Download : https://drive.google.com/file/d/1r8o56TbB2xRj05T3IODPna4LZyfbWrDG/view?usp=sharing

# Installation

Download and install [the latest version of Android Studio](https://developer.android.com/studio), and clone this repository. The app files reside in "android app"/ <br> <br>
Firebase project for the app resides at: https://console.firebase.google.com/u/1/project/appccd-6ee6a/overview

So far, we used to release a debug version of the application on Google Drive, which takes up significantly more storage space. It is advisable to build a release version and sign it with a private keystore, [as shown here](https://developer.android.com/studio/publish/app-signing).

Due to recommendation data being stored in csv files on the remote server pushing to Firebase, we had decided to use csv files as a database within the app, a very odd decision looking back. We are now in the process of migrating to Room database.
