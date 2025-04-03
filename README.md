# 🌾 Farmers Collective

## 📘 Title  
**AI-Based Market Intelligence Systems for Farmer Collectives: A Case Study from India**  
📚 [Published in ACM Digital Library](https://dl.acm.org/doi/10.1145/3609262)  
💰 [Google AI for Social Good – Funding](https://drive.google.com/file/d/1pkqTaaMOIHWkB5ayBQtXoK61rBnOsAx0/view)

---

## 🧑‍🔬 About

This project is a sub-part of a larger initiative under the **Google AI for Social Good** program in collaboration with:

- **ACT4D – IIT Delhi**
- **Gram Vaani**
- **Centre for Collective Development (CCD)**

It focuses on improving market outcomes for smallholder farmers by building AI-assisted forecasting and recommendation tools for crop price decision-making, specifically targeting **non-perishable crops** like **soybean**.

### 🔨 Key Work Areas

- **Digitalization Tool:**  
  Developed Android-based ODK forms for CCD field officers to collect mandi price data, eliminating manual recordkeeping.

- **Market Intelligence Development:**  
  Designed the foundation for a digital market surveillance system using the **CoRE Stack (Commoning for Resilience and Equality)**.

- **Recognition:**  
  Awarded **Honorable Mention for Best Presentation** at the Google AI4SG Mid-Program Workshop for impactful field deployment and iterative product design.  
  🎓 [Workshop Projects](https://sites.google.com/view/aiforsocialgoodworkshop/2021-projects/ai4sg-workshop-7-10-feb-22)

---

## 📱 Android Application – *Rythu Vaani*

The core frontend system was developed as a mobile app called **Rythu Vaani**, built for deployment across farmer cooperatives supported by CCD.

### 🔧 Features & Role

- **Historical Pricing Trends:**  
  Allows comparison of Adilabad mandi prices with surrogate mandis to support more informed sale decisions.

- **Forecast & Recommendations:**  
  Presents top 3 recommended sale dates based on AI-generated price forecasts.

- **Review Past Advice vs Reality:**  
  Lets field officers and cooperatives verify the accuracy of past recommendations against actual market outcomes.

- **Lightweight Design:**  
  Works offline and syncs with Firebase when connectivity is available. Designed for low-resource rural environments.

- **Field Tested:**  
  Actively deployed across 16 cooperatives. Iterated based on weekly calls with CCD staff to improve usability.

---

## 🧠 AI Models – Forecasting & Decision Support

### Models Used

- **LSTM (Long Short-Term Memory):**  
  Recurrent model used to learn long-range trends in mandi prices.

- **Temporal Convolutional Network (TCN):**  
  Used for short-term multivariate time-series forecasting. Performed better than LSTM in both accuracy and training stability.

  Inputs:
  - Local + surrogate mandi prices
  - Arrival volumes
  - Seasonality encoded as cyclic features (day-of-year)

### Recommendation System

- Built a 5-model **TCN ensemble**
- Incorporated **Prospect Theory** to rank sale dates, simulating risk aversion under price uncertainty
- Metrics used for evaluation:
  - **Probability of Accurate Prediction (PAP)**
  - **Net Gain (NG)**
  - **Oracle Gain** (upper bound)

These models power the recommendations shown in the Android app.

---

## 🔗 Resources

| Resource                            | Link |
|-------------------------------------|------|
| 🏗️ System Architecture (Figma)          | [View](https://www.figma.com/file/FxrJba3VazzLoQwDRo8qkZ/CCD-App) |
| 🔁 Application FlowChart (Figma)       | [View](https://www.figma.com/file/XqE6NrQ4jf9Hglowd6YjBA/FlowChart) |
| 🎥 Google AI4SG Workshop Presentation | [YouTube](https://www.youtube.com/watch?v=CBejp1uK55c&t=1835s) |
| 🖼️ App UI Screenshots                 | [Google Drive](https://drive.google.com/file/d/1Lhp8tbWW9vtFGcLZf8ANXB4qR5ED-4Kz/view?usp=sharing) |
| 📽️ Project Slideshow                  | [Slides](https://docs.google.com/presentation/d/1aJQr4w4535DM4W9SDmgYJvEZIvhNGMgpx35XLd3NYpc/edit?usp=sharing) |
| 📱 APK Download                      | [Download APK](https://drive.google.com/file/d/1r8o56TbB2xRj05T3IODPna4LZyfbWrDG/view?usp=sharing) |
| 🔥 Firebase Console                  | [Firebase](https://console.firebase.google.com/u/1/project/appccd-6ee6a/overview) |

---

## ⚙️ Installation

1. Install [Android Studio](https://developer.android.com/studio)
2. Clone this repository
3. Open the project inside the `android app/` directory
4. Sync Gradle, connect your emulator/device, and run the app

---

## 🧾 Notes

- Earlier debug versions of the APK were large. For deployment, it's advised to create a signed release APK with a private keystore.  
  🔐 [App Signing Guide](https://developer.android.com/studio/publish/app-signing)

- The original version used **CSV files** as lightweight structured data pushed to Firebase. We are currently migrating to a more stable **Room database**.

---

> Built for real-world deployment with smallholder farmers, this app brings AI-assisted decision-making directly into the hands of rural cooperatives.
