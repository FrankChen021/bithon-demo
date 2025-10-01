/*
 *    Copyright 2020 bithon.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bithon.demo.user.service.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import org.bithon.agent.sdk.tracing.ISpanScope;
import org.bithon.agent.sdk.tracing.ITraceScope;
import org.bithon.agent.sdk.tracing.TraceContext;
import org.bithon.demo.user.api.ChangePasswordRequest;
import org.bithon.demo.user.api.GetProfileResponse;
import org.bithon.demo.user.api.IUserApi;
import org.bithon.demo.user.api.RegisterUserRequest;
import org.bithon.demo.user.api.RegisterUserResponse;
import org.bithon.demo.user.service.db.UserDao;
import org.bithon.demo.user.service.db.jooq.tables.pojos.User;
import org.bithon.demo.user.service.event.IEventPublisher;
import org.bithon.demo.user.service.mongo.LogService;
import org.bithon.demo.user.service.redis.RedisCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserApi implements IUserApi {

    private final UserDao userDao;
    private final RedisCache cache;

    private final LogService logService;
    private final IEventPublisher eventPublisher;

    public UserApi(UserDao userDao,
                   RedisCache cache,
                   LogService logService,
                   IEventPublisher eventPublisher) {
        this.userDao = userDao;
        this.cache = cache;
        this.logService = logService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        Long uid;

        // Use the SDK API to create a span
        try (ISpanScope span = TraceContext.newScopedSpan("dao#create")
                                           .create()) {

            // business code
            uid = userDao.create(request.getUserName(), request.getPassword());
            if (uid == null) {
                return RegisterUserResponse.builder().error(String.format("User [%s] exists.", request.getUserName())).build();
            }
        }

        logService.addLog(request.getUserName(), "REGISTER");

        //
        // Use the SDK API to create a trace for async processing
        //
        String traceId = TraceContext.currentTraceId();
        String parentId = TraceContext.currentSpanId();
        new Thread(() -> {
            try (ITraceScope traceScope = TraceContext.newTrace("event#publish")
                                                      .parent(traceId, parentId)
                                                      .attach()) {
                // Update span info(optional)
                traceScope.currentSpan()
                          .method(eventPublisher.getClass(), "publishEvent");

                // simulate some processing time
                try (ISpanScope span = TraceContext.newScopedSpan("sleepBeforePublish")
                                                   .create()) {

                    // simulate event serialization time
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // business code
                eventPublisher.publishEvent("REGISTER");
            }
        }).start();

        return RegisterUserResponse.builder().uid(uid.toString()).build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        if (!userDao.setPassword(request.getUserName(), request.getOldPassword(), request.getNewPassword())) {
            throw new RuntimeException("User not exist or wrong password");
        }
    }

    @Override
    public GetProfileResponse getProfile(String uid) {
        User user = cache.get(uid, Duration.ofMinutes(1), User.class, () -> userDao.getUser(Long.parseLong(uid)));
        return GetProfileResponse.builder()
                                 .name(user.getName())
                                 .build();
    }

    @Override
    public GetProfileResponse getProfile(String userName, String password) {
        if ("Frank".equals(userName)) {
            return GetProfileResponse.builder()
                                     .name(userName)
                                     .build();
        }
        return GetProfileResponse.builder().name(null).build();
    }

    @Override
    public void unregister(ArrayList<String> uids) {
        userDao.unregister(uids.stream().map(Long::parseLong).collect(Collectors.toList()));

        cache.remove(uids);
    }

    @Override
    public List<String> showLogs() {
        return logService.getLogs();
    }

    @Data
    @Builder
    static class ExceptionEntity {
        private String message;
        private String stackTrace;
        private String exception;
        private String url;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionEntity> exceptionHandler(HttpServletRequest req, Exception ex) {
        return new ResponseEntity<>(ExceptionEntity.builder()
                                                   .url(req.getRequestURI())
                                                   .exception(ex.getClass().getName())
                                                   .message(ex.getMessage())
                                                   .stackTrace(ex.toString())
                                                   .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
