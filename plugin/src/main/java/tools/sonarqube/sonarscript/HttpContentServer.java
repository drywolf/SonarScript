package tools.sonarqube.sonarscript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class HttpContentServer {

    private static final Logger LOG = LoggerFactory.getLogger(SonarScriptPlugin.class);

	public HttpContentServer() {
		
		try {
			_server = HttpServer.create(new InetSocketAddress(9099), 0);
	        //Create the context for the server.
			_server.createContext("/", new RootHandler());
			_server.setExecutor(null); // creates a default executor
			_server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.printStackTrace(System.out);
		}
	}
	
	static class RootHandler implements HttpHandler {
        //@Override
        public void handle(HttpExchange t) throws IOException {
            //String response = "This is the response";            
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            String cwd = System.getProperty("user.dir");
            LOG.info("SonarScript: http-server-cwd: " + cwd);
            
            PluginsPackager pp = new PluginsPackager();
            pp.zipIt(cwd + "/extensions/plugins/sonarscript", baos, "extensions/plugins/sonarscript");
            
            byte[] body = baos.toByteArray();
            baos.close();

            LOG.info("HTTP response length: " + body.length);
            
            t.getResponseHeaders().set("Content-Type", "application/zip");
            t.sendResponseHeaders(200, body.length);
            OutputStream os = t.getResponseBody();
            os.write(body);
            os.close();
        }
    }
	
	private HttpServer _server;
}
