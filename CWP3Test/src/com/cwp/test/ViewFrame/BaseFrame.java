package com.cwp.test.ViewFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yc on 2018/3/14.
 */
public class BaseFrame extends JInternalFrame {

    public JPanel centerPanel;
    public JPanel contentPanel;
    public JScrollPane scrollPane;
    public JTable table;
    public MyTableModel tableModel;

    private Field[] fields;
    private List contentList;
    private Class contentClass;
    private List columnList;

    public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public BaseFrame(String title, List<String> inputColumnList, Class inputClass, List inputList) {
        super(title, true, false, true, true);
        this.columnList = inputColumnList;
        this.contentClass = inputClass;
        this.contentList = inputList;
        this.fields = contentClass.getDeclaredFields();
        initComponents();
    }

    public BaseFrame(String title, Class inputClass, List inputList) {
        super(title, true, false, true, true);
        this.contentClass = inputClass;
        this.contentList = inputList;
        this.fields = contentClass.getDeclaredFields();
        this.columnList = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            columnList.add(fields[i].getName());
        }
        initComponents();
        //listener
        initListeners();
    }

    private void initComponents() {
        setSize(GlobalData.width - 50, GlobalData.height - 200);
        centerPanel = new JPanel();
        centerPanel.setBorder(new TitledBorder(null, title,
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        this.setLayout(new BorderLayout(0, 0));
        this.add(contentPanel, BorderLayout.CENTER);

        centerPanel.setLayout(new BorderLayout(0, 0));
        scrollPane = new JScrollPane();
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        scrollPane.setViewportView(table);
        tableModel = new MyTableModel();
        for (int i = 0; i < fields.length; i++) {
            tableModel.addColumn(columnList.get(i), fields[i].getType());
        }
        table.setDefaultRenderer(Date.class, new MyDateTableCellRenderer());
        table.setAutoCreateRowSorter(true);
        table.setModel(tableModel);
        refreshTableData();
    }

    private void initListeners() {
        GlobalData.addIValueChangeListener(new IValueChangeListener() {
            @Override
            public void valueChanged() {
//                System.out.println("VesselCode Changed!");
                refreshTableData();
            }
        });
    }

    private void refreshTableData() {
//        System.out.println("******refresh Table Data********" + this.title);
        String vesselCode = GlobalData.selectedVesselCode;
        Long berthId = GlobalData.selectedBerthId;
//        System.out.println("vesselCode:" + vesselCode + " berthId:" + berthId);

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
//            System.out.println("remove column " + i);
        }
        for (Object o : contentList) {
            boolean matchVesselCode = true;
            boolean matchBerthId = true;
            Object[] rowData = new Object[fields.length];
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                Object value = getFieldValueByName(field.getName(), o);
                rowData[i] = value;
//                System.out.println("field name:" + field.getName() + "  value:" + value);
                if (field.getName().equalsIgnoreCase("vesselCode")) {
                    if (vesselCode != null && vesselCode != "") {
                        if (!vesselCode.equals(value)) {
                            matchVesselCode = false;
                        }
                    }
                }
                if (field.getName().equalsIgnoreCase("berthId")) {
                    if (berthId != null) {
                        if (!berthId.equals(value)) {
                            matchBerthId = false;
                        }
                    }
                }
            }
//            System.out.println("matchVesselCode:" + matchVesselCode + " matchBerthId:" + matchBerthId);
            if (matchBerthId && matchVesselCode) {
                tableModel.addRow(rowData);
            }
        }
    }

    private Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method getterMethod = null;
            Method[] methods = o.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(getter)) {
                    getterMethod = method;
                }
            }
            Object value = getterMethod.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            System.out.println("属性对应的Getter方法不存在");
            return null;
        }
    }

    class MyDateTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Date date = (Date) value;
            if (date != null) {
                setText(sdf.format(date));
            } else {
                setText("");
            }
            return this;
        }
    }

    class MyTableModel extends DefaultTableModel {

        private List<Class> colTypes = new ArrayList<Class>();

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class getColumnClass(int c) {
            return colTypes.get(c);
        }

        public void addColumn(Object columnName, Class columnType) {
            this.addColumn(columnName);
            this.colTypes.add(columnType);
        }
    }

}
