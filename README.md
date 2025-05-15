# Newsly

Newsly is a Kotlin-based Android application that lets you search, browse and manage news articles from NewsAPI.org. It features:

- **All Articles**: search by query, date range, sort order and language  
- **Favorites**: mark/unmark articles as favorites and persist them locally  
- **History**: view and re-run past search queries with a collapsible, date-grouped list  
- **Recommendations**: AI-powered personalized recommendations based on your search history  
- **Detailed Article View**: built-in WebView with ad-blocking, text selection and rich bookmarking/highlighting  

The app follows MVVM architecture with Jetpack components, Room for local persistence, Retrofit for networking, Kotlin Coroutines for asynchronous work, and Material3 for UI.

---

## Features

- **Responsive Tabbed Interface**  
  – ViewPager2 + TabLayout with three tabs: Recommendations, All Articles, Favorites  
  – Gradient indicator for Recommendations tab  

- **Powerful Search**  
  – Full-text search across multiple fields  
  – Date-range picker (MaterialDatePicker)  
  – Sort by publication date, relevancy or popularity  
  – Language filter (all / English / Russian)  

- **History Management**  
  – Your last searches saved in Room database  
  – Collapsible headers by day, tap to expand/collapse entries  
  – Tap a history item to re-run search  

- **Favorites**  
  – Mark/unmark articles as favorite from any list or detail view  
  – Favorites persisted in Room and synced across tabs  

- **In-App Browser & Bookmarks**  
  – WebView with ad-blocking (AdBlockingWebViewClient)  
  – Select text to create “bookmarks” with DOM-aware validation (no ads/links)  
  – Injected Mark.js highlights your bookmarks inside the article  
  – Smooth scroll & animation to last/highlighted selection  

- **Recommendations**  
  – Algorithm analyzes top 3 most frequent search queries from your history  
  – Fetches recent popular articles for those queries  
  – Displays up to 20 unique recommended articles  
  – Pull-to-refresh to update recommendations on demand  

- **Architecture & Quality**  
  – MVVM with ViewModel & LiveData  
  – Kotlin Coroutines + lifecycleScope  
  – Room DAO + Repository pattern  
  – Retrofit + Gson converter + NewsAPI integration  
  – Picasso for image loading  
  – Material3 theming & responsive layouts  

---

## Technologies Used

- **Language & Platform**: Kotlin • Android 13+ (minSdk 31)  
- **Architecture**: MVVM • Jetpack ViewModel • LiveData  
- **Networking**: Retrofit 2 • Kotlin Coroutines  
- **Local Storage**: Room • DAO + Entities  
- **Image Loading**: Picasso  
- **UI**: Material3 • ViewBinding • SwipeRefreshLayout • BottomSheetDialog  
- **WebView**: AdBlockingWebViewClient • Mark.js injection  
- **Date Picker**: MaterialDatePicker  
- **Build**: Gradle (KSP for Room)  
- **Version Control**: Git • GitHub integration  

---

## System Requirements

- Android 13.0 (API 31) or later  
- Android Studio Giraffe or newer  
- Internet connection for fetching news  

---

## Setup Instructions

1. **Clone the repository**  
   ```bash
   git clone git@github.com:<your-username>/Newsly.git
   cd Newsly

2. **Obtain a NewsAPI API key
	•	Sign up at https://newsapi.org/
	•	Copy your API key
3. **Configure the API key
	•	In the project root, open (or create) local.properties and add:

NEWS_API_KEY="your_actual_api_key_here"


	•	In app/build.gradle, ensure the key is passed into your code via BuildConfig:

defaultConfig {
    // …
    buildConfigField "String", "NEWS_API_KEY", NEWS_API_KEY
}


	•	At runtime, Retrofit’s ApiService will automatically pick up BuildConfig.NEWS_API_KEY.

4. **Import into Android Studio
	•	File → Open… → select the project folder
	•	Let Gradle sync
5. **Run on Device or Emulator
	•	Choose your target SDK (min API 31+)
	•	Run

![Screenshot_2025-05-15-12-40-41-124_by bsu newsly](https://github.com/user-attachments/assets/5906dbc0-2ab1-4d56-9b95-537a1bf4a625)
![Screenshot_2025-05-15-12-40-58-699_by bsu newsly](https://github.com/user-attachments/assets/0b86b01f-9bc9-405c-968c-1c3dfa7d888d)
![Screenshot_2025-05-15-12-41-12-001_by bsu newsly](https://github.com/user-attachments/assets/aeeac444-1bbf-472b-9196-6daf01f1e7ee)
![Screenshot_2025-05-15-12-41-21-125_by bsu newsly](https://github.com/user-attachments/assets/e67b7120-e02d-4801-a2e6-d281ee780b40)
![Screenshot_2025-05-15-12-41-29-602_by bsu newsly](https://github.com/user-attachments/assets/04cc3b84-bc0c-4bfc-b4aa-9af84698c697)
![Screenshot_2025-05-15-12-41-36-474_by bsu newsly](https://github.com/user-attachments/assets/60553488-9215-461a-bd0f-67458a66fdda)
![Screenshot_2025-05-15-12-41-45-803_by bsu newsly](https://github.com/user-attachments/assets/a6e0ec9d-56c2-4c31-9eb1-bc55132dde54)
![Screenshot_2025-05-15-12-42-43-814_by bsu newsly](https://github.com/user-attachments/assets/c64aa948-189d-46dd-9dbb-19d65f9952f3)
![Screenshot_2025-05-15-12-42-58-055_by bsu newsly](https://github.com/user-attachments/assets/fb5e3eea-9abb-4f20-8b2b-d8502d382ae0)
![Screenshot_2025-05-15-12-43-03-861_by bsu newsly](https://github.com/user-attachments/assets/ae6861c0-44ee-4496-952f-b13db5bb6de2)
![Screenshot_2025-05-15-12-43-10-278_by bsu newsly](https://github.com/user-attachments/assets/7d33cbee-ffa0-4fa6-ba68-f6ed3a7d6651)
