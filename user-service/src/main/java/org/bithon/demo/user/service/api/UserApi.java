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

import org.bithon.demo.user.api.ChangePasswordRequest;
import org.bithon.demo.user.api.RegisterUserRequest;
import org.bithon.demo.user.api.RegisterUserResponse;
import org.bithon.demo.user.api.GetProfileResponse;
import org.bithon.demo.user.api.IUserApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApi implements IUserApi {

    @Override
    public RegisterUserResponse register(RegisterUserRequest request) {
        return RegisterUserResponse.builder().uid("1").build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    @Override
    public GetProfileResponse getProfileRequest(String uid) {
        return null;
    }
}
