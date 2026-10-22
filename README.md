# Floww

**A swipe-based productivity app for Android, built to make planning, focusing, and reflecting feel effortless.**

Floww reimagines everyday productivity through intuitive swipe interactions, focused work sessions, personal journaling, and a companion that grows alongside you. Instead of overwhelming users with complex dashboards, Floww brings daily tasks and personal progress into a simple, interactive experience.

## Preview

<p align="center">
  <img src="https://github.com/user-attachments/assets/81e8c2ac-0d91-41df-89d2-5979ef0855a8" width="24%" alt="floww home screen" />
  <img src="https://github.com/user-attachments/assets/8ebc950d-5631-49ae-bd04-484482348d0a" width="24%" alt="floww task management" />
  <img src="https://github.com/user-attachments/assets/464f3bb7-88fe-40d3-bdf0-e2ad01c8a8ab" width="24%" alt="floww swipe cards" />
  <img src="https://github.com/user-attachments/assets/c4d359f0-37b6-4475-9d27-8614203fc169" width="24%" alt="floww focus timer" />

</p>


> **Note:** The previews may not show the app in its entirety. Please visit the download link to explore the full app in a live preview.

## Features

- **Swipe-Based Task Management** — Interact with daily tasks using simple gestures. Swipe to complete tasks or skip them.
- **Focus Timer** — Run customizable focus sessions with timer settings, custom backgrounds, and ringtone support.
- **Daily Planning** — Organize tasks with customizable colors and emoji identifiers.
- **Personal Journal** — Write daily reflections and revisit previous entries in an archive-style interface.
- **Mascot Companion** — Choose a companion, track its hunger and happiness, and receive motivational messages.
- **Progress Tracking** — Visualize goal completion, progress multipliers, and weekly insights.
- **Smart Notifications** — Configure reminders, completion alerts, and companion notifications.
- **Personalized Onboarding** — Set up your profile, personal goal, and preferences.
- **Customizable Experience** — Manage your profile, mascot, notification preferences, and focus settings.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| UI | XML Layouts |
| IDE | Android Studio |
| UI Components | Material Components |
| App Architecture | Activities and Fragments |
| Local Storage | SharedPreferences |
| Notifications | Android Notification Channels |
| Minimum Android SDK | API 24 |
| Target Android SDK | API 34 |

## Project Structure

```text
Floww/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── .../
│           │       ├── MainActivity.java
│           │       ├── fragments/
│           │       ├── adapters/
│           │       └── utils/
│           ├── res/
│           │   ├── layout/
│           │   ├── drawable/
│           │   ├── mipmap/
│           │   └── values/
│           └── AndroidManifest.xml
├── docs/
│   └── screenshots/
└── README.md
```

*The structure above is illustrative; adjust it to match the actual repository.*

## Getting Started

### Prerequisites

- Android Studio
- JDK version compatible with the project's Gradle configuration
- Android SDK with the required build tools
- An Android emulator or physical device running Android 7.0 (API 24) or later

### Installation

1. Clone the repository:

   ```bash
   git clone git@github.com:naayann/floww.git
   ```

2. Open the cloned project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Select an emulator or connect an Android device.
5. Build and run the `app` configuration.

## Design Philosophy

Floww is built around a simple idea: productivity tools should help users take action, not create more friction.

- **Interaction over complexity:** Use gestures to make routine task management faster.
- **Focus over clutter:** Keep daily priorities and focus sessions accessible.
- **Reflection as a habit:** Make journaling and reviewing past entries part of the workflow.
- **Progress with personality:** Use a companion and visual feedback to make consistency more engaging.

## Roadmap

Potential areas for future development:

- Cloud synchronization and backup
- Dark mode
- Habit streaks and calendar views
- Home-screen widgets
- Expanded mascot animations and interactions
- Journal export and data portability

## Contributing

Contributions, bug reports, and feature suggestions are welcome. Open an issue to discuss a proposed change before submitting a pull request.


---

<p align="center">
  <strong>Floww</strong><br/>
  Focus on what matters. One swipe at a time.
</p>
