package com.blog.application.blog.helpers.params;

public class TagQueryClauses {
    // Field names
    public static final String ID = "id";
    public static final String NAME = "name";

    // Query parts

    public static final String EQUALS_CONDITION = " = :";
    public static final String WHERE_CONDITION = " WHERE 1=1";
    public static final String SELECT_T_FROM_TAG = "SELECT t FROM Tag t";
    public static final String AND_WITH_T_ALIAS = " AND t.";


}
