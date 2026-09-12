# 🌐 Distributed Systems Laboratory

> **An interactive desktop laboratory for exploring, simulating, and visualizing distributed systems concepts through real-time JavaFX simulations.**

**Distributed Systems Laboratory** is a Java-based educational engineering application designed to make complex distributed computing concepts **observable, interactive, and easier to understand**.

Instead of learning distributed systems only through diagrams and theory, this project provides a visual environment where users can create nodes, simulate communication, trigger failures, observe algorithms, and inspect system behavior in real time.

Built with **Java 25 + JavaFX**.

---

## ✨ Highlights

* 🖥️ Modern JavaFX desktop interface
* 🌐 Interactive distributed network visualization
* 🔄 Real-time message simulation
* 🧠 Distributed algorithm simulations
* 👑 Leader election
* 🤝 Consensus algorithms
* 🕐 Logical and vector clocks
* 💾 Data replication
* ⚡ Fault injection and failure simulation
* 🔐 Distributed transactions
* 📊 Real-time system metrics
* 📝 Event and message logging
* ⏯️ Play / Pause / Step simulation controls
* 🎛️ Configurable network conditions
* 🧩 Modular architecture designed for extension

---

# 🚀 What Can You Explore?

The laboratory brings multiple distributed-systems concepts together inside a single interactive environment.

### 🧩 Network Simulation

Create and manipulate a distributed network containing multiple nodes.

You can simulate:

* Node creation
* Node removal
* Node startup
* Node shutdown
* Node failure
* Node recovery
* Node-to-node connections
* Message transmission
* Network latency
* Packet loss
* Network partitions

---

### 👑 Leader Election

Explore how distributed nodes select a coordinator when there is no central authority.

Implemented concepts include:

* **Bully Algorithm**
* **Ring Election Algorithm**

Observe:

```text
Election Started
       ↓
Election Messages
       ↓
Votes / Responses
       ↓
Coordinator Announcement
       ↓
New Leader
```

---

### 🤝 Raft Consensus

Explore the fundamental concepts behind the **Raft consensus algorithm**.

The simulation models:

* Leader
* Follower
* Candidate
* Terms
* Elections
* Voting
* Heartbeats
* Log replication
* Commit index
* Leader failure
* Leader recovery

The interface allows the simulation to be executed continuously or one event at a time.

```text
             ┌──────────────┐
             │    LEADER    │
             └──────┬───────┘
                    │
             ┌──────┴──────┐
             ▼             ▼
        ┌──────────┐  ┌──────────┐
        │ FOLLOWER │  │ FOLLOWER │
        └──────────┘  └──────────┘
```

---

### 🕐 Lamport Logical Clocks

Visualize logical time inside a distributed system.

The laboratory demonstrates how nodes maintain logical clocks despite having no globally shared physical clock.

```text
Node A        Node B

  1  ───────►  1
  2  ───────►  2
  3  ───────►  4
```

Explore:

* Event ordering
* Message timestamps
* Causal relationships
* Distributed event sequencing

---

### 🧮 Vector Clocks

Explore causal relationships between distributed events using vector clocks.

The system can distinguish between:

* Before
* After
* Concurrent

This makes it possible to visualize situations where two events cannot be causally ordered.

---

### 🔗 Chord Distributed Hash Table

Explore the architecture of a distributed hash table using a Chord-style ring.

Features include:

* Node joining
* Node leaving
* Key hashing
* Successor discovery
* Predecessor relationships
* Finger tables
* Distributed key lookup
* Node failures

Example:

```text
             Node 8
          ╱          ╲
       Node 4        Node 12
        │               │
       Node 2 ─────── Node 14
```

---

### 💾 Distributed Replication

Explore how distributed systems maintain multiple copies of data.

Simulate:

* Primary node
* Replica nodes
* Synchronous replication
* Asynchronous replication
* Primary failure
* Replica recovery

Observe how data propagates throughout the system.

---

### 🔄 Consistency Models

Compare different consistency approaches.

The laboratory demonstrates concepts such as:

* Strong consistency
* Eventual consistency
* Replication delay
* Stale reads
* Concurrent writes

---

### 💳 Two-Phase Commit

Explore distributed transaction coordination using **2PC**.

