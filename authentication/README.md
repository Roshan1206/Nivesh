# Auth
Responsible for managing user across Nivesh

### System user
In case of 401 do forgot password
```json
{
  "email": "system@nivesh.com",
  "password": "Password@1234"
}
```

## Tokens & Cache
3 different types of token is user.
1. Access Token - Normal user flow. KYC done
2. Onboarded Token - User has completed registration
3. Refresh token - Refresh token for user

Cache is used for user registration and login
```yaml
nivesh:
  auth:
    token:
      onboarded-expiry: 10 //user registration in min
      access-expiry: 15   // access token - in min
      refresh-expiry: 7   // refresh token - in days
    cache:
      max-attempt: 3  //user locked after 3 failed login
      max-cache-size: 500
      lock-duration-min: 60 //locked for 1 hour
```