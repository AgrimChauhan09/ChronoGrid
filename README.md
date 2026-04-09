# ChronoGrid: Distributed Job Scheduler

**ChronoGrid** is a highly scalable, fault-tolerant, and distributed microservices-based job scheduling system. Think of it as an enterprise-level background task manager. It accepts tasks (like sending emails or processing data), queues them, assigns them to the least-loaded worker, and ensures they are executed successfully—even if a server crashes.

ChronoGrid intentionally avoids heavy external message brokers like Kafka or RabbitMQ. Instead, it relies on a custom, thread-safe in-memory `LinkedBlockingQueue` backed by MongoDB for persistence.

---

##  Tech Stack
- **Language:** Java
- **Framework:** Spring Boot (Microservices Architecture)
- **Database:** MongoDB
- **Build Tool:** Maven
- **Inter-service Communication:** REST APIs

---

## Architecture & Microservices Flow

The system is divided into focused, single-responsibility microservices. Here is the step-by-step flow of how a job is processed:

1. **Gateway Service (Port: 8089):** The Front Door. All external client requests enter here and are dynamically routed to the appropriate internal service.
2. **Job Scheduler Service (Port: 8087):** The Brain. It validates the job, saves it in MongoDB with a `PENDING` status, and sends it to the Queue.
3. **Queue Service (Port: 8085):** The Waiting Room. It stores jobs in a thread-safe FIFO queue and saves a backup in the database (Status: `QUEUED`).
4. **Coordinator Service (Port: 8086):** The Manager. It runs every 1 second, pulls jobs from the Queue, checks the Load Balancer for the freest Worker (Least-Loaded strategy), and assigns the job.
5. **Worker Service (Port: 8082):** The Executor. It receives the job, updates the status to `RUNNING`, performs the actual business logic, and finally updates the status to `SUCCESS` (or `FAILED`).

```text
+-------------------------------------------------------+
|                   CLIENT / POSTMAN                    |
+-------------------------------------------------------+
                           | HTTP POST /api/jobs/submit
                           v
+-------------------------------------------------------+
|                API GATEWAY (Port 8089)                |
| - Single entry point for all requests                 |
| - Forwards matching routes to Job Scheduler           |
+-------------------------------------------------------+
                           |
                           v
+-------------------------------------------------------+
|             JOB SCHEDULER SERVICE (Port 8087)         |
|                     (The Brain)                       |
| - Validates & saves job as PENDING                    |
| - Sends job payload to Queue Service                  |
| - Exposes API for workers to update status            |
+-------------------------------------------------------+
         |                                  |
   (Saves State)                      (Pushes Job)
         |                                  |
         v                                  v
+------------------+        +-----------------------------------+
|     MONGODB      |        |       QUEUE SERVICE (Port 8085)   |
| (chronogrid DB)  |        | - LinkedBlockingQueue (In-Memory) |
|                  |<-------| - Backs up queue items to DB      |
| - jobs           |        +-----------------------------------+
| - workers        |                        |
| - schedules      |                        | (Polls Queue every 1s)
| - job_queue      |                        v
+------------------+        +-----------------------------------+
         ^                  |       COORDINATOR (Port 8086)     |
         |                  | - Dequeues job atomically         |
         |                  | - Uses INTERNAL LOAD BALANCER     |
         |                  | - Selects Least-Loaded Worker     |
         |                  +-----------------------------------+
         |                                  |
         |                      (HTTP POST /workers/execute)
         |                                  |
         |           +----------------------+----------------------+
         |           |                      |                      |
         |           v                      v                      v
         |   +----------------+     +----------------+     +----------------+
         |   | WORKER 1       |     | WORKER 2       |     | WORKER N       |
         |   | (Port 8082)    |     | (Port 80xx)    |     | (Port 80xx)    |
         +---| - Set RUNNING  |     | - Set RUNNING  |     | - Set RUNNING  |
             | - Execute task |     | - Execute task |     | - Execute task |
             | - Set SUCCESS  |     | - Set SUCCESS  |     | - Set SUCCESS  |
             +----------------+     +----------------+     +----------------+

==================== BACKGROUND FAULT TOLERANCE & CRON ====================

+-----------------------------+                 +-----------------------------+
| WATCHER SERVICE (Port 8083) |                 | SCHEDULER SERVICE (Port 8081)|
| - Scans DB for stuck jobs   | ----(Reads)---->| - Leader Election via DB    |
|   (RUNNING > 5 mins)        |                 | - Scans DB for due CRON jobs|
| - Sends RETRY to Scheduler  | <---(Writes)----| - Submits job to Scheduler  |
+-----------------------------+                 +-----------------------------+
```

