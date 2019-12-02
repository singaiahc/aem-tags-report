package com.aem.tags.core.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.jcr.RepositoryException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.tags.core.util.TagsUtilBean;
import com.day.cq.commons.RangeIterator;
import com.day.cq.dam.api.AssetManager;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.google.common.base.Throwables;

//------------------------------------------
/**
* Tags Util Processor
* 
* @author Singaiah Chintalapudi
*/
//-------------------------------------------
public class TagsUtilProcessor
{

  
  
  //------------------------------------------------------- Private data members
  private SlingHttpServletRequest m_req;
  private ResourceResolver        m_resolver;
  private PrintWriter             m_pw;
  private TagsUtilBean            m_tB;
  private TagManager              m_tm;
  private String[]                columnNames = {"Tag Title", "Tag ID", "URL", "Published?", "In-Flight Changes?"};
  private final Logger            logger = LoggerFactory.getLogger(getClass());
  
  public void initialize(SlingHttpServletRequest request, SlingHttpServletResponse response, PrintWriter pw) throws Exception 
  {
    m_req = request;
    m_resolver = m_req.getResourceResolver();
    if(m_resolver == null) throw new Exception("ResourceResolver is null"); //TODO: Use your custom exception handling framework
    m_tm = m_resolver.adaptTo(TagManager.class);
    if (m_tm == null) throw new Exception("TagManager is null"); //TODO: Use your custom exception handling framework
    m_pw  = pw;
    m_tB  = new TagsUtilBean();
    
  } //initialize
  
  public void process() 
  {
    processTagsRequest();
    
  } //process
  
  
  private void processTagsRequest() 
  {
    
    try 
    {
      m_tB.setTagPath(m_req.getParameter("tagsPath").trim());
      m_tB.setIncludeChildTags(Boolean.parseBoolean(m_req.getParameter("excludeSubTags").trim()));
      m_tB.setContentPath(m_req.getParameter("contentPath").trim());
      m_tB.setTagReportPath(m_req.getParameter("damPath").trim());
      m_tB.setReportName(m_req.getParameter("reportName").trim());
      
      Map<List<String>, List<Resource>> map = getAllRefs();
      
      writeExcel(map);
    }    
    catch(Exception e) 
    {
      printError(e);
      //TODO: Handle the exceptions here
      e.printStackTrace();
    }
    
    return;
    
  } //processTagsRequest
  
  private Map<List<String>, List<Resource>> getAllRefs() throws Exception 
  {
    Map<List<String>, List<Resource>> tagRefsMap = new HashMap<List<String>, List<Resource>>();
    
    try 
    {
      String tagPath   = m_tB.getTagPath();
      Resource res     = m_resolver.resolve(tagPath);
      
      //Parent Tag
      Tag tagNameSpace             = res.adaptTo(Tag.class);
      List<String> tagInfo         = Arrays.asList(tagNameSpace.getTagID(), tagNameSpace.getName());
      List<Resource> parentTagRefs = getRefsForTag(tagNameSpace);
      tagRefsMap.put(tagInfo, parentTagRefs);
      
      
      if(m_tB.isIncludeChildTags()) return tagRefsMap;
      
      //Child Tags
      tagNameSpace.listAllSubTags().forEachRemaining(childTag -> {
        List<String> childTagInfo  = Arrays.asList(childTag.getTagID(), childTag.getName());
        List<Resource> refs = getRefsForTag(childTag);
        tagRefsMap.put(childTagInfo, refs);
      });
      
    }
    catch(Exception e) 
    {
      printError(e);
      //TODO: Use your custom exception handling
    }
    
    return tagRefsMap;
    
  } //getAllRefs
  
  
  private void printError(Exception e) 
  {
    m_pw.println("Exception Occured While Processing. Please contact DEV TEAM");
    logger.info(Throwables.getStackTraceAsString(e));
    
  } //printError
  
