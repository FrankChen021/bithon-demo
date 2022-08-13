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

package org.bithon.demo.user.service.mongo;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author frank.chen021@outlook.com
 * @date 2022/8/12 19:11
 */
@Service
@EnableMongoRepositories
public class LogService {
    private final LogRepository repository;

    public LogService(LogRepository repository) {
        this.repository = repository;
    }

    public void addLog(String user, String message) {
        try {
            this.repository.save(LogDocument.builder()
                                            .id(UUID.randomUUID().toString())
                                            .timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())))
                                            .user(user)
                                            .message(message)
                                            .build());
        } catch (Exception ignored) {
        }
    }

    public List<String> getLogs() {
        return this.repository.findAll()
                              .stream()
                              .map((logDocument) -> String.format("%s %s : %s", logDocument.getTimestamp(), logDocument.getUser(), logDocument.getMessage()))
                              .collect(Collectors.toList());
    }
}
