SocialConnect
SocialConnect is a modern, feature-rich Android social media application built with a bold Red, Yellow, and Black theme. It allows users to connect, share content, and interact with a community in a streamlined, high-performance environment.

🚀 Tech Stack
•	Language: Java
•	UI Framework: Material Components (Material 3)
•	Backend: Firebase (Authentication, Firestore, Storage, Cloud Messaging)
•	Image Handling: Cloudinary (hosting) & Glide (loading/caching)
•	UI Effects: Facebook Shimmer (loading states)
•	Architecture: Fragment-based navigation with a single MainActivity

✨ Key Features

🔐 User Authentication
•	Sign Up: Create a new account using email and password.
•	Login: Secure access to your profile.
•	Forgot Password: Integrated Firebase reset-email logic to recover account access.

📱 Social Feed (Home)
•	Dynamic Feed: View posts from users in a scrollable RecyclerView.
•	Visual Content: Support for high-quality images and multi-line text.
•	Interactive UI:
  o	Likes: Real-time like functionality.
  o	Comments: Dedicated Bottom Sheet for viewing and adding comments without leaving the feed.
•	Pull-to-Refresh: Swipe down to get the latest posts.
•	Loading States: Shimmer effects for a premium loading experience.

✍️ Content Creation
•	Create Post: Dedicated screen to share thoughts and photos.
•	Image Uploads: Seamless image selection and hosting integration.

👤 Profile Management
•	Personal Profile: View and edit your full name and bio.
•	Profile Photo: Upload and change your avatar.
•	Public Profiles: View other users' profiles to see their information.

⚙️ Settings & Privacy
•	Account Management: Easy access to Log Out.
•	Security: Ability to permanently delete your account.

🔔 Notifications
•	Push Notifications: Stay updated with community activity via Firebase Cloud Messaging.

🎨 Design & Theme
The app features a custom "Midnight Flare" theme designed for striking contrast and modern aesthetics:
•	Background: Pure Black (#000000) for high contrast and battery efficiency on OLED screens.
•	Primary Elements: Vibrant Red (#D32F2F) for headers and primary actions.
•	Accents: Golden Yellow (#FBC02D) for icons, buttons, and interaction highlights.
•	Modern UI: Rounded corners (16dp), Material Design 3 components, and intuitive navigation.

🛠️ Project Structure
•	HomeFragment: Manages the main social feed and post interaction logic.
•	CreatePostActivity: Handles the creation and uploading of new content.
•	CommentBottomSheet: An elegant overlay for managing post discussions.
•	ProfileFragment: The user's personal hub for identity management.
•	PostAdapter & CommentAdapter: High-performance RecyclerView adapters using DiffUtil for smooth updates.

🏗️ Getting Started
Follow these instructions to get a local copy of the project up and running.
1. Clone the repository:
Bash
git clone https://github.com/AhmadWasay/SocialConnect.git
2. Firebase Setup:
•	Create a new project in the Firebase Console.
•	Register your Android app and download the google-services.json file.
•	Place the google-services.json file into the app/ directory of this project.
•	Enable Email/Password Authentication, Firestore Database, and Firebase Storage in the console.
3. Cloudinary Setup:
•	Create a free account on Cloudinary.
•	Configure your Cloudinary credentials within the project code to successfully handle image uploads.
4. Build and Run:
•	Open the project in Android Studio.
•	Sync the Gradle files.
•	Run the application on an Android Emulator or a physical device.

