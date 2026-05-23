# CertPrep

CertPrep is a Java certification study tool for exams such as the
[*Oracle Certified Professional: Java SE 11 Developer* exam (`1Z0-819`)](https://education.oracle.com/oracle-certified-professional-java-se-11-developer/trackp_OCPJAV11).

CertPrep simulates parts of the Oracle Java certification exam experience, including the built-in whiteboard. It also adds pacing indicators and saves session results for later review.

CertPrep is licensed under the GPL v3. See the LICENSE file for details.

## Build & Language Requirements

Built with Gradle's Kotlin DSL and targeting Java 11.

## Data & Directory Structure

### Data directory

By default, CertPrep reads question assets and the master answer key from `~/.certprep/data`. This path can be overridden with `--data-dir`.

Required files:

* Question images use the format `chNN-qMM.png`, where chapter and question numbers are zero-padded.
    * Example: `ch01-q03.png`
* Answer images use one of these formats:
    * `chNN-qMM-answer.png`
    * `chNN-qMM-ans.png`
* The master answer key is stored in `master-answer-key.csv`.

`master-answer-key.csv` columns: `Chapter,Question,Answer,Possible`

### Session directory

By default, CertPrep stores exam session results in `~/.certprep/sessions`. This path can be overridden with `--session-dir`.

Session files use this naming format:

* `session-yyyyMMdd-###.csv`
    * Example: `session-20260522-001.csv`

For each new session created on the same day, the program creates a new file by incrementing the serial number:

* `session-20260522-001.csv`
* `session-20260522-002.csv`
* `session-20260522-003.csv`

New sessions are not appended to existing session files.

Session CSV columns: `Chapter,Question,Answer,Completed,Elapsed Time,Correct Yes/No,Reviewed`

## Command-Line Interface

### Interactive Mode
If no arguments are provided, the application launches an interactive CLI. It uses `java.io.Console` when available and falls back to standard input when necessary.

* Prompts for mode selection (Exam, Review, Grade).
* If **Exam Mode** is selected, lists available chapters from the data directory and allows selection by number (displaying available question ranges for each), then prompts for Start Question #, and End Question #.
* If **Review** or **Grade Mode** is selected, lists available `.csv` session files from the session directory and allows selection by number.

### Non-Interactive Mode

* `--exam`: Runs **Timed Exam Mode**. Requires `--chapter`, `--start`, and `--end`.
* `--chapter <#>`: Selects a specific chapter.
* `--start <#>` / `--end <#>`: Defines the inclusive question range.
* `--review <filename>`: Runs **Untimed Review Mode** for the specified session CSV.
* `--grade <filename>`: Prints a score report for the specified session CSV.
* `--data-dir <path>` / `--session-dir <path>`: Overrides default directory locations.
* `help` / `--help` / `-h`: Displays a formatted usage menu and exits.

## Operational Modes
The application operates in three mutually exclusive modes:
* **Exam Mode (`--exam`)**: Take a timed practice exam.
* **Review Mode (`--review`)**: Review a previous session's answers.
* **Grade Mode (`--grade`)**: Generate a performance report for a session.

One of these modes must be selected, either through command-line arguments or the interactive prompt.

## User Interface

* **Dark Theme:** The application uses a dark Swing theme with black backgrounds, white text, and dark gray buttons.
* **Exam Mode Full Screen:** Exam Mode launches in undecorated, true full-screen mode, hiding the taskbar.
* **Review Mode Window:** Review Mode launches as a maximized desktop window.
* **Exam Mode Whiteboard:**
    * Fixed-size panel (400px wide) docked to the right; no scrolling.
    * **Persistence:** Contents remain when the whiteboard is closed or when advancing questions.
    * **Tools:** Freehand drawing, a "T" text tool that requires a click for each new text block, and a "Delete" button that wipes the entire canvas.

## Triple Pacing Indicators
1. **Question Pacing Bar:** 108-second per-question timer based on elapsed time. Green: 0-64s, Yellow: 65-100s, Red: 101s+.
2. **Exam Pacing Bar:** Total session time, calculated as `questions × 108 seconds`. **Static Color:** Stays blue with no color shifts.
3. **Completion Pace Bar:** Compares completion percentage against elapsed-time percentage.
    * **Green:** Ahead of pace.
    * **Yellow:** Within a one-question buffer.
    * **Red:** Behind pace.

## Exam Mode Flow
* **Continuous Flow:** Upon clicking "Answer," the result is logged, and the next question loads immediately.
* **No Review:** There is no pause or "Correct/Incorrect" feedback during the test.
* **Conclusion:** After the final answer is logged, the application exits. Use Grade Mode to generate a score report for the session.

## Untimed Review Mode
* **Comprehensive Review:** Loads **all** questions from a session file for navigation.
* **Split-Screen Layout:** Question image on the left, Answer image on the right.
* **Status Header:** Displays `[OK]` or `[FAIL]`, chapter/question number, the user's recorded answer, and time spent as `M:SS`.
* **Navigation:** Includes `<< Prev` and `Next >>` buttons.
* **Reviewed Toggle:** A checkbox that, when toggled, immediately updates the `Reviewed` column (true/false) in the session CSV.

## Image Scaling (Fit to Screen)
* **Scalable Labels:** Both Exam and Review modes support image scaling.
* **Toggleable Mode:** Toggling "Fit" dynamically scales the image to the pane while preserving aspect ratio.
* **Scrollbars:** Re-enabled only when "Fit" mode is off and the image exceeds pane dimensions.
