package com.expensesplitter.util;

import com.expensesplitter.models.User;

public final class Session {

    private static User currentUser;

    private Session() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}
