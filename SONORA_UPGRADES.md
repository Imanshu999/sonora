# Sonora upgrade pack

Implemented in this source build:

1. Light / White theme option.
2. Dark theme remains available.
3. AMOLED black theme remains available.
4. System theme follows Android automatically.
5. Theme-aware text/icon contrast.
6. Mini-player previous-track control.
7. Mini-player 10-second rewind.
8. Mini-player 10-second forward.
9. Mini-player play/pause control.
10. Mini-player next-track control.
11. Android Media3 MediaSession background playback/notification integration remains enabled.
12. Notification metadata uses title, artist, album and artwork from the active track.
13. Search source filters are horizontally scrollable so the last option no longer expands/clips the layout.
14. YouTube Music is exposed as a separate source filter instead of being silently hidden.
15. Search discovery now includes more genre/region tags (Hindi, English, Punjabi, Phonk, Brazilian Funk, Pop, Hip-Hop, Electronic, Lo-Fi, etc.).

Real-data note:
- Audius, Jamendo and FreeToUse are network-backed sources in this project.
- The YouTubeMusicSource is intentionally not a fake direct-audio extractor. A YouTube Data API key can provide metadata, but it does not grant a legal direct audio stream for ExoPlayer.
- The project therefore does not bundle or scrape copyrighted commercial music. A complete worldwide commercial catalog requires the appropriate licensed provider/API.
