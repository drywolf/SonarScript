
var ArrayList = ClassHelpers.getClass("java.util.ArrayList");

var Metrics = ClassHelpers.getClass("org.sonar.api.measures.Metrics");
var Sensor = ClassHelpers.getClass("org.sonar.api.batch.Sensor");

var Metric = ClassHelpers.getClass("org.sonar.api.measures.Metric");
Metric.Builder = ClassHelpers.getClass("org.sonar.api.measures.Metric$Builder");
Metric.ValueType = ClassHelpers.getClass("org.sonar.api.measures.Metric$ValueType");

var CoreMetrics = ClassHelpers.getClass("org.sonar.api.measures.CoreMetrics");
var SonarScriptPlugin = ClassHelpers.getClass("tools.sonarqube.sonarscript.SonarScriptPlugin");

var SensorContext = ClassHelpers.getClass("org.sonar.api.batch.SensorContext");
var Measure = ClassHelpers.getClass("org.sonar.api.measures.Measure");

var metrics = new ArrayList();

var QualityMetric = new Metric.Builder(
       'proj-struct-id',
       'Project Structure Quality',
       Metric.ValueType.PERCENT)
       .setDescription('Just some description for the JS metric')
       .setQualitative(true)
       .setDomain(CoreMetrics.DOMAIN_GENERAL)
       .create();

metrics.add(QualityMetric);

var IntMetric = new Metric.Builder(
       'abs-metric-test',
       'Absolute Int Value Metric',
       Metric.ValueType.INT)
       .setDescription('Just some description for the JS metric')
       .setQualitative(true)
       .setDomain(CoreMetrics.DOMAIN_GENERAL)
       .create();

metrics.add(IntMetric);

var MetricsExt = Metrics.$extend(
{
    __name__: 'MetricsExt',
    // TODO: support init override / check if JS init for JAVA objects works fully
//    __init__: function()
//    {
//        print("new MEEETRICS", this.__javaInstance);
//    },

    getMetrics: function()
    {
        '@Override';
        return metrics;
    },
});

var SensorExt = Sensor.$extend(
{
    __name__: 'SensorExt',
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

    analyse: function(project, sensorContext)
    {
        '@Override';

        var metricVal = Math.random() * 100;
        var measure = new Measure(QualityMetric, metricVal);
        sensorContext.saveMeasure(measure);

        // TODO: improve function matching algorithm (take into account type coercion)
        // TODO: support "type" comments in JS to enforce a specific overload to be called
        // TODO: throw Java exceptions back into Node.JS at the position where the error started from in NJS code
        metricVal = parseInt(Math.random() * 10000) + 0.1;
        var intMeasure = new Measure(IntMetric, metricVal/*int*/, 0);
        sensorContext.saveMeasure(intMeasure);
    },
});

print("BEFORE plugin init");
SonarScriptPlugin.registerExtension(MetricsExt.__class);
SonarScriptPlugin.registerExtension(SensorExt.__class);
print("AFTER plugin init");