### Background Services (For Reliability)
* **Watcher Service (Port: 8083):** The Security Guard. It continuously scans the database. If a job is stuck in `RUNNING` for more than 5 minutes (e.g., Worker crashed), it auto-retries the job.
* **Scheduler Service (Port: 8081):** The Clock. Handles time-based Cron jobs (e.g., "Run every day at 9 AM"). Uses MongoDB for Leader Election to prevent duplicate executions across multiple instances.
Gateway -> Scheduler -> Queue -> Coordinator -> Worker
---

## How to Run Locally

### Prerequisites
* Java 17 or higher
* MongoDB running locally on `localhost:27017`
* Maven installed

### Step 1: Database Setup
1. Open MongoDB Compass.
2. Connect to `mongodb://localhost:27017`.
3. Create a new database named exactly: **`chronogrid`**. (Spring Boot will auto-create collections upon startup).

### Step 2: Build the Project
Open your terminal in the root folder of the project and run:
```bash
mvn clean install -DskipTests
```
 ### Step 3: Start the Services (Strict Order)
 Microservices depend on each other. Run the main application files in your IDE in this exact sequence:
 
- **QueueServiceApplication** (Port: 8085)
- **JobSchedulerApplication** (Port: 8087)
- **CoordinatorApplication** (Port: 8086)
- **WorkerApplication** (Port: 8082) - Watch the Coordinator console to confirm the worker registers successfully.
- **GatewayApplication** (Port: 8089)
### MongoDB Collections Breakdown
Once running, ChronoGrid auto-generates these collections in your `chronogrid` database:

- **jobs**: The main collection. Stores the state (`PENDING`, `QUEUED`, `RUNNING`, `SUCCESS`) and payload of every job.
- **workers**: The attendance register. Stores details of active worker nodes and their current load.
- **job_queue**: A backup of the in-memory queue to prevent data loss on server restart.
- **coordinator_dispatches**: Receipts of which job was assigned to which worker.
- **leader_election**: Used by the Cron Scheduler to ensure only one instance fires scheduled jobs.

## Important API Endpoints (Postman Guide)
All client requests should be sent to the API Gateway (`8089`).

### 1. Submit a New Job
**Method:** POST

**URL:** [http://localhost:8089/api/jobs/submit](http://localhost:8089/api/jobs/submit)

**Headers:** Content-Type: application/json

**Body:**
```json
{
  "name": "Welcome Email Job",
  "type": "EMAIL",
  "cronExpression": "",
  "payload": {
    "to": "user@example.com",
    "subject": "Welcome!",
    "body": "Thank you for signing up."
  },
  "maxRetries": 3,
  "timeoutSeconds": 60
}
```

### 2. Check Job Status
**Method:** GET

**URL:** [http://localhost:8089/api/jobs/{jobId}](http://localhost:8089/api/jobs/%7BjobId%7D)
*(Replace `{jobId}` with the ID received from the submit response. Watch it go from QUEUED -> SUCCESS!)*

### 3. Check System Stats
- Queue Stats: [GET http://localhost:8085/queue/stats](http://localhost:8085/queue/stats)
- Worker Stats: [GET http://localhost:8082/workers/stats](http://localhost:8082/workers/stats)
- Coordinator Stats: [GET http://localhost:8086/coordinator/stats](http://localhost:8086/coordinator/stats)

## 🎯 Key Design Highlights
- **Custom In-Memory Queue:** Avoids the overhead of external brokers like Kafka while maintaining thread safety via Java's `LinkedBlockingQueue`.
- **Smart Load Balancing:** Implements a Least-Connections strategy to dynamically assign work to the most available worker.
- **Self-Healing:** The Watcher service ensures that dead or stuck jobs are caught and automatically re-queued.
- **Leader Election:** Uses MongoDB TTL (Time-To-Live) indexes to ensure distributed locking for cron jobs.
