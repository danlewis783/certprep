## 1. Architectural & Language Requirements
* **Java 11 Standard:**
* **Code Structure:**
* **Fail-Fast Design:** The program must validate all command-line arguments and physical disk assets (images/CSV) before launching any graphical interface.

## 2. Data & Directory Structure
* **`data/` directory:** * Contains question images: `chNN-qMM.png` (zero-padded).
    * Contains answer images: `chNN-qMM-answer.png` or `chNN-qMM-ans.png`.
    * **Master Key:** `master-answer-key.csv` (Columns: `Chapter, Question, Answer, Possible`).
* **`sessions/` directory:**
    * Stores results in `session-yyyyMMdd-###.csv` format. 
    * **Serial Logic:** Program must increment the serial number (001, 002...) for each new session on the same day rather than appending to existing files.
    * **CSV Columns:** `Chapter, Question, Answer, Completed, Elapsed Time, Correct Yes/No, Reviewed`.

## 3. Command-Line Interface
* `--chapter <#>`: Filters test to a specific chapter.
* `--start <#>` / `--end <#>`: Defines a specific question range.
* `--review-session <filename>`: Triggers **Untimed Review Mode** for the specified CSV.
* `--data <path>` / `--session <path>`: Overrides default directory locations.
* `help` / `--help` / `-h`: Displays a formatted usage menu and exits.

## 4. General UI (Dark Mode & Whiteboard)
* **Visual Theme:** Strict Dark Mode (Black backgrounds, White text, Dark Gray buttons). No focus rings/dotted outlines on components.
* **Full Screen:** Application must launch in undecorated, true full-screen mode (hiding the taskbar).
* **Whiteboard:**
    * Fixed-size panel (400px wide) docked to the right; no scrolling.
    * **Persistent:** Contents remain when the whiteboard is closed or when advancing questions.
    * **Tools:** Freehand drawing, "T" button (Text tool—requires a click for every new text block), and "Delete" (Wipes the entire canvas).

## 5. Triple Pacing Indicators
1.  **Question Pacing Bar:** 108-second countdown. (Green: 0-64s, Yellow: 65-100s, Red: 101s+).
2.  **Test Pacing Bar:** Total session time (Questions * 108s). **Static Color:** Stays Blue (no color shifts).
3.  **Completion Pace Bar:** Tracks Questions Answered vs. Total Questions.
    * **Green:** Completion % > Time %.
    * **Yellow:** Within a 1-question time buffer (Even with pace).
    * **Red:** Completion % < Time % (Behind pace).

## 6. Test Mode Flow
* **Continuous Flow:** Upon clicking "Answer," the result is logged, and the next question loads immediately.
* **No Review:** There is no pause or "Correct/Incorrect" feedback during the test.
* **Conclusion:** Prints score and total time to the standard console before exiting. No pop-up windows.

## 7. Untimed Review Mode
* **Comprehensive Review:** Loads **all** questions from a session file for navigation.
* **Split-Screen Layout:** Question image on the left, Answer image on the right.
* **Status Header:** Displays the user's recorded answer, "CORRECT/INCORRECT" status (color-coded), and time spent in `MM:SS`.
* **Navigation:** Includes `<< Previous` and `Next >>` buttons.
* **Reviewed Toggle:** A checkbox that, when toggled, immediately updates the `Reviewed` column (true/false) in the session CSV.

## 8. Image Scaling (Fit to Screen)
* **Scalable Labels:** Both Test and Review modes support image scaling.
* **Toggleable Mode:** Toggling "Fit" dynamically scales the image to the pane while preserving aspect ratio. 
* **Scrollbars:** Re-enabled only when "Fit" mode is off and the image exceeds pane dimensions.
