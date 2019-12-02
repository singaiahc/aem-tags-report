package com.aem.tags.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.aem.tags.core.processors.TagsUtilProcessor;

//-----------------------------------------------------------------------
/**
* General Tag Utility Servlet
* @author Singaiah Chintalapudi
*/
//-----------------------------------------------------------------------
@Component(service=Servlet.class,configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TagUtilityServlet extends SlingSafeMethodsServlet
{


  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException
  {
    PrintWriter pw = response.getWriter();
    try 
    {
      TagsUtilProcessor processor = new TagsUtilProcessor();
      processor.initialize(request, response, pw);
      processor.process();
      
     return;
    }
    catch(Exception e) 
    {
      pw.print("Error Occured");
    }
    
    return;
    
  } //doGet
  
  

} //TagUtilityServlet
