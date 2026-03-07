# UniFi Auto Access (Android Auto)

This project is a starter Android + Android Auto app that:

1. Signs users in with **Microsoft Entra ID** via MSAL.
2. Fetches available UniFi **doors/gates** from the UniFi Access API.
3. Shows those entries on the Android Auto screen.
4. Sends an open command when the user taps a door/gate.

## What you must configure

Before building, update these values:

- `app/src/main/res/raw/auth_config_single_account.json`
  - `client_id`
  - `tenant_id`
  - `redirect_uri`
- `AuthManager` scopes (`api://unifi/.default`) if your API app registration differs.
- UniFi API endpoint paths in `UnifiApiService` if your UniFi controller version differs.

## Runtime usage

1. Launch app on phone.
2. Enter your UniFi controller base URL (e.g. `https://controller.example.com/`).
3. Tap **Sign in with Microsoft Entra**.
4. Tap **Refresh doors/gates**.
5. Open Android Auto and use the **UniFi Doors & Gates** screen.
6. Tap a door or gate row to send open command.

## Notes

- Android Auto sign-in UI is intentionally on the phone app; automotive templates do not support full web sign-in flows.
- This repo is a starting point and should be reviewed for your environment's production security requirements (certificate pinning, stricter host validation, telemetry, retries, etc.).
