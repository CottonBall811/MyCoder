package com.mycoder.community.util;

public interface CommunityConstant {

    /**
     * activate successfully
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * repeat activate
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * activate fail;
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * default expired time for login ticket
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * rememberme status expired time for login ticket
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

}
