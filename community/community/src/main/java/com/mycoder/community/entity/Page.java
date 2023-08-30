package com.mycoder.community.entity;

/**
 * package information about page
 */
public class Page {

    // current page number
    private int current = 1;
    // display upperbound
    private int limit = 10;
    // total number (to calculate the total pages)
    private int rows;
    // query path(to reuse the link of paging)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the starting row for current page
     * @return
     */
    public int getOffest(){
        // current * limit - limit
        return (current - 1) * limit;
    }

    /**
     * Get the total number of pages
     * @return
     */
    public int getTotal(){
        // rows / limit (+1)
        if(rows % limit == 0)
            return rows / limit;
        else
            return rows / limit + 1;
    }

    /**
     * get start page number
     * @return
     */
    public int getFrom(){
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     * get end page number
     * @return
     */
    public int getTo(){
        int to = current + 2;
        int total = getTotal();
        return Math.min(to, total);
    }
}
