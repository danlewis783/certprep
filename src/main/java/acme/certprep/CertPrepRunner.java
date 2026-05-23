package acme.certprep;

import acme.certprep.ui.CertPrepUi;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

@NullMarked
public final class CertPrepRunner {
    private static final Logger logger = LoggerFactory.getLogger(CertPrepRunner.class);

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";

    private final ArgParser parser;
    private final InteractiveConfigCli interactiveConfigCli;
    private final GradeReporter gradeReporter;
    private final ReviewAssetValidator reviewAssetValidator;
    private final UiLauncher uiLauncher;
    private final PrintStream out;
    private final PrintStream err;

    public CertPrepRunner() {
        this(
                new ArgParser(),
                new InteractiveConfigCli(),
                new GradeReporter(),
                new ReviewAssetValidator(),
                new SwingUiLauncher(),
                System.out,
                System.err
        );
    }

    CertPrepRunner(
            ArgParser parser,
            InteractiveConfigCli interactiveConfigCli,
            GradeReporter gradeReporter,
            ReviewAssetValidator reviewAssetValidator,
            UiLauncher uiLauncher,
            PrintStream out,
            PrintStream err
    ) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.interactiveConfigCli = Objects.requireNonNull(interactiveConfigCli, "interactiveConfigCli");
        this.gradeReporter = Objects.requireNonNull(gradeReporter, "gradeReporter");
        this.reviewAssetValidator = Objects.requireNonNull(reviewAssetValidator, "reviewAssetValidator");
        this.uiLauncher = Objects.requireNonNull(uiLauncher, "uiLauncher");
        this.out = Objects.requireNonNull(out, "out");
        this.err = Objects.requireNonNull(err, "err");
    }

    public int run(String[] args) {
        ParseResult parseResult = parser.parse(args);
        try {
            switch (parseResult.getStatus()) {
                case HELP:
                    printHelp();
                    return 0;
                case FAILURE:
                    err.println("Argument Error: " + parseResult.getMessage());
                    err.println("Run 'java acme.certprep.CertPrep help' for usage details.");
                    return 1;
                case INTERACTIVE:
                    return runConfig(interactiveConfigCli.prompt());
                case SUCCESS:
                    return runConfig(parseResult.getConfig());
                default:
                    err.println("Unknown parser result: " + parseResult.getStatus());
                    return 1;
            }
        } catch (InteractiveConfigException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            logger.warn("CertPrep failed", e);
            e.printStackTrace(err);
            return 1;
        }
    }

    private int runConfig(Config config) throws Exception {
        if (config instanceof GradeConfig) {
            out.print(gradeReporter.report((GradeConfig) config));
            return 0;
        } else if (config instanceof ReviewConfig) {
            ReviewConfig reviewConfig = (ReviewConfig) config;
            Session session = new SessionRepository(reviewConfig.getSessionDir()).existing(reviewConfig.getSessionFile());
            List<SessionRow> allRows = session.loadRows();
            if (allRows.isEmpty()) {
                out.println("The session file appears to be empty.");
                return 0;
            }
            List<String> missingAssets = reviewAssetValidator.missingAssets(reviewConfig, allRows);
            if (!missingAssets.isEmpty()) {
                err.println(RED + "FATAL: Missing Assets" + RESET);
                for (String missingAsset : missingAssets) {
                    err.println(" - " + missingAsset);
                }
                return 1;
            }
            uiLauncher.showReview(reviewConfig, session, allRows);
            return 0;
        } else if (config instanceof ExamConfig) {
            ExamConfig examConfig = (ExamConfig) config;
            List<QuestionInfo> questions = QuestionBank.load(examConfig);
            Session session = new SessionRepository(examConfig.getSessionDir()).createNew();
            uiLauncher.showExam(examConfig, questions, session);
            return 0;
        }
        throw new IllegalArgumentException("Unsupported config type: " + config.getClass().getName());
    }

    private void printHelp() {
        out.println("Usage: java acme.certprep.CertPrep [options]");
        out.println("Options:");
        out.println("  --exam                Start a new practice exam (requires --chapter, --start, --end)");
        out.println("  --chapter <#>         Chapter for exam");
        out.println("  --start <#>           First question number");
        out.println("  --end <#>             Last question number");
        out.println("  --review <f>          Navigate/toggle review for a session CSV");
        out.println("  --grade <f>           Output score report for a session CSV");
        out.println("  --data-dir <path>     Path to images (default: '~/.certprep/data')");
        out.println("  --session-dir <path>  Path to sessions (default: '~/.certprep/sessions')");
    }

    interface UiLauncher {
        void showReview(ReviewConfig config, Session session, List<SessionRow> rows);

        void showExam(ExamConfig config, List<QuestionInfo> questions, Session session);
    }

    private static final class SwingUiLauncher implements UiLauncher {

        @Override
        public void showReview(ReviewConfig config, Session session, List<SessionRow> rows) {
            CertPrepUi.showReview(config, session, rows);
        }

        @Override
        public void showExam(ExamConfig config, List<QuestionInfo> questions, Session session) {
            CertPrepUi.showExam(config, questions, session);
        }
    }
}
