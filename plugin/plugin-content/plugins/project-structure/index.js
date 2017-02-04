//var System = Java.type("java.lang.System");
var ArrayList = Java.type("java.util.ArrayList");

var Metrics = Java.type("org.sonar.api.measures.Metrics");
var Metric = Java.type("org.sonar.api.measures.Metric");
var CoreMetrics = Java.type("org.sonar.api.measures.CoreMetrics");
var SonarScriptPlugin = Java.type("tools.sonarqube.sonarscript.SonarScriptPlugin");
var Sensor = Java.type("org.sonar.api.batch.Sensor");
var SensorContext = Java.type("org.sonar.api.batch.SensorContext");
var Measure = Java.type("org.sonar.api.measures.Measure");

var SonarSensorExtensionProxy = Java.type("tools.sonarqube.sonarscript.SonarSensorExtensionProxy");

module.exports = {};

var QualityMetric;
function initMetrics()
{
	var metrics = new ArrayList();
	
	QualityMetric = new Metric.Builder(
			'proj-struct-id',
            'Project Structure Quality',
            Metric.ValueType.PERCENT)
            .setDescription('Just some description for the JS metric')
            .setQualitative(true)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();
	
	metrics.add(QualityMetric);
	
	var MetricsExt = Java.extend(Metrics, 
	{
	    getMetrics: function()
		{
			return metrics;
	    }
	});
	
	return MetricsExt;
}

function initSensors()
{
	var SensorExt = Java.extend(Sensor, 
	{
		SensorExt: function(file_system)
		{
			var _fs = file_system;
			print("----------------> JS PLUGIN STUFF: " + _fs.baseDir().getPath());
		},
		
		shouldExecuteOnProject: function(project)
		{
			// always execute
			return true;
		},
		
		analyse: function(project, sensorContext)
		{
			var _fs = SonarSensorExtensionProxy.getFs();
			print("222----------------> JS PLUGIN STUFF: " + _fs.baseDir().getPath());
			
			var metricVal = Math.random() * 100;
			var measure = new Measure(QualityMetric, metricVal);
			sensorContext.saveMeasure(measure);
		}
	});
	
	return SensorExt;
}

module.exports.init = function()
{
	var MetricsExt = initMetrics();
	var SensorExt = initSensors();
	
	SonarScriptPlugin.getExt().add(MetricsExt.class);
	SonarScriptPlugin.getExt().add(SensorExt.class);
}
