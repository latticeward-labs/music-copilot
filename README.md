# Music Copilot

> An AI-assisted music experience by **Latticeward Labs**.

Music Copilot is an early-stage product focused on helping listeners discover, organize, and interact with music through a simple mobile experience and carefully scoped intelligent features.

## Project status

**Phase:** MVP definition and product foundation  
**Repository:** Private  
**Primary target:** Android internal testing

The product scope, technical baseline, and UX flows are being finalized before feature development begins.

## MVP goals

- A clear and reliable onboarding flow
- Home and music discovery surfaces
- Search
- A functional player using legally approved audio sources
- Likes and playlists
- Minimal, privacy-conscious product analytics
- Crash reporting
- An internal Android test release

See [Product Scope](docs/PRODUCT_SCOPE.md) for the current boundaries.

## Repository map

```text
.github/                   Collaboration templates
docs/                      Product and engineering decisions
README.md                  Project entry point
CONTRIBUTING.md            Team workflow
SECURITY.md                Security reporting policy
```

Application source will be added only after the technical decision issue is approved.

## Working model

- `main`: stable, reviewed state
- `develop`: integration branch for active development
- `feature/<short-name>`: new product work
- `fix/<short-name>`: bug fixes
- Pull requests are required for product code
- Every task must have acceptance criteria
- No secrets, production credentials, copyrighted tracks, or personal data may be committed

## Start here

1. Read [Product Scope](docs/PRODUCT_SCOPE.md).
2. Review the open GitHub Issues and select only items marked **Ready** on the project board.
3. Confirm the technical baseline in the architecture decision issue.
4. Design the core user flows in Figma.
5. Build the first vertical slice: open app → find approved sample audio → play it.
6. Validate the slice before expanding the backlog.

## Core documentation

- [Product Scope](docs/PRODUCT_SCOPE.md)
- [Technical Decisions](docs/TECHNICAL_DECISIONS.md)
- [Roadmap](docs/ROADMAP.md)
- [Analytics & Privacy](docs/ANALYTICS_AND_PRIVACY.md)
- [Definition of Done](docs/DEFINITION_OF_DONE.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)

## Legal note

Music Copilot must not stream, copy, cache, distribute, or monetize commercial music without the necessary licenses and agreements. Development should use owned, licensed, public-domain, or explicitly authorized sample audio.

---

Built by **Latticeward Labs**.
