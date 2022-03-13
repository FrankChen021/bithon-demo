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

package org.bithon.demo.user.service.db;

import lombok.extern.slf4j.Slf4j;
import org.bithon.demo.user.service.db.jooq.Tables;
import org.bithon.demo.user.service.db.jooq.tables.pojos.User;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author frank.chen021@outlook.com
 * @date 2021/3/21 20:20
 */
@Slf4j
@Component
public class UserDao {

    public UserDao(DSLContext dslContext) {
        this.dslContext = dslContext;

        dslContext.createTableIfNotExists(Tables.USER)
                  .columns(Tables.USER.fields())
                  .indexes(Tables.USER.getIndexes())
                  .execute();
    }

    public User getUser(long uid) {
        return this.dslContext.selectFrom(Tables.USER).where(Tables.USER.ID.eq(uid)).fetchOneInto(User.class);
    }

    public Long create(String userName, String password) {
        Record1<Long> id = this.dslContext.insertInto(Tables.USER)
                                          .set(Tables.USER.NAME, userName)
                                          .set(Tables.USER.PASSWORD, password)
                                          .onDuplicateKeyIgnore()
                                          .returningResult(Tables.USER.ID)
                                          .fetchOne();
        return id == null ? null : (Long) id.get(0);
    }

    public boolean setPassword(String userName, String oldPassword, String newPassword) {
        return this.dslContext.update(Tables.USER)
                              .set(Tables.USER.PASSWORD, newPassword)
                              .where(Tables.USER.NAME.eq(userName))
                              .and(Tables.USER.PASSWORD.eq(oldPassword))
                              .execute() > 0;
    }

    private final DSLContext dslContext;

    public void unregister(List<Long> uids) {
        this.dslContext.deleteFrom(Tables.USER)
                       .where(Tables.USER.ID.in(uids))
                       .execute();
    }
}
