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

package org.bithon.demo.user.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;

public interface IUserApi {

    @PostMapping("/api/user/register")
    RegisterUserResponse register(@RequestBody RegisterUserRequest request);

    @PostMapping("/api/user/changePassword")
    void changePassword(@RequestBody ChangePasswordRequest request);

    /**
     * An HTTP interface that accepts a variable on the path.
     * This will demonstrate how Bithon folds all request path for different users into one for the metrics
     */
    @GetMapping("/api/user/getProfile/{uid}")
    GetProfileResponse getProfileRequest(@PathVariable("uid") String uid);

    @PostMapping("/api/user/unregister")
    void unregister(ArrayList<String> uids);
}
