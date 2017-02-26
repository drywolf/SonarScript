package tools.sonarqube.sonarscript;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Locker;
import io.js.J2V8Classes.V8JavaClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarPlugin;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

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
				throw new RuntimeException(e);
			}
		}

		// run sync
		//this.run();
	}

	public void run() {

		synchronized (this)
		{
			ClassLoader cl = SonarScriptPlugin.class.getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);

			// TODO: those are just experimental extensions, remove once no longer needed
			//_extensions.add(SonarMetricsExtensionProxy.class);
			_extensions.add(SonarSensorExtensionProxy.class);

			if (_njsrt == null) {

				LOG.info("create njs");
				_njsrt = NodeJS.createNodeJS();

				//V8JavaClasses.initMainClassPaths();
				V8JavaClasses.ClassAliases.put("org.sonar.batch.deprecated.DeprecatedSensorContext", "org.sonar.api.batch.SensorContext");

				V8 v8 = _njsrt.getRuntime();
				LOG.info("before inject classes");
				V8JavaClasses.injectClassHelper(v8, "extendedNJS");

//				while(_njsrt.isRunning()) {
//					_njsrt.handleMessage();
//				}

				LOG.info("after inject classes");

				try {
					// TODO: put any JS runtime init here if needed

					//_engine.eval("load('" + ScriptRootDir() + "/extensions/plugins/sonarscript/jvm-npm.js');");

					// apply custom script root directory to JS engine context
					//_engine.eval("require.root = '" + ScriptRootDir() + "';");
					
					//_engine.eval("print('DEBUG ----> : ' + Java.type('tools.sonarqube.sonarscript.SonarScriptPlugin').ScriptRootDir());");
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					e.printStackTrace(System.out);

					LOG.error("NJS INIT error: " + e.toString());
				}
			}

			LOG.info("before plugins");

			InitJsPlugins();

			LOG.info("after plugins");

			for (Object i : _extensions)
				LOG.info("java extension: " + i);

			LOG.info("before notify");

			notifyAll();

			LOG.info("after notify");

			while(_njsrt.isRunning()) {
				_njsrt.handleMessage();
			}

			LOG.info("after NJS loop");

			//_njsrt.release();

			_njsrt.getRuntime().getLocker().release();

			LOG.info("fully done");
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
		NodeJS njs = SonarScriptPlugin.getNJS();
		
		String plugin_name = plugin_dir.getName();
		
		LOG.info("SonarScript: handle plugin: " + plugin_name);
		
		try
		{
			// TODO: is there a better way than this ?
			//engine.eval("var curr_plug_module = require(\'./extensions/plugins/sonarscript/plugins/" + plugin_name + "\');");
			//JSObject curr_plug_module = (JSObject)engine.get("curr_plug_module");

			// TODO: call some other more appropriate init function
			//invoke.invokeMethod(curr_plug_module, "init");

			File pluginScript = new File(_script_root_dir,"extensions/plugins/sonarscript/plugins/" + plugin_name + "/index.js");
			njs.exec(pluginScript);

			LOG.info("done with plugin " + plugin_name);

		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("PLUGIN RT INIT error: " + e.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOG.error("PLUGIN INIT error: " + e.toString());
		}
	}
	
	public List<Class> getExtensions() {
		LOG.info("Returning extensions: " + _extensions.size());

		for (Class c : _extensions)
			LOG.info("> " + c.getCanonicalName());

		return _extensions;
	}
	
	public static List<Class> getExt() {
		LOG.info("Returning extensions for JS: " + _extensions.size());
		return _extensions;		
	}

	public static void registerExtension(Class extension) {
		_extensions.add(extension);
	}

//	public static ScriptEngine getEngine() {
//		return _engine;
//	}

	public static NodeJS getNJS() {
		return _njsrt;
	}
		
	public static String ScriptRootDir() {
		return _script_root_dir;
	}

//	public static Invocable getInvocable() {
//		return (Invocable) _engine;
//	}

	private static NodeJS _njsrt;

	private static ArrayList<Class> _extensions = new ArrayList<Class>();
	
	private static String _script_root_dir;
}