The simulation contains:

```text
             Coordinator
             /    |    \
            /     |     \
           ▼      ▼      ▼
        Node A  Node B  Node C
```

### Phase 1 — Prepare

Participants vote:

```text
PREPARE
   ↓
YES / NO
```

### Phase 2 — Decision

The coordinator determines:

```text
COMMIT
   or
ABORT
```

Failures can be introduced to observe how the protocol behaves.

---

### 💥 Fault Injection

Distributed systems must assume that things will fail.

The laboratory allows controlled failure scenarios including:

* Node crashes
* Network partitions
* Message loss
* Message delays
* Packet loss
* Leader failure
* Replica failure
* Timeouts
* Recovery

Example:

```text
Node A ● ─────────────── X ─────── ● Node B
                           NETWORK FAILURE
```

---

### ⚖️ CAP Theorem

Explore the relationship between:

* **Consistency**
* **Availability**
* **Partition Tolerance**

The laboratory provides interactive scenarios that demonstrate the consequences of network partitions.

---

### ⚡ Load Balancing

Experiment with different request-distribution strategies:

* Round Robin
* Weighted Round Robin
* Least Connections
* Random
* Least Load

Monitor:

* Requests
* Server load
* Latency
* Successful requests
* Failed requests
* Throughput

---

### 🌍 Peer-to-Peer Networks

Simulate decentralized peer-to-peer communication.

Features include:

* Peer discovery
* Direct messaging
* Broadcast
* Peer joining
* Peer leaving
* Peer failure
* Network recovery

---

### 📁 Distributed File Systems

Explore how files can be divided into chunks and distributed across multiple nodes.

Example:

```text
document.dat
│
├── Chunk 1 → Node 1 + Node 3
├── Chunk 2 → Node 2 + Node 4
└── Chunk 3 → Node 1 + Node 4
```

When a node fails, replicated chunks can be used for recovery.

---

# 🎮 Simulation Controls

The laboratory provides interactive simulation controls.

| Control   | Purpose                      |
| --------- | ---------------------------- |
| ▶ Play    | Run the simulation           |
| ⏸ Pause   | Pause execution              |
| ⏭ Step    | Execute one simulation event |
| 🔄 Reset  | Restore initial state        |
| ⚡ Speed   | Change simulation speed      |
| 💥 Fail   | Inject node failure          |
| ♻ Recover | Recover a failed node        |

The **Step Mode** is particularly useful for understanding algorithms event by event.

---

# 📊 Real-Time Monitoring

The application provides real-time information about the distributed system.

Monitor metrics such as:

* Total Nodes
* Online Nodes
* Failed Nodes
* Messages Sent
* Messages Received
* Messages Lost
* Average Latency
* Throughput
* Active Connections
* Current Leader
* Current Term
* Replication State

---

# 📝 Event Log

Every important simulation event can be recorded.

Example:

```text
[16:30:12.124] Node 1 started
[16:30:12.300] Node 1 → Node 2 : HEARTBEAT
[16:30:13.001] Node 3 started election
[16:30:13.120] Node 3 → Node 1 : REQUEST_VOTE
[16:30:13.210] Node 1 → Node 3 : VOTE_GRANTED
[16:30:14.000] Node 3 became LEADER
```

The event system makes it possible to understand not only **what happened**, but also **when and why it happened**.

---

# 🏗️ Architecture

The project follows a modular architecture separating the simulation engine, distributed algorithms, network model, and JavaFX presentation layer.

```text
DistributedSystemsLab
│
├── application
│   └── Main
│
├── model
│   ├── Node
│   ├── Message
│   ├── Network
│   ├── Cluster
│   └── LogEntry
│
├── simulation
│   └── SimulationEngine
│
├── algorithms
│   ├── raft
│   ├── election
│   ├── clocks
│   ├── chord
│   ├── replication
│   ├── transactions
│   └── loadbalancing
│
├── network
│   ├── MessageRouter
│   ├── TCPManager
│   └── UDPManager
│
├── services
│
├── controllers
│
├── visualization
│   ├── NetworkCanvas
│   ├── NodeView
│   └── MessageAnimation
│
├── ui
│
└── utils
```

