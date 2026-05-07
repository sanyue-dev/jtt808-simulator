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

**Task Group Monitor**:
A live view centered on all task groups currently known by the simulator process.
_Avoid_: Runtime overview, batch summary, task list summary

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
A live monitor list where each row represents one task group and its aggregate status.
_Avoid_: Trip task list

**Monitor Polling**:
Periodic in-page refresh for task group monitor data, using a one-second interval in the first version.
_Avoid_: Manual refresh, first-version WebSocket

**Task Group Monitor Snapshot**:
One polling response containing the runtime summary and all task group summaries.
_Avoid_: Trip task export, task detail payload

**Trip Task List**:
The canonical list for inspecting individual trip tasks.
_Avoid_: Task group detail list

**Global Stop Action**:
A stop command that targets every non-terminated trip task known by the current simulator process.
_Avoid_: Runtime summary, task group stop

## Relationships

- A **Task Group** contains one or more **Trip Tasks**.
- A **Task Group** has a stable identifier and a **Task Group Display Name**.
- A **Task Group Source** is either single-task creation or batch creation in the current product.
- A single-task creation action produces exactly one **Task Group** with one **Trip Task**.
- A batch creation action produces exactly one **Task Group** with many **Trip Tasks**.
- A **Task Group** is not manually assembled from existing **Trip Tasks**.
- A **Task Group** remains visible in the current simulator process after its **Trip Tasks** finish, until the process restarts.
- A **Task Group** is both an observation boundary and a stop-control boundary for its **Trip Tasks**.
- A **Task Group Monitor** centers on all **Task Groups** currently known by the simulator process.
- A **Runtime Summary** is separate from any one **Task Group** and represents the simulator process as a whole.
- A **Runtime Summary** includes all **Trip Tasks** known by the current process, including completed trip tasks.
- A **Runtime Summary** includes task counts, protocol-stage counts, reporting throughput, failure counts, runtime resource usage, and scheduler delay.
- A **Runtime Summary** is displayed as grouped process-level metrics: task status, protocol runtime, resource usage, and scheduler health.
- A **Task Group Monitor** presents a **Runtime Summary** and a **Task Group List**.
- A **Task Group Monitor** is the only UI page that presents the **Runtime Summary**.
- A **Task Group Monitor** refreshes through **Monitor Polling** without a full page reload.
- A **Task Group List** shows all task groups known by the current process, is not paginated, and is rendered in full in the first version.
- A **Task Group Monitor Snapshot** contains one **Runtime Summary** and the complete **Task Group List**.
- A **Task Group Monitor Snapshot** does not contain individual **Trip Task** rows.
- **Monitor Polling** preserves expanded **Task Group List** rows while refreshing their displayed metrics.
- A **Creation Result** is not a **Task Group Summary**; it only reports whether the creation action was accepted and which task group was produced.
- A **Task Group Summary** can change after the **Creation Result** is returned.
- After a successful **Creation Result**, the user observes the resulting **Task Group** from the **Task Group Monitor**, not from the creation entry point.
- After either single-task creation or batch creation succeeds, the UI takes the user to the **Task Group Monitor** focused on the produced **Task Group**.
- A **Task Group List** row can be expanded or opened to inspect group-level progress and stop results for that **Task Group**.
- Expanded task group details distinguish **Task Group Launch Status** from **Task Group Current Status**.
- A **Trip Task List** is the only place that lists individual **Trip Tasks**; it can be filtered by **Task Group**.
- A **Trip Task List** does not present the **Runtime Summary**.
- A **Trip Task List** may expose a **Global Stop Action** as an emergency control, but it remains separate from the **Runtime Summary**.
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
> **Domain expert:** "In the task group monitor: the summary gives process-level context, and the task group list shows each creation action."
>
> **Dev:** "Should the trip task list also show the runtime summary?"
> **Domain expert:** "No. The runtime summary belongs to the task group monitor; the trip task list is for filtering and inspecting individual trip tasks."
>
> **Dev:** "Should the task group monitor runtime summary be less detailed than the old trip task list summary?"
> **Domain expert:** "No. It must carry the complete process-level metrics, including resource usage and scheduler delay."
>
> **Dev:** "Should all runtime summary metrics appear as one flat block?"
> **Domain expert:** "No. Keep one runtime summary, but group it into task status, protocol runtime, resource usage, and scheduler health."
>
> **Dev:** "Is the response from clicking batch creation the same data as the task group's later summary?"
> **Domain expert:** "No. The creation result tells whether the creation action was accepted and which task group was created; the task group summary tells how that group is running afterward."
>
> **Dev:** "Where should the UI take the user after a successful creation action?"
> **Domain expert:** "To the task group monitor, focused on the task group produced by that creation action."
>
> **Dev:** "Does single-task creation follow the same navigation rule as batch creation?"
> **Domain expert:** "Yes. Both creation paths produce a task group and then focus that task group in the task group monitor."
>
> **Dev:** "Should users refresh the task group monitor or page through task groups?"
> **Domain expert:** "No. The monitor updates in place and shows all task groups known by the current process without pagination or lazy loading in the first version."
>
> **Dev:** "How does the task group monitor stay current?"
> **Domain expert:** "It uses one-second monitor polling in the page; the first version does not require WebSocket or Server-Sent Events."
>
> **Dev:** "Should polling fetch the summary and task group list separately?"
> **Domain expert:** "No. Fetch one task group monitor snapshot containing the runtime summary and all task group summaries, but not individual trip task rows."
>
> **Dev:** "What happens if a user has expanded a task group while polling refreshes?"
> **Domain expert:** "Keep that row expanded and update its metrics in place, as long as the task group is still present."
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
> **Dev:** "If the trip task list no longer shows the runtime summary, should it lose the stop-all button too?"
> **Domain expert:** "No. It may keep a global stop action as an emergency control, but that does not make the trip task list the process-level monitoring page."
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
- **Runtime Summary** must not be duplicated on the **Trip Task List**; otherwise users have two competing places for process-level truth.
- Task group details must distinguish launch-side data from current runtime data, even when they are shown together in one expanded row.
- "runtime overview" was too generic; the resolved product term is **Task Group Monitor** because the page is centered on task groups.

