package tools.sonarqube.sonarscript;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarPlugin;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

import jdk.nashorn.api.scripting.JSObject;

@SuppressWarnings({"restriction", "rawtypes"})
public class SonarScriptPlugin extends SonarPlugin implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(SonarScriptPlugin.class);

	private static final String PLUGIN_HOST_SONAR_SERVER = "org.sonar.server.app.WebServer";
	private static final String PLUGIN_HOST_SONAR_RUNNER = "org.sonar.runner.Main";
	
	public SonarScriptPlugin() {
				
		String plugin_host = System.getProperty("sun.java.command");
		LOG.info("SonarScript: host: " + plugin_host);
		
        String cwd = System.getProperty("user.dir");
        LOG.info("SonarScript: cwd: " + cwd);
        
        _script_root_dir = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().replace('\\', '/');
        
		if (plugin_host.startsWith(PLUGIN_HOST_SONAR_SERVER))
		{
			LOG.info("SonarScript: mode: http-server");
	        
			// TODO: handle shutdown gracefully & manage server life-cycle in some way
			HttpContentServer server = new HttpContentServer();
			
			// reset script-root to current working directory
			_script_root_dir = System.getProperty("user.dir").replace('\\', '/');
		}
		else if (plugin_host.startsWith(PLUGIN_HOST_SONAR_RUNNER))
		{
			LOG.info("SonarScript: mode: http-client");
			
			String jar_dir2 = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			String parent_dir = new File(jar_dir2).getParent();
			
			LOG.info(parent_dir);
			LOG.info("VS");
			LOG.info(_script_root_dir);
			
			try {
				URL url = new URL("http://localhost:9099");
				URLConnection conn = url.openConnection();
				InputStream is = conn.getInputStream();
								
				String filePath = "plugin-content.zip";
				File zip_file = new File(_script_root_dir, filePath);
				FileOutputStream fos = new FileOutputStream(zip_file);
				int inByte;
				while((inByte = is.read()) != -1)
				     fos.write(inByte);
				is.close();
				fos.close();
				
				PluginsPackager pp = new PluginsPackager();
				pp.unZipIt(zip_file.getPath(), parent_dir);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Process the data from the input stream.
			//get.releaseConnection();
		}		
		
        LOG.info("SonarScript: script-root-dir: " + _script_root_dir);
		
		synchronized (this) {
			(new Thread(this)).start();
			try {
				this.wait();
			} catch (InterruptedException e) {
				// handle it somehow
			}
		}
	}
	
	@Override
	public void run() {
		
		synchronized (this)
		{
			ClassLoader cl = SonarScriptPlugin.class.getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);

			// TODO: those are just experimental extensions, remove once no longer needed
			//_extensions.add(SonarMetricsExtensionProxy.class);
			_extensions.add(SonarSensorExtensionProxy.class);
			
			_manager = new ScriptEngineManager(null);

			if (_engine == null) {
				
				_engine = _manager.getEngineByName("nashorn");

				try {			
					_engine.eval("load('" + ScriptRootDir() + "/extensions/plugins/sonarscript/jvm-npm.js');");

					// apply custom script root directory to JS engine context
					_engine.eval("require.root = '" + ScriptRootDir() + "';");
					
					_engine.eval("print('DEBUG ----> : ' + Java.type('tools.sonarqube.sonarscript.SonarScriptPlugin').ScriptRootDir());");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					e.printStackTrace(System.out);
				}
			}
			
			InitJsPlugins();
			
			for (Object i : _extensions)
				LOG.info("java extension: " + i);
			
			notifyAll();
		}
	}

	private void InitJsPlugins() {
		File plugins_root = new File(SonarScriptPlugin.ScriptRootDir() + "/extensions/plugins/sonarscript/plugins/");

		File[] plugin_dirs = plugins_root.listFiles();
		
		if (plugin_dirs != null)
			for (File plug_dir : plugin_dirs)
			{
				if (plug_dir.isDirectory())
			    {
					initPluginDir(plug_dir);
			    }
			}
	}
	
	private void initPluginDir(File plugin_dir)
	{
		ScriptEngine engine = SonarScriptPlugin.getEngine();
		Invocable invoke = SonarScriptPlugin.getInvocable();
		
		String plugin_name = plugin_dir.getName();
		
		LOG.info("SonarScript: handle plugin: " + plugin_name);
		
		try
		{
			// TODO: is there a better way than this ?
			engine.eval("var curr_plug_module = require(\'./extensions/plugins/sonarscript/plugins/" + plugin_name + "\');");
			JSObject curr_plug_module = (JSObject)engine.get("curr_plug_module");

			// TODO: call some other more appropriate init function
			invoke.invokeMethod(curr_plug_module, "init");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Class> getExtensions() {
		LOG.info("Returning extensions: " + _extensions.size());
		return _extensions;
	}
	
	public static List<Class> getExt() {
		return _extensions;		
	}

	public static ScriptEngine getEngine() {
		return _engine;
	}
		
	public static String ScriptRootDir() {
		return _script_root_dir;
	}

	public static Invocable getInvocable() {
		return (Invocable) _engine;
	}

	private ScriptEngineManager _manager;
	private static ScriptEngine _engine;
	
	private static ArrayList<Class> _extensions = new ArrayList<Class>();
	
	private static String _script_root_dir;
}
