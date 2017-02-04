
module.exports = {};
module.exports.getMetrics = function getMetrics()
{
	return [
		{ id: 'someid1', name: 'somename1', valtype: 'sometype1', description: 'some description1' },
		{ id: 'someid2', name: 'somename2', valtype: 'sometype2', description: 'some description2' }
	];
};

module.exports.getMetrics = function getMetrics()
{
	var Metrics = Java.type("org.sonar.api.measures.Metrics");
	var Metric = Java.type("org.sonar.api.measures.Metric");
	var CoreMetrics = Java.type("org.sonar.api.measures.CoreMetrics");
	var SonarScriptPlugin = Java.type("tools.sonarqube.sonarscript.SonarScriptPlugin");
	var ArrayList = Java.type("java.util.ArrayList");
	var Sensor = Java.type("org.sonar.api.batch.Sensor");
	var SensorContext = Java.type("org.sonar.api.batch.SensorContext");
	var Measure = Java.type("org.sonar.api.measures.Measure");
		
	print('----------------> ' + SonarScriptPlugin.ScriptRootDir());
	
	var metrics = new ArrayList();
	
	var metric = new Metric.Builder(
			'new-metric-id',
            'Another Awesome JS Metric',
            Metric.ValueType.PERCENT)
            .setDescription('Just some description for the JS metric')
            .setQualitative(true)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();
	
	metrics.add(metric);
	
	var MyMetrics = Java.extend(Metrics, {
	    getMetrics: function() {
			return metrics;
	    }
	});
	
	var MySensor = Java.extend(Sensor, {
		
		shouldExecuteOnProject: function(project) {
			// always execute
			return true;
		},
		
		analyse: function(project, sensorContext) {
			var measure = new Measure(metric, 50);
			sensorContext.saveMeasure(measure);
		}
	});
	
	SonarScriptPlugin.getExt().add(MyMetrics.class);
	SonarScriptPlugin.getExt().add(MySensor.class);
		
	print("pure JS INIT DONE");
	
	return [
			{ id: 'project-structure-quality', name: 'Project Structure Quality', valtype: 'sometype1', description: 'Are there inconsistencies with how you envision your project structure ?!?' },
			{ id: 'some-other-metric', name: 'Some other metric', valtype: 'sometype2', description: 'some description2' },
			{ id: 'foo-bar', name: 'Foo-Bar', valtype: 'sometype2', description: 'hello world!' }
		];
};
