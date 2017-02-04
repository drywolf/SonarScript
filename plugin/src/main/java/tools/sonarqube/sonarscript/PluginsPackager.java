package tools.sonarqube.sonarscript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class PluginsPackager
{
	private List<String> fileList;
	private String source_folder;
	
	public PluginsPackager()
	{
	   fileList = new ArrayList<String>();
	}

	/*public static void main(String[] args)
	{
	   ZipUtils appZip = new ZipUtils();
	   appZip.generateFileList(new File(SOURCE_FOLDER));
	   appZip.zipIt(OUTPUT_ZIP_FILE);
	}*/

	public void zipIt(String source_folder, OutputStream output_stream)
	{
		zipIt(source_folder, output_stream);
	}
	
	public void zipIt(String source_folder, OutputStream output_stream, boolean use_base_folder)
	{
		String source = "";
		try
		{
			source = source_folder.substring(source_folder.lastIndexOf("\\") + 1, source_folder.length());
		}
		catch (Exception e)
		{
			source = source_folder;
		}

		zipIt(source_folder, output_stream, use_base_folder ? source : null);
	}
	
	public void zipIt(String source_folder, OutputStream output_stream, String target_base_folder)
	{
		this.source_folder = source_folder;
		
		generateFileList(new File(source_folder));
		
	   byte[] buffer = new byte[1024];

	   //FileOutputStream fos = null;
	   ZipOutputStream zos = null;
	   try
	   {
	     //fos = new FileOutputStream(zipFile);
	     zos = new ZipOutputStream(output_stream);

	     FileInputStream in = null;

	     for (String file : this.fileList)
	     {
	        String entry_name = target_base_folder != null ? (target_base_folder + File.separator + file) : file;
	        
	        ZipEntry ze = new ZipEntry(entry_name);
	        zos.putNextEntry(ze);
	        try
	        {
	           in = new FileInputStream(source_folder + File.separator + file);
	           int len;
	           while ((len = in.read(buffer)) > 0)
	           {
	              zos.write(buffer, 0, len);
	           }
	        }
		  	  catch (Exception ex)
		  	  {
		  	     ex.printStackTrace();
		  	  }
	        finally
	        {
	           in.close();
	        }
	     }

	     zos.closeEntry();
	  }
	  catch (IOException ex)
	  {
	     ex.printStackTrace();
	  }
	  finally
	  {
	     try
	     {
	        zos.close();
	     }
	     catch (IOException e)
	     {
	        e.printStackTrace();
	     }
	  }
	}

    public void unZipIt(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];
       	
        try{
       		
       	//create output directory is not exists
       	File folder = new File(outputFolder);
       	if(!folder.exists()){
       		folder.mkdir();
       	}
       		
       	//get the zip file content
       	ZipInputStream zis = 
       		new ZipInputStream(new FileInputStream(zipFile));
       	//get the zipped file list entry
       	ZipEntry ze = zis.getNextEntry();
       		
       	while(ze!=null){
       			
       	   String fileName = ze.getName();
              File newFile = new File(outputFolder + File.separator + fileName);
                                      
               //create all non exists folders
               //else you will hit FileNotFoundException for compressed folder
               new File(newFile.getParent()).mkdirs();
                 
               FileOutputStream fos = new FileOutputStream(newFile);             

               int len;
               while ((len = zis.read(buffer)) > 0) {
          		fos.write(buffer, 0, len);
               }
           		
               fos.close();   
               ze = zis.getNextEntry();
       	}
       	
           zis.closeEntry();
       	zis.close();
       		       		
       }catch(IOException ex){
          ex.printStackTrace(); 
       }
      }    
	
	public void generateFileList(File node)
	{

	  // add file only
	  if (node.isFile())
	  {
	     fileList.add(generateZipEntry(node.toString()));
	  }

	  if (node.isDirectory())
	  {
	     String[] subNote = node.list();
	     for (String filename : subNote)
	     {
	        generateFileList(new File(node, filename));
	     }
	  }
	}

	private String generateZipEntry(String file)
	{
	   return file.substring(source_folder.length() + 1, file.length());
	}
}
