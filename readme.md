# 🎨 Color Assist App

An Android application designed to **assist color-blind users** by making color-dependent information easier to see, understand, and interpret.

Instead of real-time camera detection, this app focuses on **uploaded images, charts, documents, and test strips**, adapting results based on different types of color blindness.

## 🧠 What Problem Does This Solve?

Many everyday things rely heavily on color:
- charts and graphs
- test strips (pH, nitrate, ammonia, etc.)
- emails or documents with confusing color highlights

For users with color vision deficiencies, this information can become unclear or misleading.  
**Color Assist App bridges that gap.**

## ✨ Key Features

### 🖼️ Image-Based Color Detection
- Upload an image from the gallery
- Tap/select a **specific region**
- Get clear color identification in readable terms

### 📊 Color-Coded Chart Conversion
- Converts color-based charts and graphs
- Adapts output based on the **user’s type of color blindness**
- Presents data in text or alternative visual formats

### 📧 OCR for Emails & Documents
- Extracts text from images or screenshots
- Reduces confusion caused by color-highlighted text
- Improves readability for color-blind users

### 🧪 Test Strip Level Identification
Supports interpretation of color-based tests such as:
- pH scale
- Nitrate levels
- Ammonia levels
- Similar chemical or diagnostic strips

Results are converted into **clear numeric or labeled values** instead of raw colors.

## 👁️ Supported Color Vision Types

- Protanopia
- Deuteranopia
- Tritanopia
- General color vision deficiency modes

*(Can be expanded as the project evolves)*

## 🛠️ Built With

- ☕ **Java**
- 📱 **Android Studio**
- 🧰 **Gradle**
- 🖼️ Image processing utilities
- 🔤 OCR (planned / implemented as applicable)

## 📂 Project Structure



Color-Assist-App
├── app/                      # Main Android app source
├── gradle/                   # Gradle wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew / gradlew.bat
└── README.md

````

Source code:
- Java files → `app/src/main/java`
- Layouts & resources → `app/src/main/res`

## ▶️ Getting Started

### Prerequisites
- Android Studio
- Android SDK
- Emulator or physical Android device

### Run Locally

```bash
git clone https://github.com/aastha-sriv18/Color-Assist-App.git
````

1. Open the project in Android Studio
2. Let Gradle sync
3. Build and run on your device or emulator

## 🚧 Future Improvements

* Better region selection for image analysis
* Expanded OCR language support
* More test strip calibration profiles
* Accessibility-first UI refinements

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Open a Pull Request

## 📫 Author

Developed by **Aastha Srivastava** and **Shubham Sengar**
Built with accessibility and inclusivity in mind 💜
