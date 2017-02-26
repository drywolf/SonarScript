/*
var ArrayList = ClassHelpers.getClass("java.util.ArrayList");

var Metrics = ClassHelpers.getClass("org.sonar.api.measures.Metrics");
var Sensor = ClassHelpers.getClass("org.sonar.api.batch.Sensor");

var Metric = ClassHelpers.getClass("org.sonar.api.measures.Metric");
Metric.Builder = ClassHelpers.getClass("org.sonar.api.measures.Metric$Builder");
Metric.ValueType = ClassHelpers.getClass("org.sonar.api.measures.Metric$ValueType");

var NewIssue = ClassHelpers.getClass("org.sonar.api.batch.sensor.issue.NewIssue");
var NewIssueLocation = ClassHelpers.getClass("org.sonar.api.batch.sensor.issue.NewIssueLocation");

var RuleKey = ClassHelpers.getClass("org.sonar.api.rule.RuleKey");

var CoreMetrics = ClassHelpers.getClass("org.sonar.api.measures.CoreMetrics");
var SonarScriptPlugin = ClassHelpers.getClass("tools.sonarqube.sonarscript.SonarScriptPlugin");

var SensorContext = ClassHelpers.getClass("org.sonar.api.batch.SensorContext");
var Measure = ClassHelpers.getClass("org.sonar.api.measures.Measure");

var NewRepository = ClassHelpers.getClass("org.sonar.api.server.rule.NewRepository");

var Severity = ClassHelpers.getClass("org.sonar.api.rule.Severity");
var RuleStatus = ClassHelpers.getClass("org.sonar.api.rule.RuleStatus");

var RulesDefinition = ClassHelpers.getClass("org.sonar.api.server.rule.RulesDefinition");

var REPO_NAME = "Sonar-Script-Example-Repo";
var LANGUAGE_KEY = "bat";
var RULE_KEY = "sonar-script-example-issue";

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

    shouldExecuteOnProject: function(project)
    {
        '@Override';
        // always execute
        return true;
    },

    analyse: function(project, ctx)
    {
        '@Override';

        var newIssue =
                ctx
                .newIssue()
                .forRule(RuleKey.of(REPO_NAME, RULE_KEY));

//        NewIssueLocation newIssueLocation =
//                newIssue
//                .newLocation()
//                .on(inputFile)
//                .message(issue.getFailure())
//                .at(inputFile.selectLine(issue.getStartPosition().getLine() + 1));

        //newIssue.at(newIssueLocation);
        newIssue.save();
    },
});

print("BEFORE plugin init");
SonarScriptPlugin.registerExtension(RulesDefinitionExt.__class);
SonarScriptPlugin.registerExtension(IssueSensorExt.__class);
print("AFTER plugin init");
*/
