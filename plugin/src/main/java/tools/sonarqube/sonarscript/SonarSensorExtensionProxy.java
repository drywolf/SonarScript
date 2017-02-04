package tools.sonarqube.sonarscript;

import java.util.Random;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;

@SuppressWarnings("rawtypes")
public class SonarSensorExtensionProxy implements Sensor {

	public SonarSensorExtensionProxy(FileSystem fileSystem) {
        SonarSensorExtensionProxy._fs = fileSystem;
    }
	
	public boolean shouldExecuteOnProject(Project project) {
		// this extension is always executed on all kinds of projects
		return true;
	}
	
	public void analyse(Project project, SensorContext sensorContext) {
		
		/*for (Metric m :  SonarMetricsExtensionProxy.GetMetrics()) {
			Measure mes = new Measure(m, _rnd.nextDouble() * 100d, 2);
			sensorContext.saveMeasure(mes);
		}*/
	}
	
	private static Random _rnd = new Random();

	public String toString() {
        return "SonarSensorExtensionProxy";
    }
	
	public static FileSystem getFs()
	{
		return _fs;
	}
	
	private static FileSystem _fs;
}
