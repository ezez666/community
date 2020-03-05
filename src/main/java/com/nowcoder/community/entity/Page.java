package com.nowcoder.community.entity;

public class Page {
    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数(用于计算总页数)
    private int rows;
    // 查询路径(用于复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=1){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *获取当前页的起始行
     * @return
     */
    public int getOffset(){
        return this.limit*(this.current-1);
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        if(rows%this.limit==0){
            return rows/this.limit;
        }else{
            return rows/this.limit+1;
        }
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        int from = this.current-2;
        return from<1?1:from;
    }

    /**
     * 获取结尾页码
     * @return
     */
    public int getTo(){
        int to = this.current+2;
        return to>this.getTotal()?this.getTotal():to;
    }



    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }
}
