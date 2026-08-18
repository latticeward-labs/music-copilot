# Cluster D - Connected Voice Flow

```mermaid
flowchart TB

    A["Driving Mode / Full Player\nManual controls remain available"] -->|"Explicit microphone tap"| B{"Approved speech capability?"}
    B -->|"No / denied / unknown"| U["Unavailable / Permission\nManual Only\nSetup only outside active Driving Mode"]
    B -->|"Yes"| L1["LISTENING attempt 1\nDuck playback\nMaximum 6 seconds\nCancel available"]
    L1 -->|"STT result"| P["PROCESSING\nRelease ducking\nDeterministic six-intent mapper"]
    L1 -->|"Eligible recognition failure"| R{"Retry budget available?"}

    P -->|"Certain supported result"| X["Submit exactly one command\nPlayback Service / MediaSession"]
    P -->|"One or two plausible candidates"| C["CONFIRMING\nOne short bounded selection"]
    P -->|"Missing / unknown / unsupported / not found"| F["FAILED SAFE\nNo command\nPlayback preserved"]

    C -->|"Presented candidate selected"| X
    C -->|"Reject / unclear / timeout / cancel"| F
    X -->|"Acknowledged"| E["EXECUTED\nRefresh authoritative state\nAt most one short TTS"]
    X -->|"Rejected / stale"| F
    E -->|"TTS success or failure"| M["Return to initiating surface\nManual controls and Exit"]

    R -->|"Yes, explicit Retry"| L2["LISTENING attempt 2\nDuck playback\nMaximum 6 seconds"]
    R -->|"Manual / dismiss"| M
    L2 -->|"STT result"| P
    L2 -->|"Any failure"| T["Terminal failure\nNo third attempt\nRelease ducking"]
    T --> M
    F --> M
    U --> M

    I["Invariants\nNo wake word or raw-audio storage\nNo transcript / artist / title telemetry\nNo guessed intent or nearest artist\nCloud path needs separate consent + complete disclosure"]
    P -.-> I
```

The Markdown graph and raw `.mmd` file are canonical logical peers. Exact visual design, animations, copy, and component ownership remain downstream Wireframe/architecture work.
