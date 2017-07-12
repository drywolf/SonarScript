
var ArrayList = ClassHelpers.getClass("java.util.ArrayList");

var Metrics = ClassHelpers.getClass("org.sonar.api.measures.Metrics");
var Sensor = ClassHelpers.getClass("org.sonar.api.batch.sensor.Sensor");

var Metric = ClassHelpers.getClass("org.sonar.api.measures.Metric");
Metric.Builder = ClassHelpers.getClass("org.sonar.api.measures.Metric$Builder");
Metric.ValueType = ClassHelpers.getClass("org.sonar.api.measures.Metric$ValueType");

var NewIssue = ClassHelpers.getClass("org.sonar.api.batch.sensor.issue.NewIssue");
var NewIssueLocation = ClassHelpers.getClass("org.sonar.api.batch.sensor.issue.NewIssueLocation");

var RuleKey = ClassHelpers.getClass("org.sonar.api.rule.RuleKey");

var CoreMetrics = ClassHelpers.getClass("org.sonar.api.measures.CoreMetrics");
var SonarScriptPlugin = ClassHelpers.getClass("tools.sonarqube.sonarscript.SonarScriptPlugin");

var SensorContext = ClassHelpers.getClass("org.sonar.api.batch.sensor.SensorContext");
var Measure = ClassHelpers.getClass("org.sonar.api.measures.Measure");

var NewRepository = ClassHelpers.getClass("org.sonar.api.server.rule.NewRepository");

var Severity = ClassHelpers.getClass("org.sonar.api.rule.Severity");
var RuleStatus = ClassHelpers.getClass("org.sonar.api.rule.RuleStatus");

var RulesDefinition = ClassHelpers.getClass("org.sonar.api.server.rule.RulesDefinition");

var REPO_NAME = "Sonar-Script-Example-Repo";
var LANGUAGE_KEY = "js";
var RULE_KEY = "sonar-script-example-issue";

var ProfileDefinition = ClassHelpers.getClass("org.sonar.api.profiles.ProfileDefinition");
var RulesProfile = ClassHelpers.getClass("org.sonar.api.profiles.RulesProfile");
var Rule = ClassHelpers.getClass("org.sonar.api.rules.Rule");
//var ValidationMessages = ClassHelpers.getClass("org.sonar.api.utils.ValidationMessages");

var DefaultInputDir = ClassHelpers.getClass("org.sonar.api.batch.fs.internal.DefaultInputDir");

var RuleProfileExt = ProfileDefinition.$extend(
{
    __name__: 'RuleProfileExt',

    createProfile: function(validation)
    {
        '@Override';

        var profile = RulesProfile.create("TsLint", LANGUAGE_KEY);
        profile.activateRule(Rule.create(REPO_NAME, RULE_KEY), null);
        return profile;
    }
});

var RulesDefinitionExt = RulesDefinition.$extend(
{
    __name__: 'RulesDefinitionExt',

    define: function(context) {
        '@Override';

        var repository =
                context
                .createRepository(REPO_NAME, LANGUAGE_KEY)
                .setName("SonarScript Basic-Issue Analyzer");

        var sonarRule = repository
            .createRule(RULE_KEY)
            .setName("Example Issue Name")
            .setSeverity(Severity.MAJOR)
            .setHtmlDescription("<b><i>bold & italic HTML description test</i></b>")
            .setStatus(RuleStatus.READY);

        repository.done();
    },
});

var IssueSensorExt = Sensor.$extend(
{
    __name__: 'IssueSensorExt',
    // TODO: support init override / check if JS init for JAVA objects works fully
//    __init__: function()
//    {
//        print("new SENSEO", this.__javaInstance);
//    },

    describe: function(desc)
    {
        '@Override';

        desc
            .name("Linting sensor for SonarScript example")
            .onlyOnLanguage(LANGUAGE_KEY);
    },

    execute: function(ctx)
    {
        '@Override';

        var files = ctx.fileSystem().inputFiles();
        var list = new ArrayList(files);

        var inputFile = list.get(0);

        var fileIssue =
                ctx
                .newIssue()
                .forRule(RuleKey.of(REPO_NAME, RULE_KEY));

        var fileIssueLocation =
                fileIssue
                .newLocation()
                .on(inputFile)
                .message("File issue message " + new Date())
                .at(inputFile.selectLine(3));

        fileIssue.at(fileIssueLocation);
        fileIssue.save();

        var dirIssue =
                ctx
                .newIssue()
                .forRule(RuleKey.of(REPO_NAME, RULE_KEY));

        var dirIssueLocation =
                dirIssue
                .newLocation()
                .on(new DefaultInputDir("my:project", "./"))
                .message("Directory issue message " + new Date());

        dirIssue.at(dirIssueLocation);
        dirIssue.save();
    },
});

SonarScriptPlugin.registerExtension(RuleProfileExt.__class);
SonarScriptPlugin.registerExtension(RulesDefinitionExt.__class);
SonarScriptPlugin.registerExtension(IssueSensorExt.__class);
