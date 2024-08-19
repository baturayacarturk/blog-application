package com.blog.application.blog.helpers.params;

public class PostQueryClauses {
    // Field names
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String USER_ID = "userId";
    public static final String TAG_NAME ="name";

    // Query parts
    public static final String USER_CONDITION = "p.user.";
    public static final String TAG_CONDITION = "t.";
    public static final String EQUALS_CONDITION = " = :";
    public static final String AND = " AND ";
    public static final String AND_WITH_P_ALIAS = " AND p.";
    public static final String LEFT_JOIN_TAGS = "LEFT JOIN p.tags t";
    public static final String WHERE_CONDITION = " WHERE 1=1";
    public static final String SELECT_DISTINCT_P_FROM_POST = "SELECT DISTINCT p FROM Post p ";

}
