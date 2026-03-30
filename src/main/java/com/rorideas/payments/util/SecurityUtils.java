package com.rorideas.payments.util;

import com.rorideas.payments.dto.UserSessionModel;
import com.rorideas.payments.enums.UserRol;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Utility class for security-related operations, such as retrieving the user's time zone from the security context.
 * This class provides methods to access security information and perform common security-related tasks.
 */
@Slf4j
@UtilityClass
public class SecurityUtils {

    /**
     * Retrieves the user's ID from the security context. If the user ID is not available, it defaults to 0L.
     *
     * @return the user's ID as a Long
     */
    public static Long getUserId() {
        UserSessionModel userSession = (UserSessionModel) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (Objects.nonNull(userSession.getUserId())) {
            return userSession.getUserId().longValue();
        }
        return 0L;
    }

    /**
     * Retrieves the user's email from the security context. If the email is not available, it defaults to an empty string.
     *
     * @return the user's email as a String
     */
    public static String getUserEmail() {
        UserSessionModel userSession = (UserSessionModel) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (Objects.nonNull(userSession.getEmail())) {
            return userSession.getEmail();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Retrieves the user's role from the security context. If the role is not available, it defaults to null.
     *
     * @return the user's role as a UserRol enum
     */
    public UserRol getUserRol() {
        UserSessionModel userSession = (UserSessionModel) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (Objects.nonNull(userSession.getRole())) {
            return userSession.getRole();
        }
        return null;
    }

    /**
     * Retrieves the user's time zone from the security context. If the time zone is not available, it defaults to UTC.
     *
     * @return the user's time zone as a ZoneId
     */
    public static ZoneId getUserZone() {
        UserSessionModel userSession = (UserSessionModel) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (Objects.nonNull(userSession.getZone())) {
            return ZoneId.of(userSession.getZone());
        }

        return ZoneOffset.UTC;
    }
}
