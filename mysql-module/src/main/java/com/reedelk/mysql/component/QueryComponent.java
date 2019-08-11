package com.reedelk.mysql.component;

import com.reedelk.runtime.api.annotation.Default;
import com.reedelk.runtime.api.annotation.ESBComponent;
import com.reedelk.runtime.api.annotation.Property;
import com.reedelk.runtime.api.annotation.Required;
import com.reedelk.runtime.api.component.ProcessorSync;
import com.reedelk.runtime.api.message.Message;
import com.reedelk.runtime.api.message.type.MimeType;
import com.reedelk.runtime.api.message.type.Type;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ESBComponent("Database Query")
@Component(service = QueryComponent.class, scope = ServiceScope.PROTOTYPE)
public class QueryComponent implements ProcessorSync {

    @Property("Username")
    @Required
    private String username;

    @Property("Password")
    @Required
    private String password;

    @Property("Query")
    @Required
    private String query;

    @Property("Database URL")
    @Required
    @Default("jdbc:mysql://localhost/mydatabase")
    private String databaseURL;

    @Override
    public Message apply(Message input) {
        try (Connection conn = DriverManager.getConnection(databaseURL, username, password);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            InternalResultSet map = map(rs);

            Type contentType = new Type(MimeType.APPLICATION_JAVA, InternalResultSet.class);

            // TODO: Fixme
            //TypedContent newContent = new MemoryTypedContent<>(map, contentType);

            Message output = new Message();

            // output.setTypedContent(newContent);

            return output;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    private static InternalResultSet map(ResultSet resultSet) throws SQLException {
        List<String> columnNames = collectColumnNames(resultSet);
        InternalResultSet internalResultSet = new InternalResultSet(columnNames);
        while (resultSet.next()) {
            List<Object> values = new ArrayList<>();
            for (int i = 1; i <= columnNames.size(); i++) {
                Object value = resultSet.getObject(i);
                values.add(value);
            }
            internalResultSet.add(values);
        }
        return internalResultSet;
    }

    private static List<String> collectColumnNames(ResultSet resultSet) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        return columnNames;
    }
}