# Offline Turkish (TDK) Dictionary / Çevrimdışı Türkçe (TDK) Sözlük

A minimalist, offline Android dictionary application for Turkish, based on the official Turkish Language Association (TDK) data. This app aims to provide a faster, simpler, and more useful alternative to the official application, with offline capabilities and seamless system integration.

<img src="https://github.com/user-attachments/assets/4cf66f97-869f-42aa-8559-67dc7a2cdbaf" alt="Offline Turkish Dictionary Screenshot" width="300"/>

## Features

*   **Offline Access:** Search for word definitions without an internet connection. The app uses a local SQLite database (`tdk.db`).
*   **Smart Search:** Features autocomplete suggestions powered by a Trie data structure for fast lookup.
*   **Audio Pronunciation:** Listen to the correct pronunciation of words (fetched from TDK servers, requires internet).
*   **System Integration:** "Process Text" support allows you to select a word in any other app (like a web browser or PDF reader) and search for it directly in the dictionary via the context menu.
*   **Random Word:** Discover new words with the "Random Word" feature.
*   **Turkish Character Support:** Easy insertion of Turkish characters (ç, ğ, ı, ö, ş, ü) directly from the interface.
*   **Dark Mode:** Supports system-wide dark mode (via Material Design).

## Technologies Used

*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Asynchronous Processing:** RxJava 3, RxAndroid, Kotlin Coroutines
*   **Database:** SQLite (with `SQLiteAssetHelper`)
*   **Network:** Volley (for fetching audio)
*   **UI:** ViewBinding, Material Design

## Installation and Build

To build and run this project locally, you will need [Android Studio](https://developer.android.com/studio).

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
    ```
2.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select "Open" and navigate to the cloned directory.
3.  **Sync Gradle:**
    *   Wait for Android Studio to sync the project with Gradle files.
4.  **Run the App:**
    *   Connect an Android device or start an emulator.
    *   Click the "Run" button (green play icon) or press `Shift + F10`.

## Contributing

Contributions are welcome! If you have suggestions for improvements or bug fixes, please feel free to:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes (`git commit -m 'Add some amazing feature'`).
4.  Push to the branch (`git push origin feature/amazing-feature`).
5.  Open a Pull Request.

## License

This project is open source.
