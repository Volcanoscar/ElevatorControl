package com.inovance.elevatorcontrol.models;

/**
 * Created by Daniel on 2015/7/10.
 */
public class SlideMenuItem {
    private String name;
    private Integer imageId;
    private Class fragmentClass;

    public SlideMenuItem(String name, Integer imageId, Class fragmentClass)
    {
        this.name = name;
        this.imageId = imageId;
        this.fragmentClass = fragmentClass;
    }

    public String getName()
    {
        return name;
    }

    public Integer getImageId()
    {
        return imageId;
    }

    public Class getFragmentClass() {return fragmentClass;}
}
