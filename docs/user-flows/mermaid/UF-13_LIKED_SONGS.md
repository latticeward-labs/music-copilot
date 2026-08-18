# UF-13 - Canonical Mermaid Diagram

This rendering is canonical with [`UF-13_LIKED_SONGS.mmd`](UF-13_LIKED_SONGS.mmd). It shows the P2 schedule gate, Empty/Ready states, local mutation acknowledgement, MediaStore reconciliation, and service-owned playback handoff.

```mermaid
flowchart TD
    A[Home or Library] --> B{P2 ships?}
    B -- No --> C[No entry or Like control<br/>P0 and P1 unaffected]
    B -- Yes --> D[Open Liked Songs]
    D --> E[Load acknowledged likes]
    E --> F[Reconcile with accessible MediaStore projection]
    F --> G{Accessible liked items?}
    G -- No --> H[Empty<br/>Explanation and Back]
    G -- Yes --> I[Ready<br/>Rows, Play list, Unlike, Back]
    I --> J{Action}
    J -- Play list --> K[Submit accessible queue<br/>start index 0]
    J -- Select row --> L[Submit same queue<br/>selected start index]
    K --> M{Service acknowledges?}
    L --> M
    M -- Yes --> N[Full Player observes Playing]
    M -- Stale or rejected --> O[Reconcile again<br/>no false Playing]
    O --> G
    J -- Unlike --> P[Remove only local Like record]
    P --> Q{Write acknowledged?}
    Q -- Yes --> F
    Q -- No --> R[Keep previous acknowledged state<br/>Retry or dismiss]
    R --> I
    S[Full Player] --> T[Like or Unlike]
    T --> U{Local write acknowledged?}
    U -- Yes --> V[Observable Like state updated]
    U -- No --> W[Previous state retained<br/>Retry outside Driving Mode]
    H --> A
    I --> A
```

The diagram does not imply a database schema, UI styling, or automatic navigation from Full Player.
