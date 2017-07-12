package tools.sonarqube.sonarscript;

import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import io.js.J2V8Classes.V8JavaClasses;
import org.sonar.api.SonarPlugin;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

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

	private static final Logger LOG = Loggers.get(SonarScriptPlugin.class);

	private static final String PLUGIN_HOST_SONAR_WEB_SERVER = "org.sonar.server.app.WebServer";
	private static final String PLUGIN_HOST_SONAR_CE_SERVER = "org.sonar.ce.app.CeServer";
	private static final String PLUGIN_HOST_SONAR_RUNNER = "org.sonarsource.scanner.cli.Main";

	public SonarScriptPlugin() {

		final String plugin_host = System.getProperty("sun.java.command");
		LOG.debug("SonarScript: host: " + plugin_host);

		if (plugin_host.startsWith(PLUGIN_HOST_SONAR_WEB_SERVER))
		{
			LOG.debug("SonarScript: mode: http-server");

			// reset script-root to current working directory
			_script_root_dir = System.getProperty("user.dir");
			LOG.debug("Server Script-Root-Dir: " + _script_root_dir);

			// TODO: handle shutdown gracefully & manage server life-cycle in some way
			HttpContentServer server = new HttpContentServer();
		}
		else if(plugin_host.startsWith(PLUGIN_HOST_SONAR_CE_SERVER))
		{
			LOG.debug("SonarScript: mode: compute-server");

			_script_root_dir = System.getProperty("user.dir");
			LOG.debug("Server Script-Root-Dir: " + _script_root_dir);
		}
		else if (plugin_host.startsWith(PLUGIN_HOST_SONAR_RUNNER))
		{
			LOG.debug("SonarScript: mode: http-client");
			
			String jar_dir2 = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			String parent_dir = new File(jar_dir2).getParent();

			_script_root_dir = parent_dir;
			LOG.debug("Client Script-Root-Dir: " + _script_root_dir);

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
		else
			throw new RuntimeException("Unhandled host-type: " + plugin_host);

		synchronized (this) {
			(new Thread(this)).start();
			try {
				this.wait();
			} catch (InterruptedException e) {
				// handle it somehow
				throw new RuntimeException(e);
			}
		}
	}

	public void run() {

		synchronized (this)
		{
			ClassLoader cl = SonarScriptPlugin.class.getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);

			if (_njsrt == null) {

				LOG.debug("create njs");
				_njsrt = NodeJS.createNodeJS();

				//V8JavaClasses.initMainClassPaths();
				V8JavaClasses.ClassAliases.put("org.sonar.batch.deprecated.DeprecatedSensorContext", "org.sonar.api.batch.SensorContext");
				V8JavaClasses.ClassAliases.put("org.sonar.batch.scan.filesystem.DefaultModuleFileSystem", "org.sonar.api.batch.fs.FileSystem");
				V8JavaClasses.ClassAliases.put("com.google.common.collect.Maps$Values", "java.util.AbstractCollection");
				//V8JavaClasses.ClassAliases.put("org.sonar.batch.deprecated.DeprecatedSensorContext", "org.sonar.api.batch.sensor.SensorContext");

				V8 v8 = _njsrt.getRuntime();
				LOG.debug("before inject classes");
				V8JavaClasses.injectClassHelper(v8, "extendedNJS");

				LOG.debug("after inject classes");
			}

			LOG.debug("before plugins");

			InitJsPlugins();

			LOG.debug("after plugins");

			LOG.debug("before notify");

			notifyAll();

			LOG.debug("after notify");

			while(_njsrt.isRunning()) {
				_njsrt.handleMessage();
			}

			LOG.debug("after NJS loop");

            for (Object i : _extensions)
                LOG.debug("java extension: " + i);

			//_njsrt.release();

			_njsrt.getRuntime().getLocker().release();

			LOG.debug("fully done");
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
		
		LOG.debug("SonarScript: handle plugin: " + plugin_name);
		
		try
		{
			File pluginScript = new File(_script_root_dir,"extensions/plugins/sonarscript/plugins/" + plugin_name + "/index.js");
			njs.exec(pluginScript);

			LOG.debug("done with plugin " + plugin_name);
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
		return _extensions;
	}

	public static void registerExtension(Class extension) {
		LOG.debug("Registering JS extension: " + extension);
		_extensions.add(extension);
	}

	public static NodeJS getNJS() {
		return _njsrt;
	}
		
	public static String ScriptRootDir() {
		return _script_root_dir;
	}

	private static NodeJS _njsrt;

	private static ArrayList<Class> _extensions = new ArrayList<Class>();
	
	private static String _script_root_dir;
}
