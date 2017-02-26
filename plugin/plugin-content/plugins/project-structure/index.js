
//var System = Java.type("java.lang.System");
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

var SonarSensorExtensionProxy = ClassHelpers.getClass("tools.sonarqube.sonarscript.SonarSensorExtensionProxy");

module.exports = {};

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

        // print("BEFORE create metrics");
        // var metrics = new ArrayList();

        // QualityMetric = new Metric.Builder(
        //         'proj-struct-id',
        //         'Project Structure Quality',
        //         Metric.ValueType.PERCENT)
        //         .setDescription('Just some description for the JS metric')
        //         .setQualitative(true)
        //         .setDomain(CoreMetrics.DOMAIN_GENERAL)
        //         .create();

        // metrics.add(QualityMetric);

        // print("BEFORE return metrics");

        // print("KEYS ", Object.keys(metrics));

        return metrics;
    },
});

var QualityMetric;
function initMetrics()
{
//	var metrics = new ArrayList();
//
//	QualityMetric = new Metric.Builder(
//			'proj-struct-id',
//            'Project Structure Quality',
//            Metric.ValueType.PERCENT)
//            .setDescription('Just some description for the JS metric')
//            .setQualitative(true)
//            .setDomain(CoreMetrics.DOMAIN_GENERAL)
//            .create();
//
//	metrics.add(QualityMetric);

	return MetricsExt;

//	try
//	{
//		var MetricsExt = Metrics.$extend(
//    	{
//            __name__: 'MetricsExt',
//
//    	    getMetrics: function()
//    	    {
//    	        '@Override';
//    	        return null;
//    	    },
//    	});
//
//    	print("BIG SUCCESS MetricsExt");
//
//    	return MetricsExt;
//	}
//	catch (e)
//	{
//	    print("DEBUG", e, typeof e);
//	}
}

function initSensors()
{
//    try
//    {
        var SensorExt = Sensor.$extend(
        {
            __name__: 'SensorExt',
            // TODO: support init override / check if JS init for JAVA objects works fully
//            __init__: function()
//            {
//                print("new SENSEO", this.__javaInstance);
//            },

            shouldExecuteOnProject: function(project)
            {
                '@Override';
                //'@Implements';

                print("HAPPY shouldExecuteOnProject");
                // always execute
                return true;
            },

            analyse: function(project, sensorContext)
            {
                '@Override';
                '@Implements';

                print("HAPPY analyse");

                //var _fs = SonarSensorExtensionProxy.getFs();
                //print("222----------------> JS PLUGIN STUFF: " + _fs.baseDir().getPath());

                var metricVal = Math.random() * 100;
                //var metricVal = 50.1;
                print("new measure!!!");
                var measure = new Measure(QualityMetric, metricVal);
                print("save measure!!!")
                sensorContext.saveMeasure(measure);

                // TODO: improve function matching algorithm (take into account type coercion)
                // TODO: support "type" comments in JS to enforce a specific overload to be called
                // TODO: throw Java exceptions back into Node.JS at the position where the error started from in NJS code
                metricVal = parseInt(Math.random() * 10000) + 0.1;
                var intMeasure = new Measure(IntMetric, metricVal/*int*/, 0);
                sensorContext.saveMeasure(intMeasure);

                print("HAPPY analyse DONE");
            },
        });

    	print("BIG SUCCESS SensorExt");

        return SensorExt;
//    }
//    catch (e)
//    {
//        print("DEBUG2", e, typeof e);
//    }
}

module.exports.init = function()
{
	var MetricsExt = initMetrics();
	var SensorExt = initSensors();

    var dbg = function(a, x)
    {
        print("DBG " + a);
        print("__javaClass = ", x.__javaClass);
        print("runtimeName = ", x.runtimeName);
        print("__jsMethods = ", x.__jsMethods);
        print("$name = ", x.$name);
        print("__javaSuperclass = ", x.__javaSuperclass);
    }

	print("MetricsExt.keys = ", Object.keys(MetricsExt));
	print("SensorExt.keys = ", Object.keys(SensorExt));

	print("MetricsExt.__class = ", Object.keys(MetricsExt.__class));
	print("SensorExt.__class = ", Object.keys(SensorExt.__class));

	MetricsExt.__class = 1;
	SensorExt.__class = 1;

	dbg("MetricsExt", MetricsExt);
	dbg("SensorExt", SensorExt);

	//var x = new Metrics();
	//var y = new Sensor();

    //var u = new MetricsExt();
    //var v = new SensorExt();

	//print("KEYS Metrics", Object.keys(x));
	//print("KEYS Sensor", Object.keys(y));

	//print("KEYS MetricsExt", Object.keys(u));
	//print("KEYS SensorExt", Object.keys(v));

	//print("TEST", u.getMetrics());
	//print("TEST[0]", u.getMetrics().get(0));

	//SonarScriptPlugin.getExt().add(MetricsExt.__class);
	//SonarScriptPlugin.getExt().add(SensorExt.__class);

	SonarScriptPlugin.registerExtension(MetricsExt.__class);
	SonarScriptPlugin.registerExtension(SensorExt.__class);
}

print("BEFORE plugin init");
module.exports.init();
print("AFTER plugin init");
