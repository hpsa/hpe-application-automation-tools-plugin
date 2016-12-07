package com.hp.mqm.atrf.alm.services.querybuilder;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by berkovir on 06/12/2016.
 */
public class QueryBuilder implements Serializable {


    private StringBuilder sb;
    private Integer pageSize;
    private Integer startIndex;
    private List<String> selectedFields;
    private List<String> orderBy;
    private Map<String, String> queryConditions;

    public static final String PREPARED_FILTER = "_PREPARED_FILTER_";

    public static QueryBuilder create() {
        return new QueryBuilder();
    }

    public QueryBuilder addQueryConditions(Map<String, String> conditions) {
        if (conditions != null && !conditions.isEmpty()) {
            if (this.queryConditions == null) {
                this.queryConditions = new HashMap<>();
            }
            this.queryConditions.putAll(conditions);
        }
        return this;
    }

    public QueryBuilder addQueryCondition(String field, String value) {
        if (queryConditions == null) {
            queryConditions = new HashMap<>();
        }
        queryConditions.put(field, value);
        return this;
    }

    public QueryBuilder addStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public QueryBuilder addPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public QueryBuilder addSelectedFields(String... fieldNames) {
        return addSelectedFields(Arrays.asList(fieldNames));
    }

    public QueryBuilder addSelectedFields(Collection<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            if (selectedFields == null) {
                selectedFields = new ArrayList<>();
            }
            selectedFields.addAll(fieldNames);
        }
        return this;
    }

    public QueryBuilder addOrderBy(String... fieldNames) {
        return addOrderBy(Arrays.asList(fieldNames));
    }

    public QueryBuilder addOrderBy(Collection<String> fieldNames) {
        if (fieldNames != null && !fieldNames.isEmpty()) {
            if (orderBy == null) {
                orderBy = new ArrayList<>();
            }
            orderBy.addAll(fieldNames);
        }
        return this;
    }

    public String build() {
        sb = new StringBuilder();
        buildPageSize();
        buildStartIndex();
        buildQuery();
        buildSelectedFields();
        buildOrderBy();
        return sb.toString();
    }

    private void buildSelectedFields() {
        if (selectedFields != null && !selectedFields.isEmpty()) {
            sb.append("&").append("fields=").append(StringUtils.join(selectedFields, ","));
        }
    }

    private void buildQuery() {
        if (queryConditions != null && !queryConditions.isEmpty()) {
            String splitter = "";
            sb.append("&").append("query={");
            if (queryConditions != null) {
                for (Map.Entry<String, String> entry : queryConditions.entrySet()) {
                    sb.append(splitter);
                    if (entry.getKey().equals(PREPARED_FILTER)) {
                        sb.append(entry.getValue());
                    } else {
                        //if value contains spaces (for example A OR B), spaces should be converted to '+',
                        //if value wrapped with ' , vor example 'Not Completed', it should be handled as is
                        String value = entry.getValue().startsWith("'") ? entry.getValue() : entry.getValue().replaceAll(" ", "+");
                        sb.append(entry.getKey()).append("[").append(value).append("]");
                        splitter = ";";
                    }

                }
            }

            sb.append("}");
        }
    }

    private void buildStartIndex() {
        if (startIndex != null) {
            sb.append("&").append("start-index=").append(startIndex);
        }
    }

    private void buildPageSize() {
        if (pageSize != null) {
            sb.append("&").append("page-size=").append(pageSize);
        }
    }

    private void buildOrderBy() {
        if (orderBy != null && !orderBy.isEmpty()) {
            sb.append("&").append("order-by=").append("{").append(StringUtils.join(orderBy, ",")).append("}");
        }
    }


    public Map<String, String> getQueryConditions() {
        return queryConditions;
    }

    public QueryBuilder clone() {
        QueryBuilder qb = (QueryBuilder) SerializationUtils.clone(this);
        return qb;
    }
}
