# Farmers Collective

## Title
**AI-based Market Intelligence Systems for Farmer Collectives: A Case Study from India** [Funding](https://drive.google.com/file/d/1xw9WReTfl2XNVFip4J1P_PGC33dBlzol/view?usp=sharing)

**Publication:** [ACM Digital Library](https://dl.acm.org/doi/10.1145/3609262)

## About
The project is a sub-part of a larger initiative under the Google AI for Social Good program and is in collaboration with ACT4D - IIT Delhi, Gram Vaani, and CCD.

- **Digitalization Tool:** Developed a tool using the ODK Collect Framework to introduce digitalization of data records, eliminating redundant data collection practices.
- **Market Development:** Currently exploring options to develop a digital market for crop price surveillance, considering the CoRE Stack: Commoning for Resilience and Equality.
- **Recognition:** Received an honorable mention for "Best Presentation" at the mid-program AI4SG workshop hosted by Google, commended for progress in field deployments and test pilots. [Workshop Projects](https://sites.google.com/view/aiforsocialgoodworkshop/2021-projects/ai4sg-workshop-7-10-feb-22)

## Resources

- **System Architecture:** [Figma](https://www.figma.com/file/FxrJba3VazzLoQwDRo8qkZ/CCD-App)
- **Application FlowChart:** [Figma](https://www.figma.com/file/XqE6NrQ4jf9Hglowd6YjBA/FlowChart)
- **Mid Program AI4SG Workshop:** [YouTube](https://www.youtube.com/watch?v=CBejp1uK55c&t=1835s)
- **Current Status of the Application (Visuals):** [Google Drive](https://drive.google.com/file/d/1Lhp8tbWW9vtFGcLZf8ANXB4qR5ED-4Kz/view?usp=sharing)
- **Application Description (SlideShow):** [Google Slides](https://docs.google.com/presentation/d/1aJQr4w4535DM4W9SDmgYJvEZIvhNGMgpx35XLd3NYpc/edit?usp=sharing)
- **APK Download:** [Google Drive](https://drive.google.com/file/d/1r8o56TbB2xRj05T3IODPna4LZyfbWrDG/view?usp=sharing)

## Installation

1. Download and install [the latest version of Android Studio](https://developer.android.com/studio).
2. Clone this repository. The app files reside in the `android app/` directory.

**Firebase Project:** [Firebase Console](https://console.firebase.google.com/u/1/project/appccd-6ee6a/overview)

### Notes

- Previously, we released a debug version of the application on Google Drive, which took up significantly more storage space. It is advisable to build a release version and sign it with a private keystore, [as shown here](https://developer.android.com/studio/publish/app-signing).
- Recommendation data was stored in CSV files on the remote server, pushing to Firebase. This approach used CSV files as a database within the app. We are now in the process of migrating to Room database.
