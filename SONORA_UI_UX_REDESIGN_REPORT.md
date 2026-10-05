# Sonora — UI/UX Redesign Report

## 1. Project Identity

**Project Name:** Sonora  
**Repository:** `Imanshu999/sonora`  
**Platform:** Native Android  
**Current documented version:** 2.0

Sonora is a native Android music player focused on discovery, playback, playlists, offline listening, and a modern listening experience.

## 2. Redesign Goal

The redesign focuses on making Sonora feel more polished, cohesive, premium, and production-ready while preserving the existing music functionality.

The visual layer should be improved without replacing the underlying playback, provider, library, or data architecture.

## 3. Core UI Direction

- Modern dark-first music interface.
- Clean hierarchy with strong typography.
- Consistent spacing, corner radii, icons, and component sizing.
- Material 3 foundations with a more distinctive Sonora visual identity.
- Smooth transitions and subtle motion rather than excessive animation.
- Clear focus on album artwork, track information, and playback controls.
- Responsive layouts for different Android screen sizes.

## 4. Player & Mini-Player

The compact player should remain immediately accessible while browsing the app.

Required controls:

- Previous track.
- 10-second rewind.
- Play / pause.
- 10-second forward.
- Next track.
- Playback progress.
- Track title and artist.
- Artwork.
- Queue access.

The full Now Playing screen should provide a stronger visual hierarchy around artwork, metadata, progress, and primary playback actions.

## 5. Navigation & Floating Surfaces

Use layered surfaces carefully for:

- Mini-player.
- Queue sheets.
- Search filters.
- Settings panels.
- Context menus.
- Playback-related dialogs.

Floating surfaces should feel integrated with the main application rather than looking like unrelated popups.

## 6. Search & Discovery

Search should remain fast and easy to scan.

The redesigned experience should preserve:

- Multi-source search.
- Source filters.
- Artist, album, and track discovery.
- Recent searches.
- Genre discovery.
- Regional and language discovery.
- Phonk and other genre-focused discovery.

Horizontal source filters must remain usable on narrow screens and must not clip or expand beyond the available layout.

## 7. Library Experience

The Library should clearly separate personal content such as:

- Liked songs.
- Playlists.
- Listening history.
- Downloads.
- Offline tracks.

Empty states should be intentional and helpful instead of appearing unfinished.

## 8. Theme System

Sonora should retain its theme flexibility:

- Light / White.
- Dark.
- AMOLED black.
- System theme.

All components must maintain readable contrast and consistent icon/text treatment across themes.

## 9. Branding & Visual Identity

The Sonora name should be treated as the primary product identity throughout the UI.

Branding should remain consistent across:

- App icon.
- Splash screen.
- App header.
- Player.
- Settings.
- Empty states.
- Documentation screenshots.
- Store-facing assets.

Avoid unnecessary visual clutter or generic template styling.

## 10. Functional Preservation

The redesign must not break existing core functionality, including:

- Media3 / ExoPlayer playback.
- MediaSession background playback.
- Queue management.
- Shuffle and repeat.
- Crossfade.
- Sleep timer.
- Search providers.
- Playlists.
- Favorites.
- History.
- Downloads / offline library.
- Lyrics.
- Smart Mix.
- Audio and theme settings.
- Bluetooth-related functionality.

UI improvements must remain separated from business logic wherever practical.

## 11. Performance & Accessibility

The redesigned UI should prioritize:

- Smooth scrolling.
- Efficient artwork loading and caching.
- Minimal unnecessary recomposition.
- Stable playback while navigating.
- Touch targets that are easy to use.
- Readable text hierarchy.
- Theme-aware contrast.
- Sensible animation durations.
- Graceful behavior on smaller screens.

## 12. Quality Standard

The finished Sonora interface should look like a deliberate, professionally designed Android music product rather than a generic generated UI.

Acceptance criteria:

- No clipped controls.
- No overlapping components.
- No raw debug text.
- No broken navigation states.
- No inconsistent spacing.
- No placeholder-looking production screens.
- Playback remains reliable while UI surfaces change.
- Theme changes update all relevant surfaces correctly.
- Mini-player controls remain accessible during browsing.

## 13. Release Documentation

This report records the intended UI/UX direction for the renamed **Sonora** project and provides a reference for future UI changes.

The existing project documentation and upgrade notes remain the source of truth for implemented technical features. This document focuses specifically on the product presentation, interaction quality, and visual redesign direction.

---

**Sonora — Music, without the noise.**
