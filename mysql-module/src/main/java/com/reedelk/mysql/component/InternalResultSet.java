package com.reedelk.mysql.component;

import java.util.ArrayList;
import java.util.List;

public class InternalResultSet {

    private final List<String> columnNames = new ArrayList<>();
    private List<List<Object>> data = new ArrayList<>();

    public InternalResultSet(List<String> columnNames) {
        this.columnNames.addAll(columnNames);
    }

    public void add(List<Object> data) {
        this.data.add(data);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<Object>> getData() {
        return data;
    }
}
