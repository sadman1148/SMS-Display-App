# SMS-Display-App

<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/ee652f13-d652-4e73-b7cc-247e83e64658" width="250" alt="App Icon">
<a href="https://www.flaticon.com/free-icons/sms" title="sms icons">Icon created by Eucalyp</a>

## Overview
The **SMS Display App** is a simple text viewer app like your built it SMS app, but with fewer functionalities.

### Key Features
- Single activity achitecture
- Uses 3 permissions: **READ_SMS, RECEIVE_SMS, READ_CONTACTS**
- Requests permission in specific order, **READ_CONTACTS > READ_SMS > RECEIVE_SMS**
- Shows saved contact names if **READ_CONTACTS** is granted
- Shows incoming texts as Toast if **RECEIVE_SMS** is granted
- Provides manual sms list reload with [swipe-to-refresh](https://developer.android.com/develop/ui/views/touch-and-input/swipe/add-swipe-interface) if **RECEIVE_SMS** is revoked
- Updates UI accordingly to granted permissions
- Phone number country code handling only for Bangladesh, however with [Libphonenumber](https://mvnrepository.com/artifact/com.googlecode.libphonenumber/libphonenumber/8.7.0) library, all countries can be handled
- Shows detailed message when an item from the message list is tapped

## Screenshots
<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/759942b4-4cc0-47c4-a22f-3aa38f629252" width="200" alt="SMS List">
<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/45e097ca-44ee-4e4a-a515-4fe4a056c646" width="200" alt="Detailed SMS">
<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/3b6cac00-9539-46d1-ba3b-7fe364b17607" width="200" alt="Toast">
<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/e2bc200c-8c71-4bf6-bd69-a89b4dc186e9" width="200" alt="Toast with READ_CONTACTS">
<img src="https://github.com/sadman1148/SMS-Display-App/assets/71433330/5bc68981-2c02-43cf-b806-15dfa0d8897b" width="200" alt="Detailed SMS with READ_CONTACTS and Light Theme">

## Getting Started
These instructions will help you set up and run the project on your local machine for development and testing purposes.

### Prerequisites
- [Android Studio](https://developer.android.com/studio)
- Required [libraries](https://developer.android.com/studio/install#64bit-libs) for 64-bit Linux machines
- Android SDK

### Installation
1. Clone the repository by pasting the following command into your terminal:
```bash
   git clone git@github.com:sadman1148/SMS-Display-App.git
```
2. Open the project in Android Studio and let it sync.
3. Connect a physical device or build a virtual device with the AVD manager.
4. Run the app.

### Usage
1. Grant permissions.
2. Browse the SMS list.
3. Tap on any message to see details about that SMS.
4. Feel free to intentionally revoke permissions and observe how the app handles it.


## Contribution
I welcome contributions from the community. To contribute to this project, follow these steps:

1. Fork this repository.
2. Create a new branch for your feature or bug fix: git checkout -b feature/your-feature-name
3. Make your changes and commit them: git commit -m 'Added a new feature'
4. Push to your branch: git push origin feature/your-feature-name
5. Create a pull request on this repository.


## Contact
Feel free to contact me on my [LinkedIn](https://www.linkedin.com/in/sadman-alam-impulse/) profile.
