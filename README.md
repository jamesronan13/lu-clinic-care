# LU Clinic Care: Appointment Booking for Laguna University Clinic

## 📌 Project Overview
LU Clinic Care is a mobile application built with **Android Studio (Java)** that allows students and staff of Laguna University to conveniently book appointments in the **Medical** and **Dental** clinics.  
The app streamlines appointment scheduling, reduces waiting times, and helps clinic staff manage daily patient flow.

---

## 🏫 Project Context
This project was developed during my 3rd year in college as part of the subject **CC 3105 - Applications Development and Emerging Technologies**.  
It fulfills the course requirement and demonstrates practical implementation of mobile app development with Firebase and Android Studio.  
The app is also deployed and available on the **Google Play Store**:  
👉 [LU Clinic Care on Google Play](https://play.google.com/store/apps/details?id=com.clinicapp.clinicapp)

---

## ✨ Features

### 👩‍🎓 Client Side
- Register and log in with email and password (Firebase Authentication).
- View profile and update details (with email verification).
- Book appointments with available time slots.
- Cancel or view appointment history.
- Access information about the clinic and services.

### 👨‍⚕️ Doctor/Scheduler Side
- View all booked appointments in real-time.
- Manage available time slots for Medical and Dental clinics.
- View patient profiles.
- Update appointment status (Confirmed → Completed / Canceled).

---

## 🏥 Clinic Services

### Dental Clinic
- Dental Consultation
- Teeth Cleaning (Prophylaxis)
- Tooth Extraction

### Medical Clinic
- General Consultation
- Health Counseling
- Medical Certificate Issuance

---

## ⏰ Booking Rules
- Booking time slots: **7 AM – 11 AM, 1 PM – 4 PM** (1-hour slots).
- Each service allows **up to 8 persons per clinic per day**.
- One user can only book **one appointment per week**.
- Time slots become unclickable once fully booked.
- Clinic closures:
    - **Dental Clinic:** Closed every Sunday and Thursday.
    - **Medical Clinic:** Closed every Tuesday and Saturday.

---

## 🛠️ Tech Stack
- **Android Studio (Java)**
- **Firebase Authentication** (user accounts)
- **Firebase Realtime Database** (appointments & user roles)

---

## 🔧 Firebase Structure

- **users**
    - userId
        - firstName
        - middleName
        - lastName
        - email
        - role (user/admin)
- **appointments**
    - clinicType (Dental/Medical)
        - appointmentId
            - userId
            - firstName
            - middleName
            - lastName
            - serviceType
            - timeSlot
            - date
            - status
            - timestamp

## 🚀 Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/jamesronan13/lu-clinic-care.git