  private List<Resource> getRefsForTag(Tag tag)
  {
    List<Resource>             refs = new ArrayList<Resource>();
    
    RangeIterator<Resource> tagRefs;
    if(m_tB.getContentPath() == null || m_tB.getContentPath() == "")
      tagRefs = m_tm.find(tag.getTagID());
    else tagRefs = m_tm.find(m_tB.getContentPath(), new String[] {tag.getTagID()});
    
    tagRefs.forEachRemaining(tagRef -> {
      try
      {
        if(isTagReferenced(tagRef, tag.getTagID())) refs.add(tagRef);
      }
      catch (RepositoryException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    
    return refs;
    
  } //getRefsForTag
  
  private boolean isTagReferenced(Resource resource, String tagId) throws RepositoryException 
  {
    ValueMap valMap = resource.getValueMap();
    if(!valMap.containsKey("cq:tags")) return false;
    
    String[] tags = (String[])valMap.get("cq:tags");
    for(String tag : tags) 
    {
      if(tag.equalsIgnoreCase(tagId)) return true;
    }
    
    return false;
    
  } //isTagReferenced
  
  private void writeExcel(Map<List<String>, List<Resource>> tagRefsMap) throws IOException 
  {
    Workbook workbook = new XSSFWorkbook();
    Sheet trSheet = workbook.createSheet("tags_report");
    setHeaderStyles(workbook, trSheet);
    
    AtomicInteger rowNo     = new AtomicInteger(1);
    tagRefsMap.forEach((k,v) -> {
      
      //If no references
      if(v.size() <= 0) 
      {
        Row row = trSheet.createRow(rowNo.get());
        row.createCell(0).setCellValue(k.get(1));
        row.createCell(1).setCellValue(k.get(0));
        
        //setRowStyles(workbook, row);
        
        rowNo.getAndIncrement();
        return;
      }
      
      int startRowNo = rowNo.get();
      //If tag has references
      IntStream.range(0, v.size()).forEach(i -> {
        Resource res = v.get(i);
        String resPath = res.getPath();
        
        if(resPath.startsWith("/content/dam")) resPath = resPath.replace("/jcr:content/metadata", "");
        else if(!resPath.startsWith("/content/dam") && resPath.startsWith("/content/")) resPath = resPath.replace("/jcr:content", ".html");
        
        Row row = trSheet.createRow(rowNo.get());
        Cell firstCell = row.createCell(0);
        setCellStyles(workbook, firstCell);
        firstCell.setCellValue(k.get(1));
        
        Cell secondCell = row.createCell(1);
        setCellStyles(workbook, secondCell);
        secondCell.setCellValue(k.get(0));
        
        row.createCell(2).setCellValue(resPath);
        try
        {
          if(isReplicated(res)) row.createCell(3).setCellValue("YES");
          else row.createCell(3).setCellValue("New Content/Never Replicated");
          
          if(hasInflightChanges(res)) row.createCell(4).setCellValue("true");
          else row.createCell(4).setCellValue("false");
        }
        catch (RepositoryException e)
        {
          printError(e);
          throw new RuntimeException("Failed to prepare the excel"); //TODO: Use your custom exception framework
        }
        
        rowNo.getAndIncrement();
        
      });
      
      if(rowNo.get() -1 > startRowNo) 
      {
        trSheet.addMergedRegion(new CellRangeAddress(startRowNo, rowNo.get() -1, 0, 0));
        trSheet.addMergedRegion(new CellRangeAddress(startRowNo, rowNo.get() -1, 1, 1));
        
      }
      
    });
    
    IntStream.range(0, columnNames.length).forEach(i -> {
      trSheet.autoSizeColumn(i);
    });
    
    
    
    uploadToDam(workbook);
    workbook.close();
    
    return;
  } //writeExcel
  
  private void setHeaderStyles(Workbook workbook, Sheet trSheet) 
  {
    CellStyle headStyles = workbook.createCellStyle();
    headStyles.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headStyles.setFillForegroundColor((short) 22);
    headStyles.setVerticalAlignment(VerticalAlignment.CENTER);
    
    
    //Create the Header ROW
    Row headerRow = trSheet.createRow(0);
    for(int i = 0; i < columnNames.length; i++)
    {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(columnNames[i]);
      cell.setCellStyle(headStyles);
    }
    
    return;
    
  } //setHeaderStyles
  
  private void setCellStyles(Workbook workbook, Cell cell) 
  {
    
    CellStyle rowStyles = workbook.createCellStyle();
    rowStyles.setVerticalAlignment(VerticalAlignment.CENTER);
    cell.setCellStyle(rowStyles);
    
    return;
    
  } //setHeaderStyles
  
  private boolean hasInflightChanges(Resource res) throws RepositoryException
  {
    ValueMap valMap = res.getValueMap();
    if(!valMap.containsKey("cq:lastReplicated")) return true;
    
    Calendar repTime  = valMap.get("cq:lastReplicated", Calendar.class);
    Calendar modTime  = valMap.get("cq:lastModified", Calendar.class);
    
    if(modTime.compareTo(repTime) > 0) return true;
    
    return false;
  } // hasInflightChanges
  
  private boolean isReplicated(Resource res) throws RepositoryException
  {
    ValueMap valMap = res.getValueMap();
    if(!valMap.containsKey("cq:lastReplicated")) return false;
    
    return true;
  } // isReplicated
  
  private void uploadToDam(Workbook workbook) throws IOException
  {
    File tempFile = File.createTempFile("temp", ".xlsx");
    
    FileOutputStream flOut = new FileOutputStream(tempFile);
    workbook.write(flOut);
    
    flOut.close();

    AssetManager assetMgr = m_resolver.adaptTo(AssetManager.class);
    String reportPath = m_tB.getTagReportPath() + "/" + m_tB.getReportName() + ".xlsx";
    assetMgr.createAsset(reportPath, new FileInputStream(tempFile), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true);
    
    tempFile.delete();
    
    m_pw.println("TAGS Report is uploaded here: " + reportPath);
    
    return;
    
  } // uploadToDam
  
} //TagsUtilProcessor
