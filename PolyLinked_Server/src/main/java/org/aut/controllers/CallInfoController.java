package org.aut.controllers;

import org.aut.dataAccessors.CallInfoAccessor;
import org.aut.dataAccessors.ConnectAccessor;
import org.aut.dataAccessors.UserAccessor;
import org.aut.models.CallInfo;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.aut.utils.exceptions.UnauthorizedException;

import java.sql.SQLException;

public class CallInfoController {

    public static void addCallInfo(CallInfo callInfo) throws SQLException, NotAcceptableException, NotFoundException {
        if (!UserController.userExistsById(callInfo.getUserId())) {
            throw new NotFoundException("User not found");
        } else if (CallInfoAccessor.callInfoExists(callInfo.getUserId())) {
            throw new NotAcceptableException("CallInfo already exists");
        } else {
            CallInfoAccessor.addCallInfo(callInfo);
        }
    }

    public static void updateCallInfo(CallInfo callInfo) throws SQLException, NotFoundException {
        if (!CallInfoAccessor.callInfoExists(callInfo.getUserId())) {
            throw new NotFoundException("CallInfo not found");
        } else {
            CallInfoAccessor.updateCallInfo(callInfo);
        }
    }

    public static void deleteCallInfo(String userId) throws SQLException, NotFoundException {
        if (!CallInfoAccessor.callInfoExists(userId)) {
            throw new NotFoundException("CallInfo not found");
        } else {
            CallInfoAccessor.deleteCallInfo(userId);
        }
    }

    public static CallInfo getCallInfo(String userId, String requesterId) throws SQLException, NotFoundException, NotAcceptableException, UnauthorizedException {
        CallInfo callInfo =  CallInfoAccessor.getCallInfoByUserId(userId);;

        String privacyLevel = callInfo.getPrivacyPolitics();
        boolean isInNetwork = ConnectAccessor.userIsInNetworkOf(requesterId, userId);
        boolean isConnected = ConnectAccessor.usersIsConnected(userId, requesterId);
        if (userId.equals(requesterId)) return callInfo;

        if (privacyLevel.equals(CallInfo.PrivacyPolitics.ONLY_ME.toString()) ||
                privacyLevel.equals(CallInfo.PrivacyPolitics.MY_CONNECTIONS.toString()) && !isConnected ||
                privacyLevel.equals(CallInfo.PrivacyPolitics.FURTHER_CONNECTIONS.toString()) && !isInNetwork) {
            throw new UnauthorizedException("Requester not allowed");
        } else {
            return callInfo;
        }
    }
}
