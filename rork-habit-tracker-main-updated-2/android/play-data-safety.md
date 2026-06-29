# Ember — Google Play Data Safety Form Answers

Use these answers when filling out the **Data safety** section in Play Console.

---

## Section 1: Data collection and security

**Does your app collect or share any of the required user data types?**
> **Yes** — minimal data is processed for optional features (AI insights, cloud sync) and for showing ads via Google AdMob in the free tier. Choose Yes so you can disclose them honestly.

**Is all of the user data collected by your app encrypted in transit?**
> **Yes**

**Do you provide a way for users to request that their data is deleted?**
> **Yes** — in-app delete (Settings) and email request to support@ember.app.

**Has your app been independently validated against a global security standard?**
> **No** (unless you obtain MASA validation later).

---

## Section 2: Data types collected

For each item below: **Collected? Yes**, **Shared? No**, **Processing: Ephemeral** unless noted, **Required or Optional: Optional**, **Purposes:** as listed.

### App activity
- **App interactions** — Collected, not shared, **ephemeral**, optional. Purpose: **App functionality** (AI insights only — habit names & completion history sent on request).
- **Other user-generated content** — Collected, not shared, **persistent (only if Cloud Sync enabled)**, optional. Purpose: **App functionality, Account management**. Description: "Habit names, notes, and check-in history backed up to your account."

### Financial info
- **Purchase history** — Collected (via Google Play Billing), not shared, persistent, required for purchases. Purpose: **App functionality** (verify Pro entitlement).

### Device or other IDs
- **Advertising ID** — Collected (via Google AdMob, free tier only), **shared** with AdMob/Google's ad network, ephemeral, optional (Pro removes ads entirely). Purpose: **Advertising or marketing**.

### Everything else
Mark as **Not collected**:
- Personal info (name, email, address, phone, race, political views, etc.)
- Location (approximate or precise)
- Health & fitness data
- Messages, photos, videos, audio files, voice recordings
- Contacts, calendar
- Files & docs
- Web browsing history
- Crash logs, diagnostics, performance data (unless you later add Crashlytics)

---

## Section 3: Data sharing

> **Advertising ID is shared with Google AdMob** to serve ads in the free tier — this is the one real third-party data share in this app. No other data is shared with third parties for advertising, analytics, or sale.
> Google Play Billing handles purchases — that is Google's own processing, not a third-party share.

---

## Section 4: Practices

- **Data is encrypted in transit:** Yes (HTTPS for AI requests & cloud sync)
- **Users can request data deletion:** Yes (in-app + email)
- **Committed to Play Families Policy:** Optional — leave No unless targeting kids
- **Independent security review:** No

---

## Section 5: Privacy policy URL

`https://YOUR_GITHUB_USERNAME.github.io/rork-habit-tracker/privacy-policy.html`

Host the ready-made page at `docs/privacy-policy.html` via GitHub Pages (Settings → Pages → Source: `main` branch, `/docs` folder) before submitting. Play requires the URL to be publicly reachable and to load over HTTPS — GitHub Pages serves HTTPS automatically.

---

## Quick filling order in Play Console

1. **App content → Privacy policy** → paste URL
2. **App content → Data safety** → use answers above
3. **App content → Ads** → "Yes, my app contains ads" (free tier shows banner ads via AdMob; Pro removes them)
4. **App content → Content rating** → fill questionnaire (Everyone) — note ads may slightly affect rating in some regions; answer the ads sub-questions honestly (non-violent, no gambling-adjacent ad content expected from AdMob's standard categories)
5. **App content → Target audience** → 13+
6. **App content → News app** → No
7. **App content → COVID-19 contact tracing** → No
8. **App content → Data deletion** → in-app + support@ember.app
9. **Monetize → Privacy & messaging → EU User Consent** → confirm the UMP consent message is enabled (the app's `ConsentManager` already requests it on launch, but Play also wants this declared here)

Once those nine items are green, your Internal testing release can move to Production review.
