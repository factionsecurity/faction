# FACTION 1.8.6

_Release date: TBD_

This release expands the default-vulnerability template API with full CRUD
endpoints, makes the CSV upload format header-driven (and forward-compatible
with CVSS 4.0 and custom fields), and ships a security policy for the project.

## 🎉 🚀 Upgrades 🎉 🚀

### Default-vulnerability template API — full CRUD

New endpoints on `/api/vulnerabilities`:

| Method | Path | Purpose |
| ------ | ---- | ------- |
| `GET`    | `/default/{id}`         | Fetch a single template (including custom fields). |
| `POST`   | `/default/{id}`         | Update a single template from a JSON body. Path id wins over body id. |
| `DELETE` | `/default/{id}`         | Delete a template and its associated custom field values. |
| `GET`    | `/default/search?name=` | Query-param form of the name search so values containing `/` (e.g. `LLMNR/NBT-NS`) round-trip cleanly. |

All endpoints return `404` when no template matches the given id, and `401`
when the API key is missing or invalid.

### CSV upload is now header-driven

`POST /api/vulnerabilities/csv/default` now matches columns by header name
(case-insensitive), so column order may vary and unknown columns are ignored.

Recognized headers:

```
Id, Name, CategoryId, CategoryName, Description, Recommendation,
SeverityId, ImpactId, LikelihoodId, isActive,
CVSS31Score, CVSS31String, CVSS40Score, CVSS40String, CustomFields
```

Highlights:
- **CVSS 4.0 columns** (`CVSS40Score`, `CVSS40String`) are supported in addition to the existing CVSS 3.1 columns.
- **Custom fields** round-trip through the `CustomFields` column as a JSON array (same shape produced by `GET /csv/default`).
- **Backward compatible** — if the first row is not a recognizable header, the upload falls back to the legacy positional column order, so older clients keep working.

### Security policy

A new top-level `SECURITY.md` documents how to report vulnerabilities
privately (GitHub Private Vulnerability Reporting and the project email),
the response SLA, scope, and a safe-harbor clause for researchers.

## 🐛 Bugfixes 🐛

- `FSUtils.getEnv` now falls back to JVM system properties when the named
  OS environment variable is unset, fixing brittle behavior in deployments
  that configure FACTION via `-D` flags.
- `HibHelper` now reads all `FACTION_MONGO_*` settings through
  `FSUtils.getEnv`, so the system-property fallback above applies to the
  Hibernate OGM bootstrap as well (previously some keys bypassed it and
  could NPE when only sysprops were set).

## 🧰 Internal / Test infrastructure

- New `VulnerabilitiesIntegrationTest` exercises the new endpoints
  end-to-end against MongoDB (via Testcontainers, or an external instance
  configured with `FACTION_MONGO_*` sysprops/env).
- `VulnerabilitiesUnitTest` gains coverage for the CSV upload helpers
  (`buildHeaderMap`, `getCol`, header detection, escape handling).
- `pom.xml` pins `maven-surefire-plugin` to 3.2.5 and adds
  `--add-opens=java.base/java.lang=ALL-UNNAMED` so Hibernate OGM's
  Javassist proxies work on JDK 9+.
- Removed the stale `AI_CONFIG_DOCUMENTATION.md` from the repo root.

## Upgrade notes

- **No database migration required.**
- **API:** all existing endpoints and CSV payloads continue to work
  unchanged. Clients that want the new CSV features should add the
  header row described above.
- **Permissions:** the new CRUD endpoints use the same API-key
  authentication as the existing `/api/vulnerabilities` endpoints.

## Full changelog

See the GitHub compare view for the complete list of commits:
<https://github.com/factionsecurity/faction/compare/1.8.5...1.8.6>