## UI Direction

**Global Design Baseline**:
The shared visual and interaction foundation used by all simulator pages.
_Avoid_: Per-page skin, one-off redesign

**Ramp-up Window**:
A startup pacing unit that controls how many trip tasks are submitted per interval during task group creation.
_Avoid_: Batch, batch size

## UI Relationships

- UI redesign starts by establishing the **Global Design Baseline** before rebuilding individual business pages.
- The **Global Design Baseline** applies the project `DESIGN.md` direction to shared page layout, typography, colors, buttons, inputs, tables, cards, and page headers.
- The **Global Design Baseline** adapts IBM Carbon-like engineering rules to a simulator control console; it must not copy enterprise marketing-page composition.
- The UI uses IBM Blue as the primary action color, white and light-gray surfaces, charcoal text, thin borders, and flat low-radius controls.
- Hero-scale marketing typography and promotional page layouts do not belong on simulator operation pages.
- The **Global Design Baseline** keeps the existing FreeMarker, jQuery, Bootstrap, Leaflet, and Font Awesome stack.
- The **Global Design Baseline** does not introduce a new frontend framework or a broad interaction rewrite.
- The first **Global Design Baseline** implementation may prefer IBM Plex Sans in the CSS font stack, but must not add external font loading or vendored font assets.
- The first implementation slice for the **Global Design Baseline** may update shared stylesheet rules that affect all pages.
- Shared CSS is the home for the **Global Design Baseline**; page-specific CSS should only refine concrete page layouts after the baseline exists.
- Establishing the **Global Design Baseline** must not change business APIs, polling behavior, task stop behavior, pagination behavior, or route/map domain behavior.
- Map pages only receive the natural effect of shared styles in the first **Global Design Baseline** implementation; map-specific layout and interaction polish belongs to a later page-level slice.
- Page-level UI redesign follows the product priority of task group observation and trip task control before route editing or map-specific presentation.
- The first UI redesign batch is split into three slices: establish the **Global Design Baseline**, refine the **Task Group Monitor**, then refine the **Trip Task List** and trip task creation pages.
- Route editing and map-specific visual polish are intentionally deferred until the core stress-test observation and control workflow has a stable baseline.
- User-facing Chinese UI uses Chinese domain labels such as "任务组" and "行程任务"; English canonical terms such as **Task Group** are for documentation and engineering discussion, not explanatory text on operation pages.
- Route-editing UI follows the same control baseline as task pages; event category choices on trouble segments use the shared custom dropdown pattern instead of native select controls.
- Task group list rows identify task groups by **Task Group Display Name** by default; the stable task group identifier may appear only in expanded details or diagnostic copy fields.
- UI labels for startup pacing use **Ramp-up Window** language; do not call ramp-up windows "batches" because task grouping is already represented by **Task Group**.