The architecture is designed to keep distributed-system logic independent from the JavaFX user interface.

---

# 🛠️ Technology Stack

| Technology                  | Purpose                              |
| --------------------------- | ------------------------------------ |
| **Java 25 LTS**             | Core application and algorithms      |
| **JavaFX**                  | Desktop UI and visualization         |
| **Java Concurrency API**    | Simulation and concurrent operations |
| **Canvas / JavaFX Nodes**   | Network visualization                |
| **CSS**                     | UI styling                           |
| **Eclipse / IntelliJ IDEA** | Development                          |

### Requirements

* Java 25
* JavaFX SDK
* Eclipse or IntelliJ IDEA

No Maven or Gradle is required.

---

# ▶️ Running the Project

## 1. Clone the repository

```bash
git clone https://github.com/qusayjber/DistributedSystemsLab.git
```

## 2. Open the project

Open the project in:

* Eclipse
* IntelliJ IDEA

## 3. Configure JavaFX

Make sure the JavaFX SDK is configured correctly for your Java 25 installation.

Add the required JavaFX modules to the project.

For example:

```text
--module-path "PATH_TO_FX/lib"
--add-modules javafx.controls,javafx.graphics,javafx.fxml
```

Adjust the modules according to the project requirements.

## 4. Run

Run:

```text
application.Main
```

The Distributed Systems Laboratory dashboard should start.

---

# 🧠 Educational Goals

This project is designed to make difficult distributed-systems concepts easier to understand through experimentation.

Instead of only reading:

> "A leader election algorithm selects a coordinator."

The user can actually watch:

```text
Node 1 ── Election ──► Node 2
Node 2 ── Election ──► Node 3
Node 3 ── Vote ──────► Node 1
Node 3 ── Coordinator ──► ALL
```

The goal is to connect:

**Theory → Algorithm → Simulation → Visualization → Understanding**

---

# 🔬 Concepts Covered

The laboratory brings together a broad range of distributed-systems topics:

* Distributed communication
* Client-server architecture
* Peer-to-peer systems
* Remote communication
* Leader election
* Consensus
* Raft
* Logical clocks
* Lamport clocks
* Vector clocks
* Causal ordering
* Distributed hash tables
* Chord
* Replication
* Consistency
* Fault tolerance
* Network partitions
* CAP theorem
* Distributed transactions
* Two-phase commit
* Load balancing
* Distributed storage
* Failure recovery

---

# 📈 Project Roadmap

The project is designed to evolve into a comprehensive distributed-systems experimentation platform.

### Core

* [x] JavaFX application
* [x] Network visualization
* [x] Node model
* [x] Message model
* [x] Simulation engine
* [x] Event logging

### Distributed Algorithms

* [x] Lamport Clock
* [x] Vector Clock
* [x] Bully Election
* [x] Ring Election
* [x] Raft
* [x] Chord

### Distributed Data

* [x] Replication
* [x] Consistency simulation
* [x] Distributed storage

### Transactions

* [x] Two-Phase Commit
* [x] Failure scenarios

### Infrastructure

* [x] Fault injection
* [x] Network latency simulation
* [x] Packet loss simulation
* [x] Network partitions
* [x] Metrics
* [x] Exportable logs

> The exact availability of individual modules depends on the current implementation in the repository.

---

# 🎯 Why This Project?

Distributed systems are difficult to understand because much of their behavior is invisible.

A node sends a message.

Another node receives it later.

A leader fails.

An election begins.

Two events happen concurrently.

A network partition occurs.

Replicas become temporarily inconsistent.

These behaviors are much easier to understand when they can be **seen and manipulated**.

This laboratory was created to turn distributed-systems theory into an interactive environment.

---

# 👨‍💻 Author

**Qusai Jaber**

Computer Science / Software Engineering

GitHub:

**https://github.com/qusayjber**

---

# 📜 License

This project is provided for educational and portfolio purposes.

Add a specific open-source license to this repository if you intend to permit reuse, modification, and redistribution.

---

# ⭐ Support

If you find this project useful for learning distributed systems, consider giving the repository a ⭐ on GitHub.

---

<p align="center">

**Distributed Systems Laboratory**

*Understand distributed systems by watching them work.*

</p>
