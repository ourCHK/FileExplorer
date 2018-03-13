package com.chk.fileexplorer.Beans;

/**
 * Created by chk on 18-3-13.
 * use for store the information of file or folder;
 */

public class FileBean {
    private String name;
    private long size;
    private boolean isFolder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        this.isFolder = folder;
    }
}
