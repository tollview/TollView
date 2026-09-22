# TollView

By Ryan McKevitt & Eric McKevitt

> **Historical project.** TollView was a working prototype developed collaboratively over a summer. It is no longer actively developed and may no longer function with its external dependencies.

TollView is an Android application built to answer a simple question while driving in Dallas–Fort Worth: **how much did that toll gantry just cost me?**

The app sampled the phone's location while driving, compared it against a dataset of North Texas Tollway Authority gantries, and detected when the user passed through one. Toll crossings and their cash values were recorded to Firebase and could later be viewed and managed through a companion web interface.

The prototype was tested during real drives around DFW and successfully detected and recorded toll crossings.

## How it worked

TollView combined phone location data with known toll-gantry coordinates.

For each location update, the application:

1. Obtained the phone's current location.
2. Compared it against known NTTA toll gantries.
3. Calculated distance using the Haversine formula.
4. Identified a nearby gantry when the user entered its detection range.
5. Recorded the crossing, location, time, and toll value.
6. Stored the event in Firebase for later access.

A simple five-minute cooldown for each gantry prevented repeated GPS samples from recording the same crossing multiple times.

## Stack

- Android
- Kotlin
- Android Location Services
- Firebase Authentication
- Firebase Realtime Database
- Haversine distance calculations
- NTTA toll gantry location and pricing data

## Companion web application

TollView also had a TypeScript web interface for viewing the toll events stored by the Android application.

The web application allowed authenticated users to view their automatically recorded toll history and correct, delete, or manually add records without modifying the underlying gantry dataset.

Its source is maintained separately in the `TollViewWebView` repository.

## Why DFW?

TollView was specifically built around the Dallas–Fort Worth toll system because the project used North Texas Tollway Authority gantry locations and prices rather than attempting to provide a general-purpose toll database.

## Status

TollView is retained as a record of the working prototype and the engineering explored through it. The Android application and its external integrations have not been maintained, so the current repository should not be expected to run without additional work.
