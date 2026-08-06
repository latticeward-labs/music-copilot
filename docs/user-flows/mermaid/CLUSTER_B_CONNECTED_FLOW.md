# Cluster B — Connected Flow Diagram

**Scope:** UF-04 → UF-05 → UF-06  
**Product:** Music Copilot MVP v1.0  
**Reviewed:** 06 August 2026  
**Connected-flow review:** **PASS**  
**Cluster B formal exit:** **OPEN** — Figma synchronization, executed Technical Spikes, and cross-role sign-off remain outstanding  
**Controlled review record:** [`../CLUSTER_B_CONNECTED_REVIEW.md`](../CLUSTER_B_CONNECTED_REVIEW.md)  
**Raw source:** [`CLUSTER_B_CONNECTED_FLOW.mmd`](CLUSTER_B_CONNECTED_FLOW.mmd)

This diagram is the canonical connected behavior map for Cluster B. It connects the approved specification-level contracts of UF-04, UF-05, and UF-06 without replacing their detailed state inventories.

```mermaid
flowchart TB

    subgraph ENTRY["UPSTREAM ENTRY"]
        direction TB
        U3["UF-03 verified Library state"]
        E{"Library usable?"}
        U2["UF-02 permission recovery"]
        U3R["UF-03 scan / reconciliation"]
        U3 --> E
        E -->|"Access denied / revoked"| U2
        E -->|"Empty / stale / unknown"| U3R
    end

    subgraph UF04["UF-04 — BROWSE AND SEARCH"]
        direction TB
        L["Home / Library Ready\nSongs · Artists · Albums · Folders"]
        P{"Browse or Search?"}
        B["Browse verified local collection"]
        S["Local Search\nResult · No Result · Error"]
        V["Revalidate selected item\nPermission + MediaStore"]
        A{"Playback service acknowledges start?"}
        F4["Retain Browse/Search context\nRecover without false Playing"]
        L --> P
        P -->|"Browse"| B
        P -->|"Search"| S
        B --> V
        S -->|"Select result"| V
        V --> A
        A -->|"No"| F4
        F4 --> P
    end

    E -->|"Ready"| L
    U2 -->|"Granted"| E
    U3R -->|"Ready"| E
    V -->|"Access / item stale"| U3R
    V -->|"Access revoked"| U2

    subgraph UF05["UF-05 — MANUAL PLAYBACK AND QUEUE"]
        direction TB
        Q["Service accepts visible local collection as queue\nVisible order · selected item current"]
        C["Full Player / Mini-player / Queue\nObserve one MediaSession"]
        M{"Manual action?"}
        CMD["Play · Pause · Next · Previous · Seek\nSelect queue item"]
        END["End of Queue\nPaused / Completed · no wrap or repeat"]
        BAD["Corrupt / moved / deleted / inaccessible\nBounded forward skip or truthful terminal error"]
        REC["Controller reconnect\nRefresh complete authoritative snapshot"]
        Q --> C
        C --> M
        M -->|"Supported command"| CMD
        CMD --> C
        M -->|"Queue exhausted"| END
        M -->|"Item failure"| BAD
        BAD -->|"Playable item remains"| C
        BAD -->|"No playable item"| END
        M -->|"Controller lost"| REC
        REC --> C
    end

    A -->|"Yes — authoritative Playing / Paused"| Q

    subgraph UF06["UF-06 — BACKGROUND AND SYSTEM CONTROLS"]
        direction TB
        T{"UI / device / system transition?"}
        P0["P0 active continuity\nBackground · UI closure/recreation · screen off/lock"]
        SYS["Notification · lock screen · Bluetooth/headset\nCompatible vehicle media keys\nControllers of the same MediaSession"]
        ACK["Route supported command to MediaSession\nRefresh every surface after acknowledgement"]
        RET["Return to app\nReconnect and restore the complete live snapshot"]
        LOST["Active lifecycle lost"]
        SNAP{"Valid local P1 snapshot?"}
        P1["P1 Paused Restore\nValidate queue/item/position\nExplicit Resume only"]
        HOME["Discard unusable app restore state\nReturn Home / Library"]
        T -->|"Service exists"| P0
        P0 --> SYS
        SYS --> ACK
        ACK --> SYS
        SYS --> RET
        T -->|"Service/session lost"| LOST
        LOST --> SNAP
        SNAP -->|"Yes / partial"| P1
        SNAP -->|"No"| HOME
    end

    M -->|"Background / close / screen off / system surface"| T
    RET --> C
    P1 -->|"Explicit Resume acknowledged"| C
    HOME --> L

    subgraph CROSS["CROSS-FLOW HANDOFFS AND LOCKED BOUNDARIES"]
        direction TB
        H11["UF-11\nCall · Audio Focus · disconnect/reconnect\nAudio Route and speaker-leak prevention"]
        H7["UF-07 Driving Mode\nIn-app surface only"]
        N["Notification permission where applicable\nContextual and non-blocking; exemption path preserved"]
        X["No account · backend · sync · catalog · streaming\nNo upload/download · Android Auto · Cast · iOS · v1.1"]
    end

    SYS -.->|"Interruption / route event"| H11
    C -.->|"Enter Driving Mode"| H7
    SYS -.->|"Visibility rule"| N
    L -.->|"Scope invariant"| X
    C -.->|"Scope invariant"| X
    SYS -.->|"Scope invariant"| X

    AUTH["Single live authority\nPlayback Service / MediaSession owns player, queue, item, state and position\nApp UI and system surfaces are controllers only"]
    Q -.-> AUTH
    C -.-> AUTH
    SYS -.-> AUTH

    OPEN["Evidence still open\nTS-01 · TS-02 · TS-06 · TS-08 · numeric T_PREVIOUS\nIssue #16 mechanics · Figma sync · cross-role sign-off"]
    AUTH -.-> OPEN

    classDef action fill:#EAF2FF,stroke:#3776D6,color:#1F2633,stroke-width:1.5px;
    classDef decision fill:#FFF4D6,stroke:#C68817,color:#1F2633,stroke-width:1.5px;
    classDef success fill:#EAF8EE,stroke:#2E8B4B,color:#1F2633,stroke-width:1.5px;
    classDef recovery fill:#FFE8E8,stroke:#C63B3B,color:#1F2633,stroke-width:1.5px;
    classDef note fill:#EEF1F5,stroke:#667085,color:#1F2633,stroke-width:1.5px;

    class U3,L,B,S,Q,C,CMD,P0,SYS,ACK,RET,P1 action;
    class E,P,A,M,T,SNAP decision;
    class END,AUTH success;
    class U2,U3R,F4,BAD,REC,LOST,HOME recovery;
    class H11,H7,N,X,OPEN note;

    style ENTRY fill:transparent,stroke:#C7CED9,stroke-width:1px
    style UF04 fill:transparent,stroke:#3776D6,stroke-width:2px
    style UF05 fill:transparent,stroke:#16BEC7,stroke-width:2px
    style UF06 fill:transparent,stroke:#2E8B4B,stroke-width:2px
    style CROSS fill:transparent,stroke:#667085,stroke-width:1px
```

## Reading rule

- Solid arrows are user/system transitions inside the connected Cluster B contract.
- Dotted arrows are governed handoffs, scope boundaries, or unresolved evidence—not alternate happy paths.
- Detailed substates and exception rules remain normative in the three individual UF documents.
- A system surface never owns playback; it issues commands to and observes the same service-owned MediaSession.

## Review boundary

The diagram passes connected-flow review because it has no missing handoff between UF-04, UF-05, and UF-06 and preserves every locked cross-flow invariant. This PASS does not assert Figma parity, device/OEM evidence, implementation readiness, Product/Design/Android/QA/Privacy approval, or final Cluster B exit.
