# JT/T 808 Simulator Context

This context describes the domain language for observing and controlling simulated JT/T 808 trip execution.

## Language

**Trip Task**:
A running or previously started simulated vehicle trip.
_Avoid_: Batch task, terminal task

**Task Group**:
A runtime grouping produced by one trip task creation action.
_Avoid_: Batch ID, batch session, manual group, tag

**Task Group Display Name**:
A human-readable label that identifies the creation action behind a task group.
_Avoid_: Raw ID

**Task Group Source**:
The kind of creation action that produced a task group.
_Avoid_: Batch type, batch mode

**Completed Task Group**:
A task group whose trip tasks have all reached a terminal state.
_Avoid_: Current run

**Runtime Overview**:
An aggregate view of all trip tasks and task groups currently known by the simulator process.
_Avoid_: Batch summary, task list summary

**Runtime Summary**:
The current aggregate metrics for all trip tasks known by the simulator process.
_Avoid_: Task group summary

**Creation Result**:
The immediate result returned after a trip task creation action is accepted or rejected.
_Avoid_: Runtime summary, task group summary

**Task Group Summary**:
The current aggregate runtime status for one task group.
_Avoid_: Creation result

**Task Group Launch Status**:
The startup-side status for the creation action that produced a task group.
_Avoid_: Runtime summary

**Task Group Current Status**:
The current runtime-side status for the trip tasks inside one task group.
_Avoid_: Creation result

**Task Group List**:
A runtime overview list where each row represents one task group and its aggregate status.
_Avoid_: Trip task list

**Trip Task List**:
The canonical list for inspecting individual trip tasks.
_Avoid_: Task group detail list

## Relationships

- A **Task Group** contains one or more **Trip Tasks**.
- A **Task Group** has a stable identifier and a **Task Group Display Name**.
- A **Task Group Source** is either single-task creation or batch creation in the current product.
- A single-task creation action produces exactly one **Task Group** with one **Trip Task**.
- A batch creation action produces exactly one **Task Group** with many **Trip Tasks**.
- A **Task Group** is not manually assembled from existing **Trip Tasks**.
- A **Task Group** remains visible in the current simulator process after its **Trip Tasks** finish, until the process restarts.
- A **Task Group** is both an observation boundary and a stop-control boundary for its **Trip Tasks**.
- A **Runtime Overview** aggregates all **Task Groups** and all **Trip Tasks** currently known by the simulator process.
- A **Runtime Summary** is separate from any one **Task Group** and represents the simulator process as a whole.
- A **Runtime Summary** includes all **Trip Tasks** known by the current process, including completed trip tasks.
- A **Runtime Overview** presents a **Runtime Summary** and a **Task Group List**.
- A **Creation Result** is not a **Task Group Summary**; it only reports whether the creation action was accepted and which task group was produced.
- A **Task Group Summary** can change after the **Creation Result** is returned.
- After a successful **Creation Result**, the user observes the resulting **Task Group** from the **Runtime Overview**, not from the creation entry point.
- After either single-task creation or batch creation succeeds, the UI takes the user to the **Runtime Overview** focused on the produced **Task Group**.
- A **Task Group List** row can be expanded or opened to inspect group-level progress and stop results for that **Task Group**.
- Expanded task group details distinguish **Task Group Launch Status** from **Task Group Current Status**.
- A **Trip Task List** is the only place that lists individual **Trip Tasks**; it can be filtered by **Task Group**.
- A **Trip Task** exposes its **Task Group** so users can tell which creation action produced it.

## Example Dialogue

> **Dev:** "When the user creates one trip task, does it belong to a task group?"
> **Domain expert:** "Yes. Every creation action produces a task group, even when the group contains only one trip task."
>
> **Dev:** "After every trip task in a task group stops, should the task group disappear or require clearing?"
> **Domain expert:** "No. It should remain visible as a completed task group in the current process, but it is not restored after restart."
>
> **Dev:** "If a batch creation starts too many trip tasks, how should the user stop only those tasks?"
> **Domain expert:** "Stop the task group produced by that creation action; do not require stopping every trip task manually."
>
> **Dev:** "Should the runtime summary drop terminated trip tasks?"
> **Domain expert:** "No. It summarizes every trip task known by the current process; restarting the process resets that memory."
>
> **Dev:** "Where should users inspect task groups?"
> **Domain expert:** "In the runtime overview: the summary shows the whole process, and the task group list shows each creation action."
>
> **Dev:** "Is the response from clicking batch creation the same data as the task group's later summary?"
> **Domain expert:** "No. The creation result tells whether the creation action was accepted and which task group was created; the task group summary tells how that group is running afterward."
>
> **Dev:** "Where should the UI take the user after a successful creation action?"
> **Domain expert:** "To the runtime overview, focused on the task group produced by that creation action."
>
> **Dev:** "Does single-task creation follow the same navigation rule as batch creation?"
> **Domain expert:** "Yes. Both creation paths produce a task group and then focus that task group in the runtime overview."
>
> **Dev:** "What should an expanded task group show?"
> **Domain expert:** "Separate the launch status from the current status: startup counts and ramp-up belong to launch status, while active, parking, terminated, and stop results belong to current status."
>
> **Dev:** "Should task group details list every trip task?"
> **Domain expert:** "No. Task group details show group-level progress; individual trip tasks are inspected in the trip task list filtered by task group."
>
> **Dev:** "How does a user inspect the tasks created by one task group?"
> **Domain expert:** "Open the trip task list with a task-group filter; each row can also show which task group produced the trip task."
>
> **Dev:** "Should users identify task groups by raw IDs?"
> **Domain expert:** "No. APIs use stable identifiers, while the UI shows a task group display name that describes the creation action."
>
> **Dev:** "What sources can produce task groups today?"
> **Domain expert:** "Single-task creation and batch creation. Other sources can be added later, but they are not part of the current language."

## Flagged Ambiguities

- "batchId" was used for observing batch-created tasks, but the resolved term is **Task Group** because single-task creation also needs the same observation boundary.
- "task group" means the boundary created by a creation action; it does not mean a manually managed fleet, label, or saved collection.
- Completed task group data is historical within the current process; it must not be presented as the current active run.
- "summary" must distinguish **Runtime Summary** from task-group-level progress or result metrics.
- Task group details must distinguish launch-side data from current runtime data, even when they are shown together in one expanded row.
