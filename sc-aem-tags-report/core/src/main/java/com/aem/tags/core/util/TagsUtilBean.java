package com.aem.tags.core.util;

//--------------------------------------
/**
* Tag Util Bean
* @author Singaiah Chintalapudi
*/
//--------------------------------------
public class TagsUtilBean
{

  private String  tagPath;
  private String  contentPath;
  private String  tagReportPath;
  private boolean includeChildTags;
  private String  reportName;
  
  
  public String getTagPath()
  {
    return tagPath;
  }
  public void setTagPath(String tagPath)
  {
    this.tagPath = tagPath;
  }
  
  
  public String getContentPath()
  {
    return contentPath;
  }
  public void setContentPath(String contentPath)
  {
    this.contentPath = contentPath;
  }
  
  
  public String getTagReportPath()
  {
    return tagReportPath;
  }
  public void setTagReportPath(String tagReportPath)
  {
    this.tagReportPath = tagReportPath;
  }
  
  
  public boolean isIncludeChildTags()
  {
    return includeChildTags;
  }
  public void setIncludeChildTags(boolean includeChildTags)
  {
    this.includeChildTags = includeChildTags;
  }
  
  
  public String getReportName()
  {
    return reportName;
  }
  public void setReportName(String reportName)
  {
    this.reportName = reportName;
  }
  
} //TagsUtilBean
