/**
 * Tags Util Component
 *
 * Created by Singaiah Chintalapudi
 */
package com.aem.tags.core.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;

@Model(adaptables=Resource.class)
public class TagsUtil 
{

    @Inject @Named("tagsPath") @Default(values="")
    protected String tagsLoc;
    
    @Inject @Named("contentPath") @Default(values="")
    protected String contentLoc;
    
    @Inject @Named("excludeSubTags") @Default(values="")
    protected String subTags;
    
    @Inject @Named("damPath") @Default(values="")
    protected String damLoc;
    
    @Inject @Named("reportName") @Default(values="")
    protected String reportLabel;
    

    @PostConstruct
    protected void init() 
    {
        //TODO: Post-processing if needed
    }
    
    public String getTagsPath() 
    {
      return tagsLoc;
    } //getTagsPath
    
    public String getContentPath() 
    {
      return contentLoc;
    } //getTagsPath
    
    public String getExcludeSubTags() 
    {
      return subTags;
    } //getTagsPath
    
    public String getDamPath() 
    {
      return damLoc;
    } //getTagsPath
    
    public String getReportName() 
    {
      return reportLabel;
    } //getTagsPath

} //TagsUtil
