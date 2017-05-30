// Import required java libraries
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

// Extend HttpServlet class
public class ManifestServlet extends HttpServlet 
{
	private static final String _filesCacheDir = "/var/tmp/playswithCache";
	private static final String _catalogCacheFile = "manifestCatalog.json";
	private static final String _mobileAssetContentCacheFile = "mobileAssetContentPath.content";
	private static final String _mobileWorldContentCacheFile = "mobileWorldContentPaths.content";
	private static String mobileAssetContentPathURL = null;
	private static String mobileWorldContentPathsURL = null;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		ServletContext context = getServletContext();
		String[] path = request.getRequestURI().split("/");
		if(path.length < 3)
		{	response.setStatus(HttpServletResponse.SC_BAD_REQUEST); return; }
		String action = path[2];
		String file = path[3];
		switch(action)
		{
			case "update":
				switch(file)
				{
					case "catalog":
						updateManifestCatalog(context);
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					case "mobileAssetContent":
						updateManifestCatalog(context);
						updateManifestCacheFile("catalog", _mobileAssetContentCacheFile, "https://www.bungie.net" + mobileAssetContentPathURL, context);
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					case "mobileWorldContent":
						updateManifestCatalog(context);
						updateManifestCacheFile("catalog", _mobileWorldContentCacheFile, "https://www.bungie.net" + mobileWorldContentPathsURL, context);
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
				}
				break;
			case "fetch":
				if(path.length < 4)
				{	response.setStatus(HttpServletResponse.SC_BAD_REQUEST); return; }
				String type = path[4];
				String databasePath;
				switch(file)
				{
					case "mobileAssetContent": databasePath = Paths.get(_filesCacheDir,_mobileAssetContentCacheFile).toString(); break;
					case "mobileWorldContent": databasePath = Paths.get(_filesCacheDir,_mobileWorldContentCacheFile).toString(); break;
					default:
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						return;
				}
				String val = null;
				if(path.length > 5)
				{
					String id = path[5];
					if(id != null && id.matches("-?\\d+"))
					{
						val = ManifestCache.getValue(databasePath, type, Long.parseLong(id));
					} else {
						val = ManifestCache.getValue(databasePath, type, id);
					}
				} else {
					val = ManifestCache.getValue(databasePath, type);
				}
				if(val == null)
				{
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					response.setStatus(HttpServletResponse.SC_OK);
					response.setContentType("application/json");
					response.setCharacterEncoding("utf-8");
					PrintWriter out = response.getWriter();
					out.write(val);
					out.close();
				}
				return;
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	private void updateManifestCatalog(ServletContext context)
		throws IOException
	{
		boolean loadCatalog = false;
		if(updateManifestCacheFile("catalog", _catalogCacheFile, DestinyAPI.manifestCatalogURL, context))
		{
			mobileAssetContentPathURL = null;
			mobileWorldContentPathsURL = null;
			loadCatalog = true;
		}
		if(loadCatalog)
		{
			try
				{
				byte[] jsonData = Files.readAllBytes(Paths.get(_filesCacheDir,_catalogCacheFile));
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode rootNode = objectMapper.readTree(jsonData);
				JsonNode responseNode = rootNode.get("Response");
				if(mobileAssetContentPathURL == null)
				{
				
					if(responseNode != null)
					{
						// get mobile assest content URL
						JsonNode mobileAssetContentPathNode = responseNode.get("mobileAssetContentPath");
						if(mobileAssetContentPathNode != null)
						{	mobileAssetContentPathURL = mobileAssetContentPathNode.textValue(); }
					}
				}
				if(mobileWorldContentPathsURL == null)
				{
					if(responseNode != null)
					{
						// get mobile assest content URL
						JsonNode mobileWorldContentPathsNode = responseNode.get("mobileWorldContentPaths");
						if(mobileWorldContentPathsNode != null)
						{
							JsonNode enNode = mobileWorldContentPathsNode.get("en");
							if(enNode != null)
							{	mobileWorldContentPathsURL = enNode.textValue(); }
						}
					}
				}
			} catch(Exception e) {
				context.log("Unable to parse manifest catalog", e);
				IOException ioe = new IOException("Unable to parse manifest catalog");
				ioe.initCause(e);
				throw ioe;
			}
		}
	}
	
	private boolean updateManifestCacheFile(String name, String localCache, String url, ServletContext context)
		throws IOException
	{
		File downloadtmp = File.createTempFile(name, ".part");
		File tmplocation = new File(_filesCacheDir);
		if(!tmplocation.exists())
		{	tmplocation.mkdir(); }
		File cacheFile = new File(tmplocation, localCache);
		long lastUpdatedCache = -1;
		if(cacheFile.exists())
		{
			lastUpdatedCache = cacheFile.lastModified();
			long twentyFourHoursAgo = System.currentTimeMillis() - (24L * 36000L);
			if(lastUpdatedCache > twentyFourHoursAgo)
			{
				context.log("Skipping updating " + name + " catalog");
				return false;
			}
		}
		URL cacheURL = new URL(url);
		HttpURLConnection cacheConnection = (HttpURLConnection) cacheURL.openConnection();
		if(lastUpdatedCache > 0L)
		{	cacheConnection.setIfModifiedSince(lastUpdatedCache); }
		cacheConnection.connect();
		switch(cacheConnection.getResponseCode())
		{
			case HttpURLConnection.HTTP_OK:
				InputStream is = cacheConnection.getInputStream();
				if("application/octet-stream".equals(cacheConnection.getContentType()))
				{
					ZipInputStream zis = new ZipInputStream(is);
					zis.getNextEntry();
					is = zis;
				}
				FileOutputStream os = new FileOutputStream(cacheFile, false);
				byte[] buff = new byte[1024];
				for(int br = is.read(buff); br > 0; br = is.read(buff))
				{	os.write(buff, 0, br); }
				os.close();
				is.close();
				context.log("manifest " + name + " updated");
				break;
			case HttpURLConnection.HTTP_NOT_MODIFIED:
				context.log("manifest " + name + " not modified");
				break;
			default:
				context.log("error updating " + name + ", got return code " + cacheConnection.getResponseCode() +" with message " + cacheConnection.getResponseMessage());
		}
		cacheConnection.disconnect();
		return true;
	}
}