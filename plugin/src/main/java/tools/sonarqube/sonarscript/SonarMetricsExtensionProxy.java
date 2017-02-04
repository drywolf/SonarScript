package tools.sonarqube.sonarscript;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

@SuppressWarnings("rawtypes")
public class SonarMetricsExtensionProxy implements Metrics {
	
	/**
     * Default constructor.
     */
    public SonarMetricsExtensionProxy() {
        super();
    }
    
	/**
	 * SonarQube Plugin API: getMetrics
	 */
	public List<Metric> getMetrics() {
		
		return _js_metrics;
	}
			
	public static List<Metric> GetMetrics(){
		return _js_metrics;
	}
	
	private static ArrayList<Metric> _js_metrics = new ArrayList<Metric>();
}
